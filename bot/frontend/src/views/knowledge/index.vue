<template>
  <div class="knowledge-page">
    <div class="page-header">
      <h2>知识库管理</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索知识库"
          prefix-icon="Search"
          clearable
          style="width: 240px"
          @input="fetchList"
        />
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="card">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button value="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          创建知识库
        </el-button>
      </div>
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="knowledge-cards">
      <el-card
        v-for="kb in knowledgeBases"
        :key="kb.id"
        shadow="hover"
        class="kb-card"
      >
        <div class="kb-card-content">
          <div class="kb-icon">
            <el-icon :size="32" color="#409eff"><Collection /></el-icon>
          </div>
          <h3 class="kb-name">{{ kb.name }}</h3>
          <p class="kb-desc">{{ kb.description || '暂无描述' }}</p>
          <div class="kb-stats">
            <span><el-icon><Document /></el-icon> {{ kb.doc_count }} 文档</span>
            <span><el-icon><CopyDocument /></el-icon> {{ kb.chunk_count }} 分块</span>
          </div>
          <div class="kb-meta">
            <span>{{ formatDate(kb.created_at) }}</span>
          </div>
        </div>
        <div class="kb-card-actions">
          <el-button text type="primary" @click="showEditDialog(kb)">编辑</el-button>
          <el-button text type="primary" @click="$router.push({ path: '/documents', query: { kb_id: kb.id } })">文档</el-button>
          <el-button text type="danger" @click="handleDelete(kb)">删除</el-button>
        </div>
      </el-card>
      <el-empty v-if="knowledgeBases.length === 0" description="暂无知识库" />
    </div>

    <!-- 列表视图 -->
    <el-table v-else :data="knowledgeBases" stripe>
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="doc_count" label="文档数" width="100" />
      <el-table-column prop="chunk_count" label="分块数" width="100" />
      <el-table-column prop="embedding_model" label="嵌入模型" width="150" />
      <el-table-column prop="created_at" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.created_at) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="showEditDialog(row)">编辑</el-button>
          <el-button text type="primary" @click="$router.push({ path: '/documents', query: { kb_id: row.id } })">文档</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑知识库' : '创建知识库'"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入知识库描述" />
        </el-form-item>
        <el-form-item v-if="!isEditing" label="嵌入模型" prop="embedding_model">
          <el-select v-model="form.embedding_model" placeholder="选择嵌入模型" style="width: 100%">
            <el-option label="text-embedding-ada-002" value="text-embedding-ada-002" />
            <el-option label="text-embedding-3-small" value="text-embedding-3-small" />
            <el-option label="text-embedding-3-large" value="text-embedding-3-large" />
            <el-option label="bge-large-zh" value="bge-large-zh" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getKnowledgeBases, createKnowledgeBase, updateKnowledgeBase, deleteKnowledgeBase } from '@/api/knowledge'
import { formatDate } from '@/utils/format'
import type { KnowledgeBase, KnowledgeBaseCreate } from '@/types/api'

const knowledgeBases = ref<KnowledgeBase[]>([])
const keyword = ref('')
const viewMode = ref<'card' | 'list'>('card')
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<KnowledgeBaseCreate & { embedding_model?: string }>({
  name: '',
  description: '',
  embedding_model: 'text-embedding-ada-002'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }]
}

onMounted(() => {
  fetchList()
})

async function fetchList() {
  try {
    const res = await getKnowledgeBases({ keyword: keyword.value, page: 1, page_size: 100 })
    knowledgeBases.value = res.data.items
  } catch {
    // ignore
  }
}

function showCreateDialog() {
  isEditing.value = false
  editingId.value = null
  form.name = ''
  form.description = ''
  form.embedding_model = 'text-embedding-ada-002'
  dialogVisible.value = true
}

function showEditDialog(kb: KnowledgeBase) {
  isEditing.value = true
  editingId.value = kb.id
  form.name = kb.name
  form.description = kb.description
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateKnowledgeBase(editingId.value, {
        name: form.name,
        description: form.description
      })
      ElMessage.success('更新成功')
    } else {
      await createKnowledgeBase(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchList()
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(kb: KnowledgeBase) {
  try {
    await ElMessageBox.confirm(`确定删除知识库"${kb.name}"？删除后不可恢复`, '提示', {
      type: 'warning'
    })
    await deleteKnowledgeBase(kb.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    // cancelled
  }
}
</script>

<style lang="scss" scoped>
.knowledge-page {
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
      align-items: center;
    }
  }

  .knowledge-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;

    .kb-card {
      .kb-card-content {
        text-align: center;
        padding: 8px 0;

        .kb-icon {
          margin-bottom: 12px;
        }

        .kb-name {
          font-size: 16px;
          margin-bottom: 8px;
        }

        .kb-desc {
          font-size: 13px;
          color: var(--el-text-color-secondary);
          margin-bottom: 12px;
          min-height: 40px;
        }

        .kb-stats {
          display: flex;
          justify-content: center;
          gap: 20px;
          font-size: 13px;
          color: var(--el-text-color-regular);
          margin-bottom: 8px;

          span {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }

        .kb-meta {
          font-size: 12px;
          color: var(--el-text-color-placeholder);
        }
      }

      .kb-card-actions {
        display: flex;
        justify-content: center;
        border-top: 1px solid var(--el-border-color-lighter);
        padding-top: 12px;
      }
    }
  }
}
</style>
