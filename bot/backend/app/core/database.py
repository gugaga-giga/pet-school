"""数据库连接与会话管理模块"""

from typing import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from app.core.config import settings

# 创建数据库引擎
engine = create_engine(
    settings.DATABASE_URL,
    pool_size=settings.DATABASE_POOL_SIZE,
    max_overflow=settings.DATABASE_MAX_OVERFLOW,
    pool_pre_ping=True,  # 连接池预检测，自动处理断开的连接
    echo=settings.DEBUG,  # 开发模式下打印SQL语句
)

# 创建会话工厂
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    """ORM模型基类，所有模型类需继承此类"""
    pass


def get_db() -> Generator:
    """获取数据库会话的依赖注入函数

    Yields:
        数据库会话实例
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
