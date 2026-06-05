<template>
  <div class="pet-detail-page">
    <!-- Pet Profile Card -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="profile-avatar">{{ petTypeEmoji }}</div>
        <div class="profile-identity">
          <h1 class="profile-name">{{ pet.name || '加载中...' }}</h1>
          <span class="profile-breed">{{ pet.breed || '-' }}</span>
        </div>
        <button class="btn btn-secondary btn-sm edit-btn" @click="goEdit">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          编辑
        </button>
      </div>
      <div class="profile-body">
        <div class="profile-meta">
          <div class="meta-item">
            <span class="meta-icon">{{ pet.gender === 1 ? '♂' : pet.gender === 2 ? '♀' : '?' }}</span>
            <span class="meta-label">{{ pet.gender === 1 ? '公' : pet.gender === 2 ? '母' : '未知' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-icon">🎂</span>
            <span class="meta-label">{{ pet.age != null ? pet.age + '岁' : '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-icon">⚖️</span>
            <span class="meta-label">{{ pet.weight != null ? pet.weight + 'kg' : '-' }}</span>
          </div>
          <div class="meta-item">
            <span :class="['status-badge', pet.status === 1 ? 'badge-active' : 'badge-inactive']">
              {{ pet.status === 1 ? '活跃' : '离场' }}
            </span>
          </div>
        </div>
        <div class="profile-pills">
          <span class="pill" :class="pet.sterilized ? 'pill-success' : 'pill-default'">
            {{ pet.sterilized ? '已绝育' : '未绝育' }}
          </span>
          <span v-if="pet.bloodType" class="pill pill-info">🩸 {{ pet.bloodType }}</span>
          <span v-if="pet.color" class="pill pill-accent">🎨 {{ pet.color }}</span>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tab-bar">
      <button
        v-for="t in tabs"
        :key="t.key"
        :class="['tab-pill', { 'tab-pill--active': activeTab === t.key }]"
        @click="activeTab = t.key"
      >{{ t.icon }} {{ t.label }}</button>
    </div>

    <!-- Tab Content -->
    <div class="tab-content">
      <!-- Course Orders -->
      <template v-if="activeTab === 'course'">
        <div v-if="courseOrders.length" class="record-list">
          <div v-for="o in courseOrders" :key="o.id" class="record-card record-card--course">
            <div class="record-main">
              <div class="record-title">{{ o.courseName || '未知课程' }}</div>
              <div class="record-sub">套餐：{{ o.packageName || '-' }} · 宠物：{{ o.petName || '-' }}</div>
              <div class="record-time">{{ formatTime(o.createTime) }}</div>
            </div>
            <span :class="['tag', orderStatusClass(o.status)]">{{ orderStatusText(o.status) }}</span>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon">📚</div>
          <div class="empty-text">暂无相关记录</div>
        </div>
      </template>

      <!-- Boarding Orders -->
      <template v-if="activeTab === 'boarding'">
        <div v-if="boardingOrders.length" class="record-list">
          <div v-for="o in boardingOrders" :key="o.id" class="record-card record-card--boarding">
            <div class="record-main">
              <div class="record-title">{{ o.roomTypeName || '未知房型' }}</div>
              <div class="record-sub">房间：{{ o.roomNumber || '-' }} · {{ o.checkIn || '-' }} ~ {{ o.checkOut || '-' }}</div>
            </div>
            <span :class="['tag', orderStatusClass(o.status)]">{{ orderStatusText(o.status) }}</span>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon">🏠</div>
          <div class="empty-text">暂无相关记录</div>
        </div>
      </template>

      <!-- Medical Records -->
      <template v-if="activeTab === 'medical'">
        <div v-if="medicalRecords.length" class="record-list">
          <div v-for="r in medicalRecords" :key="r.id" class="record-card record-card--medical">
            <div class="record-main">
              <div class="record-title">🩺 {{ r.doctorName || '未知医生' }}</div>
              <div class="record-sub">诊断：{{ r.diagnosis || '-' }}</div>
              <div class="record-time">{{ r.visitTime || '-' }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon">🏥</div>
          <div class="empty-text">暂无相关记录</div>
        </div>
      </template>

      <!-- Health Records -->
      <template v-if="activeTab === 'health'">
        <div v-if="healthRecords.length" class="record-list">
          <div v-for="r in healthRecords" :key="r.id" class="record-card record-card--health">
            <div class="record-main">
              <div class="record-title">
                健康评分：
                <span class="health-score" :class="healthScoreClass(r.healthScore)">
                  {{ r.healthScore != null ? r.healthScore : '-' }}
                </span>
              </div>
              <div class="record-sub">
                体检日期：{{ r.inspectionDate || '-' }}
                <span v-if="r.riskLevel != null" class="tag" :class="riskTagClass(r.riskLevel)" style="margin-left: 8px;">
                  {{ riskText(r.riskLevel) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon">💊</div>
          <div class="empty-text">暂无相关记录</div>
        </div>
      </template>

      <!-- Certificates -->
      <template v-if="activeTab === 'certificate'">
        <div v-if="certificates.length" class="record-list">
          <div v-for="c in certificates" :key="c.id" class="record-card record-card--cert">
            <div class="record-main">
              <div class="record-title">🎓 {{ c.courseName || '未知课程' }}</div>
              <div class="record-sub">证书编号：{{ c.certificateNo || '-' }} · 毕业日期：{{ c.graduateDate || '-' }}</div>
            </div>
            <span :class="['tag', certStatusClass(c.status)]">{{ certStatusText(c.status) }}</span>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon">🎓</div>
          <div class="empty-text">暂无相关记录</div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { petApi, courseApi, orderApi, roomApi, healthApi, medicalApi, certificateApi } from '../../api'

const router = useRouter()
const route = useRoute()

const pet = ref({})
const courseOrders = ref([])
const boardingOrders = ref([])
const healthRecords = ref([])
const medicalRecords = ref([])
const certificates = ref([])
const activeTab = ref('course')

const tabs = [
  { key: 'course', icon: '📚', label: '课程记录' },
  { key: 'boarding', icon: '🏠', label: '寄宿记录' },
  { key: 'medical', icon: '🏥', label: '医疗记录' },
  { key: 'health', icon: '💊', label: '健康记录' },
  { key: 'certificate', icon: '🎓', label: '毕业证书' }
]

const petTypeEmoji = computed(() => {
  const type = pet.value.petType
  if (type === 1 || type === '狗') return '🐕'
  if (type === 2 || type === '猫') return '🐈'
  if (type === 3 || type === '鸟') return '🐦'
  if (type === 4 || type === '兔') return '🐇'
  return '🐾'
})

function goEdit() {
  router.push(`/client/pet/edit/${route.params.id}`)
}

function formatTime(t) {
  if (!t) return ''
  if (typeof t === 'string') return t.replace('T', ' ').slice(0, 19)
  return ''
}

function orderStatusText(s) {
  return s === 0 ? '待支付' : s === 1 ? '已支付' : s === 2 ? '已取消' : '未知'
}

function orderStatusClass(s) {
  return s === 0 ? 'tag-warning' : s === 1 ? 'tag-success' : 'tag-danger'
}

function healthScoreClass(score) {
  if (score == null) return ''
  if (score >= 80) return 'score-green'
  if (score >= 60) return 'score-yellow'
  return 'score-red'
}

function riskText(level) {
  const map = { 0: '健康', 1: '注意', 2: '风险', 3: '高风险' }
  return map[level] ?? '未知'
}

function riskTagClass(level) {
  const map = { 0: 'tag-success', 1: 'tag-warning', 2: 'tag-accent', 3: 'tag-danger' }
  return map[level] ?? 'tag-info'
}

function certStatusText(s) {
  return s === 1 ? '有效' : s === 0 ? '无效' : '未知'
}

function certStatusClass(s) {
  return s === 1 ? 'tag-success' : 'tag-danger'
}

async function loadData() {
  const id = route.params.id
  if (!id) return

  try {
    const petRes = await petApi.detail(id)
    if (petRes.code === 200 && petRes.data) {
      pet.value = petRes.data
    }

    const userId = pet.value.userId
    if (!userId) return

    const [courseRes, boardingRes, healthRes, medicalRes, certRes] = await Promise.all([
      orderApi.courseByUser(userId).catch(() => null),
      roomApi.boardingByUser(userId).catch(() => null),
      healthApi.recordsByPet(id).catch(() => null),
      medicalApi.recordsByPet(id).catch(() => null),
      certificateApi.getByPet(id).catch(() => null)
    ])

    if (courseRes?.code === 200 && courseRes.data) {
      courseOrders.value = courseRes.data.filter(o => o.petId == id)
    }
    if (boardingRes?.code === 200 && boardingRes.data) {
      boardingOrders.value = boardingRes.data.filter(o => o.petId == id)
    }
    if (healthRes?.code === 200 && healthRes.data) {
      healthRecords.value = healthRes.data
    }
    if (medicalRes?.code === 200 && medicalRes.data) {
      medicalRecords.value = medicalRes.data
    }
    if (certRes?.code === 200 && certRes.data) {
      certificates.value = certRes.data
    }
  } catch (e) {
    console.error('加载宠物详情失败', e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.pet-detail-page {
  animation: fadeInUp var(--transition-base) ease both;
}

/* Profile Card */
.profile-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: var(--space-4);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}

.profile-header {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  padding: var(--space-5) var(--space-5) var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  position: relative;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  flex-shrink: 0;
  border: 3px solid rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
}

.profile-identity {
  flex: 1;
  min-width: 0;
}

.profile-name {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-inverse);
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.profile-breed {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.8);
  font-weight: var(--font-weight-medium);
}

.edit-btn {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  background: rgba(255, 255, 255, 0.2);
  color: var(--text-inverse);
  border: 1.5px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
}

.edit-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.6);
}

.profile-body {
  padding: var(--space-4) var(--space-5);
}

.profile-meta {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  margin-bottom: var(--space-3);
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.meta-icon {
  font-size: var(--font-size-md);
}

.meta-label {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  font-weight: var(--font-weight-medium);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.02em;
}

.badge-active {
  background: var(--color-success-bg);
  color: var(--color-success-text);
}

.badge-inactive {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
}

.profile-pills {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 14px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  background: var(--bg-hover);
  color: var(--text-body);
  border: 1px solid var(--border-light);
}

.pill-success {
  background: var(--color-success-bg);
  color: var(--color-success-text);
  border-color: transparent;
}

.pill-info {
  background: var(--color-info-bg);
  color: var(--color-primary-dark);
  border-color: transparent;
}

.pill-accent {
  background: var(--color-accent-bg);
  color: var(--color-warning-text);
  border-color: transparent;
}

.pill-default {
  background: var(--bg-hover);
  color: var(--text-body);
}

/* Tab Bar */
.tab-bar {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  padding: var(--space-2);
  background: var(--bg-card);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  overflow-x: auto;
}

.tab-pill {
  padding: 8px 20px;
  border-radius: var(--radius-full);
  border: 1.5px solid transparent;
  background: transparent;
  color: var(--text-body);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family);
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.tab-pill:hover {
  background: var(--bg-hover);
  color: var(--color-primary);
}

.tab-pill--active {
  background: var(--color-primary);
  color: var(--text-inverse);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-button);
}

.tab-pill--active:hover {
  background: var(--color-primary-dark);
  color: var(--text-inverse);
}

/* Tab Content */
.tab-content {
  min-height: 200px;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.record-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  transition: all var(--transition-base);
  border-left: 4px solid var(--border-light);
  animation: fadeInUp var(--transition-base) ease both;
}

.record-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.record-card--course {
  border-left-color: var(--color-primary);
}

.record-card--boarding {
  border-left-color: var(--color-accent);
}

.record-card--medical {
  border-left-color: var(--color-danger);
}

.record-card--health {
  border-left-color: var(--color-success);
}

.record-card--cert {
  border-left-color: var(--color-warning);
}

.record-main {
  flex: 1;
  min-width: 0;
}

.record-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin-bottom: var(--space-1);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.record-sub {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin-bottom: var(--space-1);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.record-time {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

/* Health Score Colors */
.health-score {
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-lg);
}

.score-green {
  color: var(--color-success);
}

.score-yellow {
  color: var(--color-warning);
}

.score-red {
  color: var(--color-danger);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: var(--space-7) var(--space-4);
  color: var(--text-muted);
}

.empty-state .empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.6;
}

.empty-state .empty-text {
  font-size: var(--font-size-md);
  color: var(--text-muted);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .profile-header {
    flex-wrap: wrap;
  }

  .edit-btn {
    position: static;
  }

  .profile-meta {
    gap: var(--space-3);
  }

  .tab-bar {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .record-card {
    flex-direction: column;
    gap: var(--space-2);
  }
}
</style>
