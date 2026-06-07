import axios from 'axios'

// AI客服API - 使用client_token认证
function getAiRequest() {
  const instance = axios.create({
    baseURL: '/api',
    timeout: 30000
  })
  instance.interceptors.request.use((config) => {
    // 优先用client_token，管理员用admin_token
    const token = localStorage.getItem('client_token') || localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  })
  instance.interceptors.response.use(
    (response) => response.data,
    (error) => Promise.reject(error)
  )
  return instance
}

const aiRequest = getAiRequest()

export const aiApi = {
  // 同步聊天
  chat: (data) => aiRequest.post('/ai/chat/sync', data),
  // 获取会话列表
  sessions: () => aiRequest.get('/ai/sessions'),
  // 获取会话消息
  messages: (sessionId) => aiRequest.get(`/ai/sessions/${sessionId}/messages`),
  // 删除会话
  deleteSession: (sessionId) => aiRequest.delete(`/ai/sessions/${sessionId}`),
  // 获取用户记忆
  memory: () => aiRequest.get('/ai/memory'),
  // SSE流式聊天
  chatStream: (message, sessionId, onMessage, onDone, onError) => {
    const token = localStorage.getItem('client_token') || localStorage.getItem('admin_token')
    const params = new URLSearchParams()
    if (token) params.set('token', token)

    const body = JSON.stringify({ message, sessionId })

    fetch(`/api/ai/chat?${params.toString()}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body
    }).then(response => {
      const reader = response.body.getReader()
      const decoder = new TextDecoder()

      function read() {
        reader.read().then(({ done, value }) => {
          if (done) {
            onDone && onDone()
            return
          }
          const text = decoder.decode(value, { stream: true })
          // 解析SSE数据
          const lines = text.split('\n')
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const data = line.substring(5).trim()
              if (data === '[DONE]') {
                onDone && onDone()
                return
              }
              try {
                const parsed = JSON.parse(data)
                onMessage && onMessage(parsed)
              } catch {
                onMessage && onMessage({ content: data })
              }
            }
          }
          read()
        }).catch(err => {
          onError && onError(err)
        })
      }
      read()
    }).catch(err => {
      onError && onError(err)
    })
  }
}
