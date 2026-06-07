<template>
  <!-- 悬浮按钮 -->
  <div class="ai-fab" @click="toggleChat" v-show="!isOpen">
    <span class="fab-icon">🤖</span>
    <span class="fab-pulse"></span>
  </div>

  <!-- 聊天窗口 -->
  <transition name="chat-window">
    <div class="ai-chat-window" v-show="isOpen">
      <!-- 标题栏 -->
      <div class="chat-header">
        <div class="header-left">
          <span class="header-icon">🤖</span>
          <div>
            <div class="header-title">AI宠物顾问</div>
            <div class="header-status">在线</div>
          </div>
        </div>
        <div class="header-actions">
          <button @click="newSession" title="新会话">➕</button>
          <button @click="toggleChat" title="关闭">✕</button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="chat-welcome">
          <div class="welcome-icon">🐾</div>
          <div class="welcome-text">你好！我是AI宠物顾问，有什么可以帮你的吗？</div>
          <div class="quick-questions">
            <button @click="sendQuick('有什么训练课程？')">📚 课程咨询</button>
            <button @click="sendQuick('我家宠物的疫苗记录？')">💉 疫苗查询</button>
            <button @click="sendQuick('我的订单状态？')">📋 订单查询</button>
            <button @click="sendQuick('金毛怎么养？')">🐕 养宠知识</button>
          </div>
        </div>
        <div v-for="msg in messages" :key="msg.id" class="chat-message" :class="msg.role">
          <div class="message-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="message-body">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-meta" v-if="msg.intentType">
              <span class="intent-tag">{{ intentLabel(msg.intentType) }}</span>
            </div>
          </div>
        </div>
        <div v-if="loading" class="chat-message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-body">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <input
          v-model="inputText"
          @keyup.enter="sendMessage"
          placeholder="输入你的问题..."
          :disabled="loading"
          class="chat-input"
        />
        <button @click="sendMessage" :disabled="loading || !inputText.trim()" class="send-btn">
          发送
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { aiApi } from '../api/ai'

const isOpen = ref(false)
const loading = ref(false)
const inputText = ref('')
const messages = ref([])
const currentSessionId = ref(null)
const messagesContainer = ref(null)

let msgIdCounter = 0

const intentMap = {
  COURSE: '课程咨询',
  VACCINE: '疫苗查询',
  ORDER: '订单查询',
  PET_KNOWLEDGE: '宠物知识',
  MEDICAL: '医疗咨询',
  BOARDING: '寄养服务',
  CERTIFICATE: '证书查询',
  GENERAL: '通用问答',
  GREETING: '问候',
  UNKNOWN: '其他'
}

function intentLabel(type) {
  return intentMap[type] || type || '其他'
}

function toggleChat() {
  isOpen.value = !isOpen.value
  if (isOpen.value && messages.value.length === 0) {
    loadSession()
  }
}

async function loadSession() {
  try {
    const res = await aiApi.sessions()
    const list = res.data || res || []
    if (Array.isArray(list) && list.length > 0) {
      currentSessionId.value = list[0].sessionId || list[0].id
      await loadMessages(currentSessionId.value)
    }
  } catch {
    // 无历史会话，忽略
  }
}

async function loadMessages(sessionId) {
  try {
    const res = await aiApi.messages(sessionId)
    const list = res.data || res || []
    if (Array.isArray(list)) {
      messages.value = list.map(m => ({
        id: ++msgIdCounter,
        role: m.role || (m.isUser ? 'user' : 'assistant'),
        content: m.content || m.message || '',
        intentType: m.intentType || null
      }))
      scrollToBottom()
    }
  } catch {
    // 加载失败忽略
  }
}

