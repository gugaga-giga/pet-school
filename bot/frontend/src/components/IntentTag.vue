<template>
  <el-tag :type="tagType" size="small" effect="light">
    {{ intentLabel }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  intent: string
}>()

const intentMap: Record<string, { label: string; type: string }> = {
  product_inquiry: { label: '产品咨询', type: '' },
  price_query: { label: '价格查询', type: 'warning' },
  tech_support: { label: '技术支持', type: 'success' },
  after_sales: { label: '售后服务', type: 'danger' },
  comparison: { label: '产品对比', type: 'info' },
  recommendation: { label: '产品推荐', type: '' },
  complaint: { label: '投诉建议', type: 'danger' },
  greeting: { label: '问候', type: 'info' },
  other: { label: '其他', type: 'info' }
}

const tagType = computed(() => {
  return (intentMap[props.intent]?.type || 'info') as any
})

const intentLabel = computed(() => {
  return intentMap[props.intent]?.label || props.intent
})
</script>
