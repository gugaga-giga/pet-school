<template>
  <div class="live-page">
    <div class="live-header">
      <div>
        <h1 class="page-title">远程直播</h1>
        <p class="live-subtitle">实时观看训练师直播，远程学习训宠技巧</p>
      </div>
    </div>

    <div class="live-layout">
      <div class="live-left-col">
        <div class="live-list-card">
          <div class="list-header">
            <h3 class="list-title">直播列表</h3>
            <button class="btn btn-secondary btn-sm" @click="loadLives">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
              刷新
            </button>
          </div>

          <div v-if="lives.length" class="live-cards">
            <div
              v-for="l in lives"
              :key="l.id"
              class="live-card"
              :class="{ 'live-card--active': liveId === l.id && (isConnecting || isConnected) }"
            >
              <div class="live-card__body">
                <div class="live-card__top">
                  <h4 class="live-card__title">{{ l.title }}</h4>
                  <span :class="['tag', l.status === 0 ? 'tag-warning' : l.status === 1 ? 'tag-danger' : 'tag-success']">
                    <span v-if="l.status === 1" class="live-dot"></span>
                    {{ ['未开始', '直播中', '已结束'][l.status] }}
                  </span>
                </div>
                <div class="live-card__meta">
                  <span>训练师ID：{{ l.trainerId }}</span>
                  <span>开始：{{ l.startTime }}</span>
                </div>
              </div>
              <button
                v-if="l.status === 1"
                class="btn btn-primary btn-sm"
                @click="joinLive(l)"
              >加入直播</button>
            </div>
          </div>

          <div v-else class="empty-state">
            <div class="empty-state__icon">📡</div>
            <p class="empty-state__text">暂无直播</p>
            <p class="empty-state__hint">管理员开播后将在此显示</p>
          </div>
        </div>

        <div v-if="isConnecting || isConnected" class="player-card">
          <div class="player-header">
            <h3 class="player-title">直播画面</h3>
            <div class="player-badges">
              <span v-if="isConnected" class="tag tag-danger">
                <span class="live-dot"></span>
                LIVE
              </span>
              <span v-else class="tag tag-warning">连接中</span>
            </div>
          </div>

          <div class="player-container">
            <video ref="remoteVideo" autoplay playsinline class="player-video"></video>
            <div v-if="!isConnected" class="player-overlay">
              <div class="player-overlay__inner">
                <div class="player-overlay__icon">
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                </div>
                <p class="player-overlay__text">{{ connectStatus }}</p>
              </div>
            </div>
          </div>

          <div class="player-controls">
            <button class="btn btn-secondary btn-sm" @click="toggleAudio">
              <svg v-if="isMuted" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/><line x1="23" y1="9" x2="17" y2="15"/><line x1="17" y1="9" x2="23" y2="15"/></svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"/></svg>
              {{ isMuted ? '取消静音' : '静音' }}
            </button>
            <button class="btn btn-danger btn-sm" @click="disconnect">断开直播</button>
          </div>
        </div>
      </div>

      <div class="live-right-col">
        <div class="join-card">
          <h3 class="join-title">加入直播</h3>
          <p class="join-desc">管理员开播后，输入直播ID自动接收直播画面</p>

          <div class="form-group">
            <label>直播ID</label>
            <input v-model.number="liveId" type="number" min="1" placeholder="输入直播ID" />
          </div>

          <button
            class="btn btn-primary join-btn"
            @click="joinLiveById"
            :disabled="isConnecting"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            {{ isConnecting ? '正在连接...' : '加入直播' }}
          </button>

          <div v-if="statusMsg" :class="['alert', statusType === 'success' ? 'alert-success' : statusType === 'error' ? 'alert-error' : 'alert-info']" style="margin-top: var(--space-3)">
            {{ statusMsg }}
          </div>
        </div>

        <div class="log-card">
          <h4 class="log-title">连接日志</h4>
          <div class="log-container" v-html="logText"></div>
        </div>

        <div class="guide-card">
          <h4 class="guide-title">使用说明</h4>
          <ol class="guide-list">
            <li>管理员在后台点击「一键开播」</li>
            <li>在本页面输入直播ID，点击「加入直播」</li>
            <li>系统自动交换信令，直播画面自动播放</li>
          </ol>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { liveApi } from '../../api'

