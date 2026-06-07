"""依赖注入模块，提供认证、权限、分页等通用依赖"""

from typing import Callable, List

from fastapi import Depends, Query
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.exceptions import ForbiddenException, UnauthorizedException
from app.core.security import verify_token
from app.models.user import User

# OAuth2 密码模式
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/auth/login")


class PaginationParams(BaseModel):
    """分页参数模型"""
    page: int = 1
    page_size: int = 10


def get_pagination_params(
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(10, ge=1, le=100, description="每页数量"),
) -> PaginationParams:
    """获取分页参数"""
    return PaginationParams(page=page, page_size=page_size)


async def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db),
) -> User:
    """从JWT令牌获取当前用户"""
    if not token:
        raise UnauthorizedException(message="未提供认证令牌")

    payload = verify_token(token)
    if payload.get("type") != "access":
        raise UnauthorizedException(message="令牌类型错误")

    user_id = payload.get("sub")
    if user_id is None:
        raise UnauthorizedException(message="令牌中缺少用户信息")

    user = db.query(User).filter(User.id == int(user_id)).first()
    if user is None:
        raise UnauthorizedException(message="用户不存在")

    return user


async def get_current_active_user(
    current_user: User = Depends(get_current_user),
) -> User:
    """获取当前活跃用户"""
    if not current_user.is_active:
        raise UnauthorizedException(message="用户已被禁用")
    return current_user


def require_permissions(*permissions: str) -> Callable:
    """权限检查依赖工厂函数"""

    async def check_permissions(
        current_user: User = Depends(get_current_active_user),
    ) -> User:
        """检查当前用户是否拥有所需权限"""
        # 超级管理员拥有所有权限
        admin_roles = [r for r in current_user.roles if r.code == "super_admin"]
        if admin_roles:
            return current_user

        # 获取用户所有权限
        user_permissions: set = set()
        for role in current_user.roles:
            for perm in role.permissions:
                user_permissions.add(perm.code)

        # 检查是否拥有所有需要的权限
        required = set(permissions)
        if not required.issubset(user_permissions):
            missing = required - user_permissions
            raise ForbiddenException(
                message="权限不足",
                detail=f"缺少权限: {', '.join(missing)}",
            )

        return current_user

    return check_permissions
