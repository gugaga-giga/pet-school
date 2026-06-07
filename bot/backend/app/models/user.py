"""用户、角色、权限 ORM 模型 - 匹配数据库表结构"""

from datetime import datetime

from sqlalchemy import BigInteger, Boolean, Column, DateTime, ForeignKey, Integer, String, Text, JSON, func
from sqlalchemy.orm import relationship

from app.core.database import Base


class User(Base):
    """用户模型，对应 users 表"""

    __tablename__ = "users"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="用户ID")
    username = Column(String(64), unique=True, nullable=False, comment="用户名")
    email = Column(String(128), unique=True, nullable=False, comment="邮箱")
    password_hash = Column(String(255), nullable=False, comment="密码哈希")
    nickname = Column(String(64), nullable=True, comment="昵称")
    avatar = Column(String(512), nullable=True, comment="头像URL")
    phone = Column(String(20), nullable=True, comment="手机号")
    status = Column(Integer, default=1, comment="状态: 1正常 0禁用")
    is_deleted = Column(Integer, default=0, comment="是否删除: 0否 1是")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    roles = relationship("Role", secondary="user_roles", back_populates="users", lazy="selectin")

    @property
    def is_active(self) -> bool:
        return self.status == 1 and self.is_deleted == 0

    def __repr__(self) -> str:
        return f"<User(id={self.id}, username='{self.username}')>"


class Role(Base):
    """角色模型，对应 roles 表"""

    __tablename__ = "roles"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="角色ID")
    name = Column(String(64), nullable=False, comment="角色名称")
    code = Column(String(64), unique=True, nullable=False, comment="角色编码")
    description = Column(String(255), nullable=True, comment="角色描述")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    users = relationship("User", secondary="user_roles", back_populates="roles", lazy="selectin")
    permissions = relationship(
        "Permission", secondary="role_permissions", back_populates="roles", lazy="selectin"
    )

    def __repr__(self) -> str:
        return f"<Role(id={self.id}, name='{self.name}')>"


class Permission(Base):
    """权限模型，对应 permissions 表"""

    __tablename__ = "permissions"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="权限ID")
    name = Column(String(64), nullable=False, comment="权限名称")
    code = Column(String(128), unique=True, nullable=False, comment="权限编码")
    resource = Column(String(64), nullable=False, comment="资源")
    action = Column(String(32), nullable=False, comment="操作")
    description = Column(String(255), nullable=True, comment="权限描述")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    roles = relationship("Role", secondary="role_permissions", back_populates="permissions", lazy="selectin")

    def __repr__(self) -> str:
        return f"<Permission(id={self.id}, code='{self.code}')>"


class UserRole(Base):
    """用户-角色关联模型，对应 user_roles 表"""

    __tablename__ = "user_roles"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="ID")
    user_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, comment="用户ID")
    role_id = Column(BigInteger, ForeignKey("roles.id", ondelete="CASCADE"), nullable=False, comment="角色ID")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")


class RolePermission(Base):
    """角色-权限关联模型，对应 role_permissions 表"""

    __tablename__ = "role_permissions"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="ID")
    role_id = Column(BigInteger, ForeignKey("roles.id", ondelete="CASCADE"), nullable=False, comment="角色ID")
    permission_id = Column(
        BigInteger, ForeignKey("permissions.id", ondelete="CASCADE"), nullable=False, comment="权限ID"
    )
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