const lives = ref([])
const liveId = ref(1)
const isConnecting = ref(false)
const isConnected = ref(false)
const isMuted = ref(false)
const connectStatus = ref('')
const statusMsg = ref('')
const statusType = ref('info')
const logText = ref('等待加入直播...')
const remoteVideo = ref(null)

let pc = null
let candidates = []
let pollTimer = null

const rtcConfig = {
  iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
}

function addLog(msg) {
  const time = new Date().toLocaleTimeString()
  logText.value += `<div>[${time}] ${msg}</div>`
}

function setStatus(text, type) {
  statusMsg.value = text
  statusType.value = type
}

async function loadLives() {
  try {
    const res = await liveApi.listAll()
    if (res.code === 200) lives.value = res.data
  } catch (e) { console.error(e) }
}

function joinLive(l) {
  liveId.value = l.id
  joinLiveById()
}

async function joinLiveById() {
  isConnecting.value = true
  connectStatus.value = '正在连接...'
  setStatus('正在连接直播...', 'info')
  addLog('📡 开始连接直播 ID: ' + liveId.value)

  try {
    addLog('→ GET /api/webrtc/signal/' + liveId.value)
    const signalRes = await fetch('/api/webrtc/signal/' + liveId.value).then(r => r.json())
    addLog('← ' + JSON.stringify(signalRes).substring(0, 200))
    if (signalRes.code !== 200 || !signalRes.data || !signalRes.data.offerSdp) {
      if (signalRes.code === 404) {
        setStatus('该直播尚未开播，请等待管理员开启', 'error')
        addLog('❌ 未找到 Offer，管理员可能尚未开播')
      } else {
        setStatus('获取信令失败: ' + (signalRes.message || '未知错误'), 'error')
        addLog('❌ 获取信令失败: ' + JSON.stringify(signalRes))
      }
      isConnecting.value = false
      return
    }

    addLog('📥 获取到 Offer')

    pc = new RTCPeerConnection(rtcConfig)
    candidates = []

    pc.ontrack = (event) => {
      if (remoteVideo.value) {
        remoteVideo.value.srcObject = event.streams[0]
        isConnected.value = true
        isConnecting.value = false
        setStatus('🟢 直播连接成功！', 'success')
        addLog('📺 收到直播画面，播放成功')
      }
    }

    pc.onicecandidate = (event) => {
      if (event.candidate) {
        candidates.push(event.candidate)
      }
    }

    pc.oniceconnectionstatechange = () => {
      const state = pc.iceConnectionState
      addLog('🔗 ICE状态: ' + state)
      if (state === 'connected' || state === 'completed') {
        isConnected.value = true
        isConnecting.value = false
        setStatus('🟢 直播连接成功！', 'success')
      } else if (state === 'disconnected') {
        isConnected.value = false
        setStatus('⚠️ 直播连接断开', 'error')
      } else if (state === 'failed') {
        isConnected.value = false
        isConnecting.value = false
        setStatus('❌ 连接失败', 'error')
      }
    }

    const offer = JSON.parse(signalRes.data.offerSdp)
    await pc.setRemoteDescription(new RTCSessionDescription(offer))
    addLog('✅ Offer 已应用')

    if (signalRes.data.senderCandidates) {
      try {
        const sc = JSON.parse(signalRes.data.senderCandidates)
        for (const c of sc) {
          await pc.addIceCandidate(new RTCIceCandidate(c))
        }
        addLog('✅ Sender Candidates 已添加 (' + sc.length + '个)')
      } catch (e) {
        addLog('⚠️ Sender Candidates 解析失败')
      }
    }

    connectStatus.value = '正在生成应答...'
    const answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)
    addLog('✅ Answer 已创建')

    await new Promise(resolve => {
      if (pc.iceGatheringState === 'complete') return resolve()
      const check = () => { if (pc.iceGatheringState === 'complete') resolve() }
      pc.onicegatheringstatechange = check
      setTimeout(resolve, 3000)
    })

    const answerSdp = JSON.stringify(pc.localDescription)
    const receiverCandidates = JSON.stringify(candidates)

    connectStatus.value = '正在上传应答...'
    await fetch('/api/webrtc/answer/' + liveId.value, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ answerSdp, receiverCandidates })
    })
    addLog('📤 Answer 已上传，等待连接建立...')
    setStatus('⏳ 等待连接建立...', 'info')
    connectStatus.value = '等待连接建立...'

  } catch (e) {
    addLog('❌ 连接失败: ' + e.message)
    setStatus('连接失败: ' + e.message, 'error')
    isConnecting.value = false
  }
}

