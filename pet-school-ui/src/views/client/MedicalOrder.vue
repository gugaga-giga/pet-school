<template>
  <div class="order-page">
    <div class="order-header">
      <h1 class="page-title">我的医疗订单</h1>
      <p class="page-subtitle">查看和管理您的医疗预约记录</p>
      <div class="sub-nav">
        <router-link to="/client/medical" class="sub-nav-item">
          <span class="sub-nav-icon">🩺</span>预约挂号
        </router-link>
        <router-link to="/client/medical-order" class="sub-nav-item active">
          <span class="sub-nav-icon">📋</span>我的预约
        </router-link>
        <router-link to="/client/medical-record" class="sub-nav-item">
          <span class="sub-nav-icon">📄</span>我的病历
        </router-link>
      </div>
    </div>

    <div class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="['tab-pill', activeTab === tab.value ? 'tab-pill--active' : '']"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <div v-if="filteredOrders.length" class="order-list">
      <div
        v-for="order in filteredOrders"
        :key="order.id"
        class="order-card"
      >
        <div class="order-card__header">
          <span class="order-no">订单号：{{ order.orderNo || order.id }}</span>
          <span :class="['tag', statusTagClass(order.status)]">{{ statusLabel(order.status) }}</span>
        </div>
        <div class="order-card__body">
          <div class="order-info-grid">
            <div class="order-info-item">
              <span class="info-label">医生</span>
              <span class="info-value">{{ order.doctorName || order.doctor?.name || '-' }}</span>
            </div>
            <div class="order-info-item">
              <span class="info-label">科室</span>
              <span class="info-value">{{ order.departmentName || order.department?.name || '-' }}</span>
            </div>
            <div class="order-info-item">
              <span class="info-label">宠物</span>
              <span class="info-value">{{ order.petName || order.pet?.name || '-' }}</span>
            </div>
            <div class="order-info-item">
              <span class="info-label">预约时间</span>
              <span class="info-value">{{ formatTime(order.appointmentTime) }}</span>
            </div>
            <div class="order-info-item">
              <span class="info-label">费用</span>
              <span class="info-value price-text">¥{{ order.price || 0 }}</span>
            </div>
          </div>
        </div>
        <div class="order-card__footer">
          <button
            v-if="order.status === 0 || order.status === 1"
            class="btn btn-secondary btn-sm"
            @click="cancelOrder(order)"
          >
            取消预约
          </button>
          <button
            v-if="order.status === 3"
            class="btn btn-primary btn-sm"
            @click="viewRecord(order)"
          >
            查看病历
          </button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">📋</div>
      <h3 class="empty-title">暂无订单</h3>
      <p class="empty-desc">{{ activeTab === -1 ? '您还没有医疗预约订单' : '该状态下暂无订单' }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { medicalApi } from '../../api'

const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('client_user') || 'null'))

const orders = ref([])
const activeTab = ref(-1)

const tabs = computed(() => [
  { label: '全部', value: -1, count: orders.value.length },
  { label: '待确认', value: 0, count: orders.value.filter(o => o.status === 0).length },
  { label: '已预约', value: 1, count: orders.value.filter(o => o.status === 1).length },
  { label: '就诊中', value: 2, count: orders.value.filter(o => o.status === 2).length },
  { label: '已完成', value: 3, count: orders.value.filter(o => o.status === 3).length },
  { label: '已取消', value: 4, count: orders.value.filter(o => o.status === 4).length }
])

const filteredOrders = computed(() => {
  if (activeTab.value === -1) return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

function statusLabel(status) {
  const map = { 0: '待确认', 1: '已预约', 2: '就诊中', 3: '已完成', 4: '已取消' }
  return map[status] ?? '未知'
}

function statusTagClass(status) {
  const map = { 0: 'tag-warning', 1: 'tag-info', 2: 'tag-primary', 3: 'tag-success', 4: 'tag-danger' }
  return map[status] ?? 'tag-info'
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 16)
}

async function loadOrders() {
  if (!user.value?.id) return
  try {
    const res = await medicalApi.myOrders(user.value.id)
    orders.value = res.data || res || []
  } catch (e) {
    console.error(e)
  }
}

async function cancelOrder(order) {
  if (!confirm('确定要取消该预约吗？')) return
  try {
    await medicalApi.cancelOrder(order.id, user.value.id)
    await loadOrders()
  } catch (e) {
    alert(e.response?.data?.message || '取消失败')
  }
}

async function viewRecord(order) {
  try {
    const res = await medicalApi.recordByOrder(order.id)
    const record = res.data || res
    if (record && record.id) {
      router.push(`/client/medical-record?recordId=${record.id}`)
    } else {
      alert('暂无病历记录')
    }
  } catch (e) {
    alert('获取病历失败')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: var(--space-6) var(--space-8);
}

.order-header {
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

.tab-bar {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-6);
  padding: var(--space-2);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 8px 18px;
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

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.25);
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
}

.tab-pill:not(.tab-pill--active) .tab-count {
  background: var(--bg-hover);
  color: var(--text-muted);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.order-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  overflow: hidden;
  transition: all var(--transition-base);
}

.order-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.order-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, rgba(79, 124, 255, 0.02), rgba(255, 184, 107, 0.02));
  border-bottom: 1px solid var(--border-light);
}

.order-no {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.order-card__body {
  padding: var(--space-4) var(--space-5);
}

.order-info-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
}

.order-info-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.info-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-weight: var(--font-weight-medium);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.info-value {
  font-size: var(--font-size-sm);
  color: var(--text-title);
  font-weight: var(--font-weight-semibold);
}

.price-text {
  color: var(--color-accent);
  font-weight: var(--font-weight-bold);
}

.order-card__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  border-top: 1px solid var(--border-light);
  background: var(--bg-input);
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

@media (max-width: 860px) {
  .order-info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .order-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
