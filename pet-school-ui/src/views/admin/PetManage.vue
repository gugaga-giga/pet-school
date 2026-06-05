<template>
  <div class="pet-manage-page">
    <div class="page-header">
      <h1 class="page-title">宠物管理</h1>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-card stat-total">
        <div class="stat-icon">🐾</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">宠物总数</div>
        </div>
      </div>
      <div class="stat-card stat-dog">
        <div class="stat-icon">🐶</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.dogCount }}</div>
          <div class="stat-label">狗数量</div>
        </div>
      </div>
      <div class="stat-card stat-cat">
        <div class="stat-icon">🐱</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.catCount }}</div>
          <div class="stat-label">猫数量</div>
        </div>
      </div>
      <div class="stat-card stat-today">
        <div class="stat-icon">✨</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayNew }}</div>
          <div class="stat-label">今日新增</div>
        </div>
      </div>
    </div>

    <!-- 搜索/筛选栏 -->
    <div class="card search-card">
      <div class="search-row">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            v-model="search.keyword"
            class="search-input"
            placeholder="搜索宠物名称 / 主人 / 品种…"
            @keyup.enter="doSearch"
          />
        </div>
        <select v-model="search.petType" class="filter-select">
          <option value="">全部类型</option>
          <option value="dog">狗</option>
          <option value="cat">猫</option>
          <option value="other">其他</option>
        </select>
        <select v-model="search.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="0">正常</option>
          <option value="1">已离世</option>
          <option value="2">已转让</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="doSearch">搜索</button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="card table-card">
      <div class="table-toolbar">
        <span class="record-count">共 <strong>{{ total }}</strong> 条</span>
      </div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>头像</th>
              <th>宠物名称</th>
              <th>主人</th>
              <th>类型</th>
              <th>品种</th>
              <th>年龄</th>
              <th>体重</th>
              <th>状态</th>
              <th>创建时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!pets.length">
              <td colspan="10">
                <div class="empty-state">
                  <div class="empty-icon">🐾</div>
                  <p>暂无宠物数据</p>
                </div>
              </td>
            </tr>
            <tr v-for="pet in pets" :key="pet.id" class="table-row">
              <td>
                <img
                  v-if="pet.avatar"
                  :src="pet.avatar"
                  class="pet-avatar"
                  alt=""
                />
                <div v-else class="pet-avatar avatar-placeholder">🐾</div>
              </td>
              <td class="cell-name">{{ pet.name }}</td>
              <td>{{ pet.username || pet.userId || '-' }}</td>
              <td><span class="tag" :class="typeTagClass(pet.petType)">{{ typeText(pet.petType) }}</span></td>
              <td>{{ pet.breed || '-' }}</td>
              <td>{{ pet.age != null ? pet.age : '-' }}</td>
              <td>{{ pet.weight != null ? pet.weight + 'kg' : '-' }}</td>
              <td><span class="tag" :class="statusTagClass(pet.status)">{{ statusText(pet.status) }}</span></td>
              <td class="col-time">{{ formatTime(pet.createTime || pet.createdAt) }}</td>
              <td class="col-actions">
                <div class="action-group">
                  <button class="btn btn-ghost btn-sm" @click="viewPet(pet.id)">查看</button>
                  <button class="btn btn-ghost btn-sm" @click="openEdit(pet)">编辑</button>
                  <button class="btn btn-ghost btn-sm text-danger" @click="deletePet(pet)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination">
        <button class="page-pill" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">&laquo;</button>
        <button
          v-for="p in visiblePages"
          :key="p"
          class="page-pill"
          :class="{ active: p === currentPage }"
          @click="goPage(p)"
        >{{ p }}</button>
        <button class="page-pill" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">&raquo;</button>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="editVisible" class="modal-overlay" @click.self="closeEdit">
      <div class="modal-content edit-modal">
        <div class="modal-header">
          <h3>编辑宠物</h3>
          <button class="btn-icon" @click="closeEdit">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="edit-body">
          <div class="form-grid">
            <div class="form-group">
              <label>名称</label>
              <input v-model="editForm.name" placeholder="宠物名称" />
            </div>
            <div class="form-group">
              <label>类型</label>
              <select v-model="editForm.petType">
                <option value="dog">狗</option>
                <option value="cat">猫</option>
                <option value="other">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>品种</label>
              <input v-model="editForm.breed" placeholder="品种" />
            </div>
            <div class="form-group">
              <label>性别</label>
              <select v-model="editForm.gender">
                <option :value="1">公</option>
                <option :value="0">母</option>
              </select>
            </div>
            <div class="form-group">
              <label>生日</label>
              <input v-model="editForm.birthday" type="date" />
            </div>
            <div class="form-group">
              <label>体重(kg)</label>
              <input v-model.number="editForm.weight" type="number" step="0.1" placeholder="体重" />
            </div>
            <div class="form-group">
              <label>毛色</label>
              <input v-model="editForm.color" placeholder="毛色" />
            </div>
            <div class="form-group">
              <label>是否绝育</label>
              <select v-model="editForm.sterilized">
                <option :value="1">是</option>
                <option :value="0">否</option>
              </select>
            </div>
            <div class="form-group">
              <label>血型</label>
              <input v-model="editForm.bloodType" placeholder="血型" />
            </div>
            <div class="form-group">
              <label>芯片号</label>
              <input v-model="editForm.microchipNo" placeholder="芯片号" />
            </div>
            <div class="form-group">
              <label>过敏信息</label>
              <input v-model="editForm.allergyInfo" placeholder="过敏信息" />
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="editForm.status">
                <option :value="0">正常</option>
                <option :value="1">已离世</option>
                <option :value="2">已转让</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="editForm.remark" placeholder="备注信息" rows="3"></textarea>
          </div>
          <div v-if="editMsg" :class="['alert', editMsgType === 'success' ? 'alert-success' : 'alert-error']">
            {{ editMsg }}
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" @click="saveEdit" :disabled="editSaving">
              {{ editSaving ? '保存中…' : '保存' }}
            </button>
            <button class="btn btn-secondary" @click="closeEdit">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../../api'

