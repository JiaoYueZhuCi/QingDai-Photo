<template>
  <div class="phopoDataContainer">
    <el-row :gutter="10" class="statistic-row">
      <el-col :xs="12" :sm="6">
        <el-card class="statistic-card">
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
                  <el-icon><Top /></el-icon>
                  {{stats.changeStats.monthlyStarredChange}} / 月
                </span>
                <span v-else-if="stats.changeStats?.monthlyStarredChange < 0" style="color:#fc605d">
                  <el-icon><Bottom /></el-icon>
                  {{Math.abs(stats.changeStats.monthlyStarredChange)}} / 月
                </span>
                <span v-else>月持平</span>
                
                <span v-if="stats.changeStats?.yearlyStarredChange > 0" style="color:#3fb27f">
                  <el-icon><Top /></el-icon>
                  {{stats.changeStats.yearlyStarredChange}} / 年
                </span>
                <span v-else-if="stats.changeStats?.yearlyStarredChange < 0" style="color:#fc605d">
                  <el-icon><Bottom /></el-icon>
                  {{Math.abs(stats.changeStats.yearlyStarredChange)}} / 年
                </span>
                <span v-else>年持平</span>
              </div>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="statistic-card">
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
                  <el-icon><Top /></el-icon>
                  {{stats.changeStats.monthlyChange}} / 月
                </span>
                <span v-else-if="stats.changeStats?.monthlyChange < 0" style="color:#fc605d">
                  <el-icon><Bottom /></el-icon>
                  {{Math.abs(stats.changeStats.monthlyChange)}} / 月
                </span>
                <span v-else>月持平</span>
                
                <span v-if="stats.changeStats?.yearlyChange > 0" style="color:#3fb27f">
                  <el-icon><Top /></el-icon>
                  {{stats.changeStats.yearlyChange}} / 年
                </span>
                <span v-else-if="stats.changeStats?.yearlyChange < 0" style="color:#fc605d">
                  <el-icon><Bottom /></el-icon>
                  {{Math.abs(stats.changeStats.yearlyChange)}} / 年
                </span>
                <span v-else>年持平</span>
              </div>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="statistic-card">
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
                  <el-icon><Top /></el-icon>
                  {{stats.changeStats.monthlyMeteorologyChange}} / 月
                </span>
                <span v-else-if="stats.changeStats?.monthlyMeteorologyChange < 0" style="color:#fc605d">
                  <el-icon><Bottom /></el-icon>
                  {{Math.abs(stats.changeStats.monthlyMeteorologyChange)}} / 月
                </span>
                <span v-else>月持平</span>
                
                <span v-if="stats.changeStats?.yearlyMeteorologyChange > 0" style="color:#3fb27f">
                  <el-icon><Top /></el-icon>
                  {{stats.changeStats.yearlyMeteorologyChange}} / 年
                </span>
                <span v-else-if="stats.changeStats?.yearlyMeteorologyChange < 0" style="color:#fc605d">
                  <el-icon><Bottom /></el-icon>
                  {{Math.abs(stats.changeStats.yearlyMeteorologyChange)}} / 年
                </span>
                <span v-else>年持平</span>
              </div>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="statistic-card">
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
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" style="margin-top: 10px">
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">相机/无人机使用统计</div>
          <div class="chart-container" ref="cameraChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">相机型号统计</div>
          <div class="chart-container" ref="cameraModelChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">镜头型号统计</div>
          <div class="chart-container" ref="lensChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" style="margin-top: 10px">
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">气象照片类型统计</div>
          <div class="chart-container" ref="subjectChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">照片分级统计</div>
          <div class="chart-container" ref="typeChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">焦距分布统计</div>
          <div class="chart-container" ref="focalLengthChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" style="margin-top: 10px">
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">ISO分布</div>
          <div class="chart-container" ref="isoChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">快门分布</div>
          <div class="chart-container" ref="shutterChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="visualization-card">
          <div class="chart-title">光圈分布</div>
          <div class="chart-container" ref="apertureChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" style="margin-top: 10px">
      <el-col :xs="24" :sm="12">
        <el-card class="visualization-card">
          <div class="chart-title">月度拍摄数量统计</div>
          <div class="chart-container" ref="monthChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card class="visualization-card">
          <div class="chart-title">年度拍摄数量统计</div>
          <div class="chart-container" ref="yearChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { Camera, Picture, Sunny, Hide, Top, Bottom } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getPhotoDashboardStats } from '@/api/photo';

