<template>
  <div class="client-layout">
    <nav class="navbar glass">
      <div class="nav-brand" @click="$router.push('/client')">
        <span class="brand-icon">🐾</span>
        <span class="brand-name">宠物培训学校</span>
      </div>

      <!-- Desktop nav -->
      <div class="nav-links">
        <router-link to="/client" class="nav-link" :class="{ active: $route.path === '/client' }">首页</router-link>
        <router-link to="/client/pet" class="nav-link">我的宠物</router-link>

        <div class="nav-dropdown" @mouseenter="openDropdown('course')" @mouseleave="closeDropdown('course')">
          <router-link to="/client/course" class="nav-link nav-link--parent" :class="{ active: isCourseActive }">
            课程训练
            <svg class="dropdown-arrow" viewBox="0 0 12 12" width="12" height="12"><path d="M3 5l3 3 3-3" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </router-link>
          <div class="dropdown-menu" v-show="dropdownOpen.course">
            <router-link to="/client/course" class="dropdown-item">课程选课</router-link>
            <router-link to="/client/video" class="dropdown-item">训练视频</router-link>
            <router-link to="/client/live" class="dropdown-item">远程直播</router-link>
          </div>
        </div>

        <router-link to="/client/room" class="nav-link">寄宿服务</router-link>

        <div class="nav-dropdown" @mouseenter="openDropdown('health')" @mouseleave="closeDropdown('health')">
          <router-link to="/client/medical" class="nav-link nav-link--parent" :class="{ active: isHealthActive }">
            医疗健康
            <svg class="dropdown-arrow" viewBox="0 0 12 12" width="12" height="12"><path d="M3 5l3 3 3-3" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </router-link>
          <div class="dropdown-menu" v-show="dropdownOpen.health">
            <router-link to="/client/medical" class="dropdown-item">医疗服务</router-link>
            <router-link to="/client/health" class="dropdown-item">宠物健康</router-link>
            <router-link to="/client/vaccine" class="dropdown-item">疫苗提醒</router-link>
          </div>
        </div>

        <router-link to="/client/order" class="nav-link">订单中心</router-link>

        <div class="nav-dropdown" @mouseenter="openDropdown('more')" @mouseleave="closeDropdown('more')">
          <button class="nav-link nav-link--parent" :class="{ active: isMoreActive }">
            更多
            <svg class="dropdown-arrow" viewBox="0 0 12 12" width="12" height="12"><path d="M3 5l3 3 3-3" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </button>
          <div class="dropdown-menu" v-show="dropdownOpen.more">
            <router-link to="/client/coupon-center" class="dropdown-item">优惠券</router-link>
            <router-link to="/client/wallet" class="dropdown-item">我的钱包</router-link>
            <router-link to="/client/certificate" class="dropdown-item">毕业证书</router-link>
          </div>
        </div>
      </div>

      <div class="nav-actions">
        <div class="user-chip">
          <div class="user-avatar">{{ (user.username || 'U')[0] }}</div>
          <span class="user-name">{{ user.username }}</span>
        </div>
        <button class="btn btn-ghost btn-sm" @click="logout">退出</button>
        <button class="hamburger-btn" @click="toggleMobileMenu" :class="{ 'is-open': mobileMenuOpen }">
          <span></span><span></span><span></span>
        </button>
      </div>
    </nav>

    <!-- Mobile menu overlay -->
    <transition name="mobile-overlay">
      <div class="mobile-overlay" v-if="mobileMenuOpen" @click="closeMobileMenu"></div>
    </transition>

    <!-- Mobile nav drawer -->
    <transition name="mobile-drawer">
      <div class="mobile-nav" v-if="mobileMenuOpen">
        <div class="mobile-nav-header">
          <div class="user-chip">
            <div class="user-avatar">{{ (user.username || 'U')[0] }}</div>
            <span class="user-name">{{ user.username }}</span>
          </div>
        </div>
        <div class="mobile-nav-body">
          <router-link to="/client" class="mobile-nav-link" @click="closeMobileMenu">首页</router-link>
          <router-link to="/client/pet" class="mobile-nav-link" @click="closeMobileMenu">我的宠物</router-link>

          <div class="mobile-nav-group">
            <div class="mobile-nav-group-title">课程训练</div>
            <router-link to="/client/course" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">课程选课</router-link>
            <router-link to="/client/video" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">训练视频</router-link>
            <router-link to="/client/live" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">远程直播</router-link>
          </div>

          <router-link to="/client/room" class="mobile-nav-link" @click="closeMobileMenu">寄宿服务</router-link>

          <div class="mobile-nav-group">
            <div class="mobile-nav-group-title">医疗健康</div>
            <router-link to="/client/medical" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">医疗服务</router-link>
            <router-link to="/client/health" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">宠物健康</router-link>
            <router-link to="/client/vaccine" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">疫苗提醒</router-link>
          </div>

          <router-link to="/client/order" class="mobile-nav-link" @click="closeMobileMenu">订单中心</router-link>

          <div class="mobile-nav-group">
            <div class="mobile-nav-group-title">更多</div>
            <router-link to="/client/coupon-center" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">优惠券</router-link>
            <router-link to="/client/wallet" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">我的钱包</router-link>
            <router-link to="/client/certificate" class="mobile-nav-link mobile-nav-sub" @click="closeMobileMenu">毕业证书</router-link>
          </div>
        </div>
        <div class="mobile-nav-footer">
          <button class="btn btn-ghost" @click="logout" style="width:100%">退出登录</button>
        </div>
      </div>
    </transition>

    <main class="client-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()
