"""RAG检索路由模块，提供向量检索API供外部服务调用"""

from typing import List, Optional

from fastapi import APIRouter, Depends
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user
from app.models.user import User
from app.utils.response import success_response

router = APIRouter(prefix="/retrieval", tags=["RAG检索"])


class RetrievalRequest(BaseModel):
    """检索请求"""
    query: str = Field(..., min_length=1, description="查询文本")
    kb_ids: List[int] = Field(..., description="知识库ID列表")
    top_k: int = Field(default=5, ge=1, le=20, description="返回结果数")


class SyncTextRequest(BaseModel):
    """文本同步请求"""
    title: str = Field(..., description="文档标题")
    content: str = Field(..., min_length=1, description="文档内容")
    knowledge_base_id: int = Field(..., description="知识库ID")
    doc_type: Optional[str] = Field(None, description="文档类型")
    ref_id: Optional[int] = Field(None, description="关联ID")


def get_rag_pipeline(db: Session = Depends(get_db)) -> RAGPipeline:
    """获取RAG流水线实例"""
    from app.rag.embedding.bge_m3 import BGEEmbedding
    from app.rag.retrieval.chunker import TextChunker
    from app.rag.retrieval.retriever import BaseRetriever
    from app.rag.vectorstore.faiss_store import FAISSVectorStore
    from app.core.config import settings

    embedding = BGEEmbedding()
    vector_store = FAISSVectorStore(settings.FAISS_INDEX_DIR)
    chunker = TextChunker(chunk_size=settings.CHUNK_SIZE, chunk_overlap=settings.CHUNK_OVERLAP)
    retriever = BaseRetriever(vector_store, embedding)

    return RAGPipeline(db, embedding, vector_store, chunker, retriever)


@router.post("", summary="RAG检索")
async def retrieval(
    request: RetrievalRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """根据查询文本在指定知识库中进行向量检索"""
    pipeline = get_rag_pipeline(db)
    results = await pipeline.query(
        question=request.query,
        kb_ids=request.kb_ids,
        top_k=request.top_k,
    )
    return success_response(data=results)


@router.post("/sync-text", summary="同步文本到向量库")
async def sync_text(
    request: SyncTextRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user),
) -> dict:
    """将文本内容同步到向量库（用于课程/宠物知识等结构化数据同步）"""
    import tempfile
    import os
    from app.models.knowledge import Document, KnowledgeBase

    # 检查知识库是否存在
    kb = db.query(KnowledgeBase).filter(KnowledgeBase.id == request.knowledge_base_id).first()
    if not kb:
        return success_response(data=None, message="知识库不存在")

    # 创建临时文本文件
    with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False, encoding='utf-8') as f:
        f.write(request.content)
        temp_path = f.name

    try:
        # 创建文档记录
        doc = Document(
            knowledge_base_id=request.knowledge_base_id,
            title=request.title,
            file_path=temp_path,
            file_type="txt",
            status="pending",
        )
        db.add(doc)
        db.commit()
        db.refresh(doc)

        # 处理文档
        pipeline = get_rag_pipeline(db)
        chunk_count = await pipeline.process_document(doc.id, request.knowledge_base_id)

        return success_response(
            data={"document_id": doc.id, "chunk_count": chunk_count},
            message="同步成功",
        )
    except Exception as e:
        # 清理临时文件
        if os.path.exists(temp_path):
            os.unlink(temp_path)
        return success_response(data=None, message=f"同步失败: {str(e)}")
