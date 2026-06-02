<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>
    <div class="login-card glass">
      <div class="login-header">
        <div class="login-logo">🐾</div>
        <h1 class="login-title">宠物培训学校</h1>
        <p class="login-subtitle">{{ isRegister ? '创建您的账号' : '欢迎回来' }}</p>
      </div>
      <div class="login-form">
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" placeholder="请输入手机号" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="submit" />
        </div>
        <div v-if="isRegister" class="form-group">
          <label>用户名</label>
          <input v-model="form.username" placeholder="请输入用户名" />
        </div>
        <button class="btn btn-primary btn-lg login-btn" @click="submit" :disabled="loading">
          <span v-if="loading" class="loading-dot">●</span>
          {{ loading ? '请求中...' : (isRegister ? '注册' : '登录') }}
        </button>
        <p class="login-switch" @click="isRegister = !isRegister">{{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}</p>
        <div v-if="msg" :class="['alert', msgType==='success'?'alert-success':'alert-error']">{{ msg }}</div>
      </div>
      <div class="login-demo">
        <p>演示账号</p>
        <div class="demo-accounts">
          <span class="demo-chip" @click="fillDemo('13900001111','123456')">客户</span>
          <span class="demo-chip" @click="fillDemo('13800000000','123456')">管理员</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
const router = useRouter()
const isRegister = ref(false)
const loading = ref(false)
const form = ref({ phone: '', password: '', username: '' })
const msg = ref(''), msgType = ref('success')

function fillDemo(phone, pwd) {
  form.value.phone = phone
  form.value.password = pwd
}

async function submit() {
  msg.value = ''
  if (!form.value.phone || !form.value.password) { msg.value = '手机号和密码不能为空'; msgType.value = 'error'; return }
  loading.value = true
  try {
    let res
    if (isRegister.value) {
      if (!form.value.username) { msg.value = '用户名不能为空'; msgType.value = 'error'; loading.value = false; return }
      res = await authApi.register(form.value)
    } else {
      res = await authApi.login({ phone: form.value.phone, password: form.value.password })
    }
    if (res.code === 200) {
      const data = res.data
      const isAdmin = data.role >= 2
      if (isAdmin) {
        localStorage.setItem('admin_token', data.token)
        localStorage.setItem('admin_user', JSON.stringify(data))
      } else {
        localStorage.setItem('client_token', data.token)
        localStorage.setItem('client_user', JSON.stringify(data))
      }
      msg.value = '登录成功，正在跳转...'; msgType.value = 'success'
      setTimeout(() => { router.push(isAdmin ? '/admin' : '/client') }, 500)
    } else {
      msg.value = res.message || '操作失败'; msgType.value = 'error'
    }
  } catch (e) {
    if (e.response) {
      const status = e.response.status
      if (status === 401) msg.value = '认证失败，请检查账号密码'
      else if (status === 500) msg.value = '服务器内部错误'
      else if (status === 502 || status === 504) msg.value = '无法连接到服务器，请确认后端已启动'
      else msg.value = '请求失败(' + status + ')'
    } else if (e.code === 'ERR_NETWORK') {
      msg.value = '网络错误，请确认后端已启动(端口8080)'
    } else {
      msg.value = '请求失败: ' + (e.message || '未知错误')
    }
    msgType.value = 'error'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4F7CFF 0%, #7B9EFF 40%, #FFB86B 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  background: #fff;
}

.bg-circle-1 { width: 600px; height: 600px; top: -200px; right: -100px; }
.bg-circle-2 { width: 400px; height: 400px; bottom: -100px; left: -50px; }
.bg-circle-3 { width: 200px; height: 200px; top: 50%; left: 50%; transform: translate(-50%, -50%); }

.login-card {
  width: 420px;
  max-width: 92vw;
  border-radius: var(--radius-xl);
  padding: var(--space-7) var(--space-6);
  position: relative;
  z-index: 1;
  box-shadow: var(--shadow-xl);
}

.login-header { text-align: center; margin-bottom: var(--space-5); }
.login-logo { font-size: 48px; margin-bottom: var(--space-2); }
.login-title { font-size: var(--font-size-2xl); font-weight: var(--font-weight-bold); color: var(--text-title); letter-spacing: -0.02em; }
.login-subtitle { font-size: var(--font-size-md); color: var(--text-muted); margin-top: var(--space-1); }

.login-btn { width: 100%; margin-top: var(--space-3); font-size: var(--font-size-md); }

.login-switch {
  text-align: center;
  margin-top: var(--space-3);
  color: var(--color-primary);
  cursor: pointer;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  transition: color var(--transition-fast);
}

.login-switch:hover { color: var(--color-primary-dark); }

.login-demo {
  text-align: center;
  margin-top: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.login-demo p { font-size: var(--font-size-xs); color: var(--text-muted); margin-bottom: var(--space-2); }

.demo-accounts { display: flex; gap: var(--space-2); justify-content: center; }

.demo-chip {
  padding: 4px 16px;
  border-radius: var(--radius-full);
  background: var(--bg-hover);
  font-size: var(--font-size-xs);
  color: var(--text-body);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-weight: var(--font-weight-medium);
}

.demo-chip:hover {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.loading-dot { animation: pulse 1s ease infinite; margin-right: 4px; }
</style>
