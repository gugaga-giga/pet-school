<template>
  <div class="video-page">
    <div class="video-header">
      <div>
        <h1 class="page-title">训练视频</h1>
        <p class="video-subtitle">观看专业训练视频，学习科学训宠方法</p>
      </div>
    </div>

    <div class="video-layout">
      <div class="video-list-col">
        <div class="list-header">
          <h3 class="list-title">视频列表</h3>
          <button class="btn btn-secondary btn-sm" @click="loadVideos">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
            刷新
          </button>
        </div>

        <div v-if="videos.length" class="video-cards">
          <div
            v-for="v in videos"
            :key="v.filename"
            class="video-card"
            :class="{ 'video-card--active': currentUrl && currentUrl.includes(v.filename) }"
            @click="playVideo(v.url)"
          >
            <div class="video-card__icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
            </div>
            <div class="video-card__info">
              <h4 class="video-card__name">{{ v.filename }}</h4>
              <span class="video-card__size">{{ (v.size / 1024 / 1024).toFixed(1) }} MB</span>
            </div>
            <button class="btn btn-primary btn-sm video-card__play">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
              播放
            </button>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-state__icon">🎬</div>
          <p class="empty-state__text">暂无视频</p>
          <p class="empty-state__hint">管理员上传后将在此显示</p>
        </div>
      </div>

      <div class="video-player-col">
        <div class="player-card">
          <div class="player-header">
            <h3 class="player-title">在线播放</h3>
            <span v-if="currentUrl" class="tag tag-primary">播放中</span>
          </div>

          <div class="form-group">
            <label>视频地址</label>
            <div class="url-input-row">
              <input v-model="videoUrl" placeholder="/api/video/file/xxx.mp4" class="url-input" />
              <button class="btn btn-primary btn-sm" @click="playVideo(videoUrl)" :disabled="!videoUrl">播放</button>
            </div>
          </div>

          <div v-if="currentUrl" class="player-area">
            <div class="player-container">
              <video
                ref="videoPlayer"
                :src="currentUrl"
                controls
                preload="metadata"
                playsinline
                class="player-video"
              ></video>
            </div>
            <p class="player-hint">支持进度条拖动、音量调节、全屏播放</p>
          </div>

          <div v-else class="player-placeholder">
            <div class="placeholder-icon">
              <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
            </div>
            <p class="placeholder-text">选择视频或输入地址开始播放</p>
          </div>
        </div>

        <div class="guide-card">
          <h4 class="guide-title">使用说明</h4>
          <ol class="guide-list">
            <li>管理员在「客户维护」页面上传训练视频</li>
            <li>在视频列表中点击播放按钮</li>
            <li>或手动输入视频地址播放</li>
            <li>支持进度条拖动、暂停、音量调节</li>
          </ol>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { videoApi } from '../../api'

const videos = ref([])
const videoUrl = ref('')
const currentUrl = ref('')
const videoPlayer = ref(null)

async function loadVideos() {
  try {
    const res = await videoApi.list()
    if (res.code === 0) videos.value = res.data
  } catch (e) {
    console.error(e)
  }
}

function playVideo(url) {
  if (!url) return
  if (!url.startsWith('http') && !url.startsWith('/')) {
    url = '/api' + (url.startsWith('/') ? url : '/' + url)
  }
  if (!url.startsWith('/api/video/file/')) {
    url = '/api/video/file/' + url.replace(/^\/video\/file\//, '')
  }
  currentUrl.value = url
  videoUrl.value = url
}

onMounted(() => loadVideos())
</script>

<style scoped>
.video-page {
  min-height: 100%;
}

.video-header {
  margin-bottom: var(--space-5);
}

.video-subtitle {
  color: var(--text-muted);
  font-size: var(--font-size-base);
  margin-top: var(--space-1);
}

.video-layout {
  display: flex;
  gap: var(--space-5);
  align-items: flex-start;
}

.video-list-col {
  width: 380px;
  flex-shrink: 0;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.list-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
}

.video-cards {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.video-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-card);
  border: 1.5px solid var(--border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.video-card:hover {
  border-color: var(--color-primary-light);
  background: var(--color-primary-bg);
  transform: translateX(4px);
  box-shadow: var(--shadow-card-hover);
}

.video-card--active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  box-shadow: 0 0 0 3px rgba(79, 124, 255, 0.1);
}

.video-card__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-base);
}

.video-card:hover .video-card__icon {
  background: var(--color-primary);
  color: var(--text-inverse);
}

.video-card--active .video-card__icon {
  background: var(--color-primary);
  color: var(--text-inverse);
}

.video-card__info {
  flex: 1;
  min-width: 0;
}

.video-card__name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.video-card__size {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.video-card__play {
  flex-shrink: 0;
}

.video-player-col {
  flex: 1;
  min-width: 0;
}

.player-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
}

.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.player-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
}

.url-input-row {
  display: flex;
  gap: var(--space-2);
}

.url-input {
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

.url-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
  background: var(--bg-card);
}

.url-input::placeholder {
  color: var(--text-muted);
}

.player-area {
  margin-top: var(--space-4);
}

.player-container {
  background: #000;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.player-video {
  width: 100%;
  max-height: 460px;
  display: block;
}

.player-hint {
  margin-top: var(--space-2);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-align: center;
}

.player-placeholder {
  margin-top: var(--space-4);
  background: var(--bg-hover);
  border-radius: var(--radius-lg);
  padding: var(--space-8) var(--space-4);
  text-align: center;
  border: 2px dashed var(--border-color);
}

.placeholder-icon {
  margin-bottom: var(--space-3);
  opacity: 0.4;
}

.placeholder-text {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.guide-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  margin-top: var(--space-4);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
}

.guide-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin-bottom: var(--space-2);
}

.guide-list {
  padding-left: 18px;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  line-height: 2;
}

.empty-state {
  text-align: center;
  padding: var(--space-7) var(--space-4);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.empty-state__icon {
  font-size: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.6;
}

.empty-state__text {
  font-size: var(--font-size-md);
  color: var(--text-muted);
  margin-bottom: var(--space-1);
}

.empty-state__hint {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  opacity: 0.7;
}

@media (max-width: 860px) {
  .video-layout {
    flex-direction: column;
  }

  .video-list-col {
    width: 100%;
  }
}
</style>
