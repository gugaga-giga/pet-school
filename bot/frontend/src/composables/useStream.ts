import { ref } from 'vue'
import { createSSEConnection } from '@/api/request'
import type { StreamChunk } from '@/types/api'

export function useStream() {
  const isStreaming = ref(false)
  const content = ref('')
  const sources = ref<any[]>([])
  const products = ref<any[]>([])
  const intent = ref('')
  const error = ref<string | null>(null)
  const abortController = ref<AbortController | null>(null)

  function startStream(
    url: string,
    body: Record<string, any>,
    onChunk?: (chunk: StreamChunk) => void
  ) {
    isStreaming.value = true
    content.value = ''
    sources.value = []
    products.value = []
    intent.value = ''
    error.value = null

    abortController.value = createSSEConnection(
      url,
      body,
      (chunk: StreamChunk) => {
        if (chunk.type === 'content') {
          content.value += chunk.data
        } else if (chunk.type === 'sources') {
          sources.value = chunk.data
        } else if (chunk.type === 'products') {
          products.value = chunk.data
        } else if (chunk.type === 'intent') {
          intent.value = chunk.data
        }
        onChunk?.(chunk)
      },
      (err) => {
        isStreaming.value = false
        error.value = err.message
      },
      () => {
        isStreaming.value = false
      }
    )
  }

  function stopStream() {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    isStreaming.value = false
  }

  return {
    isStreaming,
    content,
    sources,
    products,
    intent,
    error,
    startStream,
    stopStream
  }
}
