<template>
  <div class="wallet-page">
    <div class="wallet-header">
      <h1 class="page-title">💰 我的钱包</h1>
    </div>

    <div class="balance-card">
      <div class="balance-card-bg"></div>
      <div class="balance-card-content">
        <div class="balance-label">账户余额</div>
        <div class="balance-amount">¥{{ formatMoney(wallet?.balance || 0) }}</div>
        <div class="balance-stats">
          <div class="stat-item">
            <span class="stat-label">累计充值</span>
            <span class="stat-value">¥{{ formatMoney(wallet?.totalRecharge || 0) }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">累计消费</span>
            <span class="stat-value">¥{{ formatMoney(wallet?.totalConsume || 0) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="recharge-section">
      <h3 class="section-title">快捷充值</h3>
      <div class="recharge-amounts">
        <button
          v-for="a in [100, 200, 500, 1000, 2000]"
          :key="a"
          :class="['recharge-chip', rechargeAmount === a ? 'recharge-chip--active' : '']"
          @click="rechargeAmount = a"
        >¥{{ a }}</button>
      </div>
      <div class="custom-amount">
        <input
          v-model="customAmount"
          type="number"
          placeholder="自定义金额"
          class="custom-input"
          min="1"
          @input="rechargeAmount = null"
        />
        <button class="btn btn-accent recharge-btn" @click="handleRecharge" :disabled="recharging">
          {{ recharging ? '充值中...' : '立即充值' }}
        </button>
      </div>
    </div>

    <div class="records-section">
      <div class="records-header">
        <h3 class="section-title">交易记录</h3>
        <div class="tab-bar">
          <button :class="['tab-pill', recordType === null ? 'tab-pill--active' : '']" @click="recordType = null">全部</button>
          <button :class="['tab-pill', recordType === 1 ? 'tab-pill--active' : '']" @click="recordType = 1">充值</button>
          <button :class="['tab-pill', recordType === 2 ? 'tab-pill--active' : '']" @click="recordType = 2">消费</button>
          <button :class="['tab-pill', recordType === 3 ? 'tab-pill--active' : '']" @click="recordType = 3">退款</button>
        </div>
      </div>

      <div v-if="records.length === 0" class="empty-state">暂无交易记录</div>

      <div v-else class="record-list">
        <div v-for="r in records" :key="r.id" class="record-item">
          <div :class="['record-icon', typeClass(r.type)]">{{ typeIcon(r.type) }}</div>
          <div class="record-info">
            <div class="record-title">{{ typeLabel(r.type) }}<span v-if="r.businessType" class="record-biz"> · {{ bizLabel(r.businessType) }}</span></div>
            <div class="record-time">{{ r.createTime }}</div>
          </div>
          <div :class="['record-amount', r.type === 2 ? 'amount-negative' : 'amount-positive']">
            {{ r.type === 2 ? '-' : '+' }}¥{{ formatMoney(r.amount) }}
          </div>
        </div>
      </div>

      <div v-if="recordTotal > recordPage * 10" class="load-more">
        <button class="btn btn-ghost btn-sm" @click="recordPage++; loadRecords()">加载更多</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { walletApi } from '../../api/index.js'

const wallet = ref(null)
const rechargeAmount = ref(500)
const customAmount = ref('')
const recharging = ref(false)
const records = ref([])
const recordType = ref(null)
const recordPage = ref(1)
const recordTotal = ref(0)

const formatMoney = (v) => Number(v).toFixed(2)

const typeIcon = (t) => { return { 1: '↓', 2: '↑', 3: '↩', 4: '⚙' }[t] || '·' }
const typeClass = (t) => { return { 1: 'icon-recharge', 2: 'icon-consume', 3: 'icon-refund', 4: 'icon-adjust' }[t] || '' }
const typeLabel = (t) => { return { 1: '充值', 2: '消费', 3: '退款', 4: '后台调整' }[t] || '未知' }
const bizLabel = (b) => { return { course: '课程', boarding: '寄宿', medical: '医疗', system: '系统' }[b] || b }

const loadWallet = async () => {
  try {
    const res = await walletApi.info()
    if (res.code === 200) wallet.value = res.data
  } catch (e) { console.error(e) }
}

const loadRecords = async () => {
  try {
    const res = await walletApi.records({ type: recordType.value, page: recordPage.value, pageSize: 10 })
    if (res.code === 200) {
      const d = res.data
      if (recordPage.value === 1) {
        records.value = d.list || []
      } else {
        records.value = [...records.value, ...(d.list || [])]
      }
      recordTotal.value = d.total || 0
    }
  } catch (e) { console.error(e) }
}

const handleRecharge = async () => {
  const amount = rechargeAmount.value || Number(customAmount.value)
  if (!amount || amount <= 0) { alert('请选择或输入充值金额'); return }
  recharging.value = true
  try {
    const res = await walletApi.recharge({ amount, remark: '用户充值' })
    if (res.code === 200) {
      alert('充值成功！')
      await loadWallet()
      recordPage.value = 1
      await loadRecords()
    } else {
      alert(res.message || '充值失败')
    }
  } catch (e) { alert('充值失败') }
  finally { recharging.value = false }
}

watch(recordType, () => { recordPage.value = 1; loadRecords() })

onMounted(() => { loadWallet(); loadRecords() })
</script>

<style scoped>
.wallet-page {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-6);
}

.wallet-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
}

.balance-card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: var(--space-8);
  box-shadow: 0 8px 32px rgba(79, 124, 255, 0.25);
}

