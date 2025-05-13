<template>
  <div class="develop-manage">
    <!-- 使用系统信息栏组件 -->
    <SystemInfoBar />
    <!-- 使用开发操作卡片组件 -->
    <DevelopActionCards />
  </div>
</template>

<script setup lang="ts">
import SystemInfoBar from '@/views/manage/develop-manage/system-info-bar/SystemInfoBar.vue';
import DevelopActionCards from '@/views/manage/develop-manage/develop-action-cards/DevelopActionCards.vue';
</script>

<style scoped>
.develop-manage {
  padding: 20px;
  background-color: var(--qd-color-bg-dark);
}
</style> 