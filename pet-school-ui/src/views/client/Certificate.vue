<template>
  <div class="my-cert-page">
    <div class="page-header">
      <h1 class="page-title">🎓 我的毕业证书</h1>
    </div>

    <div v-if="!certificates.length" class="empty-card card">
      <div class="empty-illustration">
        <div class="empty-cap">🎓</div>
        <div class="empty-sparkle sparkle-1">✨</div>
        <div class="empty-sparkle sparkle-2">⭐</div>
        <div class="empty-sparkle sparkle-3">💫</div>
      </div>
      <h3 class="empty-title">您的宠物还没有毕业证书</h3>
      <p class="empty-hint">完成课程后系统将自动生成毕业证书</p>
    </div>

    <div v-else class="cert-grid">
      <div v-for="c in certificates" :key="c.id" class="cert-card card">
        <div class="cert-thumb">
          <img
            v-if="c.certificateUrl"
            :src="'/api' + c.certificateUrl"
            alt="毕业证书"
            class="cert-img"
          />
          <div v-else class="cert-thumb-placeholder">
            <span class="placeholder-icon">🎓</span>
            <span class="placeholder-label">毕业证书</span>
          </div>
        </div>
        <div class="cert-body">
          <h3 class="cert-pet-name">{{ c.petName || '宠物' }}</h3>
          <p class="cert-course">{{ c.courseName || '训练课程' }}</p>
          <div class="cert-meta">
            <span class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              {{ c.graduateDate || c.issueDate || '-' }}
            </span>
            <span class="meta-item meta-cert-no">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              {{ c.certNo || c.certificateNo || '-' }}
            </span>
          </div>
          <div class="cert-status">
            <span class="tag" :class="c.status === 1 ? 'tag-danger' : 'tag-success'">{{ c.status === 1 ? '已作废' : '正常' }}</span>
          </div>
        </div>
        <div class="cert-actions">
          <button class="btn btn-primary btn-sm cert-action-btn" @click="viewCert(c)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            查看证书
          </button>
          <button class="btn btn-accent btn-sm cert-action-btn" @click="downloadCert(c)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            下载
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { certificateApi } from '../../api'

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('client_user')) || {}
  } catch {
    return {}
  }
})

const certificates = ref([])

async function loadCertificates() {
  if (!user.value.id) return
  try {
    const res = await certificateApi.myCertificates(user.value.id)
    if (res.code === 200) certificates.value = res.data
  } catch {}
}

function viewCert(cert) {
  if (cert.certificateUrl) {
    window.open('/api' + cert.certificateUrl, '_blank')
  } else {
    alert('证书图片未生成')
  }
}

async function downloadCert(cert) {
  try {
    await certificateApi.download(cert.id)
  } catch {}
}

onMounted(loadCertificates)
</script>

<style scoped>
.my-cert-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-5);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-title);
  margin: 0;
}

.empty-card {
  text-align: center;
  padding: var(--space-8) var(--space-5);
}

.empty-illustration {
  position: relative;
  display: inline-block;
  margin-bottom: var(--space-5);
}

.empty-cap {
  font-size: 72px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.empty-sparkle {
  position: absolute;
  font-size: 20px;
  animation: sparkle 2s ease-in-out infinite;
}

.sparkle-1 {
  top: -8px;
  right: -16px;
  animation-delay: 0s;
}

.sparkle-2 {
  top: 8px;
  left: -20px;
  animation-delay: 0.6s;
}

.sparkle-3 {
  bottom: 4px;
  right: -24px;
  animation-delay: 1.2s;
}

@keyframes sparkle {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.empty-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
}

.empty-hint {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
}

.cert-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5);
}

.cert-card {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all var(--transition-slow);
}

.cert-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-glow-accent), var(--shadow-card-hover);
  transform: translateY(-4px);
}

.cert-thumb {
  width: 100%;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: linear-gradient(135deg, #fff8ee 0%, #eef2ff 50%, #ecfdf5 100%);
  position: relative;
}

.cert-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.cert-card:hover .cert-img {
  transform: scale(1.03);
}

.cert-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.placeholder-icon {
  font-size: 48px;
  opacity: 0.6;
}

.placeholder-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  font-weight: 500;
}

.cert-body {
  padding: var(--space-4) var(--space-4) 0;
  flex: 1;
}

.cert-pet-name {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--text-title);
  margin: 0 0 var(--space-1) 0;
}

.cert-course {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  font-weight: 500;
  margin: 0 0 var(--space-3) 0;
}

.cert-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.meta-cert-no {
  font-family: var(--font-mono);
}

.cert-status {
  margin-bottom: var(--space-3);
}

.cert-actions {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4) var(--space-4);
  border-top: 1px solid var(--border-light);
}

.cert-action-btn {
  flex: 1;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1024px) {
  .cert-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .cert-grid {
    grid-template-columns: 1fr;
  }
}
</style>
