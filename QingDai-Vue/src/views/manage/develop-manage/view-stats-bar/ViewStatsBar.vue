<template>
  <el-card class="view-stats-bar">
    <div class="stats-content">
      <el-statistic :value="totalViews">
        <template #title>
          <div style="display: inline-flex; align-items: center; gap: 4px;">
            <el-icon><View /></el-icon>
            <span>网页总访问量</span>
          </div>
        </template>
      </el-statistic>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { View } from '@element-plus/icons-vue';
import { getTotalViewCount } from '@/api/views';

// 总浏览量
const totalViews = ref<number>(0);

// 在组件挂载时获取浏览量
onMounted(async () => {
  try {
    // 获取总浏览量
    const response = await getTotalViewCount();
    totalViews.value = response;
  } catch (error) {
    console.error('获取浏览量失败:', error);
  }
});
</script>

<style scoped>
.view-stats-bar {
  margin-bottom: 20px;
}


.stats-content {
  text-align: center;
}
</style> 