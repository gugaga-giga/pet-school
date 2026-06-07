"""日志路由模块"""

from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user, get_pagination_params, PaginationParams
from app.models.chat import QALog
from app.models.system import OperationLog
from app.models.user import User
from app.utils.pagination import paginate
from app.utils.response import page_response, success_response

router = APIRouter(prefix="/logs", tags=["日志管理"])


@router.get("/qa", summary="获取问答日志列表")
def get_qa_log_list(
    intent_type: Optional[str] = Query(None, description="意图类型筛选"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取问答日志列表（分页）"""
    query = db.query(QALog).filter(QALog.is_deleted == 0)

    if intent_type:
        query = query.filter(QALog.intent_type == intent_type)

    query = query.order_by(QALog.created_at.desc())
    result = paginate(query, pagination.page, pagination.page_size)

    items = [
        {
            "id": log.id,
            "user_id": log.user_id,
            "session_id": log.session_id,
            "question": log.question,
            "answer": log.answer[:200] if log.answer else "",
            "intent_type": log.intent_type,
            "retrieval_time": log.retrieval_time,
            "llm_time": log.llm_time,
            "total_time": log.total_time,
            "created_at": str(log.created_at) if log.created_at else None,
        }
        for log in result["items"]
    ]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/operation", summary="获取操作日志列表")
def get_operation_log_list(
    action: Optional[str] = Query(None, description="操作类型筛选"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取操作日志列表（分页）"""
    query = db.query(OperationLog).filter(OperationLog.is_deleted == 0)

    if action:
        query = query.filter(OperationLog.action == action)

    query = query.order_by(OperationLog.created_at.desc())
    result = paginate(query, pagination.page, pagination.page_size)

    items = [
        {
            "id": log.id,
            "user_id": log.user_id,
            "action": log.action,
            "resource": log.resource,
            "detail": log.detail,
            "ip_address": log.ip_address,
            "created_at": str(log.created_at) if log.created_at else None,
        }
        for log in result["items"]
    ]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/statistics", summary="获取日志统计")
def get_log_statistics(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取日志统计信息"""
    total_qa = db.query(QALog).filter(QALog.is_deleted == 0).count()
    total_operations = db.query(OperationLog).filter(OperationLog.is_deleted == 0).count()

    avg_total_time = db.query(func.avg(QALog.total_time)).filter(
        QALog.is_deleted == 0
    ).scalar() or 0

    return success_response(data={
        "total_qa_logs": total_qa,
        "total_operation_logs": total_operations,
        "avg_response_time": int(avg_total_time),
    })