function newSession() {
  messages.value = []
  currentSessionId.value = null
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  messages.value.push({
    id: ++msgIdCounter,
    role: 'user',
    content: text,
    intentType: null
  })
  inputText.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const res = await aiApi.chat({
      message: text,
      sessionId: currentSessionId.value
    })
    const data = res.data || res
    if (data.sessionId) {
      currentSessionId.value = data.sessionId
    }
    messages.value.push({
      id: ++msgIdCounter,
      role: 'assistant',
      content: data.content || data.message || data.reply || '抱歉，我暂时无法回答这个问题。',
      intentType: data.intentType || data.intent || null
    })
  } catch {
    messages.value.push({
      id: ++msgIdCounter,
      role: 'assistant',
      content: '网络异常，请稍后重试。',
      intentType: null
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function sendQuick(text) {
  inputText.value = text
  sendMessage()
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  // 预加载不打开窗口
})
</script>

<style scoped>
/* ========== 悬浮按钮 ========== */
.ai-fab {
  position: fixed;
  bottom: 28px;
  right: 28px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 9999;
  box-shadow: 0 4px 16px rgba(79, 142, 247, 0.4);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  user-select: none;
}

.ai-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(79, 142, 247, 0.5);
}

.ai-fab:active {
  transform: scale(0.96);
}

.fab-icon {
  font-size: 26px;
  line-height: 1;
  z-index: 1;
}

.fab-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--color-primary);
  animation: fabPulse 2s ease-out infinite;
  z-index: 0;
}

@keyframes fabPulse {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.8); opacity: 0; }
}

/* ========== 聊天窗口 ========== */
.ai-chat-window {
  position: fixed;
  bottom: 28px;
  right: 28px;
  width: 380px;
  height: 520px;
  border-radius: var(--radius-xl);
  display: flex;
  flex-direction: column;
  z-index: 9999;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-float);
}

/* ========== 窗口动画 ========== */
.chat-window-enter-active {
  animation: chatWindowIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.chat-window-leave-active {
  animation: chatWindowOut 0.2s ease;
}

@keyframes chatWindowIn {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes chatWindowOut {
  from {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
  to {
    opacity: 0;
    transform: scale(0.8) translateY(20px);
  }
}

/* ========== 标题栏 ========== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--text-inverse);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 24px;
}

.header-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  line-height: 1.3;
}

.header-status {
  font-size: var(--font-size-xs);
  opacity: 0.85;
  line-height: 1.2;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.header-actions button {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.2);
  color: var(--text-inverse);
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-fast);
}

.header-actions button:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* ========== 消息列表 ========== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ========== 欢迎区域 ========== */
.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  text-align: center;
}

.welcome-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.welcome-text {
  font-size: var(--font-size-base);
  color: var(--text-body);
  margin-bottom: 20px;
  line-height: var(--line-height-relaxed);
}

.quick-questions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  width: 100%;
}

.quick-questions button {
  padding: 10px 12px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--text-body);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: var(--font-family);
  white-space: nowrap;
}

.quick-questions button:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

/* ========== 消息气泡 ========== */
.chat-message {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  background: var(--bg-hover);
}

.chat-message.assistant .message-avatar {
  background: var(--color-primary-bg);
}

.message-body {
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-message.user .message-body {
  align-items: flex-end;
}

.message-content {
  padding: 10px 14px;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-base);
  line-height: var(--line-height-relaxed);
  word-break: break-word;
}

.chat-message.user .message-content {
  background: var(--color-primary);
  color: var(--text-inverse);
  border-bottom-right-radius: 4px;
}

.chat-message.assistant .message-content {
  background: var(--bg-card);
  color: var(--text-title);
  border: 1px solid var(--border-light);
  border-bottom-left-radius: 4px;
}

.message-meta {
  display: flex;
  align-items: center;
}

.intent-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
}

/* ========== 打字指示器 ========== */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  border-bottom-left-radius: 4px;
}

.typing-indicator span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--text-muted);
  animation: typingBounce 1.2s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* ========== 输入区域 ========== */
.chat-input-area {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--border-light);
  background: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

.chat-input {
  flex: 1;
  padding: 10px 14px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-family: var(--font-family);
  background: var(--bg-input);
  color: var(--text-title);
  outline: none;
  transition: all var(--transition-base);
}

.chat-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
  background: var(--bg-card);
}

.chat-input::placeholder {
  color: var(--text-muted);
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn {
  padding: 10px 18px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--text-inverse);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family);
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.send-btn:active:not(:disabled) {
  transform: scale(0.96);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ========== 移动端适配 ========== */
@media (max-width: 480px) {
  .ai-fab {
    bottom: 16px;
    right: 16px;
    width: 50px;
    height: 50px;
  }

  .fab-icon {
    font-size: 22px;
  }

  .ai-chat-window {
    bottom: 0;
    right: 0;
    width: 100%;
    height: 70vh;
    border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  }

  .message-body {
    max-width: 85%;
  }
}
</style>
