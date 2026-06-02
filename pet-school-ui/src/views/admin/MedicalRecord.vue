<template>
  <div class="record-page">
    <div class="page-header">
      <h1 class="page-title">📝 病历管理</h1>
    </div>

    <div class="card search-card">
      <div class="search-row">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input v-model="search.keyword" class="search-input" placeholder="搜索宠物 / 主人 / 医生…" @keyup.enter="doSearch" />
        </div>
        <button class="btn btn-primary btn-sm" @click="doSearch">搜索</button>
        <button class="btn btn-secondary btn-sm" @click="resetSearch">重置</button>
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
              <th>宠物</th>
              <th>主人</th>
              <th>医生</th>
              <th>主诉</th>
              <th>诊断</th>
              <th>就诊时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!list.length">
              <td colspan="7">
                <div class="empty-state">
                  <div class="empty-icon">📝</div>
                  <div class="empty-text">暂无病历数据</div>
                </div>
              </td>
            </tr>
            <tr v-for="item in list" :key="item.id" class="table-row">
              <td>{{ item.petName || item.petId || '-' }}</td>
              <td>{{ item.ownerName || item.userName || '-' }}</td>
              <td>{{ item.doctorName || item.doctorId || '-' }}</td>
              <td class="td-complaint">{{ item.chiefComplaint || '-' }}</td>
              <td class="td-diagnosis">{{ item.diagnosis || '-' }}</td>
              <td class="td-time">{{ item.visitTime || item.createTime || '-' }}</td>
              <td class="col-actions">
                <div class="action-group">
                  <button class="btn btn-ghost btn-sm" @click="viewDetail(item)">查看</button>
                  <button class="btn btn-primary btn-sm" @click="handleEdit(item)">编辑</button>
                  <button class="btn btn-danger btn-sm" @click="handleDelete(item.id)">删除</button>
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

    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h3>病历详情</h3>
          <button class="btn-icon modal-close" @click="showDetailModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <span class="detail-label">宠物</span>
            <span class="detail-value">{{ detailData.petName || detailData.petId || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">主人</span>
            <span class="detail-value">{{ detailData.ownerName || detailData.userName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">医生</span>
            <span class="detail-value">{{ detailData.doctorName || detailData.doctorId || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">主诉</span>
            <span class="detail-value">{{ detailData.chiefComplaint || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">体格检查</span>
            <span class="detail-value">{{ detailData.physicalExam || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">诊断</span>
            <span class="detail-value">{{ detailData.diagnosis || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">医嘱</span>
            <span class="detail-value">{{ detailData.medicalAdvice || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">用药</span>
            <span class="detail-value">{{ detailData.medication || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">备注</span>
            <span class="detail-value">{{ detailData.remark || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">就诊时间</span>
            <span class="detail-value">{{ detailData.visitTime || detailData.createTime || '-' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-content form-modal">
        <div class="modal-header">
          <h3>编辑病历</h3>
          <button class="btn-icon modal-close" @click="showEditModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="form-body">
          <div class="form-group">
            <label>主诉</label>
            <input v-model="editForm.chiefComplaint" placeholder="请输入主诉" />
          </div>
          <div class="form-group">
            <label>体格检查</label>
            <textarea v-model="editForm.physicalExam" placeholder="体格检查结果" rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>诊断</label>
            <input v-model="editForm.diagnosis" placeholder="请输入诊断" />
          </div>
          <div class="form-group">
            <label>医嘱</label>
            <textarea v-model="editForm.medicalAdvice" placeholder="医嘱内容" rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>用药</label>
            <input v-model="editForm.medication" placeholder="用药信息" />
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="editForm.remark" placeholder="备注信息" rows="2"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showEditModal = false">取消</button>
          <button class="btn btn-primary" @click="handleSave">保存修改</button>
        </div>
        <div v-if="editMsg" :class="['alert', editMsgType === 'success' ? 'alert-success' : 'alert-error']" style="margin: var(--space-3) var(--space-5) 0">{{ editMsg }}</div>
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
const search = ref({ keyword: '' })

const showDetailModal = ref(false)
const detailData = ref({})

const showEditModal = ref(false)
const editForm = ref({ id: null, chiefComplaint: '', physicalExam: '', diagnosis: '', medicalAdvice: '', medication: '', remark: '' })
const editMsg = ref('')
const editMsgType = ref('success')

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

function doSearch() {
  currentPage.value = 1
  loadData()
}

function resetSearch() {
  search.value = { keyword: '' }
  currentPage.value = 1
  loadData()
}

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadData()
}

function viewDetail(item) {
  detailData.value = item
  showDetailModal.value = true
}

function handleEdit(item) {
  editForm.value = {
    id: item.id,
    chiefComplaint: item.chiefComplaint || '',
    physicalExam: item.physicalExam || '',
    diagnosis: item.diagnosis || '',
    medicalAdvice: item.medicalAdvice || '',
    medication: item.medication || '',
    remark: item.remark || ''
  }
  editMsg.value = ''
  showEditModal.value = true
}

async function loadData() {
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize,
      keyword: search.value.keyword
    }
    const res = await adminApi.medicalRecordPage(params)
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

async function handleSave() {
  if (!editForm.value.chiefComplaint) { editMsg.value = '请输入主诉'; editMsgType.value = 'error'; return }
  if (!editForm.value.diagnosis) { editMsg.value = '请输入诊断'; editMsgType.value = 'error'; return }
  try {
    const res = await adminApi.medicalRecordUpdate(editForm.value)
    if (res.code === 200) {
      editMsg.value = '修改成功'
      editMsgType.value = 'success'
      setTimeout(() => { showEditModal.value = false; loadData() }, 800)
    } else {
      editMsg.value = res.message || '修改失败'
      editMsgType.value = 'error'
    }
  } catch {
    editMsg.value = '请求失败'
    editMsgType.value = 'error'
  }
}

async function handleDelete(id) {
  if (!confirm('确认删除该病历？')) return
  try {
    await adminApi.medicalRecordDelete(id)
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.record-page {
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

.td-complaint,
.td-diagnosis {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.td-time {
  white-space: nowrap;
  color: var(--text-muted);
  font-size: var(--font-size-xs);
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
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(12px) scale(0.98); } to { opacity: 1; transform: translateY(0) scale(1); } }

.detail-modal {
  width: 520px;
  max-width: 92vw;
  max-height: 85vh;
  overflow-y: auto;
  border-radius: var(--radius-xl);
  animation: slideUp 0.25s ease;
}

.form-modal {
  width: 560px;
  max-width: 92vw;
  max-height: 88vh;
  overflow-y: auto;
  border-radius: var(--radius-xl);
  animation: slideUp 0.25s ease;
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

.detail-body {
  padding: var(--space-5) var(--space-6);
}

.detail-row {
  display: flex;
  align-items: flex-start;
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
  margin-right: var(--space-4);
}

.detail-value {
  font-size: var(--font-size-sm);
  color: var(--text-title);
  font-weight: 500;
  text-align: right;
  word-break: break-all;
}

.form-body {
  padding: var(--space-5) var(--space-6);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.empty-state {
  padding: var(--space-8) 0;
  text-align: center;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.6;
}

.empty-text {
  font-size: var(--font-size-md);
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input-wrap {
    min-width: 0;
  }
}
</style>
