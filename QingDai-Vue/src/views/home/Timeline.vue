<template>
  <div class="timelineContainer">
    <el-empty v-if="!timelines || timelines.length === 0" description="暂无时间轴数据"></el-empty>
    <el-timeline v-else>
      <el-timeline-item v-for="(timelineItem, index) in timelines" :key="index" :timestamp="timelineItem.time" placement="top">
        <el-card>
          <h4>{{ timelineItem.title }}</h4>
          <p>{{ timelineItem.text }}</p>
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
.timelineContainer {
  padding: 10px;
}
</style>