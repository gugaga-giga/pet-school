"""FastAPI应用入口模块

创建FastAPI应用实例，注册中间件、路由，配置Swagger文档，管理应用生命周期。
"""

import os
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

from app.api.v1 import api_router
from app.core.config import settings
from app.middleware.error_handler import ErrorHandlerMiddleware
from app.middleware.logging_middleware import LoggingMiddleware


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理

    启动时初始化资源，关闭时清理资源。
    """
    # ==================== 启动事件 ====================
    logger.info(f"🚀 {settings.APP_NAME} v{settings.APP_VERSION} 启动中...")

    # 创建必要的目录
    os.makedirs(settings.UPLOAD_DIR, exist_ok=True)
    os.makedirs(settings.FAISS_INDEX_DIR, exist_ok=True)

    logger.info(f"📁 上传目录: {settings.UPLOAD_DIR}")
    logger.info(f"📁 FAISS索引目录: {settings.FAISS_INDEX_DIR}")
    logger.info(f"✅ {settings.APP_NAME} 启动完成")

    yield

    # ==================== 关闭事件 ====================
    logger.info(f"👋 {settings.APP_NAME} 正在关闭...")


def create_app() -> FastAPI:
    """创建FastAPI应用实例

    Returns:
        配置好的FastAPI应用实例
    """
    app = FastAPI(
        title=settings.APP_NAME,
        description="基于Transformer和RAG的智能售前客服机器人后端API",
        version=settings.APP_VERSION,
        docs_url="/docs",
        redoc_url="/redoc",
        openapi_url="/openapi.json",
        lifespan=lifespan,
    )

    # ==================== 注册中间件 ====================
    # CORS中间件
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # 日志中间件
    app.add_middleware(LoggingMiddleware)

    # 全局异常处理中间件
    app.add_middleware(ErrorHandlerMiddleware)

    # ==================== 注册路由 ====================
    app.include_router(api_router)

    # 健康检查端点
    @app.get("/health", tags=["系统"], summary="健康检查")
    async def health_check():
        """健康检查接口，用于监控服务是否正常运行"""
        return {
            "status": "healthy",
            "app": settings.APP_NAME,
            "version": settings.APP_VERSION,
        }

    return app


# 创建应用实例
app = create_app()


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG,
    )
