"""聊天会话、聊天消息、问答日志 ORM 模型 - 匹配数据库表结构"""

from sqlalchemy import BigInteger, Column, DateTime, ForeignKey, Integer, String, Text, JSON, func
from sqlalchemy.orm import relationship

from app.core.database import Base


class ChatSession(Base):
    """聊天会话模型，对应 chat_sessions 表"""

    __tablename__ = "chat_sessions"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="会话ID")
    user_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, comment="用户ID")
    title = Column(String(255), default="新对话", comment="会话标题")
    knowledge_base_id = Column(BigInteger, ForeignKey("knowledge_bases.id", ondelete="SET NULL"), nullable=True, comment="知识库ID")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    messages = relationship(
        "ChatMessage", back_populates="session", lazy="selectin", order_by="ChatMessage.created_at"
    )

    def __repr__(self) -> str:
        return f"<ChatSession(id={self.id}, title='{self.title}')>"


class ChatMessage(Base):
    """聊天消息模型，对应 chat_messages 表"""

    __tablename__ = "chat_messages"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="消息ID")
    session_id = Column(
        BigInteger, ForeignKey("chat_sessions.id", ondelete="CASCADE"), nullable=False, comment="会话ID"
    )
    role = Column(String(16), nullable=False, comment="角色: user/assistant/system")
    content = Column(Text, nullable=False, comment="消息内容")
    sources = Column(JSON, nullable=True, comment="引用来源(JSON)")
    intent_type = Column(String(64), nullable=True, comment="意图类型")
    recommended_products = Column(JSON, nullable=True, comment="推荐产品(JSON)")
    token_count = Column(Integer, default=0, comment="Token数量")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    session = relationship("ChatSession", back_populates="messages")

    def __repr__(self) -> str:
        return f"<ChatMessage(id={self.id}, role='{self.role}', session_id={self.session_id})>"


class QALog(Base):
    """问答日志模型，对应 qa_logs 表"""

    __tablename__ = "qa_logs"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="日志ID")
    user_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, comment="用户ID")
    session_id = Column(BigInteger, ForeignKey("chat_sessions.id", ondelete="CASCADE"), nullable=False, comment="会话ID")
    question = Column(Text, nullable=False, comment="用户问题")
    answer = Column(Text, nullable=False, comment="系统回答")
    intent_type = Column(String(64), nullable=True, comment="意图类型")
    sources = Column(JSON, nullable=True, comment="引用来源(JSON)")
    retrieval_time = Column(Integer, default=0, comment="检索耗时(ms)")
    llm_time = Column(Integer, default=0, comment="LLM耗时(ms)")
    total_time = Column(Integer, default=0, comment="总耗时(ms)")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<QALog(id={self.id})>"
