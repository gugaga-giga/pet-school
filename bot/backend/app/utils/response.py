"""统一返回格式工具模块"""

from typing import Any

from pydantic import BaseModel


class ResponseModel(BaseModel):
    """统一响应模型"""

    code: int = 0
    message: str = "success"
    data: Any = None


class PageModel(BaseModel):
    """分页数据模型"""

    items: list = []
    total: int = 0
    page: int = 1
    page_size: int = 10


def success_response(data: Any = None, message: str = "success") -> dict:
    """构建成功响应

    Args:
        data: 响应数据
        message: 响应消息

    Returns:
        统一格式的成功响应字典
    """
    return {
        "code": 0,
        "message": message,
        "data": data,
    }


def error_response(code: int, message: str) -> dict:
    """构建错误响应

    Args:
        code: 错误码
        message: 错误消息

    Returns:
        统一格式的错误响应字典
    """
    return {
        "code": code,
        "message": message,
        "data": None,
    }


def page_response(
    items: list,
    total: int,
    page: int,
    page_size: int,
) -> dict:
    """构建分页响应

    Args:
        items: 当前页数据列表
        total: 总记录数
        page: 当前页码
        page_size: 每页数量

    Returns:
        统一格式的分页响应字典
    """
    return success_response(
        data={
            "items": items,
            "total": total,
            "page": page,
            "page_size": page_size,
        }
    )
