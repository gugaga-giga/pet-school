<template>
  <div class="my-coupon-page">
    <div class="page-header">
      <h1 class="page-title">🎟️ 我的优惠券</h1>
      <div class="coupon-stats">
        <span class="stat-chip stat-chip--primary">可用 <strong>{{ availableCount }}</strong> 张</span>
        <span class="stat-chip">已使用 <strong>{{ usedCount }}</strong> 张</span>
      </div>
    </div>

    <div class="tab-bar">
      <button
        :class="['tab-pill', tab === 0 ? 'tab-pill--active' : '']"
        @click="switchTab(0)"
      >可使用</button>
      <button
        :class="['tab-pill', tab === 1 ? 'tab-pill--active' : '']"
        @click="switchTab(1)"
      >已使用</button>
      <button
        :class="['tab-pill', tab === 2 ? 'tab-pill--active' : '']"
        @click="switchTab(2)"
      >已过期</button>
    </div>

    <div v-if="filteredCoupons.length" class="coupon-list">
      <div
        v-for="uc in filteredCoupons"
        :key="uc.id"
        class="ticket-card"
        :class="{
          'ticket-card--used': tab === 1,
          'ticket-card--expired': tab === 2
        }"
      >
        <div class="ticket-left" :class="'ticket-left--' + typeClass(getCouponField(uc, 'type'))">
          <div class="ticket-value">
            <template v-if="getCouponField(uc, 'discountType') === 1">
              <span class="ticket-symbol">¥</span>
              <span class="ticket-amount">{{ getCouponField(uc, 'discountValue') }}</span>
            </template>
            <template v-else>
              <span class="ticket-amount">{{ getCouponField(uc, 'discountValue') }}</span>
              <span class="ticket-unit">折</span>
            </template>
          </div>
          <div v-if="(getCouponField(uc, 'minAmount') || 0) > 0" class="ticket-threshold">满{{ getCouponField(uc, 'minAmount') }}可用</div>
          <div v-else class="ticket-threshold">无门槛</div>
        </div>

        <div class="ticket-divider">
          <div class="divider-circle divider-circle--top"></div>
          <div class="divider-line"></div>
          <div class="divider-circle divider-circle--bottom"></div>
        </div>

        <div class="ticket-right">
          <div class="ticket-info">
            <h3 class="ticket-name">{{ uc.couponName || uc.name }}</h3>
            <div class="ticket-tags">
              <span :class="['tag', typeTagClass(getCouponField(uc, 'type'))]">{{ typeText(getCouponField(uc, 'type')) }}</span>
            </div>
            <div v-if="tab === 1 && uc.useTime" class="ticket-meta">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              使用于 {{ formatTime(uc.useTime) }}
            </div>
            <div v-else-if="tab === 1 && uc.orderNo" class="ticket-meta">
              订单号: {{ uc.orderNo }}
            </div>
            <div v-else class="ticket-period">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              {{ formatDate(getCouponField(uc, 'startTime')) }} - {{ formatDate(getCouponField(uc, 'endTime')) }}
            </div>
          </div>
          <div class="ticket-action">
            <button
              v-if="tab === 0"
              class="btn btn-primary btn-sm ticket-btn"
              @click="goUse(uc)"
            >去使用</button>
          </div>
        </div>

        <div v-if="tab === 2" class="ticket-stamp">已过期</div>
        <div v-if="tab === 1" class="ticket-stamp ticket-stamp--used">已使用</div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">{{ tab === 0 ? '🎟️' : tab === 1 ? '📋' : '⏰' }}</div>
      <div class="empty-text">{{ emptyText }}</div>
      <button v-if="tab === 0" class="btn btn-primary btn-sm" style="margin-top: var(--space-3)" @click="router.push('/client/coupon-center')">去领券中心</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { couponApi } from '../../api'

const router = useRouter()
const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('client_user')) || {} } catch { return {} }
})

const tab = ref(0)
const coupons = ref([])

const availableCount = computed(() => coupons.value.filter(c => c.status === 0 || c.status === 'unused').length)
const usedCount = computed(() => coupons.value.filter(c => c.status === 1 || c.status === 'used').length)

const filteredCoupons = computed(() => {
  if (tab.value === 0) return coupons.value.filter(c => c.status === 0 || c.status === 'unused')
  if (tab.value === 1) return coupons.value.filter(c => c.status === 1 || c.status === 'used')
  return coupons.value.filter(c => c.status === 2 || c.status === 'expired')
})

const emptyText = computed(() => {
  return ['暂无可用优惠券', '暂无已使用的优惠券', '暂无已过期的优惠券'][tab.value]
})

function getCouponField(uc, field) {
  return uc[field] || uc.coupon?.[field]
}

function typeText(t) {
  return { 1: '课程券', 2: '寄宿券', 3: '通用券' }[t] || '优惠券'
}

