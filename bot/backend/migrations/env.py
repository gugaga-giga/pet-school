"""Alembic迁移环境配置模块

配置数据库连接、元数据自动生成等迁移相关设置。
"""

from logging.config import fileConfig

from alembic import context
from sqlalchemy import engine_from_config, pool

from app.core.config import settings
from app.core.database import Base

# 导入所有模型，确保Base.metadata中包含所有表定义
from app.models import (  # noqa: F401
    ChatMessage,
    ChatSession,
    Document,
    DocumentChunk,
    KnowledgeBase,
    OperationLog,
    Pet,
    Permission,
    Product,
    QALog,
    RecommendRule,
    Role,
    RolePermission,
    SystemConfig,
    User,
    UserRole,
)

# Alembic Config对象
config = context.config

# 从应用配置中设置数据库连接字符串
config.set_main_option("sqlalchemy.url", settings.DATABASE_URL)

# 设置Python日志
if config.config_file_name is not None:
    fileConfig(config.config_file_name)

# 目标元数据，用于自动生成迁移脚本
target_metadata = Base.metadata


def run_migrations_offline() -> None:
    """离线模式运行迁移

    不需要连接数据库，仅生成SQL脚本。
    """
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )

    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    """在线模式运行迁移

    需要连接数据库，直接执行迁移。
    """
    connectable = engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    with connectable.connect() as connection:
        context.configure(
            connection=connection,
            target_metadata=target_metadata,
        )

        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
