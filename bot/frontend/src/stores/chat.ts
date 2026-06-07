import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getSessions, getMessages, createSession, deleteSession } from '@/api/chat'
import { createSSEConnection } from '@/api/request'
import type { ChatSession, ChatMessage } from '@/types/api'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<number | null>(null)
  const messages = ref<ChatMessage[]>([])
  const isLoading = ref(false)
  const isStreaming = ref(false)
  const streamingContent = ref('')
  const abortController = ref<AbortController | null>(null)

  const currentSession = computed(() =>
    sessions.value.find((s) => s.id === currentSessionId.value) || null
  )

  async function fetchSessions() {
    try {
      const res = await getSessions({ page: 1, page_size: 50 })
      const d = (res as any).data || res
      sessions.value = d.items || []
    } catch {
      sessions.value = []
    }
  }

  async function fetchMessages(sessionId: number) {
    isLoading.value = true
    try {
      const res = await getMessages(sessionId, { page: 1, page_size: 100 })
      const d = (res as any).data || res
      messages.value = d.items || []
    } catch {
      messages.value = []
    } finally {
      isLoading.value = false
    }
  }

  async function newSession(data?: { title?: string; knowledge_base_id?: number }) {
    const res = await createSession(data)
    const d = (res as any).data || res
    sessions.value.unshift(d)
    currentSessionId.value = d.id
    messages.value = []
    return d
  }

  async function removeSession(id: number) {
    await deleteSession(id)
    sessions.value = sessions.value.filter((s) => s.id !== id)
    if (currentSessionId.value === id) {
      currentSessionId.value = sessions.value.length > 0 ? sessions.value[0].id : null
      if (currentSessionId.value) {
        await fetchMessages(currentSessionId.value)
      } else {
        messages.value = []
      }
    }
  }

  function selectSession(id: number) {
    currentSessionId.value = id
    fetchMessages(id)
  }

  async function send(message: string, knowledgeBaseId?: number) {
    if (!currentSessionId.value) return

    // 添加用户消息
    const userMessage: any = {
      id: Date.now(),
      session_id: currentSessionId.value,
      role: 'user',
      content: message,
      created_at: new Date().toISOString()
    }
    messages.value.push(userMessage)

    // 添加空的助手消息占位
    const assistantMessage: any = {
      id: Date.now() + 1,
      session_id: currentSessionId.value,
      role: 'assistant',
      content: '',
      created_at: new Date().toISOString()
    }
    messages.value.push(assistantMessage)

    isStreaming.value = true
    streamingContent.value = ''

    abortController.value = createSSEConnection(
      '/chat/stream',
      {
        session_id: currentSessionId.value,
        message,
        knowledge_base_id: knowledgeBaseId,
        stream: true
      },
      (chunk: any) => {
        // 后端返回格式: {"type": "content", "content": "xxx"}
        if (chunk.type === 'content') {
          streamingContent.value += chunk.content || ''
          const lastMsg = messages.value[messages.value.length - 1]
          if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.content = streamingContent.value
          }
        } else if (chunk.type === 'session') {
          // 更新session_id
          if (chunk.session_id) {
            currentSessionId.value = chunk.session_id
          }
        } else if (chunk.type === 'error') {
          const lastMsg = messages.value[messages.value.length - 1]
          if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.content = '抱歉，回复出现问题：' + (chunk.content || '未知错误')
          }
          isStreaming.value = false
        } else if (chunk.type === 'done') {
          isStreaming.value = false
          // 刷新会话列表
          fetchSessions()
        }
      },
      (error) => {
        isStreaming.value = false
        console.error('Stream error:', error)
        const lastMsg = messages.value[messages.value.length - 1]
        if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.content) {
          lastMsg.content = '网络错误，请重试'
        }
      },
      () => {
        isStreaming.value = false
      }
    )
  }

  function stopStreaming() {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    isStreaming.value = false
  }

  return {
    sessions,
    currentSessionId,
    currentSession,
    messages,
    isLoading,
    isStreaming,
    streamingContent,
    fetchSessions,
    fetchMessages,
    newSession,
    removeSession,
    selectSession,
    send,
    stopStreaming
  }
})
