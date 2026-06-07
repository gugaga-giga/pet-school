"""文档服务模块，处理文档上传、解析、删除、状态管理"""

import os
import uuid
from typing import Optional

from fastapi import UploadFile
from loguru import logger
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.exceptions import BadRequestException, FileUploadException, NotFoundException
from app.models.knowledge import Document, DocumentChunk, KnowledgeBase
from app.utils.pagination import paginate


class DocumentService:
    """文档服务类"""

    @staticmethod
    def get_document_by_id(db: Session, doc_id: int) -> Document:
        """根据ID获取文档

        Args:
            db: 数据库会话
            doc_id: 文档ID

        Returns:
            文档对象

        Raises:
            NotFoundException: 文档不存在
        """
        doc = db.query(Document).filter(Document.id == doc_id).first()
        if not doc:
            raise NotFoundException(message="文档不存在")
        return doc

    @staticmethod
    def get_document_list(
        db: Session,
        knowledge_base_id: Optional[int] = None,
        page: int = 1,
        page_size: int = 10,
        status: Optional[str] = None,
    ) -> dict:
        """获取文档列表（分页）

        Args:
            db: 数据库会话
            knowledge_base_id: 知识库ID筛选
            page: 页码
            page_size: 每页数量
            status: 状态筛选

        Returns:
            分页结果字典
        """
        query = db.query(Document)

        if knowledge_base_id:
            query = query.filter(Document.knowledge_base_id == knowledge_base_id)

        if status:
            query = query.filter(Document.status == status)

        query = query.order_by(Document.created_at.desc())

        return paginate(query, page, page_size)

    @staticmethod
    async def upload_document(
        db: Session,
        knowledge_base_id: int,
        file: UploadFile,
        user_id: Optional[int] = None,
    ) -> Document:
        """上传文档

        Args:
            db: 数据库会话
            knowledge_base_id: 知识库ID
            file: 上传的文件
            user_id: 上传者ID

        Returns:
            创建的文档对象

        Raises:
            NotFoundException: 知识库不存在
            FileUploadException: 文件上传失败
        """
        # 验证知识库存在
        kb = db.query(KnowledgeBase).filter(KnowledgeBase.id == knowledge_base_id).first()
        if not kb:
            raise NotFoundException(message="知识库不存在")

        # 验证文件类型
        allowed_types = {"pdf", "docx", "xlsx", "txt", "md", "csv"}
        file_ext = file.filename.split(".")[-1].lower() if file.filename else ""
        if file_ext not in allowed_types:
            raise FileUploadException(
                message=f"不支持的文件类型: {file_ext}",
                detail=f"支持的类型: {', '.join(allowed_types)}",
            )

        # 验证文件大小
        content = await file.read()
        if len(content) > settings.MAX_UPLOAD_SIZE:
            raise FileUploadException(
                message="文件大小超过限制",
                detail=f"最大允许: {settings.MAX_UPLOAD_SIZE // (1024*1024)}MB",
            )

        # 保存文件
        upload_dir = os.path.join(settings.UPLOAD_DIR, str(knowledge_base_id))
        os.makedirs(upload_dir, exist_ok=True)

        file_name = f"{uuid.uuid4().hex}.{file_ext}"
        file_path = os.path.join(upload_dir, file_name)

        try:
            with open(file_path, "wb") as f:
                f.write(content)
        except Exception as e:
            logger.error(f"文件保存失败: {e}")
            raise FileUploadException(message="文件保存失败", detail=str(e))

        # 创建文档记录
        doc = Document(
            knowledge_base_id=knowledge_base_id,
            title=file.filename or file_name,
            file_name=file.filename or file_name,
            file_path=file_path,
            file_type=file_ext,
            file_size=len(content),
            status="pending",
        )
        db.add(doc)

        # 更新知识库文档计数
        kb.document_count += 1

        db.commit()
        db.refresh(doc)

        logger.info(f"文档上传成功: {doc.id} - {doc.title}")
        return doc

    @staticmethod
    def delete_document(db: Session, doc_id: int) -> None:
        """删除文档

        Args:
            db: 数据库会话
            doc_id: 文档ID

        Raises:
            NotFoundException: 文档不存在
        """
        doc = DocumentService.get_document_by_id(db, doc_id)

        # 删除物理文件
        if os.path.exists(doc.file_path):
            try:
                os.remove(doc.file_path)
            except Exception as e:
                logger.warning(f"物理文件删除失败: {doc.file_path}, 错误: {e}")

        # 更新知识库文档计数
        kb = db.query(KnowledgeBase).filter(KnowledgeBase.id == doc.knowledge_base_id).first()
        if kb:
            kb.document_count = max(0, kb.document_count - 1)

        db.delete(doc)
        db.commit()

    @staticmethod
    def update_document_status(
        db: Session, doc_id: int, status: str, error_message: Optional[str] = None
    ) -> Document:
        """更新文档处理状态

        Args:
            db: 数据库会话
            doc_id: 文档ID
            status: 新状态
            error_message: 错误信息

        Returns:
            更新后的文档对象

        Raises:
            NotFoundException: 文档不存在
        """
        doc = DocumentService.get_document_by_id(db, doc_id)
        doc.status = status
        if error_message:
            doc.error_message = error_message
        db.commit()
        db.refresh(doc)
        return doc

    @staticmethod
    def get_document_chunks(
        db: Session, doc_id: int, page: int = 1, page_size: int = 20
    ) -> dict:
        """获取文档分块列表

        Args:
            db: 数据库会话
            doc_id: 文档ID
            page: 页码
            page_size: 每页数量

        Returns:
            分页结果字典
        """
        query = db.query(DocumentChunk).filter(DocumentChunk.document_id == doc_id)
        query = query.order_by(DocumentChunk.chunk_index.asc())
        return paginate(query, page, page_size)
