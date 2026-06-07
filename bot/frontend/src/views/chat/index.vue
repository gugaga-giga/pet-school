<template>
  <div class="chat-page">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" @click="handleNewSession">
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
      </div>
      <div class="session-list">
        <div
          v-for="session in chatStore.sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === chatStore.currentSessionId }"
          @click="chatStore.selectSession(session.id)"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span class="session-title">{{ session.title || '新对话' }}</span>
          <el-icon class="delete-icon" @click.stop="handleDeleteSession(session.id)">
            <Delete />
          </el-icon>
        </div>
        <el-empty v-if="chatStore.sessions.length === 0" description="暂无对话" :image-size="80" />
      </div>
    </div>

    <div class="chat-main">
      <div v-if="!chatStore.currentSessionId" class="chat-empty">
        <el-icon :size="64" color="#c0c4cc"><ChatDotRound /></el-icon>
        <h3>开始一段新对话</h3>
        <p>选择知识库，向智能客服提问</p>
        <el-button type="primary" @click="handleNewSession">新建对话</el-button>
      </div>

      <template v-else>
        <div class="chat-header">
          <span class="chat-title">{{ chatStore.currentSession?.title || '新对话' }}</span>
          <el-select
            v-model="selectedKB"
            placeholder="选择知识库"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </div>

        <div ref="messageListRef" class="message-list">
          <div v-if="chatStore.messages.length === 0" class="message-empty">
            <el-icon :size="48" color="#c0c4cc"><ChatLineSquare /></el-icon>
            <p>向智能客服提问，获取专业售前咨询</p>
          </div>
          <ChatMessage
            v-for="msg in chatStore.messages"
            :key="msg.id"
            :message="msg"
          />
          <div v-if="chatStore.isStreaming" class="streaming-indicator">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="输入您的问题，Enter发送，Shift+Enter换行"
            resize="none"
            @keydown="handleKeydown"
          />
          <div class="input-actions">
            <el-button
              v-if="chatStore.isStreaming"
              type="danger"
              circle
              @click="chatStore.stopStreaming()"
            >
              <el-icon><VideoPause /></el-icon>
            </el-button>
            <el-button
              v-else
              type="primary"
              :disabled="!inputMessage.trim()"
              circle
              @click="handleSend"
            >
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { getKnowledgeBases } from '@/api/knowledge'
import ChatMessage from '@/components/ChatMessage.vue'
import type { KnowledgeBase } from '@/types/api'

const chatStore = useChatStore()
const inputMessage = ref('')
const selectedKB = ref<number | undefined>()
const knowledgeBases = ref<KnowledgeBase[]>([])
const messageListRef = ref<HTMLElement>()

onMounted(async () => {
  await chatStore.fetchSessions()
  await fetchKnowledgeBases()
  if (chatStore.sessions.length > 0 && !chatStore.currentSessionId) {
    chatStore.selectSession(chatStore.sessions[0].id)
  }
})

watch(
  () => chatStore.messages.length,
  () => {
    scrollToBottom()
  }
)

watch(
  () => chatStore.messages[chatStore.messages.length - 1]?.content,
  () => {
    scrollToBottom()
  }
)

async function fetchKnowledgeBases() {
  try {
    const res = await getKnowledgeBases({ page: 1, page_size: 100 })
    knowledgeBases.value = res.data.items
  } catch {
    // ignore
  }
}

async function handleNewSession() {
  await chatStore.newSession({
    knowledge_base_id: selectedKB.value
  })
}

async function handleDeleteSession(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该对话？', '提示', {
      type: 'warning'
    })
    await chatStore.removeSession(id)
  } catch {
    // cancelled
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

async function handleSend() {
  const msg = inputMessage.value.trim()
  if (!msg || chatStore.isStreaming) return

  inputMessage.value = ''
  await chatStore.send(msg, selectedKB.value)
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 100px);
  background: var(--el-bg-color);
  border-radius: 8px;
  overflow: hidden;
  margin: -20px;
}

.chat-sidebar {
  width: 260px;
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);

  .sidebar-header {
    padding: 16px;

    .new-chat-btn {
      width: 100%;
    }
  }

  .session-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 8px;

    .session-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      margin-bottom: 4px;

      &:hover {
        background: var(--el-fill-color-light);
      }

      &.active {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }

      .session-title {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 14px;
      }

      .delete-icon {
        opacity: 0;
        color: var(--el-text-color-secondary);
        transition: opacity 0.2s;

        &:hover {
          color: var(--el-color-danger);
        }
      }

      &:hover .delete-icon {
        opacity: 1;
      }
    }
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--el-text-color-secondary);

  h3 {
    font-size: 18px;
    color: var(--el-text-color-primary);
  }

  p {
    font-size: 14px;
  }
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .chat-title {
    font-size: 16px;
    font-weight: 600;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;

  .message-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    gap: 12px;
    color: var(--el-text-color-secondary);
  }
}

.streaming-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 16px;

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--el-color-primary);
    animation: bounce 1.4s infinite ease-in-out both;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  gap: 12px;
  align-items: flex-end;

  :deep(.el-textarea__inner) {
    border-radius: 12px;
    padding: 12px 16px;
  }

  .input-actions {
    display: flex;
    gap: 8px;
    padding-bottom: 4px;
  }
}
</style>
