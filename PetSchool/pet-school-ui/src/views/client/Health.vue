<template>
  <div class="health-page">
    <div class="page-header">
      <h1 class="page-title">🩺 我的宠物健康</h1>
    </div>

    <div v-if="!myPets.length" class="card empty-card">
      <div class="empty-state">
        <div class="empty-icon">🐾</div>
        <div class="empty-text">您还没有宠物，请先添加宠物</div>
      </div>
    </div>

    <template v-else>
      <div class="pet-selector">
        <div
          v-for="pet in myPets"
          :key="pet.id"
          :class="['pet-tab', { active: selectedPetId === pet.id }]"
          @click="selectPet(pet.id)"
        >
          <span class="pet-emoji">🐾</span>
          <span class="pet-name">{{ pet.name }}</span>
        </div>
      </div>

      <div v-if="!latestRecord" class="card empty-card">
        <div class="empty-state">
          <div class="empty-icon">🩺</div>
          <div class="empty-text">暂无体检数据</div>
        </div>
      </div>

      <template v-else>
        <div class="score-section card">
          <div class="score-ring-wrap">
            <div class="score-ring" :class="scoreColorClass">
              <svg class="score-ring-svg" viewBox="0 0 160 160">
                <circle class="score-ring-bg" cx="80" cy="80" r="70" />
                <circle
                  class="score-ring-fill"
                  cx="80" cy="80" r="70"
                  :stroke="scoreColor"
                  :stroke-dasharray="scoreDashArray"
                  stroke-dashoffset="0"
                />
              </svg>
              <div class="score-ring-inner">
                <span class="score-number" :style="{ color: scoreColor }">{{ latestRecord.healthScore ?? '-' }}</span>
                <span class="score-label">{{ scoreText }}</span>
              </div>
            </div>
          </div>
          <div class="score-meta">
            <h2 class="pet-health-name">{{ currentPetName }}</h2>
            <span class="tag" :class="riskTagClass(latestRecord.riskLevel)">{{ riskText(latestRecord.riskLevel) }}</span>
            <p class="score-date">最近体检：{{ latestRecord.inspectionDate || '-' }}</p>
          </div>
        </div>

        <div class="indicators-grid">
          <div class="indicator-card card">
            <div class="indicator-header">
              <span class="indicator-label">体温</span>
              <span class="indicator-dot" :class="tempStatus"></span>
            </div>
            <span class="indicator-value">{{ latestRecord.temperature != null ? latestRecord.temperature + '℃' : '-' }}</span>
          </div>
          <div class="indicator-card card">
            <div class="indicator-header">
              <span class="indicator-label">体重</span>
              <span class="indicator-dot" :class="weightStatus"></span>
            </div>
            <span class="indicator-value">{{ latestRecord.weight != null ? latestRecord.weight + 'kg' : '-' }}</span>
          </div>
          <div class="indicator-card card">
            <div class="indicator-header">
              <span class="indicator-label">心率</span>
              <span class="indicator-dot" :class="heartStatus"></span>
            </div>
            <span class="indicator-value">{{ latestRecord.heartRate != null ? latestRecord.heartRate + 'bpm' : '-' }}</span>
          </div>
          <div class="indicator-card card">
            <div class="indicator-header">
              <span class="indicator-label">呼吸</span>
              <span class="indicator-dot" :class="respStatus"></span>
            </div>
            <span class="indicator-value">{{ latestRecord.respirationRate != null ? latestRecord.respirationRate + '次/分' : '-' }}</span>
          </div>
          <div class="indicator-card card indicator-small">
            <div class="indicator-header">
              <span class="indicator-label">食欲</span>
              <span class="indicator-dot" :class="conditionStatus(latestRecord.appetite)"></span>
            </div>
            <span class="indicator-value indicator-value-sm">{{ statusText(latestRecord.appetite) }}</span>
          </div>
          <div class="indicator-card card indicator-small">
            <div class="indicator-header">
              <span class="indicator-label">精神</span>
              <span class="indicator-dot" :class="conditionStatus(latestRecord.mentalStatus)"></span>
            </div>
            <span class="indicator-value indicator-value-sm">{{ statusText(latestRecord.mentalStatus) }}</span>
          </div>
          <div class="indicator-card card indicator-small">
            <div class="indicator-header">
              <span class="indicator-label">毛发</span>
              <span class="indicator-dot" :class="conditionStatus(latestRecord.hairCondition)"></span>
            </div>
            <span class="indicator-value indicator-value-sm">{{ statusText(latestRecord.hairCondition) }}</span>
          </div>
          <div class="indicator-card card indicator-small">
            <div class="indicator-header">
              <span class="indicator-label">粪便</span>
              <span class="indicator-dot" :class="conditionStatus(latestRecord.fecesStatus)"></span>
            </div>
            <span class="indicator-value indicator-value-sm">{{ statusText(latestRecord.fecesStatus) }}</span>
          </div>
        </div>

        <div v-if="latestRecord.riskLevel > 0" class="risk-alerts card">
          <h3 class="section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            风险提醒
          </h3>
          <div class="alert" :class="latestRecord.riskLevel >= 3 ? 'alert-error' : 'alert-warning'">
            {{ latestRecord.riskLevel >= 3 ? '您的宠物健康状态为高风险，请尽快带宠物就医！' : '您的宠物健康状态需要关注，建议及时检查。' }}
          </div>
        </div>

        <div v-if="latestRecord.aiAdvice" class="ai-advice card">
          <h3 class="section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M12 2a7 7 0 0 1 7 7c0 2.38-1.19 4.47-3 5.74V17a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1v-2.26C6.19 13.47 5 11.38 5 9a7 7 0 0 1 7-7z"/><line x1="9" y1="21" x2="15" y2="21"/></svg>
            AI 健康建议
          </h3>
          <div class="ai-advice-content">{{ latestRecord.aiAdvice }}</div>
        </div>

        <div class="charts-section card">
          <h3 class="section-title">趋势变化</h3>
          <div class="charts-grid">
            <div class="chart-item">
              <h4 class="chart-label">体重变化</h4>
              <div ref="weightChartRef" class="chart-container"></div>
            </div>
            <div class="chart-item">
              <h4 class="chart-label">体温变化</h4>
              <div ref="tempChartRef" class="chart-container"></div>
            </div>
            <div class="chart-item">
              <h4 class="chart-label">心率变化</h4>
              <div ref="heartChartRef" class="chart-container"></div>
            </div>
          </div>
        </div>

        <div class="timeline-section card">
          <h3 class="section-title">体检历史</h3>
          <div v-if="allRecords.length" class="timeline">
            <div v-for="r in allRecords" :key="r.id" class="timeline-item">
              <div class="timeline-dot" :class="riskDotClass(r.riskLevel)"></div>
              <div class="timeline-content">
                <div class="timeline-date">{{ r.inspectionDate || '-' }}</div>
                <div class="timeline-info">
                  <span class="timeline-score" :style="{ color: getScoreColor(r.healthScore) }">{{ r.healthScore ?? '-' }}分</span>
                  <span class="tag" :class="riskTagClass(r.riskLevel)">{{ riskText(r.riskLevel) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <div class="empty-text">暂无历史记录</div>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { healthApi, petApi } from '../../api'
import * as echarts from 'echarts'

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('client_user')) || {}
  } catch {
    return {}
  }
})

