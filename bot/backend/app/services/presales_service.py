"""售前客服集成服务 - 串联意图识别→FAQ匹配→知识库检索→产品推荐→答案生成"""

import json
import time
from typing import Any, AsyncGenerator, Dict, List, Optional

from loguru import logger
from sqlalchemy.orm import Session

from app.intent.classifier import IntentClassifier
from app.intent.faq_matcher import FAQMatcher
from app.llm.llm_service import LLMService
from app.llm.prompts.templates import PromptTemplates
from app.models.chat import ChatMessage, ChatSession, QALog
from app.rag.retrieval.hybrid_retriever import HybridRetriever
from app.recommend.engine import RecommendEngine


class PresalesService:
    """售前客服集成服务

    串联完整的售前客服对话流程：
    1. FAQ匹配（快速响应）
    2. 意图识别
    3. 知识库检索
    4. 产品推荐（根据意图）
    5. LLM生成答案
    6. 记录日志
    """

    def __init__(
        self,
        db_session: Session,
        llm_service: LLMService,
        hybrid_retriever: HybridRetriever,
        intent_classifier: IntentClassifier,
        faq_matcher: FAQMatcher,
        recommend_engine: RecommendEngine,
    ) -> None:
        """初始化售前客服服务

        Args:
            db_session: 数据库会话
            llm_service: LLM服务实例
            hybrid_retriever: 混合检索器实例
            intent_classifier: 意图分类器实例
            faq_matcher: FAQ匹配器实例
            recommend_engine: 推荐引擎实例
        """
        self.db = db_session
        self.llm_service = llm_service
        self.hybrid_retriever = hybrid_retriever
        self.intent_classifier = intent_classifier
        self.faq_matcher = faq_matcher
        self.recommend_engine = recommend_engine

    async def chat(
        self,
        query: str,
        session_id: int,
        kb_ids: List[int],
        history: Optional[List[Dict[str, Any]]] = None,
    ) -> Dict[str, Any]:
        """售前客服对话

        完整流程：FAQ匹配 → 意图识别 → 知识库检索 → 产品推荐 → LLM生成答案

        Args:
            query: 用户查询文本
            session_id: 会话ID
            kb_ids: 知识库ID列表
            history: 历史对话消息列表，格式为[{"role": "user/assistant", "content": "..."}]

        Returns:
            对话结果字典，包含:
                - answer: 回答内容
                - intent: 意图识别结果
                - sources: 检索到的知识来源
                - recommended_products: 推荐产品列表
                - faq_matched: FAQ匹配结果（如有）
        """
        start_time = time.time()

        # 初始化结果
        result: Dict[str, Any] = {
            "answer": "",
            "intent": None,
            "sources": [],
            "recommended_products": [],
            "faq_matched": None,
        }

        # 1. FAQ匹配（快速响应）
        try:
            kb_id_strs = [str(kid) for kid in kb_ids]
            faq_match = await self.faq_matcher.match(query, kb_id_strs)
            if faq_match:
                result["faq_matched"] = faq_match
                result["answer"] = faq_match["answer"]
                logger.info(f"FAQ匹配成功，直接返回FAQ答案: score={faq_match['score']}")
        except Exception as e:
            logger.warning(f"FAQ匹配异常: {e}")

        # 2. 意图识别
        try:
            intent = await self.intent_classifier.classify(query)
            result["intent"] = intent
        except Exception as e:
            logger.warning(f"意图识别异常，使用默认意图: {e}")
            intent = {"intent_type": "general", "confidence": 0.0, "keywords": [], "sub_intent": None}
            result["intent"] = intent

        # 如果FAQ已匹配且置信度高，可以跳过后续步骤直接返回
        if result["faq_matched"] and result["answer"]:
            # 但仍然进行产品推荐（FAQ答案可能需要补充产品推荐）
            try:
                recommended = await self.recommend_engine.recommend(query, intent, top_k=3)
                result["recommended_products"] = recommended
            except Exception as e:
                logger.warning(f"产品推荐异常: {e}")

            # 记录日志
            self._save_chat_log(
                session_id=session_id,
                query=query,
                answer=result["answer"],
                kb_id=kb_ids[0] if kb_ids else None,
                sources=result["sources"],
                intent=intent,
                response_time=int((time.time() - start_time) * 1000),
            )

            return result

        # 3. 知识库检索
        context = ""
        sources: List[Dict[str, Any]] = []
        try:
            kb_id_strs = [str(kid) for kid in kb_ids]
            retrieve_results = await self.hybrid_retriever.retrieve(
                query=query,
                kb_ids=kb_id_strs,
                top_k=5,
            )
            # 拼接检索到的上下文
            context_parts: List[str] = []
            for doc in retrieve_results:
                content = doc.get("content", "")
                if content:
                    context_parts.append(content)
                    sources.append({
                        "chunk_id": doc.get("chunk_id"),
                        "content": content[:200],
                        "score": doc.get("rerank_score", doc.get("score", 0)),
                        "kb_id": doc.get("kb_id"),
                    })

            context = "\n\n".join(context_parts)
            result["sources"] = sources
            logger.info(f"知识库检索完成: 获取 {len(sources)} 条相关文档")
        except Exception as e:
            logger.warning(f"知识库检索异常: {e}")

        # 4. 产品推荐（根据意图）
        try:
            recommended = await self.recommend_engine.recommend(query, intent, top_k=3)
            result["recommended_products"] = recommended
        except Exception as e:
            logger.warning(f"产品推荐异常: {e}")

        # 5. LLM生成答案
        try:
            # 构建增强的上下文（知识库检索结果 + 产品推荐信息）
            enhanced_context = self._build_enhanced_context(
                context=context,
                recommended_products=result["recommended_products"],
                intent=intent,
            )

            # 构建系统提示词
            system_prompt = self._build_presales_prompt(intent, result["recommended_products"])

            answer = await self.llm_service.chat_with_context(
                query=query,
                context=enhanced_context,
                history=history,
                system_prompt=system_prompt,
                temperature=0.7,
            )
            result["answer"] = answer
        except Exception as e:
            logger.error(f"LLM生成答案失败: {e}")
            # 降级：返回检索到的上下文或默认回复
            if context:
                result["answer"] = f"根据知识库信息：\n{context[:500]}"
            else:
                result["answer"] = "抱歉，我暂时无法回答您的问题，请稍后再试或联系人工客服。"

        # 6. 记录日志
        response_time = int((time.time() - start_time) * 1000)
        self._save_chat_log(
            session_id=session_id,
            query=query,
            answer=result["answer"],
            kb_id=kb_ids[0] if kb_ids else None,
            sources=sources,
            intent=intent,
            response_time=response_time,
        )

        logger.info(
            f"售前客服对话完成: intent={intent.get('intent_type')}, "
            f"sources={len(sources)}, "
            f"products={len(result['recommended_products'])}, "
            f"time={response_time}ms"
        )

        return result

    async def chat_stream(
        self,
        query: str,
        session_id: int,
        kb_ids: List[int],
        history: Optional[List[Dict[str, Any]]] = None,
    ) -> AsyncGenerator[Dict[str, Any], None]:
        """流式售前客服对话

        分步骤流式返回结果：
        1. 先返回意图识别结果
        2. 再返回检索到的来源
        3. 再返回推荐产品
        4. 最后流式输出答案

        Args:
            query: 用户查询文本
            session_id: 会话ID
            kb_ids: 知识库ID列表
            history: 历史对话消息列表

        Yields:
            分步骤的结果字典，包含type字段标识当前步骤
        """
        start_time = time.time()

        # 1. FAQ匹配（快速响应）
        faq_match = None
        try:
            kb_id_strs = [str(kid) for kid in kb_ids]
            faq_match = await self.faq_matcher.match(query, kb_id_strs)
        except Exception as e:
            logger.warning(f"FAQ匹配异常: {e}")

        # 2. 意图识别
        intent: Dict[str, Any] = {"intent_type": "general", "confidence": 0.0, "keywords": [], "sub_intent": None}
        try:
            intent = await self.intent_classifier.classify(query)
        except Exception as e:
            logger.warning(f"意图识别异常: {e}")

        # 发送意图识别结果
        yield {
            "type": "intent",
            "data": intent,
        }

        # 如果FAQ匹配成功，直接返回FAQ答案
        if faq_match:
            yield {
                "type": "faq_matched",
                "data": faq_match,
            }

        # 3. 知识库检索
        context = ""
        sources: List[Dict[str, Any]] = []
        try:
            kb_id_strs = [str(kid) for kid in kb_ids]
            retrieve_results = await self.hybrid_retriever.retrieve(
                query=query,
                kb_ids=kb_id_strs,
                top_k=5,
            )
            context_parts: List[str] = []
            for doc in retrieve_results:
                content = doc.get("content", "")
                if content:
                    context_parts.append(content)
                    sources.append({
                        "chunk_id": doc.get("chunk_id"),
                        "content": content[:200],
                        "score": doc.get("rerank_score", doc.get("score", 0)),
                        "kb_id": doc.get("kb_id"),
                    })
            context = "\n\n".join(context_parts)
        except Exception as e:
            logger.warning(f"知识库检索异常: {e}")

        # 发送检索来源
        yield {
            "type": "sources",
            "data": sources,
        }

        # 4. 产品推荐
        recommended_products: List[Dict[str, Any]] = []
        try:
            recommended_products = await self.recommend_engine.recommend(query, intent, top_k=3)
        except Exception as e:
            logger.warning(f"产品推荐异常: {e}")

        # 发送推荐产品
        yield {
            "type": "recommended_products",
            "data": recommended_products,
        }

        # 5. 流式输出答案
        if faq_match and faq_match.get("answer"):
            # FAQ已匹配，直接输出FAQ答案
            yield {
                "type": "answer_chunk",
                "data": faq_match["answer"],
            }
            answer = faq_match["answer"]
        else:
            # LLM流式生成答案
            answer = ""
            try:
                enhanced_context = self._build_enhanced_context(
                    context=context,
                    recommended_products=recommended_products,
                    intent=intent,
                )
                system_prompt = self._build_presales_prompt(intent, recommended_products)

                async for token in self.llm_service.chat_stream_with_context(
                    query=query,
                    context=enhanced_context,
                    history=history,
                    system_prompt=system_prompt,
                    temperature=0.7,
                ):
                    answer += token
                    yield {
                        "type": "answer_chunk",
                        "data": token,
                    }
            except Exception as e:
                logger.error(f"LLM流式生成失败: {e}")
                error_msg = "抱歉，我暂时无法回答您的问题，请稍后再试或联系人工客服。"
                yield {
                    "type": "answer_chunk",
                    "data": error_msg,
                }
                answer = error_msg

        # 6. 发送完成信号
        response_time = int((time.time() - start_time) * 1000)
        yield {
            "type": "done",
            "data": {
                "session_id": session_id,
                "response_time": response_time,
            },
        }

        # 7. 记录日志
        self._save_chat_log(
            session_id=session_id,
            query=query,
            answer=answer,
            kb_id=kb_ids[0] if kb_ids else None,
            sources=sources,
            intent=intent,
            response_time=response_time,
        )

    # ==================== 辅助方法 ====================

    @staticmethod
    def _build_enhanced_context(
        context: str,
        recommended_products: List[Dict[str, Any]],
        intent: Dict[str, Any],
    ) -> str:
        """构建增强上下文

        将知识库检索结果和产品推荐信息合并为增强上下文。

        Args:
            context: 知识库检索到的上下文
            recommended_products: 推荐产品列表
            intent: 意图识别结果

        Returns:
            增强后的上下文文本
        """
        parts: List[str] = []

        if context:
            parts.append(f"【知识库参考资料】\n{context}")

        if recommended_products:
            product_infos: List[str] = []
            for i, product in enumerate(recommended_products, 1):
                info = (
                    f"{i}. {product.get('name', '未知产品')}\n"
                    f"   品牌：{product.get('brand', '未知')}\n"
                    f"   价格：{product.get('price', '未知')}\n"
                    f"   推荐理由：{product.get('reason', '无')}"
                )
                product_infos.append(info)

            products_text = "\n".join(product_infos)
            parts.append(f"【推荐产品信息】\n{products_text}")

        return "\n\n".join(parts) if parts else ""

    @staticmethod
    def _build_presales_prompt(
        intent: Dict[str, Any],
        recommended_products: List[Dict[str, Any]],
    ) -> str:
        """构建售前客服系统提示词

        根据意图类型和推荐产品动态构建提示词。

        Args:
            intent: 意图识别结果
            recommended_products: 推荐产品列表

        Returns:
            系统提示词文本
        """
        intent_type = intent.get("intent_type", "general")

        base_prompt = """你是一个专业的售前客服助手，名叫"小智"。你的职责是：
1. 热情友好地回答客户关于产品和服务的问题
2. 根据客户需求推荐合适的产品
3. 提供准确的产品规格、价格和功能信息
4. 对比不同产品的优缺点，帮助客户做出决策
5. 回答常见问题（FAQ）

回答要求：
- 回答要准确、专业、有礼貌
- 如果不确定信息，请诚实告知客户，不要编造
- 适当引导客户了解产品优势
- 使用简洁清晰的语言"""

        # 根据意图类型添加专项指引
        intent_guides: Dict[str, str] = {
            "product_inquiry": "\n\n特别注意：用户正在咨询产品信息，请重点介绍产品特性、规格参数，并推荐合适的产品。",
            "price_inquiry": "\n\n特别注意：用户关注价格，请重点说明产品价格、优惠活动和性价比，帮助用户在预算内选择。",
            "purchase_advice": "\n\n特别注意：用户需要购买建议，请综合分析需求，推荐最合适的产品并说明理由。",
            "comparison": "\n\n特别注意：用户想对比产品，请从功能、价格、适用场景等维度进行对比，给出明确建议。",
            "feature_inquiry": "\n\n特别注意：用户询问产品功能，请详细说明产品功能特性，并举例说明使用场景。",
            "after_sales": "\n\n特别注意：用户有售后问题，请耐心解答，提供具体的解决方案和流程指引。",
            "complaint": "\n\n特别注意：用户有不满情绪，请先表达理解和歉意，然后提供解决方案，态度要诚恳。",
        }

        guide = intent_guides.get(intent_type, "")
        return base_prompt + guide

    def _save_chat_log(
        self,
        session_id: int,
        query: str,
        answer: str,
        kb_id: Optional[int],
        sources: List[Dict[str, Any]],
        intent: Dict[str, Any],
        response_time: int,
    ) -> None:
        """保存聊天日志

        Args:
            session_id: 会话ID
            query: 用户问题
            answer: 系统回答
            kb_id: 知识库ID
            sources: 检索来源
            intent: 意图识别结果
            response_time: 响应时间（毫秒）
        """
        try:
            qa_log = QALog(
                session_id=session_id,
                question=query,
                answer=answer,
                knowledge_base_id=kb_id,
                retrieved_chunks=json.dumps(
                    sources[:3], ensure_ascii=False
                ) if sources else None,
                relevance_score=str(intent.get("confidence", 0)),
                response_time=response_time,
                tokens_used=0,
            )
            self.db.add(qa_log)
            self.db.commit()
        except Exception as e:
            logger.warning(f"保存聊天日志失败: {e}")
            self.db.rollback()