const user = computed(() => { try { return JSON.parse(localStorage.getItem('client_user')) || {} } catch { return {} } })

const mobileMenuOpen = ref(false)
const dropdownOpen = reactive({ course: false, health: false, more: false })

function openDropdown(key) { dropdownOpen[key] = true }
function closeDropdown(key) { dropdownOpen[key] = false }
function toggleMobileMenu() { mobileMenuOpen.value = !mobileMenuOpen.value }
function closeMobileMenu() { mobileMenuOpen.value = false }

const isCourseActive = computed(() => ['/client/course', '/client/video', '/client/live'].includes(route.path))
const isHealthActive = computed(() => ['/client/medical', '/client/health', '/client/vaccine', '/client/medical-order', '/client/medical-record'].includes(route.path))
const isMoreActive = computed(() => ['/client/coupon-center', '/client/wallet', '/client/certificate', '/client/my-coupon'].includes(route.path))

function logout() {
  localStorage.removeItem('client_token')
  localStorage.removeItem('client_user')
  router.push('/login')
}
</script>

<style scoped>
.client-layout { min-height: 100vh; background: var(--bg-page); }

.navbar {
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--border-light);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  flex-shrink: 0;
}

.brand-icon { font-size: 24px; }
.brand-name { font-size: var(--font-size-lg); font-weight: var(--font-weight-bold); color: var(--text-title); letter-spacing: -0.01em; }

.nav-links {
  display: flex;
  align-items: center;
  gap: 2px;
}

.nav-link {
  padding: 8px 16px;
  border-radius: var(--radius-full);
  color: var(--text-body);
  text-decoration: none;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-family);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.nav-link:hover {
  color: var(--text-title);
  background: var(--bg-hover);
}

.nav-link.router-link-active,
.nav-link.active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
  font-weight: var(--font-weight-semibold);
}

.dropdown-arrow {
  transition: transform var(--transition-fast);
  flex-shrink: 0;
}

.nav-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* Dropdown */
.nav-dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  min-width: 160px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-1) 0;
  z-index: 200;
  animation: dropdownFadeIn var(--transition-fast) ease;
}

@keyframes dropdownFadeIn {
  from { opacity: 0; transform: translateX(-50%) translateY(-4px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}

.dropdown-item {
  display: block;
  padding: 10px var(--space-4);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-body);
  text-decoration: none;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.dropdown-item:hover {
  background: var(--bg-hover);
  color: var(--text-title);
}

.dropdown-item.router-link-active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
  font-weight: var(--font-weight-semibold);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 4px 12px 4px 4px;
  background: var(--bg-hover);
  border-radius: var(--radius-full);
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
}

.user-name { font-size: var(--font-size-sm); font-weight: var(--font-weight-medium); color: var(--text-title); }

/* Hamburger */
.hamburger-btn {
  display: none;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  width: 36px;
  height: 36px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.hamburger-btn:hover {
  background: var(--bg-hover);
}

.hamburger-btn span {
  display: block;
  width: 18px;
  height: 2px;
  background: var(--text-body);
  border-radius: 1px;
  transition: all var(--transition-fast);
}

.hamburger-btn.is-open span:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}
.hamburger-btn.is-open span:nth-child(2) {
  opacity: 0;
}
.hamburger-btn.is-open span:nth-child(3) {
  transform: rotate(-45deg) translate(5px, -5px);
}

/* Mobile overlay */
.mobile-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: var(--bg-overlay);
  z-index: 150;
}

.mobile-overlay-enter-active,
.mobile-overlay-leave-active {
  transition: opacity var(--transition-base);
}
.mobile-overlay-enter-from,
.mobile-overlay-leave-to {
  opacity: 0;
}

/* Mobile drawer */
.mobile-nav {
  position: fixed;
  top: 0;
  right: 0;
  width: 280px;
  max-width: 85vw;
  height: 100vh;
  background: var(--bg-card);
  z-index: 200;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-float);
}

.mobile-drawer-enter-active,
.mobile-drawer-leave-active {
  transition: transform var(--transition-slow);
}
.mobile-drawer-enter-from,
.mobile-drawer-leave-to {
  transform: translateX(100%);
}

.mobile-nav-header {
  padding: var(--space-4) var(--space-4) var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.mobile-nav-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2) 0;
}

.mobile-nav-link {
  display: block;
  padding: 12px var(--space-4);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-body);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.mobile-nav-sub {
  padding-left: var(--space-6);
  font-size: var(--font-size-sm);
}

.mobile-nav-group {
  margin-bottom: var(--space-1);
}

.mobile-nav-group-title {
  padding: 10px var(--space-4) 4px;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.mobile-nav-footer {
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--border-light);
}

.client-content {
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: var(--space-5);
}

/* Page transition */
.page-enter-active,
.page-leave-active {
  transition: opacity var(--transition-fast), transform var(--transition-fast);
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 768px) {
  .navbar { padding: 0 var(--space-3); }
  .nav-links { display: none; }
  .user-chip { display: none; }
  .hamburger-btn { display: flex; }
  .client-content { padding: var(--space-3); }
  .brand-name { display: none; }
}
</style>