const myPets = ref([])
const selectedPetId = ref(null)
const latestRecord = ref(null)
const allRecords = ref([])
const trendData = ref([])

const weightChartRef = ref(null)
const tempChartRef = ref(null)
const heartChartRef = ref(null)

let weightChart = null
let tempChart = null
let heartChart = null

const currentPetName = computed(() => {
  const pet = myPets.value.find(p => p.id === selectedPetId.value)
  return pet?.name || '宠物'
})

const scoreColor = computed(() => {
  const s = latestRecord.value?.healthScore
  if (s == null) return 'var(--text-muted)'
  if (s >= 90) return 'var(--color-success)'
  if (s >= 75) return 'var(--color-primary)'
  if (s >= 60) return 'var(--color-warning)'
  return 'var(--color-danger)'
})

const scoreColorClass = computed(() => {
  const s = latestRecord.value?.healthScore
  if (s == null) return ''
  if (s >= 90) return 'ring-success'
  if (s >= 75) return 'ring-primary'
  if (s >= 60) return 'ring-warning'
  return 'ring-danger'
})

const scoreText = computed(() => {
  const s = latestRecord.value?.healthScore
  if (s == null) return '-'
  if (s >= 90) return '健康优秀'
  if (s >= 75) return '良好'
  if (s >= 60) return '注意'
  return '风险'
})

