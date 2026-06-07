"""分页工具模块"""

from typing import Any

from sqlalchemy.orm import Query


def paginate(query: Query, page: int, page_size: int) -> dict:
    """对SQLAlchemy查询进行分页

    Args:
        query: SQLAlchemy查询对象
        page: 页码，从1开始
        page_size: 每页数量

    Returns:
        包含分页信息的字典，结构为:
        {
            "items": [...],  # 当前页数据列表
            "total": int,    # 总记录数
            "page": int,     # 当前页码
            "page_size": int # 每页数量
        }
    """
    # 计算总记录数
    total = query.count()

    # 计算偏移量
    offset = (page - 1) * page_size

    # 获取当前页数据
    items = query.offset(offset).limit(page_size).all()

    return {
        "items": items,
        "total": total,
        "page": page,
        "page_size": page_size,
    }
