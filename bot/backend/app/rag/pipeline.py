"""RAG完整流水线，串联解析→切分→向量化→存储→检索"""

from typing import Any, Dict, List

import numpy as np
from loguru import logger
from sqlalchemy.orm import Session

from app.rag.embedding.bge_m3 import BGEEmbedding
from app.rag.parsers.parser_factory import ParserFactory
from app.rag.retrieval.chunker import TextChunker
from app.rag.retrieval.retriever import BaseRetriever
from app.rag.vectorstore.faiss_store import FAISSVectorStore


class RAGPipeline:
    """RAG处理流水线，提供文档处理、删除、重建和查询功能"""

    def __init__(
        self,
        db_session: Session,
        embedding_service: BGEEmbedding,
        vector_store: FAISSVectorStore,
        chunker: TextChunker,
        retriever: BaseRetriever,
    ) -> None:
        """初始化RAG流水线

        Args:
            db_session: 数据库会话
            embedding_service: Embedding服务实例
            vector_store: FAISS向量存储实例
            chunker: 文本切分器实例
            retriever: 检索器实例
        """
        self.db = db_session
        self.embedding_service = embedding_service
        self.vector_store = vector_store
        self.chunker = chunker
        self.retriever = retriever

    async def process_document(
        self, document_id: int, knowledge_base_id: int
    ) -> int:
        """处理文档：解析→切分→向量化→存储

        Args:
            document_id: 文档ID
            knowledge_base_id: 知识库ID

        Returns:
            生成的chunk数量

        Raises:
            Exception: 处理过程中的任何错误
        """
        from app.models.knowledge import Document, DocumentChunk, KnowledgeBase

        kb_id_str = str(knowledge_base_id)

        # 获取文档记录
        doc = self.db.query(Document).filter(Document.id == document_id).first()
        if not doc:
            raise ValueError(f"文档不存在: {document_id}")

        try:
            # 更新文档状态为处理中
            doc.status = "processing"
            self.db.commit()

            # 1. 根据文件类型获取解析器
            parser = ParserFactory.get_parser(doc.file_type)

            # 2. 解析文档
            logger.info(f"开始解析文档: {doc.title} (类型: {doc.file_type})")
            parsed_docs = parser.parse(doc.file_path)

            if not parsed_docs:
                doc.status = "completed"
                doc.chunk_count = 0
                self.db.commit()
                logger.info(f"文档 {doc.title} 解析结果为空，跳过向量化")
                return 0

            # 3. 切分文本
            logger.info(f"开始切分文档: {doc.title}，解析块数: {len(parsed_docs)}")
            chunks = self.chunker.chunk_documents(parsed_docs)

            if not chunks:
                doc.status = "completed"
                doc.chunk_count = 0
                self.db.commit()
                logger.info(f"文档 {doc.title} 切分结果为空，跳过向量化")
                return 0

            # 4. 批量向量化
            texts = [chunk["content"] for chunk in chunks]
            logger.info(f"开始向量化，共 {len(texts)} 个chunk")
            vectors = self.embedding_service.embed_documents_with_progress(
                texts, batch_size=32
            )
            vectors_np = np.array(vectors, dtype=np.float32)

            # 5. 写入数据库(document_chunks表)
            chunk_ids: List[int] = []
            for chunk_idx, chunk in enumerate(chunks):
                db_chunk = DocumentChunk(
                    document_id=document_id,
                    chunk_index=chunk_idx,
                    content=chunk["content"],
                    token_count=len(chunk["content"]),
                )
                self.db.add(db_chunk)
                self.db.flush()  # 获取自增ID
                chunk_ids.append(db_chunk.id)

            # 6. 存入FAISS
            # 确保索引存在
            if kb_id_str not in self.vector_store.indexes:
                self.vector_store.load_index(kb_id_str)
            if kb_id_str not in self.vector_store.indexes:
                self.vector_store.create_index(kb_id_str)

            self.vector_store.add_vectors(kb_id_str, vectors_np, chunk_ids)
            self.vector_store.save_index(kb_id_str)

            # 更新chunk的faiss_index字段
            for chunk_id in chunk_ids:
                db_chunk = (
                    self.db.query(DocumentChunk)
                    .filter(DocumentChunk.id == chunk_id)
                    .first()
                )
                if db_chunk:
                    reverse_map = self.vector_store.reverse_id_maps.get(kb_id_str, {})
                    db_chunk.faiss_index = reverse_map.get(chunk_id)

            # 7. 更新文档状态为completed
            doc.status = "completed"
            doc.chunk_count = len(chunks)

            # 更新知识库chunk计数
            kb = (
                self.db.query(KnowledgeBase)
                .filter(KnowledgeBase.id == knowledge_base_id)
                .first()
            )
            if kb:
                kb.chunk_count = (kb.chunk_count or 0) + len(chunks)

            self.db.commit()

            logger.info(
                f"文档处理完成: {doc.title}，生成 {len(chunks)} 个chunk"
            )
            return len(chunks)

        except Exception as e:
            # 更新文档状态为失败
            doc.status = "failed"
            doc.error_message = str(e)[:500]  # 限制错误信息长度
            self.db.commit()
            logger.error(f"文档处理失败: {doc.title}, 错误: {e}")
            raise

    async def delete_document(
        self, document_id: int, knowledge_base_id: int
    ) -> None:
        """删除文档的向量数据

        Args:
            document_id: 文档ID
            knowledge_base_id: 知识库ID
        """
        from app.models.knowledge import Document, DocumentChunk, KnowledgeBase

        kb_id_str = str(knowledge_base_id)

        # 获取该文档的所有chunk
        chunks = (
            self.db.query(DocumentChunk)
            .filter(DocumentChunk.document_id == document_id)
            .all()
        )

        if not chunks:
            logger.info(f"文档 {document_id} 没有chunk数据，无需删除向量")
            return

        chunk_ids = [chunk.id for chunk in chunks]

        # 从FAISS中删除向量
        try:
            if kb_id_str in self.vector_store.indexes or self.vector_store.load_index(
                kb_id_str
            ):
                self.vector_store.delete_vectors(kb_id_str, chunk_ids)
                self.vector_store.save_index(kb_id_str)
        except Exception as e:
            logger.error(f"FAISS向量删除失败: {e}")

        # 更新知识库chunk计数
        kb = (
            self.db.query(KnowledgeBase)
            .filter(KnowledgeBase.id == knowledge_base_id)
            .first()
        )
        if kb:
            kb.chunk_count = max(0, (kb.chunk_count or 0) - len(chunks))

        # 数据库中的chunk会通过外键CASCADE自动删除
        self.db.commit()

        logger.info(
            f"文档 {document_id} 的 {len(chunks)} 个chunk向量已删除"
        )

    async def rebuild_knowledge_base(self, knowledge_base_id: int) -> None:
        """重建知识库索引

        从数据库中读取所有chunk，重新生成向量并构建索引

        Args:
            knowledge_base_id: 知识库ID
        """
        from app.models.knowledge import Document, DocumentChunk, KnowledgeBase

        kb_id_str = str(knowledge_base_id)

        # 获取该知识库下所有已完成文档的chunk
        completed_docs = (
            self.db.query(Document)
            .filter(
                Document.knowledge_base_id == knowledge_base_id,
                Document.status == "completed",
            )
            .all()
        )

        doc_ids = [doc.id for doc in completed_docs]
        all_chunks = (
            self.db.query(DocumentChunk)
            .filter(DocumentChunk.document_id.in_(doc_ids))
            .order_by(DocumentChunk.document_id, DocumentChunk.chunk_index)
            .all()
        )

        if not all_chunks:
            # 没有chunk，创建空索引
            self.vector_store.create_index(kb_id_str)
            self.vector_store.save_index(kb_id_str)
            logger.info(f"知识库 {knowledge_base_id} 重建完成（空索引）")
            return

        # 重新向量化所有chunk
        texts = [chunk.content for chunk in all_chunks]
        chunk_ids = [chunk.id for chunk in all_chunks]

        logger.info(f"重建知识库 {knowledge_base_id}，共 {len(texts)} 个chunk")
        vectors = self.embedding_service.embed_documents_with_progress(
            texts, batch_size=32
        )
        vectors_np = np.array(vectors, dtype=np.float32)

        # 重建索引
        self.vector_store.rebuild_index(kb_id_str, vectors_np, chunk_ids)
        self.vector_store.save_index(kb_id_str)

        # 更新chunk的faiss_index字段
        reverse_map = self.vector_store.reverse_id_maps.get(kb_id_str, {})
        for chunk in all_chunks:
            chunk.faiss_index = reverse_map.get(chunk.id)

        # 更新知识库chunk计数
        kb = (
            self.db.query(KnowledgeBase)
            .filter(KnowledgeBase.id == knowledge_base_id)
            .first()
        )
        if kb:
            kb.chunk_count = len(all_chunks)

        self.db.commit()

        logger.info(
            f"知识库 {knowledge_base_id} 重建完成，共 {len(all_chunks)} 个chunk"
        )

    async def query(
        self,
        question: str,
        kb_ids: List[int],
        top_k: int = 5,
    ) -> List[Dict[str, Any]]:
        """查询知识库

        Args:
            question: 查询问题
            kb_ids: 知识库ID列表
            top_k: 返回最相似的k个结果

        Returns:
            检索结果列表，每项包含:
                - chunk_id: 分块ID
                - content: 分块内容
                - score: 相似度分数
                - kb_id: 知识库ID
                - document_id: 文档ID
                - metadata: 元数据
        """
        from app.models.knowledge import DocumentChunk

        # 将kb_ids转为字符串列表
        kb_id_strs = [str(kb_id) for kb_id in kb_ids]

        # 执行检索
        raw_results = self.retriever.retrieve(question, kb_id_strs, top_k)

        if not raw_results:
            return []

        # 从数据库获取chunk详情
        chunk_ids = [r["chunk_id"] for r in raw_results]
        chunks = (
            self.db.query(DocumentChunk)
            .filter(DocumentChunk.id.in_(chunk_ids))
            .all()
        )
        chunk_map = {chunk.id: chunk for chunk in chunks}

        # 组装结果
        results: List[Dict[str, Any]] = []
        for raw in raw_results:
            chunk = chunk_map.get(raw["chunk_id"])
            if chunk:
                results.append(
                    {
                        "chunk_id": raw["chunk_id"],
                        "content": chunk.content,
                        "score": raw["score"],
                        "kb_id": raw["kb_id"],
                        "document_id": chunk.document_id,
                        "chunk_index": chunk.chunk_index,
                    }
                )

        return results
