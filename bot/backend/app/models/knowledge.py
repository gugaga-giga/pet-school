"""知识库、文档、文档分块 ORM 模型 - 匹配数据库表结构"""

from sqlalchemy import BigInteger, Column, DateTime, ForeignKey, Integer, String, Text, JSON, func
from sqlalchemy.orm import relationship

from app.core.database import Base


class KnowledgeBase(Base):
    """知识库模型，对应 knowledge_bases 表"""

    __tablename__ = "knowledge_bases"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="知识库ID")
    name = Column(String(128), nullable=False, comment="知识库名称")
    description = Column(Text, nullable=True, comment="知识库描述")
    icon = Column(String(512), nullable=True, comment="图标")
    document_count = Column(Integer, default=0, comment="文档数量")
    chunk_count = Column(Integer, default=0, comment="分块数量")
    owner_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, comment="创建者ID")
    is_public = Column(Integer, default=0, comment="是否公开: 0否 1是")
    status = Column(Integer, default=1, comment="状态: 1正常 0禁用")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    documents = relationship("Document", back_populates="knowledge_base", lazy="selectin")

    def __repr__(self) -> str:
        return f"<KnowledgeBase(id={self.id}, name='{self.name}')>"


class Document(Base):
    """文档模型，对应 documents 表"""

    __tablename__ = "documents"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="文档ID")
    knowledge_base_id = Column(
        BigInteger, ForeignKey("knowledge_bases.id", ondelete="CASCADE"), nullable=False, comment="知识库ID"
    )
    title = Column(String(255), nullable=False, comment="文档标题")
    file_name = Column(String(255), nullable=False, comment="原始文件名")
    file_path = Column(String(512), nullable=False, comment="文件存储路径")
    file_type = Column(String(32), nullable=False, comment="文件类型")
    file_size = Column(BigInteger, default=0, comment="文件大小(字节)")
    chunk_count = Column(Integer, default=0, comment="分块数量")
    status = Column(String(32), default="pending", comment="处理状态: pending/processing/completed/failed")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    knowledge_base = relationship("KnowledgeBase", back_populates="documents")
    chunks = relationship("DocumentChunk", back_populates="document", lazy="selectin")

    def __repr__(self) -> str:
        return f"<Document(id={self.id}, title='{self.title}')>"


class DocumentChunk(Base):
    """文档分块模型，对应 document_chunks 表"""

    __tablename__ = "document_chunks"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="分块ID")
    document_id = Column(
        BigInteger, ForeignKey("documents.id", ondelete="CASCADE"), nullable=False, comment="文档ID"
    )
    knowledge_base_id = Column(
        BigInteger, ForeignKey("knowledge_bases.id", ondelete="CASCADE"), nullable=False, comment="知识库ID"
    )
    chunk_index = Column(Integer, nullable=False, comment="分块索引")
    content = Column(Text, nullable=False, comment="分块内容")
    token_count = Column(Integer, default=0, comment="Token数量")
    faiss_id = Column(BigInteger, nullable=True, comment="FAISS向量索引位置")
    chunk_metadata = Column("metadata", JSON, nullable=True, comment="元数据(JSON)")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    document = relationship("Document", back_populates="chunks")

    def __repr__(self) -> str:
        return f"<DocumentChunk(id={self.id}, document_id={self.document_id}, index={self.chunk_index})>"
