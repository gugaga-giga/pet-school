"""文本切分器"""

import re
from typing import Any, Dict, List


class TextChunker:
    """文本切分器，按chunk_size切分文本，支持overlap重叠和句子边界优先切分"""

    # 中英文句子结束标记
    SENTENCE_ENDINGS = re.compile(r"[。！？.!?\n]")

    def __init__(self, chunk_size: int = 500, chunk_overlap: int = 100) -> None:
        """初始化文本切分器

        Args:
            chunk_size: 每个chunk的最大字符数
            chunk_overlap: 相邻chunk之间的重叠字符数
        """
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap

    def chunk_text(
        self, text: str, metadata: Dict[str, Any] | None = None
    ) -> List[Dict[str, Any]]:
        """将文本切分为chunks

        优先在句子边界切分，支持overlap重叠

        Args:
            text: 待切分的文本
            metadata: 附加到每个chunk的元数据

        Returns:
            切分结果列表，每项包含:
                - content: chunk文本内容
                - metadata: 元数据（包含chunk_index）
        """
        if not text or not text.strip():
            return []

        if metadata is None:
            metadata = {}

        # 如果文本长度不超过chunk_size，直接返回
        if len(text) <= self.chunk_size:
            return [
                {
                    "content": text.strip(),
                    "metadata": {**metadata, "chunk_index": 0},
                }
            ]

        chunks: List[Dict[str, Any]] = []
        chunk_index = 0
        start = 0

        while start < len(text):
            # 确定当前chunk的结束位置
            end = start + self.chunk_size

            if end >= len(text):
                # 已经到达文本末尾
                chunk_text = text[start:].strip()
                if chunk_text:
                    chunks.append(
                        {
                            "content": chunk_text,
                            "metadata": {**metadata, "chunk_index": chunk_index},
                        }
                    )
                break

            # 尝试在句子边界切分
            split_pos = self._find_split_position(text, start, end)

            chunk_text = text[start:split_pos].strip()
            if chunk_text:
                chunks.append(
                    {
                        "content": chunk_text,
                        "metadata": {**metadata, "chunk_index": chunk_index},
                    }
                )
                chunk_index += 1

            # 计算下一个chunk的起始位置（考虑overlap）
            start = split_pos - self.chunk_overlap
            # 确保start向前推进，避免死循环
            if start <= (split_pos - self.chunk_size + self.chunk_overlap):
                start = split_pos

        return chunks

    def _find_split_position(self, text: str, start: int, end: int) -> int:
        """在指定范围内寻找最佳切分位置（优先句子边界）

        Args:
            text: 完整文本
            start: 当前chunk起始位置
            end: 当前chunk期望结束位置

        Returns:
            实际切分位置
        """
        # 在end附近寻找句子边界（向后搜索一小段距离）
        search_range = min(50, self.chunk_size // 4)

        # 从end位置向前搜索句子结束标记
        for pos in range(end, max(end - search_range, start), -1):
            if pos < len(text) and self.SENTENCE_ENDINGS.match(text[pos]):
                return pos + 1  # 包含句子结束标记

        # 没找到句子边界，在end附近搜索空格或换行
        for pos in range(end, max(end - search_range, start), -1):
            if pos < len(text) and text[pos] in (" ", "\n"):
                return pos + 1

        # 仍未找到合适的切分点，直接在end位置切分
        return end

    def chunk_documents(self, documents: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """批量切分文档

        Args:
            documents: 文档列表，每项包含content和metadata

        Returns:
            所有文档切分后的chunk列表
        """
        all_chunks: List[Dict[str, Any]] = []

        for doc in documents:
            content = doc.get("content", "")
            metadata = doc.get("metadata", {})
            chunks = self.chunk_text(content, metadata)
            all_chunks.extend(chunks)

        return all_chunks
