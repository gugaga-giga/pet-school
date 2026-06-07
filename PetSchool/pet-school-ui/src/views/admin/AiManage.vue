<template>
  <div class="ai-manage">
    <div class="page-header">
      <h2 class="page-title">🤖 AI客服管理</h2>
    </div>

    <!-- Tab导航 -->
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.icon }} {{ tab.label }}
      </button>
    </div>

    <!-- Tab内容 -->
    <div class="tab-content">
      <!-- 会话管理 -->
      <div v-show="activeTab === 'sessions'" class="tab-panel">
        <div class="panel-toolbar">
          <input v-model="sessionFilter.userId" placeholder="按用户ID筛选" class="input" />
          <input v-model="sessionFilter.startDate" type="date" class="input" />
          <input v-model="sessionFilter.endDate" type="date" class="input" />
          <button @click="loadSessions" class="btn btn-primary btn-sm">查询</button>
          <button @click="resetSessionFilter" class="btn btn-secondary btn-sm">重置</button>
        </div>
        <div class="card table-card">
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户ID</th>
                  <th>标题</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in sessions" :key="s.id">
                  <td>{{ s.id }}</td>
                  <td>{{ s.userId }}</td>
                  <td>{{ s.title || '未命名' }}</td>
                  <td>{{ formatTime(s.createdAt) }}</td>
                  <td class="td-actions">
                    <button @click="viewSession(s.id)" class="btn btn-ghost btn-sm">查看</button>
                    <button @click="deleteSession(s.id)" class="btn btn-danger btn-sm">删除</button>
                  </td>
                </tr>
                <tr v-if="!sessions.length">
                  <td colspan="5">
                    <div class="empty-state">
                      <div class="empty-icon">💬</div>
                      <div class="empty-text">暂无会话记录</div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 会话消息弹窗 -->
        <div v-if="selectedSession" class="modal-overlay" @click.self="selectedSession = null">
          <div class="modal-content session-modal">
            <div class="modal-header">
              <h3 class="modal-title">会话详情 #{{ selectedSession.id }}</h3>
              <button @click="selectedSession = null" class="btn btn-ghost btn-sm">✕</button>
            </div>
            <div class="modal-body">
              <div v-if="!sessionMessages.length" class="empty-state">
                <div class="empty-text">暂无消息</div>
              </div>
              <div v-for="msg in sessionMessages" :key="msg.id" class="msg-item" :class="msg.role">
                <span class="msg-role">{{ msg.role === 'user' ? '用户' : 'AI' }}</span>
                <span class="msg-content">{{ msg.content }}</span>
                <span class="msg-intent" v-if="msg.intentType">{{ intentLabel(msg.intentType) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 知识库管理 -->
      <div v-show="activeTab === 'knowledge'" class="tab-panel">
        <div class="panel-toolbar">
          <button @click="syncAllCourses" class="btn btn-primary btn-sm" :disabled="syncing">
            {{ syncing ? '同步中...' : '同步所有课程到RAG' }}
          </button>
          <span v-if="syncResult" class="sync-result">{{ syncResult }}</span>
        </div>
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-value">{{ knowledgeStats.totalDocs }}</div>
            <div class="stat-label">文档总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ knowledgeStats.totalChunks }}</div>
            <div class="stat-label">分块总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ knowledgeStats.courseCount }}</div>
            <div class="stat-label">课程数</div>
          </div>
        </div>
        <div class="card table-card">
          <div class="card-header">
            <h3 class="card-title">知识库文档</h3>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>文档ID</th>
                  <th>标题</th>
                  <th>类型</th>
                  <th>分块数</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="doc in knowledgeDocs" :key="doc.id">
                  <td>{{ doc.id }}</td>
                  <td>{{ doc.title || '-' }}</td>
                  <td><span class="tag tag-info">{{ doc.type || '课程' }}</span></td>
                  <td>{{ doc.chunkCount ?? '-' }}</td>
                  <td><span class="tag" :class="doc.status === 'SYNCED' ? 'tag-success' : 'tag-warning'">{{ doc.status === 'SYNCED' ? '已同步' : (doc.status || '未知') }}</span></td>
                </tr>
                <tr v-if="!knowledgeDocs.length">
                  <td colspan="5">
                    <div class="empty-state">
                      <div class="empty-icon">📚</div>
                      <div class="empty-text">暂无知识库文档</div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 系统日志 -->
      <div v-show="activeTab === 'logs'" class="tab-panel">
        <div class="panel-toolbar">
          <select v-model="logFilter.intentType" class="input">
            <option value="">全部意图</option>
            <option value="FAQ">FAQ</option>
            <option value="Course">课程咨询</option>
            <option value="PetKnowledge">宠物知识</option>
            <option value="Order">订单查询</option>
            <option value="PetProfile">宠物档案</option>
            <option value="UserInfo">用户信息</option>
            <option value="Greeting">问候</option>
          </select>
          <input v-model="logFilter.startDate" type="date" class="input" />
          <input v-model="logFilter.endDate" type="date" class="input" />
          <button @click="loadLogs" class="btn btn-primary btn-sm">查询</button>
        </div>
        <div class="card table-card">
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>时间</th>
                  <th>用户ID</th>
                  <th>消息</th>
                  <th>意图</th>
                  <th>角色</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in logs" :key="log.id">
                  <td>{{ formatTime(log.createdAt) }}</td>
                  <td>{{ log.userId || '-' }}</td>
                  <td class="msg-cell">{{ log.content }}</td>
                  <td><span class="intent-badge">{{ intentLabel(log.intentType) || '-' }}</span></td>
                  <td>{{ log.role === 'user' ? '用户' : 'AI' }}</td>
                </tr>
                <tr v-if="!logs.length">
                  <td colspan="5">
                    <div class="empty-state">
                      <div class="empty-icon">📋</div>
                      <div class="empty-text">暂无日志记录</div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- AI参数配置 -->
      <div v-show="activeTab === 'config'" class="tab-panel">
        <div class="card">
          <div class="config-section">
            <h3>Prompt模板</h3>
            <textarea v-model="config.systemPrompt" class="config-textarea" rows="8" placeholder="请输入系统Prompt模板..."></textarea>
          </div>
        </div>
        <div class="card">
          <div class="config-section">
            <h3>意图关键词规则</h3>
            <table>
              <thead>
                <tr>
                  <th>意图</th>
                  <th>关键词（逗号分隔）</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(keywords, intent) in config.intentRules" :key="intent">
                  <td><span class="intent-badge">{{ intentLabel(intent) }}</span></td>
                  <td><input v-model="config.intentRules[intent]" class="input input-full" /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <button @click="saveConfig" class="btn btn-primary">保存配置</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminRequest } from '../../api/index.js'

