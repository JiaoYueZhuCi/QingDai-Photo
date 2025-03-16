<template>
  <div class="phopoDataContainer">
    <el-row :gutter="10">
      <el-col :span="8">
        <div class="statistic-card">
          <el-statistic :value="startPhotoCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                代表作照片总数
                <el-tooltip effect="dark" content="具有个人代表的的照片累计和" placement="top">
                  <el-icon style="margin-left: 4px" :size="12">
                    <Warning />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-statistic>
          <div class="statistic-footer">
            <div class="footer-item">
              <span>本月变化</span>
              <span :class="startPhotoMonthlyChange < 0 ? 'red' : 'green'">
                {{ startPhotoMonthlyChange }}
                <el-icon>
                  <component :is="startPhotoMonthlyChange < 0 ? CaretBottom : CaretTop" />
                </el-icon>
              </span>
              <span>年度变化</span>
              <span :class="startPhotoYearlyChange < 0 ? 'red' : 'green'">
                {{ startPhotoYearlyChange }}
                <el-icon>
                  <component :is="startPhotoYearlyChange < 0 ? CaretBottom : CaretTop" />
                </el-icon>
              </span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="statistic-card">
          <el-statistic :value="photoCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                精选照片总数
                <el-tooltip effect="dark" content="普通照片累积和" placement="top">
                  <el-icon style="margin-left: 4px" :size="12">
                    <Warning />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-statistic>
          <div class="statistic-footer">
            <div class="footer-item">
              <span>本月变化</span>
              <span :class="photoMonthlyChange < 0 ? 'red' : 'green'">
                {{ photoMonthlyChange }}
                <el-icon>
                  <component :is="photoMonthlyChange < 0 ? CaretBottom : CaretTop" />
                </el-icon>
              </span>
              <span>年度变化</span>
              <span :class="photoYearlyChange < 0 ? 'red' : 'green'">
                {{ photoYearlyChange }}
                <el-icon>
                  <component :is="photoYearlyChange < 0 ? CaretBottom : CaretTop" />
                </el-icon>
              </span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="statistic-card">
          <el-statistic :value="cameraCount + droneCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                累积照片总数
                <div class="sub-counts">
                  <span class="sub-count">相机 {{ cameraCount }}</span>
                  <span class="sub-count">无人机 {{ droneCount }}</span>
                </div>
                <el-tooltip effect="dark" content="参考快门数累计和" placement="top">
                  <el-icon style="margin-left: 4px" :size="12">
                    <Warning />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-statistic>
          <div class="statistic-footer">
            <div class="footer-item">
              <span>本月变化</span>
              <span :class="photoMonthlyChange < 0 ? 'red' : 'green'">
                {{ photoAccumulateMonthlyChange }}
                <el-icon>
                  <component :is="photoMonthlyChange < 0 ? CaretBottom : CaretTop" />
                </el-icon>
              </span>
              <span>年度变化</span>
              <span :class="photoMonthlyChange < 0 ? 'red' : 'green'">
                {{ photoAccumulateMonthlyChange }}
                <el-icon>
                  <component :is="photoMonthlyChange < 0 ? CaretBottom : CaretTop" />
                </el-icon>
              </span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import {
  CaretBottom,
  CaretTop,
  Warning,
  SemiSelect
} from '@element-plus/icons-vue';
import {
  getStartPhotoCount,
  getMonthlyStartPhotoCountChange,
  getYearlyStartPhotoCountChange,
  getPhotoCount,
  getMonthlyPhotoCountChange,
  getYearlyPhotoCountChange
} from '@/api/photo'

const startPhotoCount = ref<number>(0);
const photoCount = ref<number>(0);
const startPhotoMonthlyChange = ref<number>(0);
const startPhotoYearlyChange = ref<number>(0);
const photoMonthlyChange = ref<number>(0);
const photoYearlyChange = ref<number>(0);
const photoAccumulateMonthlyChange = ref<number>(1);
const photoAccumulateYearlyChange = ref<number>(1);

const fetchStartPhotoCount = async () => {
  try {
    const response = await getStartPhotoCount();
    startPhotoCount.value = Number(response);
    // 获取本月变化数据
    const monthlyChangeResponse = await getMonthlyStartPhotoCountChange();
    startPhotoMonthlyChange.value = Number(monthlyChangeResponse);
    // 获取年度变化数据
    const yearlyChangeResponse = await getYearlyStartPhotoCountChange();
    startPhotoYearlyChange.value = Number(yearlyChangeResponse);
  } catch (error) {
    console.error('获取代表作照片总数失败:', error);
  }
};

const fetchPhotoCount = async () => {
  try {
    const response = await getPhotoCount();
    photoCount.value = Number(response);
    // 获取本月变化数据
    const monthlyChangeResponse = await getMonthlyPhotoCountChange();
    photoMonthlyChange.value = Number(monthlyChangeResponse);
    // 获取年度变化数据
    const yearlyChangeResponse = await getYearlyPhotoCountChange();
    photoYearlyChange.value = Number(yearlyChangeResponse);
  } catch (error) {
    console.error('获取精选照片总数失败:', error);
  }
};

const cameraCount = ref<number>(71765);
const droneCount = ref<number>(7405);

// 在fetchPhotoCount方法中添加
const fetchDeviceCounts = async () => {
  try {
  } catch (error) {
    console.error('获取设备数量失败:', error);
  }
};


// 在onMounted中添加调用
onMounted(() => {
  fetchStartPhotoCount();
  fetchPhotoCount();
  fetchDeviceCounts();
});


</script>

<style scoped>
.phopoDataContainer {
  background-color: black;
  padding: 10px;
}

:global(h2#card-usage ~ .example .example-showcase) {
  background-color: var(--el-fill-color) !important;
}

.el-statistic {
  --el-statistic-content-font-size: 28px;
}

.statistic-card {
  height: 150px;
  padding: 20px;
  border-radius: 4px;
  background-color: var(--el-bg-color-overlay);
}

.statistic-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-top: 16px;
}

.statistic-footer .footer-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistic-footer .footer-item span:last-child {
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
}

.green {
  color: var(--el-color-success);
}

.red {
  color: var(--el-color-error);
}

.el-col {
  padding: 0;
}

/* 添加移动端响应式样式 */
@media screen and (max-width: 768px) {
  .el-statistic {
    --el-statistic-content-font-size: 20px;
  }

  .statistic-card {
    height: 160px;
    min-height: 130px;
    padding: 5px;
  }

  .statistic-footer {
    font-size: 10px;
    margin-top: 12px;
  }

  :deep(.el-statistic__title) {
    font-size: 12px;
  }

  .sub-counts {
    flex-direction: column;
    gap: 2px;
  }

  .sub-count {
    font-size: 10px;
  }

  .phopoDataContainer {
    height: 25%;
  }
}
</style>

<style>
.sub-counts {
  margin-left: 8px;
  display: flex;
  gap: 6px;
}

.sub-count {
  font-size: 12px;
  color: #999;
  position: relative;
  padding-right: 8px;
}

.sub-count:not(:last-child)::after {
  content: '+';
  position: absolute;
  right: -2px;
}

/* 添加移动端响应式样式 */
@media screen and (max-width: 768px) {
  .el-col {
    margin-bottom: 10px;
  }
}
</style>
