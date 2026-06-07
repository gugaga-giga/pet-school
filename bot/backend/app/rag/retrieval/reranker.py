"""Rerank重排序器，基于CrossEncoder对检索结果进行精排"""

from typing import Any, Dict, List

from loguru import logger


class Reranker:
    """重排序器，使用CrossEncoder模型对检索结果进行语义相关性精排"""

    def __init__(self, model_name: str = "BAAI/bge-reranker-v2-m3") -> None:
        """初始化重排序器

        Args:
            model_name: CrossEncoder模型名称，默认BAAI/bge-reranker-v2-m3
        """
        self.model_name = model_name
        self._model = None

    @property
    def model(self):
        """延迟加载CrossEncoder模型"""
        if self._model is None:
            self._load_model()
        return self._model

    def _load_model(self) -> None:
        """加载CrossEncoder模型"""
        try:
            from sentence_transformers import CrossEncoder

            logger.info(f"正在加载Reranker模型: {self.model_name}")
            self._model = CrossEncoder(self.model_name)
            logger.info(f"Reranker模型加载完成: {self.model_name}")
        except ImportError:
            logger.warning(
                "sentence_transformers未安装CrossEncoder支持，"
                "Reranker将使用简单评分策略"
            )
            self._model = None
        except Exception as e:
            logger.error(f"Reranker模型加载失败: {e}，将使用简单评分策略")
            self._model = None

    def rerank(
        self,
        query: str,
        documents: List[Dict[str, Any]],
        top_k: int = 5,
    ) -> List[Dict[str, Any]]:
        """重排序文档列表

        对query与每个document计算相关性分数，按分数降序排列，返回top_k结果

        Args:
            query: 查询文本
            documents: 文档列表，每项需包含"content"字段
            top_k: 返回前k个结果

        Returns:
            重排序后的文档列表，每项新增"rerank_score"字段
        """
        if not documents:
            return []

        if not query or not query.strip():
            return documents[:top_k]

        # 如果模型不可用，使用简单评分策略
        if self._model is None:
            return self._simple_rerank(query, documents, top_k)

        try:
            # 构建query-document对
            pairs = [(query, doc.get("content", "")) for doc in documents]

            # 批量计算相关性分数
            scores = self.model.predict(pairs)

            # 将分数附加到文档并排序
            for idx, doc in enumerate(documents):
                doc["rerank_score"] = float(scores[idx])

            # 按rerank_score降序排序
            sorted_docs = sorted(
                documents, key=lambda x: x.get("rerank_score", 0.0), reverse=True
            )

            return sorted_docs[:top_k]

        except Exception as e:
            logger.error(f"Rerank失败，回退到简单评分: {e}")
            return self._simple_rerank(query, documents, top_k)

    def compute_score(self, query: str, document: str) -> float:
        """计算单个文档的相关性分数

        Args:
            query: 查询文本
            document: 文档文本

        Returns:
            相关性分数，越高越相关
        """
        if not query or not document:
            return 0.0

        if self._model is None:
            return self._simple_score(query, document)

        try:
            score = self.model.predict([(query, document)])
            return float(score[0])
        except Exception as e:
            logger.error(f"计算相关性分数失败: {e}")
            return self._simple_score(query, document)

    def _simple_rerank(
        self,
        query: str,
        documents: List[Dict[str, Any]],
        top_k: int,
    ) -> List[Dict[str, Any]]:
        """简单重排序策略（模型不可用时的降级方案）

        基于关键词匹配度进行简单评分

        Args:
            query: 查询文本
            documents: 文档列表
            top_k: 返回前k个结果

        Returns:
            重排序后的文档列表
        """
        for doc in documents:
            doc["rerank_score"] = self._simple_score(
                query, doc.get("content", "")
            )

        sorted_docs = sorted(
            documents, key=lambda x: x.get("rerank_score", 0.0), reverse=True
        )
        return sorted_docs[:top_k]

    @staticmethod
    def _simple_score(query: str, document: str) -> float:
        """简单关键词匹配评分

        计算query中的词在document中出现的比例

        Args:
            query: 查询文本
            document: 文档文本

        Returns:
            匹配分数 [0, 1]
        """
        if not query or not document:
            return 0.0

        query_lower = query.lower()
        doc_lower = document.lower()

        # 简单字符级匹配
        query_chars = set(query_lower)
        doc_chars = set(doc_lower)

        if not query_chars:
            return 0.0

        # 匹配字符比例
        matched = query_chars & doc_chars
        return len(matched) / len(query_chars)
