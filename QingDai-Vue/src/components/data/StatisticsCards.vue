<template>
  <div class="statistic-container">
    <div class="statistic-card">
      <el-statistic :value="stats.typeStats?.starred || 0">
        <template #title>
          <div style="display: inline-flex; align-items: center">
            精选照片数量
            <el-icon style="margin-left: 4px">
              <Camera />
            </el-icon>
          </div>
        </template>
        <template #suffix>
          <div class="change-info">
            <span v-if="stats.changeStats?.monthlyStarredChange > 0" style="color:#3fb27f">
              <el-icon>
                <Top />
              </el-icon>
              {{ stats.changeStats.monthlyStarredChange }} / 月
            </span>
            <span v-else-if="stats.changeStats?.monthlyStarredChange < 0" style="color:#fc605d">
              <el-icon>
                <Bottom />
              </el-icon>
              {{ Math.abs(stats.changeStats.monthlyStarredChange) }} / 月
            </span>
            <span v-else>月持平</span>

            <span v-if="stats.changeStats?.yearlyStarredChange > 0" style="color:#3fb27f">
              <el-icon>
                <Top />
              </el-icon>
              {{ stats.changeStats.yearlyStarredChange }} / 年
            </span>
            <span v-else-if="stats.changeStats?.yearlyStarredChange < 0" style="color:#fc605d">
              <el-icon>
                <Bottom />
              </el-icon>
              {{ Math.abs(stats.changeStats.yearlyStarredChange) }} / 年
            </span>
            <span v-else>年持平</span>
          </div>
        </template>
      </el-statistic>
    </div>
    <div class="statistic-card">
      <el-statistic :value="stats.typeStats?.normal || 0">
        <template #title>
          <div style="display: inline-flex; align-items: center">
            普通照片数量
            <el-icon style="margin-left: 4px">
              <Picture />
            </el-icon>
          </div>
        </template>
        <template #suffix>
          <div class="change-info">
            <span v-if="stats.changeStats?.monthlyChange > 0" style="color:#3fb27f">
              <el-icon>
                <Top />
              </el-icon>
              {{ stats.changeStats.monthlyChange }} / 月
            </span>
            <span v-else-if="stats.changeStats?.monthlyChange < 0" style="color:#fc605d">
              <el-icon>
                <Bottom />
              </el-icon>
              {{ Math.abs(stats.changeStats.monthlyChange) }} / 月
            </span>
            <span v-else>月持平</span>

            <span v-if="stats.changeStats?.yearlyChange > 0" style="color:#3fb27f">
              <el-icon>
                <Top />
              </el-icon>
              {{ stats.changeStats.yearlyChange }} / 年
            </span>
            <span v-else-if="stats.changeStats?.yearlyChange < 0" style="color:#fc605d">
              <el-icon>
                <Bottom />
              </el-icon>
              {{ Math.abs(stats.changeStats.yearlyChange) }} / 年
            </span>
            <span v-else>年持平</span>
          </div>
        </template>
      </el-statistic>
    </div>
    <div class="statistic-card">
      <el-statistic :value="stats.typeStats?.meteorology || 0">
        <template #title>
          <div style="display: inline-flex; align-items: center">
            气象照片数量
            <el-icon style="margin-left: 4px">
              <Sunny />
            </el-icon>
          </div>
        </template>
        <template #suffix>
          <div class="change-info">
            <span v-if="stats.changeStats?.monthlyMeteorologyChange > 0" style="color:#3fb27f">
              <el-icon>
                <Top />
              </el-icon>
              {{ stats.changeStats.monthlyMeteorologyChange }} / 月
            </span>
            <span v-else-if="stats.changeStats?.monthlyMeteorologyChange < 0" style="color:#fc605d">
              <el-icon>
                <Bottom />
              </el-icon>
              {{ Math.abs(stats.changeStats.monthlyMeteorologyChange) }} / 月
            </span>
            <span v-else>月持平</span>

            <span v-if="stats.changeStats?.yearlyMeteorologyChange > 0" style="color:#3fb27f">
              <el-icon>
                <Top />
              </el-icon>
              {{ stats.changeStats.yearlyMeteorologyChange }} / 年
            </span>
            <span v-else-if="stats.changeStats?.yearlyMeteorologyChange < 0" style="color:#fc605d">
              <el-icon>
                <Bottom />
              </el-icon>
              {{ Math.abs(stats.changeStats.yearlyMeteorologyChange) }} / 年
            </span>
            <span v-else>年持平</span>
          </div>
        </template>
      </el-statistic>
    </div>
    <div class="statistic-card">
      <el-statistic :value="stats.typeStats?.hidden || 0">
        <template #title>
          <div style="display: inline-flex; align-items: center">
            隐藏照片数量
            <el-icon style="margin-left: 4px">
              <Hide />
            </el-icon>
          </div>
        </template>
      </el-statistic>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Camera, Picture, Sunny, Hide, Top, Bottom } from '@element-plus/icons-vue';

defineProps({
  stats: {
    type: Object,
    required: true,
    default: () => ({
      typeStats: {},
      changeStats: {}
    })
  }
});
</script>

<style scoped>
.statistic-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 8px;
  height: 120px;
}

.statistic-card {
  background-color: var(--qd-color-bg);
  border: 1px solid var(--qd-color-border);
  border-radius: 4px;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.2);
  padding: 8px;
  height: 100px;
}

.change-info {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  color: var(--qd-color-text-secondary);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .statistic-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 4px;
    height: 220px;
  }

  .statistic-card {
    padding: 4px;
  }
}

/* 夜间模式下的卡片样式 */
.dark .statistic-card {
  background-color: var(--qd-color-primary-dark-9);
  border: 1px solid var(--qd-color-primary-dark-7);
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);
}

.dark .change-info {
  color: var(--qd-color-primary-light-6);
}

[data-theme="dark"] .statistic-card,
.dark .statistic-card {
  background: var(--qd-color-dark-8);
  border-color: var(--qd-color-dark-6);
}
</style> 