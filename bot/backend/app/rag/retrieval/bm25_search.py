"""BM25关键词检索，基于rank-bm25库和jieba中文分词"""

import json
import os
from typing import Dict, List, Tuple

import jieba
from loguru import logger
from rank_bm25 import BM25Okapi


class BM25Search:
    """BM25关键词检索，支持中文分词、索引构建、检索、持久化"""

    def __init__(self) -> None:
        """初始化BM25检索器"""
        # kb_id -> 分词后的文档列表
        self.corpus: Dict[str, List[List[str]]] = {}
        # kb_id -> 原始文档列表（用于返回结果时引用）
        self.raw_documents: Dict[str, List[str]] = {}
        # kb_id -> BM25模型
        self.bm25: Dict[str, BM25Okapi] = {}

    # ==================== 索引构建 ====================

    def build_index(self, kb_id: str, documents: List[str]) -> None:
        """构建BM25索引

        Args:
            kb_id: 知识库ID
            documents: 文档文本列表
        """
        if not documents:
            logger.warning(f"知识库 {kb_id} 文档列表为空，跳过索引构建")
            return

        # 对每个文档进行jieba分词
        tokenized_corpus = [self._tokenize(doc) for doc in documents]

        self.corpus[kb_id] = tokenized_corpus
        self.raw_documents[kb_id] = documents
        self.bm25[kb_id] = BM25Okapi(tokenized_corpus)

        logger.info(
            f"知识库 {kb_id} BM25索引构建完成，文档数: {len(documents)}"
        )

    # ==================== 检索 ====================

    def search(
        self, kb_id: str, query: str, top_k: int = 5
    ) -> List[Tuple[int, float]]:
        """BM25检索

        Args:
            kb_id: 知识库ID
            query: 查询文本
            top_k: 返回前k个结果

        Returns:
            [(文档索引, BM25分数), ...] 列表，按分数降序排列

        Raises:
            ValueError: 知识库索引不存在
        """
        if kb_id not in self.bm25:
            raise ValueError(f"知识库 {kb_id} 的BM25索引不存在")

        if not query or not query.strip():
            return []

        # 对查询进行分词
        tokenized_query = self._tokenize(query)

        # 获取BM25分数
        scores = self.bm25[kb_id].get_scores(tokenized_query)

        # 按分数降序排序，取top_k
        scored_docs = [(idx, float(score)) for idx, score in enumerate(scores)]
        scored_docs.sort(key=lambda x: x[1], reverse=True)

        return scored_docs[:top_k]

    # ==================== 增量操作 ====================

    def add_documents(self, kb_id: str, documents: List[str]) -> None:
        """增量添加文档（重建索引）

        Args:
            kb_id: 知识库ID
            documents: 新增文档文本列表
        """
        if not documents:
            return

        # 合并已有文档和新增文档
        existing_docs = self.raw_documents.get(kb_id, [])
        all_docs = existing_docs + documents

        # 重建索引
        self.build_index(kb_id, all_docs)
        logger.info(
            f"知识库 {kb_id} 增量添加 {len(documents)} 个文档，"
            f"总文档数: {len(all_docs)}"
        )

    def remove_documents(self, kb_id: str, doc_indices: List[int]) -> None:
        """删除文档（重建索引）

        Args:
            kb_id: 知识库ID
            doc_indices: 要删除的文档索引列表
        """
        if kb_id not in self.raw_documents:
            logger.warning(f"知识库 {kb_id} 不存在，无需删除")
            return

        if not doc_indices:
            return

        # 过滤掉要删除的文档
        indices_set = set(doc_indices)
        remaining_docs = [
            doc
            for idx, doc in enumerate(self.raw_documents[kb_id])
            if idx not in indices_set
        ]

        # 重建索引
        self.build_index(kb_id, remaining_docs)
        logger.info(
            f"知识库 {kb_id} 删除 {len(doc_indices)} 个文档，"
            f"剩余文档数: {len(remaining_docs)}"
        )

    # ==================== 持久化 ====================

    def save_index(self, kb_id: str, path: str) -> None:
        """保存索引到磁盘

        Args:
            kb_id: 知识库ID
            path: 保存目录路径
        """
        if kb_id not in self.bm25:
            raise ValueError(f"知识库 {kb_id} 的BM25索引不存在")

        os.makedirs(path, exist_ok=True)

        # 保存原始文档
        docs_path = os.path.join(path, f"{kb_id}_bm25_docs.json")
        with open(docs_path, "w", encoding="utf-8") as f:
            json.dump(self.raw_documents[kb_id], f, ensure_ascii=False, indent=2)

        # 保存分词结果
        tokens_path = os.path.join(path, f"{kb_id}_bm25_tokens.json")
        with open(tokens_path, "w", encoding="utf-8") as f:
            json.dump(self.corpus[kb_id], f, ensure_ascii=False, indent=2)

        logger.info(f"知识库 {kb_id} BM25索引已保存到 {path}")

    def load_index(self, kb_id: str, path: str) -> bool:
        """从磁盘加载索引

        Args:
            kb_id: 知识库ID
            path: 索引文件目录路径

        Returns:
            是否加载成功
        """
        docs_path = os.path.join(path, f"{kb_id}_bm25_docs.json")
        tokens_path = os.path.join(path, f"{kb_id}_bm25_tokens.json")

        if not os.path.exists(docs_path) or not os.path.exists(tokens_path):
            logger.debug(f"知识库 {kb_id} BM25索引文件不存在")
            return False

        try:
            # 加载原始文档
            with open(docs_path, "r", encoding="utf-8") as f:
                self.raw_documents[kb_id] = json.load(f)

            # 加载分词结果
            with open(tokens_path, "r", encoding="utf-8") as f:
                self.corpus[kb_id] = json.load(f)

            # 重建BM25模型
            self.bm25[kb_id] = BM25Okapi(self.corpus[kb_id])

            logger.info(
                f"知识库 {kb_id} BM25索引加载成功，文档数: {len(self.raw_documents[kb_id])}"
            )
            return True

        except Exception as e:
            logger.error(f"知识库 {kb_id} BM25索引加载失败: {e}")
            return False

    # ==================== 工具方法 ====================

    @staticmethod
    def _tokenize(text: str) -> List[str]:
        """使用jieba进行中文分词

        Args:
            text: 待分词文本

        Returns:
            分词结果列表（过滤停用词和短词）
        """
        if not text:
            return []

        # jieba分词，cut_all=False表示精确模式
        tokens = jieba.lcut(text)

        # 过滤：去除空白、单字符、纯数字
        filtered = [
            token.strip()
            for token in tokens
            if token.strip() and len(token.strip()) > 1 and not token.strip().isdigit()
        ]

        return filtered
