"""用户相关的Pydantic Schema"""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class UserCreate(BaseModel):
    """创建用户请求"""

    username: str = Field(..., min_length=2, max_length=50, description="用户名")
    password: str = Field(..., min_length=6, max_length=100, description="密码")
    email: Optional[str] = Field(None, description="邮箱")
    phone: Optional[str] = Field(None, description="手机号")
    nickname: Optional[str] = Field(None, max_length=50, description="昵称")
    role_ids: Optional[list[int]] = Field(None, description="角色ID列表")
    is_active: bool = Field(default=True, description="是否激活")


class UserUpdate(BaseModel):
    """更新用户请求"""

    email: Optional[str] = Field(None, description="邮箱")
    phone: Optional[str] = Field(None, description="手机号")
    nickname: Optional[str] = Field(None, max_length=50, description="昵称")
    avatar: Optional[str] = Field(None, description="头像URL")
    is_active: Optional[bool] = Field(None, description="是否激活")
    role_ids: Optional[list[int]] = Field(None, description="角色ID列表")


class UserResponse(BaseModel):
    """用户响应"""

    id: int = Field(..., description="用户ID")
    username: str = Field(..., description="用户名")
    email: Optional[str] = Field(None, description="邮箱")
    phone: Optional[str] = Field(None, description="手机号")
    nickname: Optional[str] = Field(None, description="昵称")
    avatar: Optional[str] = Field(None, description="头像URL")
    is_active: bool = Field(..., description="是否激活")
    is_superuser: bool = Field(..., description="是否超级管理员")
    roles: list[dict] = Field(default_factory=list, description="角色列表")
    last_login_at: Optional[datetime] = Field(None, description="最后登录时间")
    created_at: datetime = Field(..., description="创建时间")
    updated_at: datetime = Field(..., description="更新时间")

    model_config = {"from_attributes": True}


class UserListResponse(BaseModel):
    """用户列表响应"""

    id: int = Field(..., description="用户ID")
    username: str = Field(..., description="用户名")
    email: Optional[str] = Field(None, description="邮箱")
    nickname: Optional[str] = Field(None, description="昵称")
    is_active: bool = Field(..., description="是否激活")
    is_superuser: bool = Field(..., description="是否超级管理员")
    roles: list[dict] = Field(default_factory=list, description="角色列表")
    created_at: datetime = Field(..., description="创建时间")

    model_config = {"from_attributes": True}


class PasswordChange(BaseModel):
    """修改密码请求"""

    old_password: str = Field(..., min_length=6, description="旧密码")
    new_password: str = Field(..., min_length=6, description="新密码")
