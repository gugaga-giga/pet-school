import { get, post, del } from './request'
import type { ChatSession, ChatMessage, SessionCreateRequest, PageData } from '@/types/api'

export function createSession(data?: SessionCreateRequest) {
  return post<ChatSession>('/chat/sessions', data)
}

export function getSessions(params?: { page?: number; page_size?: number }) {
  return get<PageData<ChatSession>>('/chat/sessions', params)
}

export function getSession(id: number) {
  return get<ChatSession>(`/chat/sessions/${id}`)
}

export function deleteSession(id: number) {
  return del(`/chat/sessions/${id}`)
}

export function getMessages(sessionId: number, params?: { page?: number; page_size?: number }) {
  return get<PageData<ChatMessage>>(`/chat/sessions/${sessionId}/messages`, params)
}

export function sendMessage(data: { session_id: number; message: string; knowledge_base_id?: number }) {
  return post<ChatMessage>('/chat/send', data)
}
