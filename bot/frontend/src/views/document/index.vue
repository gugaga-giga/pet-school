<template>
  <div class="document-page">
    <div class="page-header">
      <h2>文档管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedKBId" placeholder="选择知识库" style="width: 200px" @change="fetchList">
          <el-option v-for="kb in knowledgeBases" :key="kb.id" :label="kb.name" :value="kb.id" />
        </el-select>
        <el-button type="primary" :disabled="!selectedKBId" @click="uploadDialogVisible = true">
          <el-icon><Upload /></el-icon>
          上传文档
        </el-button>
      </div>
    </div>

    <el-table :data="documents" stripe v-loading="loading">
      <el-table-column prop="filename" label="文件名" min-width="200" show-overflow-tooltip />
      <el-table-column prop="file_type" label="类型" width="100" />
      <el-table-column prop="file_size" label="大小" width="100">
        <template #default="{ row }">{{ formatFileSize(row.file_size) }}</template>
      </el-table-column>
      <el-table-column prop="chunk_count" label="分块数" width="100" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="error_message" label="错误信息" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.error_message" class="error-text">{{ row.error_message }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="上传时间" width="180">
        <template #default="{ row }">{{ formatDate(row.created_at) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="showChunks(row)">查看分块</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文档" width="600px">
      <FileUpload
        :knowledge-base-id="selectedKBId!"
        @success="handleUploadSuccess"
      />
    </el-dialog>

    <!-- 分块详情对话框 -->
    <el-dialog v-model="chunksDialogVisible" title="文档分块" width="700px">
      <el-table :data="chunks" stripe max-height="400">
        <el-table-column prop="chunk_index" label="序号" width="80" />
        <el-table-column prop="content" label="内容" min-width="400" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocuments, deleteDocument, getDocumentChunks } from '@/api/document'
import { getKnowledgeBases } from '@/api/knowledge'
import { formatDate, formatFileSize } from '@/utils/format'
import FileUpload from '@/components/FileUpload.vue'
import type { Document, DocumentChunk, KnowledgeBase } from '@/types/api'

const route = useRoute()
const documents = ref<Document[]>([])
const knowledgeBases = ref<KnowledgeBase[]>([])
const selectedKBId = ref<number | undefined>(undefined)
const loading = ref(false)
const uploadDialogVisible = ref(false)
const chunksDialogVisible = ref(false)
const chunks = ref<DocumentChunk[]>([])

onMounted(async () => {
  await fetchKnowledgeBases()
  const kbId = route.query.kb_id
  if (kbId) {
    selectedKBId.value = Number(kbId)
    await fetchList()
  }
})

async function fetchKnowledgeBases() {
  try {
    const res = await getKnowledgeBases({ page: 1, page_size: 100 })
    knowledgeBases.value = res.data.items
  } catch {
    // ignore
  }
}

async function fetchList() {
  if (!selectedKBId.value) return
  loading.value = true
  try {
    const res = await getDocuments({ knowledge_base_id: selectedKBId.value, page: 1, page_size: 100 })
    documents.value = res.data.items
  } finally {
    loading.value = false
  }
}

function statusType(status: string) {
  const map: Record<string, string> = {
    pending: 'info',
    processing: 'warning',
    completed: 'success',
    failed: 'danger'
  }
  return map[status] || 'info'
}

function statusText(status: string) {
  const map: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    failed: '失败'
  }
  return map[status] || status
}

async function showChunks(doc: Document) {
  try {
    const res = await getDocumentChunks(doc.id)
    chunks.value = res.data
    chunksDialogVisible.value = true
  } catch {
    ElMessage.error('获取分块信息失败')
  }
}

async function handleDelete(doc: Document) {
  try {
    await ElMessageBox.confirm(`确定删除文档"${doc.filename}"？`, '提示', { type: 'warning' })
    await deleteDocument(doc.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    // cancelled
  }
}

function handleUploadSuccess() {
  uploadDialogVisible.value = false
  fetchList()
}
</script>

<style lang="scss" scoped>
.document-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 20px;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .error-text {
    color: var(--el-color-danger);
    font-size: 12px;
  }
}
</style>