function toggleAudio() {
  if (remoteVideo.value) {
    remoteVideo.value.muted = !remoteVideo.value.muted
    isMuted.value = remoteVideo.value.muted
  }
}

function disconnect() {
  if (pc) { pc.close(); pc = null }
  isConnected.value = false
  isConnecting.value = false
  setStatus('已断开直播', 'info')
  addLog('⏹ 已断开直播')
}

onMounted(() => loadLives())
onUnmounted(() => disconnect())
</script>

<style scoped>
.live-page {
  min-height: 100%;
}

.live-header {
  margin-bottom: var(--space-5);
}

.live-subtitle {
  color: var(--text-muted);
  font-size: var(--font-size-base);
  margin-top: var(--space-1);
}

.live-layout {
  display: flex;
  gap: var(--space-5);
  align-items: flex-start;
}

.live-left-col {
  flex: 1;
  min-width: 0;
}

.live-list-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
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

.live-cards {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.live-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-card);
  border: 1.5px solid var(--border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.live-card:hover {
  border-color: var(--color-primary-light);
  background: var(--color-primary-bg);
  box-shadow: var(--shadow-card-hover);
}

.live-card--active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  box-shadow: 0 0 0 3px rgba(79, 124, 255, 0.1);
}

.live-card__body {
  flex: 1;
  min-width: 0;
}

.live-card__top {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: 4px;
}

.live-card__title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.live-card__meta {
  display: flex;
  gap: var(--space-3);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.live-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
  background: var(--color-danger);
  margin-right: 4px;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.player-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  margin-top: var(--space-4);
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

.player-badges {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.player-container {
  position: relative;
  background: #0a0a1a;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.player-video {
  width: 100%;
  max-height: 500px;
  display: block;
}

.player-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(10, 10, 26, 0.85);
  backdrop-filter: blur(4px);
}

.player-overlay__inner {
  text-align: center;
}

.player-overlay__icon {
  color: var(--text-muted);
  margin-bottom: var(--space-3);
  opacity: 0.6;
}

.player-overlay__text {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.player-controls {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.live-right-col {
  width: 380px;
  flex-shrink: 0;
}

.join-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
}

.join-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin-bottom: var(--space-1);
}

.join-desc {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-bottom: var(--space-4);
}

.join-btn {
  width: 100%;
  padding: 12px;
  font-size: var(--font-size-md);
}

.log-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  margin-top: var(--space-4);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
}

.log-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin-bottom: var(--space-3);
}

.log-container {
  background: #0d1117;
  border-radius: var(--radius-md);
  padding: var(--space-3);
  max-height: 220px;
  overflow-y: auto;
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
  color: #3fb950;
  line-height: 1.7;
}

.log-container :deep(div) {
  padding: 1px 0;
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
  .live-layout {
    flex-direction: column;
  }

  .live-right-col {
    width: 100%;
  }
}
</style>
