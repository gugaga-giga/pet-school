import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResponse } from '@/types/api'

const service: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code !== 0 && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, { params, ...config }).then((res) => res.data)
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, data, config).then((res) => res.data)
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.put(url, data, config).then((res) => res.data)
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.delete(url, config).then((res) => res.data)
}

export function upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    ...config
  }).then((res) => res.data)
}

export function createSSEConnection(
  url: string,
  body: Record<string, any>,
  onMessage: (data: any) => void,
  onError?: (error: any) => void,
  onDone?: () => void
): AbortController {
  const controller = new AbortController()
  const token = localStorage.getItem('access_token')

  fetch(`/api/v1${url}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(body),
    signal: controller.signal
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const reader = response.body?.getReader()
      if (!reader) return

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6)
            if (data === '[DONE]') {
              onDone?.()
              return
            }
            try {
              const parsed = JSON.parse(data)
              onMessage(parsed)
            } catch {
              onMessage({ type: 'content', data })
            }
          }
        }
      }
      onDone?.()
    })
    .catch((error) => {
      if (error.name !== 'AbortError') {
        onError?.(error)
      }
    })

  return controller
}

export default service
