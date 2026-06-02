<template>
  <div class="order-page">
    <h1 class="page-title">我的订单</h1>

    <div class="stat-row">
      <div class="stat-card stat-pending">
        <div class="stat-icon">⏳</div>
        <div class="stat-info">
          <div class="stat-value">{{ pendingCount }}</div>
          <div class="stat-label">待支付</div>
        </div>
      </div>
      <div class="stat-card stat-paid">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ paidCount }}</div>
          <div class="stat-label">已支付</div>
        </div>
      </div>
      <div class="stat-card stat-cancelled">
        <div class="stat-icon">✕</div>
        <div class="stat-info">
          <div class="stat-value">{{ cancelledCount }}</div>
          <div class="stat-label">已取消</div>
        </div>
      </div>
      <div class="stat-card stat-total">
        <div class="stat-icon">💰</div>
        <div class="stat-info">
          <div class="stat-value">¥{{ totalSpent }}</div>
          <div class="stat-label">总消费</div>
        </div>
      </div>
    </div>

    <div class="tab-bar">
      <button :class="['tab-pill', tab === 'all' ? 'tab-pill--active' : '']" @click="tab = 'all'">全部</button>
      <button :class="['tab-pill', tab === 'course' ? 'tab-pill--active' : '']" @click="tab = 'course'">课程订单</button>
      <button :class="['tab-pill', tab === 'boarding' ? 'tab-pill--active' : '']" @click="tab = 'boarding'">寄宿订单</button>
    </div>

    <div v-if="filteredOrders.length" class="order-list">
      <div v-for="o in filteredOrders" :key="o._key" class="order-card">
        <div class="order-left">
          <div class="order-header">
            <span :class="['type-badge', o._type === 'course' ? 'type-course' : 'type-boarding']">
              {{ o._type === 'course' ? '课程' : '寄宿' }}
            </span>
            <span class="order-no">{{ o.orderNo }}</span>
          </div>
          <div class="order-main">{{ o._mainInfo }}</div>
          <div class="order-sub">{{ o._subInfo }}</div>
          <div class="order-date">{{ formatTime(o.createTime) }}</div>
        </div>
        <div class="order-right">
          <div class="order-price-group">
            <div v-if="o.couponAmount > 0" class="order-original">¥{{ o.totalPrice }}</div>
            <div class="order-price">¥{{ o.finalPrice || o.totalPrice }}</div>
            <div v-if="o.couponAmount > 0" class="order-coupon-tag">优惠 -¥{{ o.couponAmount }}</div>
          </div>
          <span :class="['tag', statusClass(o.status)]">{{ statusText(o.status) }}</span>
          <div v-if="o.status === 0" class="order-actions">
            <button class="btn btn-primary btn-sm" @click="o._type === 'course' ? payCourse(o.id) : payBoarding(o.id)">支付</button>
            <button class="btn btn-ghost btn-sm" @click="o._type === 'course' ? cancelCourse(o.id) : cancelBoarding(o.id)">取消</button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">📋</div>
      <div class="empty-text">暂无订单记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderApi, roomApi } from '../../api'

const user = computed(() => { try { return JSON.parse(localStorage.getItem('client_user')) || {} } catch { return {} } })

const tab = ref('all')
const courseOrders = ref([])
const boardingOrders = ref([])

function statusText(s) {
  return s === 0 ? '待支付' : s === 1 ? '已支付' : s === 2 ? '已取消' : '未知'
}
function statusClass(s) {
  return s === 0 ? 'tag-warning' : s === 1 ? 'tag-success' : 'tag-danger'
}
function formatTime(t) {
  if (!t) return ''
  if (typeof t === 'string') return t.replace('T', ' ').slice(0, 19)
  return ''
}

const allOrders = computed(() => {
  const courseList = courseOrders.value.map(o => ({
    ...o,
    _type: 'course',
    _key: 'c' + o.id,
    _mainInfo: o.courseName || '未知课程',
    _subInfo: `套餐：${o.packageName || '-'} · 宠物：${o.petName || '-'} · ${o.months}个月`
  }))
  const boardingList = boardingOrders.value.map(o => ({
    ...o,
    _type: 'boarding',
    _key: 'b' + o.id,
    _mainInfo: o.roomTypeName || '未知房型',
    _subInfo: `房间：${o.roomNumber || '-'} · 宠物：${o.petName || '-'} · ${o.checkIn} ~ ${o.checkOut}`
  }))
  return [...courseList, ...boardingList].sort((a, b) => (b.id || 0) - (a.id || 0))
})

