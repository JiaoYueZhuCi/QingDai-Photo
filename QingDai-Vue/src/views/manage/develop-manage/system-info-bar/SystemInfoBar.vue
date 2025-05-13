<template>
  <div class="system-info-bar">
    <div class="info-item">
      <span class="info-label">系统已运行：</span>
      <span class="info-value">{{ systemRunningDays }}</span>
    </div>
    <div class="info-item">
      <span class="info-label">前端已部署：</span>
      <span class="info-value">{{ frontendDeploymentDays }}</span>
    </div>
    <div class="info-item">
      <span class="info-label">后端已部署：</span>
      <span class="info-value">{{ backendRunningDays }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getSystemRunningDays, getFrontendDeploymentDays, getBackendRunningTime, setBackendStartTime, initFrontendDeployTime } from '@/data/system-info';
import { getBackendInfo } from '@/api/system';

// 系统运行信息
const systemRunningDays = ref<string>('0天0小时0分钟0秒');
const frontendDeploymentDays = ref<string>('0天0小时0分钟0秒');
const backendRunningDays = ref<string>('0天0小时0分钟0秒');

// 添加获取系统运行时间的方法
const fetchSystemRunningInfo = async () => {
  try {
    // 从后端获取启动时间
    const info:any = await getBackendInfo();
    if (info) {
      // 设置后端启动时间
      setBackendStartTime(info.startTime);
    }
    
    // 更新显示值
    systemRunningDays.value = getSystemRunningDays();
    frontendDeploymentDays.value = getFrontendDeploymentDays();
    backendRunningDays.value = getBackendRunningTime();
    
    // 每秒更新一次时间
    setInterval(() => {
      systemRunningDays.value = getSystemRunningDays();
      frontendDeploymentDays.value = getFrontendDeploymentDays();
      backendRunningDays.value = getBackendRunningTime();
    }, 1000);
  } catch (error) {
    console.error('获取系统运行信息失败', error);
  }
};

onMounted(() => {
  // 获取后端运行时间信息
  fetchSystemRunningInfo();
  // 获取前端部署时间
  initFrontendDeployTime()
});
</script>

<style scoped>
/* 系统信息栏样式 */
.system-info-bar {
  display: flex;
  justify-content: space-around;
  background: var(--qd-color-bg-light);
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 6px;
  border: 1px solid var(--qd-color-border);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.info-label {
  color: var(--qd-color-text-secondary);
  margin-right: 5px;
}

.info-value {
  color: var(--qd-color-primary);
  font-weight: bold;
}

/* 媒体查询适配移动端 */
@media (max-width: 768px) {
  .system-info-bar {
    flex-direction: column;
    gap: 10px;
    align-items: center;
  }
}
</style> 