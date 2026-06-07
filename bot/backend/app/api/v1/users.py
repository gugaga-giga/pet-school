"""用户路由模块"""

from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user, get_pagination_params
from app.core.dependencies import PaginationParams
from app.models.user import User
from app.schemas.user import UserCreate, UserResponse, UserUpdate
from app.services.user_service import UserService
from app.utils.response import page_response, success_response

router = APIRouter(prefix="/users", tags=["用户管理"])


@router.get("", summary="获取用户列表")
def get_user_list(
    keyword: Optional[str] = Query(None, description="搜索关键词"),
    is_active: Optional[bool] = Query(None, description="是否激活"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取用户列表（分页）

    需要登录权限，支持关键词搜索和状态筛选。
    """
    result = UserService.get_user_list(
        db, page=pagination.page, page_size=pagination.page_size,
        keyword=keyword, is_active=is_active,
    )
    items = [UserResponse.model_validate(user).model_dump() for user in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/{user_id}", summary="获取用户详情")
def get_user_detail(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """根据ID获取用户详情"""
    user = UserService.get_user_by_id(db, user_id)
    return success_response(data=UserResponse.model_validate(user).model_dump())


@router.post("", summary="创建用户")
def create_user(
    user_data: UserCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """创建新用户"""
    user = UserService.create_user(db, user_data)
    return success_response(
        data=UserResponse.model_validate(user).model_dump(),
        message="创建成功",
    )


@router.put("/{user_id}", summary="更新用户")
def update_user(
    user_id: int,
    user_data: UserUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """更新用户信息"""
    user = UserService.update_user(db, user_id, user_data)
    return success_response(
        data=UserResponse.model_validate(user).model_dump(),
        message="更新成功",
    )


@router.delete("/{user_id}", summary="删除用户")
def delete_user(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """删除用户"""
    UserService.delete_user(db, user_id)
    return success_response(message="删除成功")