const tabs = [
  { key: 'sessions', icon: '💬', label: '会话管理' },
  { key: 'knowledge', icon: '📚', label: '知识库管理' },
  { key: 'logs', icon: '📋', label: '系统日志' },
  { key: 'config', icon: '⚙️', label: 'AI参数配置' }
]

const activeTab = ref('sessions')

// ========== 会话管理 ==========
const sessions = ref([])
const selectedSession = ref(null)
const sessionMessages = ref([])
const sessionFilter = reactive({
  userId: '',
  startDate: '',
  endDate: ''
})

async function loadSessions() {
  try {
    const params = {}
    if (sessionFilter.userId) params.userId = sessionFilter.userId
    if (sessionFilter.startDate) params.startDate = sessionFilter.startDate
    if (sessionFilter.endDate) params.endDate = sessionFilter.endDate
    const res = await adminRequest.get('/ai/sessions', { params })
    if (res.code === 200) {
      sessions.value = res.data || []
    }
  } catch {
    sessions.value = []
  }
}

function resetSessionFilter() {
  sessionFilter.userId = ''
  sessionFilter.startDate = ''
  sessionFilter.endDate = ''
  loadSessions()
}

async function viewSession(id) {
  try {
    const res = await adminRequest.get(`/ai/sessions/${id}/messages`)
    if (res.code === 200) {
      sessionMessages.value = res.data || []
      selectedSession.value = { id }
    }
  } catch {
    sessionMessages.value = []
  }
}

