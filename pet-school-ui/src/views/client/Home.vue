<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-content">
          <h1 class="hero-title">让宠物拥有<br />更好的成长方式</h1>
          <p class="hero-subtitle">专业训练、安心寄宿、科学管理</p>
          <div class="hero-actions">
            <button class="btn btn-primary btn-lg" @click="router.push('/client/course')">立即预约课程</button>
            <button class="btn btn-accent btn-lg" @click="router.push('/client/room')">查看寄宿服务</button>
          </div>
        </div>
        <div class="hero-visual">
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cute%20golden%20retriever%20puppy%20training%20in%20modern%20bright%20pet%20school%2C%20warm%20sunlight%2C%20professional%20training%20environment%2C%20soft%20colors&image_size=landscape_16_9"
            alt="宠物训练"
            class="hero-img"
          />
        </div>
      </div>
    </section>

    <section class="stats">
      <div class="stats-grid">
        <div class="stat-card card" v-for="s in stats" :key="s.label">
          <span class="stat-value">{{ s.value }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </div>
    </section>

    <section class="courses">
      <h2 class="section-title">热门课程</h2>
      <p class="section-desc">精选优质训练课程，助力宠物健康成长</p>
      <div class="courses-grid" v-if="courses.length">
        <div class="course-card card" v-for="c in courses" :key="c.id">
          <div class="course-body">
            <h3 class="course-name">{{ c.name }}</h3>
            <p class="course-desc">{{ c.description || '暂无描述' }}</p>
            <div class="course-meta">
              <span class="price">¥{{ c.monthlyPrice }}<small>/月</small></span>
              <span class="tag">{{ c.duration }}个月</span>
            </div>
          </div>
          <button class="btn btn-primary" @click="router.push('/client/course')">立即报名</button>
        </div>
      </div>
      <div v-else class="card empty-card">暂无课程</div>
    </section>

    <section class="features">
      <h2 class="section-title">我们的优势</h2>
      <p class="section-desc">全方位宠物服务，让您和爱宠安心无忧</p>
      <div class="features-grid">
        <div class="feature-card card" v-for="f in features" :key="f.title">
          <div class="feature-icon" v-html="f.icon"></div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
        </div>
      </div>
    </section>
    <!-- First login guidance modal -->
    <div class="modal-overlay" v-if="showGuide" @click.self="showGuide = false">
      <div class="modal-content guide-modal">
        <div class="guide-icon">🐾</div>
        <h2 class="guide-title">欢迎使用宠物综合服务平台</h2>
        <p class="guide-desc">请先完善宠物资料，以便享受课程、寄宿、医疗等全方位服务</p>
        <div class="guide-actions">
          <button class="btn btn-primary btn-lg" @click="goAddPet">立即填写</button>
          <button class="btn btn-ghost" @click="dismissGuide">稍后填写</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { courseApi, petApi } from '../../api'

const router = useRouter()
const courses = ref([])
const showGuide = ref(false)

const stats = [
  { value: '2,680+', label: '累计宠物' },
  { value: '36+', label: '训练课程' },
  { value: '1,500+', label: '满意用户' },
  { value: '98.6%', label: '好评率' }
]

const features = [
  {
    icon: '<svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="24" cy="24" r="22" style="fill:var(--color-primary-bg)"/><path d="M16 24l4 4 8-8" style="stroke:var(--color-primary)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 14h20v20H14z" style="stroke:var(--color-primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity=".3"/></svg>',
    title: '专业训练',
    desc: '持证训练师团队，科学正向训练方法，量身定制训练方案'
  },
  {
    icon: '<svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="24" cy="24" r="22" style="fill:var(--color-accent-bg)"/><path d="M24 14v10l6 4" style="stroke:var(--color-accent)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="24" cy="24" r="10" style="stroke:var(--color-accent)" stroke-width="2" opacity=".3"/></svg>',
    title: '安心寄宿',
    desc: '全天候专业看护，恒温舒适环境，实时视频查看爱宠状态'
  },
  {
    icon: '<svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="24" cy="24" r="22" style="fill:var(--color-success-bg)"/><path d="M18 30V22m6-4v12m6-8v8" style="stroke:var(--color-success)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 34h20" style="stroke:var(--color-success)" stroke-width="2" stroke-linecap="round" opacity=".3"/></svg>',
    title: '科学管理',
    desc: 'AI健康监测预警，疫苗驱虫智能提醒，全方位健康档案管理'
  }
]

async function loadCourses() {
  try {
    const res = await courseApi.listAll()
    if (res.code === 200) courses.value = res.data
  } catch (e) {}
}

async function checkPetGuide() {
  try {
    const res = await petApi.hasPet()
    if (res.code === 200 && !res.data.hasPet) {
      const dismissed = sessionStorage.getItem('pet_guide_dismissed')
      if (!dismissed) {
        showGuide.value = true
      }
    }
  } catch (e) {}
}

function goAddPet() {
  showGuide.value = false
  router.push('/client/pet/create')
}

function dismissGuide() {
  showGuide.value = false
  sessionStorage.setItem('pet_guide_dismissed', '1')
}

onMounted(() => {
  loadCourses()
  checkPetGuide()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg-page);
}

.hero {
  background: linear-gradient(135deg, var(--color-primary-bg) 0%, var(--color-accent-bg) 50%, var(--color-success-bg) 100%);
  padding: var(--space-8) var(--space-6);
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: var(--space-8);
}

.hero-content {
  flex: 1;
  min-width: 0;
}

.hero-title {
  font-size: var(--font-size-3xl);
  font-weight: 800;
  color: var(--text-title);
  line-height: 1.25;
  margin: 0 0 var(--space-4) 0;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: var(--font-size-lg);
  color: var(--text-body);
  margin: 0 0 var(--space-6) 0;
}

.hero-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.hero-visual {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: center;
}

.hero-img {
  width: 100%;
  max-width: 520px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card-hover);
  object-fit: cover;
  aspect-ratio: 16 / 9;
}

