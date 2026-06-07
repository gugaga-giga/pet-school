"""聊天相关的Pydantic Schema"""

from datetime import datetime
from typing import Optional, Any

from pydantic import BaseModel, Field


class ChatSessionCreate(BaseModel):
    """创建聊天会话请求"""
    title: Optional[str] = Field(None, max_length=200, description="会话标题")
    knowledge_base_id: Optional[int] = Field(None, description="关联知识库ID")


class ChatSessionResponse(BaseModel):
    """聊天会话响应"""
    id: int = Field(..., description="会话ID")
    user_id: Optional[int] = Field(None, description="用户ID")
    title: Optional[str] = Field(None, description="会话标题")
    knowledge_base_id: Optional[int] = Field(None, description="知识库ID")
    created_at: Optional[datetime] = Field(None, description="创建时间")
    updated_at: Optional[datetime] = Field(None, description="更新时间")

    model_config = {"from_attributes": True}


class ChatMessageCreate(BaseModel):
    """创建聊天消息请求"""
    content: str = Field(..., min_length=1, description="消息内容")


class ChatMessageResponse(BaseModel):
    """聊天消息响应"""
    id: int = Field(..., description="消息ID")
    session_id: int = Field(..., description="会话ID")
    role: str = Field(..., description="角色: user/assistant/system")
    content: str = Field(..., description="消息内容")
    token_count: int = Field(default=0, description="Token数量")
    sources: Optional[Any] = Field(None, description="引用来源")
    intent_type: Optional[str] = Field(None, description="意图类型")
    recommended_products: Optional[Any] = Field(None, description="推荐产品")
    created_at: Optional[datetime] = Field(None, description="创建时间")

    model_config = {"from_attributes": True}


class ChatRequest(BaseModel):
    """聊天请求"""
    session_id: Optional[int] = Field(None, description="会话ID，为空则创建新会话")
    message: str = Field(..., min_length=1, description="用户消息")
    knowledge_base_id: Optional[int] = Field(None, description="使用的知识库ID")
    stream: bool = Field(default=True, description="是否流式响应")


class ChatStreamChunk(BaseModel):
    """流式响应数据块"""
    type: str = Field(..., description="类型: content/done/error")
    content: Optional[str] = Field(None, description="内容")
    session_id: Optional[int] = Field(None, description="会话ID")
    sources: Optional[list] = Field(None, description="引用来源")
