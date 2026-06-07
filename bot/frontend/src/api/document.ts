import { get, del, upload } from './request'
import type { Document, DocumentChunk, PageData } from '@/types/api'

export function getDocuments(params: { knowledge_base_id: number; page?: number; page_size?: number }) {
  return get<PageData<Document>>('/documents', params)
}

export function getDocument(id: number) {
  return get<Document>(`/documents/${id}`)
}

export function uploadDocument(knowledgeBaseId: number, files: File[], onProgress?: (event: any) => void) {
  const formData = new FormData()
  formData.append('knowledge_base_id', String(knowledgeBaseId))
  files.forEach((file) => {
    formData.append('files', file)
  })
  return upload<Document[]>('/documents/upload', formData, {
    onUploadProgress: onProgress
  })
}

export function deleteDocument(id: number) {
  return del(`/documents/${id}`)
}

export function getDocumentChunks(documentId: number) {
  return get<DocumentChunk[]>(`/documents/${documentId}/chunks`)
}
