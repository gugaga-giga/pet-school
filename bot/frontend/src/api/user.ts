import { get, put, del } from './request'
import type { UserInfo, UserListParams, UserUpdateRequest, PageData } from '@/types/api'

export function getUserList(params?: UserListParams) {
  return get<PageData<UserInfo>>('/users', params)
}

export function getUser(id: number) {
  return get<UserInfo>(`/users/${id}`)
}

export function updateUser(id: number, data: UserUpdateRequest) {
  return put<UserInfo>(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return del(`/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return put(`/users/${id}/status`, { status })
}

export function updateUserRoles(id: number, roles: string[]) {
  return put(`/users/${id}/roles`, { roles })
}
