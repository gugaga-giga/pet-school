<template>
  <div class="vaccine-page">
    <div class="vaccine-header">
      <h1 class="page-title">疫苗与驱虫提醒</h1>
      <p class="page-subtitle">及时接种，守护爱宠健康</p>
    </div>

    <div class="tab-bar">
      <button
        :class="['tab-pill', mainTab === 'vaccine' ? 'tab-pill--active' : '']"
        @click="mainTab = 'vaccine'"
      >
        💉 疫苗提醒
      </button>
      <button
        :class="['tab-pill', mainTab === 'deworming' ? 'tab-pill--active' : '']"
        @click="mainTab = 'deworming'"
      >
        🐛 驱虫提醒
      </button>
    </div>

    <div class="remind-sections">
      <div class="remind-section">
        <div class="section-header section-header--warning">
          <span class="section-icon">⚠️</span>
          <h3 class="section-title">即将到期</h3>
          <span class="section-badge badge-warning">{{ upcomingList.length }}</span>
        </div>
        <div v-if="upcomingList.length" class="remind-cards">
          <div v-for="item in upcomingList" :key="item.id" class="remind-card remind-card--warning">
            <div class="remind-card__top">
              <span class="remind-name">{{ item.name || item.vaccineName || item.dewormingName }}</span>
              <span class="remind-days">剩余 {{ item.daysLeft ?? item.remainingDays }} 天</span>
            </div>
            <div class="remind-card__info">
              <span class="remind-pet">🐾 {{ item.petName || item.pet?.name || '-' }}</span>
              <span class="remind-date">到期：{{ formatDate(item.nextDueDate) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="remind-empty">暂无即将到期的项目</div>
      </div>

      <div class="remind-section">
        <div class="section-header section-header--danger">
          <span class="section-icon">🔴</span>
          <h3 class="section-title">已过期</h3>
          <span class="section-badge badge-danger">{{ expiredList.length }}</span>
        </div>
        <div v-if="expiredList.length" class="remind-cards">
          <div v-for="item in expiredList" :key="item.id" class="remind-card remind-card--danger">
            <div class="remind-card__top">
              <span class="remind-name">{{ item.name || item.vaccineName || item.dewormingName }}</span>
              <span class="remind-expired-tag">已过期</span>
            </div>
            <div class="remind-card__info">
              <span class="remind-pet">🐾 {{ item.petName || item.pet?.name || '-' }}</span>
              <span class="remind-date">过期日期：{{ formatDate(item.nextDueDate) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="remind-empty">暂无过期项目</div>
      </div>

      <div class="remind-section">
        <div class="section-header section-header--success">
          <span class="section-icon">✅</span>
          <h3 class="section-title">正常</h3>
          <span class="section-badge badge-success">{{ normalList.length }}</span>
        </div>
        <div v-if="normalList.length" class="remind-cards">
          <div v-for="item in normalList" :key="item.id" class="remind-card remind-card--success">
            <div class="remind-card__top">
              <span class="remind-name">{{ item.name || item.vaccineName || item.dewormingName }}</span>
            </div>
            <div class="remind-card__info">
              <span class="remind-pet">🐾 {{ item.petName || item.pet?.name || '-' }}</span>
              <span class="remind-date">下次日期：{{ formatDate(item.nextDueDate) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="remind-empty">暂无正常项目</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { medicalApi } from '../../api'

const user = computed(() => JSON.parse(localStorage.getItem('client_user') || 'null'))

const mainTab = ref('vaccine')
const vaccineUpcoming = ref([])
const vaccineExpired = ref([])
const dewormingUpcoming = ref([])
const dewormingExpired = ref([])

const upcomingList = computed(() => {
  return mainTab.value === 'vaccine' ? vaccineUpcoming.value : dewormingUpcoming.value
})

const expiredList = computed(() => {
  return mainTab.value === 'vaccine' ? vaccineExpired.value : dewormingExpired.value
})

const normalList = computed(() => {
  // TODO: 后端暂未提供"正常"状态接口，暂时返回空列表
  return []
})

function formatDate(t) {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 10)
}

async function loadVaccineData() {
  if (!user.value?.id) return
  try {
    const [upRes, exRes] = await Promise.all([
      medicalApi.vaccineUpcoming(user.value.id, 7),
      medicalApi.vaccineExpired(user.value.id)
    ])
    vaccineUpcoming.value = upRes.data || upRes || []
    vaccineExpired.value = exRes.data || exRes || []
  } catch (e) {
    console.error(e)
  }
}

async function loadDewormingData() {
  if (!user.value?.id) return
  try {
    const [upRes, exRes] = await Promise.all([
      medicalApi.dewormingUpcoming(user.value.id, 3),
      medicalApi.dewormingExpired(user.value.id)
    ])
    dewormingUpcoming.value = upRes.data || upRes || []
    dewormingExpired.value = exRes.data || exRes || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadVaccineData()
  loadDewormingData()
})
</script>

<style scoped>
.vaccine-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: var(--space-6) var(--space-8);
}

.vaccine-header {
  margin-bottom: var(--space-6);
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: var(--space-1) 0 0 0;
}

.tab-bar {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
  padding: var(--space-2);
  background: var(--bg-card);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  width: fit-content;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 8px 20px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family);
  cursor: pointer;
  border: 1.5px solid transparent;
  background: transparent;
  color: var(--text-body);
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

.remind-sections {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.remind-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.section-header--warning {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.06), rgba(245, 158, 11, 0.02));
}

.section-header--danger {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.06), rgba(239, 68, 68, 0.02));
}

.section-header--success {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.06), rgba(34, 197, 94, 0.02));
}

.section-icon {
  font-size: var(--font-size-md);
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0;
  flex: 1;
}

.section-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  line-height: 1;
}

.badge-warning {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
}

.badge-danger {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
}

.badge-success {
  background: var(--color-success-bg);
  color: var(--color-success-text);
}

.remind-cards {
  padding: var(--space-4) var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.remind-card {
  padding: var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  transition: all var(--transition-base);
}

.remind-card:hover {
  transform: translateX(4px);
}

.remind-card--warning {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.08), rgba(245, 158, 11, 0.03));
  border-color: rgba(245, 158, 11, 0.25);
}

.remind-card--danger {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.08), rgba(239, 68, 68, 0.03));
  border-color: rgba(239, 68, 68, 0.25);
}

.remind-card--success {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.08), rgba(34, 197, 94, 0.03));
  border-color: rgba(34, 197, 94, 0.25);
}

.remind-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.remind-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.remind-days {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-warning-text);
  background: var(--color-warning-bg);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}

.remind-expired-tag {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-danger-text);
  background: var(--color-danger-bg);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}

.remind-card__info {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.remind-pet {
  font-size: var(--font-size-sm);
  color: var(--text-body);
}

.remind-date {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.remind-empty {
  padding: var(--space-5);
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

@media (max-width: 560px) {
  .remind-card__info {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-1);
  }
}
</style>
