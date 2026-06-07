import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const isDark = ref<boolean>(localStorage.getItem('theme') === 'dark')
  const sidebarCollapsed = ref<boolean>(false)
  const globalLoading = ref<boolean>(false)

  function toggleDark() {
    isDark.value = !isDark.value
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setGlobalLoading(val: boolean) {
    globalLoading.value = val
  }

  return {
    isDark,
    sidebarCollapsed,
    globalLoading,
    toggleDark,
    toggleSidebar,
    setGlobalLoading
  }
})
