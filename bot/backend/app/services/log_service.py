"""日志服务模块，处理操作日志查询和统计"""

from typing import Optional

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.system import OperationLog
from app.utils.pagination import paginate


class LogService:
    """日志服务类"""

    @staticmethod
    def get_log_list(
        db: Session,
        page: int = 1,
        page_size: int = 10,
        user_id: Optional[int] = None,
        module: Optional[str] = None,
        action: Optional[str] = None,
        status: Optional[str] = None,
    ) -> dict:
        """获取操作日志列表（分页）

        Args:
            db: 数据库会话
            page: 页码
            page_size: 每页数量
            user_id: 用户ID筛选
            module: 模块筛选
            action: 操作类型筛选
            status: 状态筛选

        Returns:
            分页结果字典
        """
        query = db.query(OperationLog)

        if user_id:
            query = query.filter(OperationLog.user_id == user_id)

        if module:
            query = query.filter(OperationLog.module == module)

        if action:
            query = query.filter(OperationLog.action == action)

        if status:
            query = query.filter(OperationLog.status == status)

        query = query.order_by(OperationLog.created_at.desc())

        return paginate(query, page, page_size)

    @staticmethod
    def get_log_statistics(db: Session) -> dict:
        """获取日志统计信息

        Args:
            db: 数据库会话

        Returns:
            统计信息字典
        """
        total = db.query(OperationLog).count()
        success_count = db.query(OperationLog).filter(OperationLog.status == "success").count()
        failed_count = db.query(OperationLog).filter(OperationLog.status == "failed").count()

        # 按模块统计
        module_stats = (
            db.query(OperationLog.module, func.count(OperationLog.id))
            .group_by(OperationLog.module)
            .all()
        )

        return {
            "total": total,
            "success": success_count,
            "failed": failed_count,
            "by_module": {module: count for module, count in module_stats if module},
        }

    @staticmethod
    def create_log(
        db: Session,
        user_id: Optional[int] = None,
        username: Optional[str] = None,
        action: str = "",
        module: Optional[str] = None,
        target: Optional[str] = None,
        detail: Optional[str] = None,
        ip_address: Optional[str] = None,
        user_agent: Optional[str] = None,
        status: str = "success",
        error_message: Optional[str] = None,
    ) -> OperationLog:
        """创建操作日志

        Args:
            db: 数据库会话
            user_id: 操作用户ID
            username: 操作用户名
            action: 操作类型
            module: 操作模块
            target: 操作对象
            detail: 操作详情
            ip_address: IP地址
            user_agent: 用户代理
            status: 操作状态
            error_message: 错误信息

        Returns:
            创建的日志对象
        """
        log = OperationLog(
            user_id=user_id,
            username=username,
            action=action,
            module=module,
            target=target,
            detail=detail,
            ip_address=ip_address,
            user_agent=user_agent,
            status=status,
            error_message=error_message,
        )
        db.add(log)
        db.commit()
        db.refresh(log)
        return log
