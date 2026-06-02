<template>
  <div class="customer-page">
    <div class="page-header">
      <h1 class="page-title">客户维护</h1>
      <p class="page-desc">管理客户信息、上传训练视频与直播控制</p>
    </div>

    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">客户列表</h3>
            <button class="btn btn-ghost btn-sm" @click="loadUsers">
              <span class="action-icon">↻</span> 刷新
            </button>
          </div>
          <div class="table-wrap" v-if="users.length">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>手机号</th>
                  <th>角色</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in users" :key="u.id">
                  <td><span class="cell-id">#{{ u.id }}</span></td>
                  <td><span class="cell-name">{{ u.username }}</span></td>
                  <td><span class="cell-phone">{{ u.phone || '-' }}</span></td>
                  <td>
                    <span :class="['tag', roleTagClass(u.role)]">{{ roleText(u.role) }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="empty-state" v-else>
            <div class="empty-icon">👥</div>
            <div class="empty-text">暂无客户数据</div>
          </div>
        </div>
      </div>

      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">上传训练视频</h3>
          </div>

          <div class="upload-area" @click="fileInput && fileInput.click()" :class="{ 'has-file': selectedFile }">
            <input type="file" accept="video/mp4" @change="onFileChange" ref="fileInput" class="file-hidden" />
            <div class="upload-icon">▶</div>
            <div class="upload-text" v-if="!selectedFile">点击选择 MP4 视频文件</div>
            <div class="upload-file-info" v-else>
              <span class="file-name">{{ selectedFile.name }}</span>
              <span class="file-size">{{ (selectedFile.size / 1024 / 1024).toFixed(1) }} MB</span>
            </div>
          </div>

          <div v-if="uploading" class="progress-bar-wrap">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ uploadProgress }}%</span>
          </div>

          <div class="form-actions" style="margin-top:var(--space-3)">
            <button class="btn btn-primary" @click="uploadVideo" :disabled="uploading || !selectedFile">
              {{ uploading ? '上传中...' : '上传视频' }}
            </button>
          </div>

          <div v-if="uploadMsg" :class="['alert', uploadMsgType==='success'?'alert-success':'alert-error']" style="margin-top:var(--space-3)">{{ uploadMsg }}</div>

          <div v-if="uploadedUrl" class="upload-result">
            <div class="result-icon">✓</div>
            <div class="result-body">
              <span class="result-label">上传成功</span>
              <span class="result-url">{{ uploadedUrl }}</span>
            </div>
            <button class="btn btn-ghost btn-sm" @click="copyUrl">复制</button>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">直播管理</h3>
          </div>
          <p class="card-desc">管理员一键开播，客户端自动接收直播画面</p>
          <div class="live-actions">
            <a href="/sender.html" target="_blank" class="btn btn-accent" style="text-decoration:none">
              <span class="live-dot"></span> 一键开播
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi, videoApi } from '../../api'
import axios from 'axios'

const users = ref([])
const selectedFile = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadMsg = ref('')
const uploadMsgType = ref('success')
const uploadedUrl = ref('')
const fileInput = ref(null)

async function loadUsers() { try{const res=await adminApi.userList();if(res.code===200) users.value=res.data}catch(e){} }

function onFileChange(e) {
  const file = e.target.files[0]
  if (file) {
    if (!file.name.toLowerCase().endsWith('.mp4')) {
      uploadMsg.value = '仅支持MP4格式视频'
      uploadMsgType.value = 'error'
      selectedFile.value = null
      return
    }
    selectedFile.value = file
    uploadMsg.value = ''
    uploadedUrl.value = ''
  }
}

async function uploadVideo() {
  if (!selectedFile.value) return
  uploading.value = true
  uploadProgress.value = 0
  uploadMsg.value = ''
  uploadedUrl.value = ''

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    const res = await axios.post('/api/video/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total) uploadProgress.value = Math.round((e.loaded / e.total) * 100)
      }
    })
    if (res.data.code === 0) {
      uploadMsg.value = '上传成功！'
      uploadMsgType.value = 'success'
      uploadedUrl.value = res.data.data.url
      selectedFile.value = null
      if (fileInput.value) fileInput.value.value = ''
    } else {
      uploadMsg.value = res.data.message || '上传失败'
      uploadMsgType.value = 'error'
    }
  } catch (e) {
    uploadMsg.value = '上传失败: ' + (e.response?.data?.message || e.message)
    uploadMsgType.value = 'error'
  } finally {
    uploading.value = false
  }
}

function copyUrl() {
  if (uploadedUrl.value) {
    navigator.clipboard.writeText(window.location.origin + uploadedUrl.value).catch(() => {})
    uploadMsg.value = '已复制视频地址'
    uploadMsgType.value = 'success'
  }
}

function roleText(role) {
  return ['客户', '训练师', '管理员'][role] || '未知'
}

function roleTagClass(role) {
  if (role === 0) return 'tag-info'
  if (role === 1) return 'tag-accent'
  if (role === 2) return 'tag-primary'
  return 'tag-warning'
}

onMounted(() => loadUsers())
</script>

<style scoped>
.customer-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-5);
}

.page-desc {
  font-size: var(--font-size-base);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0;
}

.card-desc {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-bottom: var(--space-4);
  line-height: 1.5;
}

.action-icon {
  font-size: var(--font-size-sm);
}

.table-wrap {
  overflow-x: auto;
  margin: 0 calc(var(--space-5) * -1);
  padding: 0 var(--space-5);
}

.cell-id {
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.cell-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
}

.cell-phone {
  font-family: var(--font-mono);
  font-size: var(--font-size-sm);
  color: var(--text-body);
}

.upload-area {
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-6) var(--space-4);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-base);
  background: var(--bg-input);
}

.upload-area:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}

.upload-area.has-file {
  border-color: var(--color-success);
  background: var(--color-success-bg);
  border-style: solid;
}

.file-hidden {
  display: none;
}

.upload-icon {
  font-size: 32px;
  color: var(--color-primary);
  margin-bottom: var(--space-2);
  opacity: 0.7;
}

.upload-text {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.upload-file-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.file-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
  font-size: var(--font-size-base);
}

.file-size {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-3);
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: var(--border-light);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  transition: width var(--transition-fast);
}

.progress-text {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  font-family: var(--font-mono);
  min-width: 36px;
  text-align: right;
}

.form-actions {
  display: flex;
  gap: var(--space-2);
}

.upload-result {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-3);
  padding: var(--space-3);
  background: var(--color-success-bg);
  border-radius: var(--radius-md);
  border: 1px solid #bbf7d0;
}

.result-icon {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  background: var(--color-success);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.result-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.result-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: #15803d;
}

.result-url {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-mono);
}

.live-actions {
  display: flex;
  align-items: center;
}

.live-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: #e74c3c;
  margin-right: var(--space-2);
  animation: pulse 1.5s ease infinite;
}
</style>
