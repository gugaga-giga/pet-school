<template>
  <div class="verify-page">
    <div class="verify-bg">
      <div class="bg-pattern"></div>
    </div>

    <div class="verify-container">
      <div v-if="loading" class="verify-card card loading-state">
        <div class="loading-spinner"></div>
        <p class="loading-text">正在验证证书…</p>
      </div>

      <div v-else-if="cert" class="verify-card card verified">
        <div class="verify-badge success">
          <div class="badge-icon">✅</div>
          <h1 class="badge-title">官方认证证书</h1>
          <p class="badge-subtitle">该证书经系统验证，真实有效</p>
        </div>

        <div class="cert-detail">
          <div class="detail-row">
            <span class="detail-label">宠物名称</span>
            <span class="detail-value">{{ cert.petName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">主人</span>
            <span class="detail-value">{{ cert.ownerName || cert.userName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">课程</span>
            <span class="detail-value">{{ cert.courseName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">毕业时间</span>
            <span class="detail-value">{{ cert.graduateDate || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">证书编号</span>
            <span class="detail-value cert-no">{{ cert.certNo || cert.certificateNo || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">颁发日期</span>
            <span class="detail-value">{{ cert.issueDate || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态</span>
            <span class="detail-value">
              <span class="tag" :class="cert.status === 1 ? 'tag-danger' : 'tag-success'">{{ cert.status === 1 ? '已作废' : '正常' }}</span>
            </span>
          </div>
        </div>

        <div v-if="cert.certificateUrl" class="cert-image-section">
          <img :src="'/api' + cert.certificateUrl" alt="证书图片" class="cert-image" />
        </div>

        <div class="verify-footer">
          <span class="footer-seal">🐾 宠物培训学校</span>
          <span class="footer-note">本验证结果由系统自动生成，具有官方效力</span>
        </div>
      </div>

      <div v-else class="verify-card card failed">
        <div class="verify-badge fail">
          <div class="badge-icon">❌</div>
          <h1 class="badge-title">证书验证失败</h1>
          <p class="badge-subtitle">{{ error || '未找到该证书信息，请核实证书编号是否正确' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { certificateApi } from '../api'

const route = useRoute()
const certNo = route.params.certNo
const cert = ref(null)
const error = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await certificateApi.verify(certNo)
    if (res.code === 200 && res.data) {
      cert.value = res.data
    } else {
      error.value = res.message || '未找到该证书信息'
    }
  } catch (e) {
    error.value = '证书验证请求失败，请稍后重试'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.verify-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.verify-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: linear-gradient(135deg, #eef2ff 0%, #f7f9fc 30%, #fff8ee 60%, #ecfdf5 100%);
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 20% 50%, rgba(79, 124, 255, 0.04) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 184, 107, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 50% 80%, rgba(60, 203, 127, 0.04) 0%, transparent 50%);
}

.verify-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 560px;
  padding: var(--space-5);
}

.verify-card {
  border-radius: var(--radius-xl);
  animation: slideUp 0.4s ease both;
}

.verify-card.loading-state {
  text-align: center;
  padding: var(--space-8) var(--space-5);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-light);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  margin: 0 auto var(--space-4);
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: var(--font-size-base);
  color: var(--text-muted);
  margin: 0;
}

.verify-badge {
  text-align: center;
  padding: var(--space-6) var(--space-5) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.verify-badge.success {
  background: linear-gradient(180deg, var(--color-success-bg) 0%, transparent 100%);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
}

.verify-badge.fail {
  background: linear-gradient(180deg, var(--color-danger-bg) 0%, transparent 100%);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
}

.badge-icon {
  font-size: 56px;
  margin-bottom: var(--space-3);
}

.badge-title {
  font-size: var(--font-size-2xl);
  font-weight: 800;
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
  letter-spacing: -0.02em;
}

.badge-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
}

.cert-detail {
  padding: var(--space-5);
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  flex-shrink: 0;
}

.detail-value {
  font-size: var(--font-size-sm);
  color: var(--text-title);
  font-weight: 500;
  text-align: right;
  word-break: break-all;
}

.cert-no {
  font-family: var(--font-mono);
  letter-spacing: 0.02em;
}

.cert-image-section {
  padding: 0 var(--space-5) var(--space-5);
}

.cert-image {
  width: 100%;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.verify-footer {
  text-align: center;
  padding: var(--space-4) var(--space-5) var(--space-5);
  border-top: 1px solid var(--border-light);
}

.footer-seal {
  display: block;
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: var(--space-1);
}

.footer-note {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 640px) {
  .verify-container {
    padding: var(--space-3);
  }

  .badge-title {
    font-size: var(--font-size-xl);
  }
}
</style>
