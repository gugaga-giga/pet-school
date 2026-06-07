"""认证相关的Pydantic Schema"""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, EmailStr, Field


class LoginRequest(BaseModel):
    """登录请求"""

    username: str = Field(..., min_length=2, max_length=50, description="用户名")
    password: str = Field(..., min_length=6, max_length=100, description="密码")


class LoginResponse(BaseModel):
    """登录响应"""

    access_token: str = Field(..., description="访问令牌")
    refresh_token: str = Field(..., description="刷新令牌")
    token_type: str = Field(default="bearer", description="令牌类型")
    expires_in: int = Field(..., description="过期时间(秒)")


class RegisterRequest(BaseModel):
    """注册请求"""

    username: str = Field(..., min_length=2, max_length=50, description="用户名")
    password: str = Field(..., min_length=6, max_length=100, description="密码")
    confirm_password: str = Field(..., min_length=6, max_length=100, description="确认密码")
    email: Optional[str] = Field(None, description="邮箱")
    phone: Optional[str] = Field(None, description="手机号")
    nickname: Optional[str] = Field(None, max_length=50, description="昵称")


class RegisterResponse(BaseModel):
    """注册响应"""

    id: int = Field(..., description="用户ID")
    username: str = Field(..., description="用户名")
    email: Optional[str] = Field(None, description="邮箱")
    created_at: datetime = Field(..., description="创建时间")


class TokenResponse(BaseModel):
    """令牌响应"""

    access_token: str = Field(..., description="访问令牌")
    refresh_token: str = Field(..., description="刷新令牌")
    token_type: str = Field(default="bearer", description="令牌类型")
    expires_in: int = Field(..., description="过期时间(秒)")


class RefreshTokenRequest(BaseModel):
    """刷新令牌请求"""

    refresh_token: str = Field(..., description="刷新令牌")
