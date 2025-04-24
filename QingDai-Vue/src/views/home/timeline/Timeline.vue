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

const timelines = ref<TimelineItem[]>([]);

const fetchTimelines = async () => {
  try {
    timelines.value = await getAllTimelines();
  } catch (error) {
    console.error('获取时间线失败:', error);
    timelines.value = [];
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
  background-color: var(--qd-color-primary-dark-10);
  padding: 10px 20px 0 10px;
}

.timeline-card {
  background-color: var(--qd-color-primary-dark-6);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.4);
  border: 1px solid var(--qd-color-primary-light-3);
  border-radius: 4px;
}

.timeline-title {
  color: var(--qd-color-primary-light-9);
  margin: 0 0 10px 0;
}

.content-cell {
  word-break: break-word;
  color: var(--qd-color-primary-light-7);
  margin: 0;
}

:deep(.el-timeline-item__timestamp) {
  color: var(--qd-color-primary-light-6);
}

:deep(.el-timeline-item__node) {
  background-color: var(--qd-color-primary-light-3);
}

:deep(.el-timeline-item__tail) {
  border-left-color: var(--qd-color-primary-light-3);
}

:deep(.el-card__body) {
  padding: 15px;
}

:deep(.el-empty__description) {
  color: var(--qd-color-primary-light-6);
}

@media (max-width: 768px) {
  .timelineContainer {
    padding: 10px 10px 0 5px;
  }
  
}
</style>