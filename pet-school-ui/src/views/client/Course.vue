<template>
  <div class="course-page">
    <div class="course-header">
      <h1 class="page-title">课程选课</h1>
      <p class="course-subtitle">为您的爱宠选择最合适的训练课程</p>
    </div>

    <div class="category-bar">
      <button
        :class="['cat-pill', !selectedCat ? 'cat-pill--active' : '']"
        @click="selectCat(null)"
      >全部</button>
      <button
        v-for="c in categories"
        :key="c.id"
        :class="['cat-pill', selectedCat === c.id ? 'cat-pill--active' : '']"
        @click="selectCat(c.id)"
      >{{ c.name }}</button>
    </div>

    <div class="course-layout">
      <div class="course-grid" :class="{ 'course-grid--narrow': selCourse }">
        <div v-if="courses.length" class="grid-inner">
          <div
            v-for="c in courses"
            :key="c.id"
            class="course-card"
            :class="{ 'course-card--selected': selCourse && selCourse.id === c.id }"
            @click="showPackages(c)"
          >
            <div class="course-card__body">
              <div class="course-card__top">
                <h3 class="course-card__name">{{ c.name }}</h3>
                <span class="course-card__duration">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  {{ c.duration }}个月
                </span>
              </div>
              <p class="course-card__desc">{{ c.description || '暂无描述' }}</p>
              <div class="course-card__footer">
                <div class="course-card__price">
                  <span class="course-card__price-val">¥{{ c.monthlyPrice }}</span>
                  <span class="course-card__price-unit">/月</span>
                </div>
                <button class="btn btn-primary btn-sm" @click.stop="showPackages(c)">选课</button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-state__icon">🐾</div>
          <p class="empty-state__text">暂无课程</p>
          <p class="empty-state__hint">请尝试切换分类或稍后再来</p>
        </div>
      </div>

      <transition name="panel-slide">
        <div v-if="selCourse" class="detail-panel">
          <div class="detail-panel__inner">
            <button class="detail-panel__close" @click="selCourse = null">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>

            <div class="detail-panel__header">
              <h2 class="detail-panel__title">{{ selCourse.name }}</h2>
              <div class="detail-panel__meta">
                <span class="detail-panel__price">¥{{ selCourse.monthlyPrice }}<small>/月</small></span>
                <span class="tag tag-primary">{{ selCourse.duration }}个月</span>
              </div>
            </div>

            <div class="detail-panel__section">
              <h4 class="section-label">选择套餐</h4>
              <div v-if="packages.length" class="pkg-list">
                <div
                  v-for="pkg in packages"
                  :key="pkg.id"
                  class="pkg-card"
                  :class="{ 'pkg-card--selected': selPkgId === pkg.id }"
                  @click="selectPackage(pkg)"
                >
                  <div class="pkg-card__info">
                    <span class="pkg-card__name">{{ pkg.name }}</span>
                    <span class="pkg-card__price">¥{{ pkg.price }}</span>
                  </div>
                  <div v-if="selPkgId === pkg.id" class="pkg-card__check">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                  </div>
                </div>
              </div>
              <p v-else class="empty-hint">暂无套餐</p>
            </div>

            <div v-if="items.length" class="detail-panel__section">
              <h4 class="section-label">套餐项目</h4>
              <div class="items-table">
                <div class="items-table__head">
                  <span>项目</span>
                  <span>包含</span>
                  <span>额外费用</span>
                </div>
                <div v-for="it in items" :key="it.id" class="items-table__row">
                  <span>{{ it.name }}</span>
                  <span>
                    <span :class="['tag', it.included === 1 ? 'tag-success' : 'tag-warning']">
                      {{ it.included === 1 ? '包含' : '额外' }}
                    </span>
                  </span>
                  <span :class="it.included === 1 ? 'text-muted' : 'text-price'">
                    {{ it.included === 1 ? '-' : '¥' + it.extraPrice }}
                  </span>
                </div>
              </div>
            </div>

            <div v-if="selPkgId" class="detail-panel__section detail-panel__order">
              <h4 class="section-label">确认下单</h4>

              <div class="form-group">
                <label>选择宠物</label>
                <select v-model.number="orderForm.petId">
                  <option :value="null" disabled>请选择您的宠物</option>
                  <option v-for="p in myPets" :key="p.id" :value="p.id">{{ p.name }} ({{ p.breed }})</option>
                </select>
              </div>

              <div class="form-group">
                <label>购买月数</label>
                <div class="stepper">
                  <button class="stepper__btn" @click="orderForm.months = Math.max(1, orderForm.months - 1)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  </button>
                  <input
                    v-model.number="orderForm.months"
                    type="number"
                    class="stepper__input"
                    min="1"
                    :max="selCourse.duration"
                  />
                  <button class="stepper__btn" @click="orderForm.months = Math.min(selCourse.duration, orderForm.months + 1)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  </button>
                </div>
              </div>

              <div class="coupon-section">
                <button class="coupon-toggle" @click="showCouponPicker = !showCouponPicker">
                  <span>🎟️ 可用优惠券</span>
                  <span v-if="availableCoupons.length" class="coupon-badge">{{ availableCoupons.length }}</span>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" :style="{ transform: showCouponPicker ? 'rotate(180deg)' : '' }"><polyline points="6 9 12 15 18 9"/></svg>
                </button>

                <div v-if="selectedCoupon" class="selected-coupon">
                  <div class="selected-coupon__info">
                    <span class="selected-coupon__name">{{ selectedCoupon.couponName || selectedCoupon.name }}</span>
                    <span class="selected-coupon__discount">-¥{{ discountAmount }}</span>
                  </div>
                  <button class="btn btn-ghost btn-sm" @click="removeCoupon">取消</button>
                </div>

                <transition name="picker-slide">
                  <div v-if="showCouponPicker" class="coupon-picker">
                    <div v-if="availableCoupons.length" class="coupon-picker__list">
                      <div
                        v-for="uc in availableCoupons"
                        :key="uc.id"
                        class="coupon-picker__item"
                        :class="{ 'coupon-picker__item--selected': selectedCoupon && selectedCoupon.id === uc.id, 'coupon-picker__item--unavailable': !isCouponAvailable(uc) }"
                        @click="isCouponAvailable(uc) && selectCoupon(uc)"
                      >
                        <div class="coupon-picker__value">
                          <template v-if="getCouponField(uc, 'discountType') === 1">¥{{ getCouponField(uc, 'discountValue') }}</template>
                          <template v-else>{{ getCouponField(uc, 'discountValue') }}折</template>
                        </div>
                        <div class="coupon-picker__detail">
                          <span class="coupon-picker__name">{{ uc.couponName || uc.name }}</span>
                          <span class="coupon-picker__condition">{{ (getCouponField(uc, 'minAmount') || 0) > 0 ? '满' + getCouponField(uc, 'minAmount') + '可用' : '无门槛' }}</span>
                        </div>
                        <span v-if="!isCouponAvailable(uc)" class="coupon-picker__reason">未满最低消费</span>
                      </div>
                    </div>
                    <div v-else class="coupon-picker__empty">暂无可用优惠券</div>
                  </div>
                </transition>
              </div>

              <div class="price-display">
                <div v-if="selectedCoupon" class="price-display__original">
                  <span class="price-display__label">原价</span>
                  <span class="price-display__original-val">¥{{ originalPrice }}</span>
                </div>
                <div v-if="selectedCoupon" class="price-display__discount">
                  <span class="price-display__label">优惠</span>
                  <span class="price-display__discount-val">-¥{{ discountAmount }}</span>
                </div>
                <div class="price-display__final">
                  <span class="price-display__label">{{ selectedCoupon ? '实付' : '预估总价' }}</span>
                  <div class="price-display__value">
                    <span class="price-display__symbol">¥</span>
                    <span class="price-display__amount">{{ finalPrice }}</span>
                  </div>
                </div>
                <span class="price-display__hint">最终价格以提交时为准</span>
              </div>

              <button class="btn btn-primary btn-accent" :disabled="submitting" @click="placeOrder">
                {{ submitting ? '提交中...' : '确认下单' }}
              </button>

              <div v-if="orderMsg" :class="['alert', orderMsgType === 'success' ? 'alert-success' : 'alert-error']" style="margin-top: var(--space-3)">
                {{ orderMsg }}
              </div>
            </div>
          </div>
        </div>
      </transition>

      <div v-if="!selCourse" class="empty-panel">
        <div class="empty-panel__illustration">
          <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
            <circle cx="40" cy="40" r="36" fill="var(--color-primary-bg)" />
            <circle cx="40" cy="40" r="28" fill="var(--color-accent-bg)" />
            <text x="40" y="48" text-anchor="middle" font-size="28">🐾</text>
          </svg>
        </div>
        <p class="empty-panel__text">请从左侧选择课程</p>
        <p class="empty-panel__hint">点击课程卡片查看详情并下单</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { courseApi, orderApi, petApi, couponApi } from '../../api'

