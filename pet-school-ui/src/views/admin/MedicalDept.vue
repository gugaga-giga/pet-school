<template>
  <div class="dept-page">
    <div class="page-header">
      <h1 class="page-title">🏥 科室管理</h1>
      <button class="btn btn-primary" @click="openCreate">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增科室
      </button>
    </div>

    <div class="card search-card">
      <div class="search-row">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input v-model="search.keyword" class="search-input" placeholder="搜索科室名称…" @keyup.enter="doSearch" />
        </div>
        <select v-model="search.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="1">启用</option>
          <option value="0">禁用</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="doSearch">搜索</button>
        <button class="btn btn-secondary btn-sm" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="card table-card">
      <div class="table-toolbar">
        <span class="record-count">共 <strong>{{ list.length }}</strong> 条记录</span>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>科室名称</th>
              <th>描述</th>
              <th>图标</th>
              <th>价格</th>
              <th>排序</th>
              <th>状态</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!list.length">
              <td colspan="7">
                <div class="empty-state">
                  <div class="empty-icon">🏥</div>
                  <div class="empty-text">暂无科室数据</div>
                </div>
              </td>
            </tr>
            <tr v-for="item in list" :key="item.id" class="table-row">
              <td class="td-name">{{ item.name || '-' }}</td>
              <td>{{ item.description || '-' }}</td>
              <td>{{ item.icon || '-' }}</td>
              <td class="td-price">{{ item.price != null ? '¥' + item.price : '-' }}</td>
              <td>{{ item.sort ?? '-' }}</td>
              <td><span :class="['tag', item.status === 1 ? 'tag-success' : 'tag-danger']">{{ item.status === 1 ? '启用' : '禁用' }}</span></td>
              <td class="col-actions">
                <div class="action-group">
                  <button class="btn btn-ghost btn-sm" @click="handleEdit(item)">编辑</button>
                  <button :class="['btn', 'btn-sm', item.status === 1 ? 'btn-secondary' : 'btn-primary']" @click="toggleStatus(item)">{{ item.status === 1 ? '禁用' : '启用' }}</button>
                  <button class="btn btn-danger btn-sm" @click="handleDelete(item.id)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content form-modal">
        <div class="modal-header">
          <h3>{{ editMode ? '编辑科室' : '新增科室' }}</h3>
          <button class="btn-icon modal-close" @click="showModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="form-body">
          <div class="form-group">
            <label>科室名称</label>
            <input v-model="form.name" placeholder="请输入科室名称" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" placeholder="请输入科室描述" rows="3"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group col">
              <label>图标</label>
              <input v-model="form.icon" placeholder="如：🏥" />
            </div>
            <div class="form-group col">
              <label>价格</label>
              <input v-model.number="form.price" type="number" placeholder="0" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group col">
              <label>排序</label>
              <input v-model.number="form.sort" type="number" placeholder="0" />
            </div>
            <div class="form-group col">
              <label>状态</label>
              <select v-model.number="form.status">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="handleSave">{{ editMode ? '保存修改' : '新增' }}</button>
        </div>
        <div v-if="formMsg" :class="['alert', formMsgType === 'success' ? 'alert-success' : 'alert-error']" style="margin: var(--space-3) var(--space-5) 0">{{ formMsg }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'

const list = ref([])
const search = ref({ keyword: '', status: '' })
const showModal = ref(false)
const editMode = ref(false)
const formMsg = ref('')
const formMsgType = ref('success')
const form = ref({ id: null, name: '', description: '', icon: '', price: 0, sort: 0, status: 1 })

function resetForm() {
  form.value = { id: null, name: '', description: '', icon: '', price: 0, sort: 0, status: 1 }
  formMsg.value = ''
}

function openCreate() {
  resetForm()
  editMode.value = false
  showModal.value = true
}

function handleEdit(item) {
  editMode.value = true
  form.value = { id: item.id, name: item.name || '', description: item.description || '', icon: item.icon || '', price: item.price ?? 0, sort: item.sort ?? 0, status: item.status ?? 1 }
  formMsg.value = ''
  showModal.value = true
}

function doSearch() {
  loadData()
}

function resetSearch() {
  search.value = { keyword: '', status: '' }
  loadData()
}

async function loadData() {
  try {
    const res = await adminApi.departmentList()
    let data = res.data || []
    if (search.value.keyword) {
      data = data.filter(d => (d.name || '').includes(search.value.keyword))
    }
    if (search.value.status !== '') {
      data = data.filter(d => String(d.status) === search.value.status)
    }
    list.value = data
  } catch {
    list.value = []
  }
}

async function handleSave() {
  if (!form.value.name) { formMsg.value = '请输入科室名称'; formMsgType.value = 'error'; return }
  try {
    const res = editMode.value
      ? await adminApi.departmentUpdate(form.value)
      : await adminApi.departmentAdd(form.value)
    if (res.code === 200) {
      formMsg.value = editMode.value ? '修改成功' : '新增成功'
      formMsgType.value = 'success'
      setTimeout(() => { showModal.value = false; loadData() }, 800)
    } else {
      formMsg.value = res.message || '操作失败'
      formMsgType.value = 'error'
    }
  } catch {
    formMsg.value = '请求失败'
    formMsgType.value = 'error'
  }
}

async function handleDelete(id) {
  if (!confirm('确认删除该科室？')) return
  try {
    await adminApi.departmentDelete(id)
    loadData()
  } catch {}
}

async function toggleStatus(item) {
  try {
    await adminApi.departmentUpdate({ id: item.id, status: item.status === 1 ? 0 : 1 })
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.dept-page {
  padding: var(--space-6);
  max-width: 1280px;
  margin: 0 auto;
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

.filter-select {
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

.filter-select:focus {
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

.td-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
}

.td-price {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.col-actions {
  width: 220px;
}

.action-group {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-wrap: wrap;
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
