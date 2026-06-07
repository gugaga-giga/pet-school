<template>
  <div class="pet-page">
    <div class="page-header">
      <h2>宠物管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        添加宠物
      </el-button>
    </div>

    <div class="pet-cards">
      <el-card v-for="pet in pets" :key="pet.id" shadow="hover" class="pet-card">
        <div class="pet-card-content">
          <div class="pet-avatar">
            <el-icon :size="40" :color="petTypeColor(pet.pet_type)">
              <component :is="petTypeIcon(pet.pet_type)" />
            </el-icon>
          </div>
          <h3 class="pet-name">{{ pet.nickname }}</h3>
          <el-tag size="small" :type="petTypeTagType(pet.pet_type)">{{ petTypeLabel(pet.pet_type) }}</el-tag>
          <div class="pet-info">
            <div class="info-row">
              <span class="label">品种：</span>
              <span>{{ pet.breed || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">性别：</span>
              <span>{{ pet.gender === 'male' ? '♂ 公' : '♀ 母' }}</span>
            </div>
            <div class="info-row">
              <span class="label">体重：</span>
              <span>{{ pet.weight ? pet.weight + ' kg' : '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">出生：</span>
              <span>{{ pet.birth_date || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">绝育：</span>
              <span>{{ pet.is_neutered ? '是' : '否' }}</span>
            </div>
            <div class="info-row">
              <span class="label">健康：</span>
              <span>{{ pet.health_status || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">疫苗：</span>
              <span>{{ pet.vaccine_status || '-' }}</span>
            </div>
          </div>
        </div>
        <div class="pet-card-actions">
          <el-button text type="primary" @click="showEditDialog(pet)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(pet)">删除</el-button>
        </div>
      </el-card>
      <el-empty v-if="pets.length === 0" description="暂无宠物" />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑宠物' : '添加宠物'"
      width="550px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入宠物昵称" />
        </el-form-item>
        <el-form-item label="类型" prop="pet_type">
          <el-select v-model="form.pet_type" placeholder="选择宠物类型" style="width: 100%">
            <el-option label="🐶 狗" value="dog" />
            <el-option label="🐱 猫" value="cat" />
            <el-option label="🐦 鸟" value="bird" />
            <el-option label="🐟 鱼" value="fish" />
            <el-option label="🐹 仓鼠" value="hamster" />
            <el-option label="🐰 兔子" value="rabbit" />
            <el-option label="🐾 其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="form.breed" placeholder="请输入品种" />
        </el-form-item>
        <el-form-item label="出生日期" prop="birth_date">
          <el-date-picker v-model="form.birth_date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">公</el-radio>
            <el-radio value="female">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否绝育">
          <el-switch v-model="form.is_neutered" />
        </el-form-item>
        <el-form-item label="健康状况">
          <el-input v-model="form.health_status" placeholder="请输入健康状况" />
        </el-form-item>
        <el-form-item label="疫苗状态">
          <el-input v-model="form.vaccine_status" placeholder="请输入疫苗状态" />
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
import { getPets, createPet, updatePet, deletePet } from '@/api/pet'
import type { Pet, PetCreateRequest } from '@/types/api'

const pets = ref<Pet[]>([])
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<PetCreateRequest>({
  nickname: '',
  pet_type: 'dog',
  breed: '',
  birth_date: '',
  weight: 0,
  gender: 'male',
  is_neutered: false,
  health_status: '',
  vaccine_status: ''
})

const rules: FormRules = {
  nickname: [{ required: true, message: '请输入宠物昵称', trigger: 'blur' }],
  pet_type: [{ required: true, message: '请选择宠物类型', trigger: 'change' }],
  breed: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

onMounted(() => {
  fetchList()
})

async function fetchList() {
  try {
    const res = await getPets({ page: 1, page_size: 100 })
    pets.value = res.data.items
  } catch {
    // ignore
  }
}

function petTypeIcon(type: string) {
  const map: Record<string, string> = {
    dog: 'Cpu',
    cat: 'ChatDotRound',
    bird: 'Promotion',
    fish: 'Odometer',
    hamster: 'Mouse',
    rabbit: 'Cherry',
    other: 'Guide'
  }
  return map[type] || 'Guide'
}

function petTypeColor(type: string) {
  const map: Record<string, string> = {
    dog: '#e6a23c',
    cat: '#409eff',
    bird: '#67c23a',
    fish: '#909399',
    hamster: '#f56c6c',
    rabbit: '#9b59b6',
    other: '#c0c4cc'
  }
  return map[type] || '#c0c4cc'
}

function petTypeLabel(type: string) {
  const map: Record<string, string> = {
    dog: '狗',
    cat: '猫',
    bird: '鸟',
    fish: '鱼',
    hamster: '仓鼠',
    rabbit: '兔子',
    other: '其他'
  }
  return map[type] || type
}

function petTypeTagType(type: string) {
  const map: Record<string, string> = {
    dog: 'warning',
    cat: '',
    bird: 'success',
    fish: 'info',
    hamster: 'danger',
    rabbit: '',
    other: 'info'
  }
  return map[type] || 'info'
}

function showCreateDialog() {
  isEditing.value = false
  editingId.value = null
  Object.assign(form, {
    nickname: '',
    pet_type: 'dog',
    breed: '',
    birth_date: '',
    weight: 0,
    gender: 'male',
    is_neutered: false,
    health_status: '',
    vaccine_status: ''
  })
  dialogVisible.value = true
}

function showEditDialog(pet: Pet) {
  isEditing.value = true
  editingId.value = pet.id
  Object.assign(form, {
    nickname: pet.nickname,
    pet_type: pet.pet_type,
    breed: pet.breed,
    birth_date: pet.birth_date,
    weight: pet.weight,
    gender: pet.gender,
    is_neutered: pet.is_neutered,
    health_status: pet.health_status,
    vaccine_status: pet.vaccine_status
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updatePet(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createPet(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await fetchList()
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(pet: Pet) {
  try {
    await ElMessageBox.confirm(`确定删除宠物"${pet.nickname}"？`, '提示', { type: 'warning' })
    await deletePet(pet.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    // cancelled
  }
}
</script>

<style lang="scss" scoped>
.pet-page {
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

  .pet-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;

    .pet-card {
      .pet-card-content {
        text-align: center;
        padding: 8px 0;

        .pet-avatar {
          margin-bottom: 12px;
        }

        .pet-name {
          font-size: 18px;
          margin: 8px 0;
        }

        .pet-info {
          text-align: left;
          margin-top: 16px;
          font-size: 13px;

          .info-row {
            display: flex;
            padding: 4px 0;
            border-bottom: 1px solid var(--el-border-color-extra-light);

            .label {
              color: var(--el-text-color-secondary);
              width: 70px;
              flex-shrink: 0;
            }
          }
        }
      }

      .pet-card-actions {
        display: flex;
        justify-content: center;
        border-top: 1px solid var(--el-border-color-lighter);
        padding-top: 12px;
      }
    }
  }
}
</style>