const router = useRouter()
const user = computed(() => { try { return JSON.parse(localStorage.getItem('client_user')) || {} } catch { return {} } })

const categories = ref([])
const selectedCat = ref(null)
const courses = ref([])
const selCourse = ref(null)
const packages = ref([])
const items = ref([])
const selPkgId = ref(null)
const selPkgPrice = ref(0)
const myPets = ref([])
const submitting = ref(false)
const orderMsg = ref('')
const orderMsgType = ref('success')

const orderForm = ref({ petId: null, months: 1 })

const availableCoupons = ref([])
const selectedCoupon = ref(null)
const discountAmount = ref(0)
const showCouponPicker = ref(false)

const originalPrice = computed(() => {
  if (!selPkgPrice.value || !orderForm.value.months) return '0.00'
  return (selPkgPrice.value * orderForm.value.months).toFixed(2)
})

const finalPrice = computed(() => {
  const original = parseFloat(originalPrice.value)
  const discount = parseFloat(discountAmount.value) || 0
  return Math.max(0, original - discount).toFixed(2)
})

const estimatedPrice = computed(() => finalPrice.value)

function getCouponField(uc, field) {
  return uc[field] || uc.coupon?.[field]
}

function isCouponAvailable(uc) {
  const minAmount = getCouponField(uc, 'minAmount') || 0
  return parseFloat(originalPrice.value) >= minAmount
}

