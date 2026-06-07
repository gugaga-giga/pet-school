"""宠物相关的Pydantic Schema - 匹配Pet ORM模型"""

from datetime import datetime
from decimal import Decimal
from typing import Optional

from pydantic import BaseModel, Field


class PetCreate(BaseModel):
    """创建宠物请求"""

    name: str = Field(..., min_length=1, max_length=64, description="宠物昵称")
    type: str = Field(..., description="宠物类型: cat/dog/bird/rabbit/fish/hamster/other")
    breed: Optional[str] = Field(None, max_length=64, description="品种")
    birth_date: Optional[str] = Field(None, description="出生日期")
    age: Optional[int] = Field(None, ge=0, description="年龄")
    weight: Optional[Decimal] = Field(None, description="体重(kg)")
    gender: Optional[int] = Field(None, description="性别: 1公 2母 0未知")
    is_neutered: Optional[int] = Field(None, description="是否绝育: 0否 1是")
    health_status: Optional[str] = Field(None, description="健康状况: healthy/sick/recovering")
    vaccine_status: Optional[str] = Field(None, description="疫苗状态: unknown/partial/complete")


class PetUpdate(BaseModel):
    """更新宠物请求"""

    name: Optional[str] = Field(None, min_length=1, max_length=64, description="宠物昵称")
    type: Optional[str] = Field(None, description="宠物类型")
    breed: Optional[str] = Field(None, max_length=64, description="品种")
    birth_date: Optional[str] = Field(None, description="出生日期")
    age: Optional[int] = Field(None, ge=0, description="年龄")
    weight: Optional[Decimal] = Field(None, description="体重(kg)")
    gender: Optional[int] = Field(None, description="性别: 1公 2母 0未知")
    is_neutered: Optional[int] = Field(None, description="是否绝育: 0否 1是")
    health_status: Optional[str] = Field(None, description="健康状况")
    vaccine_status: Optional[str] = Field(None, description="疫苗状态")


class PetResponse(BaseModel):
    """宠物响应"""

    id: int = Field(..., description="宠物ID")
    user_id: int = Field(..., description="用户ID")
    name: str = Field(..., description="宠物昵称")
    type: str = Field(..., description="宠物类型")
    breed: Optional[str] = Field(None, description="品种")
    birth_date: Optional[str] = Field(None, description="出生日期")
    age: Optional[int] = Field(None, description="年龄")
    weight: Optional[Decimal] = Field(None, description="体重(kg)")
    gender: Optional[int] = Field(None, description="性别: 1公 2母 0未知")
    is_neutered: Optional[int] = Field(None, description="是否绝育: 0否 1是")
    health_status: Optional[str] = Field(None, description="健康状况")
    vaccine_status: Optional[str] = Field(None, description="疫苗状态")
    is_deleted: Optional[int] = Field(None, description="是否删除")
    created_at: Optional[datetime] = Field(None, description="创建时间")
    updated_at: Optional[datetime] = Field(None, description="更新时间")

    model_config = {"from_attributes": True}
