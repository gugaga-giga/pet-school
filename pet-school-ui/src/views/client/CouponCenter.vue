<template>
  <div class="coupon-center">
    <div class="center-header">
      <h1 class="page-title">🎟️ 优惠券中心</h1>
      <p class="center-subtitle">领取优惠券，享受更多优惠</p>
    </div>

    <div v-if="coupons.length" class="coupon-grid">
      <div
        v-for="c in coupons"
        :key="c.id"
        class="ticket-card"
        :class="{
          'ticket-card--claimed': myCouponIds.has(c.id),
          'ticket-card--soldout': c.totalCount != null && c.receiveCount >= c.totalCount && !myCouponIds.has(c.id)
        }"
      >
        <div class="ticket-left" :class="'ticket-left--' + typeClass(c.type)">
          <div class="ticket-value">
            <template v-if="c.discountType === 1">
              <span class="ticket-symbol">¥</span>
              <span class="ticket-amount">{{ c.discountValue }}</span>
            </template>
            <template v-else>
              <span class="ticket-amount">{{ c.discountValue }}</span>
              <span class="ticket-unit">折</span>
            </template>
          </div>
          <div v-if="c.minAmount > 0" class="ticket-threshold">满{{ c.minAmount }}可用</div>
          <div v-else class="ticket-threshold">无门槛</div>
        </div>

        <div class="ticket-divider">
          <div class="divider-circle divider-circle--top"></div>
          <div class="divider-line"></div>
          <div class="divider-circle divider-circle--bottom"></div>
        </div>

        <div class="ticket-right">
          <div class="ticket-info">
            <h3 class="ticket-name">{{ c.name }}</h3>
            <div class="ticket-tags">
              <span :class="['tag', typeTagClass(c.type)]">{{ typeText(c.type) }}</span>
              <span v-if="c.discountType === 2 && c.maxDiscount" class="tag tag-warning">最高减¥{{ c.maxDiscount }}</span>
            </div>
            <div class="ticket-period">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              {{ formatDate(c.startTime) }} - {{ formatDate(c.endTime) }}
            </div>
            <div v-if="c.description" class="ticket-desc">{{ c.description }}</div>
          </div>
          <div class="ticket-action">
            <button
              v-if="myCouponIds.has(c.id)"
              class="btn btn-secondary btn-sm ticket-btn"
              disabled
            >已领取</button>
            <button
              v-else-if="c.totalCount != null && c.receiveCount >= c.totalCount"
              class="btn btn-secondary btn-sm ticket-btn"
              disabled
            >已领完</button>
            <button
              v-else
              class="btn btn-accent btn-sm ticket-btn"
              @click="claimCoupon(c.id)"
            >领取</button>
          </div>
        </div>

        <div v-if="myCouponIds.has(c.id)" class="ticket-stamp">已领取</div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">🎟️</div>
      <div class="empty-text">暂无可领取的优惠券</div>
      <div class="empty-hint">敬请期待更多优惠活动</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { couponApi } from '../../api'

const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('client_user')) || {} } catch { return {} }
})

const coupons = ref([])
const myCouponIds = ref(new Set())

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

async function loadCoupons() {
  try {
    const [activeRes, myRes] = await Promise.all([
      couponApi.activeList(),
      user.value.id ? couponApi.userCoupons(user.value.id) : Promise.resolve({ data: [] })
    ])
    if (activeRes.code === 200) coupons.value = activeRes.data || []
    if (myRes.code === 200) {
      const myCoupons = myRes.data || []
      myCouponIds.value = new Set(myCoupons.map(uc => uc.couponId))
    }
  } catch {
    coupons.value = []
  }
}

async function claimCoupon(couponId) {
  try {
    const res = await couponApi.receive(couponId, user.value.id)
    if (res.code === 200) {
      myCouponIds.value.add(couponId)
      myCouponIds.value = new Set(myCouponIds.value)
    } else {
      alert(res.message || '领取失败')
    }
  } catch (e) {
    alert('领取失败，请重试')
  }
}

onMounted(loadCoupons)
</script>

<style scoped>
.coupon-center {
  min-height: 100%;
}

.center-header {
  margin-bottom: var(--space-6);
}

.center-subtitle {
  color: var(--text-muted);
  font-size: var(--font-size-base);
  margin-top: var(--space-1);
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5);
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

.ticket-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.ticket-card--claimed,
.ticket-card--soldout {
  opacity: 0.6;
}

.ticket-card--claimed:hover,
.ticket-card--soldout:hover {
  transform: none;
  box-shadow: var(--shadow-card);
}

.ticket-left {
  width: 120px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
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
  background: linear-gradient(135deg, #4F7CFF 0%, #FFB86B 100%);
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
  flex-direction: column;
  justify-content: space-between;
  padding: var(--space-4) var(--space-4) var(--space-4) var(--space-3);
}

.ticket-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
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

.ticket-period {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.ticket-desc {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ticket-action {
  margin-top: var(--space-3);
}

.ticket-btn {
  width: 100%;
}

.ticket-stamp {
  position: absolute;
  top: 50%;
  right: var(--space-5);
  transform: translateY(-50%) rotate(-15deg);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-danger);
  opacity: 0.3;
  border: 3px solid var(--color-danger);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  pointer-events: none;
  letter-spacing: 0.1em;
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
  margin-bottom: var(--space-2);
}

.empty-hint {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  opacity: 0.7;
}

@media (max-width: 960px) {
  .coupon-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .coupon-grid {
    grid-template-columns: 1fr;
  }

  .ticket-left {
    width: 100px;
  }

  .ticket-amount {
    font-size: var(--font-size-2xl);
  }
}
</style>
