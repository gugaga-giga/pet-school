<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>💳 钱包流水</h2>
    </div>

    <div class="toolbar">
      <input v-model="keyword" class="search-input" placeholder="搜索用户名/手机号" @keyup.enter="loadData" />
      <select v-model="filterType" class="filter-select" @change="loadData">
        <option :value="null">全部类型</option>
        <option :value="1">充值</option>
        <option :value="2">消费</option>
        <option :value="3">退款</option>
        <option :value="4">后台调整</option>
      </select>
      <select v-model="filterBiz" class="filter-select" @change="loadData">
        <option value="">全部业务</option>
        <option value="course">课程</option>
        <option value="boarding">寄宿</option>
        <option value="medical">医疗</option>
        <option value="system">系统</option>
      </select>
      <button class="btn btn-primary btn-sm" @click="loadData">搜索</button>
      <button class="btn btn-secondary btn-sm" @click="keyword=''; filterType=null; filterBiz=''; loadData()">重置</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>用户</th>
          <th>类型</th>
          <th>金额</th>
          <th>余额变化</th>
          <th>业务来源</th>
          <th>流水号</th>
          <th>备注</th>
          <th>时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in records" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ r.username || ('用户#' + r.userId) }}</td>
          <td><span :class="['type-tag', typeClass(r.type)]">{{ typeLabel(r.type) }}</span></td>
          <td :class="r.type === 2 ? 'money-negative' : 'money-positive'">{{ r.type === 2 ? '-' : '+' }}¥{{ (r.amount || 0).toFixed(2) }}</td>
          <td class="money">¥{{ (r.balanceBefore || 0).toFixed(2) }} → ¥{{ (r.balanceAfter || 0).toFixed(2) }}</td>
          <td>{{ bizLabel(r.businessType) }}</td>
          <td class="txn-no">{{ r.transactionNo }}</td>
          <td class="remark-cell">{{ r.remark }}</td>
          <td>{{ r.createTime }}</td>
        </tr>
        <tr v-if="records.length === 0"><td colspan="9" class="empty">暂无数据</td></tr>
      </tbody>
    </table>

    <div class="pagination" v-if="totalPages > 1">
      <button class="page-pill" :disabled="page <= 1" @click="page--; loadData()">«</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button class="page-pill" :disabled="page >= totalPages" @click="page++; loadData()">»</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { adminApi } from '../../api/index.js'

const records = ref([])
const keyword = ref('')
const filterType = ref(null)
const filterBiz = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize))

const typeLabel = (t) => { return { 1: '充值', 2: '消费', 3: '退款', 4: '后台调整' }[t] || '未知' }
const typeClass = (t) => { return { 1: 'tag-recharge', 2: 'tag-consume', 3: 'tag-refund', 4: 'tag-adjust' }[t] || '' }
const bizLabel = (b) => { return { course: '课程', boarding: '寄宿', medical: '医疗', system: '系统' }[b] || b || '-' }

const loadData = async () => {
  try {
    const res = await adminApi.walletRecords({ keyword: keyword.value, type: filterType.value, businessType: filterBiz.value || null, page: page.value, pageSize })
    if (res.code === 200) {
      records.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
}

onMounted(() => loadData())
</script>

<style scoped>
.admin-page { padding: var(--space-6); }
.page-header { margin-bottom: var(--space-6); }
.page-header h2 { font-size: var(--font-size-xl); font-weight: var(--font-weight-bold); color: var(--text-title); }
.toolbar { display: flex; gap: var(--space-2); margin-bottom: var(--space-4); flex-wrap: wrap; }
.search-input { padding: 8px 14px; border: 1.5px solid var(--border-color); border-radius: var(--radius-md); font-size: var(--font-size-sm); font-family: var(--font-family); outline: none; min-width: 180px; }
.search-input:focus { border-color: var(--color-primary); }
.filter-select { padding: 8px 12px; border: 1.5px solid var(--border-color); border-radius: var(--radius-md); font-size: var(--font-size-sm); font-family: var(--font-family); outline: none; background: var(--bg-card); }
.filter-select:focus { border-color: var(--color-primary); }
.data-table { width: 100%; border-collapse: collapse; background: var(--bg-card); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.data-table th { padding: var(--space-3) var(--space-4); text-align: left; font-size: var(--font-size-xs); font-weight: var(--font-weight-semibold); color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid var(--border-light); background: var(--bg-page); white-space: nowrap; }
.data-table td { padding: var(--space-3) var(--space-4); font-size: var(--font-size-sm); color: var(--text-body); border-bottom: 1px solid var(--border-light); }
.data-table tr:hover td { background: var(--bg-hover); }
.money { font-variant-numeric: tabular-nums; }
.money-positive { color: var(--color-success-text); font-weight: var(--font-weight-semibold); }
.money-negative { color: var(--color-danger-text); font-weight: var(--font-weight-semibold); }
.empty { text-align: center; color: var(--text-muted); padding: var(--space-8) !important; }
.type-tag { display: inline-block; padding: 2px 10px; border-radius: var(--radius-full); font-size: var(--font-size-xs); font-weight: var(--font-weight-semibold); }
.tag-recharge { background: var(--color-success-bg); color: var(--color-success-text); }
.tag-consume { background: var(--color-danger-bg); color: var(--color-danger-text); }
.tag-refund { background: var(--color-info-bg); color: var(--color-info-text); }
.tag-adjust { background: var(--color-warning-bg); color: var(--color-warning-dark); }
.txn-no { font-family: var(--font-mono); font-size: var(--font-size-xs); color: var(--text-muted); }
.remark-cell { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pagination { display: flex; align-items: center; justify-content: center; gap: var(--space-3); margin-top: var(--space-4); }
.page-pill { padding: 6px 14px; border: 1px solid var(--border-color); border-radius: var(--radius-md); background: var(--bg-card); cursor: pointer; font-size: var(--font-size-sm); }
.page-pill:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: var(--font-size-sm); color: var(--text-muted); }
</style>
