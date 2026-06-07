"""应用配置模块，使用 pydantic-settings 管理所有配置项"""

import os
from typing import List

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """应用全局配置类，从环境变量或 .env 文件加载配置"""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )

    # ==================== 数据库配置 ====================
    DATABASE_URL: str = "mysql+pymysql://root:123456@localhost:3306/chatbot"
    DATABASE_POOL_SIZE: int = 20
    DATABASE_MAX_OVERFLOW: int = 10

    # ==================== JWT 配置 ====================
    SECRET_KEY: str = "your-secret-key-change-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    # ==================== 文件上传配置 ====================
    UPLOAD_DIR: str = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "uploads")
    MAX_UPLOAD_SIZE: int = 50 * 1024 * 1024  # 50MB

    # ==================== LLM 配置 ====================
    LLM_API_KEY: str = "sk-3b6348001a484079bc5a52f66463fd29"
    LLM_BASE_URL: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    LLM_MODEL_NAME: str = "qwen-plus"

    # ==================== Embedding 配置 ====================
    EMBEDDING_MODEL_NAME: str = "BAAI/bge-m3"
    EMBEDDING_DIMENSION: int = 1024

    # ==================== RAG 配置 ====================
    CHUNK_SIZE: int = 500
    CHUNK_OVERLAP: int = 100
    FAISS_INDEX_DIR: str = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "faiss_indexes")

    # ==================== Redis 配置 ====================
    REDIS_URL: str = "redis://localhost:6379/0"

    # ==================== CORS 配置 ====================
    CORS_ORIGINS: List[str] = [
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:5173",
    ]

    # ==================== 应用配置 ====================
    APP_NAME: str = "智能售前客服机器人"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = True


# 创建全局配置实例
settings = Settings()
