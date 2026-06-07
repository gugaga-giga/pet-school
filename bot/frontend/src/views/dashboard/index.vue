<template>
  <div class="dashboard-page">
    <div class="stat-cards">
      <StatCard title="用户总数" :value="stats.user_count" icon="User" color="#409eff" />
      <StatCard title="知识库数" :value="stats.knowledge_base_count" icon="Collection" color="#67c23a" />
      <StatCard title="文档总数" :value="stats.document_count" icon="Document" color="#e6a23c" />
      <StatCard title="对话总数" :value="stats.session_count" icon="ChatDotRound" color="#f56c6c" />
    </div>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>对话趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>意图分布</span>
          </template>
          <div ref="intentChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近问答</span>
          <el-button text type="primary" @click="$router.push('/admin/logs')">查看更多</el-button>
        </div>
      </template>
      <el-table :data="recentLogs" stripe style="width: 100%">
        <el-table-column prop="question" label="用户问题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="intent_type" label="意图" width="120">
          <template #default="{ row }">
            <IntentTag :intent="row.intent_type" />
          </template>
        </el-table-column>
        <el-table-column prop="total_time" label="响应时间" width="120">
          <template #default="{ row }">
            {{ row.total_time ? row.total_time + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="时间" width="180">
          <template #default="{ row }">
            {{ row.created_at || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboard, getTrendData, getIntentDistribution, getQALogs } from '@/api/admin'
import StatCard from '@/components/StatCard.vue'
import IntentTag from '@/components/IntentTag.vue'

const stats = ref({
  user_count: 0,
  knowledge_base_count: 0,
  document_count: 0,
  session_count: 0,
})

const recentLogs = ref<any[]>([])
const trendChartRef = ref<HTMLElement>()
const intentChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let intentChart: echarts.ECharts | null = null

onMounted(async () => {
  await Promise.all([fetchStats(), fetchTrend(), fetchIntentDist(), fetchRecentLogs()])
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  intentChart?.dispose()
})

async function fetchStats() {
  try {
    const res = await getDashboard()
    const d = (res as any).data || res
    // 后端返回嵌套结构，展平
    stats.value = {
      user_count: d.users?.total || 0,
      knowledge_base_count: d.knowledge?.total_knowledge_bases || 0,
      document_count: d.knowledge?.total_documents || 0,
      session_count: d.chat?.total_sessions || 0,
    }
  } catch {
    // use default
  }
}

async function fetchTrend() {
  try {
    const res = await getTrendData({ days: 7 })
    const d = (res as any).data || res
    const items = Array.isArray(d) ? d : []
    renderTrendChart(items)
  } catch {
    renderTrendChart([])
  }
}

async function fetchIntentDist() {
  try {
    const res = await getIntentDistribution()
    const d = (res as any).data || res
    const items = Array.isArray(d) ? d : []
    renderIntentChart(items)
  } catch {
    renderIntentChart([])
  }
}

async function fetchRecentLogs() {
  try {
    const res = await getQALogs({ page: 1, page_size: 5 })
    const d = (res as any).data || res
    recentLogs.value = d.items || []
  } catch {
    // use default
  }
}

function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (intentChartRef.value) {
    intentChart = echarts.init(intentChartRef.value)
  }
}

function renderTrendChart(items: any[]) {
  if (!trendChart) {
    trendChart = trendChartRef.value ? echarts.init(trendChartRef.value) : null
  }
  if (!trendChart) return

  const dates = items.map((i: any) => i.date)
  const counts = items.map((i: any) => i.session_count)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['对话数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      {
        name: '对话数',
        type: 'line',
        smooth: true,
        data: counts,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#409eff' }
      }
    ]
  })
}

function renderIntentChart(data: any[]) {
  if (!intentChart) {
    intentChart = intentChartRef.value ? echarts.init(intentChartRef.value) : null
  }
  if (!intentChart) return

  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6']
  intentChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}: {d}%' },
        data: data.map((item, index) => ({
          name: item.name || item.intent_type,
          value: item.value || item.count,
          itemStyle: { color: colors[index % colors.length] }
        }))
      }
    ]
  })
}

function handleResize() {
  trendChart?.resize()
  intentChart?.resize()
}
</script>

<style lang="scss" scoped>
.dashboard-page {
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .chart-row {
    margin-bottom: 20px;
  }

  .chart-card {
    .chart-container {
      height: 350px;
    }
  }

  .recent-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
