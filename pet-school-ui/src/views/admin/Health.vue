<template>
  <div class="health-page">
    <div class="page-header">
      <h1 class="page-title">🩺 宠物健康管理</h1>
    </div>

    <div class="card search-card">
      <div class="search-row">
        <div class="form-group" style="flex: 1; margin-bottom: 0">
          <input v-model="search.keyword" placeholder="搜索宠物名称/主人..." @keyup.enter="loadData" />
        </div>
        <div class="form-group" style="width: 160px; margin-bottom: 0">
          <select v-model="search.riskLevel">
            <option value="">全部</option>
            <option value="0">健康</option>
            <option value="1">注意</option>
            <option value="2">风险</option>
            <option value="3">高风险</option>
          </select>
        </div>
        <div class="form-group" style="width: 160px; margin-bottom: 0">
          <input v-model="search.startDate" type="date" />
        </div>
        <div class="form-group" style="width: 160px; margin-bottom: 0">
          <input v-model="search.endDate" type="date" />
        </div>
        <button class="btn btn-primary btn-sm" @click="loadData">搜索</button>
        <button class="btn btn-secondary btn-sm" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总记录数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-healthy">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.healthy }}</span>
          <span class="stat-label">健康宠物</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-attention">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.attention }}</span>
          <span class="stat-label">需注意</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-danger">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.highRisk }}</span>
          <span class="stat-label">高风险</span>
        </div>
      </div>
    </div>

    <div class="card table-card">
      <div class="card-header">
        <h3 class="card-title">健康记录</h3>
        <button class="btn btn-primary btn-sm" @click="openAdd">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增记录
        </button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>宠物名称</th>
              <th>主人</th>
              <th>体温</th>
              <th>体重</th>
              <th>心率</th>
              <th>健康评分</th>
              <th>风险等级</th>
              <th>体检时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in records" :key="r.id">
              <td class="td-name">{{ r.petName || '-' }}</td>
              <td>{{ r.ownerName || '-' }}</td>
              <td>{{ r.temperature != null ? r.temperature + '℃' : '-' }}</td>
              <td>{{ r.weight != null ? r.weight + 'kg' : '-' }}</td>
              <td>{{ r.heartRate != null ? r.heartRate + 'bpm' : '-' }}</td>
              <td>
                <span class="score-badge" :class="scoreClass(r.healthScore)">{{ r.healthScore != null ? r.healthScore : '-' }}</span>
              </td>
              <td>
                <span class="tag" :class="riskTagClass(r.riskLevel)">{{ riskText(r.riskLevel) }}</span>
              </td>
              <td>{{ r.inspectionDate || '-' }}</td>
              <td class="td-actions">
                <button class="btn btn-ghost btn-sm" @click="viewDetail(r)">查看</button>
                <button class="btn btn-ghost btn-sm" @click="openEdit(r)">编辑</button>
                <button class="btn btn-danger btn-sm" @click="handleDelete(r.id)">删除</button>
              </td>
            </tr>
            <tr v-if="!records.length">
              <td colspan="9">
                <div class="empty-state">
                  <div class="empty-icon">🩺</div>
                  <div class="empty-text">暂无健康记录</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button class="btn btn-sm btn-secondary" :disabled="currentPage <= 1" @click="currentPage--; loadData()">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="btn btn-sm btn-secondary" :disabled="currentPage >= totalPages" @click="currentPage++; loadData()">下一页</button>
      </div>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-content form-modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ editMode ? '编辑健康记录' : '新增健康记录' }}</h3>
          <button class="btn btn-ghost btn-sm" @click="showForm = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="form-group">
              <label>宠物ID</label>
              <input v-model.number="form.petId" type="number" placeholder="输入宠物ID" />
            </div>
            <div class="form-group">
              <label>体重 (kg)</label>
              <input v-model.number="form.weight" type="number" step="0.1" placeholder="如 15.5" />
            </div>
            <div class="form-group">
              <label>体温 (℃)</label>
              <input v-model.number="form.temperature" type="number" step="0.1" placeholder="如 38.5" />
            </div>
            <div class="form-group">
              <label>心率 (bpm)</label>
              <input v-model.number="form.heartRate" type="number" placeholder="如 120" />
            </div>
            <div class="form-group">
              <label>呼吸频率 (次/分)</label>
              <input v-model.number="form.respirationRate" type="number" placeholder="如 20" />
            </div>
            <div class="form-group">
              <label>食欲</label>
              <select v-model.number="form.appetite">
                <option :value="1">差</option>
                <option :value="2">一般</option>
                <option :value="3">良好</option>
              </select>
            </div>
            <div class="form-group">
              <label>精神状态</label>
              <select v-model.number="form.mentalStatus">
                <option :value="1">差</option>
                <option :value="2">一般</option>
                <option :value="3">良好</option>
              </select>
            </div>
            <div class="form-group">
              <label>毛发状态</label>
              <select v-model.number="form.hairCondition">
                <option :value="1">差</option>
                <option :value="2">一般</option>
                <option :value="3">良好</option>
              </select>
            </div>
            <div class="form-group">
              <label>粪便状态</label>
              <select v-model.number="form.fecesStatus">
                <option :value="1">差</option>
                <option :value="2">一般</option>
                <option :value="3">良好</option>
              </select>
            </div>
            <div class="form-group">
              <label>疫苗状态</label>
              <select v-model="form.vaccineStatus">
                <option value="未接种">未接种</option>
                <option value="部分">部分</option>
                <option value="已完成">已完成</option>
              </select>
            </div>
            <div class="form-group">
              <label>驱虫状态</label>
              <select v-model="form.dewormingStatus">
                <option value="未驱虫">未驱虫</option>
                <option value="已驱虫">已驱虫</option>
              </select>
            </div>
            <div class="form-group">
              <label>体检日期</label>
              <input v-model="form.inspectionDate" type="date" />
            </div>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="form.remark" placeholder="输入备注信息..." rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showForm = false">取消</button>
          <button class="btn btn-primary" @click="handleSave">保存</button>
        </div>
      </div>
    </div>

    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h3 class="modal-title">健康详情</h3>
          <button class="btn btn-ghost btn-sm" @click="showDetail = false">✕</button>
        </div>
        <div class="modal-body" v-if="detailRecord">
          <div class="detail-score-section">
            <div class="detail-score-ring" :class="scoreClass(detailRecord.healthScore)">
              <span class="detail-score-num">{{ detailRecord.healthScore != null ? detailRecord.healthScore : '-' }}</span>
              <span class="detail-score-label">健康评分</span>
            </div>
            <span class="tag" :class="riskTagClass(detailRecord.riskLevel)">{{ riskText(detailRecord.riskLevel) }}</span>
          </div>

          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">宠物名称</span>
              <span class="detail-value">{{ detailRecord.petName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">主人</span>
              <span class="detail-value">{{ detailRecord.ownerName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">体温</span>
              <span class="detail-value">{{ detailRecord.temperature != null ? detailRecord.temperature + '℃' : '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">体重</span>
              <span class="detail-value">{{ detailRecord.weight != null ? detailRecord.weight + 'kg' : '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">心率</span>
              <span class="detail-value">{{ detailRecord.heartRate != null ? detailRecord.heartRate + 'bpm' : '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">呼吸频率</span>
              <span class="detail-value">{{ detailRecord.respirationRate != null ? detailRecord.respirationRate + '次/分' : '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">食欲</span>
              <span class="detail-value">{{ statusText(detailRecord.appetite) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">精神状态</span>
              <span class="detail-value">{{ statusText(detailRecord.mentalStatus) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">毛发状态</span>
              <span class="detail-value">{{ statusText(detailRecord.hairCondition) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">粪便状态</span>
              <span class="detail-value">{{ statusText(detailRecord.fecesStatus) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">疫苗状态</span>
              <span class="detail-value">{{ detailRecord.vaccineStatus || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">驱虫状态</span>
              <span class="detail-value">{{ detailRecord.dewormingStatus || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">体检日期</span>
              <span class="detail-value">{{ detailRecord.inspectionDate || '-' }}</span>
            </div>
          </div>

          <div v-if="detailRecord.remark" class="detail-remark">
            <span class="detail-label">备注</span>
            <p class="remark-text">{{ detailRecord.remark }}</p>
          </div>

          <div v-if="detailRecord.aiAdvice" class="ai-advice-section">
            <div class="ai-advice-header">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M12 2a7 7 0 0 1 7 7c0 2.38-1.19 4.47-3 5.74V17a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1v-2.26C6.19 13.47 5 11.38 5 9a7 7 0 0 1 7-7z"/><line x1="9" y1="21" x2="15" y2="21"/></svg>
              AI 健康建议
            </div>
            <div class="alert alert-info ai-advice-content">{{ detailRecord.aiAdvice }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 10

const search = ref({
  keyword: '',
  riskLevel: '',
  startDate: '',
  endDate: ''
})

const showForm = ref(false)
const editMode = ref(false)
const form = ref(getDefaultForm())
const showDetail = ref(false)
const detailRecord = ref(null)

const stats = computed(() => {
  const list = records.value
  return {
    total: total.value || list.length,
    healthy: list.filter(r => r.riskLevel === 0).length,
    attention: list.filter(r => r.riskLevel === 1).length,
    highRisk: list.filter(r => r.riskLevel >= 2).length
  }
})

function getDefaultForm() {
  return {
    id: null,
    petId: null,
    weight: null,
    temperature: null,
    heartRate: null,
    respirationRate: null,
    appetite: 3,
    mentalStatus: 3,
    hairCondition: 3,
    fecesStatus: 3,
    vaccineStatus: '未接种',
    dewormingStatus: '未驱虫',
    remark: '',
    inspectionDate: new Date().toISOString().slice(0, 10)
  }
}

function statusText(val) {
  if (val == null) return '-'
  const map = { 1: '差', 2: '一般', 3: '良好' }
  if (typeof val === 'number') return map[val] || String(val)
  return val
}

function riskText(level) {
  const map = { 0: '健康', 1: '注意', 2: '风险', 3: '高风险' }
  return map[level] ?? '未知'
}

function riskTagClass(level) {
  const map = { 0: 'tag-success', 1: 'tag-warning', 2: 'tag-accent', 3: 'tag-danger' }
  return map[level] ?? 'tag-info'
}

function scoreClass(score) {
  if (score == null) return ''
  if (score >= 90) return 'score-success'
  if (score >= 75) return 'score-primary'
  if (score >= 60) return 'score-warning'
  return 'score-danger'
}

async function loadData() {
  try {
    const params = {
      page: currentPage.value,
      pageSize,
      keyword: search.value.keyword || undefined,
      riskLevel: search.value.riskLevel || undefined,
      startDate: search.value.startDate || undefined,
      endDate: search.value.endDate || undefined
    }
    const res = await adminApi.healthPage(params)
    if (res.code === 200) {
      records.value = res.data?.records || res.data?.list || []
      total.value = res.data?.total || 0
      totalPages.value = res.data?.totalPages || res.data?.pages || Math.ceil(total.value / pageSize) || 1
    }
  } catch {}
}

function resetSearch() {
  search.value = { keyword: '', riskLevel: '', startDate: '', endDate: '' }
  currentPage.value = 1
  loadData()
}

function openAdd() {
  editMode.value = false
  form.value = getDefaultForm()
  showForm.value = true
}

function openEdit(record) {
  editMode.value = true
  form.value = { ...getDefaultForm(), ...record }
  showForm.value = true
}

async function handleSave() {
  if (!form.value.petId) {
    alert('请输入宠物ID')
    return
  }
  try {
    const res = editMode.value
      ? await adminApi.healthUpdate(form.value)
      : await adminApi.healthCreate(form.value)
    if (res.code === 200) {
      showForm.value = false
      loadData()
    } else {
      alert(res.message || '操作失败')
    }
  } catch {
    alert('请求失败')
  }
}

async function handleDelete(id) {
  if (!confirm('确定删除该健康记录？')) return
  try {
    const res = await adminApi.healthDelete(id)
    if (res.code === 200) {
      loadData()
    } else {
      alert(res.message || '删除失败')
    }
  } catch {
    alert('请求失败')
  }
}

async function viewDetail(record) {
  try {
    const res = await adminApi.healthDetail(record.id)
    if (res.code === 200) {
      detailRecord.value = res.data || record
    } else {
      detailRecord.value = record
    }
  } catch {
    detailRecord.value = record
  }
  showDetail.value = true
}

onMounted(loadData)
</script>

<style scoped>
.health-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-4);
}

.page-title {
  margin: 0;
}

.search-card {
  padding: var(--space-4);
}

.search-row {
  display: flex;
  gap: var(--space-3);
  align-items: flex-end;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  transition: all var(--transition-base);
}

.stat-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-total {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.stat-icon-healthy {
  background: var(--color-success-bg);
  color: var(--color-success);
}

.stat-icon-attention {
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.stat-icon-danger {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-info .stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  line-height: 1.2;
}

.stat-info .stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.card-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0;
}

.table-wrap {
  overflow-x: auto;
}

.td-name {
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.td-actions {
  display: flex;
  gap: var(--space-1);
}

.score-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 28px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  padding: 0 8px;
}

.score-success {
  background: var(--color-success-bg);
  color: #15803d;
}

.score-primary {
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
}

.score-warning {
  background: var(--color-warning-bg);
  color: #92400e;
}

.score-danger {
  background: var(--color-danger-bg);
  color: #991b1b;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border-light);
}

.page-info {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.form-modal {
  width: 640px;
  max-width: 95vw;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.modal-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0;
}

.modal-body {
  margin-bottom: var(--space-4);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3) var(--space-4);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.detail-modal {
  width: 720px;
  max-width: 95vw;
}

.detail-score-section {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.detail-score-ring {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 4px solid var(--border-color);
  flex-shrink: 0;
}

.detail-score-ring.score-success {
  border-color: var(--color-success);
  background: var(--color-success-bg);
}

.detail-score-ring.score-primary {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}

.detail-score-ring.score-warning {
  border-color: var(--color-warning);
  background: var(--color-warning-bg);
}

.detail-score-ring.score-danger {
  border-color: var(--color-danger);
  background: var(--color-danger-bg);
}

.detail-score-num {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  line-height: 1;
}

.detail-score-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-weight: var(--font-weight-semibold);
}

.detail-value {
  font-size: var(--font-size-base);
  color: var(--text-title);
  font-weight: var(--font-weight-medium);
}

.detail-remark {
  margin-bottom: var(--space-4);
  padding: var(--space-3);
  background: var(--bg-hover);
  border-radius: var(--radius-md);
}

.remark-text {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin: var(--space-1) 0 0 0;
  line-height: 1.6;
}

.ai-advice-section {
  margin-top: var(--space-3);
}

.ai-advice-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  margin-bottom: var(--space-2);
}

.ai-advice-content {
  margin-bottom: 0;
  line-height: 1.7;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .search-row {
    flex-wrap: wrap;
  }

  .search-row .form-group {
    width: 100% !important;
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
