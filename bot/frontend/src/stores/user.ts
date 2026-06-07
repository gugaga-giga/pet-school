import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getProfile, refreshToken as refreshTokenApi } from '@/api/auth'
import type { UserInfo, LoginRequest, RegisterRequest, TokenResponse } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('access_token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refresh_token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.roles?.includes('admin') || false)
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  function setTokens(data: TokenResponse) {
    token.value = data.access_token
    refreshToken.value = data.refresh_token
    localStorage.setItem('access_token', data.access_token)
    localStorage.setItem('refresh_token', data.refresh_token)
  }

  function clearTokens() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  }

  async function login(data: LoginRequest) {
    const res = await loginApi(data)
    setTokens(res.data)
    await fetchProfile()
    return res
  }

  async function register(data: RegisterRequest) {
    const res = await registerApi(data)
    setTokens(res.data)
    await fetchProfile()
    return res
  }

  async function fetchProfile() {
    try {
      const res = await getProfile()
      userInfo.value = res.data
    } catch {
      clearTokens()
    }
  }

  async function refreshAccessToken() {
    if (!refreshToken.value) return
    try {
      const res = await refreshTokenApi(refreshToken.value)
      setTokens(res.data)
    } catch {
      clearTokens()
    }
  }

  function logout() {
    clearTokens()
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    nickname,
    login,
    register,
    fetchProfile,
    refreshAccessToken,
    logout
  }
})