.stats {
  max-width: 1200px;
  margin: calc(var(--space-8) * -1) auto 0;
  padding: 0 var(--space-6);
  position: relative;
  z-index: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.stat-card {
  text-align: center;
  padding: var(--space-5) var(--space-4);
  border-radius: var(--radius-lg);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.stat-value {
  display: block;
  font-size: var(--font-size-2xl);
  font-weight: 800;
  color: var(--color-primary);
  margin-bottom: var(--space-1);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.courses {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-title);
  text-align: center;
  margin: 0 0 var(--space-2) 0;
}

.section-desc {
  font-size: var(--font-size-base);
  color: var(--text-muted);
  text-align: center;
  margin: 0 0 var(--space-6) 0;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5);
}

.course-card {
  display: flex;
  flex-direction: column;
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.course-body {
  flex: 1;
  margin-bottom: var(--space-4);
}

.course-name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
}

.course-desc {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin: 0 0 var(--space-3) 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.course-meta .price {
  font-size: var(--font-size-xl);
}

.course-meta .price small {
  font-size: var(--font-size-xs);
  font-weight: 400;
  color: var(--text-muted);
}

.course-card .btn {
  width: 100%;
}

.empty-card {
  text-align: center;
  padding: var(--space-8);
  color: var(--text-muted);
}

.features {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-6) var(--space-8);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5);
}

.feature-card {
  text-align: center;
  padding: var(--space-6) var(--space-5);
  border-radius: var(--radius-lg);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.feature-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto var(--space-4) auto;
}

.feature-icon svg {
  width: 100%;
  height: 100%;
}

.feature-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
}

.feature-desc {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin: 0;
  line-height: 1.7;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero,
.stats,
.courses,
.features {
  animation: fadeInUp 0.6s ease both;
}

.stats {
  animation-delay: 0.15s;
}

.courses {
  animation-delay: 0.3s;
}

.features {
  animation-delay: 0.45s;
}

@media (max-width: 1024px) {
  .courses-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-inner {
    flex-direction: column;
    text-align: center;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-title {
    font-size: var(--font-size-2xl);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .courses-grid,
  .features-grid {
    grid-template-columns: 1fr;
  }
}

.guide-modal {
  max-width: 440px;
  text-align: center;
  padding: var(--space-7) var(--space-5);
  box-shadow: var(--shadow-float);
}

.guide-icon {
  font-size: var(--font-size-3xl);
  margin-bottom: var(--space-3);
  animation: bounce 1s ease infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.guide-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-title);
  margin: 0 0 var(--space-2) 0;
}

.guide-desc {
  font-size: var(--font-size-base);
  color: var(--text-body);
  margin: 0 0 var(--space-5) 0;
  line-height: var(--line-height-relaxed);
}

.guide-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
</style>
