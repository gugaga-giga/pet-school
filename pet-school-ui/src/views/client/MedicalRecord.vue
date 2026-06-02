<template>
  <div class="record-page">
    <div class="record-header">
      <h1 class="page-title">我的病历</h1>
      <p class="page-subtitle">查看所有宠物的就诊记录与诊断详情</p>
      <div class="sub-nav">
        <router-link to="/client/medical" class="sub-nav-item">
          <span class="sub-nav-icon">🩺</span>预约挂号
        </router-link>
        <router-link to="/client/medical-order" class="sub-nav-item">
          <span class="sub-nav-icon">📋</span>我的预约
        </router-link>
        <router-link to="/client/medical-record" class="sub-nav-item active">
          <span class="sub-nav-icon">📄</span>我的病历
        </router-link>
      </div>
    </div>

    <div v-if="myPets.length > 1" class="pet-filter">
      <button
        :class="['pet-pill', !selPetId ? 'pet-pill--active' : '']"
        @click="selPetId = null"
      >
        全部宠物
      </button>
      <button
        v-for="pet in myPets"
        :key="pet.id"
        :class="['pet-pill', selPetId === pet.id ? 'pet-pill--active' : '']"
        @click="selPetId = pet.id"
      >
        🐾 {{ pet.name }}
      </button>
    </div>

    <div v-if="filteredRecords.length" class="timeline">
      <div
        v-for="(record, idx) in filteredRecords"
        :key="record.id"
        class="timeline-item"
      >
        <div class="timeline-dot"></div>
        <div v-if="idx < filteredRecords.length - 1" class="timeline-line"></div>
        <div class="timeline-card">
          <div class="timeline-card__header">
            <div class="timeline-date">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              {{ formatDate(record.visitTime || record.createTime) }}
            </div>
            <div class="timeline-meta">
              <span class="meta-doctor">{{ record.doctorName || record.doctor?.name || '-' }}</span>
              <span class="meta-dept">{{ record.departmentName || record.department?.name || '-' }}</span>
            </div>
          </div>

          <div class="timeline-card__pet" v-if="record.petName || record.pet?.name">
            🐾 {{ record.petName || record.pet?.name }}
          </div>

          <div class="timeline-card__body">
            <div v-if="record.chiefComplaint" class="record-section">
              <h5 class="section-title">主诉</h5>
              <p class="section-content">{{ record.chiefComplaint }}</p>
            </div>

            <div v-if="record.physicalExam" class="record-section">
              <h5 class="section-title">体格检查</h5>
              <p class="section-content">{{ record.physicalExam }}</p>
            </div>

            <div v-if="record.diagnosis" class="record-section">
              <h5 class="section-title">诊断</h5>
              <p class="section-content diagnosis-text">{{ record.diagnosis }}</p>
            </div>

            <div v-if="record.medicalAdvice" class="record-section">
              <h5 class="section-title">医嘱</h5>
              <p class="section-content">{{ record.medicalAdvice }}</p>
            </div>

            <div v-if="record.medication" class="record-section">
              <h5 class="section-title">药品记录</h5>
              <p class="section-content">{{ record.medication }}</p>
            </div>

            <div v-if="record.notes || record.remark" class="record-section">
              <h5 class="section-title">备注</h5>
              <p class="section-content">{{ record.notes || record.remark }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">📋</div>
      <h3 class="empty-title">暂无病历记录</h3>
      <p class="empty-desc">您的宠物还没有就诊记录</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { medicalApi, petApi } from '../../api'

const user = computed(() => JSON.parse(localStorage.getItem('client_user') || 'null'))

const myPets = ref([])
const selPetId = ref(null)
const allRecords = ref([])

const filteredRecords = computed(() => {
  if (!selPetId.value) return allRecords.value
  return allRecords.value.filter(r => r.petId === selPetId.value)
})

function formatDate(t) {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 10)
}

async function loadPets() {
  if (!user.value?.id) return
  try {
    const res = await petApi.getByUserId(user.value.id)
    myPets.value = res.data || res || []
  } catch (e) {
    console.error(e)
  }
}

async function loadRecords() {
  allRecords.value = []
  for (const pet of myPets.value) {
    try {
      const res = await medicalApi.recordsByPet(pet.id)
      const list = res.data || res || []
      allRecords.value.push(...list.map(r => ({ ...r, petId: pet.id, petName: pet.name })))
    } catch (e) {
      console.error(e)
    }
  }
  allRecords.value.sort((a, b) => {
    const da = new Date(a.visitTime || a.createTime || 0)
    const db = new Date(b.visitTime || b.createTime || 0)
    return db - da
  })
}

onMounted(async () => {
  await loadPets()
  await loadRecords()
})
</script>

<style scoped>
.record-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: var(--space-6) var(--space-8);
}

.record-header {
  margin-bottom: var(--space-6);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: var(--space-1) 0 0 0;
}

.sub-nav {
  display: inline-flex;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-5);
  padding: var(--space-2);
  background: var(--bg-card);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.sub-nav-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 8px 20px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-body);
  text-decoration: none;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.sub-nav-item:hover {
  background: var(--bg-hover);
  color: var(--color-primary);
}

.sub-nav-item.active,
.sub-nav-item.router-link-active {
  background: var(--color-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-button);
}

.sub-nav-icon {
  font-size: 14px;
}

.pet-filter {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-6);
}

.pet-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family);
  cursor: pointer;
  border: 1.5px solid var(--border-color);
  background: var(--bg-card);
  color: var(--text-body);
  transition: all var(--transition-base);
  white-space: nowrap;
}

.pet-pill:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.pet-pill--active {
  background: var(--color-primary);
  color: var(--text-inverse);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-button);
}

.pet-pill--active:hover {
  background: var(--color-primary-dark);
  color: var(--text-inverse);
}

.timeline {
  position: relative;
  padding-left: var(--space-7);
}

.timeline-item {
  position: relative;
  padding-bottom: var(--space-6);
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: calc(var(--space-7) * -1 + 6px);
  top: 8px;
  width: 14px;
  height: 14px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  border: 3px solid var(--color-primary-bg);
  box-shadow: 0 0 0 2px var(--color-primary);
  z-index: 1;
}

.timeline-line {
  position: absolute;
  left: calc(var(--space-7) * -1 + 12px);
  top: 24px;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, var(--color-primary-bg), var(--border-light));
}

.timeline-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  overflow: hidden;
  transition: all var(--transition-base);
}

.timeline-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateX(4px);
}

.timeline-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, rgba(79, 124, 255, 0.03), rgba(255, 184, 107, 0.03));
  border-bottom: 1px solid var(--border-light);
}

.timeline-date {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.timeline-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.meta-doctor {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.meta-dept {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  background: var(--bg-hover);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}

.timeline-card__pet {
  padding: var(--space-2) var(--space-5);
  font-size: var(--font-size-sm);
  color: var(--text-body);
  background: var(--bg-input);
  border-bottom: 1px solid var(--border-light);
}

.timeline-card__body {
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.record-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.section-title {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0;
}

.section-content {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  line-height: 1.7;
  margin: 0;
  padding: var(--space-2) var(--space-3);
  background: var(--bg-input);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--color-primary-bg);
}

.diagnosis-text {
  color: var(--color-primary-dark);
  font-weight: var(--font-weight-medium);
  background: var(--color-primary-bg);
  border-left-color: var(--color-primary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.empty-title {
  font-size: var(--font-size-xl);
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
}

.empty-desc {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
}

@media (max-width: 560px) {
  .timeline-card__header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }
}
</style>
