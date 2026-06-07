"""通用Pydantic Schema"""

from typing import Optional

from pydantic import BaseModel, Field


class PageRequest(BaseModel):
    """分页请求"""

    page: int = Field(default=1, ge=1, description="页码")
    page_size: int = Field(default=10, ge=1, le=100, description="每页数量")


class PageResponse(BaseModel):
    """分页响应"""

    items: list = Field(default_factory=list, description="数据列表")
    total: int = Field(default=0, description="总记录数")
    page: int = Field(default=1, description="当前页码")
    page_size: int = Field(default=10, description="每页数量")


class IDResponse(BaseModel):
    """ID响应"""

    id: int = Field(..., description="记录ID")
