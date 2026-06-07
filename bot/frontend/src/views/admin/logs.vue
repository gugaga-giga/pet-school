<template>
  <div class="logs-page">
    <div class="page-header">
      <h2>问答日志</h2>
      <div class="filter-bar">
        <el-select v-model="filters.intent" placeholder="意图筛选" clearable style="width: 150px" @change="fetchList">
          <el-option label="产品咨询" value="product_inquiry" />
          <el-option label="价格查询" value="price_query" />
          <el-option label="技术支持" value="tech_support" />
          <el-option label="售后服务" value="after_sales" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
          @change="handleDateChange"
        />
        <el-input
          v-model="filters.keyword"
          placeholder="搜索关键词"
          prefix-icon="Search"
          clearable
          style="width: 200px"
          @input="fetchList"
        />
      </div>
    </div>

    <el-table :data="logs" stripe v-loading="loading">
      <el-table-column prop="user_message" label="用户问题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="assistant_message" label="AI回答" min-width="200" show-overflow-tooltip />
      <el-table-column prop="intent" label="意图" width="120">
        <template #default="{ row }">
          <IntentTag :intent="row.intent" />
        </template>
      </el-table-column>
      <el-table-column prop="sources_count" label="引用数" width="80" />
      <el-table-column prop="tokens" label="Token数" width="100" />
      <el-table-column prop="response_time" label="响应时间" width="110">
        <template #default="{ row }">{{ formatDuration(row.response_time) }}</template>
      </el-table-column>
      <el-table-column prop="created_at" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.created_at) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="问答详情" width="700px">
      <div v-if="currentLog" class="log-detail">
        <div class="detail-item">
          <label>用户问题：</label>
          <p>{{ currentLog.user_message }}</p>
        </div>
        <div class="detail-item">
          <label>AI回答：</label>
          <p>{{ currentLog.assistant_message }}</p>
        </div>
        <div class="detail-item">
          <label>意图：</label>
          <IntentTag :intent="currentLog.intent" />
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <label>引用数：</label>
            <span>{{ currentLog.sources_count }}</span>
          </div>
          <div class="detail-item">
            <label>Token数：</label>
            <span>{{ currentLog.tokens }}</span>
          </div>
          <div class="detail-item">
            <label>响应时间：</label>
            <span>{{ formatDuration(currentLog.response_time) }}</span>
          </div>
          <div class="detail-item">
            <label>时间：</label>
            <span>{{ formatDate(currentLog.created_at) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getQALogs } from '@/api/admin'
import { formatDate, formatDuration } from '@/utils/format'
import IntentTag from '@/components/IntentTag.vue'
import type { QALog } from '@/types/api'

const logs = ref<QALog[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref<string[]>([])
const detailVisible = ref(false)
const currentLog = ref<QALog | null>(null)

const filters = reactive({
  intent: '',
  keyword: '',
  start_date: '',
  end_date: ''
})

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getQALogs({
      page: page.value,
      page_size: pageSize.value,
      intent: filters.intent || undefined,
      keyword: filters.keyword || undefined,
      start_date: filters.start_date || undefined,
      end_date: filters.end_date || undefined
    })
    logs.value = res.data.items
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleDateChange(val: string[] | null) {
  if (val && val.length === 2) {
    filters.start_date = val[0]
    filters.end_date = val[1]
  } else {
    filters.start_date = ''
    filters.end_date = ''
  }
  fetchList()
}

function showDetail(log: QALog) {
  currentLog.value = log
  detailVisible.value = true
}
</script>

<style lang="scss" scoped>
.logs-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 20px;
      margin: 0;
    }

    .filter-bar {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .log-detail {
    .detail-item {
      margin-bottom: 16px;

      label {
        font-weight: 600;
        color: var(--el-text-color-primary);
        margin-bottom: 4px;
        display: block;
      }

      p {
        color: var(--el-text-color-regular);
        line-height: 1.6;
        white-space: pre-wrap;
      }
    }

    .detail-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .detail-item {
        label {
          display: inline;
          margin-right: 8px;
        }
      }
    }
  }
}
</style>
