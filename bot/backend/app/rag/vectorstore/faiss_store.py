"""FAISS向量存储管理"""

import os
from typing import Any, Dict, List, Optional, Tuple

import faiss
import numpy as np
from loguru import logger


class FAISSVectorStore:
    """FAISS向量存储管理，每个知识库独立索引，使用IndexFlatIP（内积相似度）"""

    def __init__(self, index_dir: str, dimension: int = 1024) -> None:
        """初始化FAISS向量存储

        Args:
            index_dir: 索引文件存储目录
            dimension: 向量维度，默认1024（BGE-M3）
        """
        self.index_dir = index_dir
        self.dimension = dimension
        # 每个知识库一个FAISS索引
        self.indexes: Dict[str, faiss.IndexFlatIP] = {}
        # faiss内部ID -> chunk_id 的映射（每个知识库独立）
        self.id_maps: Dict[str, Dict[int, int]] = {}
        # chunk_id -> faiss内部ID 的反向映射
        self.reverse_id_maps: Dict[str, Dict[int, int]] = {}

        # 确保索引目录存在
        os.makedirs(self.index_dir, exist_ok=True)

    def create_index(self, kb_id: str) -> None:
        """为知识库创建FAISS索引（Inner Product，需先归一化向量）

        Args:
            kb_id: 知识库ID（字符串形式）
        """
        index = faiss.IndexFlatIP(self.dimension)
        self.indexes[kb_id] = index
        self.id_maps[kb_id] = {}
        self.reverse_id_maps[kb_id] = {}
        logger.info(f"为知识库 {kb_id} 创建FAISS索引，维度: {self.dimension}")

    def add_vectors(
        self, kb_id: str, vectors: np.ndarray, chunk_ids: List[int]
    ) -> None:
        """添加向量到索引

        Args:
            kb_id: 知识库ID
            vectors: 向量矩阵，shape=(n, dimension)
            chunk_ids: 对应的chunk_id列表

        Raises:
            ValueError: 知识库索引不存在或参数不匹配
        """
        if kb_id not in self.indexes:
            self.create_index(kb_id)

        if len(vectors) != len(chunk_ids):
            raise ValueError(
                f"向量数量({len(vectors)})与chunk_id数量({len(chunk_ids)})不匹配"
            )

        if vectors.shape[1] != self.dimension:
            raise ValueError(
                f"向量维度({vectors.shape[1]})与索引维度({self.dimension})不匹配"
            )

        # L2归一化（配合IndexFlatIP等价于余弦相似度）
        faiss.normalize_L2(vectors)

        # 记录当前索引大小，作为新向量的起始faiss_id
        start_id = self.indexes[kb_id].ntotal

        # 添加向量
        self.indexes[kb_id].add(vectors.astype(np.float32))

        # 更新ID映射
        for i, chunk_id in enumerate(chunk_ids):
            faiss_id = start_id + i
            self.id_maps[kb_id][faiss_id] = chunk_id
            self.reverse_id_maps[kb_id][chunk_id] = faiss_id

        logger.info(
            f"知识库 {kb_id} 添加 {len(chunk_ids)} 个向量，"
            f"当前总数: {self.indexes[kb_id].ntotal}"
        )

    def search(
        self, kb_id: str, query_vector: np.ndarray, top_k: int = 5
    ) -> List[Tuple[int, float]]:
        """检索最相似的向量

        Args:
            kb_id: 知识库ID
            query_vector: 查询向量，shape=(dimension,) 或 (1, dimension)
            top_k: 返回最相似的k个结果

        Returns:
            [(chunk_id, score), ...] 列表，按相似度降序排列

        Raises:
            ValueError: 知识库索引不存在
        """
        if kb_id not in self.indexes:
            raise ValueError(f"知识库 {kb_id} 的索引不存在")

        if self.indexes[kb_id].ntotal == 0:
            return []

        # 确保查询向量是2D
        if query_vector.ndim == 1:
            query_vector = query_vector.reshape(1, -1)

        # L2归一化查询向量
        faiss.normalize_L2(query_vector)

        # 限制top_k不超过索引中的向量数
        actual_k = min(top_k, self.indexes[kb_id].ntotal)

        # 执行检索
        scores, faiss_ids = self.indexes[kb_id].search(
            query_vector.astype(np.float32), actual_k
        )

        # 转换结果
        results: List[Tuple[int, float]] = []
        for i in range(actual_k):
            faiss_id = int(faiss_ids[0][i])
            score = float(scores[0][i])
            chunk_id = self.id_maps[kb_id].get(faiss_id)
            if chunk_id is not None:
                results.append((chunk_id, score))

        return results

    def delete_vectors(self, kb_id: str, chunk_ids: List[int]) -> None:
        """删除向量（FAISS不支持直接删除，需重建索引）

        Args:
            kb_id: 知识库ID
            chunk_ids: 要删除的chunk_id列表
        """
        if kb_id not in self.indexes:
            logger.warning(f"知识库 {kb_id} 的索引不存在，无需删除")
            return

        if not chunk_ids:
            return

        # 收集需要保留的向量
        chunk_ids_set = set(chunk_ids)
        remaining_vectors: List[np.ndarray] = []
        remaining_chunk_ids: List[int] = []

        index = self.indexes[kb_id]
        id_map = self.id_maps[kb_id]

        for faiss_id, chunk_id in id_map.items():
            if chunk_id not in chunk_ids_set:
                # 重建向量：从索引中提取
                vector = index.reconstruct(faiss_id)
                remaining_vectors.append(vector)
                remaining_chunk_ids.append(chunk_id)

        # 重建索引
        if remaining_vectors:
            vectors = np.array(remaining_vectors)
            self.rebuild_index(kb_id, vectors, remaining_chunk_ids)
        else:
            # 没有剩余向量，创建空索引
            self.create_index(kb_id)

        logger.info(
            f"知识库 {kb_id} 删除 {len(chunk_ids)} 个向量，"
            f"剩余 {len(remaining_chunk_ids)} 个"
        )

    def save_index(self, kb_id: str) -> None:
        """保存索引到磁盘

        Args:
            kb_id: 知识库ID

        Raises:
            ValueError: 知识库索引不存在
        """
        if kb_id not in self.indexes:
            raise ValueError(f"知识库 {kb_id} 的索引不存在")

        # 确保目录存在
        kb_dir = os.path.join(self.index_dir, kb_id)
        os.makedirs(kb_dir, exist_ok=True)

        # 保存FAISS索引
        index_path = os.path.join(kb_dir, "index.faiss")
        faiss.write_index(self.indexes[kb_id], index_path)

        # 保存ID映射
        id_map_path = os.path.join(kb_dir, "id_map.npy")
        np.save(id_map_path, self.id_maps[kb_id])

        logger.info(f"知识库 {kb_id} 索引已保存到 {kb_dir}")

    def load_index(self, kb_id: str) -> bool:
        """从磁盘加载索引

        Args:
            kb_id: 知识库ID

        Returns:
            是否加载成功
        """
        kb_dir = os.path.join(self.index_dir, kb_id)
        index_path = os.path.join(kb_dir, "index.faiss")
        id_map_path = os.path.join(kb_dir, "id_map.npy")

        if not os.path.exists(index_path):
            logger.debug(f"知识库 {kb_id} 的索引文件不存在: {index_path}")
            return False

        try:
            # 加载FAISS索引
            self.indexes[kb_id] = faiss.read_index(index_path)

            # 加载ID映射
            if os.path.exists(id_map_path):
                loaded_map = np.load(id_map_path, allow_pickle=True).item()
                self.id_maps[kb_id] = loaded_map
                # 构建反向映射
                self.reverse_id_maps[kb_id] = {
                    v: k for k, v in loaded_map.items()
                }
            else:
                self.id_maps[kb_id] = {}
                self.reverse_id_maps[kb_id] = {}

            logger.info(
                f"知识库 {kb_id} 索引加载成功，"
                f"向量数: {self.indexes[kb_id].ntotal}"
            )
            return True

        except Exception as e:
            logger.error(f"知识库 {kb_id} 索引加载失败: {e}")
            return False

    def rebuild_index(
        self, kb_id: str, vectors: np.ndarray, chunk_ids: List[int]
    ) -> None:
        """重建索引

        Args:
            kb_id: 知识库ID
            vectors: 全部向量矩阵
            chunk_ids: 对应的chunk_id列表
        """
        # 先删除旧索引
        if kb_id in self.indexes:
            del self.indexes[kb_id]
        if kb_id in self.id_maps:
            del self.id_maps[kb_id]
        if kb_id in self.reverse_id_maps:
            del self.reverse_id_maps[kb_id]

        # 创建新索引并添加向量
        self.create_index(kb_id)
        if len(vectors) > 0:
            self.add_vectors(kb_id, vectors, chunk_ids)

        logger.info(f"知识库 {kb_id} 索引重建完成，向量数: {len(chunk_ids)}")

    def get_index_info(self, kb_id: str) -> Dict[str, Any]:
        """获取索引信息

        Args:
            kb_id: 知识库ID

        Returns:
            索引信息字典
        """
        if kb_id not in self.indexes:
            return {
                "kb_id": kb_id,
                "exists": False,
                "total_vectors": 0,
            }

        return {
            "kb_id": kb_id,
            "exists": True,
            "total_vectors": self.indexes[kb_id].ntotal,
            "dimension": self.dimension,
            "mapped_chunks": len(self.id_maps.get(kb_id, {})),
        }
