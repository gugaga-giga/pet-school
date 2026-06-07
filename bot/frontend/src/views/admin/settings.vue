<template>
  <div class="settings-page">
    <h2>系统设置</h2>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="settings-card">
          <template #header>
            <span>LLM 参数配置</span>
          </template>
          <el-form ref="llmFormRef" :model="settings" label-width="120px">
            <el-form-item label="模型">
              <el-select v-model="settings.llm_model" style="width: 100%">
                <el-option label="GPT-4" value="gpt-4" />
                <el-option label="GPT-4-Turbo" value="gpt-4-turbo" />
                <el-option label="GPT-3.5-Turbo" value="gpt-3.5-turbo" />
                <el-option label="GLM-4" value="glm-4" />
                <el-option label="GLM-3-Turbo" value="glm-3-turbo" />
              </el-select>
            </el-form-item>
            <el-form-item label="温度">
              <el-slider v-model="settings.llm_temperature" :min="0" :max="2" :step="0.1" show-input />
            </el-form-item>
            <el-form-item label="最大Token">
              <el-input-number v-model="settings.llm_max_tokens" :min="100" :max="8192" :step="100" style="width: 100%" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="settings-card">
          <template #header>
            <span>RAG 参数配置</span>
          </template>
          <el-form ref="ragFormRef" :model="settings" label-width="120px">
            <el-form-item label="Chunk Size">
              <el-input-number v-model="settings.rag_chunk_size" :min="100" :max="2000" :step="50" style="width: 100%" />
            </el-form-item>
            <el-form-item label="Chunk Overlap">
              <el-input-number v-model="settings.rag_chunk_overlap" :min="0" :max="500" :step="10" style="width: 100%" />
            </el-form-item>
            <el-form-item label="Top K">
              <el-input-number v-model="settings.rag_top_k" :min="1" :max="20" :step="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="分数阈值">
              <el-slider v-model="settings.rag_score_threshold" :min="0" :max="1" :step="0.05" show-input />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="settings-card">
      <template #header>
        <span>系统参数配置</span>
      </template>
      <el-form ref="sysFormRef" :model="settings" label-width="120px">
        <el-form-item label="系统提示词">
          <el-input
            v-model="settings.system_prompt"
            type="textarea"
            :rows="6"
            placeholder="请输入系统提示词"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <div class="actions">
      <el-button type="primary" :loading="saving" @click="handleSave">
        <el-icon><Check /></el-icon>
        保存设置
      </el-button>
      <el-button @click="fetchSettings">
        <el-icon><RefreshLeft /></el-icon>
        重置
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemSettings, updateSystemSettings } from '@/api/admin'
import type { SystemSettings } from '@/types/api'

const saving = ref(false)

const settings = reactive<SystemSettings>({
  llm_model: 'gpt-3.5-turbo',
  llm_temperature: 0.7,
  llm_max_tokens: 2048,
  rag_chunk_size: 500,
  rag_chunk_overlap: 50,
  rag_top_k: 5,
  rag_score_threshold: 0.5,
  system_prompt: '你是一个专业的售前客服，请根据用户的问题提供准确、专业的回答。'
})

onMounted(() => {
  fetchSettings()
})

async function fetchSettings() {
  try {
    const res = await getSystemSettings()
    Object.assign(settings, res.data)
  } catch {
    // use default
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateSystemSettings(settings)
    ElMessage.success('设置保存成功')
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.settings-page {
  h2 {
    font-size: 20px;
    margin-bottom: 20px;
  }

  .settings-card {
    margin-bottom: 20px;
  }

  .actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }
}
</style>
