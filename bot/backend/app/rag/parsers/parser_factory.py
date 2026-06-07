"""文档解析器工厂"""

from typing import Dict, Type

from loguru import logger

from app.rag.parsers.base import BaseParser
from app.rag.parsers.excel_parser import ExcelParser
from app.rag.parsers.pdf_parser import PDFParser
from app.rag.parsers.txt_parser import TXTParser
from app.rag.parsers.word_parser import WordParser


class ParserFactory:
    """文档解析器工厂，根据文件类型返回对应的解析器实例"""

    # 文件类型到解析器类的映射
    _parser_map: Dict[str, Type[BaseParser]] = {
        "pdf": PDFParser,
        "docx": WordParser,
        "xlsx": ExcelParser,
        "txt": TXTParser,
        "md": TXTParser,  # Markdown使用TXT解析器
        "csv": ExcelParser,  # CSV使用Excel解析器
    }

    @staticmethod
    def get_parser(file_type: str) -> BaseParser:
        """根据文件类型获取对应的解析器

        Args:
            file_type: 文件类型（pdf/docx/xlsx/txt/md/csv）

        Returns:
            对应的解析器实例

        Raises:
            ValueError: 不支持的文件类型
        """
        file_type = file_type.lower().strip()

        parser_class = ParserFactory._parser_map.get(file_type)
        if parser_class is None:
            supported = ", ".join(ParserFactory._parser_map.keys())
            raise ValueError(f"不支持的文件类型: {file_type}，支持的类型: {supported}")

        logger.debug(f"为文件类型 '{file_type}' 创建解析器: {parser_class.__name__}")
        return parser_class()

    @staticmethod
    def get_supported_types() -> list:
        """获取支持的文件类型列表

        Returns:
            支持的文件类型列表
        """
        return list(ParserFactory._parser_map.keys())
