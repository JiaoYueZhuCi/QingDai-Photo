<template>
  <div :class="['app-container', { 'dark-theme': isDarkTheme }]">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useThemeStore } from './src/stores/theme';

const themeStore = useThemeStore();

// 计算是否为暗色主题
const isDarkTheme = computed(() => {
  return themeStore.theme === 'dark';
});
</script>

<style>
.app-container {
  min-height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.dark-theme {
  background-color: var(--qd-color-primary-light-10);
  color: var(--qd-color-primary-light-3);
}
</style> 