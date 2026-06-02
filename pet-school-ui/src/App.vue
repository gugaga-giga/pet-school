<template>
  <div class="app">
    <router-view v-slot="{ Component }">
      <transition name="page" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<style>
.page-enter-active { animation: fadeInUp 0.3s ease; }
.page-leave-active { animation: fadeIn 0.15s ease reverse; }
</style>
