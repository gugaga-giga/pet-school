"""基础向量检索器"""

from typing import Any, Dict, List

import numpy as np
from loguru import logger

from app.rag.embedding.bge_m3 import BGEEmbedding
from app.rag.vectorstore.faiss_store import FAISSVectorStore


class BaseRetriever:
    """基础向量检索器，基于FAISS进行向量相似度检索"""

    def __init__(
        self,
        vector_store: FAISSVectorStore,
        embedding_service: BGEEmbedding,
    ) -> None:
        """初始化检索器

        Args:
            vector_store: FAISS向量存储实例
            embedding_service: Embedding服务实例
        """
        self.vector_store = vector_store
        self.embedding_service = embedding_service

    def retrieve(
        self,
        query: str,
        kb_ids: List[str],
        top_k: int = 5,
    ) -> List[Dict[str, Any]]:
        """检索相关文档块

        Args:
            query: 查询文本
            kb_ids: 要检索的知识库ID列表
            top_k: 每个知识库返回的最相似结果数

        Returns:
            检索结果列表，每项包含:
                - chunk_id: 分块ID
                - score: 相似度分数
                - kb_id: 知识库ID

        Raises:
            ValueError: 查询文本为空
        """
        if not query or not query.strip():
            raise ValueError("查询文本不能为空")

        if not kb_ids:
            return []

        # 1. 向量化query
        query_vector = self.embedding_service.embed_query(query)
        query_np = np.array([query_vector], dtype=np.float32)

        # 2. 在指定知识库中检索
        all_results: List[Dict[str, Any]] = []

        for kb_id in kb_ids:
            try:
                # 尝试加载索引（如果尚未加载）
                if kb_id not in self.vector_store.indexes:
                    self.vector_store.load_index(kb_id)

                if kb_id not in self.vector_store.indexes:
                    logger.warning(f"知识库 {kb_id} 索引不存在，跳过")
                    continue

                # 执行检索
                results = self.vector_store.search(kb_id, query_np, top_k)

                for chunk_id, score in results:
                    all_results.append(
                        {
                            "chunk_id": chunk_id,
                            "score": score,
                            "kb_id": kb_id,
                        }
                    )

            except Exception as e:
                logger.error(f"知识库 {kb_id} 检索失败: {e}")
                continue

        # 3. 按相似度降序排序
        all_results.sort(key=lambda x: x["score"], reverse=True)

        # 截取top_k个结果
        return all_results[:top_k]
