<template>
  <div class="coupon-page">
    <div class="page-header">
      <h1 class="page-title">🎟️ 优惠券管理</h1>
      <button class="btn btn-primary" @click="openCreate">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新建优惠券
      </button>
    </div>

    <div class="search-card card">
      <div class="search-row">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input v-model="search.keyword" class="search-input" placeholder="搜索优惠券名称…" @keyup.enter="doSearch" />
        </div>
        <select v-model="search.type" class="filter-select">
          <option value="">全部类型</option>
          <option value="1">课程券</option>
          <option value="2">寄宿券</option>
          <option value="3">通用券</option>
        </select>
        <select v-model="search.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="1">启用</option>
          <option value="0">禁用</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="doSearch">搜索</button>
        <button class="btn btn-secondary btn-sm" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon--primary">🎫</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总券数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--success">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-label">启用中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--accent">🎁</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.claimed }}</div>
          <div class="stat-label">已领取总量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--warning">⏰</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.expiring }}</div>
          <div class="stat-label">即将过期</div>
        </div>
      </div>
    </div>

    <div class="card table-card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>优惠方式</th>
              <th>优惠力度</th>
              <th>门槛</th>
              <th>适用范围</th>
              <th>库存</th>
              <th>已领取</th>
              <th>有效期</th>
              <th>状态</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!coupons.length">
              <td colspan="11">
                <div class="empty-state">
                  <div class="empty-icon">🎫</div>
                  <div class="empty-text">暂无优惠券数据</div>
                </div>
              </td>
            </tr>
            <tr v-for="c in coupons" :key="c.id" class="table-row">
              <td class="td-name">{{ c.name }}</td>
              <td><span :class="['tag', typeTagClass(c.type)]">{{ typeText(c.type) }}</span></td>
              <td>{{ c.discountType === 1 ? '满减' : '折扣' }}</td>
              <td class="td-value">{{ discountDisplay(c) }}</td>
              <td>{{ c.minAmount > 0 ? '¥' + c.minAmount : '无门槛' }}</td>
              <td>{{ scopeText(c.scopeType) }}</td>
              <td>{{ c.stock === null || c.stock === -1 ? '无限' : c.stock }}</td>
              <td>{{ c.claimedCount || 0 }}</td>
              <td class="td-date">{{ formatDate(c.startTime) }} ~ {{ formatDate(c.endTime) }}</td>
              <td><span :class="['tag', c.status === 1 ? 'tag-success' : 'tag-danger']">{{ c.status === 1 ? '启用' : '禁用' }}</span></td>
              <td class="col-actions">
                <div class="action-group">
                  <button class="btn btn-ghost btn-sm" @click="handleEdit(c)">编辑</button>
                  <button class="btn btn-accent btn-sm" @click="openSend(c)">发券</button>
                  <button :class="['btn', 'btn-sm', c.status === 1 ? 'btn-secondary' : 'btn-primary']" @click="toggleStatus(c)">{{ c.status === 1 ? '禁用' : '启用' }}</button>
                  <button class="btn btn-danger btn-sm" @click="handleDelete(c.id)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button class="page-pill" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">&laquo;</button>
        <button v-for="p in visiblePages" :key="p" class="page-pill" :class="{ active: p === currentPage }" @click="goPage(p)">{{ p }}</button>
        <button class="page-pill" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">&raquo;</button>
      </div>
    </div>

    <div v-if="showCreateForm" class="modal-overlay" @click.self="showCreateForm = false">
      <div class="modal-content form-modal">
        <div class="modal-header">
          <h3>{{ editMode ? '编辑优惠券' : '新建优惠券' }}</h3>
          <button class="btn-icon modal-close" @click="showCreateForm = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="form-body">
          <div class="form-group">
            <label>优惠券名称</label>
            <input v-model="couponForm.name" placeholder="如：满3000减300" />
          </div>
          <div class="form-row">
            <div class="form-group col">
              <label>类型</label>
              <select v-model.number="couponForm.type">
                <option :value="1">课程券</option>
                <option :value="2">寄宿券</option>
                <option :value="3">通用券</option>
              </select>
            </div>
            <div class="form-group col">
              <label>优惠方式</label>
              <select v-model.number="couponForm.discountType" @change="onDiscountTypeChange">
                <option :value="1">满减</option>
                <option :value="2">折扣</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group col">
              <label>{{ couponForm.discountType === 1 ? '减免金额' : '折扣百分比(如80表示8折)' }}</label>
              <input v-model.number="couponForm.discountValue" type="number" :placeholder="couponForm.discountType === 1 ? '输入减免金额' : '输入折扣百分比'" />
            </div>
            <div class="form-group col">
              <label>最低消费</label>
              <input v-model.number="couponForm.minAmount" type="number" placeholder="0表示无门槛" />
            </div>
          </div>
          <div v-if="couponForm.discountType === 2" class="form-group">
            <label>最大优惠金额</label>
            <input v-model.number="couponForm.maxDiscount" type="number" placeholder="留空表示无限制" />
          </div>
          <div class="form-row">
            <div class="form-group col">
              <label>适用范围</label>
              <select v-model.number="couponForm.scopeType">
                <option :value="1">全部</option>
                <option :value="2">指定课程</option>
                <option :value="3">仅寄宿</option>
              </select>
            </div>
            <div v-if="couponForm.scopeType === 2" class="form-group col">
              <label>指定课程ID</label>
              <input v-model="couponForm.scopeId" placeholder="输入课程ID" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group col">
              <label>有效期开始</label>
              <input v-model="couponForm.startTime" type="datetime-local" />
            </div>
            <div class="form-group col">
              <label>有效期结束</label>
              <input v-model="couponForm.endTime" type="datetime-local" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group col">
              <label>库存</label>
              <input v-model.number="couponForm.stock" type="number" placeholder="留空表示无限" />
            </div>
            <div class="form-group col">
              <label>每人限领</label>
              <input v-model.number="couponForm.perLimit" type="number" placeholder="默认1" />
            </div>
          </div>
          <div class="form-group">
            <label>说明</label>
            <textarea v-model="couponForm.description" placeholder="优惠券使用说明（选填）" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="couponForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showCreateForm = false">取消</button>
          <button class="btn btn-primary" @click="handleSave">{{ editMode ? '保存修改' : '创建优惠券' }}</button>
        </div>
        <div v-if="formMsg" :class="['alert', formMsgType === 'success' ? 'alert-success' : 'alert-error']" style="margin: var(--space-3) var(--space-5) 0">{{ formMsg }}</div>
      </div>
    </div>

    <div v-if="showSendModal" class="modal-overlay" @click.self="showSendModal = false">
      <div class="modal-content send-modal">
        <div class="modal-header">
          <h3>发放优惠券 — {{ sendCoupon?.name }}</h3>
          <button class="btn-icon modal-close" @click="showSendModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="send-body">
          <div class="send-option">
            <button class="btn btn-primary btn-lg send-all-btn" @click="sendToAll" :disabled="sending">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              发给全部用户
            </button>
          </div>
          <div class="send-divider"><span>或指定用户</span></div>
          <div class="form-group">
            <label>用户ID</label>
            <div class="send-user-row">
              <input v-model.number="sendTarget" type="number" placeholder="输入用户ID" />
              <button class="btn btn-accent" @click="sendToUser" :disabled="sending || !sendTarget">发送</button>
            </div>
          </div>
          <div v-if="sendMsg" :class="['alert', sendMsgType === 'success' ? 'alert-success' : 'alert-error']">{{ sendMsg }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi, couponApi } from '../../api'