async function loadAvailableCoupons() {
  if (!user.value.id || !selPkgId.value) return
  try {
    const res = await couponApi.availableForOrder({
      userId: user.value.id,
      orderType: 'course',
      orderAmount: originalPrice.value,
      scopeId: selCourse.value?.id
    })
    if (res.code === 200) availableCoupons.value = res.data || []
  } catch {
    availableCoupons.value = []
  }
}

async function selectCoupon(uc) {
  if (!isCouponAvailable(uc)) return
  selectedCoupon.value = uc
  showCouponPicker.value = false
  try {
    const res = await couponApi.calculate({
      couponId: uc.id,
      orderType: 'course',
      orderAmount: originalPrice.value
    })
    if (res.code === 200) {
      discountAmount.value = res.data?.discountAmount || 0
    } else {
      calcDiscountLocally(uc)
    }
  } catch {
    calcDiscountLocally(uc)
  }
}

function calcDiscountLocally(uc) {
  const original = parseFloat(originalPrice.value)
  const dtype = getCouponField(uc, 'discountType')
  const dval = getCouponField(uc, 'discountValue')
  if (dtype === 1) {
    discountAmount.value = Math.min(dval, original)
  } else {
    let disc = original * (1 - dval / 100)
    const maxD = getCouponField(uc, 'maxDiscount')
    if (maxD && disc > maxD) disc = maxD
    discountAmount.value = Math.min(disc, original)
  }
  discountAmount.value = discountAmount.value.toFixed(2)
}

function removeCoupon() {
  selectedCoupon.value = null
  discountAmount.value = 0
}

watch([selPkgId, () => orderForm.value.months], () => {
  removeCoupon()
  loadAvailableCoupons()
})

async function loadCategories() {
  try {
    const res = await courseApi.categoryList()
    if (res.code === 200) categories.value = res.data
  } catch (e) {}
}

async function selectCat(id) {
  selectedCat.value = id
  try {
    const res = id ? await courseApi.getByCategory(id) : await courseApi.listAll()
    if (res.code === 200) courses.value = res.data
  } catch (e) {}
  selCourse.value = null
}

async function showPackages(c) {
  selCourse.value = c
  selPkgId.value = null
  items.value = []
  orderForm.value.months = c.duration
  removeCoupon()
  try {
    const res = await courseApi.packageList(c.id)
    if (res.code === 200) packages.value = res.data
  } catch (e) {}
}

async function selectPackage(pkg) {
  selPkgId.value = pkg.id
  selPkgPrice.value = pkg.price
  try {
    const res = await courseApi.packageItems(pkg.id)
    if (res.code === 200) items.value = res.data
  } catch (e) {}
}

async function loadMyPets() {
  if (!user.value.id) return
  try {
    const res = await petApi.getByUserId(user.value.id)
    if (res.code === 200) myPets.value = res.data
  } catch (e) {}
}

