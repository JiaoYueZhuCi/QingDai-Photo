<template>
  <div class="timelineContainer">
    <el-timeline>
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
import axios from 'axios';
import type { Timeline } from '@/types';

const timelines = ref<Timeline[]>([]);

const fetchTimelines = async () => {
  try {
    const response = await axios.get('/api/QingDai/timeline/getAllTimelines');
    timelines.value = response.data;
  } catch (error) {
    console.error('获取时间线失败:', error);
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