let cameraChart: echarts.ECharts | null = null;
let subjectChart: echarts.ECharts | null = null;
let typeChart: echarts.ECharts | null = null;
let cameraModelChart: echarts.ECharts | null = null;
let lensChart: echarts.ECharts | null = null;
let isoChart: echarts.ECharts | null = null;
let shutterChart: echarts.ECharts | null = null;
let apertureChart: echarts.ECharts | null = null;
let monthChart: echarts.ECharts | null = null;
let yearChart: echarts.ECharts | null = null;
let focalLengthChart: echarts.ECharts | null = null;

const cameraChartRef = ref<HTMLElement | null>(null);
const subjectChartRef = ref<HTMLElement | null>(null);
const typeChartRef = ref<HTMLElement | null>(null);
const cameraModelChartRef = ref<HTMLElement | null>(null);
const lensChartRef = ref<HTMLElement | null>(null);
const isoChartRef = ref<HTMLElement | null>(null);
const shutterChartRef = ref<HTMLElement | null>(null);
const apertureChartRef = ref<HTMLElement | null>(null);
const monthChartRef = ref<HTMLElement | null>(null);
const yearChartRef = ref<HTMLElement | null>(null);
const focalLengthChartRef = ref<HTMLElement | null>(null);

const stats = ref<any>({
  typeStats: {},
  changeStats: {},
  subjectStats: {},
  cameraStats: [],
  lensStats: [],
  isoStats: [],
  shutterStats: [],
  apertureStats: [],
  monthStats: {},
  yearStats: {},
  focalLengthStats: []
});

// 获取照片数据统计
const fetchPhotoStats = async () => {
  try {
    const response = await getPhotoDashboardStats();
    stats.value = response;
    
    // 确保DOM已完全渲染后再初始化图表
    nextTick(() => {
        initCharts();
    });
  } catch (error) {
    console.error('获取照片统计数据失败', error);
  }
};

// 初始化所有图表
const initCharts = () => {
  initCameraChart();
  initSubjectChart();
  initTypeChart();
  initCameraModelChart();
  initLensChart();
  initIsoChart();
  initShutterChart();
  initApertureChart();
  initMonthChart();
  initYearChart();
  initFocalLengthChart();
};

// 定义统一的图表配置和样式
const getCommonChartConfig = () => {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '0%',
      right: '0%',
      bottom: '0%',  
      top: '0%',
      containLabel: true
    }
  };
};

// 定义统一的颜色列表，使用月度拍摄数量统计的配色方案
const colorList = [
  '#fc605d', '#fdd555', '#3fb27f', '#4992ff', 
  '#b18fd6', '#ff7741', '#fcac52', '#5ac8fa', 
  '#ef6aa7', '#66c654', '#67ace6', '#cc5325'
];

// 获取柱状图通用配色函数
const getBarColors = (index: number) => {
  return colorList[index % colorList.length];
};