function typeClass(t) {
  return { 1: 'course', 2: 'boarding', 3: 'universal' }[t] || 'course'
}

function typeTagClass(t) {
  return { 1: 'tag-primary', 2: 'tag-accent', 3: 'tag-success' }[t] || ''
}

function formatDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  const pad = n => String(n).padStart(2, '0')
  return `${pad(dt.getMonth() + 1)}.${pad(dt.getDate())}`
}

function formatTime(t) {
  if (!t) return ''
  const dt = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}

function switchTab(t) {
  tab.value = t
  loadCoupons()
}

function goUse(uc) {
  const type = getCouponField(uc, 'type')
  if (type === 2 || type === 3) {
    router.push('/client/room')
  } else {
    router.push('/client/course')
  }
}

async function loadCoupons() {
  if (!user.value.id) return
  try {
    const res = await couponApi.userCoupons(user.value.id)
    if (res.code === 200) coupons.value = res.data || []
  } catch {
    coupons.value = []
  }
}

onMounted(loadCoupons)
watch(tab, loadCoupons)
</script>

<style scoped>
.my-coupon-page {
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.coupon-stats {
  display: flex;
  gap: var(--space-3);
}

.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  color: var(--text-body);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
}

.stat-chip strong {
  color: var(--text-title);
  font-weight: var(--font-weight-bold);
}

.stat-chip--primary {
  background: var(--color-primary-bg);
  border-color: var(--color-primary-200);
  color: var(--color-primary-dark);
}

.stat-chip--primary strong {
  color: var(--color-primary);
}

.tab-bar {
  display: flex;
  gap: var(--space-1);
  margin-bottom: var(--space-5);
  background: var(--bg-card);
  padding: 4px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border-light);
  width: fit-content;
}

.tab-pill {
  padding: var(--space-2) var(--space-5);
  border: none;
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--text-body);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.tab-pill:hover {
  color: var(--text-title);
  background: var(--color-primary-50);
}

.tab-pill--active {
  background: var(--color-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-button);
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.ticket-card {
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  position: relative;
  transition: all var(--transition-base);
  border: 1.5px solid var(--border-light);
}

.ticket-card:not(.ticket-card--used):not(.ticket-card--expired):hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.ticket-card--used,
.ticket-card--expired {
  opacity: 0.55;
  filter: grayscale(0.3);
}

.ticket-left {
  width: 130px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-5) var(--space-4);
  color: var(--text-inverse);
}

.ticket-left--course {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
}

.ticket-left--boarding {
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-light) 100%);
  color: var(--text-title);
}

.ticket-left--universal {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
}

.ticket-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
}

.ticket-symbol {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.ticket-amount {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  line-height: 1.1;
  letter-spacing: -0.03em;
}

.ticket-unit {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  margin-left: 2px;
}

.ticket-threshold {
  font-size: var(--font-size-xs);
  opacity: 0.85;
  margin-top: var(--space-1);
  white-space: nowrap;
}

.ticket-divider {
  width: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.divider-circle {
  width: 16px;
  height: 8px;
  background: var(--bg-page);
  position: absolute;
  z-index: 1;
}

.divider-circle--top {
  top: -1px;
  border-radius: 0 0 8px 8px;
}

.divider-circle--bottom {
  bottom: -1px;
  border-radius: 8px 8px 0 0;
}

.divider-line {
  width: 0;
  height: 100%;
  border-left: 2px dashed var(--border-color);
  position: absolute;
  top: 8px;
  bottom: 8px;
}

.ticket-right {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5) var(--space-4) var(--space-4);
  gap: var(--space-4);
}

.ticket-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.ticket-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  line-height: 1.3;
  margin: 0;
}

.ticket-tags {
  display: flex;
  gap: var(--space-1);
  flex-wrap: wrap;
}

.ticket-period,
.ticket-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.ticket-action {
  flex-shrink: 0;
}

.ticket-btn {
  min-width: 80px;
}

.ticket-stamp {
  position: absolute;
  top: 50%;
  right: var(--space-6);
  transform: translateY(-50%) rotate(-15deg);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-muted);
  opacity: 0.25;
  border: 3px solid var(--text-muted);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  pointer-events: none;
  letter-spacing: 0.1em;
}

.ticket-stamp--used {
  color: var(--color-success);
  border-color: var(--color-success);
  opacity: 0.3;
}

.empty-state {
  text-align: center;
  padding: var(--space-8) var(--space-4);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.empty-text {
  font-size: var(--font-size-lg);
  color: var(--text-muted);
}

@media (max-width: 600px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .ticket-left {
    width: 100px;
  }

  .ticket-amount {
    font-size: var(--font-size-2xl);
  }

  .ticket-right {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }
}
</style>