.balance-card-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #4F7CFF 0%, #7B61FF 50%, #FFB86B 100%);
}

.balance-card-content {
  position: relative;
  padding: var(--space-8) var(--space-6);
  color: #fff;
}

.balance-label {
  font-size: var(--font-size-sm);
  opacity: 0.85;
  margin-bottom: var(--space-2);
}

.balance-amount {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -1px;
  margin-bottom: var(--space-6);
}

.balance-stats {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.7;
}

.stat-value {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.3);
}

.recharge-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-8);
  border: 1px solid var(--border-light);
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin-bottom: var(--space-4);
}

.recharge-amounts {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.recharge-chip {
  padding: 10px 24px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--border-color);
  background: var(--bg-page);
  color: var(--text-body);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family);
  cursor: pointer;
  transition: all var(--transition-base);
}

.recharge-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.recharge-chip--active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
  box-shadow: var(--shadow-button);
}

.custom-amount {
  display: flex;
  gap: var(--space-3);
}

.custom-input {
  flex: 1;
  padding: 10px 16px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--border-color);
  font-size: var(--font-size-sm);
  font-family: var(--font-family);
  outline: none;
  transition: border-color var(--transition-base);
}

.custom-input:focus {
  border-color: var(--color-primary);
}

.recharge-btn {
  white-space: nowrap;
}

.records-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  border: 1px solid var(--border-light);
}

.records-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
  gap: var(--space-3);
}

.tab-bar {
  display: inline-flex;
  gap: var(--space-1);
  padding: 3px;
  background: var(--bg-page);
  border-radius: var(--radius-full);
}

.tab-pill {
  padding: 6px 14px;
  border-radius: var(--radius-full);
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: var(--font-weight-medium);
  font-family: var(--font-family);
  cursor: pointer;
  transition: all var(--transition-base);
}

.tab-pill:hover {
  color: var(--color-primary);
}

.tab-pill--active {
  background: var(--color-primary);
  color: #fff;
  box-shadow: var(--shadow-sm);
}

.empty-state {
  text-align: center;
  padding: var(--space-8);
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.record-list {
  display: flex;
  flex-direction: column;
}

.record-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-light);
}

.record-item:last-child {
  border-bottom: none;
}

.record-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  flex-shrink: 0;
}

.icon-recharge { background: #E8F5E9; color: #2E7D32; }
.icon-consume { background: #FFEBEE; color: #C62828; }
.icon-refund { background: #E3F2FD; color: #1565C0; }
.icon-adjust { background: #FFF3E0; color: #E65100; }

.record-info {
  flex: 1;
  min-width: 0;
}

.record-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
}

.record-biz {
  color: var(--text-muted);
  font-weight: var(--font-weight-normal);
}

.record-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.record-amount {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  white-space: nowrap;
}

.amount-positive { color: #2E7D32; }
.amount-negative { color: #C62828; }

.load-more {
  text-align: center;
  margin-top: var(--space-4);
}
</style>
