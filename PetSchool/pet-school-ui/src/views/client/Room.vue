<template>
  <div class="room-page">
    <div class="room-header">
      <h1 class="page-title">🐾 宠物寄养客房</h1>
      <p class="page-subtitle">为您的爱宠提供舒适温馨的入住体验</p>
    </div>

    <div class="room-layout">
      <div class="room-type-list">
        <div
          v-for="rt in roomTypes"
          :key="rt.id"
          class="room-type-card card"
          :class="{
            active: selType?.id === rt.id,
            unavailable: !rt.available,
            selected: selType?.id === rt.id
          }"
          @click="rt.available && selectType(rt)"
        >
          <div class="rt-top">
            <h3 class="rt-name">{{ rt.name }}</h3>
            <span
              class="availability-badge"
              :class="rt.available ? 'badge-available' : 'badge-full'"
            >
              {{ rt.available ? '可入住' : '已满房' }}
            </span>
          </div>
          <p class="rt-facilities">{{ rt.facilities || rt.description }}</p>
          <div class="rt-bottom">
            <span class="rt-price price-lg">¥{{ rt.price }}<span class="price-unit">/天</span></span>
            <button
              v-if="rt.available"
              class="btn btn-primary btn-sm"
              @click.stop="selectType(rt)"
            >
              选择入住
            </button>
            <span v-else class="full-text">已满房</span>
          </div>
        </div>
      </div>

      <div class="room-detail-panel">
        <template v-if="selType">
          <div class="detail-header">
            <h2 class="detail-title">{{ selType.name }}</h2>
            <p class="detail-info">{{ selType.facilities || selType.description }}</p>
          </div>

          <div class="room-grid-section">
            <h4 class="section-label">选择房间</h4>
            <div class="room-grid">
              <div
                v-for="r in rooms"
                :key="r.id"
                class="room-cell"
                :class="{
                  'room-occupied': r.status === 'occupied',
                  'room-available': r.status !== 'occupied',
                  'room-selected': selRoom?.id === r.id
                }"
                @click="r.status !== 'occupied' && selectRoom(r)"
              >
                <span class="room-number">{{ r.roomNumber }}</span>
                <span class="room-status-text">{{ r.status === 'occupied' ? '已住' : '空闲' }}</span>
              </div>
            </div>
          </div>

          <div class="order-form-section">
            <h4 class="section-label">预订信息</h4>
            <div class="order-form">
              <div class="form-group">
                <label>选择宠物</label>
                <select v-model="form.petId">
                  <option :value="null" disabled>请选择宠物</option>
                  <option v-for="p in myPets" :key="p.id" :value="p.id">{{ p.name }}</option>
                </select>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>入住日期</label>
                  <input type="date" v-model="form.checkIn" :min="today" />
                </div>
                <div class="form-group">
                  <label>退房日期</label>
                  <input type="date" v-model="form.checkOut" :min="form.checkIn || today" />
                </div>
              </div>
            </div>
          </div>

          <div v-if="selRoom && days > 0" class="coupon-section">
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

          <div v-if="selRoom && days > 0" class="price-summary">
            <div class="summary-row">
              <span class="summary-label">房间号</span>
              <span class="summary-value">{{ selRoom.roomNumber }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">入住天数</span>
              <span class="summary-value">{{ days }} 天</span>
            </div>
            <div v-if="selectedCoupon" class="summary-row">
              <span class="summary-label">原价</span>
              <span class="summary-original">¥{{ originalPrice }}</span>
            </div>
            <div v-if="selectedCoupon" class="summary-row">
              <span class="summary-label">优惠</span>
              <span class="summary-discount">-¥{{ discountAmount }}</span>
            </div>
            <div class="summary-row total-row">
              <span class="summary-label">{{ selectedCoupon ? '实付' : '预估总价' }}</span>
              <span class="summary-price price-lg">¥{{ finalPrice }}</span>
            </div>
          </div>

          <div v-if="msg" :class="['alert', msgType === 'success' ? 'alert-success' : 'alert-error']">
            {{ msg }}
          </div>

          <button
            class="btn btn-primary submit-btn"
            :disabled="submitting"
            @click="placeOrder"
          >
            {{ submitting ? '提交中...' : '确认预订' }}
          </button>
        </template>

        <div v-else class="empty-state">
          <div class="empty-icon">🏨</div>
          <h3 class="empty-title">请选择房型</h3>
          <p class="empty-desc">从左侧选择一个房型，查看可用房间并预订</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { roomApi, petApi, couponApi } from '../../api'

const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('client_user') || 'null'))

