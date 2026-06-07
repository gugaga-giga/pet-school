<template>
  <div class="stat-card" :style="{ borderLeftColor: color }">
    <div class="stat-icon" :style="{ backgroundColor: color + '20', color: color }">
      <el-icon :size="24"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-info">
      <span class="stat-value">{{ formatNumber(value) }}</span>
      <span class="stat-title">{{ title }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatNumber } from '@/utils/format'

defineProps<{
  title: string
  value: number
  icon: string
  color: string
}>()
</script>

<style lang="scss" scoped>
.stat-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-left: 4px solid;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-info {
    display: flex;
    flex-direction: column;

    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: var(--el-text-color-primary);
      line-height: 1.2;
    }

    .stat-title {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      margin-top: 4px;
    }
  }
}
</style>