const filteredOrders = computed(() => {
  if (tab.value === 'course') return allOrders.value.filter(o => o._type === 'course')
  if (tab.value === 'boarding') return allOrders.value.filter(o => o._type === 'boarding')
  return allOrders.value
})

const pendingCount = computed(() => allOrders.value.filter(o => o.status === 0).length)
const paidCount = computed(() => allOrders.value.filter(o => o.status === 1).length)
const cancelledCount = computed(() => allOrders.value.filter(o => o.status === 2).length)
const totalSpent = computed(() => {
  return allOrders.value.filter(o => o.status === 1).reduce((sum, o) => sum + Number(o.finalPrice || o.totalPrice || 0), 0).toFixed(2)
})

async function loadOrders() {
  if (!user.value.id) return
  try {
    const [cRes, bRes] = await Promise.all([
      orderApi.courseByUser(user.value.id),
      roomApi.boardingByUser(user.value.id)
    ])
    if (cRes.code === 200) courseOrders.value = cRes.data || []
    if (bRes.code === 200) boardingOrders.value = bRes.data || []
  } catch (e) {}
}

async function payCourse(id) {
  try {
    const res = await orderApi.coursePay(id)
    if (res.code === 200) { alert('支付成功'); await loadOrders() }
    else alert(res.message || '支付失败')
  } catch (e) { alert('支付请求失败') }
}

async function cancelCourse(id) {
  if (!confirm('确定取消该订单？')) return
  try {
    const res = await orderApi.courseCancel(id)
    if (res.code === 200) { alert('已取消'); await loadOrders() }
    else alert(res.message || '取消失败')
  } catch (e) { alert('取消请求失败') }
}

async function payBoarding(id) {
  try {
    const res = await roomApi.boardingPay(id)
    if (res.code === 200) { alert('支付成功'); await loadOrders() }
    else alert(res.message || '支付失败')
  } catch (e) { alert('支付请求失败') }
}

async function cancelBoarding(id) {
  if (!confirm('确定取消该订单？')) return
  try {
    const res = await roomApi.boardingCancel(id)
    if (res.code === 200) { alert('已取消'); await loadOrders() }
    else alert(res.message || '取消失败')
  } catch (e) { alert('取消请求失败') }
}

onMounted(loadOrders)
</script>

<style scoped>
.order-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  border: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  transition: all var(--transition-base);
  box-shadow: var(--shadow-card);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-pending .stat-icon {
  background: var(--color-warning-bg);
}

.stat-paid .stat-icon {
  background: var(--color-success-bg);
}

.stat-cancelled .stat-icon {
  background: var(--color-danger-bg);
}

.stat-total .stat-icon {
  background: var(--color-primary-bg);
}

.stat-info {
  min-width: 0;
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.stat-pending .stat-value {
  color: var(--color-warning);
}

.stat-paid .stat-value {
  color: var(--color-success);
}

.stat-cancelled .stat-value {
  color: var(--color-danger);
}

.stat-total .stat-value {
  color: var(--color-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.tab-bar {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  padding: var(--space-2);
  background: var(--bg-card);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
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

.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.order-card {
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
  animation: fadeInUp var(--transition-base) ease both;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.order-left {
  flex: 1;
  min-width: 0;
}

.order-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.type-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.02em;
}

.type-course {
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
}

.type-boarding {
  background: var(--color-accent-bg);
  color: #92400e;
}

.order-no {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.order-main {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin-bottom: var(--space-1);
}

.order-sub {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin-bottom: var(--space-1);
}

.order-date {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.order-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-2);
  flex-shrink: 0;
}

.order-price {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.order-price-group {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.order-original {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-decoration: line-through;
}

.order-coupon-tag {
  font-size: var(--font-size-xs);
  color: var(--color-accent);
  font-weight: var(--font-weight-semibold);
}

.order-actions {
  display: flex;
  gap: var(--space-2);
}

@media (max-width: 768px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .order-card {
    flex-direction: column;
    gap: var(--space-3);
  }

  .order-right {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }
}
</style>