const roomTypes = ref([])
const selType = ref(null)
const rooms = ref([])
const selRoom = ref(null)
const myPets = ref([])
const submitting = ref(false)
const msg = ref('')
const msgType = ref('')

const today = new Date().toISOString().slice(0, 10)
const form = ref({ petId: null, checkIn: '', checkOut: '' })

const availableCoupons = ref([])
const selectedCoupon = ref(null)
const discountAmount = ref(0)
const showCouponPicker = ref(false)

const days = computed(() => {
  if (!form.value.checkIn || !form.value.checkOut) return 0
  const diff = new Date(form.value.checkOut) - new Date(form.value.checkIn)
  const d = Math.ceil(diff / (1000 * 60 * 60 * 24))
  return d > 0 ? d : 0
})

const originalPrice = computed(() => {
  if (!selType.value || days.value <= 0) return '0.00'
  return (selType.value.price * days.value).toFixed(2)
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
  if (!user.value?.id || !selRoom.value) return
  try {
    const res = await couponApi.availableForOrder({
      userId: user.value.id,
      orderType: 'boarding',
      orderAmount: originalPrice.value,
      scopeId: null
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
      orderType: 'boarding',
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

watch([selRoom, days], () => {
  removeCoupon()
  if (selRoom.value && days.value > 0) {
    loadAvailableCoupons()
  }
})

async function loadRoomTypes() {
  try {
    const res = await roomApi.typeList()
    const list = res.data || res
    roomTypes.value = (Array.isArray(list) ? list : []).map(rt => ({
      ...rt,
      price: rt.price || rt.dailyPrice,
      available: rt.capacity === null || rt.capacity === undefined || rt.capacity > 0
    }))
  } catch (e) {
    console.error(e)
  }
}

function selectType(rt) {
  selType.value = rt
  selRoom.value = null
  form.value = { petId: null, checkIn: '', checkOut: '' }
  msg.value = ''
  removeCoupon()
  loadRooms()
}

async function loadRooms() {
  if (!selType.value) return
  try {
    const res = await roomApi.roomList(selType.value.id)
    rooms.value = res.data || res
  } catch (e) {
    console.error(e)
  }
}

function selectRoom(r) {
  selRoom.value = r
}

async function loadMyPets() {
  try {
    const res = await petApi.getByUserId(user.value?.id)
    myPets.value = res.data || res
  } catch (e) {
    console.error(e)
  }
}

async function placeOrder() {
  if (!form.value.petId) { msg.value = '请选择宠物'; msgType.value = 'error'; return }
  if (!selRoom.value) { msg.value = '请选择房间'; msgType.value = 'error'; return }
  if (!form.value.checkIn) { msg.value = '请选择入住日期'; msgType.value = 'error'; return }
  if (!form.value.checkOut) { msg.value = '请选择退房日期'; msgType.value = 'error'; return }
  if (days.value <= 0) { msg.value = '退房日期须晚于入住日期'; msgType.value = 'error'; return }

  submitting.value = true
  msg.value = ''
  try {
    await roomApi.boardingCreate({
      petId: form.value.petId,
      roomId: selRoom.value.id,
      checkIn: form.value.checkIn,
      checkOut: form.value.checkOut,
      couponId: selectedCoupon.value?.id || null
    })
    msg.value = '预订成功！'
    msgType.value = 'success'
    selRoom.value = null
    form.value = { petId: null, checkIn: '', checkOut: '' }
    removeCoupon()
    loadRooms()
  } catch (e) {
    msg.value = e.response?.data?.message || '预订失败，请重试'
    msgType.value = 'error'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadRoomTypes()
  loadMyPets()
})
</script>

<style scoped>
.room-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: var(--space-6) var(--space-8);
}

.room-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.page-title {
  font-size: var(--font-size-2xl);
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
}

.room-layout {
  display: flex;
  gap: var(--space-6);
  align-items: flex-start;
}

.room-type-list {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.room-type-card {
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base), border-color var(--transition-base);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.room-type-card.unavailable {
  opacity: 0.55;
  cursor: not-allowed;
  filter: grayscale(0.3);
}

.room-type-card:not(.unavailable):hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
}

.room-type-card.selected {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary), var(--shadow-card-hover);
}

.rt-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.rt-name {
  font-size: var(--font-size-xl);
  color: var(--text-title);
  margin: 0;
}

.availability-badge {
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.5px;
}

.badge-available {
  color: var(--color-success);
  background: var(--color-success-bg);
  border: 1px solid var(--color-success-light);
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.2);
}

.badge-full {
  color: var(--text-muted);
  background: var(--bg-hover);
  border: 1px solid var(--border-color);
}

.rt-facilities {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin: 0 0 var(--space-4) 0;
  line-height: 1.6;
}

.rt-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rt-price {
  color: var(--color-accent);
  font-weight: var(--font-weight-bold);
}

.price-unit {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-normal);
  color: var(--text-muted);
  margin-left: 2px;
}

.full-text {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  font-weight: var(--font-weight-medium);
}

.room-detail-panel {
  flex: 1;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-7);
  min-height: 600px;
  position: sticky;
  top: var(--space-6);
}

