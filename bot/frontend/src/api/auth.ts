import { post, get } from './request'
import type { LoginRequest, RegisterRequest, TokenResponse, UserInfo } from '@/types/api'

export function login(data: LoginRequest) {
  return post<TokenResponse>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return post<TokenResponse>('/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return post<TokenResponse>('/auth/refresh', { refresh_token: refreshToken })
}

export function getProfile() {
  return get<UserInfo>('/auth/profile')
}

export function logout() {
  return post('/auth/logout')
}
