<template>
  <div class="pet-detail-page">
    <!-- Back Button -->
    <button class="btn btn-ghost back-btn" @click="router.push('/admin/pet')">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>
      返回宠物列表
    </button>

    <!-- Pet Profile Card -->
    <div class="profile-card" v-if="pet.id">
      <div class="profile-header">
        <div class="profile-avatar">{{ petTypeEmoji }}</div>
        <div class="profile-identity">
          <h1 class="profile-name">{{ pet.name || '-' }}</h1>
          <span class="profile-breed">{{ petTypeEmoji }} {{ pet.breed || '-' }}</span>
        </div>
        <div class="profile-owner" v-if="pet.username">
          <span class="owner-label">主人</span>
          <span class="owner-name">{{ pet.username }}</span>
        </div>
      </div>
      <div class="profile-body">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">性别</span>
            <span class="info-value">{{ pet.gender === 1 ? '♂ 公' : pet.gender === 2 ? '♀ 母' : '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">年龄</span>
            <span class="info-value">{{ pet.age != null ? pet.age + '岁' : '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">体重</span>
            <span class="info-value">{{ pet.weight != null ? pet.weight + 'kg' : '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">毛色</span>
            <span class="info-value">{{ pet.color || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">绝育状态</span>
            <span :class="['info-value', pet.sterilized ? 'text-success' : 'text-muted']">{{ pet.sterilized ? '已绝育' : '未绝育' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">血型</span>
            <span class="info-value">{{ pet.bloodType || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">芯片号</span>
            <span class="info-value">{{ pet.chipNumber || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态</span>
            <span :class="['status-badge', pet.status === 1 ? 'badge-active' : 'badge-inactive']">
              {{ pet.status === 1 ? '活跃' : '离场' }}
            </span>
          </div>
        </div>
        <div class="info-section" v-if="pet.allergy">
          <span class="section-label">⚠️ 过敏信息</span>
          <span class="section-content">{{ pet.allergy }}</span>
        </div>
        <div class="info-section" v-if="pet.remark">
          <span class="section-label">📝 备注</span>
          <span class="section-content">{{ pet.remark }}</span>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div class="profile-card loading-card" v-else>
      <div class="loading-text">加载中...</div>
    </div>

    <!-- Business Data Section -->
    <div class="section-heading" v-if="pet.id">
      <h2 class="section-title">业务数据</h2>
    </div>

    <div class="summary-row" v-if="pet.id">
      <div class="summary-card summary-card--course">
        <span class="summary-icon">📚</span>
        <div class="summary-body">
          <span class="summary-count">{{ courseOrders.length }}</span>
          <span class="summary-label">课程订单</span>
        </div>
      </div>
      <div class="summary-card summary-card--boarding">
        <span class="summary-icon">🏠</span>
        <div class="summary-body">
          <span class="summary-count">{{ boardingOrders.length }}</span>
          <span class="summary-label">寄宿订单</span>
        </div>
      </div>
      <div class="summary-card summary-card--medical">
        <span class="summary-icon">🏥</span>
        <div class="summary-body">
          <span class="summary-count">{{ medicalOrders.length }}</span>
          <span class="summary-label">医疗订单</span>
        </div>
      </div>
      <div class="summary-card summary-card--health">
        <span class="summary-icon">💊</span>
        <div class="summary-body">
          <span class="summary-count">{{ healthRecords.length }}</span>
          <span class="summary-label">健康记录</span>
        </div>
      </div>
      <div class="summary-card summary-card--cert">
        <span class="summary-icon">🎓</span>
        <div class="summary-body">
          <span class="summary-count">{{ certificates.length }}</span>
          <span class="summary-label">毕业证书</span>
        </div>
      </div>
    </div>

    <!-- Tab Section -->
    <div class="tab-bar" v-if="pet.id">
      <button
        v-for="t in tabs"
        :key="t.key"
        :class="['tab-pill', { 'tab-pill--active': activeTab === t.key }]"
        @click="activeTab = t.key"
      >{{ t.icon }} {{ t.label }}</button>
    </div>

    <div class="tab-content" v-if="pet.id">
      <!-- Course Orders -->
      <template v-if="activeTab === 'course'">
        <div v-if="courseOrders.length" class="record-list">
          <div v-for="o in courseOrders" :key="o.id" class="record-card record-card--course">
            <div class="record-main">
              <div class="record-title">{{ o.orderNo || '-' }}</div>
              <div class="record-sub">课程：{{ o.courseName || '-' }}</div>
              <div class="record-meta">
                <span class="meta-price">¥{{ o.totalPrice != null ? o.totalPrice : '-' }}</span>
                <span class="meta-time">{{ formatTime(o.createTime) }}</span>
              </div>
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
              <div class="record-title">{{ o.orderNo || '-' }}</div>
              <div class="record-sub">房型：{{ o.roomTypeName || '-' }} · {{ o.checkIn || '-' }} ~ {{ o.checkOut || '-' }}</div>
              <div class="record-meta">
                <span class="meta-price">¥{{ o.totalPrice != null ? o.totalPrice : '-' }}</span>
              </div>
            </div>
            <span :class="['tag', orderStatusClass(o.status)]">{{ orderStatusText(o.status) }}</span>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon">🏠</div>
          <div class="empty-text">暂无相关记录</div>
        </div>
      </template>

      <!-- Medical Orders -->
      <template v-if="activeTab === 'medical'">
        <div v-if="medicalOrders.length" class="record-list">
          <div v-for="o in medicalOrders" :key="o.id" class="record-card record-card--medical">
            <div class="record-main">
              <div class="record-title">{{ o.orderNo || '-' }}</div>
              <div class="record-sub">科室：{{ o.departmentName || '-' }} · 医生：{{ o.doctorName || '-' }}</div>
              <div class="record-sub">症状：{{ o.symptom || '-' }}</div>
              <div class="record-meta">
                <span class="meta-price">¥{{ o.price != null ? o.price : '-' }}</span>
              </div>
            </div>
            <span :class="['tag', orderStatusClass(o.status)]">{{ orderStatusText(o.status) }}</span>
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
              <div class="record-sub">体温：{{ r.temperature != null ? r.temperature + '°C' : '-' }} · 体重：{{ r.weight != null ? r.weight + 'kg' : '-' }}</div>
              <div class="record-meta">
                <span class="meta-time">体检日期：{{ r.inspectionDate || '-' }}</span>
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
              <div class="record-title">{{ c.certificateNo || '-' }}</div>
              <div class="record-sub">课程：{{ c.courseName || '-' }}</div>
              <div class="record-meta">
                <span class="meta-time">毕业日期：{{ c.graduateDate || '-' }}</span>
              </div>
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
import { adminApi } from '../../api'

const router = useRouter()
const route = useRoute()

const pet = ref({})
const courseOrders = ref([])
const boardingOrders = ref([])
const medicalOrders = ref([])
const healthRecords = ref([])
const certificates = ref([])
const activeTab = ref('course')

const tabs = [
  { key: 'course', icon: '📚', label: '课程订单' },
  { key: 'boarding', icon: '🏠', label: '寄宿订单' },
  { key: 'medical', icon: '🏥', label: '医疗订单' },
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
    const petRes = await adminApi.petDetail(id)
    if (petRes.code === 200 && petRes.data) {
      pet.value = petRes.data
    }

    const [courseRes, boardingRes, medicalRes, healthRes, certRes] = await Promise.all([
      adminApi.courseOrderPage({ page: 1, pageSize: 100 }).catch(() => null),
      adminApi.boardingOrderPage({ page: 1, pageSize: 100 }).catch(() => null),
      adminApi.medicalOrderPage({ page: 1, pageSize: 100 }).catch(() => null),
      adminApi.healthByPet(id).catch(() => null),
      adminApi.certificatePage({ page: 1, pageSize: 100 }).catch(() => null)
    ])

    if (courseRes?.code === 200 && courseRes.data?.list) {
      courseOrders.value = courseRes.data.list.filter(o => o.petId == id)
    }
    if (boardingRes?.code === 200 && boardingRes.data?.list) {
      boardingOrders.value = boardingRes.data.list.filter(o => o.petId == id)
    }
    if (medicalRes?.code === 200 && medicalRes.data?.list) {
      medicalOrders.value = medicalRes.data.list.filter(o => o.petId == id)
    }
    if (healthRes?.code === 200 && healthRes.data) {
      healthRecords.value = Array.isArray(healthRes.data) ? healthRes.data : (healthRes.data.list || [])
    }
    if (certRes?.code === 200 && certRes.data?.list) {
      certificates.value = certRes.data.list.filter(c => c.petId == id)
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

/* Back Button */
.back-btn {
  margin-bottom: var(--space-4);
  gap: var(--space-1);
  color: var(--text-body);
  font-weight: var(--font-weight-medium);
}

.back-btn:hover {
  color: var(--color-primary);
}

/* Profile Card */
.profile-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: var(--space-5);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}

.profile-header {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  padding: var(--space-5) var(--space-5) var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-3xl);
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

.profile-owner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--space-2) var(--space-4);
  background: rgba(255, 255, 255, 0.18);
  border-radius: var(--radius-md);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.owner-label {
  font-size: var(--font-size-xs);
  color: rgba(255, 255, 255, 0.7);
  font-weight: var(--font-weight-medium);
}

.owner-name {
  font-size: var(--font-size-md);
  color: var(--text-inverse);
  font-weight: var(--font-weight-semibold);
}

.profile-body {
  padding: var(--space-4) var(--space-5);
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-3) var(--space-4);
  margin-bottom: var(--space-4);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-weight: var(--font-weight-medium);
}

.info-value {
  font-size: var(--font-size-base);
  color: var(--text-title);
  font-weight: var(--font-weight-semibold);
}

.text-success {
  color: var(--color-success);
}

.text-muted {
  color: var(--text-muted);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  width: fit-content;
}

.badge-active {
  background: var(--color-success-bg);
  color: var(--color-success-text);
}

.badge-inactive {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
}

/* Info Sections */
.info-section {
  padding: var(--space-3);
  background: var(--bg-input);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
  display: flex;
  gap: var(--space-2);
  align-items: flex-start;
}

.section-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  white-space: nowrap;
}

.section-content {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  line-height: 1.5;
}

/* Loading */
.loading-card {
  padding: var(--space-7);
  text-align: center;
}

.loading-text {
  color: var(--text-muted);
  font-size: var(--font-size-md);
}

/* Section Heading */
.section-heading {
  margin-bottom: var(--space-4);
}

.section-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0;
  letter-spacing: -0.01em;
}

/* Summary Row */
.summary-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.summary-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  border-left: 4px solid var(--border-light);
  transition: all var(--transition-base);
}

.summary-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.summary-card--course {
  border-left-color: var(--color-primary);
}

.summary-card--boarding {
  border-left-color: var(--color-accent);
}

.summary-card--medical {
  border-left-color: var(--color-danger);
}

.summary-card--health {
  border-left-color: var(--color-success);
}

.summary-card--cert {
  border-left-color: var(--color-warning);
}

.summary-icon {
  font-size: var(--font-size-xl);
  flex-shrink: 0;
}

.summary-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.summary-count {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.summary-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-weight: var(--font-weight-medium);
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
  margin-bottom: 4px;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-1);
}

.meta-price {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.meta-time {
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
  font-size: var(--font-size-3xl);
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

@media (max-width: 1024px) {
  .summary-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .summary-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .profile-header {
    flex-wrap: wrap;
  }

  .profile-owner {
    width: 100%;
    flex-direction: row;
    justify-content: center;
    gap: var(--space-2);
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