const router = useRouter()

const pets = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const totalPages = ref(0)

const search = ref({ keyword: '', petType: '', status: '' })

const stats = ref({ total: 0, dogCount: 0, catCount: 0, todayNew: 0 })

const editVisible = ref(false)
const editForm = ref({})
const editSaving = ref(false)
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

function typeText(t) {
  const map = { dog: '狗', cat: '猫', other: '其他' }
  return map[t] ?? t ?? '-'
}

function typeTagClass(t) {
  const map = { dog: 'tag-info', cat: 'tag-accent', other: 'tag-warning' }
  return map[t] ?? 'tag-info'
}

function statusText(s) {
  const map = { 0: '正常', 1: '已离世', 2: '已转让' }
  return map[s] ?? '未知'
}

function statusTagClass(s) {
  const map = { 0: 'tag-success', 1: 'tag-info', 2: 'tag-warning' }
  return map[s] ?? ''
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function doSearch() {
  currentPage.value = 1
  loadData()
}

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadData()
}

function viewPet(id) {
  router.push(`/admin/pet/detail/${id}`)
}

function openEdit(pet) {
  editForm.value = { ...pet }
  editMsg.value = ''
  editVisible.value = true
}

function closeEdit() {
  editVisible.value = false
  editForm.value = {}
  editMsg.value = ''
}

async function saveEdit() {
  editSaving.value = true
  editMsg.value = ''
  try {
    const res = await adminApi.petUpdate(editForm.value)
    if (res.code === 200) {
      editMsg.value = res.message || '保存成功'
      editMsgType.value = 'success'
      loadData()
    } else {
      editMsg.value = res.message || '保存失败'
      editMsgType.value = 'error'
    }
  } catch (e) {
    editMsg.value = '保存失败: ' + (e.response?.data?.message || e.message)
    editMsgType.value = 'error'
  } finally {
    editSaving.value = false
  }
}

async function deletePet(pet) {
  if (!confirm(`确认删除宠物「${pet.name}」？`)) return
  try {
    const res = await adminApi.petDelete(pet.id)
    if (res.code === 200) {
      loadData()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '删除失败'
    alert(msg)
  }
}

async function loadStats() {
  try {
    const res = await adminApi.petStats()
    if (res.code === 200 && res.data) {
      stats.value = {
        total: res.data.total || 0,
        dogCount: res.data.dogCount || 0,
        catCount: res.data.catCount || 0,
        todayNew: res.data.todayNew || 0
      }
    }
  } catch {}
}

async function loadData() {
  const params = {
    keyword: search.value.keyword,
    petType: search.value.petType,
    status: search.value.status,
    page: currentPage.value,
    pageSize
  }
  try {
    const res = await adminApi.petPage(params)
    if (res.code === 200 && res.data) {
      pets.value = res.data.list || []
      total.value = res.data.total || 0
      totalPages.value = Math.ceil(total.value / pageSize) || 0
    }
  } catch {
    pets.value = []
    total.value = 0
    totalPages.value = 0
  }
}

onMounted(() => {
  loadStats()
  loadData()
})
</script>

<style scoped>
.pet-manage-page {
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
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0;
}

/* 统计卡片 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
}

.stat-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.stat-total {
  background: linear-gradient(135deg, var(--bg-card) 0%, var(--color-primary-bg) 100%);
}

.stat-dog {
  background: linear-gradient(135deg, var(--bg-card) 0%, var(--color-accent-bg) 100%);
}

.stat-cat {
  background: linear-gradient(135deg, var(--bg-card) 0%, var(--color-success-bg) 100%);
}

.stat-today {
  background: linear-gradient(135deg, var(--bg-card) 0%, #F3E8FF 100%);
}

.stat-icon {
  font-size: var(--font-size-3xl);
  line-height: 1;
  flex-shrink: 0;
}

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

/* 搜索栏 */
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

/* 表格 */
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

.table-row:hover td {
  border-bottom-color: var(--color-primary-bg);
}

.pet-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-hover);
  font-size: var(--font-size-base);
  line-height: 1;
}

.cell-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
}

.col-time {
  white-space: nowrap;
  font-size: var(--font-size-xs);
  color: var(--text-body);
}

.col-actions {
  width: 160px;
  text-align: center !important;
}

.action-group {
  display: flex;
  gap: var(--space-1);
  justify-content: flex-end;
  flex-wrap: wrap;
}

.text-danger {
  color: var(--color-danger) !important;
}

.text-danger:hover {
  background: var(--color-danger-bg) !important;
}

/* 分页 */
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

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-overlay);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.edit-modal {
  width: 640px;
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
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.modal-header .btn-icon {
  color: var(--text-muted);
  transition: color var(--transition-base);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
}

.modal-header .btn-icon:hover {
  color: var(--text-title);
  background: var(--bg-hover);
}

.edit-body {
  padding: var(--space-5) var(--space-6);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 var(--space-4);
}

.form-actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.empty-state {
  padding: var(--space-8) 0;
  text-align: center;
  color: var(--text-muted);
}

.empty-state .empty-icon {
  font-size: var(--font-size-3xl);
  margin-bottom: var(--space-3);
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: var(--font-size-sm);
}

@media (max-width: 768px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