async function deleteSession(id) {
  if (!confirm('确定删除该会话？')) return
  try {
    const res = await adminRequest.delete(`/ai/sessions/${id}`)
    if (res.code === 200) {
      loadSessions()
    } else {
      alert(res.message || '删除失败')
    }
  } catch {
    alert('请求失败')
  }
}

// ========== 知识库管理 ==========
const syncing = ref(false)
const syncResult = ref('')
const knowledgeDocs = ref([])
const knowledgeStats = reactive({
  totalDocs: 0,
  totalChunks: 0,
  courseCount: 0
})

async function syncAllCourses() {
  syncing.value = true
  syncResult.value = ''
  try {
    const res = await adminRequest.post('/course/admin/sync-rag-all')
    if (res.code === 200) {
      syncResult.value = '同步成功'
      loadKnowledge()
    } else {
      syncResult.value = res.message || '同步失败'
    }
  } catch {
    syncResult.value = '同步请求失败'
  } finally {
    syncing.value = false
  }
}

async function loadKnowledge() {
  try {
    const res = await adminRequest.get('/ai/knowledge/documents')
    if (res.code === 200) {
      knowledgeDocs.value = res.data?.documents || res.data || []
      knowledgeStats.totalDocs = res.data?.totalDocs || knowledgeDocs.value.length
      knowledgeStats.totalChunks = res.data?.totalChunks || 0
      knowledgeStats.courseCount = res.data?.courseCount || 0
    }
  } catch {
    knowledgeDocs.value = []
  }
}

// ========== 系统日志 ==========
const logs = ref([])
const logFilter = reactive({
  intentType: '',
  startDate: '',
  endDate: ''
})

async function loadLogs() {
  try {
    const res = await adminRequest.get('/ai/sessions')
    if (res.code === 200) {
      const allSessions = res.data || []
      let allMessages = []
      for (const session of allSessions) {
        try {
          const msgRes = await adminRequest.get(`/ai/sessions/${session.id}/messages`)
          if (msgRes.code === 200 && msgRes.data) {
            const msgs = msgRes.data.map(m => ({
              ...m,
              userId: session.userId
            }))
            allMessages = allMessages.concat(msgs)
          }
        } catch {
          // skip failed session
        }
      }
      // 按意图筛选
      if (logFilter.intentType) {
        allMessages = allMessages.filter(m => m.intentType === logFilter.intentType)
      }
      // 按时间筛选
      if (logFilter.startDate) {
        allMessages = allMessages.filter(m => m.createdAt >= logFilter.startDate)
      }
      if (logFilter.endDate) {
        allMessages = allMessages.filter(m => m.createdAt <= logFilter.endDate + 'T23:59:59')
      }
      logs.value = allMessages
    }
  } catch {
    logs.value = []
  }
}

// ========== AI参数配置 ==========
const config = reactive({
  systemPrompt: '',
  intentRules: {
    FAQ: '常见问题,faq,帮助',
    Course: '课程,培训,训练,上课',
    PetKnowledge: '宠物知识,养宠,喂养,饲养',
    Order: '订单,购买,支付,退款',
    PetProfile: '宠物档案,宠物信息,我的宠物',
    UserInfo: '个人信息,账号,修改密码',
    Greeting: '你好,hello,嗨,在吗'
  }
})

function loadConfig() {
  try {
    const saved = localStorage.getItem('ai_config')
    if (saved) {
      const parsed = JSON.parse(saved)
      if (parsed.systemPrompt !== undefined) config.systemPrompt = parsed.systemPrompt
      if (parsed.intentRules) config.intentRules = { ...config.intentRules, ...parsed.intentRules }
    }
  } catch {
    // ignore
  }
}

