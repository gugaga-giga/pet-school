<template>
  <div class="cert-page">
    <div class="page-header">
      <h1 class="page-title">🎓 毕业证书管理</h1>
    </div>

    <div class="card search-card">
      <div class="search-row">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            v-model="search.keyword"
            class="search-input"
            placeholder="搜索证书编号 / 宠物名称 / 主人 / 课程…"
            @keyup.enter="doSearch"
          />
        </div>
        <select v-model="search.status" class="status-select">
          <option value="">全部状态</option>
          <option value="0">正常</option>
          <option value="1">已作废</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="doSearch">搜索</button>
        <button class="btn btn-secondary btn-sm" @click="resetSearch">重置</button>
        <button class="btn btn-accent btn-sm" @click="showTestModal = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14" style="margin-right:4px;vertical-align:-2px"><path d="M12 5v14M5 12h14"/></svg>
          测试生成
        </button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon--primary">📜</div>
        <div class="stat-info">
          <span class="stat-value">{{ statsData.total }}</span>
          <span class="stat-label">总证书数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--accent">✨</div>
        <div class="stat-info">
          <span class="stat-value">{{ statsData.thisMonth }}</span>
          <span class="stat-label">本月新增</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--success">✅</div>
        <div class="stat-info">
          <span class="stat-value">{{ statsData.active }}</span>
          <span class="stat-label">正常</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--danger">❌</div>
        <div class="stat-info">
          <span class="stat-value">{{ statsData.revoked }}</span>
          <span class="stat-label">已作废</span>
        </div>
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
              <th>证书编号</th>
              <th>宠物名称</th>
              <th>主人</th>
              <th>课程</th>
              <th>毕业时间</th>
              <th>颁发时间</th>
              <th>状态</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!certificates.length">
              <td colspan="9">
                <div class="empty-state">
                  <div class="empty-icon">📜</div>
                  <div class="empty-text">暂无证书数据</div>
                </div>
              </td>
            </tr>
            <tr v-for="c in certificates" :key="c.id" class="table-row">
              <td class="col-check">
                <input type="checkbox" :value="c.id" v-model="selectedIds" />
              </td>
              <td class="col-cert-no">{{ c.certNo || c.certificateNo || '-' }}</td>
              <td>{{ c.petName || c.petId || '-' }}</td>
              <td>{{ c.ownerName || c.userName || '-' }}</td>
              <td>{{ c.courseName || c.courseId || '-' }}</td>
              <td class="col-time">{{ c.graduateDate || '-' }}</td>
              <td class="col-time">{{ c.issueDate || '-' }}</td>
              <td><span class="tag" :class="statusClass(c.status)">{{ statusText(c.status) }}</span></td>
              <td class="col-actions">
                <div class="action-group">
                  <button class="btn btn-ghost btn-sm" @click="viewDetail(c)">查看</button>
                  <button class="btn btn-ghost btn-sm" @click="downloadCert(c)">下载</button>
                  <button class="btn btn-ghost btn-sm" @click="regenerateCert(c.id)">重新生成</button>
                  <button class="btn btn-danger btn-sm" @click="deleteCert(c.id)">删除</button>
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

    <div v-if="detailCert" class="modal-overlay" @click.self="detailCert = null">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h3>证书详情</h3>
          <button class="btn-icon" @click="detailCert = null">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="detail-body">
          <div class="cert-preview">
            <img
              v-if="detailCert.certificateUrl"
              :src="'/api' + detailCert.certificateUrl"
              alt="证书图片"
              class="cert-image"
            />
            <div v-else class="cert-placeholder">
              <span class="cert-placeholder-icon">🎓</span>
              <span class="cert-placeholder-text">证书图片未生成</span>
            </div>
          </div>
          <div class="detail-row">
            <span class="detail-label">证书编号</span>
            <span class="detail-value">{{ detailCert.certNo || detailCert.certificateNo || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">宠物名称</span>
            <span class="detail-value">{{ detailCert.petName || detailCert.petId || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">主人</span>
            <span class="detail-value">{{ detailCert.ownerName || detailCert.userName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">课程</span>
            <span class="detail-value">{{ detailCert.courseName || detailCert.courseId || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">毕业时间</span>
            <span class="detail-value">{{ detailCert.graduateDate || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">颁发时间</span>
            <span class="detail-value">{{ detailCert.issueDate || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态</span>
            <span class="detail-value"><span class="tag" :class="statusClass(detailCert.status)">{{ statusText(detailCert.status) }}</span></span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showTestModal" class="modal-overlay" @click.self="showTestModal = false">
      <div class="modal-content test-modal">
        <div class="modal-header">
          <h3>🧪 测试生成证书</h3>
          <button class="btn-icon" @click="showTestModal = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="test-modal-body">
          <p class="test-hint">输入信息后直接生成一张测试毕业证书，无需关联订单。</p>
          <div class="test-field">
            <label class="test-label">宠物名称</label>
            <input v-model="testForm.petName" class="test-input" placeholder="例如：旺财" />
          </div>
          <div class="test-field">
            <label class="test-label">主人姓名</label>
            <input v-model="testForm.ownerName" class="test-input" placeholder="例如：张三" />
          </div>
          <div class="test-field">
            <label class="test-label">课程名称</label>
            <input v-model="testForm.courseName" class="test-input" placeholder="例如：基础服从训练" />
          </div>
          <div class="test-actions">
            <button class="btn btn-secondary" @click="showTestModal = false">取消</button>
            <button class="btn btn-primary" :disabled="testGenerating" @click="doGenerateTest">
              {{ testGenerating ? '生成中…' : '立即生成' }}
            </button>
          </div>
          <div v-if="testResult" class="test-result">
            <div class="test-result-header">
              <span class="test-result-badge">✅ 生成成功</span>
              <span class="test-result-no">{{ testResult.certificateNo }}</span>
            </div>
            <div class="cert-preview" style="margin-top:12px">
              <img
                v-if="testResult.certificateUrl"
                :src="'/api' + testResult.certificateUrl"
                alt="测试证书"
                class="cert-image"
              />
            </div>
            <div class="test-actions" style="margin-top:12px">
              <button class="btn btn-accent btn-sm" @click="downloadCert(testResult)">下载证书</button>
              <button class="btn btn-secondary btn-sm" @click="testResult = null; showTestModal = false; loadData()">关闭并刷新</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

const certificates = ref([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(0)
const pageSize = 10
const selectedIds = ref([])
const detailCert = ref(null)
const search = ref({ keyword: '', status: '' })
const showTestModal = ref(false)
const testGenerating = ref(false)
const testResult = ref(null)
const testForm = ref({ petName: '', ownerName: '', courseName: '' })

const statsData = computed(() => {
  const list = certificates.value
  const now = new Date()
  const thisMonth = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0')
  return {
    total: total.value,
    thisMonth: list.filter(c => (c.issueDate || '').startsWith(thisMonth)).length,
    active: list.filter(c => c.status === 0).length,
    revoked: list.filter(c => c.status === 1).length
  }
})

const allChecked = computed(() => {
  return certificates.value.length > 0 && certificates.value.every(c => selectedIds.value.includes(c.id))
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
  return s === 1 ? '已作废' : '正常'
}

function statusClass(s) {
  return s === 1 ? 'tag-danger' : 'tag-success'
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
    selectedIds.value = certificates.value.map(c => c.id)
  } else {
    selectedIds.value = []
  }
}

function viewDetail(cert) {
  detailCert.value = cert
}

function downloadCert(cert) {
  if (cert.certificateUrl) {
    const link = document.createElement('a')
    link.href = '/api' + cert.certificateUrl
    link.download = (cert.certNo || cert.certificateNo || 'certificate') + '.png'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } else {
    alert('证书图片未生成，无法下载')
  }
}

async function regenerateCert(id) {
  if (!confirm('确认重新生成该证书？')) return
  try {
    await adminApi.certificateRegenerate(id)
    loadData()
  } catch {}
}

async function deleteCert(id) {
  if (!confirm('确认删除该证书？')) return
  try {
    await adminApi.certificateDelete(id)
    selectedIds.value = selectedIds.value.filter(sid => sid !== id)
    loadData()
  } catch {}
}

async function batchDelete() {
  if (!selectedIds.value.length) return
  if (!confirm(`确认删除选中的 ${selectedIds.value.length} 条证书？`)) return
  try {
    await adminApi.certificateBatchDelete(selectedIds.value)
    selectedIds.value = []
    loadData()
  } catch {}
}

async function doGenerateTest() {
  testGenerating.value = true
  testResult.value = null
  try {
    const res = await adminApi.certificateGenerateTest({
      petName: testForm.value.petName || '测试宠物',
      ownerName: testForm.value.ownerName || '测试主人',
      courseName: testForm.value.courseName || '测试课程'
    })
    if (res.code === 200) {
      testResult.value = res.data
    } else {
      alert(res.message || '生成失败')
    }
  } catch (e) {
    alert('生成失败，请重试')
  } finally {
    testGenerating.value = false
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
    const res = await adminApi.certificatePage(params)
    certificates.value = res.data?.records || res.data?.list || res.data?.data?.records || []
    total.value = res.data?.total || res.data?.data?.total || 0
    totalPages.value = Math.ceil(total.value / pageSize) || 0
  } catch {
    certificates.value = []
    total.value = 0
    totalPages.value = 0
  }
}

onMounted(loadData)
</script>

<style scoped>
.cert-page {
  padding: var(--space-6);
  max-width: 1280px;
  margin: 0 auto;
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-5);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-title);
  margin: 0;
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
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
  font-size: 22px;
  flex-shrink: 0;
}

.stat-icon--primary {
  background: var(--color-primary-bg);
}

.stat-icon--accent {
  background: var(--color-accent-bg);
}

.stat-icon--success {
  background: var(--color-success-bg);
}

.stat-icon--danger {
  background: var(--color-danger-bg);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-info .stat-value {
  font-size: var(--font-size-2xl);
  font-weight: 800;
  color: var(--text-title);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.stat-info .stat-label {
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
  width: 160px;
  text-align: center !important;
}

.col-time {
  white-space: nowrap;
}

.col-cert-no {
  font-family: var(--font-mono);
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

.detail-modal {
  width: 520px;
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

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
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

.cert-preview {
  margin-bottom: var(--space-5);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-light);
}

.cert-image {
  width: 100%;
  display: block;
}

.cert-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-7);
  background: var(--bg-page);
}

.cert-placeholder-icon {
  font-size: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.5;
}

.cert-placeholder-text {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
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

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.5;
}

.empty-text {
  font-size: var(--font-size-sm);
}

.test-modal {
  width: 480px;
  max-width: 92vw;
  max-height: 90vh;
  overflow-y: auto;
  border-radius: var(--radius-lg);
  animation: slideUp 0.25s ease;
}

.test-modal-body {
  padding: var(--space-5) var(--space-6);
}

.test-hint {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0 0 var(--space-4) 0;
  line-height: 1.5;
}

.test-field {
  margin-bottom: var(--space-4);
}

.test-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 6px;
}

.test-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--text-title);
  background: var(--bg-page);
  outline: none;
  transition: all var(--transition-base);
  box-sizing: border-box;
}

.test-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(79, 124, 255, 0.12);
  background: var(--bg-card);
}

.test-input::placeholder {
  color: var(--text-muted);
}

.test-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-5);
}

.test-result {
  margin-top: var(--space-5);
  padding: var(--space-4);
  background: var(--bg-page);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.test-result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.test-result-badge {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-success);
}

.test-result-no {
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
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
    min-width: auto;
  }
}
</style>
