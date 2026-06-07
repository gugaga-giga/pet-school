"""TXT文本解析器"""

from typing import Any, Dict, List

from loguru import logger

from app.rag.parsers.base import BaseParser


class TXTParser(BaseParser):
    """TXT文本解析器，支持UTF-8/GBK自动检测编码，按段落分割"""

    def parse(self, file_path: str) -> List[Dict[str, Any]]:
        """解析TXT文本文件，按段落分割

        Args:
            file_path: TXT文件路径

        Returns:
            解析结果列表:
                - content: 段落文本内容
                - metadata: 包含paragraph_index等信息

        Raises:
            FileNotFoundError: 文件不存在
            Exception: 文件读取失败
        """
        results: List[Dict[str, Any]] = []

        try:
            text = self._read_file(file_path)
            if not text or not text.strip():
                logger.warning(f"TXT文件内容为空: {file_path}")
                return results

            logger.info(f"开始解析TXT文件: {file_path}，文本长度: {len(text)}")

            # 按段落分割（以空行分隔）
            paragraphs = self._split_paragraphs(text)

            for para_idx, paragraph in enumerate(paragraphs):
                cleaned = self.clean_text(paragraph)
                if not cleaned:
                    continue

                results.append(
                    {
                        "content": cleaned,
                        "metadata": {
                            "paragraph_index": para_idx,
                            "source": file_path,
                            "type": "text",
                        },
                    }
                )

            logger.info(f"TXT解析完成，共提取 {len(results)} 个段落")

        except FileNotFoundError:
            raise FileNotFoundError(f"文件不存在: {file_path}")
        except Exception as e:
            logger.error(f"TXT解析失败: {e}")
            raise Exception(f"TXT解析失败: {e}")

        return results

    def _read_file(self, file_path: str) -> str:
        """读取文件内容，自动检测编码

        Args:
            file_path: 文件路径

        Returns:
            文件文本内容
        """
        # 尝试的编码列表
        encodings = ["utf-8", "gbk", "gb2312", "gb18030", "big5", "latin-1"]

        for encoding in encodings:
            try:
                with open(file_path, "r", encoding=encoding) as f:
                    content = f.read()
                logger.debug(f"使用编码 {encoding} 成功读取文件")
                return content
            except (UnicodeDecodeError, UnicodeError):
                continue
            except Exception:
                continue

        # 所有编码都失败，使用二进制模式读取并忽略错误
        logger.warning(f"无法自动检测文件编码，使用utf-8并忽略错误: {file_path}")
        with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
            return f.read()

    def _split_paragraphs(self, text: str) -> List[str]:
        """将文本按段落分割

        优先按空行分割，如果没有空行则按换行符分割

        Args:
            text: 原始文本

        Returns:
            段落列表
        """
        # 按空行分割
        paragraphs = text.split("\n\n")

        # 如果只有一个段落，尝试按单换行分割
        if len(paragraphs) <= 1:
            paragraphs = text.split("\n")

        # 过滤空段落
        return [p for p in paragraphs if p.strip()]
