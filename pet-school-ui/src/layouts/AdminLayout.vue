<template>
  <div class="admin-layout">
    <!-- Mobile overlay -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>

    <aside class="sidebar glass" :class="{ open: sidebarOpen }">
      <div class="sidebar-brand" @click="$router.push('/admin')">
        <div class="brand-icon">🐾</div>
        <div class="brand-text">
          <span class="brand-name">PetSchool</span>
          <span class="brand-sub">管理后台</span>
        </div>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin/course" class="nav-item">
          <span class="nav-icon">📚</span><span class="nav-label">课程管理</span>
        </router-link>
        <router-link to="/admin/pet" class="nav-item">
          <span class="nav-icon">🐾</span><span class="nav-label">宠物管理</span>
        </router-link>
        <router-link to="/admin/customer" class="nav-item nav-sub-item">
          <span class="nav-icon">👥</span><span class="nav-label">客户维护</span>
        </router-link>
        <router-link to="/admin/room" class="nav-item">
          <span class="nav-icon">🏠</span><span class="nav-label">寄宿管理</span>
        </router-link>
        <router-link to="/admin/order" class="nav-item">
          <span class="nav-icon">📋</span><span class="nav-label">订单管理</span>
        </router-link>
        <router-link to="/admin/coupon" class="nav-item">
          <span class="nav-icon">🎫</span><span class="nav-label">优惠管理</span>
        </router-link>
        <router-link to="/admin/certificate" class="nav-item">
          <span class="nav-icon">🎓</span><span class="nav-label">毕业证书</span>
        </router-link>

        <!-- Medical Group -->
        <div class="nav-group" :class="{ expanded: medicalExpanded }">
          <div class="nav-group-header" @click="medicalExpanded = !medicalExpanded">
            <span class="nav-group-title">🏥 医疗管理</span>
            <span class="nav-group-arrow" :class="{ rotated: medicalExpanded }">›</span>
          </div>
          <div class="nav-group-items" v-show="medicalExpanded">
            <router-link to="/admin/medical-dept" class="nav-item nav-sub-item">
              <span class="nav-icon">🏥</span><span class="nav-label">科室管理</span>
            </router-link>
            <router-link to="/admin/medical-doctor" class="nav-item nav-sub-item">
              <span class="nav-icon">🩺</span><span class="nav-label">医生管理</span>
            </router-link>
            <router-link to="/admin/medical-order" class="nav-item nav-sub-item">
              <span class="nav-icon">📋</span><span class="nav-label">医疗订单</span>
            </router-link>
            <router-link to="/admin/medical-record" class="nav-item nav-sub-item">
              <span class="nav-icon">📄</span><span class="nav-label">病历管理</span>
            </router-link>
            <router-link to="/admin/medical-vaccine" class="nav-item nav-sub-item">
              <span class="nav-icon">💉</span><span class="nav-label">疫苗管理</span>
            </router-link>
            <router-link to="/admin/medical-deworming" class="nav-item nav-sub-item">
              <span class="nav-icon">🐛</span><span class="nav-label">驱虫管理</span>
            </router-link>
          </div>
        </div>

        <!-- Finance Group -->
        <div class="nav-group" :class="{ expanded: financeExpanded }">
          <div class="nav-group-header" @click="financeExpanded = !financeExpanded">
            <span class="nav-group-title">💰 财务管理</span>
            <span class="nav-group-arrow" :class="{ rotated: financeExpanded }">›</span>
          </div>
          <div class="nav-group-items" v-show="financeExpanded">
            <router-link to="/admin/wallet" class="nav-item nav-sub-item">
              <span class="nav-icon">💰</span><span class="nav-label">钱包管理</span>
            </router-link>
            <router-link to="/admin/wallet-record" class="nav-item nav-sub-item">
              <span class="nav-icon">💳</span><span class="nav-label">钱包流水</span>
            </router-link>
          </div>
        </div>

        <router-link to="/admin/health" class="nav-item">
          <span class="nav-icon">🩺</span><span class="nav-label">健康管理</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="user-card">
          <div class="user-avatar">{{ (user.username || 'A')[0] }}</div>
          <div class="user-info">
            <span class="user-name">{{ user.username || '管理员' }}</span>
            <span class="user-role">超级管理员</span>
          </div>
        </div>
      </div>
    </aside>
    <div class="admin-main">
      <header class="topbar glass">
        <div class="topbar-left">
          <button class="btn btn-ghost btn-icon hamburger" @click="sidebarOpen = !sidebarOpen" title="菜单">☰</button>
          <div class="topbar-breadcrumb">
            <div class="breadcrumb">
              <a @click.prevent="$router.push('/admin')">首页</a>
              <span class="separator">/</span>
              <span class="current">{{ currentPageTitle }}</span>
            </div>
            <h2 class="topbar-title">{{ currentPageTitle }}</h2>
          </div>
        </div>
        <div class="topbar-right">
          <button class="btn btn-ghost btn-icon" title="刷新" @click="$router.go(0)">🔄</button>
          <button class="btn btn-ghost btn-sm" @click="logout">
            <span>退出</span>
          </button>
        </div>
      </header>
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()
const user = computed(() => { try { return JSON.parse(localStorage.getItem('admin_user')) || {} } catch { return {} } })

