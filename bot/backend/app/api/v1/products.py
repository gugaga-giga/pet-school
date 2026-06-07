"""产品路由模块"""

from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user, get_pagination_params, PaginationParams
from app.models.user import User
from app.schemas.product import (
    ProductCreate,
    ProductResponse,
    ProductUpdate,
    RecommendRuleCreate,
    RecommendRuleResponse,
    RecommendRuleUpdate,
)
from app.services.product_service import ProductService
from app.utils.response import page_response, success_response

router = APIRouter(prefix="/products", tags=["产品管理"])


# ==================== 产品 ====================

@router.get("", summary="获取产品列表")
def get_product_list(
    category: Optional[str] = Query(None, description="分类筛选"),
    is_available: Optional[bool] = Query(None, description="是否上架"),
    keyword: Optional[str] = Query(None, description="搜索关键词"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取产品列表（分页）"""
    result = ProductService.get_product_list(
        db, page=pagination.page, page_size=pagination.page_size,
        category=category, is_available=is_available, keyword=keyword,
    )
    items = [ProductResponse.model_validate(p).model_dump() for p in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/{product_id}", summary="获取产品详情")
def get_product_detail(
    product_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """根据ID获取产品详情"""
    product = ProductService.get_product_by_id(db, product_id)
    return success_response(data=ProductResponse.model_validate(product).model_dump())


@router.post("", summary="创建产品")
def create_product(
    product_data: ProductCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """创建产品"""
    product = ProductService.create_product(db, product_data)
    return success_response(
        data=ProductResponse.model_validate(product).model_dump(),
        message="创建成功",
    )


@router.put("/{product_id}", summary="更新产品")
def update_product(
    product_id: int,
    product_data: ProductUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """更新产品信息"""
    product = ProductService.update_product(db, product_id, product_data)
    return success_response(
        data=ProductResponse.model_validate(product).model_dump(),
        message="更新成功",
    )


@router.delete("/{product_id}", summary="删除产品")
def delete_product(
    product_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """删除产品"""
    ProductService.delete_product(db, product_id)
    return success_response(message="删除成功")


# ==================== 推荐规则 ====================

@router.get("/rules/list", summary="获取推荐规则列表")
def get_recommend_rule_list(
    rule_type: Optional[str] = Query(None, description="规则类型"),
    is_active: Optional[bool] = Query(None, description="是否启用"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取推荐规则列表（分页）"""
    result = ProductService.get_recommend_rule_list(
        db, page=pagination.page, page_size=pagination.page_size,
        rule_type=rule_type, is_active=is_active,
    )
    items = [RecommendRuleResponse.model_validate(r).model_dump() for r in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/rules/{rule_id}", summary="获取推荐规则详情")
def get_recommend_rule_detail(
    rule_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """根据ID获取推荐规则详情"""
    rule = ProductService.get_recommend_rule_by_id(db, rule_id)
    return success_response(data=RecommendRuleResponse.model_validate(rule).model_dump())


@router.post("/rules", summary="创建推荐规则")
def create_recommend_rule(
    rule_data: RecommendRuleCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """创建推荐规则"""
    rule = ProductService.create_recommend_rule(db, rule_data, user_id=current_user.id)
    return success_response(
        data=RecommendRuleResponse.model_validate(rule).model_dump(),
        message="创建成功",
    )


@router.put("/rules/{rule_id}", summary="更新推荐规则")
def update_recommend_rule(
    rule_id: int,
    rule_data: RecommendRuleUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """更新推荐规则"""
    rule = ProductService.update_recommend_rule(db, rule_id, rule_data)
    return success_response(
        data=RecommendRuleResponse.model_validate(rule).model_dump(),
        message="更新成功",
    )


@router.delete("/rules/{rule_id}", summary="删除推荐规则")
def delete_recommend_rule(
    rule_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """删除推荐规则"""
    ProductService.delete_recommend_rule(db, rule_id)
    return success_response(message="删除成功")
