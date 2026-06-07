"""系统配置、操作日志 ORM 模型 - 匹配数据库表结构"""

from sqlalchemy import BigInteger, Column, DateTime, Integer, String, Text, JSON, func

from app.core.database import Base


class SystemConfig(Base):
    """系统配置模型，对应 system_configs 表"""

    __tablename__ = "system_configs"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="配置ID")
    config_key = Column(String(128), unique=True, nullable=False, comment="配置键")
    config_value = Column(Text, nullable=False, comment="配置值")
    description = Column(String(255), nullable=True, comment="配置描述")
    value_type = Column(String(32), default="string", comment="值类型: string/int/float/json/bool")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<SystemConfig(id={self.id}, config_key='{self.config_key}')>"


class OperationLog(Base):
    """操作日志模型，对应 operation_logs 表"""

    __tablename__ = "operation_logs"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="日志ID")
    user_id = Column(BigInteger, nullable=True, comment="操作用户ID")
    action = Column(String(64), nullable=False, comment="操作类型")
    resource = Column(String(64), nullable=False, comment="操作资源")
    detail = Column(JSON, nullable=True, comment="操作详情(JSON)")
    ip_address = Column(String(45), nullable=True, comment="IP地址")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<OperationLog(id={self.id}, action='{self.action}')>"
