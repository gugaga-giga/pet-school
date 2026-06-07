<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :before-upload="beforeUpload"
      :on-progress="handleProgress"
      :on-success="handleSuccess"
      :on-error="handleError"
      :file-list="fileList"
      :accept="acceptTypes"
      :multiple="true"
      drag
      :auto-upload="true"
    >
      <div class="upload-area">
        <el-icon :size="48" color="#c0c4cc"><UploadFilled /></el-icon>
        <p>将文件拖拽到此处，或<em>点击上传</em></p>
        <p class="upload-tip">支持 PDF、Word、TXT、Markdown 格式，单文件不超过 50MB</p>
      </div>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import type { UploadRawFile } from 'element-plus'

const props = defineProps<{
  knowledgeBaseId: number
}>()

const emit = defineEmits<{
  success: []
}>()

const uploadRef = ref()
const fileList = ref<UploadFile[]>([])
const acceptTypes = '.pdf,.doc,.docx,.txt,.md,.csv,.xlsx'

const uploadUrl = computed(() => `/api/v1/documents/upload`)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('access_token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

function beforeUpload(rawFile: UploadRawFile) {
  const maxSize = 50 * 1024 * 1024
  if (rawFile.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

function handleProgress(_file: UploadFile) {
  // progress handled by el-upload
}

function handleSuccess() {
  ElMessage.success('上传成功')
  fileList.value = []
  emit('success')
}

function handleError() {
  ElMessage.error('上传失败')
}
</script>

<style lang="scss" scoped>
.file-upload {
  .upload-area {
    padding: 20px;
    text-align: center;

    p {
      margin-top: 8px;
      color: var(--el-text-color-regular);
      font-size: 14px;

      em {
        color: var(--el-color-primary);
        font-style: normal;
      }
    }

    .upload-tip {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }
}
</style>
