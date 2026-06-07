"""宠物路由模块"""

from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user, get_pagination_params, PaginationParams
from app.models.user import User
from app.schemas.pet import PetCreate, PetResponse, PetUpdate
from app.services.pet_service import PetService
from app.utils.response import page_response, success_response

router = APIRouter(prefix="/pets", tags=["宠物管理"])


@router.get("", summary="获取宠物列表")
def get_pet_list(
    pet_type: Optional[str] = Query(None, description="宠物类型筛选"),
    keyword: Optional[str] = Query(None, description="搜索关键词"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取宠物列表（分页）"""
    result = PetService.get_pet_list(
        db, page=pagination.page, page_size=pagination.page_size,
        pet_type=pet_type, user_id=current_user.id, keyword=keyword,
    )
    items = [PetResponse.model_validate(pet).model_dump() for pet in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/statistics", summary="获取宠物统计")
def get_pet_statistics(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取宠物统计信息"""
    stats = PetService.get_statistics(db)
    return success_response(data=stats)


@router.get("/{pet_id}", summary="获取宠物详情")
def get_pet_detail(
    pet_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """根据ID获取宠物详情"""
    pet = PetService.get_pet_by_id(db, pet_id)
    return success_response(data=PetResponse.model_validate(pet).model_dump())


@router.post("", summary="创建宠物")
def create_pet(
    pet_data: PetCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """创建宠物信息"""
    pet = PetService.create_pet(db, pet_data, user_id=current_user.id)
    return success_response(
        data=PetResponse.model_validate(pet).model_dump(),
        message="创建成功",
    )


@router.put("/{pet_id}", summary="更新宠物")
def update_pet(
    pet_id: int,
    pet_data: PetUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """更新宠物信息"""
    pet = PetService.update_pet(db, pet_id, pet_data)
    return success_response(
        data=PetResponse.model_validate(pet).model_dump(),
        message="更新成功",
    )


@router.delete("/{pet_id}", summary="删除宠物")
def delete_pet(
    pet_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """删除宠物"""
    PetService.delete_pet(db, pet_id)
    return success_response(message="删除成功")
