"""聊天服务模块，处理会话创建、消息发送、历史查询

集成完整流程：意图识别 → FAQ匹配 → RAG检索 → MySQL查询 → 产品推荐 → LLM生成
"""

import json
import time
from typing import AsyncGenerator, Dict, List, Optional

from loguru import logger
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.exceptions import NotFoundException
from app.models.chat import ChatMessage, ChatSession, QALog
from app.models.course import Course, Order
from app.models.knowledge import KnowledgeBase
from app.models.pet import Pet
from app.models.product import Product
from app.models.user import User
from app.schemas.chat import ChatRequest, ChatSessionCreate
from app.utils.pagination import paginate


class ChatService:
    """聊天服务类 - 集成意图识别、RAG检索、MySQL查询、LLM生成"""

    @staticmethod
    def create_session(
        db: Session, user_id: Optional[int], session_data: ChatSessionCreate
    ) -> ChatSession:
        """创建聊天会话"""
        session = ChatSession(
            user_id=user_id,
            title=session_data.title or "新对话",
        )
        db.add(session)
        db.commit()
        db.refresh(session)
        return session

    @staticmethod
    def get_session_by_id(db: Session, session_id: int) -> ChatSession:
        """根据ID获取会话"""
        session = db.query(ChatSession).filter(
            ChatSession.id == session_id, ChatSession.is_deleted == 0
        ).first()
        if not session:
            raise NotFoundException(message="会话不存在")
        return session

    @staticmethod
    def get_session_list(
        db: Session,
        user_id: Optional[int] = None,
        page: int = 1,
        page_size: int = 10,
    ) -> dict:
        """获取会话列表"""
        query = db.query(ChatSession).filter(ChatSession.is_deleted == 0)

        if user_id:
            query = query.filter(ChatSession.user_id == user_id)

        query = query.order_by(ChatSession.updated_at.desc())
        return paginate(query, page, page_size)

    @staticmethod
    def delete_session(db: Session, session_id: int) -> None:
        """删除会话（软删除）"""
        session = ChatService.get_session_by_id(db, session_id)
        session.is_deleted = 1
        db.commit()

    @staticmethod
    def get_message_history(
        db: Session, session_id: int, page: int = 1, page_size: int = 50
    ) -> dict:
        """获取会话消息历史"""
        query = db.query(ChatMessage).filter(
            ChatMessage.session_id == session_id, ChatMessage.is_deleted == 0
        )
        query = query.order_by(ChatMessage.created_at.asc())
        return paginate(query, page, page_size)

    @staticmethod
    def save_message(
        db: Session,
        session_id: int,
        role: str,
        content: str,
        token_count: int = 0,
        sources: Optional[dict] = None,
        intent_type: Optional[str] = None,
    ) -> ChatMessage:
        """保存聊天消息"""
        message = ChatMessage(
            session_id=session_id,
            role=role,
            content=content,
            token_count=token_count,
            sources=sources,
            intent_type=intent_type,
        )
        db.add(message)
        db.commit()
        db.refresh(message)
        return message

    @staticmethod
    def save_qa_log(
        db: Session,
        user_id: int,
        session_id: int,
        question: str,
        answer: str,
        intent_type: Optional[str] = None,
        sources: Optional[dict] = None,
        retrieval_time: int = 0,
        llm_time: int = 0,
        total_time: int = 0,
    ) -> QALog:
        """保存问答日志"""
        qa_log = QALog(
            user_id=user_id,
            session_id=session_id,
            question=question,
            answer=answer,
            intent_type=intent_type,
            sources=sources,
            retrieval_time=retrieval_time,
            llm_time=llm_time,
            total_time=total_time,
        )
        db.add(qa_log)
        db.commit()
        db.refresh(qa_log)
        return qa_log

    # ==================== 意图识别 ====================

    @staticmethod
    async def classify_intent(query: str) -> Dict:
        """意图识别 - 优先LLM，降级规则匹配"""
        try:
            from app.llm.llm_service import LLMService
            from app.intent.classifier import IntentClassifier

            llm = LLMService(
                api_key=settings.LLM_API_KEY,
                base_url=settings.LLM_BASE_URL,
                model_name=settings.LLM_MODEL_NAME,
            )
            classifier = IntentClassifier(llm)
            result = await classifier.classify(query)
            return result
        except Exception as e:
            logger.warning(f"意图识别失败，使用规则降级: {e}")
            from app.intent.classifier import IntentClassifier
            from app.llm.llm_service import LLMService
            llm = LLMService(
                api_key=settings.LLM_API_KEY,
                base_url=settings.LLM_BASE_URL,
                model_name=settings.LLM_MODEL_NAME,
            )
            classifier = IntentClassifier(llm)
            result = classifier.classify_by_rules(query)
            return result

    # ==================== FAQ匹配 ====================

    @staticmethod
    def match_faq(query: str) -> Optional[str]:
        """基于规则的FAQ快速匹配"""
        faq_rules = [
            (["营业时间", "几点开门", "几点下班", "上班时间"], "我们的营业时间是每天 9:00-21:00，全年无休。周末和节假日正常营业。"),
            (["地址", "在哪里", "怎么去", "门店"], "我们的地址是：宠物学校总部位于市中心宠物公园旁，地铁3号线宠物公园站B出口步行5分钟即到。"),
            (["电话", "联系方式", "客服电话"], "客服热线：400-888-PETS (7387)，工作时间 9:00-21:00。也可通过微信公众号「宠物学校」在线咨询。"),
            (["收费", "价格表", "收费标准"], "课程价格根据类型不同：幼犬基础班 ¥1,280，成犬进阶班 ¥1,680，行为纠正课 ¥2,380，敏捷竞技班 ¥1,980。详情可咨询客服获取完整价格表。"),
            (["预约", "怎么报名", "如何预约"], "您可以通过以下方式预约：1. 微信公众号「宠物学校」在线预约；2. 拨打客服热线 400-888-7387；3. 到店前台直接预约。建议提前3天预约。"),
            (["退款", "退课"], "课程退款政策：开课前7天以上可全额退款，开课前3-7天退款80%，开课前3天内退款50%，开课后不予退款。"),
        ]

        query_lower = query.lower()
        for keywords, answer in faq_rules:
            for kw in keywords:
                if kw in query_lower:
                    return answer
        return None

    # ==================== MySQL数据查询 ====================

    @staticmethod
    def query_courses(db: Session, query: str) -> str:
        """查询课程信息"""
        courses = db.query(Course).filter(Course.status == 1, Course.is_deleted == 0).all()
        if not courses:
            return "暂无可用课程信息。"

        lines = ["当前可报名的课程如下：\n"]
        for c in courses:
            spots = c.max_students - c.current_students
            lines.append(
                f"- **{c.name}**\n"
                f"  分类：{c.category} | 难度：{c.difficulty}\n"
                f"  价格：¥{c.price} | 课时：{c.duration}\n"
                f"  训练师：{c.trainer} | 剩余名额：{spots}人\n"
                f"  简介：{c.description}\n"
            )
        return "\n".join(lines)

    @staticmethod
    def query_orders(db: Session, user_id: int) -> str:
        """查询用户订单"""
        orders = db.query(Order).filter(Order.user_id == user_id, Order.is_deleted == 0).all()
        if not orders:
            return "您暂无订单记录。"

        status_map = {"pending": "待支付", "paid": "已支付", "completed": "已完成", "cancelled": "已取消", "refunded": "已退款"}
        lines = ["您的订单记录如下：\n"]
        for o in orders:
            course_name = ""
            product_name = ""
            if o.course_id:
                course = db.query(Course).filter(Course.id == o.course_id).first()
                if course:
                    course_name = course.name
            if o.product_id:
                product = db.query(Product).filter(Product.id == o.product_id).first()
                if product:
                    product_name = product.name

            item_name = course_name or product_name or "未知项目"
            status_text = status_map.get(o.status, o.status)
            lines.append(
                f"- 订单号：{o.order_no}\n"
                f"  项目：{item_name}\n"
                f"  金额：¥{o.amount} | 状态：{status_text}\n"
                f"  下单时间：{o.created_at}\n"
            )
        return "\n".join(lines)

    @staticmethod
    def query_pet_profile(db: Session, user_id: int, query: str = "") -> str:
        """查询用户宠物档案"""
        pets = db.query(Pet).filter(Pet.user_id == user_id, Pet.is_deleted == 0).all()
        if not pets:
            return "您暂无宠物档案，可以在「宠物管理」中添加宠物信息。"

        # 尝试匹配特定宠物
        target_pet = None
        for pet in pets:
            if pet.name in query:
                target_pet = pet
                break

        if target_pet:
            pet = target_pet
        else:
            pet = pets[0]

        gender_map = {1: "公", 2: "母", 0: "未知"}
        neutered_map = {0: "未绝育", 1: "已绝育"}
        health_map = {"healthy": "健康", "sick": "生病中", "recovering": "恢复中"}
        vaccine_map = {"unknown": "未知", "partial": "部分接种", "complete": "已完全接种"}

        result = (
            f"宠物档案 - **{pet.name}**\n"
            f"  类型：{pet.type} | 品种：{pet.breed or '未知'}\n"
            f"  年龄：{pet.age}岁 | 性别：{gender_map.get(pet.gender, '未知')}\n"
            f"  体重：{pet.weight}kg | 绝育状态：{neutered_map.get(pet.is_neutered, '未知')}\n"
            f"  健康状况：{health_map.get(pet.health_status, pet.health_status)}\n"
            f"  疫苗状态：{vaccine_map.get(pet.vaccine_status, pet.vaccine_status)}\n"
        )

        if len(pets) > 1 and not target_pet:
            other_names = [p.name for p in pets if p.id != pet.id]
            result += f"\n您还有其他宠物：{', '.join(other_names)}，可以指定宠物名查询详细信息。"

        return result

    @staticmethod
    def query_user_info(db: Session, user_id: int) -> str:
        """查询用户账户信息"""
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return "用户信息不存在。"

        roles = [role.code for role in user.roles] if user.roles else []
        pets = db.query(Pet).filter(Pet.user_id == user_id, Pet.is_deleted == 0).all()
        orders = db.query(Order).filter(Order.user_id == user_id, Order.is_deleted == 0).all()

        return (
            f"账户信息\n"
            f"  用户名：{user.username}\n"
            f"  昵称：{user.nickname or user.username}\n"
            f"  邮箱：{user.email or '未设置'}\n"
            f"  手机：{user.phone or '未设置'}\n"
            f"  角色：{', '.join(roles) if roles else '普通用户'}\n"
            f"  宠物数量：{len(pets)}只\n"
            f"  订单数量：{len(orders)}个\n"
            f"  注册时间：{user.created_at}\n"
        )

    # ==================== RAG知识库检索 ====================

    @staticmethod
    async def rag_retrieve(query: str, kb_ids: List[int]) -> str:
        """RAG知识库检索"""
        try:
            from app.rag.embedding.bge_m3 import BGEEmbedding
            from app.rag.vectorstore.faiss_store import FAISSVectorStore
            from app.rag.retrieval.hybrid_retriever import HybridRetriever
            from app.rag.retrieval.bm25_search import BM25Search
            from app.rag.retrieval.reranker import Reranker

            embedding = BGEEmbedding()
            vector_store = FAISSVectorStore(settings.FAISS_INDEX_DIR)
            bm25 = BM25Search()
            reranker = Reranker()

            retriever = HybridRetriever(
                vector_store=vector_store,
                embedding_service=embedding,
                bm25_search=bm25,
                reranker=reranker,
            )

            kb_id_strs = [str(kid) for kid in kb_ids]
            results = await retriever.retrieve(query=query, kb_ids=kb_id_strs, top_k=5)

            if not results:
                return ""

            context_parts = []
            for doc in results:
                content = doc.get("content", "")
                if content:
                    context_parts.append(content)

            return "\n\n".join(context_parts)
        except Exception as e:
            logger.warning(f"RAG检索异常: {e}")
            return ""

    # ==================== 构建系统提示词 ====================

    @staticmethod
    def build_system_prompt(intent_type: str) -> str:
        """根据意图类型构建系统提示词"""
        base = """你是一个专业的宠物学校客服助手，名叫"小智"。你负责回答关于宠物训练课程、宠物饲养知识、订单查询等问题。

回答要求：
- 回答要准确、专业、有礼貌
- 如果不确定信息，请诚实告知，不要编造
- 使用简洁清晰的语言
- 适当使用emoji让回复更生动"""

        intent_prompts = {
            "FAQ": "\n\n用户在询问常见问题，请基于提供的FAQ答案直接回答，可以适当补充细节。",
            "Course": "\n\n用户在咨询课程信息，请基于提供的课程数据详细介绍，包括价格、课时、训练师、名额等，并给出适合的建议。",
            "PetKnowledge": "\n\n用户在咨询宠物饲养知识，请基于提供的参考资料回答，如果资料不足，可以结合专业知识给出建议，但要说明仅供参考。",
            "Order": "\n\n用户在查询订单状态，请基于提供的订单数据准确回答，包括订单号、项目、金额、状态等。",
            "PetProfile": "\n\n用户在查询宠物档案，请基于提供的宠物数据回答，包括品种、年龄、体重、健康状况、疫苗状态等。",
            "UserInfo": "\n\n用户在查询账户信息，请基于提供的用户数据回答。",
            "Greeting": "\n\n用户在打招呼，请热情友好地回应，并简要介绍你能提供的服务。",
            "Other": "\n\n请根据用户的问题尽力回答。",
        }

        return base + intent_prompts.get(intent_type, intent_prompts["Other"])

    # ==================== 主聊天流程 ====================

    @staticmethod
    async def chat_stream(
        db: Session, chat_request: ChatRequest, user_id: Optional[int] = None
    ) -> AsyncGenerator[str, None]:
        """流式聊天接口 - 集成意图识别→FAQ→RAG→MySQL→LLM完整流程"""
        start_time = time.time()

        # 获取或创建会话
        if chat_request.session_id:
            session = ChatService.get_session_by_id(db, chat_request.session_id)
        else:
            session = ChatService.create_session(
                db, user_id, ChatSessionCreate(title="新对话")
            )

        # 发送会话ID
        yield f"data: {json.dumps({'type': 'session', 'session_id': session.id}, ensure_ascii=False)}\n\n"

        # 保存用户消息
        ChatService.save_message(db, session.id, "user", chat_request.message)

        query = chat_request.message

        # ========== 1. 意图识别 ==========
        intent = {"intent_type": "Other", "confidence": 0.0, "keywords": []}
        try:
            intent = await ChatService.classify_intent(query)
        except Exception as e:
            logger.warning(f"意图识别异常: {e}")

        intent_type = intent.get("intent_type", "Other")
        logger.info(f"意图识别: query='{query[:30]}...', intent={intent_type}, confidence={intent.get('confidence', 0):.2f}")

        # 发送意图识别结果
        yield f"data: {json.dumps({'type': 'intent', 'intent_type': intent_type, 'confidence': intent.get('confidence', 0)}, ensure_ascii=False)}\n\n"

        # ========== 2. 根据意图获取上下文 ==========
        context = ""

        # 2a. FAQ快速匹配
        if intent_type in ("FAQ", "Other"):
            faq_answer = ChatService.match_faq(query)
            if faq_answer:
                context = f"【FAQ参考答案】\n{faq_answer}"
                logger.info("FAQ匹配成功")

        # 2b. MySQL数据查询
        if intent_type == "Course":
            course_info = ChatService.query_courses(db, query)
            context = f"【课程数据】\n{course_info}"

        elif intent_type == "Order" and user_id:
            order_info = ChatService.query_orders(db, user_id)
            context = f"【订单数据】\n{order_info}"

        elif intent_type == "PetProfile" and user_id:
            pet_info = ChatService.query_pet_profile(db, user_id, query)
            context = f"【宠物档案数据】\n{pet_info}"

        elif intent_type == "UserInfo" and user_id:
            user_info = ChatService.query_user_info(db, user_id)
            context = f"【用户数据】\n{user_info}"

        # 2c. RAG知识库检索（PetKnowledge、Course、FAQ等意图补充）
        if intent_type in ("PetKnowledge", "Course", "FAQ") and not context:
            # 获取知识库ID列表
            kbs = db.query(KnowledgeBase).filter(
                KnowledgeBase.status == 1, KnowledgeBase.is_deleted == 0
            ).all()
            if kbs:
                kb_ids = [kb.id for kb in kbs]
                rag_context = await ChatService.rag_retrieve(query, kb_ids)
                if rag_context:
                    if context:
                        context += f"\n\n【知识库参考资料】\n{rag_context}"
                    else:
                        context = f"【知识库参考资料】\n{rag_context}"

        # ========== 3. 构建LLM消息 ==========
        history_msgs = (
            db.query(ChatMessage)
            .filter(ChatMessage.session_id == session.id, ChatMessage.is_deleted == 0)
            .order_by(ChatMessage.created_at.asc())
            .all()
        )

        messages = []
        system_prompt = ChatService.build_system_prompt(intent_type)

        if context:
            system_prompt += f"\n\n以下是与用户问题相关的参考数据，请优先基于这些数据回答：\n\n{context}"

        messages.append({"role": "system", "content": system_prompt})

        # 添加历史消息（最近10条）
        for msg in history_msgs[-10:]:
            if msg.role in ("user", "assistant"):
                messages.append({"role": msg.role, "content": msg.content})

        # ========== 4. 调用LLM流式生成 ==========
        full_response = ""
        try:
            import openai

            client = openai.AsyncOpenAI(
                api_key=settings.LLM_API_KEY,
                base_url=settings.LLM_BASE_URL,
            )

            stream = await client.chat.completions.create(
                model=settings.LLM_MODEL_NAME,
                messages=messages,
                stream=True,
            )

            async for chunk in stream:
                if chunk.choices and chunk.choices[0].delta.content:
                    content = chunk.choices[0].delta.content
                    full_response += content
                    yield f"data: {json.dumps({'type': 'content', 'content': content}, ensure_ascii=False)}\n\n"

        except Exception as e:
            logger.error(f"LLM调用失败: {e}")
            # 降级：如果有上下文数据，直接返回
            if context:
                full_response = context
                yield f"data: {json.dumps({'type': 'content', 'content': context}, ensure_ascii=False)}\n\n"
            else:
                full_response = "抱歉，我暂时无法回答您的问题，请稍后再试或联系人工客服。"
                yield f"data: {json.dumps({'type': 'error', 'content': full_response}, ensure_ascii=False)}\n\n"

        # ========== 5. 保存结果 ==========
        ChatService.save_message(
            db, session.id, "assistant", full_response,
            intent_type=intent_type,
        )

        total_time = int((time.time() - start_time) * 1000)

        ChatService.save_qa_log(
            db,
            user_id=user_id or 0,
            session_id=session.id,
            question=query,
            answer=full_response,
            intent_type=intent_type,
            total_time=total_time,
        )

        # 发送完成信号
        yield f"data: {json.dumps({'type': 'done', 'session_id': session.id}, ensure_ascii=False)}\n\n"
