<template>
  <el-container class="main-layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <el-icon :size="28"><ChatDotRound /></el-icon>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">智能售前客服</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
        background-color="transparent"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>智能对话</template>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><Collection /></el-icon>
          <template #title>知识库管理</template>
        </el-menu-item>
        <el-menu-item index="/documents">
          <el-icon><Document /></el-icon>
          <template #title>文档管理</template>
        </el-menu-item>
        <el-menu-item index="/pets">
          <el-icon><Guide /></el-icon>
          <template #title>宠物管理</template>
        </el-menu-item>
        <el-sub-menu v-if="userStore.isAdmin" index="admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><List /></el-icon>
            <template #title>问答日志</template>
          </el-menu-item>
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentMeta?.title">{{ currentMeta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-switch
            v-model="isDark"
            :active-action-icon="Moon"
            :inactive-action-icon="Sunny"
            @change="appStore.toggleDark()"
            style="--el-switch-on-color: #2c2c2c; --el-switch-off-color: #dcdfe6"
          />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { Moon, Sunny } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)
const currentMeta = computed(() => route.meta as { title?: string })
const isDark = ref(appStore.isDark)

watch(
  () => appStore.isDark,
  (val) => {
    isDark.value = val
  }
)

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-light);
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border-bottom: 1px solid var(--el-border-color-light);
    color: var(--el-color-primary);
    font-size: 18px;
    font-weight: 700;
    padding: 0 16px;
    white-space: nowrap;
    overflow: hidden;

    .logo-text {
      font-size: 16px;
    }
  }

  .sidebar-menu {
    border-right: none;
    height: calc(100vh - 60px);
    overflow-y: auto;
  }
}

.main-container {
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 0 20px;
  height: 60px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: var(--el-text-color-primary);

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: var(--el-text-color-primary);
      }
    }
  }
}

.main-content {
  background: var(--el-bg-color-page);
  overflow-y: auto;
  padding: 20px;
}
</style>
