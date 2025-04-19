<template>
  <div class="phopoDataContainer">
    <el-row :gutter="10">
      <el-col :span="8">
        <div class="statistic-card">
          <el-statistic :value="startPhotoCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                精选照片总数
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
                普通照片总数
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
            统计截止于 {{ CountTime }}
            <!-- <div class="footer-item">
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
            </div> -->
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="row2" style="margin-top: 10px;">
      <el-col :span="6">
        <div class="statistic-card">
          <el-statistic :value="morningGlowCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                已记录朝霞次数
                <el-tooltip effect="dark" content="朝霞照片的累计次数" placement="top">
                  <el-icon style="margin-left: 4px" :size="12">
                    <Warning />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-statistic>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="statistic-card">
          <el-statistic :value="eveningGlowCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                已记录晚霞次数
                <el-tooltip effect="dark" content="晚霞照片的累计次数" placement="top">
                  <el-icon style="margin-left: 4px" :size="12">
                    <Warning />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-statistic>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="statistic-card">
          <el-statistic :value="sunriseCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                已记录日出次数
                <el-tooltip effect="dark" content="日出照片的累计次数" placement="top">
                  <el-icon style="margin-left: 4px" :size="12">
                    <Warning />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-statistic>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="statistic-card">
          <el-statistic :value="sunsetCount">
            <template #title>
              <div style="display: inline-flex; align-items: center">
                已记录日落次数
                <el-tooltip effect="dark" content="日落照片的累计次数" placement="top">
                  <el-icon style="margin-left: 4px" :size="12">
                    <Warning />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-statistic>
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
import { getGroupPhotoCount } from '@/api/groupPhoto'
const startPhotoCount = ref<number>(0);
const photoCount = ref<number>(0);
const startPhotoMonthlyChange = ref<number>(0);
const startPhotoYearlyChange = ref<number>(0);
const photoMonthlyChange = ref<number>(0);
const photoYearlyChange = ref<number>(0);
const photoAccumulateMonthlyChange = ref<number>(1);
const photoAccumulateYearlyChange = ref<number>(1);

const morningGlowCount = ref<number>(0);
const eveningGlowCount = ref<number>(0);
const sunriseCount = ref<number>(0);
const sunsetCount = ref<number>(0);

const fetchMorningGlowCount = async () => {
  try {
    const response = await getGroupPhotoCount('1');
    morningGlowCount.value = Number(response);
  } catch (error) {
    console.error('获取朝霞次数失败:', error);
  }
};

const fetchEveningGlowCount = async () => {
  try {
    const response = await getGroupPhotoCount('2');
    eveningGlowCount.value = Number(response);
  } catch (error) {
    console.error('获取晚霞次数失败:', error);
  }
};

const fetchSunriseCount = async () => {
  try {
    const response = await getGroupPhotoCount('3');
    sunriseCount.value = Number(response);
  } catch (error) {
    console.error('获取日出次数失败:', error);
  }
};

const fetchSunsetCount = async () => {
  try {
    const response = await getGroupPhotoCount('4');
    sunsetCount.value = Number(response);
  } catch (error) {
    console.error('获取日落次数失败:', error);
  }
};

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

const CountTime = ref<string>('2025-04-12');
const cameraCount = ref<number>(79656);
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
  fetchMorningGlowCount();
  fetchEveningGlowCount();
  fetchSunriseCount();
  fetchSunsetCount();
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
    height: 95%;
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
    /* height: 200px; */
  }

  .el-row {
    margin-top: 10px !important
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
@media screen and (max-width: 768px) {}
</style>
