<template>
    <div class="phopoDataContainer">
      <StatisticsCards :stats="stats" />
      <VisualizationCharts ref="chartsRef" :stats="stats" />
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, nextTick } from 'vue';
  import { getPhotoDashboardStats } from '@/api/photo';
  import StatisticsCards from './statistics-cards/StatisticsCards.vue';
  import VisualizationCharts from './visualization-charts/VisualizationCharts.vue';
  
  const stats = ref<any>({
    typeStats: {},
    changeStats: {},
    subjectStats: {},
    cameraStats: [],
    lensStats: [],
    isoStats: [],
    shutterStats: [],
    apertureStats: [],
    monthStats: {},
    yearStats: {},
    focalLengthStats: []
  });
  
  const chartsRef = ref<InstanceType<typeof VisualizationCharts> | null>(null);
  
  // 获取照片数据统计
  const fetchPhotoStats = async () => {
    try {
      const response = await getPhotoDashboardStats();
      stats.value = response;
    } catch (error) {
      console.error('获取照片统计数据失败', error);
    }
  };
  
  onMounted(() => {
    // 在window.onload回调中初始化,确保DOM完全加载
    if (document.readyState === 'complete') {
      fetchPhotoStats();
    } else {
      window.addEventListener('load', fetchPhotoStats);
    }
  
    return () => {
      window.removeEventListener('load', fetchPhotoStats);
    };
  });
  </script>
  
  <style scoped>
  .phopoDataContainer {
    padding: 8px;
    background-color: var(--qd-color-bg-dark);
  }
  
  /* 响应式调整 */
  @media (max-width: 768px) {
    .phopoDataContainer {
      padding: 4px;
    }
  }
  </style>