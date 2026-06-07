"""文档解析器基类"""

import re
from abc import ABC, abstractmethod
from typing import Any, Dict, List


class BaseParser(ABC):
    """文档解析器抽象基类，所有解析器需继承此类并实现parse方法"""

    @abstractmethod
    def parse(self, file_path: str) -> List[Dict[str, Any]]:
        """解析文档，返回内容列表

        Args:
            file_path: 文件路径

        Returns:
            解析结果列表，每项包含:
                - content: 文本内容
                - metadata: 元数据字典（页码、段落、sheet名等）
        """

    def clean_text(self, text: str) -> str:
        """清洗文本，去除多余空白和特殊字符

        Args:
            text: 原始文本

        Returns:
            清洗后的文本
        """
        if not text:
            return ""

        # 替换制表符为空格
        text = text.replace("\t", " ")
        # 合并连续空格
        text = re.sub(r" {2,}", " ", text)
        # 合并连续换行（最多保留两个换行，即一个空行）
        text = re.sub(r"\n{3,}", "\n\n", text)
        # 去除每行首尾空白
        lines = [line.strip() for line in text.split("\n")]
        text = "\n".join(lines)
        # 去除首尾空白
        text = text.strip()

        return text
