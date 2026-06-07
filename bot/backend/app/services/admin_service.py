"""后台管理服务模块，处理仪表盘统计和系统管理"""

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.chat import ChatMessage, ChatSession, QALog
from app.models.knowledge import Document, DocumentChunk, KnowledgeBase
from app.models.pet import Pet
from app.models.product import Product
from app.models.system import OperationLog, SystemConfig
from app.models.user import User


class AdminService:
    """后台管理服务类"""

    @staticmethod
    def get_dashboard_statistics(db: Session) -> dict:
        """获取仪表盘统计数据"""
        # 用户统计
        total_users = db.query(User).filter(User.is_deleted == 0).count()
        active_users = db.query(User).filter(User.is_deleted == 0, User.status == 1).count()

        # 知识库统计
        total_knowledge_bases = db.query(KnowledgeBase).filter(KnowledgeBase.is_deleted == 0).count()
        total_documents = db.query(Document).filter(Document.is_deleted == 0).count()
        total_chunks = db.query(DocumentChunk).filter(DocumentChunk.is_deleted == 0).count()

        # 聊天统计
        total_sessions = db.query(ChatSession).filter(ChatSession.is_deleted == 0).count()
        total_messages = db.query(ChatMessage).filter(ChatMessage.is_deleted == 0).count()
        total_qa_logs = db.query(QALog).filter(QALog.is_deleted == 0).count()

        # 宠物统计
        total_pets = db.query(Pet).filter(Pet.is_deleted == 0).count()

        # 产品统计
        total_products = db.query(Product).filter(Product.is_deleted == 0).count()
        available_products = db.query(Product).filter(Product.is_deleted == 0, Product.status == 1).count()

        return {
            "users": {
                "total": total_users,
                "active": active_users,
            },
            "knowledge": {
                "total_knowledge_bases": total_knowledge_bases,
                "total_documents": total_documents,
                "total_chunks": total_chunks,
            },
            "chat": {
                "total_sessions": total_sessions,
                "total_messages": total_messages,
                "total_qa_logs": total_qa_logs,
            },
            "pets": {
                "total": total_pets,
            },
            "products": {
                "total": total_products,
                "available": available_products,
            },
        }

    @staticmethod
    def get_recent_activities(db: Session, limit: int = 10) -> list:
        """获取最近活动记录"""
        logs = (
            db.query(OperationLog)
            .filter(OperationLog.is_deleted == 0)
            .order_by(OperationLog.created_at.desc())
            .limit(limit)
            .all()
        )
        return [
            {
                "id": log.id,
                "user_id": log.user_id,
                "action": log.action,
                "resource": log.resource,
                "ip_address": log.ip_address,
                "created_at": str(log.created_at) if log.created_at else None,
            }
            for log in logs
        ]

    @staticmethod
    def get_system_configs(db: Session, group: str | None = None) -> list:
        """获取系统配置列表"""
        query = db.query(SystemConfig).filter(SystemConfig.is_deleted == 0)
        return query.all()

    @staticmethod
    def update_system_config(db: Session, key: str, value: str) -> SystemConfig:
        """更新系统配置"""
        from app.core.exceptions import NotFoundException

        config = db.query(SystemConfig).filter(SystemConfig.config_key == key).first()
        if not config:
            raise NotFoundException(message="配置项不存在")

        config.config_value = value
        db.commit()
        db.refresh(config)
        return config

    @staticmethod
    def get_chat_trend(db: Session, days: int = 7) -> list:
        """获取聊天趋势数据"""
        from datetime import timedelta, datetime
        from sqlalchemy import cast, Date

        now = datetime.now()
        start_date = now - timedelta(days=days)

        trend = (
            db.query(
                cast(ChatSession.created_at, Date).label("date"),
                func.count(ChatSession.id).label("session_count"),
            )
            .filter(ChatSession.created_at >= start_date, ChatSession.is_deleted == 0)
            .group_by(cast(ChatSession.created_at, Date))
            .order_by(cast(ChatSession.created_at, Date))
            .all()
        )

        return [
            {
                "date": str(row.date),
                "session_count": row.session_count,
            }
            for row in trend
        ]