const scoreDashArray = computed(() => {
  const s = latestRecord.value?.healthScore ?? 0
  const circumference = 2 * Math.PI * 70
  const filled = (s / 100) * circumference
  return `${filled} ${circumference}`
})

const tempStatus = computed(() => getVitalStatus(latestRecord.value?.temperature, 37.5, 39.5))
const weightStatus = computed(() => 'dot-green')
const heartStatus = computed(() => getVitalStatus(latestRecord.value?.heartRate, 60, 180))
const respStatus = computed(() => getVitalStatus(latestRecord.value?.respirationRate, 10, 35))

function getVitalStatus(value, low, high) {
  if (value == null) return 'dot-muted'
  if (value < low || value > high) return 'dot-red'
  if (value < low * 1.05 || value > high * 0.95) return 'dot-yellow'
  return 'dot-green'
}

function conditionStatus(val) {
  const text = statusText(val)
  if (text === '良好') return 'dot-green'
  if (text === '一般') return 'dot-yellow'
  if (text === '差') return 'dot-red'
  return 'dot-muted'
}

function statusText(val) {
  if (val == null) return '-'
  const map = { 1: '差', 2: '一般', 3: '良好' }
  if (typeof val === 'number') return map[val] || String(val)
  return val
}

function riskText(level) {
  const map = { 0: '健康', 1: '注意', 2: '风险', 3: '高风险' }
  return map[level] ?? '未知'
}

function riskTagClass(level) {
  const map = { 0: 'tag-success', 1: 'tag-warning', 2: 'tag-accent', 3: 'tag-danger' }
  return map[level] ?? 'tag-info'
}

function riskDotClass(level) {
  const map = { 0: 'dot-green', 1: 'dot-yellow', 2: 'dot-orange', 3: 'dot-red' }
  return map[level] ?? 'dot-muted'
}

function getScoreColor(score) {
  if (score == null) return 'var(--text-muted)'
  if (score >= 90) return 'var(--color-success)'
  if (score >= 75) return 'var(--color-primary)'
  if (score >= 60) return 'var(--color-warning)'
  return 'var(--color-danger)'
}

async function loadPets() {
  if (!user.value.id) return
  try {
    const res = await petApi.getByUserId(user.value.id)
    if (res.code === 200 && res.data?.length) {
      myPets.value = res.data
      selectedPetId.value = res.data[0].id
      loadHealthData()
    }
  } catch {}
}

function selectPet(petId) {
  selectedPetId.value = petId
  loadHealthData()
}

async function loadHealthData() {
  if (!selectedPetId.value) return
  latestRecord.value = null
  allRecords.value = []
  trendData.value = []
  try {
    const [latestRes, recordsRes, trendRes] = await Promise.all([
      healthApi.latestByPet(selectedPetId.value).catch(() => null),
      healthApi.recordsByPet(selectedPetId.value).catch(() => null),
      healthApi.trend(selectedPetId.value).catch(() => null)
    ])
    if (latestRes?.code === 200 && latestRes.data) {
      latestRecord.value = latestRes.data
    }
    if (recordsRes?.code === 200 && recordsRes.data) {
      allRecords.value = recordsRes.data
      if (!latestRecord.value && recordsRes.data.length) {
        latestRecord.value = recordsRes.data[0]
      }
    }
    if (trendRes?.code === 200 && trendRes.data) {
      trendData.value = trendRes.data
    }
  } catch {}
  nextTick(initCharts)
}

