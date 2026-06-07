"""知识库路由模块"""

from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user, get_pagination_params, PaginationParams
from app.models.user import User
from app.schemas.knowledge import KnowledgeBaseCreate, KnowledgeBaseResponse, KnowledgeBaseUpdate
from app.services.knowledge_service import KnowledgeService
from app.utils.response import page_response, success_response

router = APIRouter(prefix="/knowledge-bases", tags=["知识库管理"])


@router.get("", summary="获取知识库列表")
def get_knowledge_base_list(
    keyword: Optional[str] = Query(None, description="搜索关键词"),
    is_active: Optional[bool] = Query(None, description="是否启用"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取知识库列表（分页）"""
    result = KnowledgeService.get_knowledge_base_list(
        db, page=pagination.page, page_size=pagination.page_size,
        keyword=keyword, is_active=is_active,
    )
    items = [KnowledgeBaseResponse.model_validate(kb).model_dump() for kb in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/statistics", summary="获取知识库统计")
def get_knowledge_statistics(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取知识库统计信息"""
    stats = KnowledgeService.get_statistics(db)
    return success_response(data=stats)


@router.get("/{kb_id}", summary="获取知识库详情")
def get_knowledge_base_detail(
    kb_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """根据ID获取知识库详情"""
    kb = KnowledgeService.get_knowledge_base_by_id(db, kb_id)
    return success_response(data=KnowledgeBaseResponse.model_validate(kb).model_dump())


@router.post("", summary="创建知识库")
def create_knowledge_base(
    kb_data: KnowledgeBaseCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """创建知识库"""
    kb = KnowledgeService.create_knowledge_base(db, kb_data, user_id=current_user.id)
    return success_response(
        data=KnowledgeBaseResponse.model_validate(kb).model_dump(),
        message="创建成功",
    )


@router.put("/{kb_id}", summary="更新知识库")
def update_knowledge_base(
    kb_id: int,
    kb_data: KnowledgeBaseUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """更新知识库信息"""
    kb = KnowledgeService.update_knowledge_base(db, kb_id, kb_data)
    return success_response(
        data=KnowledgeBaseResponse.model_validate(kb).model_dump(),
        message="更新成功",
    )


@router.delete("/{kb_id}", summary="删除知识库")
def delete_knowledge_base(
    kb_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """删除知识库"""
    KnowledgeService.delete_knowledge_base(db, kb_id)
    return success_response(message="删除成功")