async function placeOrder() {
  if (!orderForm.value.petId) { orderMsg.value = '请选择宠物'; orderMsgType.value = 'error'; return }
  if (!selPkgId.value) { orderMsg.value = '请选择套餐'; orderMsgType.value = 'error'; return }
  if (!orderForm.value.months || orderForm.value.months < 1) { orderMsg.value = '请输入有效月数'; orderMsgType.value = 'error'; return }

  submitting.value = true
  orderMsg.value = ''
  try {
    const payload = {
      petId: orderForm.value.petId,
      packageId: selPkgId.value,
      months: orderForm.value.months,
      couponId: selectedCoupon.value?.id || null
    }
    const res = await orderApi.courseCreate(payload)
    if (res.code === 200) {
      orderMsg.value = '下单成功！即将跳转到我的订单...'
      orderMsgType.value = 'success'
      setTimeout(() => router.push('/client/order'), 1000)
    } else {
      orderMsg.value = res.message || '下单失败'
      orderMsgType.value = 'error'
    }
  } catch (e) {
    orderMsg.value = '下单请求失败'
    orderMsgType.value = 'error'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await selectCat(null)
  await loadMyPets()
})
</script>

<style scoped>
.course-page {
  min-height: 100%;
}

.course-header {
  margin-bottom: var(--space-5);
}

.course-subtitle {
  color: var(--text-muted);
  font-size: var(--font-size-base);
  margin-top: var(--space-1);
}

.category-bar {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-5);
}

.cat-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 18px;
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

.cat-pill:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.cat-pill--active {
  background: var(--color-primary);
  color: var(--text-inverse);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-button);
}

.cat-pill--active:hover {
  background: var(--color-primary-dark);
  color: var(--text-inverse);
}

.course-layout {
  display: flex;
  gap: var(--space-5);
  align-items: flex-start;
}

.course-grid {
  flex: 1;
  min-width: 0;
  transition: all var(--transition-slow);
}

.course-grid--narrow {
  flex: 1;
}

.grid-inner {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.course-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1.5px solid var(--border-light);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: all var(--transition-base);
  overflow: hidden;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--color-primary-bg);
}

.course-card--selected {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card-hover), 0 0 0 3px var(--color-primary-bg);
}

.course-card__body {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.course-card__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-2);
}

.course-card__name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  line-height: 1.3;
}

.course-card__duration {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  background: var(--color-accent-bg);
  color: #92400e;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
  flex-shrink: 0;
}

.course-card__desc {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-light);
}

.course-card__price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.course-card__price-val {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  letter-spacing: -0.02em;
}

.course-card__price-unit {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.empty-state {
  text-align: center;
  padding: var(--space-7) var(--space-4);
  grid-column: 1 / -1;
}

.empty-state__icon {
  font-size: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.6;
}

.empty-state__text {
  font-size: var(--font-size-md);
  color: var(--text-muted);
  margin-bottom: var(--space-1);
}

.empty-state__hint {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  opacity: 0.7;
}

.detail-panel {
  width: 420px;
  flex-shrink: 0;
  position: sticky;
  top: var(--space-5);
  max-height: calc(100vh - var(--space-6));
  overflow-y: auto;
}

.detail-panel__inner {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1.5px solid var(--border-light);
  box-shadow: var(--shadow-md);
  padding: var(--space-5);
  position: relative;
}

.detail-panel__close {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  border: none;
  background: var(--bg-hover);
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.detail-panel__close:hover {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

.detail-panel__header {
  margin-bottom: var(--space-5);
  padding-right: var(--space-6);
}

.detail-panel__title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin-bottom: var(--space-2);
}

.detail-panel__meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.detail-panel__price {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.detail-panel__price small {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-normal);
  color: var(--text-muted);
}

.detail-panel__section {
  margin-bottom: var(--space-5);
}

.section-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin-bottom: var(--space-3);
  letter-spacing: 0.01em;
}

.pkg-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.pkg-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  background: var(--bg-card);
}

.pkg-card:hover {
  border-color: var(--color-primary-light);
  background: var(--color-primary-bg);
}

.pkg-card--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  box-shadow: 0 0 0 3px rgba(79, 124, 255, 0.1);
}

