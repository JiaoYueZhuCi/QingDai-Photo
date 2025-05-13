<template>
  <div class="control-manage">
    
    <!-- 使用操作卡片组件 -->
    <ControlActionCards />
  </div>
</template>

<script setup lang="ts">
import ControlActionCards from '@/views/manage/control-manage/control-action-cards/ControlActionCards.vue';
</script>

<style scoped>
.control-manage {
  padding: 20px;
  background-color: var(--qd-color-bg-dark);
}
</style>