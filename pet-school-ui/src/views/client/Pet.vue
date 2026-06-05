<template>
  <div class="pet-page">
    <!-- 顶部渐变卡片 -->
    <div class="header-card gradient-primary">
      <div class="header-content">
        <h1 class="header-title">我有 {{ pets.length }} 只宠物</h1>
        <p class="header-sub">管理你的爱宠信息，记录每一个成长瞬间</p>
      </div>
      <button class="btn btn-accent btn-add" @click="router.push('/client/pet/create')">
        <span class="add-icon">+</span> 添加宠物
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="pet-grid">
      <div class="pet-card card skeleton-card" v-for="i in 3" :key="i">
        <div class="skeleton" style="width:64px;height:64px;border-radius:50%;margin:0 auto 16px"></div>
        <div class="skeleton" style="width:60%;height:18px;margin:0 auto 8px"></div>
        <div class="skeleton" style="width:40%;height:14px;margin:0 auto"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!pets.length" class="empty-state">
      <div class="empty-illustration">
        <div class="empty-paw">🐾</div>
        <div class="empty-face">
          <div class="empty-ear left"></div>
          <div class="empty-ear right"></div>
          <div class="empty-head">
            <div class="empty-eye left"></div>
            <div class="empty-eye right"></div>
            <div class="empty-nose"></div>
          </div>
        </div>
      </div>
      <p class="empty-text">还没有宠物，快添加一只吧</p>
      <button class="btn btn-primary btn-lg" @click="router.push('/client/pet/create')">添加我的第一只宠物</button>
    </div>

    <!-- 宠物卡片网格 -->
    <div v-else class="pet-grid">
      <div class="pet-card card" v-for="pet in pets" :key="pet.id">
        <!-- 头像区域 -->
        <div class="pet-avatar-wrap">
          <div class="pet-avatar" :style="avatarStyle(pet)">
            <img v-if="pet.avatar" :src="pet.avatar" :alt="pet.name" class="pet-avatar-img" />
            <span v-else class="pet-avatar-fallback">{{ pet.name?.charAt(0) || '?' }}</span>
          </div>
          <span class="pet-status-badge" :class="statusClass(pet.status)">{{ statusText(pet.status) }}</span>
        </div>

        <!-- 信息区域 -->
        <div class="pet-info">
          <div class="pet-name-row">
            <h3 class="pet-name">{{ pet.name }}</h3>
            <span class="pet-type-icon">{{ petTypeIcon(pet.petType) }}</span>
          </div>
          <p class="pet-breed">{{ pet.breed || '未知品种' }}</p>

          <div class="pet-meta">
            <div class="meta-item">
              <span class="meta-label">年龄</span>
              <span class="meta-value">{{ pet.age ?? computeAge(pet.birthday) }}岁</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">体重</span>
              <span class="meta-value">{{ pet.weight ? pet.weight + 'kg' : '--' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">性别</span>
              <span class="meta-value gender" :class="pet.gender === 0 ? 'gender-female' : 'gender-male'">
                {{ pet.gender === 0 ? '♀' : '♂' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="pet-actions">
          <button class="btn btn-sm btn-primary" @click="router.push(`/client/pet/detail/${pet.id}`)">查看详情</button>
          <button class="btn btn-sm btn-secondary" @click="router.push(`/client/pet/edit/${pet.id}`)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="handleDelete(pet)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { petApi } from '../../api'

const router = useRouter()
const route = useRoute()
const pets = ref([])
const loading = ref(true)

function computeAge(birthday) {
  if (!birthday) return '--'
  const birth = new Date(birthday)
  const now = new Date()
  let age = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) {
    age--
  }
  return age < 0 ? 0 : age
}

function petTypeIcon(petType) {
  if (petType === 'dog') return '🐶'
  if (petType === 'cat') return '🐱'
  return '🐹'
}

function statusText(status) {
  if (status === 1) return '已离世'
  if (status === 2) return '已转让'
  return '正常'
}

function statusClass(status) {
  if (status === 1) return 'status-passed'
  if (status === 2) return 'status-transferred'
  return 'status-normal'
}

function avatarStyle(pet) {
  const colors = ['#4F8EF7', '#FFB86B', '#3CCB7F', '#FF6B6B', '#A78BFA', '#F472B6']
  const idx = pet.id ? pet.id % colors.length : 0
  return { background: pet.avatar ? 'transparent' : colors[idx] }
}

async function loadPets() {
  loading.value = true
  try {
    const res = await petApi.my()
    if (res.code === 200) {
      pets.value = res.data || []
    }
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleDelete(pet) {
  if (!confirm(`确定要删除宠物「${pet.name}」吗？此操作不可恢复。`)) return
  try {
    const res = await petApi.delete(pet.id)
    if (res.code === 200) {
      pets.value = pets.value.filter(p => p.id !== pet.id)
    } else {
      alert(res.message || '删除失败，该宠物可能有关联的业务记录')
    }
  } catch (e) {
    const msg = e?.response?.data?.message || '删除失败，该宠物可能有关联的业务记录'
    alert(msg)
  }
}

onMounted(() => {
  loadPets()
})
</script>

<style scoped>
.pet-page {
  min-height: 100vh;
  padding-bottom: var(--space-8);
}

/* 顶部渐变卡片 */
.header-card {
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  margin-bottom: var(--space-5);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  color: #fff;
  position: relative;
  overflow: hidden;
}

.header-card::before {
  content: '';
  position: absolute;
  top: -30%;
  right: -10%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.header-card::after {
  content: '';
  position: absolute;
  bottom: -40%;
  right: 10%;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-title {
  font-size: var(--font-size-2xl);
  font-weight: 800;
  margin: 0 0 var(--space-1) 0;
  letter-spacing: -0.02em;
}

.header-sub {
  font-size: var(--font-size-sm);
  margin: 0;
  opacity: 0.85;
}

.btn-add {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}

.add-icon {
  font-size: var(--font-size-lg);
  font-weight: 700;
  margin-right: 2px;
}

/* 宠物卡片网格 */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

@media (max-width: 1024px) {
  .pet-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .pet-grid {
    grid-template-columns: 1fr;
  }

  .header-card {
    flex-direction: column;
    text-align: center;
  }
}

/* 宠物卡片 */
.pet-card {
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  border: 1px solid var(--border-light);
}

.pet-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

/* 头像 */
.pet-avatar-wrap {
  position: relative;
  margin-bottom: var(--space-3);
}

.pet-avatar {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.pet-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-avatar-fallback {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-inverse);
  user-select: none;
}

/* 状态徽章 */
.pet-status-badge {
  position: absolute;
  bottom: -2px;
  right: -4px;
  font-size: var(--font-size-xs);
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  border: 2px solid var(--text-inverse);
  white-space: nowrap;
}

.status-normal {
  background: var(--color-success);
  color: var(--text-inverse);
}

.status-passed {
  background: var(--text-muted);
  color: var(--text-inverse);
}

.status-transferred {
  background: var(--color-accent);
  color: var(--text-inverse);
}

/* 信息区域 */
.pet-info {
  width: 100%;
  margin-bottom: var(--space-4);
}

.pet-name-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.pet-name {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--text-title);
  margin: 0;
}

.pet-type-icon {
  font-size: var(--font-size-lg);
}

.pet-breed {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0 0 var(--space-3) 0;
}

.pet-meta {
  display: flex;
  justify-content: center;
  gap: var(--space-4);
}

.meta-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.meta-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-weight: 500;
}

.meta-value {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-title);
}

.gender-female {
  color: var(--color-accent, #F472B6);
}

.gender-male {
  color: var(--color-primary);
}

/* 操作按钮 */
.pet-actions {
  display: flex;
  gap: var(--space-2);
  width: 100%;
}

.pet-actions .btn {
  flex: 1;
  min-width: 0;
}

/* 骨架屏 */
.skeleton-card {
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-8) var(--space-4);
}

.empty-illustration {
  margin-bottom: var(--space-5);
  position: relative;
  display: inline-block;
}

.empty-paw {
  font-size: var(--font-size-3xl, 64px);
  opacity: 0.2;
  animation: pawBounce 2s ease-in-out infinite;
}

@keyframes pawBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-face {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.empty-head {
  width: 40px;
  height: 32px;
  background: var(--color-primary-bg);
  border-radius: 50%;
  position: relative;
}

.empty-eye {
  width: 6px;
  height: 6px;
  background: var(--color-primary);
  border-radius: 50%;
  position: absolute;
  top: 10px;
}

.empty-eye.left { left: 10px; }
.empty-eye.right { right: 10px; }

.empty-nose {
  width: 5px;
  height: 4px;
  background: var(--color-accent);
  border-radius: 50%;
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
}

.empty-ear {
  width: 12px;
  height: 14px;
  background: var(--color-primary-bg);
  position: absolute;
  top: -8px;
  border-radius: 50% 50% 0 0;
}

.empty-ear.left { left: 4px; transform: rotate(-15deg); }
.empty-ear.right { right: 4px; transform: rotate(15deg); }

.empty-text {
  font-size: var(--font-size-md);
  color: var(--text-muted);
  margin: 0 0 var(--space-5) 0;
}

/* 动画 */
.pet-card {
  animation: fadeInUp 0.4s ease both;
}

.pet-card:nth-child(1) { animation-delay: 0s; }
.pet-card:nth-child(2) { animation-delay: 0.06s; }
.pet-card:nth-child(3) { animation-delay: 0.12s; }
.pet-card:nth-child(4) { animation-delay: 0.18s; }
.pet-card:nth-child(5) { animation-delay: 0.24s; }
.pet-card:nth-child(6) { animation-delay: 0.30s; }

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
