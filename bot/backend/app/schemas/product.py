"""产品相关的Pydantic Schema"""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class ProductCreate(BaseModel):
    """创建产品请求"""

    name: str = Field(..., min_length=1, max_length=200, description="产品名称")
    category: Optional[str] = Field(None, max_length=100, description="产品分类")
    brand: Optional[str] = Field(None, max_length=100, description="品牌")
    description: Optional[str] = Field(None, description="产品描述")
    specifications: Optional[str] = Field(None, description="规格参数(JSON格式)")
    price: Optional[str] = Field(None, description="价格")
    original_price: Optional[str] = Field(None, description="原价")
    image_url: Optional[str] = Field(None, description="主图URL")
    images: Optional[str] = Field(None, description="图片列表(JSON格式)")
    stock: Optional[int] = Field(None, ge=0, description="库存数量")
    is_available: bool = Field(default=True, description="是否上架")
    tags: Optional[str] = Field(None, description="标签(逗号分隔)")
    rating: Optional[str] = Field(None, description="评分")


class ProductUpdate(BaseModel):
    """更新产品请求"""

    name: Optional[str] = Field(None, min_length=1, max_length=200, description="产品名称")
    category: Optional[str] = Field(None, max_length=100, description="产品分类")
    brand: Optional[str] = Field(None, max_length=100, description="品牌")
    description: Optional[str] = Field(None, description="产品描述")
    specifications: Optional[str] = Field(None, description="规格参数(JSON格式)")
    price: Optional[str] = Field(None, description="价格")
    original_price: Optional[str] = Field(None, description="原价")
    image_url: Optional[str] = Field(None, description="主图URL")
    images: Optional[str] = Field(None, description="图片列表(JSON格式)")
    stock: Optional[int] = Field(None, ge=0, description="库存数量")
    is_available: Optional[bool] = Field(None, description="是否上架")
    tags: Optional[str] = Field(None, description="标签(逗号分隔)")
    rating: Optional[str] = Field(None, description="评分")


class ProductResponse(BaseModel):
    """产品响应"""

    id: int = Field(..., description="产品ID")
    name: str = Field(..., description="产品名称")
    category: Optional[str] = Field(None, description="产品分类")
    brand: Optional[str] = Field(None, description="品牌")
    description: Optional[str] = Field(None, description="产品描述")
    specifications: Optional[str] = Field(None, description="规格参数(JSON格式)")
    price: Optional[str] = Field(None, description="价格")
    original_price: Optional[str] = Field(None, description="原价")
    image_url: Optional[str] = Field(None, description="主图URL")
    images: Optional[str] = Field(None, description="图片列表(JSON格式)")
    stock: int = Field(default=0, description="库存数量")
    is_available: bool = Field(..., description="是否上架")
    tags: Optional[str] = Field(None, description="标签(逗号分隔)")
    sales_count: int = Field(default=0, description="销量")
    rating: Optional[str] = Field(None, description="评分")
    created_at: datetime = Field(..., description="创建时间")
    updated_at: datetime = Field(..., description="更新时间")

    model_config = {"from_attributes": True}


class RecommendRuleCreate(BaseModel):
    """创建推荐规则请求"""

    name: str = Field(..., min_length=1, max_length=100, description="规则名称")
    description: Optional[str] = Field(None, description="规则描述")
    rule_type: str = Field(..., description="规则类型: keyword/category/tag/custom")
    conditions: str = Field(..., description="触发条件(JSON格式)")
    product_ids: Optional[str] = Field(None, description="推荐产品ID列表(JSON格式)")
    priority: int = Field(default=0, description="优先级")
    is_active: bool = Field(default=True, description="是否启用")


class RecommendRuleUpdate(BaseModel):
    """更新推荐规则请求"""

    name: Optional[str] = Field(None, min_length=1, max_length=100, description="规则名称")
    description: Optional[str] = Field(None, description="规则描述")
    rule_type: Optional[str] = Field(None, description="规则类型")
    conditions: Optional[str] = Field(None, description="触发条件(JSON格式)")
    product_ids: Optional[str] = Field(None, description="推荐产品ID列表(JSON格式)")
    priority: Optional[int] = Field(None, description="优先级")
    is_active: Optional[bool] = Field(None, description="是否启用")


class RecommendRuleResponse(BaseModel):
    """推荐规则响应"""

    id: int = Field(..., description="规则ID")
    name: str = Field(..., description="规则名称")
    description: Optional[str] = Field(None, description="规则描述")
    rule_type: str = Field(..., description="规则类型")
    conditions: str = Field(..., description="触发条件(JSON格式)")
    product_ids: Optional[str] = Field(None, description="推荐产品ID列表(JSON格式)")
    priority: int = Field(default=0, description="优先级")
    is_active: bool = Field(..., description="是否启用")
    created_by: Optional[int] = Field(None, description="创建者ID")
    created_at: datetime = Field(..., description="创建时间")
    updated_at: datetime = Field(..., description="更新时间")

    model_config = {"from_attributes": True}
