<template>
  <router-view />
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

if (appStore.isDark) {
  document.documentElement.classList.add('dark')
}
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
}
</style>
