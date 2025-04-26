<template>
  <div class="timelineContainer">
    <el-empty v-if="!timelines || timelines.length === 0" description="暂无时间轴数据"></el-empty>
    <el-timeline v-else>
      <el-timeline-item v-for="(timelineItem, index) in timelines" :key="index" :timestamp="timelineItem.time" placement="top">
        <el-card class="timeline-card">
          <h4 class="timeline-title">{{ timelineItem.title }}</h4>
          <p class="content-cell">{{ timelineItem.text }}</p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { TimelineItem } from '@/types';
import { getAllTimelines } from '@/api/timeline';
import { ElLoading } from 'element-plus';

const timelines = ref<TimelineItem[]>([]);
const loading = ref(false);

const fetchTimelines = async () => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '加载时间线中...',
    background: 'rgba(0, 0, 0, 0.8)',
    fullscreen: true
  });
  
  try {
    loading.value = true;
    timelines.value = await getAllTimelines();
  } catch (error) {
    console.error('获取时间线失败:', error);
    timelines.value = [];
  } finally {
    loading.value = false;
    loadingInstance.close();
  }
};

onMounted(() => {
  fetchTimelines();
});
</script>

<style lang="css" scoped>
.el-timeline{
    padding: 0;
  }
.timelineContainer {
  background-color: var(--qd-color-bg-dark);
  padding: 10px 20px 0 10px;
  min-height: calc(100vh - 200px);
}

.timeline-card {
  background-color: var(--qd-color-bg);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.4);
  border: 1px solid var(--qd-color-border);
  border-radius: 4px;
}

.timeline-title {
  color: var(--qd-color-text-primary);
  margin: 0 0 10px 0;
}

.content-cell {
  word-break: break-word;
  color: var(--qd-color-text-secondary);
  margin: 0;
}

/* 自定义加载样式 */
:deep(.el-loading-mask) {
  z-index: 9999;
}

:deep(.el-loading-spinner .el-loading-text) {
  color: #fff;
  font-size: 16px;
  margin-top: 10px;
}

:deep(.el-loading-spinner .path) {
  stroke: #fff;
}

@media (max-width: 768px) {
  .timelineContainer {
    padding: 10px 10px 0 5px;
  }
}
</style>