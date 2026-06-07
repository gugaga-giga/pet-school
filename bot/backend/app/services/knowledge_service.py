"""知识库服务模块，处理知识库CRUD和统计 - 匹配ORM模型"""

from typing import Optional

from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundException
from app.models.knowledge import KnowledgeBase
from app.schemas.knowledge import KnowledgeBaseCreate, KnowledgeBaseUpdate
from app.utils.pagination import paginate


class KnowledgeService:
    """知识库服务类"""

    @staticmethod
    def get_knowledge_base_by_id(db: Session, kb_id: int) -> KnowledgeBase:
        kb = db.query(KnowledgeBase).filter(
            KnowledgeBase.id == kb_id, KnowledgeBase.is_deleted == 0
        ).first()
        if not kb:
            raise NotFoundException(message="知识库不存在")
        return kb

    @staticmethod
    def get_knowledge_base_list(
        db: Session,
        page: int = 1,
        page_size: int = 10,
        keyword: Optional[str] = None,
        is_active: Optional[bool] = None,
    ) -> dict:
        query = db.query(KnowledgeBase).filter(KnowledgeBase.is_deleted == 0)

        if keyword:
            query = query.filter(
                (KnowledgeBase.name.like(f"%{keyword}%"))
                | (KnowledgeBase.description.like(f"%{keyword}%"))
            )

        if is_active is not None:
            status_val = 1 if is_active else 0
            query = query.filter(KnowledgeBase.status == status_val)

        query = query.order_by(KnowledgeBase.created_at.desc())
        return paginate(query, page, page_size)

    @staticmethod
    def create_knowledge_base(
        db: Session, kb_data: KnowledgeBaseCreate, user_id: Optional[int] = None
    ) -> KnowledgeBase:
        kb = KnowledgeBase(
            name=kb_data.name,
            description=kb_data.description,
            icon=kb_data.icon,
            is_public=kb_data.is_public or 0,
            owner_id=user_id or 1,
        )
        db.add(kb)
        db.commit()
        db.refresh(kb)
        return kb

    @staticmethod
    def update_knowledge_base(
        db: Session, kb_id: int, kb_data: KnowledgeBaseUpdate
    ) -> KnowledgeBase:
        kb = KnowledgeService.get_knowledge_base_by_id(db, kb_id)
        update_fields = kb_data.model_dump(exclude_unset=True)
        for field, value in update_fields.items():
            setattr(kb, field, value)
        db.commit()
        db.refresh(kb)
        return kb

    @staticmethod
    def delete_knowledge_base(db: Session, kb_id: int) -> None:
        kb = KnowledgeService.get_knowledge_base_by_id(db, kb_id)
        kb.is_deleted = 1
        db.commit()

    @staticmethod
    def get_statistics(db: Session) -> dict:
        from app.models.knowledge import Document, DocumentChunk

        total_count = db.query(KnowledgeBase).filter(KnowledgeBase.is_deleted == 0).count()
        active_count = db.query(KnowledgeBase).filter(
            KnowledgeBase.status == 1, KnowledgeBase.is_deleted == 0
        ).count()
        document_count = db.query(Document).filter(Document.is_deleted == 0).count()
        chunk_count = db.query(DocumentChunk).filter(DocumentChunk.is_deleted == 0).count()

        return {
            "total_knowledge_bases": total_count,
            "active_knowledge_bases": active_count,
            "total_documents": document_count,
            "total_chunks": chunk_count,
        }
