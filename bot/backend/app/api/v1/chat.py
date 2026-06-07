"""聊天路由模块，支持SSE流式响应"""

from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user, get_pagination_params, PaginationParams
from app.models.user import User
from app.schemas.chat import (
    ChatMessageResponse,
    ChatRequest,
    ChatSessionCreate,
    ChatSessionResponse,
)
from app.services.chat_service import ChatService
from app.utils.response import page_response, success_response

router = APIRouter(prefix="/chat", tags=["聊天"])


@router.get("/sessions", summary="获取会话列表")
def get_session_list(
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取当前用户的会话列表"""
    result = ChatService.get_session_list(
        db, user_id=current_user.id,
        page=pagination.page, page_size=pagination.page_size,
    )
    items = [ChatSessionResponse.model_validate(s).model_dump() for s in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.post("/sessions", summary="创建会话")
def create_session(
    session_data: ChatSessionCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """创建新的聊天会话"""
    session = ChatService.create_session(db, user_id=current_user.id, session_data=session_data)
    return success_response(
        data=ChatSessionResponse.model_validate(session).model_dump(),
        message="创建成功",
    )


@router.get("/sessions/{session_id}", summary="获取会话详情")
def get_session_detail(
    session_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取会话详情"""
    session = ChatService.get_session_by_id(db, session_id)
    return success_response(data=ChatSessionResponse.model_validate(session).model_dump())


@router.delete("/sessions/{session_id}", summary="删除会话")
def delete_session(
    session_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """删除聊天会话（软删除）"""
    ChatService.delete_session(db, session_id)
    return success_response(message="删除成功")


@router.get("/sessions/{session_id}/messages", summary="获取会话消息历史")
def get_message_history(
    session_id: int,
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取会话的消息历史记录"""
    result = ChatService.get_message_history(
        db, session_id,
        page=pagination.page, page_size=pagination.page_size,
    )
    items = [ChatMessageResponse.model_validate(msg).model_dump() for msg in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.post("/send", summary="发送消息")
async def send_message(
    chat_request: ChatRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """发送聊天消息（非流式）

    发送消息并获取完整的AI回复。
    """
    # 非流式模式：收集完整响应
    full_content = ""
    session_id = None

    async for chunk_str in ChatService.chat_stream(db, chat_request, user_id=current_user.id):
        if chunk_str.startswith("data: "):
            import json
            try:
                chunk_data = json.loads(chunk_str[6:].strip())
                if chunk_data.get("type") == "content":
                    full_content += chunk_data.get("content", "")
                elif chunk_data.get("type") == "session":
                    session_id = chunk_data.get("session_id")
            except json.JSONDecodeError:
                pass

    return success_response(data={
        "session_id": session_id,
        "content": full_content,
    })


@router.post("/stream", summary="流式聊天")
async def chat_stream(
    chat_request: ChatRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> StreamingResponse:
    """流式聊天接口（SSE）

    以Server-Sent Events格式流式返回AI回复。
    """
    return StreamingResponse(
        ChatService.chat_stream(db, chat_request, user_id=current_user.id),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )
