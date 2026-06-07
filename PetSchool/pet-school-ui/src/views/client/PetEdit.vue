<template>
  <div class="pet-edit-page">
    <div class="page-header">
      <button class="btn btn-ghost back-btn" @click="router.push('/client/pet')">
        <span class="back-arrow">&larr;</span> 返回
      </button>
      <h1 class="page-title">{{ isEdit ? '编辑宠物' : '新增宠物' }}</h1>
    </div>

    <div class="card form-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-grid">
          <!-- 宠物头像 -->
          <div class="form-group avatar-group">
            <label>宠物头像</label>
            <div class="avatar-row">
              <div class="avatar-preview" v-if="form.avatar">
                <img :src="form.avatar" alt="头像预览" />
              </div>
              <div class="avatar-preview avatar-placeholder" v-else>
                <span>暂无</span>
              </div>
              <input v-model="form.avatar" type="text" placeholder="请输入头像图片URL" />
            </div>
          </div>

          <!-- 宠物名称 -->
          <div class="form-group">
            <label>宠物名称 <span class="required">*</span></label>
            <input v-model="form.name" type="text" placeholder="请输入宠物名称" required />
          </div>

          <!-- 宠物类型 -->
          <div class="form-group">
            <label>宠物类型</label>
            <select v-model="form.petType">
              <option value="dog">狗</option>
              <option value="cat">猫</option>
              <option value="other">其他</option>
            </select>
          </div>

          <!-- 品种 -->
          <div class="form-group">
            <label>品种</label>
            <input v-model="form.breed" type="text" placeholder="如金毛、柯基、英短" />
          </div>

          <!-- 性别 -->
          <div class="form-group">
            <label>性别</label>
            <div class="segment-control">
              <button type="button" class="segment-btn" :class="{ active: form.gender === 1 }" @click="form.gender = 1">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="10.5" cy="10.5" r="6.5"/><line x1="15.5" y1="15.5" x2="21" y2="21"/></svg>
                <span>公</span>
              </button>
              <button type="button" class="segment-btn" :class="{ active: form.gender === 0 }" @click="form.gender = 0">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 3v6"/><path d="M9 6h6"/></svg>
                <span>母</span>
              </button>
            </div>
          </div>

          <!-- 生日 -->
          <div class="form-group">
            <label>生日</label>
            <input v-model="form.birthday" type="date" />
          </div>

          <!-- 体重 -->
          <div class="form-group">
            <label>体重 (kg)</label>
            <input v-model.number="form.weight" type="number" step="0.1" min="0" placeholder="请输入体重" />
          </div>

          <!-- 毛色 -->
          <div class="form-group">
            <label>毛色</label>
            <input v-model="form.color" type="text" placeholder="如白色、黑色、金色" />
          </div>

          <!-- 是否绝育 -->
          <div class="form-group">
            <label>是否绝育</label>
            <div class="segment-control">
              <button type="button" class="segment-btn segment-btn--success" :class="{ active: form.sterilized === 0 }" @click="form.sterilized = 0">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                <span>否</span>
              </button>
              <button type="button" class="segment-btn segment-btn--success" :class="{ active: form.sterilized === 1 }" @click="form.sterilized = 1">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
                <span>是</span>
              </button>
            </div>
          </div>

          <!-- 血型 -->
          <div class="form-group">
            <label>血型</label>
            <input v-model="form.bloodType" type="text" placeholder="如 DEA 1.1、A型" />
          </div>

          <!-- 芯片号 -->
          <div class="form-group">
            <label>芯片号</label>
            <input v-model="form.microchipNo" type="text" placeholder="请输入芯片编号" />
          </div>

          <!-- 过敏信息 -->
          <div class="form-group full-width">
            <label>过敏信息</label>
            <textarea v-model="form.allergyInfo" placeholder="请输入过敏信息，如无则留空" rows="3"></textarea>
          </div>

          <!-- 备注 -->
          <div class="form-group full-width">
            <label>备注</label>
            <textarea v-model="form.remark" placeholder="请输入备注信息" rows="3"></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary btn-lg submit-btn" :disabled="submitting">
            {{ submitting ? '提交中...' : (isEdit ? '保存修改' : '新增宠物') }}
          </button>
        </div>
      </form>
    </div>

    <!-- Toast -->
    <Transition name="toast">
      <div v-if="toast.show" :class="['toast', toast.type]">
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { petApi } from '../../api'

const router = useRouter()
const route = useRoute()

const isEdit = !!route.params.id
const submitting = ref(false)

