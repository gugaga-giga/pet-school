<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>💰 钱包管理</h2>
    </div>

    <div class="toolbar">
      <input v-model="keyword" class="search-input" placeholder="搜索用户名/手机号" @keyup.enter="loadData" />
      <button class="btn btn-primary btn-sm" @click="loadData">搜索</button>
      <button class="btn btn-secondary btn-sm" @click="keyword = ''; loadData()">重置</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>用户</th>
          <th>余额</th>
          <th>累计充值</th>
          <th>累计消费</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="w in wallets" :key="w.id">
          <td>{{ w.id }}</td>
          <td>{{ w.username || ('用户#' + w.userId) }}</td>
          <td class="money">¥{{ (w.balance || 0).toFixed(2) }}</td>
          <td class="money">¥{{ (w.totalRecharge || 0).toFixed(2) }}</td>
          <td class="money">¥{{ (w.totalConsume || 0).toFixed(2) }}</td>
          <td><span :class="['status-tag', w.status === 1 ? 'status-active' : 'status-frozen']">{{ w.status === 1 ? '正常' : '冻结' }}</span></td>
          <td>{{ w.createTime }}</td>
          <td>
            <div class="action-group">
              <button class="btn btn-ghost btn-sm" @click="openAdjust(w)">调整</button>
              <button class="btn btn-ghost btn-sm" @click="toggleStatus(w)">{{ w.status === 1 ? '冻结' : '启用' }}</button>
            </div>
          </td>
        </tr>
        <tr v-if="wallets.length === 0"><td colspan="8" class="empty">暂无数据</td></tr>
      </tbody>
    </table>

    <div class="pagination" v-if="totalPages > 1">
      <button class="page-pill" :disabled="page <= 1" @click="page--; loadData()">«</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button class="page-pill" :disabled="page >= totalPages" @click="page++; loadData()">»</button>
    </div>

    <div v-if="showAdjustModal" class="modal-overlay" @click.self="showAdjustModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>余额调整 - {{ adjustWallet?.username || ('用户#' + adjustWallet?.userId) }}</h3>
          <button class="btn-icon modal-close" @click="showAdjustModal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>当前余额</label>
            <div class="current-balance">¥{{ (adjustWallet?.balance || 0).toFixed(2) }}</div>
          </div>
          <div class="form-group">
            <label>调整金额（正数增加，负数扣减）</label>
            <input v-model.number="adjustAmount" type="number" class="form-input" placeholder="请输入金额" />
          </div>
          <div class="form-group">
            <label>调整原因</label>
            <input v-model="adjustRemark" class="form-input" placeholder="请输入原因" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showAdjustModal = false">取消</button>
          <button class="btn btn-primary" @click="doAdjust">确认调整</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { adminApi } from '../../api/index.js'

const wallets = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize))

const showAdjustModal = ref(false)
const adjustWallet = ref(null)
const adjustAmount = ref(null)
const adjustRemark = ref('')

const loadData = async () => {
  try {
    const res = await adminApi.walletPage({ keyword: keyword.value, page: page.value, pageSize })
    if (res.code === 200) {
      wallets.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
}

const openAdjust = (w) => {
  adjustWallet.value = w
  adjustAmount.value = null
  adjustRemark.value = ''
  showAdjustModal.value = true
}

const doAdjust = async () => {
  if (!adjustAmount.value) { alert('请输入调整金额'); return }
  try {
    const res = await adminApi.walletAdjust({ walletId: adjustWallet.value.id, amount: adjustAmount.value, remark: adjustRemark.value })
    if (res.code === 200) {
      alert('调整成功')
      showAdjustModal.value = false
      loadData()
    } else {
      alert(res.message || '调整失败')
    }
  } catch (e) { alert('调整失败') }
}

const toggleStatus = async (w) => {
  const newStatus = w.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '冻结' : '启用'
  if (!confirm(`确认${action}该钱包？`)) return
  try {
    const res = await adminApi.walletStatus({ walletId: w.id, status: newStatus })
    if (res.code === 200) {
      alert(`${action}成功`)
      loadData()
    } else {
      alert(res.message || '操作失败')
    }
  } catch (e) { alert('操作失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.admin-page { padding: var(--space-6); }
.page-header { margin-bottom: var(--space-6); }
.page-header h2 { font-size: var(--font-size-xl); font-weight: var(--font-weight-bold); color: var(--text-title); }
.toolbar { display: flex; gap: var(--space-2); margin-bottom: var(--space-4); }
.search-input { padding: 8px 14px; border: 1.5px solid var(--border-color); border-radius: var(--radius-md); font-size: var(--font-size-sm); font-family: var(--font-family); outline: none; min-width: 200px; }
.search-input:focus { border-color: var(--color-primary); }
.data-table { width: 100%; border-collapse: collapse; background: var(--bg-card); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.data-table th { padding: var(--space-3) var(--space-4); text-align: left; font-size: var(--font-size-xs); font-weight: var(--font-weight-semibold); color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid var(--border-light); background: var(--bg-page); }
.data-table td { padding: var(--space-3) var(--space-4); font-size: var(--font-size-sm); color: var(--text-body); border-bottom: 1px solid var(--border-light); }
.data-table tr:hover td { background: var(--bg-hover); }
.money { font-weight: var(--font-weight-semibold); font-variant-numeric: tabular-nums; }
.empty { text-align: center; color: var(--text-muted); padding: var(--space-8) !important; }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: var(--radius-full); font-size: var(--font-size-xs); font-weight: var(--font-weight-semibold); }
.status-active { background: var(--color-success-bg); color: var(--color-success-text); }
.status-frozen { background: var(--color-danger-bg); color: var(--color-danger-text); }
.action-group { display: flex; gap: var(--space-2); }
.pagination { display: flex; align-items: center; justify-content: center; gap: var(--space-3); margin-top: var(--space-4); }
.page-pill { padding: 6px 14px; border: 1px solid var(--border-color); border-radius: var(--radius-md); background: var(--bg-card); cursor: pointer; font-size: var(--font-size-sm); }
.page-pill:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: var(--font-size-sm); color: var(--text-muted); }
.modal-overlay { position: fixed; inset: 0; background: var(--bg-overlay); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: var(--bg-card); border-radius: var(--radius-lg); width: 440px; max-width: 90vw; box-shadow: var(--shadow-lg); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: var(--space-5) var(--space-6); border-bottom: 1px solid var(--border-light); }
.modal-header h3 { font-size: var(--font-size-lg); font-weight: var(--font-weight-semibold); }
.btn-icon { background: none; border: none; cursor: pointer; font-size: var(--font-size-lg); color: var(--text-muted); }
.modal-body { padding: var(--space-6); }
.form-group { margin-bottom: var(--space-4); }
.form-group label { display: block; font-size: var(--font-size-sm); font-weight: var(--font-weight-medium); color: var(--text-body); margin-bottom: var(--space-1); }
.form-input { width: 100%; padding: 10px 14px; border: 1.5px solid var(--border-color); border-radius: var(--radius-md); font-size: var(--font-size-sm); font-family: var(--font-family); outline: none; box-sizing: border-box; }
.form-input:focus { border-color: var(--color-primary); }
.current-balance { font-size: var(--font-size-2xl); font-weight: var(--font-weight-bold); color: var(--color-primary); }
.modal-footer { display: flex; justify-content: flex-end; gap: var(--space-3); padding: var(--space-4) var(--space-6); border-top: 1px solid var(--border-light); }
</style>
