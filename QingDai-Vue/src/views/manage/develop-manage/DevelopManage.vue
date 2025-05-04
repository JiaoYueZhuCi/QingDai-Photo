<template>
  <div class="develop-manage">
    <!-- 使用系统信息栏组件 -->
    <SystemInfoBar />
    
    <!-- 使用操作卡片组件 -->
    <DevelopActionCards />
  </div>
</template>

<script setup lang="ts">
import SystemInfoBar from './SystemInfoBar.vue';
import DevelopActionCards from './DevelopActionCards.vue';
</script>

<style scoped>
.develop-manage {
  padding: 20px;
  background-color: var(--qd-color-bg-dark);
}
</style>