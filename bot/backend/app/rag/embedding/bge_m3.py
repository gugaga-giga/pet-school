"""BGE-M3 Embedding服务封装"""

from typing import List

import numpy as np
from loguru import logger
from sentence_transformers import SentenceTransformer


class BGEEmbedding:
    """BGE-M3 Embedding服务，基于sentence-transformers加载模型"""

    def __init__(self, model_name: str = "BAAI/bge-m3") -> None:
        """初始化Embedding模型

        Args:
            model_name: 模型名称，默认BAAI/bge-m3
        """
        self.model_name = model_name
        self.dimension: int = 1024  # BGE-M3输出维度
        self._model: SentenceTransformer | None = None
        self._device: str = "cpu"

    @property
    def model(self) -> SentenceTransformer:
        """延迟加载模型，首次访问时初始化"""
        if self._model is None:
            self._load_model()
        return self._model

    def _load_model(self) -> None:
        """加载模型，自动检测GPU/CPU"""
        try:
            import torch

            if torch.cuda.is_available():
                self._device = "cuda"
                logger.info(f"检测到GPU，使用CUDA加速: {torch.cuda.get_device_name(0)}")
            else:
                self._device = "cpu"
                logger.info("未检测到GPU，使用CPU模式")
        except ImportError:
            self._device = "cpu"
            logger.info("torch未安装，使用CPU模式")

        logger.info(f"正在加载Embedding模型: {self.model_name}，设备: {self._device}")
        self._model = SentenceTransformer(self.model_name, device=self._device)
        logger.info(f"Embedding模型加载完成，维度: {self.dimension}")

    def embed_query(self, text: str) -> List[float]:
        """单文本向量化

        Args:
            text: 输入文本

        Returns:
            文本对应的向量表示

        Raises:
            ValueError: 输入文本为空
        """
        if not text or not text.strip():
            raise ValueError("输入文本不能为空")

        vector = self.model.encode(text, normalize_embeddings=True)
        return vector.tolist()

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """批量文本向量化，支持batch处理

        Args:
            texts: 文本列表

        Returns:
            向量列表

        Raises:
            ValueError: 输入文本列表为空
        """
        if not texts:
            raise ValueError("输入文本列表不能为空")

        # 过滤空文本
        valid_texts = [t if t and t.strip() else " " for t in texts]

        vectors = self.model.encode(
            valid_texts,
            normalize_embeddings=True,
            batch_size=32,
            show_progress_bar=False,
        )
        return vectors.tolist()

    def embed_documents_with_progress(
        self, texts: List[str], batch_size: int = 32
    ) -> List[List[float]]:
        """带进度条的批量向量化

        Args:
            texts: 文本列表
            batch_size: 每批处理数量

        Returns:
            向量列表

        Raises:
            ValueError: 输入文本列表为空
        """
        if not texts:
            raise ValueError("输入文本列表不能为空")

        # 过滤空文本
        valid_texts = [t if t and t.strip() else " " for t in texts]

        logger.info(f"开始批量向量化，共 {len(valid_texts)} 条文本，batch_size={batch_size}")

        vectors = self.model.encode(
            valid_texts,
            normalize_embeddings=True,
            batch_size=batch_size,
            show_progress_bar=True,
        )

        logger.info(f"批量向量化完成，生成 {len(vectors)} 个向量")
        return vectors.tolist()

    def get_dimension(self) -> int:
        """获取向量维度

        Returns:
            向量维度
        """
        return self.dimension