.pkg-card__info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.pkg-card__name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.pkg-card__price {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.pkg-card__check {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-hint {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
  text-align: center;
  padding: var(--space-4);
}

.items-table {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.items-table__head {
  display: grid;
  grid-template-columns: 1fr 80px 80px;
  padding: var(--space-2) var(--space-3);
  background: var(--bg-hover);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.items-table__row {
  display: grid;
  grid-template-columns: 1fr 80px 80px;
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-sm);
  color: var(--text-body);
  border-top: 1px solid var(--border-light);
  align-items: center;
}

.text-muted {
  color: var(--text-muted);
}

.text-price {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.detail-panel__order {
  background: var(--bg-page);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  margin-left: calc(var(--space-5) * -1);
  margin-right: calc(var(--space-5) * -1);
  margin-bottom: calc(var(--space-5) * -1);
  border-top: 1px solid var(--border-light);
  padding-bottom: var(--space-5);
}

.stepper {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-input);
  width: fit-content;
}

.stepper__btn {
  width: 40px;
  height: 40px;
  border: none;
  background: var(--bg-hover);
  color: var(--text-title);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
  flex-shrink: 0;
}

.stepper__btn:hover {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.stepper__btn:active {
  background: var(--border-color);
}

.stepper__input {
  width: 56px;
  height: 40px;
  border: none;
  border-left: 1px solid var(--border-color);
  border-right: 1px solid var(--border-color);
  text-align: center;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family);
  color: var(--text-title);
  background: var(--bg-card);
  outline: none;
  -moz-appearance: textfield;
}

.stepper__input::-webkit-outer-spin-button,
.stepper__input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.coupon-section {
  margin-bottom: var(--space-4);
}

.coupon-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-input);
  color: var(--text-title);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family);
  cursor: pointer;
  transition: all var(--transition-base);
}

.coupon-toggle:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}

.coupon-toggle svg {
  margin-left: auto;
  transition: transform var(--transition-base);
}

.coupon-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: var(--radius-full);
  background: var(--color-danger);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
}

.selected-coupon {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--color-primary-bg);
  border: 1px solid rgba(79, 124, 255, 0.2);
  border-radius: var(--radius-md);
}

.selected-coupon__info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.selected-coupon__name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.selected-coupon__discount {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
}

.coupon-picker {
  margin-top: var(--space-2);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  max-height: 240px;
  overflow-y: auto;
}

.coupon-picker__list {
  padding: var(--space-2);
}

.coupon-picker__item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.coupon-picker__item:hover {
  background: var(--bg-hover);
}

.coupon-picker__item--selected {
  background: var(--color-primary-bg);
  border-color: var(--color-primary);
}

.coupon-picker__item--unavailable {
  opacity: 0.45;
  cursor: not-allowed;
}

.coupon-picker__value {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  min-width: 50px;
  text-align: center;
}

.coupon-picker__detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.coupon-picker__name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-picker__condition {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.coupon-picker__reason {
  margin-left: auto;
  font-size: var(--font-size-xs);
  color: var(--color-danger);
  white-space: nowrap;
}

.coupon-picker__empty {
  padding: var(--space-5);
  text-align: center;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.picker-slide-enter-active {
  animation: pickerSlide 0.2s ease;
}

.picker-slide-leave-active {
  animation: pickerSlide 0.15s ease reverse;
}

@keyframes pickerSlide {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

.price-display {
  text-align: center;
  padding: var(--space-4);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
  border: 1px solid var(--border-light);
}

.price-display__original {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
}

.price-display__label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.price-display__original-val {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  text-decoration: line-through;
}

.price-display__discount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.price-display__discount-val {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
}

.price-display__final {
  margin-bottom: var(--space-1);
}

.price-display__value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
}

.price-display__symbol {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.price-display__amount {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  letter-spacing: -0.03em;
  transition: all var(--transition-base);
}

.price-display__hint {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.detail-panel__order .btn {
  width: 100%;
  padding: 12px;
  font-size: var(--font-size-md);
}

.empty-panel {
  width: 420px;
  flex-shrink: 0;
  position: sticky;
  top: var(--space-5);
}

.empty-panel__illustration {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-4);
}

.empty-panel__text {
  text-align: center;
  font-size: var(--font-size-md);
  color: var(--text-muted);
  margin-bottom: var(--space-1);
}

.empty-panel__hint {
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  opacity: 0.7;
}

.panel-slide-enter-active {
  animation: slideInRight var(--transition-slow);
}

.panel-slide-leave-active {
  animation: slideInRight var(--transition-base) reverse;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(24px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@media (max-width: 1100px) {
  .grid-inner {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-panel,
  .empty-panel {
    width: 360px;
  }
}

@media (max-width: 860px) {
  .course-layout {
    flex-direction: column;
  }

  .detail-panel,
  .empty-panel {
    width: 100%;
    position: static;
    max-height: none;
  }

  .grid-inner {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .grid-inner {
    grid-template-columns: 1fr;
  }
}
</style>
