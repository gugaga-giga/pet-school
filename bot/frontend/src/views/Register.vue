<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
    </div>
    <div class="register-card">
      <div class="register-header">
        <el-icon :size="40" color="#409eff"><ChatDotRound /></el-icon>
        <h2>注册账号</h2>
        <p>创建您的智能售前客服系统账号</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？
        <router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      email: form.email,
      password: form.password
    })
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch (err: any) {
    ElMessage.error(err.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  position: relative;
  overflow: hidden;
}

.register-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;

  .bg-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    background: #fff;
  }

  .circle-1 {
    width: 500px;
    height: 500px;
    top: -150px;
    left: -100px;
  }

  .circle-2 {
    width: 350px;
    height: 350px;
    bottom: -100px;
    right: -80px;
  }
}

.register-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  z-index: 1;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    margin: 12px 0 8px;
    font-size: 22px;
    color: #303133;
  }

  p {
    font-size: 13px;
    color: #909399;
  }
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #909399;

  .link {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
