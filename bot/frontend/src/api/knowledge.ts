import { get, post, put, del } from './request'
import type { KnowledgeBase, KnowledgeBaseCreate, KnowledgeBaseUpdate, PageData } from '@/types/api'

export function getKnowledgeBases(params?: { page?: number; page_size?: number; keyword?: string }) {
  return get<PageData<KnowledgeBase>>('/knowledge-bases', params)
}

export function getKnowledgeBase(id: number) {
  return get<KnowledgeBase>(`/knowledge-bases/${id}`)
}

export function createKnowledgeBase(data: KnowledgeBaseCreate) {
  return post<KnowledgeBase>('/knowledge-bases', data)
}

export function updateKnowledgeBase(id: number, data: KnowledgeBaseUpdate) {
  return put<KnowledgeBase>(`/knowledge-bases/${id}`, data)
}

export function deleteKnowledgeBase(id: number) {
  return del(`/knowledge-bases/${id}`)
}
