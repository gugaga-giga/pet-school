"""混合检索器：向量检索 + BM25 + Rerank，实现多路召回与精排"""

from typing import Any, Dict, List, Optional

import numpy as np
from loguru import logger

from app.rag.embedding.bge_m3 import BGEEmbedding
from app.rag.retrieval.bm25_search import BM25Search
from app.rag.retrieval.reranker import Reranker
from app.rag.vectorstore.faiss_store import FAISSVectorStore


class HybridRetriever:
    """混合检索器：向量检索 + BM25 + Rerank

    检索流程：
    1. 向量检索（语义相似度）
    2. BM25检索（关键词匹配）
    3. RRF加权融合两路结果
    4. Rerank精排
    5. MMR去重（兼顾相关性与多样性）
    """

    def __init__(
        self,
        vector_store: FAISSVectorStore,
        embedding_service: BGEEmbedding,
        bm25_search: BM25Search,
        reranker: Reranker,
        vector_weight: float = 0.6,
        bm25_weight: float = 0.4,
    ) -> None:
        """初始化混合检索器

        Args:
            vector_store: FAISS向量存储实例
            embedding_service: Embedding服务实例
            bm25_search: BM25检索实例
            reranker: Reranker重排序实例
            vector_weight: 向量检索权重（默认0.6）
            bm25_weight: BM25检索权重（默认0.4）
        """
        self.vector_store = vector_store
        self.embedding_service = embedding_service
        self.bm25_search = bm25_search
        self.reranker = reranker
        self.vector_weight = vector_weight
        self.bm25_weight = bm25_weight

    # ==================== 主检索入口 ====================

    async def retrieve(
        self,
        query: str,
        kb_ids: List[str],
        top_k: int = 5,
        filters: Optional[Dict[str, Any]] = None,
    ) -> List[Dict[str, Any]]:
        """混合检索主入口

        Args:
            query: 查询文本
            kb_ids: 知识库ID列表
            top_k: 最终返回的结果数
            filters: 元数据过滤条件

        Returns:
            检索结果列表，每项包含:
                - chunk_id: 分块ID
                - content: 分块内容
                - score: 综合分数
                - kb_id: 知识库ID
                - rerank_score: 重排序分数
        """
        if not query or not query.strip():
            raise ValueError("查询文本不能为空")
        if not kb_ids:
            return []

        # 1. 向量检索（多召回）
        vector_results = await self._vector_search(query, kb_ids, top_k * 3)

        # 2. BM25检索（多召回）
        bm25_results = await self._bm25_search(query, kb_ids, top_k * 3)

        # 3. RRF加权融合
        merged = self._merge_results(vector_results, bm25_results)

        if not merged:
            return []

        # 4. Metadata过滤
        if filters:
            merged = self.metadata_filter(merged, filters)

        # 5. Rerank精排
        reranked = self.reranker.rerank(query, merged, top_k * 2)

        # 6. MMR去重
        final = self._mmr_rerank(query, reranked, top_k)

        logger.info(
            f"混合检索完成: query='{query[:30]}...', "
            f"向量结果={len(vector_results)}, BM25结果={len(bm25_results)}, "
            f"融合后={len(merged)}, 最终={len(final)}"
        )

        return final

    # ==================== 向量检索 ====================

    async def _vector_search(
        self, query: str, kb_ids: List[str], top_k: int
    ) -> List[Dict[str, Any]]:
        """向量检索

        Args:
            query: 查询文本
            kb_ids: 知识库ID列表
            top_k: 返回结果数

        Returns:
            向量检索结果列表
        """
        results: List[Dict[str, Any]] = []

        try:
            # 向量化query
            query_vector = self.embedding_service.embed_query(query)
            query_np = np.array([query_vector], dtype=np.float32)

            for kb_id in kb_ids:
                try:
                    # 尝试加载索引
                    if kb_id not in self.vector_store.indexes:
                        self.vector_store.load_index(kb_id)

                    if kb_id not in self.vector_store.indexes:
                        logger.warning(f"知识库 {kb_id} 向量索引不存在，跳过")
                        continue

                    # 执行向量检索
                    search_results = self.vector_store.search(kb_id, query_np, top_k)

                    for chunk_id, score in search_results:
                        results.append(
                            {
                                "chunk_id": chunk_id,
                                "score": float(score),
                                "kb_id": kb_id,
                                "source": "vector",
                            }
                        )

                except Exception as e:
                    logger.error(f"知识库 {kb_id} 向量检索失败: {e}")
                    continue

        except Exception as e:
            logger.error(f"向量检索整体失败: {e}")

        return results

    # ==================== BM25检索 ====================

    async def _bm25_search(
        self, query: str, kb_ids: List[str], top_k: int
    ) -> List[Dict[str, Any]]:
        """BM25检索

        Args:
            query: 查询文本
            kb_ids: 知识库ID列表
            top_k: 返回结果数

        Returns:
            BM25检索结果列表
        """
        results: List[Dict[str, Any]] = []

        for kb_id in kb_ids:
            try:
                if kb_id not in self.bm25_search.bm25:
                    logger.warning(f"知识库 {kb_id} BM25索引不存在，跳过")
                    continue

                # 执行BM25检索
                bm25_results = self.bm25_search.search(kb_id, query, top_k)

                for doc_idx, score in bm25_results:
                    # BM25的doc_idx对应raw_documents中的索引
                    # 需要映射到chunk_id
                    raw_docs = self.bm25_search.raw_documents.get(kb_id, [])
                    if doc_idx < len(raw_docs):
                        results.append(
                            {
                                "doc_index": doc_idx,
                                "content": raw_docs[doc_idx],
                                "score": float(score),
                                "kb_id": kb_id,
                                "source": "bm25",
                            }
                        )

            except Exception as e:
                logger.error(f"知识库 {kb_id} BM25检索失败: {e}")
                continue

        return results

    # ==================== RRF融合 ====================

    def _merge_results(
        self,
        vector_results: List[Dict[str, Any]],
        bm25_results: List[Dict[str, Any]],
        k: int = 60,
    ) -> List[Dict[str, Any]]:
        """加权融合：Reciprocal Rank Fusion (RRF)

        RRF算法：score = sum(weight / (k + rank)) for each ranked list
        其中k=60是标准RRF参数，用于平滑排名的影响

        Args:
            vector_results: 向量检索结果
            bm25_results: BM25检索结果
            k: RRF平滑参数，默认60

        Returns:
            融合后的结果列表，按RRF分数降序排列
        """
        # 使用字典收集每个文档的RRF分数
        rrf_scores: Dict[str, float] = {}
        doc_map: Dict[str, Dict[str, Any]] = {}

        # 处理向量检索结果
        for rank, doc in enumerate(sorted(vector_results, key=lambda x: x.get("score", 0), reverse=True)):
            # 使用chunk_id和kb_id组合作为唯一标识
            doc_key = f"v_{doc.get('kb_id', '')}_{doc.get('chunk_id', rank)}"
            rrf_scores[doc_key] = rrf_scores.get(doc_key, 0.0) + self.vector_weight / (k + rank + 1)
            doc["rrf_rank_vector"] = rank + 1
            doc_map[doc_key] = doc

        # 处理BM25检索结果
        for rank, doc in enumerate(sorted(bm25_results, key=lambda x: x.get("score", 0), reverse=True)):
            doc_key = f"b_{doc.get('kb_id', '')}_{doc.get('doc_index', rank)}"
            rrf_scores[doc_key] = rrf_scores.get(doc_key, 0.0) + self.bm25_weight / (k + rank + 1)
            doc["rrf_rank_bm25"] = rank + 1
            if doc_key not in doc_map:
                doc_map[doc_key] = doc

        # 按RRF分数降序排序
        sorted_keys = sorted(rrf_scores.keys(), key=lambda x: rrf_scores[x], reverse=True)

        merged: List[Dict[str, Any]] = []
        for doc_key in sorted_keys:
            doc = doc_map[doc_key]
            doc["rrf_score"] = rrf_scores[doc_key]
            merged.append(doc)

        return merged

    # ==================== MMR去重 ====================

    def _mmr_rerank(
        self,
        query: str,
        documents: List[Dict[str, Any]],
        top_k: int,
        lambda_param: float = 0.7,
    ) -> List[Dict[str, Any]]:
        """MMR (Maximal Marginal Relevance) 去重

        在相关性和多样性之间平衡，避免返回内容过于相似的文档

        MMR = λ * Sim(d_i, q) - (1-λ) * max[Sim(d_i, d_j)] for d_j in S

        Args:
            query: 查询文本
            documents: 候选文档列表
            top_k: 返回结果数
            lambda_param: 相关性-多样性权衡参数
                - 1.0: 最大相关性（不考虑多样性）
                - 0.0: 最大多样性（不考虑相关性）
                - 默认0.7: 偏向相关性但兼顾多样性

        Returns:
            MMR选择后的文档列表
        """
        if not documents:
            return []

        if len(documents) <= top_k:
            return documents

        selected: List[Dict[str, Any]] = []
        remaining = list(documents)

        # 选择第一个：rerank_score最高的
        remaining.sort(key=lambda x: x.get("rerank_score", 0.0), reverse=True)
        selected.append(remaining.pop(0))

        # 逐步选择后续文档
        while len(selected) < top_k and remaining:
            best_score = -float("inf")
            best_idx = 0

            for i, doc in enumerate(remaining):
                # 相关性分数（使用rerank_score）
                relevance = doc.get("rerank_score", 0.0)

                # 与已选文档的最大相似度（基于content的简单字符重叠）
                max_similarity = 0.0
                doc_content = doc.get("content", "")

                for selected_doc in selected:
                    selected_content = selected_doc.get("content", "")
                    similarity = self._compute_text_similarity(doc_content, selected_content)
                    max_similarity = max(max_similarity, similarity)

                # MMR分数
                mmr_score = lambda_param * relevance - (1 - lambda_param) * max_similarity

                if mmr_score > best_score:
                    best_score = mmr_score
                    best_idx = i

            selected.append(remaining.pop(best_idx))

        return selected

    # ==================== Metadata过滤 ====================

    @staticmethod
    def metadata_filter(
        documents: List[Dict[str, Any]], filters: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """Metadata过滤

        根据元数据字段过滤文档列表

        Args:
            documents: 文档列表
            filters: 过滤条件，如{"kb_id": "1", "category": "产品"}

        Returns:
            过滤后的文档列表
        """
        if not filters:
            return documents

        filtered = []
        for doc in documents:
            match = True
            for key, value in filters.items():
                doc_value = doc.get(key)
                if doc_value is None:
                    match = False
                    break
                # 支持列表匹配（如kb_id在多个值中）
                if isinstance(value, list):
                    if doc_value not in value:
                        match = False
                        break
                elif doc_value != value:
                    match = False
                    break
            if match:
                filtered.append(doc)

        return filtered

    # ==================== 工具方法 ====================

    @staticmethod
    def _compute_text_similarity(text1: str, text2: str) -> float:
        """计算两段文本的简单相似度（字符级Jaccard相似度）

        Args:
            text1: 文本1
            text2: 文本2

        Returns:
            相似度分数 [0, 1]
        """
        if not text1 or not text2:
            return 0.0

        # 使用字符n-gram（2-gram）计算Jaccard相似度
        def get_ngrams(text: str, n: int = 2) -> set:
            return {text[i : i + n] for i in range(len(text) - n + 1)}

        ngrams1 = get_ngrams(text1)
        ngrams2 = get_ngrams(text2)

        if not ngrams1 or not ngrams2:
            return 0.0

        intersection = ngrams1 & ngrams2
        union = ngrams1 | ngrams2

        return len(intersection) / len(union) if union else 0.0
