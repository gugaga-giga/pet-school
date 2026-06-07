<template>
  <div class="order-page">
    <div class="page-header">
      <h1 class="page-title">📋 医疗订单</h1>
    </div>

    <div class="card search-card">
      <div class="search-row">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input v-model="search.keyword" class="search-input" placeholder="搜索订单号 / 宠物 / 主人…" @keyup.enter="doSearch" />
        </div>
        <select v-model="search.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="0">待确认</option>
          <option value="1">已预约</option>
          <option value="2">就诊中</option>
          <option value="3">已完成</option>
          <option value="4">已取消</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="doSearch">搜索</button>
        <button class="btn btn-secondary btn-sm" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon--primary">📋</div>
        <div class="stat-info">
          <div class="stat-value">{{ statsData.total }}</div>
          <div class="stat-label">总订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--warning">⏳</div>
        <div class="stat-info">
          <div class="stat-value">{{ statsData.pending }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--info">📅</div>
        <div class="stat-info">
          <div class="stat-value">{{ statsData.appointed }}</div>
          <div class="stat-label">已预约</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--accent">🩺</div>
        <div class="stat-info">
          <div class="stat-value">{{ statsData.treating }}</div>
          <div class="stat-label">就诊中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--success">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ statsData.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--muted">🚫</div>
        <div class="stat-info">
          <div class="stat-value">{{ statsData.cancelled }}</div>
          <div class="stat-label">已取消</div>
        </div>
      </div>
    </div>

    <div class="card table-card">
      <div class="table-toolbar">
        <span class="record-count">共 <strong>{{ total }}</strong> 条记录</span>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>宠物</th>
              <th>主人</th>
              <th>医生</th>
              <th>科室</th>
              <th>预约时间</th>
              <th>费用</th>
              <th>状态</th>
              <th>创建时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!list.length">
              <td colspan="10">
                <div class="empty-state">
                  <div class="empty-icon">📋</div>
                  <div class="empty-text">暂无订单数据</div>
                </div>
              </td>
            </tr>
            <tr v-for="item in list" :key="item.id" class="table-row">
              <td class="td-order-no">{{ item.orderNo || item.id || '-' }}</td>
              <td>{{ item.petName || item.petId || '-' }}</td>
              <td>{{ item.ownerName || item.userName || '-' }}</td>
              <td>{{ item.doctorName || item.doctorId || '-' }}</td>
              <td>{{ item.departmentName || item.departmentId || '-' }}</td>
              <td class="td-time">{{ item.appointmentTime || '-' }}</td>
              <td class="td-price">{{ item.price != null ? '¥' + item.price : '-' }}</td>
              <td><span :class="['tag', statusTagClass(item.status)]">{{ statusText(item.status) }}</span></td>
              <td class="td-time">{{ item.createTime || item.createdAt || '-' }}</td>
              <td class="col-actions">
                <div class="action-group">
                  <template v-if="item.status === 0">
                    <button class="btn btn-primary btn-sm" @click="updateStatus(item.id, 1)">确认预约</button>
                    <button class="btn btn-secondary btn-sm" @click="updateStatus(item.id, 4)">取消</button>
                  </template>
                  <template v-else-if="item.status === 1">
                    <button class="btn btn-accent btn-sm" @click="updateStatus(item.id, 2)">开始诊疗</button>
                    <button class="btn btn-secondary btn-sm" @click="updateStatus(item.id, 4)">取消</button>
                  </template>
                  <template v-else-if="item.status === 2">
                    <button class="btn btn-primary btn-sm" @click="openRecordModal(item)">录入病历</button>
                  </template>
                  <template v-else-if="item.status === 3">
                    <button class="btn btn-ghost btn-sm" @click="viewRecord(item)">查看病历</button>
                  </template>
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

    <div v-if="showRecordModal" class="modal-overlay" @click.self="showRecordModal = false">
      <div class="modal-content form-modal">
        <div class="modal-header">
          <h3>录入病历</h3>
          <button class="btn-icon modal-close" @click="showRecordModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="form-body">
          <div class="form-group">
            <label>主诉</label>
            <input v-model="recordForm.chiefComplaint" placeholder="请输入主诉" />
          </div>
          <div class="form-group">
            <label>体格检查</label>
            <textarea v-model="recordForm.physicalExam" placeholder="体格检查结果" rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>诊断</label>
            <input v-model="recordForm.diagnosis" placeholder="请输入诊断" />
          </div>
          <div class="form-group">
            <label>医嘱</label>
            <textarea v-model="recordForm.medicalAdvice" placeholder="医嘱内容" rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>用药</label>
            <input v-model="recordForm.medication" placeholder="用药信息" />
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="recordForm.remark" placeholder="备注信息" rows="2"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showRecordModal = false">取消</button>
          <button class="btn btn-primary" @click="submitRecord">提交病历</button>
        </div>
        <div v-if="recordMsg" :class="['alert', recordMsgType === 'success' ? 'alert-success' : 'alert-error']" style="margin: var(--space-3) var(--space-5) 0">{{ recordMsg }}</div>
      </div>
    </div>

    <div v-if="showRecordDetail" class="modal-overlay" @click.self="showRecordDetail = false">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h3>病历详情</h3>
          <button class="btn-icon modal-close" @click="showRecordDetail = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <span class="detail-label">主诉</span>
            <span class="detail-value">{{ recordDetail.chiefComplaint || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">体格检查</span>
            <span class="detail-value">{{ recordDetail.physicalExam || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">诊断</span>
            <span class="detail-value">{{ recordDetail.diagnosis || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">医嘱</span>
            <span class="detail-value">{{ recordDetail.medicalAdvice || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">用药</span>
            <span class="detail-value">{{ recordDetail.medication || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">备注</span>
            <span class="detail-value">{{ recordDetail.remark || '-' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(0)
const pageSize = 10
const search = ref({ keyword: '', status: '' })

const showRecordModal = ref(false)
const currentOrder = ref(null)
const recordForm = ref({ chiefComplaint: '', physicalExam: '', diagnosis: '', medicalAdvice: '', medication: '', remark: '' })
const recordMsg = ref('')
const recordMsgType = ref('success')

const showRecordDetail = ref(false)
const recordDetail = ref({})

const statsData = computed(() => {
  const all = list.value
  return {
    total: total.value,
    pending: all.filter(o => o.status === 0).length,
    appointed: all.filter(o => o.status === 1).length,
    treating: all.filter(o => o.status === 2).length,
    completed: all.filter(o => o.status === 3).length,
    cancelled: all.filter(o => o.status === 4).length
  }
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
  return { 0: '待确认', 1: '已预约', 2: '就诊中', 3: '已完成', 4: '已取消' }[s] ?? '未知'
}

function statusTagClass(s) {
  return { 0: 'tag-warning', 1: 'tag-info', 2: 'tag-accent', 3: 'tag-success', 4: 'tag-danger' }[s] ?? ''
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

async function loadData() {
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize,
      keyword: search.value.keyword,
      status: search.value.status
    }
    const res = await adminApi.medicalOrderPage(params)
    const data = res.data || {}
    list.value = data.records || data.list || []
    total.value = data.total || 0
    totalPages.value = Math.ceil(total.value / pageSize) || 0
  } catch {
    list.value = []
    total.value = 0
    totalPages.value = 0
  }
}

async function updateStatus(id, status) {
  const actionText = { 1: '确认预约', 2: '开始诊疗', 4: '取消' }[status] || '操作'
  if (!confirm(`确认${actionText}？`)) return
  try {
    await adminApi.medicalOrderUpdateStatus({ id, status })
    loadData()
  } catch {}
}

function openRecordModal(order) {
  currentOrder.value = order
  recordForm.value = { chiefComplaint: '', physicalExam: '', diagnosis: '', medicalAdvice: '', medication: '', remark: '' }
  recordMsg.value = ''
  showRecordModal.value = true
}

async function submitRecord() {
  if (!recordForm.value.chiefComplaint) { recordMsg.value = '请输入主诉'; recordMsgType.value = 'error'; return }
  if (!recordForm.value.diagnosis) { recordMsg.value = '请输入诊断'; recordMsgType.value = 'error'; return }
  try {
    const payload = {
      ...recordForm.value,
      medicalOrderId: currentOrder.value.id,
      petId: currentOrder.value.petId
    }
    const res = await adminApi.medicalRecordCreate(payload)
    if (res.code === 200) {
      recordMsg.value = '病历录入成功'
      recordMsgType.value = 'success'
      setTimeout(() => { showRecordModal.value = false; loadData() }, 800)
    } else {
      recordMsg.value = res.message || '录入失败'
      recordMsgType.value = 'error'
    }
  } catch {
    recordMsg.value = '请求失败'
    recordMsgType.value = 'error'
  }
}

async function viewRecord(order) {
  try {
    const res = await adminApi.medicalRecordByOrder(order.id)
    const record = res.data || res
    if (record && record.id) {
      recordDetail.value = record
      showRecordDetail.value = true
    } else {
      alert('暂无病历记录')
    }
  } catch {
    alert('获取病历失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.order-page {
  padding: var(--space-6);
  max-width: 1280px;
  margin: 0 auto;
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
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
  grid-template-columns: repeat(6, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-base);
}

.stat-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
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
.stat-icon--warning { background: var(--color-warning-bg); }
.stat-icon--info { background: var(--color-info-bg); }
.stat-icon--accent { background: var(--color-accent-bg); }
.stat-icon--success { background: var(--color-success-bg); }
.stat-icon--muted { background: var(--bg-hover); }

.stat-info {
  display: flex;
  flex-direction: column;
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

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.record-count {
  font-size: var(--font-size-sm);
  color: var(--text-body);
}

.record-count strong {
  color: var(--text-title);
  font-weight: var(--font-weight-semibold);
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

.td-order-no {
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
  color: var(--text-body);
}

.td-time {
  white-space: nowrap;
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.td-price {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.col-actions {
  width: 200px;
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
@keyframes slideUp { from { opacity: 0; transform: translateY(12px) scale(0.98); } to { opacity: 1; transform: translateY(0) scale(1); } }

.form-modal {
  width: 560px;
  max-width: 92vw;
  max-height: 88vh;
  overflow-y: auto;
  border-radius: var(--radius-xl);
  animation: slideUp 0.25s ease;
}

.detail-modal {
  width: 520px;
  max-width: 92vw;
  max-height: 85vh;
  overflow-y: auto;
  border-radius: var(--radius-xl);
  animation: slideUp 0.25s ease;
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-color);
}

.detail-body {
  padding: var(--space-5) var(--space-6);
}

.detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-light);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  flex-shrink: 0;
  margin-right: var(--space-4);
}

.detail-value {
  font-size: var(--font-size-sm);
  color: var(--text-title);
  font-weight: var(--font-weight-medium);
  text-align: right;
  word-break: break-all;
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

@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input-wrap {
    min-width: 0;
  }
}
</style>
