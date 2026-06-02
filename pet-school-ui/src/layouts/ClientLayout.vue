<template>
  <div class="client-layout">
    <nav class="navbar glass">
      <div class="nav-brand" @click="$router.push('/client')">
        <span class="brand-icon">🐾</span>
        <span class="brand-name">宠物培训学校</span>
      </div>
      <div class="nav-links">
        <router-link to="/client" class="nav-link" :class="{ active: $route.path === '/client' }">首页</router-link>
        <router-link to="/client/course" class="nav-link">课程选课</router-link>
        <router-link to="/client/room" class="nav-link">寄宿服务</router-link>
        <router-link to="/client/video" class="nav-link">训练视频</router-link>
        <router-link to="/client/live" class="nav-link">远程直播</router-link>
        <router-link to="/client/order" class="nav-link">我的订单</router-link>
        <router-link to="/client/coupon-center" class="nav-link">优惠券</router-link>
        <router-link to="/client/health" class="nav-link">宠物健康</router-link>
        <router-link to="/client/medical" class="nav-link">🏥 医疗服务</router-link>
        <router-link to="/client/wallet" class="nav-link">💰 我的钱包</router-link>
        <router-link to="/client/certificate" class="nav-link">毕业证书</router-link>
      </div>
      <div class="nav-actions">
        <div class="user-chip">
          <div class="user-avatar">{{ (user.username || 'U')[0] }}</div>
          <span class="user-name">{{ user.username }}</span>
        </div>
        <button class="btn btn-ghost btn-sm" @click="logout">退出</button>
      </div>
    </nav>
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const user = computed(() => { try { return JSON.parse(localStorage.getItem('client_user')) || {} } catch { return {} } })

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

.client-content {
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: var(--space-5);
}

@media (max-width: 768px) {
  .navbar { padding: 0 var(--space-3); }
  .nav-links { display: none; }
  .client-content { padding: var(--space-3); }
  .brand-name { display: none; }
}
</style>