function initCharts() {
  if (!trendData.value.length) return

  const dates = trendData.value.map(d => d.inspectionDate || '').slice().reverse()
  const weights = trendData.value.map(d => d.weight).slice().reverse()
  const temps = trendData.value.map(d => d.temperature).slice().reverse()
  const hearts = trendData.value.map(d => d.heartRate).slice().reverse()

  const baseOption = {
    grid: { top: 20, right: 16, bottom: 30, left: 45 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { fontSize: 11, color: '#9CA3AF' },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F3F4F6' } },
      axisLabel: { fontSize: 11, color: '#9CA3AF' }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: { color: '#1F2937', fontSize: 13 }
    }
  }

  if (weightChartRef.value) {
    if (weightChart) weightChart.dispose()
    weightChart = echarts.init(weightChartRef.value)
    weightChart.setOption({
      ...baseOption,
      series: [{
        type: 'line',
        data: weights,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#4F8EF7', width: 2 },
        itemStyle: { color: '#4F8EF7' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79,142,247,0.15)' },
            { offset: 1, color: 'rgba(79,142,247,0.01)' }
          ])
        }
      }]
    })
  }

  if (tempChartRef.value) {
    if (tempChart) tempChart.dispose()
    tempChart = echarts.init(tempChartRef.value)
    tempChart.setOption({
      ...baseOption,
      series: [{
        type: 'line',
        data: temps,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#EF4444', width: 2 },
        itemStyle: { color: '#EF4444' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(239,68,68,0.15)' },
            { offset: 1, color: 'rgba(239,68,68,0.01)' }
          ])
        }
      }]
    })
  }

  if (heartChartRef.value) {
    if (heartChart) heartChart.dispose()
    heartChart = echarts.init(heartChartRef.value)
    heartChart.setOption({
      ...baseOption,
      series: [{
        type: 'line',
        data: hearts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#22C55E', width: 2 },
        itemStyle: { color: '#22C55E' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(34,197,94,0.15)' },
            { offset: 1, color: 'rgba(34,197,94,0.01)' }
          ])
        }
      }]
    })
  }
}

onMounted(loadPets)
</script>

<style scoped>
.health-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-4);
}

.page-title {
  margin: 0;
}

.empty-card {
  text-align: center;
}

.pet-selector {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
}

.pet-tab {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: var(--bg-card);
  border: 1.5px solid var(--border-color);
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-body);
}

.pet-tab:hover {
  border-color: var(--color-primary-light);
  color: var(--color-primary);
}

.pet-tab.active {
  background: var(--color-primary-bg);
  border-color: var(--color-primary);
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.pet-emoji {
  font-size: var(--font-size-md);
}

.score-section {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-6);
}

.score-ring-wrap {
  flex-shrink: 0;
}

.score-ring {
  position: relative;
  width: 160px;
  height: 160px;
}

.score-ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.score-ring-bg {
  fill: none;
  stroke: var(--border-light);
  stroke-width: 8;
}

.score-ring-fill {
  fill: none;
  stroke-width: 8;
  stroke-linecap: round;
  transition: stroke-dasharray 0.8s ease;
}

.score-ring-inner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.score-number {
  font-size: 42px;
  font-weight: var(--font-weight-bold);
  line-height: 1;
  letter-spacing: -0.03em;
}

.score-label {
  display: block;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-top: 4px;
  font-weight: var(--font-weight-medium);
}

.score-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.pet-health-name {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0;
}

.score-date {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
}

.indicators-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.indicator-card {
  padding: var(--space-3) var(--space-4);
}

.indicator-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.indicator-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-weight: var(--font-weight-semibold);
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.dot-green {
  background: var(--color-success);
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.4);
}

.dot-yellow {
  background: var(--color-warning);
  box-shadow: 0 0 6px rgba(245, 158, 11, 0.4);
}

.dot-orange {
  background: var(--color-accent);
  box-shadow: 0 0 6px rgba(255, 184, 107, 0.4);
}

.dot-red {
  background: var(--color-danger);
  box-shadow: 0 0 6px rgba(239, 68, 68, 0.4);
}

.dot-muted {
  background: var(--text-muted);
}

.indicator-value {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  letter-spacing: -0.02em;
}

.indicator-value-sm {
  font-size: var(--font-size-md);
}

.indicator-small {
  padding: var(--space-2) var(--space-3);
}

.risk-alerts {
  margin-bottom: var(--space-4);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0 0 var(--space-3) 0;
}

.ai-advice {
  margin-bottom: var(--space-4);
}

.ai-advice-content {
  padding: var(--space-3);
  background: var(--color-primary-bg);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--text-body);
  line-height: 1.8;
}

.charts-section {
  margin-bottom: var(--space-4);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.chart-item {
  min-width: 0;
}

.chart-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-muted);
  margin: 0 0 var(--space-2) 0;
}

.chart-container {
  width: 100%;
  height: 200px;
}

.timeline-section {
  margin-bottom: var(--space-4);
}

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: var(--border-light);
}

.timeline-item {
  position: relative;
  padding-bottom: var(--space-4);
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: var(--radius-full);
  border: 2px solid var(--bg-card);
  z-index: 1;
}

.timeline-content {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.timeline-date {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  min-width: 100px;
}

.timeline-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.timeline-score {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
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
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .score-section {
    flex-direction: column;
    text-align: center;
  }

  .score-meta {
    align-items: center;
  }

  .indicators-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