const sidebarOpen = ref(false)
const medicalExpanded = ref(true)
const financeExpanded = ref(true)

const pageTitles = {
  '/admin/course': '课程管理',
  '/admin/customer': '客户维护',
  '/admin/pet': '宠物管理',
  '/admin/certificate': '毕业证书管理',
  '/admin/room': '寄宿管理',
  '/admin/order': '订单管理',
  '/admin/coupon': '优惠管理',
  '/admin/health': '健康管理',
  '/admin/medical': '医疗服务',
  '/admin/medical-dept': '科室管理',
  '/admin/medical-doctor': '医生管理',
  '/admin/medical-order': '医疗订单',
  '/admin/medical-record': '病历管理',
  '/admin/medical-vaccine': '疫苗管理',
  '/admin/medical-deworming': '驱虫管理',
  '/admin/wallet': '钱包管理',
  '/admin/wallet-record': '钱包流水'
}

const currentPageTitle = computed(() => pageTitles[route.path] || '管理后台')

function logout() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_user')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}

/* ========== Sidebar Overlay (mobile) ========== */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: var(--bg-overlay);
  z-index: 90;
  animation: fadeIn var(--transition-fast) ease;
}

/* ========== Sidebar ========== */
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-light);
  padding: var(--space-4) var(--space-3);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-2);
  margin-bottom: var(--space-5);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.sidebar-brand:hover { background: var(--bg-hover); }

.brand-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  background: var(--color-primary-bg);
  border-radius: var(--radius-md);
}

.brand-text { display: flex; flex-direction: column; }
.brand-name { font-size: var(--font-size-md); font-weight: var(--font-weight-bold); color: var(--text-title); line-height: 1.2; }
.brand-sub { font-size: var(--font-size-xs); color: var(--text-muted); }

/* ========== Sidebar Nav ========== */
.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 10px 12px;
  border-radius: var(--radius-md);
  color: var(--text-body);
  text-decoration: none;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
}

.nav-item:hover {
  background: var(--bg-hover);
  color: var(--text-title);
}

.nav-item.router-link-active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

/* Sub-item indentation */
.nav-sub-item {
  padding-left: 24px;
  font-size: var(--font-size-sm);
}

.nav-icon { font-size: var(--font-size-md); width: 24px; text-align: center; flex-shrink: 0; }
.nav-label { white-space: nowrap; }

/* ========== Nav Group (collapsible) ========== */
.nav-group {
  display: flex;
  flex-direction: column;
}

.nav-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
  user-select: none;
}

.nav-group-header:hover {
  background: var(--bg-hover);
}

.nav-group-title {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.nav-group-arrow {
  font-size: var(--font-size-md);
  color: var(--text-muted);
  transition: transform var(--transition-fast);
  line-height: 1;
}

.nav-group-arrow.rotated {
  transform: rotate(90deg);
}

.nav-group-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* ========== Sidebar Footer ========== */
.sidebar-footer {
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.user-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2);
  border-radius: var(--radius-md);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.user-info { display: flex; flex-direction: column; }
.user-name { font-size: var(--font-size-sm); font-weight: var(--font-weight-semibold); color: var(--text-title); line-height: 1.2; }
.user-role { font-size: var(--font-size-xs); color: var(--text-muted); }

/* ========== Main Area ========== */
.admin-main {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* ========== Topbar ========== */
.topbar {
  height: var(--topbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 1px solid var(--border-light);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.hamburger {
  display: none;
  font-size: 18px;
}

.topbar-breadcrumb {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.topbar-breadcrumb .breadcrumb {
  margin-bottom: 0;
  font-size: var(--font-size-xs);
}

.topbar-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  letter-spacing: -0.01em;
  line-height: 1.3;
}

.topbar-right { display: flex; align-items: center; gap: var(--space-2); }

/* ========== Content ========== */
.admin-content {
  flex: 1;
  padding: var(--space-5);
  max-width: var(--page-max-width);
  width: 100%;
  margin: 0 auto;
}

/* ========== Responsive ========== */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform var(--transition-base);
    width: 260px;
  }
  .sidebar.open { transform: translateX(0); }
  .admin-main { margin-left: 0; }
  .admin-content { padding: var(--space-3); }
  .hamburger { display: inline-flex; }
}
</style>
