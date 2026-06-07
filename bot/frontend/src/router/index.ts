import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue'),
        meta: { title: '智能对话', icon: 'ChatDotRound' }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/index.vue'),
        meta: { title: '知识库管理', icon: 'Collection' }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import('@/views/document/index.vue'),
        meta: { title: '文档管理', icon: 'Document' }
      },
      {
        path: 'pets',
        name: 'Pets',
        component: () => import('@/views/pet/index.vue'),
        meta: { title: '宠物管理', icon: 'Guide' }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAdmin: true }
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/logs.vue'),
        meta: { title: '问答日志', icon: 'List', requiresAdmin: true }
      },
      {
        path: 'admin/settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/settings.vue'),
        meta: { title: '系统设置', icon: 'Setting', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Dashboard' })
    return
  }

  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Dashboard' })
    return
  }

  if (userStore.isLoggedIn && !userStore.userInfo) {
    await userStore.fetchProfile()
  }

  next()
})

export default router