const form = reactive({
  id: null,
  avatar: '',
  name: '',
  petType: 'dog',
  breed: '',
  gender: 1,
  birthday: '',
  weight: null,
  color: '',
  sterilized: 0,
  bloodType: '',
  microchipNo: '',
  allergyInfo: '',
  remark: ''
})

const toast = reactive({
  show: false,
  message: '',
  type: 'success'
})

function showToast(message, type = 'success') {
  toast.show = true
  toast.message = message
  toast.type = type
  setTimeout(() => {
    toast.show = false
  }, 2500)
}

async function loadPet() {
  try {
    const res = await petApi.detail(route.params.id)
    if (res.code === 200 && res.data) {
      const pet = res.data
      Object.keys(form).forEach(key => {
        if (pet[key] !== undefined && pet[key] !== null) {
          form[key] = pet[key]
        }
      })
    } else {
      showToast(res.message || '加载宠物信息失败', 'error')
    }
  } catch (e) {
    showToast('加载宠物信息失败', 'error')
  }
}

async function handleSubmit() {
  if (!form.name.trim()) {
    showToast('请输入宠物名称', 'error')
    return
  }

  submitting.value = true
  try {
    const res = isEdit
      ? await petApi.update({ ...form })
      : await petApi.create({ ...form })

    if (res.code === 200) {
      showToast(isEdit ? '修改成功' : '新增成功', 'success')
      setTimeout(() => {
        router.push('/client/pet')
      }, 800)
    } else {
      showToast(res.message || '操作失败', 'error')
    }
  } catch (e) {
    showToast('请求失败，请稍后重试', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit) {
    loadPet()
  }
})
</script>

<style scoped>
.pet-edit-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-4);
}

.page-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.back-btn {
  padding: 6px 12px;
  font-size: var(--font-size-sm);
  border-radius: var(--radius-md);
}

.back-arrow {
  margin-right: 2px;
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0;
  letter-spacing: -0.02em;
}

.form-card {
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4) var(--space-5);
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  display: block;
  margin-bottom: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}

.required {
  color: var(--color-danger);
  margin-left: 2px;
}

.avatar-group {
  grid-column: 1 / -1;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.avatar-preview {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid var(--border-color);
  background: var(--bg-input);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.avatar-row input {
  flex: 1;
  padding: 10px 14px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-family: var(--font-family);
  background: var(--bg-input);
  color: var(--text-title);
  outline: none;
  transition: all var(--transition-base);
}

.avatar-row input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
  background: var(--bg-card);
}

.segment-control {
  display: flex;
  background: var(--bg-input);
  border-radius: var(--radius-lg);
  padding: 3px;
  border: 1.5px solid var(--border-color);
  gap: 2px;
}

.segment-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 18px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-muted);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.segment-btn svg {
  opacity: 0.4;
  transition: opacity 0.2s ease;
}

.segment-btn:hover {
  color: var(--text-body);
  background: var(--bg-hover);
}

.segment-btn:hover svg {
  opacity: 0.7;
}

.segment-btn.active {
  background: var(--color-primary);
  color: white;
  font-weight: var(--font-weight-semibold);
  box-shadow: 0 2px 8px rgba(var(--color-primary-rgb, 79,108,254), 0.3);
}

.segment-btn.active svg {
  opacity: 1;
  stroke: white;
}

.segment-btn--success.active {
  background: var(--color-success, #22c55e);
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.segment-btn--success.active svg {
  stroke: white;
}

.form-actions {
  margin-top: var(--space-6);
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-light);
}

.submit-btn {
  width: 100%;
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  padding: 14px 32px;
}

.toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  z-index: 9999;
  box-shadow: var(--shadow-lg);
  pointer-events: none;
}

.toast.success {
  background: var(--color-success-bg);
  color: var(--color-success-text);
  border: 1px solid var(--color-success-light);
}

.toast.error {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
  border: 1px solid var(--color-danger-light);
}

.toast-enter-active {
  animation: toastIn 0.3s ease;
}

.toast-leave-active {
  animation: toastOut 0.25s ease;
}

@keyframes toastIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-12px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@keyframes toastOut {
  from {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  to {
    opacity: 0;
    transform: translateX(-50%) translateY(-12px);
  }
}

@media (max-width: 768px) {
  .pet-edit-page {
    padding: var(--space-3);
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-card {
    padding: var(--space-4);
  }

  .avatar-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .avatar-row input {
    width: 100%;
  }

  .page-title {
    font-size: var(--font-size-xl);
  }
}
</style>