const coupons = ref([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(0)
const pageSize = 10
const search = ref({ keyword: '', type: '', status: '' })

const stats = ref({ total: 0, active: 0, claimed: 0, expiring: 0 })

const showCreateForm = ref(false)
const editMode = ref(false)
const formMsg = ref('')
const formMsgType = ref('success')

const couponForm = ref({
  id: null,
  name: '',
  type: 1,
  discountType: 1,
  discountValue: null,
  minAmount: 0,
  maxDiscount: null,
  scopeType: 1,
  scopeId: '',
  startTime: '',
  endTime: '',
  stock: null,
  perLimit: 1,
  description: '',
  status: 1
})

const showSendModal = ref(false)
const sendCoupon = ref(null)
const sendTarget = ref(null)
const sending = ref(false)
const sendMsg = ref('')
const sendMsgType = ref('success')

const visiblePages = computed(() => {
  const pages = []
  const c = currentPage.value
  const t = totalPages.value
  let start = Math.max(1, c - 2)
  let end = Math.min(t, c + 2)
  if (end - start < 4) {
    if (start === 1) end = Math.min(t, start + 4)
    else start = Math.max(1, end - 4)
  }
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

function typeText(t) {
  return { 1: '课程券', 2: '寄宿券', 3: '通用券' }[t] || '未知'
}

function typeTagClass(t) {
  return { 1: 'tag-primary', 2: 'tag-accent', 3: 'tag-success' }[t] || ''
}

function scopeText(s) {
  return { 1: '全部', 2: '指定课程', 3: '仅寄宿' }[s] || '未知'
}

function discountDisplay(c) {
  if (c.discountType === 1) return '¥' + c.discountValue
  return c.discountValue + '折'
}

function formatDate(d) {
  if (!d) return '-'
  const dt = new Date(d)
  const pad = n => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`
}

function resetForm() {
  couponForm.value = {
    id: null, name: '', type: 1, discountType: 1, discountValue: null,
    minAmount: 0, maxDiscount: null, scopeType: 1, scopeId: '',
    startTime: '', endTime: '', stock: null, perLimit: 1, description: '', status: 1
  }
  formMsg.value = ''
}

function openCreate() {
  resetForm()
  editMode.value = false
  showCreateForm.value = true
}

function onDiscountTypeChange() {
  if (couponForm.value.discountType === 1) {
    couponForm.value.maxDiscount = null
  }
}

function doSearch() {
  currentPage.value = 1
  loadData()
}

function resetSearch() {
  search.value = { keyword: '', type: '', status: '' }
  currentPage.value = 1
  loadData()
}

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadData()
}

async function loadData() {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      keyword: search.value.keyword,
      type: search.value.type,
      status: search.value.status
    }
    const res = await adminApi.couponPage(params)
    const data = res.data || {}
    coupons.value = data.records || data.list || []
    total.value = data.total || 0
    totalPages.value = Math.ceil(total.value / pageSize) || 0
    computeStats()
  } catch {
    coupons.value = []
    total.value = 0
    totalPages.value = 0
  }
}

function computeStats() {
  const all = coupons.value
  stats.value.total = total.value
  stats.value.active = all.filter(c => c.status === 1).length
  stats.value.claimed = all.reduce((s, c) => s + (c.claimedCount || 0), 0)
  const now = new Date()
  const weekLater = new Date(now.getTime() + 7 * 24 * 3600 * 1000)
  stats.value.expiring = all.filter(c => {
    if (!c.endTime) return false
    const end = new Date(c.endTime)
    return end > now && end <= weekLater
  }).length
}

async function handleSave() {
  const f = couponForm.value
  if (!f.name) { formMsg.value = '请输入优惠券名称'; formMsgType.value = 'error'; return }
  if (f.discountValue === null || f.discountValue <= 0) { formMsg.value = '请输入有效的优惠值'; formMsgType.value = 'error'; return }
  if (!f.startTime || !f.endTime) { formMsg.value = '请设置有效期'; formMsgType.value = 'error'; return }

  try {
    const payload = { ...f }
    if (payload.stock === null || payload.stock === '') payload.stock = -1
    if (payload.discountType === 1) payload.maxDiscount = null
    if (payload.scopeType !== 2) payload.scopeId = null

    const res = editMode.value
      ? await adminApi.updateCoupon(payload)
      : await adminApi.createCoupon(payload)

    if (res.code === 200) {
      formMsg.value = editMode.value ? '修改成功' : '创建成功'
      formMsgType.value = 'success'
      setTimeout(() => {
        showCreateForm.value = false
        loadData()
      }, 800)
    } else {
      formMsg.value = res.message || '操作失败'
      formMsgType.value = 'error'
    }
  } catch {
    formMsg.value = '请求失败'
    formMsgType.value = 'error'
  }
}

function handleEdit(c) {
  editMode.value = true
  couponForm.value = {
    id: c.id,
    name: c.name || '',
    type: c.type || 1,
    discountType: c.discountType || 1,
    discountValue: c.discountValue,
    minAmount: c.minAmount ?? 0,
    maxDiscount: c.maxDiscount ?? null,
    scopeType: c.scopeType || 1,
    scopeId: c.scopeId || '',
    startTime: c.startTime ? c.startTime.slice(0, 16) : '',
    endTime: c.endTime ? c.endTime.slice(0, 16) : '',
    stock: c.stock === -1 ? null : c.stock,
    perLimit: c.perLimit ?? 1,
    description: c.description || '',
    status: c.status ?? 1
  }
  formMsg.value = ''
  showCreateForm.value = true
}

async function handleDelete(id) {
  if (!confirm('确认删除该优惠券？删除后不可恢复。')) return
  try {
    await adminApi.deleteCoupon(id)
    loadData()
  } catch {}
}

async function toggleStatus(c) {
  try {
    await adminApi.toggleCouponStatus({ id: c.id, status: c.status === 1 ? 0 : 1 })
    loadData()
  } catch {}
}

function openSend(c) {
  sendCoupon.value = c
  sendTarget.value = null
  sendMsg.value = ''
  showSendModal.value = true
}

async function sendToAll() {
  sending.value = true
  sendMsg.value = ''
  try {
    const res = await adminApi.sendCoupon({ couponId: sendCoupon.value.id, target: 'all' })
    if (res.code === 200) {
      sendMsg.value = '已向全部用户发放优惠券！'
      sendMsgType.value = 'success'
    } else {
      sendMsg.value = res.message || '发券失败'
      sendMsgType.value = 'error'
    }
  } catch {
    sendMsg.value = '请求失败'
    sendMsgType.value = 'error'
  } finally {
    sending.value = false
  }
}

async function sendToUser() {
  if (!sendTarget.value) return
  sending.value = true
  sendMsg.value = ''
  try {
    const res = await adminApi.sendCoupon({ couponId: sendCoupon.value.id, target: 'user', userId: sendTarget.value })
    if (res.code === 200) {
      sendMsg.value = '已向用户发放优惠券！'
      sendMsgType.value = 'success'
    } else {
      sendMsg.value = res.message || '发券失败'
      sendMsgType.value = 'error'
    }
  } catch {
    sendMsg.value = '请求失败'
    sendMsgType.value = 'error'
  } finally {
    sending.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.coupon-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.search-card {
  margin-bottom: var(--space-5);
  padding: var(--space-4) var(--space-5);
}

.search-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.search-input-wrap {
  position: relative;
  flex: 1;
  min-width: 200px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 40px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-page);
  font-size: var(--font-size-sm);
  color: var(--text-title);
  outline: none;
  transition: all var(--transition-base);
}

.search-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
  background: var(--bg-card);
}

.search-input::placeholder {
  color: var(--text-muted);
}

.filter-select {
  padding: 10px 32px 10px 14px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-page);
  font-size: var(--font-size-sm);
  color: var(--text-title);
  outline: none;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%239CA3AF' stroke-width='2'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  transition: all var(--transition-base);
}

.filter-select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xl);
  flex-shrink: 0;
}

.stat-icon--primary { background: var(--color-primary-bg); }
.stat-icon--success { background: var(--color-success-bg); }
.stat-icon--accent { background: var(--color-accent-bg); }
.stat-icon--warning { background: var(--color-warning-bg); }

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

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-top: 2px;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.table-wrap {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead th {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  white-space: nowrap;
  background: var(--bg-page);
}

.data-table tbody td {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--text-title);
  border-bottom: 1px solid var(--border-light);
  vertical-align: middle;
}

.table-row {
  transition: background var(--transition-base);
}

.table-row:hover {
  background: var(--bg-hover);
}

.td-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.td-value {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.td-date {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  white-space: nowrap;
}

.col-actions {
  width: 260px;
}

.action-group {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border-light);
}

.page-pill {
  min-width: 36px;
  height: 36px;
  padding: 0 var(--space-2);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  background: transparent;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-body);
  cursor: pointer;
  transition: all var(--transition-base);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.page-pill:hover:not(:disabled) {
  background: var(--bg-page);
  color: var(--text-title);
}

.page-pill.active {
  background: var(--color-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-button);
}

.page-pill:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-overlay);
  backdrop-filter: blur(8px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.form-modal {
  width: 640px;
  max-width: 92vw;
  max-height: 88vh;
  overflow-y: auto;
  border-radius: var(--radius-xl);
  animation: slideUp 0.25s ease;
}

.send-modal {
  width: 480px;
  max-width: 92vw;
  border-radius: var(--radius-xl);
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(12px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.modal-close {
  color: var(--text-muted);
  transition: color var(--transition-base);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
}

.modal-close:hover {
  color: var(--text-title);
  background: var(--bg-hover);
}

.form-body {
  padding: var(--space-5) var(--space-6);
}

.form-row {
  display: flex;
  gap: var(--space-4);
}

.form-row .col {
  flex: 1;
  min-width: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-color);
}

.send-body {
  padding: var(--space-5) var(--space-6);
}

.send-option {
  text-align: center;
  margin-bottom: var(--space-4);
}

.send-all-btn {
  width: 100%;
}

.send-divider {
  text-align: center;
  margin: var(--space-4) 0;
  position: relative;
}

.send-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border-color);
}

.send-divider span {
  position: relative;
  background: var(--bg-card);
  padding: 0 var(--space-3);
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.send-user-row {
  display: flex;
  gap: var(--space-3);
}

.send-user-row input {
  flex: 1;
}

.empty-state {
  padding: var(--space-8) 0;
  text-align: center;
  color: var(--text-muted);
}

.empty-icon {
  font-size: var(--font-size-3xl);
  margin-bottom: var(--space-3);
  opacity: 0.6;
}

.empty-text {
  font-size: var(--font-size-md);
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input-wrap {
    min-width: 0;
  }

  .action-group {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
