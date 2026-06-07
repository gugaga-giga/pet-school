"""知识库相关的Pydantic Schema - 匹配ORM模型"""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


# ==================== 知识库 ====================

class KnowledgeBaseCreate(BaseModel):
    """创建知识库请求"""

    name: str = Field(..., min_length=1, max_length=128, description="知识库名称")
    description: Optional[str] = Field(None, description="知识库描述")
    icon: Optional[str] = Field(None, description="图标")
    is_public: Optional[int] = Field(default=0, description="是否公开: 0否 1是")


class KnowledgeBaseUpdate(BaseModel):
    """更新知识库请求"""

    name: Optional[str] = Field(None, min_length=1, max_length=128, description="知识库名称")
    description: Optional[str] = Field(None, description="知识库描述")
    icon: Optional[str] = Field(None, description="图标")
    is_public: Optional[int] = Field(None, description="是否公开: 0否 1是")
    status: Optional[int] = Field(None, description="状态: 1正常 0禁用")


class KnowledgeBaseResponse(BaseModel):
    """知识库响应"""

    id: int = Field(..., description="知识库ID")
    name: str = Field(..., description="知识库名称")
    description: Optional[str] = Field(None, description="知识库描述")
    icon: Optional[str] = Field(None, description="图标")
    document_count: int = Field(default=0, description="文档数量")
    chunk_count: int = Field(default=0, description="分块数量")
    owner_id: Optional[int] = Field(None, description="创建者ID")
    is_public: Optional[int] = Field(None, description="是否公开")
    status: Optional[int] = Field(None, description="状态: 1正常 0禁用")
    is_deleted: Optional[int] = Field(None, description="是否删除")
    created_at: Optional[datetime] = Field(None, description="创建时间")
    updated_at: Optional[datetime] = Field(None, description="更新时间")

    model_config = {"from_attributes": True}


# ==================== 文档 ====================

class DocumentUpload(BaseModel):
    """文档上传请求"""

    knowledge_base_id: int = Field(..., description="知识库ID")


class DocumentResponse(BaseModel):
    """文档响应"""

    id: int = Field(..., description="文档ID")
    knowledge_base_id: int = Field(..., description="知识库ID")
    title: str = Field(..., description="文档标题")
    file_name: str = Field(..., description="原始文件名")
    file_type: str = Field(..., description="文件类型")
    file_size: int = Field(default=0, description="文件大小(字节)")
    chunk_count: int = Field(default=0, description="分块数量")
    status: str = Field(..., description="处理状态")
    is_deleted: Optional[int] = Field(None, description="是否删除")
    created_at: Optional[datetime] = Field(None, description="创建时间")
    updated_at: Optional[datetime] = Field(None, description="更新时间")

    model_config = {"from_attributes": True}


class DocumentChunkResponse(BaseModel):
    """文档分块响应"""

    id: int = Field(..., description="分块ID")
    document_id: int = Field(..., description="文档ID")
    knowledge_base_id: Optional[int] = Field(None, description="知识库ID")
    chunk_index: int = Field(..., description="分块索引")
    content: str = Field(..., description="分块内容")
    token_count: int = Field(default=0, description="Token数量")
    faiss_id: Optional[int] = Field(None, description="FAISS向量索引位置")
    is_deleted: Optional[int] = Field(None, description="是否删除")
    created_at: Optional[datetime] = Field(None, description="创建时间")
    updated_at: Optional[datetime] = Field(None, description="更新时间")

    model_config = {"from_attributes": True}
