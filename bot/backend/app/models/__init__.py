"""ORM模型统一导入模块，导入所有模型以便Alembic检测"""

from app.models.chat import ChatMessage, ChatSession, QALog
from app.models.course import Course, Order
from app.models.knowledge import Document, DocumentChunk, KnowledgeBase
from app.models.pet import Pet
from app.models.product import Product, RecommendRule
from app.models.system import OperationLog, SystemConfig
from app.models.user import Permission, Role, RolePermission, User, UserRole

__all__ = [
    # 用户模块
    "User",
    "Role",
    "Permission",
    "UserRole",
    "RolePermission",
    # 知识库模块
    "KnowledgeBase",
    "Document",
    "DocumentChunk",
    # 聊天模块
    "ChatSession",
    "ChatMessage",
    "QALog",
    # 宠物模块
    "Pet",
    # 产品模块
    "Product",
    "RecommendRule",
    # 课程模块
    "Course",
    "Order",
    # 系统模块
    "SystemConfig",
    "OperationLog",
]
