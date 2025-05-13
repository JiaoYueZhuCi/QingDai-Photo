<template>
  <div class="timelineContainer">
    <!-- 空状态显示 - 仅在数据加载完成且为空时显示 -->
    <el-empty v-if="!loading && (!timelines || timelines.length === 0)" description="暂无时间轴数据"></el-empty>
    
    <!-- 时间轴 - 仅在有数据时显示 -->
    <el-timeline v-if="timelines && timelines.length > 0">
      <el-timeline-item v-for="(timelineItem, index) in timelines" :key="index" :timestamp="timelineItem.recordTime" placement="top">
        <el-card class="timeline-card">
          <h4 class="timeline-title">{{ timelineItem.title }}</h4>
          <p class="content-cell">{{ timelineItem.text }}</p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    
    <!-- 加载指示器 - 仅当有更多数据时显示 -->
      <load-more-indicator v-if="hasMore" text="加载更多时间轴..." />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import type { TimelineItem } from '@/types';
import { getTimelinesByPage } from '@/api/timeline';
import { ElMessage } from 'element-plus';
import { debounce } from 'lodash';
import { PhotoPagination, InfiniteScrollConfig } from '@/config/pagination';
import LoadMoreIndicator from '@/components/common/loading/LoadMoreIndicator.vue'

const timelines = ref<TimelineItem[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(PhotoPagination.TIMELINE_PAGE_SIZE);
const hasMore = ref(true);

const fetchTimelines = async () => {
  if (!hasMore.value || loading.value) return;
  
  try {
    loading.value = true;
    const response = await getTimelinesByPage({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    
    if (response && response.records && response.records.length > 0) {
      // 将新加载的数据添加到现有数据的末尾
      timelines.value = [...timelines.value, ...response.records];
      
      // 判断是否还有更多数据
      hasMore.value = currentPage.value < response.pages;
      
      // 更新当前页码，为下次加载做准备
      if (hasMore.value) {
        currentPage.value++;
      }
    } else {
      // 没有数据或者没有更多数据
      hasMore.value = false;
      if (timelines.value.length === 0) {
        timelines.value = [];
      }
    }
  } catch (error) {
    console.error('获取时间线失败:', error);
    ElMessage.error('获取时间线数据失败');
  } finally {
    loading.value = false;
  }
};

// 滚动事件处理
const handleScroll = debounce(() => {
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
  const scrollBottom = scrollHeight - (scrollTop + clientHeight);

  // 当距离底部小于配置的触发距离且还有更多数据时，加载更多
  if (scrollBottom < InfiniteScrollConfig.TRIGGER_DISTANCE && hasMore.value && !loading.value) {
    fetchTimelines();
  }
}, InfiniteScrollConfig.THROTTLE_DELAY);

onMounted(() => {
  // 初始加载
  fetchTimelines();
  
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  // 移除滚动监听
  window.removeEventListener('scroll', handleScroll);
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

@media (max-width: 768px) {
  .timelineContainer {
    padding: 10px 10px 0 5px;
  }
}
</style>