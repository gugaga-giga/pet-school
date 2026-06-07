"""FAQ匹配器 - 基于向量相似度的FAQ快速匹配"""

from typing import Any, Dict, List, Optional

import numpy as np
from loguru import logger
from sqlalchemy.orm import Session

from app.rag.embedding.bge_m3 import BGEEmbedding


class FAQMatcher:
    """FAQ匹配器

    通过向量相似度计算用户问题与FAQ库中标准问题的匹配程度，
    超过阈值时直接返回FAQ答案，实现快速响应。
    """

    def __init__(self, db_session: Session, embedding_service: BGEEmbedding) -> None:
        """初始化FAQ匹配器

        Args:
            db_session: 数据库会话
            embedding_service: Embedding服务实例
        """
        self.db = db_session
        self.embedding_service = embedding_service
        # FAQ缓存: kb_id -> FAQ列表
        # 每个FAQ项包含: {id, question, answer, question_vector}
        self.faq_cache: Dict[str, List[Dict[str, Any]]] = {}

    async def match(
        self,
        query: str,
        kb_ids: List[str],
        threshold: float = 0.85,
    ) -> Optional[Dict[str, Any]]:
        """匹配FAQ

        将用户问题向量化后与FAQ库中的标准问题计算余弦相似度，
        超过阈值则返回匹配的FAQ答案。

        Args:
            query: 用户查询文本
            kb_ids: 知识库ID列表
            threshold: 相似度阈值，默认0.85

        Returns:
            匹配结果字典，包含:
                - faq_id: FAQ记录ID
                - question: 匹配的标准问题
                - answer: FAQ答案
                - score: 相似度分数
            未匹配时返回None
        """
        if not query or not query.strip():
            return None

        if not kb_ids:
            return None

        # 向量化用户查询
        try:
            query_vector = self.embedding_service.embed_query(query)
        except Exception as e:
            logger.error(f"FAQ匹配 - 向量化查询失败: {e}")
            return None

        query_np = np.array(query_vector, dtype=np.float32)

        best_match: Optional[Dict[str, Any]] = None
        best_score = 0.0

        # 遍历指定知识库的FAQ缓存
        for kb_id in kb_ids:
            faq_list = self.faq_cache.get(str(kb_id), [])
            if not faq_list:
                # 尝试加载该知识库的FAQ
                self.load_faq(str(kb_id))
                faq_list = self.faq_cache.get(str(kb_id), [])

            for faq_item in faq_list:
                faq_vector = faq_item.get("question_vector")
                if faq_vector is None:
                    continue

                # 计算余弦相似度
                faq_np = np.array(faq_vector, dtype=np.float32)
                similarity = float(np.dot(query_np, faq_np) / (
                    np.linalg.norm(query_np) * np.linalg.norm(faq_np) + 1e-8
                ))

                if similarity > best_score:
                    best_score = similarity
                    best_match = {
                        "faq_id": faq_item["id"],
                        "question": faq_item["question"],
                        "answer": faq_item["answer"],
                        "score": round(similarity, 4),
                        "kb_id": kb_id,
                    }

        # 判断是否超过阈值
        if best_match and best_score >= threshold:
            logger.info(
                f"FAQ匹配成功: query='{query[:30]}...', "
                f"matched_question='{best_match['question'][:30]}...', "
                f"score={best_score:.4f}"
            )
            return best_match

        logger.debug(
            f"FAQ未匹配: query='{query[:30]}...', best_score={best_score:.4f}, "
            f"threshold={threshold}"
        )
        return None

    def load_faq(self, kb_id: str) -> None:
        """加载指定知识库的FAQ到缓存

        从数据库中读取FAQ数据，向量化标准问题后存入缓存。

        Args:
            kb_id: 知识库ID
        """
        kb_id_str = str(kb_id)

        try:
            # 从数据库查询FAQ数据
            # 使用DocumentChunk作为FAQ数据源，查找标题包含"FAQ"或"常见问题"的文档分块
            from app.models.knowledge import Document, DocumentChunk

            documents = (
                self.db.query(Document)
                .filter(
                    Document.knowledge_base_id == int(kb_id_str),
                    Document.title.like("%FAQ%")
                    | Document.title.like("%常见问题%")
                    | Document.title.like("%问答%"),
                )
                .all()
            )

            if not documents:
                logger.debug(f"知识库 {kb_id_str} 未找到FAQ文档")
                self.faq_cache[kb_id_str] = []
                return

            faq_list: List[Dict[str, Any]] = []

            for doc in documents:
                chunks = (
                    self.db.query(DocumentChunk)
                    .filter(DocumentChunk.document_id == doc.id)
                    .all()
                )

                for chunk in chunks:
                    content = chunk.content
                    if not content:
                        continue

                    # 解析FAQ格式：假设每条FAQ以"Q:"/"问："开头，"A:"/"答："分隔
                    faq_pairs = self._parse_faq_content(content)

                    for faq in faq_pairs:
                        # 向量化标准问题
                        try:
                            question_vector = self.embedding_service.embed_query(faq["question"])
                        except Exception as e:
                            logger.warning(f"FAQ向量化失败，跳过: {e}")
                            continue

                        faq_list.append({
                            "id": faq["id"],
                            "question": faq["question"],
                            "answer": faq["answer"],
                            "question_vector": question_vector,
                        })

            self.faq_cache[kb_id_str] = faq_list
            logger.info(f"知识库 {kb_id_str} 加载FAQ完成，共 {len(faq_list)} 条")

        except Exception as e:
            logger.error(f"加载知识库 {kb_id_str} FAQ失败: {e}")
            self.faq_cache[kb_id_str] = []

    def refresh_cache(self) -> None:
        """刷新所有FAQ缓存

        清空当前缓存并重新加载所有已缓存的知识库FAQ。
        """
        cached_kb_ids = list(self.faq_cache.keys())
        self.faq_cache.clear()
        logger.info(f"清空FAQ缓存，准备重新加载 {len(cached_kb_ids)} 个知识库")

        for kb_id in cached_kb_ids:
            self.load_faq(kb_id)

        logger.info("FAQ缓存刷新完成")

    def _parse_faq_content(self, content: str) -> List[Dict[str, Any]]:
        """解析FAQ内容，提取问答对

        支持多种FAQ格式：
        - Q: xxx A: xxx
        - 问：xxx 答：xxx
        - 问题：xxx 回答：xxx

        Args:
            content: FAQ文档内容

        Returns:
            FAQ问答对列表，每项包含: {id, question, answer}
        """
        faq_pairs: List[Dict[str, Any]] = []
        faq_id = 0

        # 尝试按"Q:"/"问："分割
        import re

        # 匹配模式：Q:/问：/问题：开头的问题，A:/答：/回答：开头的答案
        patterns = [
            # Q: xxx A: xxx 格式
            r"Q[：:]\s*(.+?)\s*A[：:]\s*(.+?)(?=Q[：:]|\Z)",
            # 问：xxx 答：xxx 格式
            r"问[：:]\s*(.+?)\s*答[：:]\s*(.+?)(?=问[：:]|\Z)",
            # 问题：xxx 回答：xxx 格式
            r"问题[：:]\s*(.+?)\s*回答[：:]\s*(.+?)(?=问题[：:]|\Z)",
        ]

        for pattern in patterns:
            matches = re.findall(pattern, content, re.DOTALL)
            for question, answer in matches:
                question = question.strip()
                answer = answer.strip()
                if question and answer:
                    faq_id += 1
                    faq_pairs.append({
                        "id": f"faq_{faq_id}",
                        "question": question,
                        "answer": answer,
                    })

        # 如果正则未匹配到，尝试按行分割（每两行为一组问答）
        if not faq_pairs:
            lines = [line.strip() for line in content.split("\n") if line.strip()]
            for i in range(0, len(lines) - 1, 2):
                question = lines[i]
                answer = lines[i + 1] if i + 1 < len(lines) else ""
                # 去掉可能的前缀
                for prefix in ["Q:", "Q：", "问：", "问:", "问题：", "问题:"]:
                    if question.startswith(prefix):
                        question = question[len(prefix):].strip()
                        break
                for prefix in ["A:", "A：", "答：", "答:", "回答：", "回答:"]:
                    if answer.startswith(prefix):
                        answer = answer[len(prefix):].strip()
                        break

                if question and answer:
                    faq_id += 1
                    faq_pairs.append({
                        "id": f"faq_{faq_id}",
                        "question": question,
                        "answer": answer,
                    })

        return faq_pairs
