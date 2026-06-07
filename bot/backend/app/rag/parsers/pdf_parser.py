"""PDF文档解析器"""

from typing import Any, Dict, List

from loguru import logger

from app.rag.parsers.base import BaseParser


class PDFParser(BaseParser):
    """PDF文档解析器，使用PyPDF2解析PDF文件"""

    def parse(self, file_path: str) -> List[Dict[str, Any]]:
        """解析PDF文档，按页分割

        Args:
            file_path: PDF文件路径

        Returns:
            解析结果列表，每页一个条目:
                - content: 页面文本内容
                - metadata: 包含page_number等信息

        Raises:
            FileNotFoundError: 文件不存在
            Exception: PDF解析失败
        """
        from PyPDF2 import PdfReader

        results: List[Dict[str, Any]] = []

        try:
            reader = PdfReader(file_path)
            total_pages = len(reader.pages)
            logger.info(f"开始解析PDF文件: {file_path}，共 {total_pages} 页")

            for page_num, page in enumerate(reader.pages, start=1):
                try:
                    text = page.extract_text()
                    if not text or not text.strip():
                        continue

                    cleaned_text = self.clean_text(text)
                    if not cleaned_text:
                        continue

                    results.append(
                        {
                            "content": cleaned_text,
                            "metadata": {
                                "page_number": page_num,
                                "total_pages": total_pages,
                                "source": file_path,
                            },
                        }
                    )
                except Exception as e:
                    logger.warning(f"PDF第 {page_num} 页解析失败: {e}")
                    continue

            # 尝试提取表格数据
            table_texts = self._extract_tables(file_path)
            for table_text in table_texts:
                results.append(
                    {
                        "content": table_text,
                        "metadata": {
                            "page_number": 0,
                            "total_pages": total_pages,
                            "source": file_path,
                            "type": "table",
                        },
                    }
                )

            logger.info(f"PDF解析完成，共提取 {len(results)} 个内容块")

        except FileNotFoundError:
            raise FileNotFoundError(f"文件不存在: {file_path}")
        except Exception as e:
            logger.error(f"PDF解析失败: {e}")
            raise Exception(f"PDF解析失败: {e}")

        return results

    def _extract_tables(self, file_path: str) -> List[str]:
        """尝试提取PDF中的表格内容

        Args:
            file_path: PDF文件路径

        Returns:
            表格文本列表
        """
        table_texts: List[str] = []
        try:
            import pdfplumber

            with pdfplumber.open(file_path) as pdf:
                for page in pdf.pages:
                    tables = page.extract_tables()
                    for table in tables:
                        if not table:
                            continue
                        # 将表格转换为文本格式
                        rows = []
                        for row in table:
                            cells = [str(cell).strip() if cell else "" for cell in row]
                            rows.append(" | ".join(cells))
                        table_text = "\n".join(rows)
                        if table_text.strip():
                            table_texts.append(self.clean_text(table_text))
        except ImportError:
            logger.debug("pdfplumber未安装，跳过表格提取")
        except Exception as e:
            logger.warning(f"PDF表格提取失败: {e}")

        return table_texts
