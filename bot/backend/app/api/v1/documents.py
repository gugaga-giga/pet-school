"""文档路由模块"""

from typing import Optional

from fastapi import APIRouter, Depends, Query, UploadFile, File, Form
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user, get_pagination_params, PaginationParams
from app.models.user import User
from app.schemas.knowledge import DocumentChunkResponse, DocumentResponse
from app.services.document_service import DocumentService
from app.utils.response import page_response, success_response

router = APIRouter(prefix="/documents", tags=["文档管理"])


@router.get("", summary="获取文档列表")
def get_document_list(
    knowledge_base_id: Optional[int] = Query(None, description="知识库ID"),
    status: Optional[str] = Query(None, description="处理状态"),
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取文档列表（分页）"""
    result = DocumentService.get_document_list(
        db, knowledge_base_id=knowledge_base_id,
        page=pagination.page, page_size=pagination.page_size,
        status=status,
    )
    items = [DocumentResponse.model_validate(doc).model_dump() for doc in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.get("/{doc_id}", summary="获取文档详情")
def get_document_detail(
    doc_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """根据ID获取文档详情"""
    doc = DocumentService.get_document_by_id(db, doc_id)
    return success_response(data=DocumentResponse.model_validate(doc).model_dump())


@router.post("/upload", summary="上传文档")
async def upload_document(
    knowledge_base_id: int = Form(..., description="知识库ID"),
    file: UploadFile = File(..., description="上传的文件"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """上传文档到知识库

    支持的文件类型: pdf, docx, xlsx, txt, md, csv
    """
    doc = await DocumentService.upload_document(
        db, knowledge_base_id, file, user_id=current_user.id
    )
    return success_response(
        data=DocumentResponse.model_validate(doc).model_dump(),
        message="上传成功",
    )


@router.get("/{doc_id}/chunks", summary="获取文档分块列表")
def get_document_chunks(
    doc_id: int,
    pagination: PaginationParams = Depends(get_pagination_params),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """获取文档的分块列表"""
    result = DocumentService.get_document_chunks(
        db, doc_id, page=pagination.page, page_size=pagination.page_size,
    )
    items = [DocumentChunkResponse.model_validate(chunk).model_dump() for chunk in result["items"]]
    return page_response(items, result["total"], result["page"], result["page_size"])


@router.delete("/{doc_id}", summary="删除文档")
def delete_document(
    doc_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """删除文档"""
    DocumentService.delete_document(db, doc_id)
    return success_response(message="删除成功")


@router.post("/sync-text", summary="同步文本到知识库")
async def sync_text(
    body: dict,
    db: Session = Depends(get_db),
) -> dict:
    """同步文本内容到知识库（供外部系统调用，如课程变更时自动同步）

    请求体:
    - title: 文档标题
    - content: 文本内容
    - knowledge_base_id: 知识库ID
    - doc_type: 文档类型（如 course/pet/faq）
    - ref_id: 外部引用ID（如课程ID）

    注意：向量索引更新为异步后台任务，不在此接口中同步执行。
    """
    try:
        from app.models.knowledge import Document, DocumentChunk

        title = body.get("title", "未命名文档")
        content = body.get("content", "")
        kb_id = body.get("knowledge_base_id", 1)
        doc_type = body.get("doc_type", "sync")
        ref_id = body.get("ref_id")

        if not content:
            return success_response(data=None, message="内容为空，跳过同步")

        # 检查是否已存在同名文档（按标题+doc_type去重）
        existing = db.query(Document).filter(
            Document.title == title,
            Document.knowledge_base_id == kb_id,
            Document.is_deleted == 0,
        ).first()

        if existing:
            # 更新已有文档
            existing.file_size = len(content.encode("utf-8"))
            db.commit()
            doc = existing
        else:
            # 创建新文档
            doc = Document(
                knowledge_base_id=kb_id,
                title=title,
                file_name=f"{title}.txt",
                file_path="",
                file_type="txt",
                file_size=len(content.encode("utf-8")),
                status="completed",
            )
            db.add(doc)
            db.commit()
            db.refresh(doc)

        # 分块并存储
        chunk_size = 500
        overlap = 50
        chunks = []
        for i in range(0, len(content), chunk_size - overlap):
            chunk_text = content[i:i + chunk_size]
            if chunk_text.strip():
                chunks.append(chunk_text)

        # 删除旧chunks
        db.query(DocumentChunk).filter(DocumentChunk.document_id == doc.id).delete()
        db.commit()

        # 创建新chunks
        for idx, chunk_text in enumerate(chunks):
            chunk = DocumentChunk(
                document_id=doc.id,
                knowledge_base_id=kb_id,
                chunk_index=idx,
                content=chunk_text,
                token_count=len(chunk_text) * 2,
                chunk_metadata={"doc_type": doc_type, "ref_id": ref_id} if ref_id else {"doc_type": doc_type},
            )
            db.add(chunk)

        doc.chunk_count = len(chunks)
        doc.status = "completed"
        db.commit()

        # 向量索引更新标记为pending，由后台任务或检索时按需构建
        # 避免在请求中同步加载torch/faiss导致Windows内存访问违规
        try:
            import os
            flag_dir = os.path.join(os.path.dirname(__file__), "..", "..", "..", "faiss_indexes")
            os.makedirs(flag_dir, exist_ok=True)
            flag_file = os.path.join(flag_dir, ".rebuild_pending")
            with open(flag_file, "w") as f:
                f.write(f"{kb_id}")
        except Exception:
            pass

        return success_response(
            data={"doc_id": doc.id, "chunk_count": len(chunks)},
            message="同步成功",
        )
    except Exception as e:
        return success_response(data=None, message=f"同步失败: {str(e)}")