function saveConfig() {
  try {
    localStorage.setItem('ai_config', JSON.stringify({
      systemPrompt: config.systemPrompt,
      intentRules: config.intentRules
    }))
    alert('配置已保存')
  } catch {
    alert('保存失败')
  }
}

// ========== 工具方法 ==========
function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const intentMap = {
  FAQ: 'FAQ',
  Course: '课程咨询',
  PetKnowledge: '宠物知识',
  Order: '订单查询',
  PetProfile: '宠物档案',
  UserInfo: '用户信息',
  Greeting: '问候'
}

function intentLabel(type) {
  return intentMap[type] || type || '-'
}

// ========== 初始化 ==========
onMounted(() => {
  loadSessions()
  loadKnowledge()
  loadConfig()
})
</script>

<style scoped>
.ai-manage {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-4);
}

/* ========== Tab导航 ========== */
.tab-nav {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  padding: var(--space-1);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-body);
  background: transparent;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.tab-btn:hover {
  background: var(--bg-hover);
  color: var(--text-title);
}

.tab-btn.active {
  background: var(--color-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-button);
}

.tab-btn.active:hover {
  background: var(--color-primary-dark);
}

/* ========== Tab面板 ========== */
.tab-panel {
  animation: fadeIn var(--transition-fast) ease;
}

/* ========== 工具栏 ========== */
.panel-toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
}

.input {
  padding: 8px 14px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family);
  background: var(--bg-input);
  color: var(--text-title);
  outline: none;
  transition: all var(--transition-base);
  min-width: 160px;
}

.input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
  background: var(--bg-card);
}

.input::placeholder {
  color: var(--text-muted);
}

.input-full {
  width: 100%;
  min-width: 0;
}

/* ========== 表格卡片 ========== */
.table-card {
  padding: 0;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.card-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0;
}

.table-wrap {
  overflow-x: auto;
}

.td-actions {
  display: flex;
  gap: var(--space-1);
}

/* ========== 消息单元格 ========== */
.msg-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 意图标签 ========== */
.intent-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
}

/* ========== 统计卡片 ========== */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.stats-cards .stat-card {
  text-align: center;
  padding: var(--space-5);
}

.stats-cards .stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  letter-spacing: -0.02em;
}

.stats-cards .stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

/* ========== 会话消息弹窗 ========== */
.session-modal {
  width: 640px;
  max-width: 95vw;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
}

.msg-item.user {
  background: var(--color-primary-bg);
}

.msg-item.assistant {
  background: var(--bg-hover);
}

.msg-role {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--text-muted);
  white-space: nowrap;
  min-width: 32px;
}

.msg-content {
  flex: 1;
  font-size: var(--font-size-sm);
  color: var(--text-title);
  line-height: 1.6;
  word-break: break-word;
}

.msg-intent {
  font-size: var(--font-size-xs);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  background: var(--color-accent-bg);
  color: var(--color-accent-dark);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
  flex-shrink: 0;
}

/* ========== 配置区域 ========== */
.config-section {
  margin-bottom: var(--space-4);
}

.config-section h3 {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin-bottom: var(--space-3);
}

.config-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family);
  background: var(--bg-input);
  color: var(--text-title);
  outline: none;
  transition: all var(--transition-base);
  resize: vertical;
  min-height: 120px;
  line-height: 1.6;
}

.config-textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
  background: var(--bg-card);
}

.config-textarea::placeholder {
  color: var(--text-muted);
}

/* ========== 同步结果 ========== */
.sync-result {
  font-size: var(--font-size-sm);
  color: var(--color-success);
  font-weight: var(--font-weight-medium);
  margin-left: var(--space-2);
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .tab-nav {
    flex-wrap: wrap;
  }

  .tab-btn {
    padding: 8px 14px;
    font-size: var(--font-size-xs);
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .panel-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .panel-toolbar .input {
    min-width: 0;
    width: 100%;
  }

  .msg-cell {
    max-width: 160px;
  }
}
</style>
