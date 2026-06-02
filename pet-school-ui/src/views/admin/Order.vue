<template>
  <div class="order-page">
    <div class="page-header">
      <h1 class="page-title">订单管理</h1>
      <div class="tab-switcher">
        <button
          class="tab-pill"
          :class="{ active: tab === 'course' }"
          @click="switchTab('course')"
        >课程订单</button>
        <button
          class="tab-pill"
          :class="{ active: tab === 'boarding' }"
          @click="switchTab('boarding')"
        >寄宿订单</button>
      </div>
    </div>

    <div class="card search-card">
      <div class="search-row">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            v-model="search.keyword"
            class="search-input"
            placeholder="搜索订单号 / 用户名…"
            @keyup.enter="doSearch"
          />
        </div>
        <select v-model="search.status" class="status-select">
          <option value="">全部状态</option>
          <option value="0">待支付</option>
          <option value="1">已支付</option>
          <option value="2">已取消</option>
          <option value="3">已完成</option>
          <option value="4">已退款</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="doSearch">搜索</button>
        <button class="btn btn-secondary btn-sm" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="card table-card">
      <div class="table-toolbar">
        <span class="record-count">共 <strong>{{ total }}</strong> 条记录</span>
        <button
          v-if="selectedIds.length"
          class="btn btn-danger btn-sm batch-btn"
          @click="batchDelete"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          批量删除
          <span class="count-badge">{{ selectedIds.length }}</span>
        </button>
      </div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-check">
                <input type="checkbox" :checked="allChecked" @change="toggleAll" />
              </th>
              <th>订单号</th>
              <th>用户</th>
              <th v-if="tab === 'course'">课程</th>
              <th v-else>寄宿</th>
              <th>金额</th>
              <th>状态</th>
              <th>创建时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!orders.length">
              <td colspan="9">
                <div class="empty-state">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/></svg>
                  <p>暂无订单数据</p>
                </div>
              </td>
            </tr>
            <tr v-for="o in orders" :key="o.id" class="table-row">
              <td class="col-check">
                <input type="checkbox" :value="o.id" v-model="selectedIds" />
              </td>
              <td class="col-order-no">{{ o.orderNo || o.id }}</td>
              <td>{{ o.userName || o.userId }}</td>
              <td>{{ o.courseName || o.boardingName || '-' }}</td>
              <td><span class="price">¥{{ o.finalPrice || o.totalPrice || o.amount || o.price || '0.00' }}</span></td>
              <td><span class="tag" :class="statusClass(o.status)">{{ statusText(o.status) }}</span></td>
              <td class="col-time">{{ formatTime(o.createTime || o.createdAt) }}</td>
              <td class="col-actions">
                <div class="action-group">
                  <button class="btn btn-ghost btn-sm" @click="detailOrder = o">查看</button>
                  <button v-if="o.status === 0" class="btn btn-primary btn-sm" @click="changeStatus({ ...o, status: 1 })">确认支付</button>
                  <button v-if="o.status === 1" class="btn btn-ghost btn-sm" @click="changeStatus({ ...o, status: 3 })">确认完成</button>
                  <button v-if="o.status === 0" class="btn btn-ghost btn-sm" @click="changeStatus({ ...o, status: 2 })">取消订单</button>
                  <button v-if="o.status === 1" class="btn btn-ghost btn-sm" @click="changeStatus({ ...o, status: 4 })">退款</button>
                  <button class="btn btn-danger btn-sm" @click="deleteOne(o)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button
          class="page-pill"
          :disabled="currentPage <= 1"
          @click="goPage(currentPage - 1)"
        >&laquo;</button>
        <button
          v-for="p in visiblePages"
          :key="p"
          class="page-pill"
          :class="{ active: p === currentPage }"
          @click="goPage(p)"
        >{{ p }}</button>
        <button
          class="page-pill"
          :disabled="currentPage >= totalPages"
          @click="goPage(currentPage + 1)"
        >&raquo;</button>
      </div>
    </div>

    <div v-if="detailOrder" class="modal-overlay" @click.self="detailOrder = null">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h3>订单详情</h3>
          <button class="btn-icon" @click="detailOrder = null">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <span class="detail-label">订单号</span>
            <span class="detail-value">{{ detailOrder.orderNo || detailOrder.id }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">用户</span>
            <span class="detail-value">{{ detailOrder.userName || detailOrder.userId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">{{ tab === 'course' ? '课程' : '寄宿' }}</span>
            <span class="detail-value">{{ detailOrder.courseName || detailOrder.boardingName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">原价</span>
            <span class="detail-value">¥{{ detailOrder.totalPrice || detailOrder.amount || '0.00' }}</span>
          </div>
          <div v-if="detailOrder.couponAmount > 0" class="detail-row">
            <span class="detail-label">优惠</span>
            <span class="detail-value" style="color: var(--color-accent)">-¥{{ detailOrder.couponAmount }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">实付</span>
            <span class="detail-value price">¥{{ detailOrder.finalPrice || detailOrder.totalPrice || detailOrder.amount || detailOrder.price || '0.00' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态</span>
            <span class="detail-value"><span class="tag" :class="statusClass(detailOrder.status)">{{ statusText(detailOrder.status) }}</span></span>
          </div>
          <div class="detail-row">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ formatTime(detailOrder.createTime || detailOrder.createdAt) }}</span>
          </div>
          <div v-if="detailOrder.payTime" class="detail-row">
            <span class="detail-label">支付时间</span>
            <span class="detail-value">{{ formatTime(detailOrder.payTime) }}</span>
          </div>
          <div v-if="detailOrder.phone" class="detail-row">
            <span class="detail-label">联系电话</span>
            <span class="detail-value">{{ detailOrder.phone }}</span>
          </div>
          <div v-if="detailOrder.remark" class="detail-row">
            <span class="detail-label">备注</span>
            <span class="detail-value">{{ detailOrder.remark }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

const tab = ref('course')
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(0)
const pageSize = 10
const selectedIds = ref([])
const detailOrder = ref(null)
const search = ref({ keyword: '', status: '' })

const allChecked = computed(() => {
  return orders.value.length > 0 && orders.value.every(o => selectedIds.value.includes(o.id))
})

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

function statusText(s) {
  const map = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '已完成', 4: '已退款' }
  return map[s] ?? '未知'
}

function statusClass(s) {
  const map = { 0: 'tag-warning', 1: 'tag-success', 2: 'tag-danger', 3: 'tag-success', 4: 'tag-danger' }
  return map[s] ?? ''
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function switchTab(t) {
  tab.value = t
  currentPage.value = 1
  selectedIds.value = []
  search.value = { keyword: '', status: '' }
  loadData()
}

function doSearch() {
  currentPage.value = 1
  loadData()
}

function resetSearch() {
  search.value = { keyword: '', status: '' }
  currentPage.value = 1
  loadData()
}

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadData()
}

function toggleAll(e) {
  if (e.target.checked) {
    selectedIds.value = orders.value.map(o => o.id)
  } else {
    selectedIds.value = []
  }
}

async function loadData() {
  const params = {
    page: currentPage.value,
    size: pageSize,
    keyword: search.value.keyword,
    status: search.value.status
  }
  try {
    const api = tab.value === 'course' ? adminApi.courseOrderPage : adminApi.boardingOrderPage
    const res = await api(params)
    orders.value = (res.data?.records || res.data?.list || res.data?.data?.records || []).map(o => ({ ...o, _showDropdown: false }))
    total.value = res.data?.total || res.data?.data?.total || 0
    totalPages.value = Math.ceil(total.value / pageSize) || 0
  } catch {
    orders.value = []
    total.value = 0
    totalPages.value = 0
  }
}

async function changeStatus(o) {
  try {
    const api = tab.value === 'course' ? adminApi.courseOrderUpdateStatus : adminApi.boardingOrderUpdateStatus
    await api({ id: o.id, status: o.status })
    loadData()
  } catch {}
}

async function deleteOne(o) {
  if (!confirm('确认删除该订单？')) return
  try {
    const api = tab.value === 'course' ? adminApi.courseOrderDelete : adminApi.boardingOrderDelete
    await api(o.id)
    loadData()
  } catch {}
}

async function batchDelete() {
  if (!selectedIds.value.length) return
  if (!confirm(`确认删除选中的 ${selectedIds.value.length} 条订单？`)) return
  try {
    const api = tab.value === 'course' ? adminApi.courseOrderBatchDelete : adminApi.boardingOrderBatchDelete
    await api(selectedIds.value)
    selectedIds.value = []
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.order-page {
  padding: var(--space-6);
  max-width: 1280px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-title);
  margin: 0;
}

.tab-switcher {
  display: flex;
  gap: var(--space-2);
  background: var(--bg-card);
  padding: var(--space-2);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.tab-pill {
  padding: 8px 20px;
  border: 1.5px solid transparent;
  border-radius: var(--radius-full);
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

.tab-pill.active {
  background: var(--color-primary);
  color: var(--text-inverse);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-button);
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
  min-width: 220px;
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
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: var(--radius-full);
  background: var(--bg-page);
  font-size: var(--font-size-sm);
  color: var(--text-title);
  outline: none;
  transition: all var(--transition-base);
}

.search-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(79, 124, 255, 0.12);
  background: var(--bg-card);
}

.search-input::placeholder {
  color: var(--text-muted);
}

.status-select {
  padding: 10px 32px 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
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

.status-select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(79, 124, 255, 0.12);
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.record-count {
  font-size: var(--font-size-sm);
  color: var(--text-body);
}

.record-count strong {
  color: var(--text-title);
  font-weight: 600;
}

.batch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: var(--radius-full);
  background: rgba(255, 107, 107, 0.2);
  color: var(--color-danger);
  font-size: var(--font-size-xs);
  font-weight: 700;
  line-height: 1;
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
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  text-align: left;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  white-space: nowrap;
  background: rgba(247, 249, 252, 0.5);
}

.col-check {
  width: 48px;
  text-align: center !important;
}

.col-actions {
  width: 140px;
  text-align: center !important;
}

.col-time {
  white-space: nowrap;
}

.col-order-no {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: var(--font-size-xs);
  color: var(--text-body);
}

.data-table tbody td {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--text-title);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  vertical-align: middle;
}

.table-row {
  transition: background var(--transition-base);
}

.table-row:hover {
  background: rgba(79, 124, 255, 0.03);
}

.table-row:hover td {
  border-bottom-color: rgba(79, 124, 255, 0.08);
}

.data-table input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
  cursor: pointer;
}

.action-group {
  display: flex;
  gap: var(--space-2);
  justify-content: flex-end;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.page-pill {
  min-width: 36px;
  height: 36px;
  padding: 0 var(--space-2);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  background: transparent;
  font-size: var(--font-size-sm);
  font-weight: 500;
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
  color: #fff;
  box-shadow: 0 2px 8px rgba(79, 124, 255, 0.3);
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
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.detail-modal {
  width: 480px;
  max-width: 92vw;
  max-height: 85vh;
  overflow-y: auto;
  border-radius: var(--radius-lg);
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.modal-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-title);
}

.modal-header .btn-icon {
  color: var(--text-muted);
  transition: color var(--transition-base);
}

.modal-header .btn-icon:hover {
  color: var(--text-title);
}

.detail-body {
  padding: var(--space-5) var(--space-6);
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  flex-shrink: 0;
}

.detail-value {
  font-size: var(--font-size-sm);
  color: var(--text-title);
  font-weight: 500;
  text-align: right;
  word-break: break-all;
}

.empty-state {
  padding: var(--space-8) 0;
  text-align: center;
  color: var(--text-muted);
}

.empty-state svg {
  opacity: 0.35;
  margin-bottom: var(--space-3);
}

.empty-state p {
  margin: 0;
  font-size: var(--font-size-sm);
}
</style>
