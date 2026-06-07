<template>
  <div class="users-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-input
        v-model="keyword"
        placeholder="搜索用户"
        prefix-icon="Search"
        clearable
        style="width: 240px"
        @input="fetchList"
      />
    </div>

    <el-table :data="users" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="roles" label="角色" width="150">
        <template #default="{ row }">
          <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 4px">
            {{ role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val: boolean) => handleStatusChange(row, val)"
            active-text="启用"
            inactive-text="禁用"
          />
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="注册时间" width="180">
        <template #default="{ row }">{{ formatDate(row.created_at) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="showRoleDialog(row)">角色</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 角色分配对话框 -->
    <el-dialog v-model="roleDialogVisible" title="角色分配" width="400px">
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox value="admin">管理员</el-checkbox>
        <el-checkbox value="user">普通用户</el-checkbox>
        <el-checkbox value="editor">编辑者</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, updateUserRoles, deleteUser } from '@/api/user'
import { formatDate } from '@/utils/format'
import type { UserInfo } from '@/types/api'

const users = ref<UserInfo[]>([])
const keyword = ref('')
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const roleDialogVisible = ref(false)
const roleSubmitting = ref(false)
const selectedRoles = ref<string[]>([])
const editingUser = ref<UserInfo | null>(null)

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      page_size: pageSize.value,
      keyword: keyword.value
    })
    users.value = res.data.items
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(user: UserInfo, val: boolean) {
  try {
    await updateUserStatus(user.id, val ? 1 : 0)
    ElMessage.success('状态更新成功')
    await fetchList()
  } catch {
    // ignore
  }
}

function showRoleDialog(user: UserInfo) {
  editingUser.value = user
  selectedRoles.value = [...user.roles]
  roleDialogVisible.value = true
}

async function handleRoleSubmit() {
  if (!editingUser.value) return
  roleSubmitting.value = true
  try {
    await updateUserRoles(editingUser.value.id, selectedRoles.value)
    ElMessage.success('角色更新成功')
    roleDialogVisible.value = false
    await fetchList()
  } catch (err: any) {
    ElMessage.error(err.message || '更新失败')
  } finally {
    roleSubmitting.value = false
  }
}

async function handleDelete(user: UserInfo) {
  try {
    await ElMessageBox.confirm(`确定删除用户"${user.username}"？`, '提示', { type: 'warning' })
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    // cancelled
  }
}
</script>

<style lang="scss" scoped>
.users-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 20px;
      margin: 0;
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
