import { get, put } from './request'
import type {
  DashboardStats,
  TrendData,
  IntentDistribution,
  QALog,
  QALogParams,
  SystemSettings,
  SystemSettingsUpdate,
  PageData
} from '@/types/api'

export function getDashboard() {
  return get<DashboardStats>('/admin/dashboard')
}

export function getTrendData(params?: { days?: number }) {
  return get<TrendData>('/admin/dashboard/chat-trend', params)
}

export function getIntentDistribution() {
  return get<IntentDistribution[]>('/admin/dashboard/intent-distribution')
}

export function getQALogs(params?: QALogParams) {
  return get<PageData<QALog>>('/logs/qa', params)
}

export function getSystemSettings() {
  return get<SystemSettings>('/admin/system-configs')
}

export function updateSystemSettings(data: SystemSettingsUpdate) {
  return put<SystemSettings>('/admin/settings', data)
}