// 初始化相机/无人机统计图
const initCameraChart = () => {
  if (!cameraChartRef.value) return;
  
  if (cameraChart) {
    cameraChart.dispose();
  }
  
  try {
    cameraChart = echarts.init(cameraChartRef.value);
    
    const cameraCount = 79656;
    const droneCount = 7405;
    
    const option = {
      ...getCommonChartConfig(),
      color: ['#3fb27f', '#4992ff'],
      xAxis: {
        type: 'category',
        data: ['相机', '无人机'],
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: [
            { value: cameraCount, itemStyle: { color: colorList[2] } },
            { value: droneCount, itemStyle: { color: colorList[3] } }
          ],
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    cameraChart.setOption(option);
  } catch (e) {
    console.error('初始化相机图表错误:', e);
  }
};

// 初始化特色照片类型统计图
const initSubjectChart = () => {
  if (!subjectChartRef.value) return;
  
  if (subjectChart) {
    subjectChart.dispose();
  }
  
  try {
    subjectChart = echarts.init(subjectChartRef.value);
    
    const { morningGlow, eveningGlow, sunrise, sunset } = stats.value.subjectStats;
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: ['朝霞', '晚霞', '日出', '日落'],
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: [
            { value: morningGlow || 0, itemStyle: { color: colorList[0] } },
            { value: eveningGlow || 0, itemStyle: { color: colorList[1] } },
            { value: sunrise || 0, itemStyle: { color: colorList[3] } },
            { value: sunset || 0, itemStyle: { color: colorList[2] } }
          ],
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    subjectChart.setOption(option);
  } catch (e) {
    console.error('初始化特色照片类型图表错误:', e);
  }
};

// 初始化照片分类统计图
const initTypeChart = () => {
  if (!typeChartRef.value) return;
  
  if (typeChart) {
    typeChart.dispose();
  }
  
  try {
    typeChart = echarts.init(typeChartRef.value);
    
    const { starred, normal, meteorology, hidden } = stats.value.typeStats;
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: ['精选', '普通', '气象', '隐藏'],
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: [
            { value: starred || 0, itemStyle: { color: colorList[0] } },
            { value: normal || 0, itemStyle: { color: colorList[1] } },
            { value: meteorology || 0, itemStyle: { color: colorList[3] } },
            { value: hidden || 0, itemStyle: { color: colorList[2] } }
          ],
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    typeChart.setOption(option);
  } catch (e) {
    console.error('初始化照片分类图表错误:', e);
  }
};

// 初始化相机型号统计图
const initCameraModelChart = () => {
  if (!cameraModelChartRef.value) return;
  
  if (cameraModelChart) {
    cameraModelChart.dispose();
  }
  
  try {
    cameraModelChart = echarts.init(cameraModelChartRef.value);
    
    // 排序并只显示前10种相机型号
    const topCameras = [...stats.value.cameraStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topCameras.map(item => item.name),
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: topCameras.map((item, index) => ({
            value: item.value,
            itemStyle: { color: getBarColors(index) }
          })),
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    cameraModelChart.setOption(option);
  } catch (e) {
    console.error('初始化相机型号图表错误:', e);
  }
};

// 初始化镜头型号统计图
const initLensChart = () => {
  if (!lensChartRef.value) return;
  
  if (lensChart) {
    lensChart.dispose();
  }
  
  try {
    lensChart = echarts.init(lensChartRef.value);
    
    // 排序并只显示前10种镜头型号
    const topLenses = [...stats.value.lensStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topLenses.map(item => item.name),
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: topLenses.map((item, index) => ({
            value: item.value,
            itemStyle: { color: getBarColors(index) }
          })),
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    lensChart.setOption(option);
  } catch (e) {
    console.error('初始化镜头型号图表错误:', e);
  }
};

// 初始化ISO分布图
const initIsoChart = () => {
  if (!isoChartRef.value) return;
  
  if (isoChart) {
    isoChart.dispose();
  }
  
  try {
    isoChart = echarts.init(isoChartRef.value);
    
    // 排序并只显示前10种ISO
    const topIsos = [...stats.value.isoStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
          type: 'category',
        data: topIsos.map(item => item.name),
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
          axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: topIsos.map((item, index) => ({
            value: item.value,
            itemStyle: { color: getBarColors(index) }
          })),
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    isoChart.setOption(option);
  } catch (e) {
    console.error('初始化ISO分布图表错误:', e);
  }
};

// 初始化快门分布图
const initShutterChart = () => {
  if (!shutterChartRef.value) return;
  
  if (shutterChart) {
    shutterChart.dispose();
  }
  
  try {
    shutterChart = echarts.init(shutterChartRef.value);
    
    // 排序并只显示前10种快门速度
    const topShutters = [...stats.value.shutterStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topShutters.map(item => item.name),
          axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
          type: 'value',
          axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: topShutters.map((item, index) => ({
            value: item.value,
            itemStyle: { color: getBarColors(index) }
          })),
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    shutterChart.setOption(option);
  } catch (e) {
    console.error('初始化快门分布图表错误:', e);
  }
};

// 初始化光圈分布图
const initApertureChart = () => {
  if (!apertureChartRef.value) return;
  
  if (apertureChart) {
    apertureChart.dispose();
  }
  
  try {
    apertureChart = echarts.init(apertureChartRef.value);
    
    // 排序并只显示前10种光圈
    const topApertures = [...stats.value.apertureStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

      const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topApertures.map(item => item.name),
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: topApertures.map((item, index) => ({
            value: item.value,
            itemStyle: { color: getBarColors(index) }
          })),
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    apertureChart.setOption(option);
  } catch (e) {
    console.error('初始化光圈分布图表错误:', e);
  }
};

// 初始化月度拍摄数量统计图
const initMonthChart = () => {
  if (!monthChartRef.value) return;
  
  if (monthChart) {
    monthChart.dispose();
  }
  
  try {
    monthChart = echarts.init(monthChartRef.value);
    
    const monthLabels = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
    const monthData = monthLabels.map((_, index) => {
      const month = String(index + 1);
      return stats.value.monthStats[month] || 0;
    });
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: monthLabels,
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          name: '拍摄数量',
          type: 'bar',
          barWidth: '60%',
          data: monthData.map((value, index) => ({
            value,
            itemStyle: { color: colorList[index] }
          })),
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    monthChart.setOption(option);
  } catch (e) {
    console.error('初始化月度拍摄统计图表错误:', e);
  }
};

// 初始化年度拍摄数量统计图
const initYearChart = () => {
  if (!yearChartRef.value) return;
  
  if (yearChart) {
    yearChart.dispose();
  }
  
  try {
    yearChart = echarts.init(yearChartRef.value);
    
    // 获取年份数据并排序
    const yearData = Object.entries(stats.value.yearStats || {})
      .sort(([yearA], [yearB]) => Number(yearA) - Number(yearB))
      .map(([year, count]) => ({ year, count }));
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: yearData.map(item => `${item.year}年`),
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          name: '拍摄数量',
          type: 'bar',
          barWidth: '60%',
          data: yearData.map((item, index) => ({
            value: item.count,
            itemStyle: { color: getBarColors(index) }
          })),
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    yearChart.setOption(option);
  } catch (e) {
    console.error('初始化年度拍摄统计图表错误:', e);
  }
};

// 初始化焦距分布图
const initFocalLengthChart = () => {
  if (!focalLengthChartRef.value) return;
  
  if (focalLengthChart) {
    focalLengthChart.dispose();
  }
  
  try {
    focalLengthChart = echarts.init(focalLengthChartRef.value);
    
    // 排序并只显示前10种焦距
    const topFocalLengths = [...stats.value.focalLengthStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);
    
    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topFocalLengths.map(item => item.name),
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      series: [
        {
          data: topFocalLengths.map((item, index) => ({
            value: item.value,
            itemStyle: { color: getBarColors(index) }
          })),
          type: 'bar',
          barWidth: '60%',
          showBackground: true,
          backgroundStyle: {
            color: 'var(--qd-color-primary-dark-8)'
          }
        }
      ]
    };
    
    focalLengthChart.setOption(option);
  } catch (e) {
    console.error('初始化焦距分布图表错误:', e);
  }
};

// 窗口大小变化时重绘图表
const handleResize = () => {
  // 使用防抖，避免频繁触发
  if (resizeTimer) {
    clearTimeout(resizeTimer);
  }
  resizeTimer = setTimeout(() => {
    // 延迟重新初始化图表，确保DOM尺寸已更新
    initCharts();
  }, 200);
};

// 手动触发重新渲染所有图表
const refreshAllCharts = () => {
  // 使用防抖，避免频繁触发
  if (refreshTimer) {
    clearTimeout(refreshTimer);
  }
  refreshTimer = setTimeout(() => {
    nextTick(() => {
      initCharts();
    });
  }, 200);
};

// 添加防抖定时器
let resizeTimer: ReturnType<typeof setTimeout> | null = null;
let refreshTimer: ReturnType<typeof setTimeout> | null = null;

onMounted(() => {
  // 在window.onload回调中初始化,确保DOM完全加载
  if (document.readyState === 'complete') {
    fetchPhotoStats();
  } else {
    window.addEventListener('load', fetchPhotoStats);
  }
  
  // 移除标签页切换事件监听，避免不必要的重绘
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  // 清理资源
  if (cameraChart) {
    cameraChart.dispose();
    cameraChart = null;
  }
  if (subjectChart) {
    subjectChart.dispose();
    subjectChart = null;
  }
  if (typeChart) {
    typeChart.dispose();
    typeChart = null;
  }
  if (cameraModelChart) {
    cameraModelChart.dispose();
    cameraModelChart = null;
  }
  if (lensChart) {
    lensChart.dispose();
    lensChart = null;
  }
  if (isoChart) {
    isoChart.dispose();
    isoChart = null;
  }
  if (shutterChart) {
    shutterChart.dispose();
    shutterChart = null;
  }
  if (apertureChart) {
    apertureChart.dispose();
    apertureChart = null;
  }
  if (monthChart) {
    monthChart.dispose();
    monthChart = null;
  }
  if (yearChart) {
    yearChart.dispose();
    yearChart = null;
  }
  if (focalLengthChart) {
    focalLengthChart.dispose();
    focalLengthChart = null;
  }
  
  // 清理定时器
  if (resizeTimer) {
    clearTimeout(resizeTimer);
  }
  if (refreshTimer) {
    clearTimeout(refreshTimer);
  }
  
  window.removeEventListener('resize', handleResize);
  window.removeEventListener('load', fetchPhotoStats);
});

</script>

<style scoped>
.phopoDataContainer {
  background-color: var(--qd-color-primary-dark-10);
  padding: 10px 10px 0px 10px;
}

.statistic-card {
  height: 80px;
  padding: 20px;
  border-radius: 4px;
  background-color: var(--qd-color-primary-dark-6);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.4);
  border: 1px solid var(--qd-color-primary-light-3);
  margin-bottom: 10px;
}

.visualization-card {
  padding: 20px 10px 10px 10px;
  border-radius: 4px;
  background-color: var(--qd-color-primary-dark-10);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.4);
  position: relative;
  border: 1px solid var(--qd-color-primary-light-3);
  /* 确保容器具有明确的尺寸 */
  height: 300px;
  box-sizing: border-box;
  margin-bottom: 10px;
}

:deep(.el-statistic__number) {
  color: var(--qd-color-primary-light-9) !important;
}

:deep(.el-statistic__head) {
  color: var(--qd-color-primary-light-6) !important;
}

.chart-container {
  padding-top: 25px;
  height: 100%;
  width: 100%;
  min-height: 250px;
  /* 修复图表左右不对称的问题 */
  left: -2vh;
}

.chart-title {
  position: absolute;
  top: 5px;
  left: 0;
  width: 100%;
  text-align: center;
  color: var(--qd-color-primary-light-6);
  font-size: 14px;
}

.change-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
}

:deep(.el-card__body) {
  padding: 0;
}

@media (max-width: 768px) {
  .statistic-card {
    height: auto;
    min-height: 80px;
  }
  
  .visualization-card {
    height: 35vh;
  }
  
  .chart-container {
    min-height: 30vh;
  }
}
</style>
