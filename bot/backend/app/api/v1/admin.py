"""后台管理路由模块"""

from typing import Optional

from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user
from app.models.user import User
from app.services.admin_service import AdminService
from app.utils.response import success_response

router = APIRouter(prefix="/admin", tags=["后台管理"])


@router.get("/dashboard", summary="仪表盘统计")
def get_dashboard(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取仪表盘统计数据"""
    stats = AdminService.get_dashboard_statistics(db)
    return success_response(data=stats)


@router.get("/dashboard/recent-activities", summary="最近活动")
def get_recent_activities(
    limit: int = Query(10, ge=1, le=50, description="返回数量"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取最近的活动记录"""
    activities = AdminService.get_recent_activities(db, limit=limit)
    return success_response(data=activities)


@router.get("/dashboard/chat-trend", summary="聊天趋势")
def get_chat_trend(
    days: int = Query(7, ge=1, le=30, description="统计天数"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取聊天趋势数据"""
    trend = AdminService.get_chat_trend(db, days=days)
    return success_response(data=trend)


@router.get("/dashboard/intent-distribution", summary="意图分布")
def get_intent_distribution(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取意图分布数据"""
    from sqlalchemy import func
    from app.models.chat import QALog

    results = (
        db.query(QALog.intent_type, func.count(QALog.id).label("count"))
        .filter(QALog.is_deleted == 0, QALog.intent_type.isnot(None))
        .group_by(QALog.intent_type)
        .all()
    )
    data = [{"name": r.intent_type, "value": r.count} for r in results]
    return success_response(data=data)


@router.get("/system-configs", summary="获取系统配置")
def get_system_configs(
    group: Optional[str] = Query(None, description="配置分组"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取系统配置列表"""
    configs = AdminService.get_system_configs(db, group=group)
    return success_response(
        data=[
            {
                "id": c.id,
                "config_key": c.config_key,
                "config_value": c.config_value,
                "description": c.description,
                "value_type": c.value_type,
            }
            for c in configs
        ]
    )


class UpdateConfigRequest(BaseModel):
    value: str


@router.put("/system-configs/{key}", summary="更新系统配置")
def update_system_config(
    key: str,
    request: UpdateConfigRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """更新系统配置项"""
    config = AdminService.update_system_config(db, key, request.value)
    return success_response(
        data={
            "id": config.id,
            "config_key": config.config_key,
            "config_value": config.config_value,
        },
        message="更新成功",
    )
