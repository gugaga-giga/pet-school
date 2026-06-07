"""API v1 路由聚合模块"""

from fastapi import APIRouter

from app.api.v1.admin import router as admin_router
from app.api.v1.auth import router as auth_router
from app.api.v1.chat import router as chat_router
from app.api.v1.documents import router as documents_router
from app.api.v1.knowledge import router as knowledge_router
from app.api.v1.logs import router as logs_router
from app.api.v1.pets import router as pets_router
from app.api.v1.products import router as products_router
from app.api.v1.retrieval import router as retrieval_router
from app.api.v1.users import router as users_router

# 创建v1版本API路由器
api_router = APIRouter(prefix="/api/v1")

# 注册所有子路由
api_router.include_router(auth_router)
api_router.include_router(users_router)
api_router.include_router(knowledge_router)
api_router.include_router(documents_router)
api_router.include_router(chat_router)
api_router.include_router(pets_router)
api_router.include_router(products_router)
api_router.include_router(logs_router)
api_router.include_router(admin_router)
api_router.include_router(retrieval_router)
