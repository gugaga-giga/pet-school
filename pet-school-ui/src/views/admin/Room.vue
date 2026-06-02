<template>
  <div class="room-page">
    <div class="page-header">
      <h1 class="page-title">房型管理</h1>
      <p class="page-desc">管理房间类型与房间信息</p>
    </div>

    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">房型列表</h3>
            <div class="card-actions">
              <button class="btn btn-ghost btn-sm" @click="loadTypes">
                <span class="action-icon">↻</span> 刷新
              </button>
              <button class="btn btn-primary btn-sm" @click="openAddType"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> 新增房型</button>
            </div>
          </div>
          <div class="table-wrap" v-if="types.length">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>房型名</th>
                  <th>日价</th>
                  <th>容量</th>
                  <th>设施</th>
                  <th style="text-align:right">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="t in types" :key="t.id" :class="{ 'row-selected': selectedType && selectedType.id === t.id }">
                  <td><span class="cell-id">#{{ t.id }}</span></td>
                  <td><span class="cell-name">{{ t.name }}</span></td>
                  <td><span class="cell-price">¥{{ t.dailyPrice }}</span></td>
                  <td><span class="cell-capacity">{{ t.capacity || 0 }} 间</span></td>
                  <td><span class="cell-desc">{{ t.facilities || '-' }}</span></td>
                  <td class="cell-actions">
                    <button class="btn btn-ghost btn-sm" @click="viewRooms(t)">查看房间</button>
                    <button class="btn btn-ghost btn-sm" @click="openEditType(t)">编辑</button>
                    <button class="btn btn-danger btn-sm" @click="handleDeleteType(t)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="empty-state" v-else>
            <div class="empty-icon">🏠</div>
            <div class="empty-text">暂无房型数据</div>
          </div>
        </div>

        <div v-if="selectedType" class="card">
          <div class="card-header">
            <h3 class="card-title">
              {{ selectedType.name }}
              <span class="card-subtitle">房间列表</span>
            </h3>
            <div class="card-actions">
              <button class="btn btn-ghost btn-sm" @click="loadRooms">
                <span class="action-icon">↻</span> 刷新
              </button>
              <button class="btn btn-primary btn-sm" @click="openAddRoom"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> 新增房间</button>
            </div>
          </div>
          <div class="table-wrap" v-if="rooms.length">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>房间号</th>
                  <th>状态</th>
                  <th style="text-align:right">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in rooms" :key="r.id">
                  <td><span class="cell-id">#{{ r.id }}</span></td>
                  <td><span class="cell-name">{{ r.roomNumber }}</span></td>
                  <td>
                    <span :class="['tag', roomStatusTag(r.status)]">{{ roomStatusText(r.status) }}</span>
                  </td>
                  <td class="cell-actions">
                    <button class="btn btn-ghost btn-sm" @click="openEditRoom(r)">编辑</button>
                    <button class="btn btn-danger btn-sm" @click="handleDeleteRoom(r)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="empty-state" v-else>
            <div class="empty-icon">🚪</div>
            <div class="empty-text">暂无房间数据</div>
          </div>
        </div>
      </div>

      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">{{ isTypeEdit ? '编辑房型' : '新增房型' }}</h3>
            <span v-if="isTypeEdit" class="tag tag-accent">编辑中</span>
          </div>
          <div class="form-group">
            <label>房型名称</label>
            <input v-model="typeForm.name" placeholder="如 豪华单间" />
          </div>
          <div class="row" style="gap:var(--space-3)">
            <div class="col form-group" style="margin-bottom:0">
              <label>日价（元）</label>
              <input v-model.number="typeForm.dailyPrice" type="number" min="0" step="0.01" placeholder="如 299.00" />
            </div>
            <div class="col form-group" style="margin-bottom:0">
              <label>可订容量（间）</label>
              <input v-model.number="typeForm.capacity" type="number" min="0" placeholder="剩余可预订房间数" />
            </div>
          </div>
          <p class="form-hint">剩余可预订数量，下单时自动-1，取消时自动+1，为0时前台显示不可订</p>
          <div class="form-group">
            <label>设施描述</label>
            <textarea v-model="typeForm.facilities" rows="3" placeholder="如 独立空调、24h监控、每日清洁"></textarea>
          </div>
          <div class="form-group">
            <label>图片URL</label>
            <input v-model="typeForm.image" placeholder="图片地址（选填）" />
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" @click="handleSaveType">{{ isTypeEdit ? '保存修改' : '添加房型' }}</button>
            <button v-if="isTypeEdit" class="btn btn-secondary" @click="resetTypeForm">取消编辑</button>
          </div>
          <div v-if="typeMsg" :class="['alert', typeMsgType==='success'?'alert-success':'alert-error']" style="margin-top:var(--space-3)">{{ typeMsg }}</div>
        </div>

        <div v-if="selectedType" class="card">
          <div class="card-header">
            <h3 class="card-title">
              {{ isRoomEdit ? '编辑房间' : '新增房间' }}
              <span class="card-subtitle">{{ selectedType.name }}</span>
            </h3>
            <span v-if="isRoomEdit" class="tag tag-accent">编辑中</span>
          </div>
          <div class="form-group">
            <label>房间号</label>
            <input v-model="roomForm.roomNumber" placeholder="如 A101" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="roomForm.status">
              <option :value="0">空闲</option>
              <option :value="1">已占用</option>
              <option :value="2">维护中</option>
            </select>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" @click="handleSaveRoom">{{ isRoomEdit ? '保存修改' : '添加房间' }}</button>
            <button v-if="isRoomEdit" class="btn btn-secondary" @click="resetRoomForm">取消编辑</button>
          </div>
          <div v-if="roomMsg" :class="['alert', roomMsgType==='success'?'alert-success':'alert-error']" style="margin-top:var(--space-3)">{{ roomMsg }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'

const types = ref([])
const rooms = ref([])
const selectedType = ref(null)
const isTypeEdit = ref(false)
const typeMsg = ref('')
const typeMsgType = ref('success')
const typeForm = ref({ id: null, name: '', dailyPrice: null, capacity: 0, facilities: '', image: '' })

const isRoomEdit = ref(false)
const roomMsg = ref('')
const roomMsgType = ref('success')
const roomForm = ref({ id: null, typeId: null, roomNumber: '', status: 0 })

async function loadTypes() {
  try {
    const res = await adminApi.roomTypeList()
    if (res.code === 200) types.value = res.data
  } catch (e) { console.error(e) }
}

async function loadRooms() {
  if (!selectedType.value) return
  try {
    const res = await adminApi.roomList(selectedType.value.id)
    if (res.code === 200) rooms.value = res.data
  } catch (e) { console.error(e) }
}

function openAddType() {
  isTypeEdit.value = false
  typeForm.value = { id: null, name: '', dailyPrice: null, capacity: 0, facilities: '', image: '' }
  typeMsg.value = ''
}

function openEditType(t) {
  isTypeEdit.value = true
  typeForm.value = { ...t }
  typeMsg.value = ''
}

function resetTypeForm() {
  openAddType()
}

async function handleSaveType() {
  const f = typeForm.value
  if (!f.name || f.dailyPrice === null) {
    typeMsg.value = '房型名称和日价为必填项'
    typeMsgType.value = 'error'
    return
  }
  try {
    let res
    if (isTypeEdit.value) {
      res = await adminApi.updateRoomType(f)
    } else {
      res = await adminApi.addRoomType(f)
    }
    if (res.code === 200) {
      typeMsg.value = isTypeEdit.value ? '修改成功' : '添加成功'
      typeMsgType.value = 'success'
      openAddType()
      loadTypes()
    } else {
      typeMsg.value = res.message || '操作失败'
      typeMsgType.value = 'error'
    }
  } catch (e) {
    typeMsg.value = '请求失败'
    typeMsgType.value = 'error'
  }
}

async function handleDeleteType(t) {
  if (!confirm(`确定删除房型「${t.name}」吗？该房型下的房间也将被删除。`)) return
  try {
    const res = await adminApi.deleteRoomType(t.id)
    if (res.code === 200) {
      if (selectedType.value && selectedType.value.id === t.id) {
        selectedType.value = null
        rooms.value = []
      }
      loadTypes()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (e) {
    alert('删除请求失败')
  }
}

function viewRooms(t) {
  selectedType.value = t
  resetRoomForm()
  loadRooms()
}

function openAddRoom() {
  isRoomEdit.value = false
  roomForm.value = { id: null, typeId: selectedType.value.id, roomNumber: '', status: 0 }
  roomMsg.value = ''
}

function openEditRoom(r) {
  isRoomEdit.value = true
  roomForm.value = { ...r }
  roomMsg.value = ''
}

function resetRoomForm() {
  isRoomEdit.value = false
  roomForm.value = { id: null, typeId: selectedType.value ? selectedType.value.id : null, roomNumber: '', status: 0 }
  roomMsg.value = ''
}

async function handleSaveRoom() {
  const f = roomForm.value
  if (!f.roomNumber) {
    roomMsg.value = '房间号为必填项'
    roomMsgType.value = 'error'
    return
  }
  try {
    let res
    if (isRoomEdit.value) {
      res = await adminApi.updateRoom(f)
    } else {
      f.typeId = selectedType.value.id
      res = await adminApi.addRoom(f)
    }
    if (res.code === 200) {
      roomMsg.value = isRoomEdit.value ? '修改成功' : '添加成功'
      roomMsgType.value = 'success'
      resetRoomForm()
      loadRooms()
    } else {
      roomMsg.value = res.message || '操作失败'
      roomMsgType.value = 'error'
    }
  } catch (e) {
    roomMsg.value = '请求失败'
    roomMsgType.value = 'error'
  }
}

async function handleDeleteRoom(r) {
  if (!confirm(`确定删除房间「${r.roomNumber}」吗？`)) return
  try {
    const res = await adminApi.deleteRoom(r.id)
    if (res.code === 200) {
      loadRooms()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (e) {
    alert('删除请求失败')
  }
}

function roomStatusText(status) {
  return ['空闲', '已占用', '维护中'][status] || '未知'
}

function roomStatusTag(status) {
  if (status === 0) return 'tag-success'
  if (status === 1) return 'tag-danger'
  return 'tag-warning'
}

onMounted(() => loadTypes())
</script>

<style scoped>
.room-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-5);
}

.page-desc {
  font-size: var(--font-size-base);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0;
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
}

.card-subtitle {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-normal);
  color: var(--text-muted);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.action-icon {
  font-size: var(--font-size-sm);
}

.table-wrap {
  overflow-x: auto;
  margin: 0 calc(var(--space-5) * -1);
  padding: 0 var(--space-5);
}

.row-selected td {
  background: var(--color-primary-bg) !important;
}

.cell-id {
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.cell-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
}

.cell-price {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.cell-capacity {
  font-weight: var(--font-weight-semibold);
  color: var(--color-accent);
}

.cell-desc {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
  max-width: 160px;
}

.cell-actions {
  text-align: right;
  white-space: nowrap;
}

.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.form-hint {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin: var(--space-1) 0 var(--space-3);
  line-height: 1.5;
}
</style>