.detail-header {
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.detail-title {
  font-size: var(--font-size-xl);
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
}

.detail-info {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin: 0;
  line-height: 1.6;
}

.section-label {
  font-size: var(--font-size-sm);
  color: var(--text-title);
  font-weight: var(--font-weight-semibold);
  margin: 0 0 var(--space-3) 0;
}

.room-grid-section {
  margin-bottom: var(--space-6);
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: var(--space-3);
}

.room-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-3) var(--space-2);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 2px solid transparent;
  user-select: none;
}

.room-available {
  background: var(--bg-card);
  border-color: var(--color-success-light);
}

.room-available:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-50);
  transform: translateY(-1px);
}

.room-occupied {
  background: var(--bg-hover);
  cursor: not-allowed;
  opacity: 0.6;
}

.room-selected {
  border-color: var(--color-primary) !important;
  background: var(--color-primary-bg) !important;
  box-shadow: 0 0 0 1px var(--color-primary);
}

.room-number {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
}

.room-occupied .room-number {
  color: var(--color-danger);
}

.room-available .room-number {
  color: var(--color-success);
}

.room-selected .room-number {
  color: var(--color-primary);
}

.room-status-text {
  font-size: var(--font-size-xs);
  margin-top: var(--space-1);
}

.room-occupied .room-status-text {
  color: var(--color-danger);
}

.room-available .room-status-text {
  color: var(--color-success);
}

.order-form-section {
  margin-bottom: var(--space-6);
}

.order-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.form-row {
  display: flex;
  gap: var(--space-4);
}

.form-row .form-group {
  flex: 1;
}

.form-group label {
  display: block;
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin-bottom: var(--space-1);
  font-weight: var(--font-weight-medium);
}

.form-group select,
.form-group input {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--text-title);
  background: var(--bg-card);
  transition: border-color var(--transition-base);
  outline: none;
  box-sizing: border-box;
}

.form-group select:focus,
.form-group input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-bg);
}

.coupon-section {
  margin-bottom: var(--space-5);
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
  color: var(--text-inverse);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  line-height: 1;
}

.selected-coupon {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--color-primary-bg);
  border: 1px solid var(--color-primary-200);
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

.price-summary {
  background: linear-gradient(135deg, var(--color-primary-50), var(--color-accent-bg));
  border-radius: var(--radius-md);
  padding: var(--space-5);
  margin-bottom: var(--space-5);
  border: 1px solid var(--color-primary-100);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) 0;
}

.summary-label {
  font-size: var(--font-size-sm);
  color: var(--text-body);
}

.summary-value {
  font-size: var(--font-size-sm);
  color: var(--text-title);
  font-weight: var(--font-weight-semibold);
}

.summary-original {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  text-decoration: line-through;
}

.summary-discount {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
}

.total-row {
  border-top: 1px dashed var(--border-light);
  margin-top: var(--space-2);
  padding-top: var(--space-3);
}

.summary-price {
  color: var(--color-accent);
  animation: pricePop 0.4s ease;
}

@keyframes pricePop {
  0% { transform: scale(0.8); opacity: 0; }
  50% { transform: scale(1.08); }
  100% { transform: scale(1); opacity: 1; }
}

.submit-btn {
  width: 100%;
  padding: var(--space-3);
  font-size: var(--font-size-base);
  border-radius: var(--radius-md);
  font-weight: var(--font-weight-semibold);
  margin-top: var(--space-2);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 480px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--space-4);
  opacity: 0.6;
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
</style>
