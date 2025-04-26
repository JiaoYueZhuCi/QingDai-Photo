<template>
  <div class="visualization-container">
    <div class="visualization-card">
      <div class="chart-title">相机/无人机使用统计</div>
      <div class="chart-container" ref="cameraChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">相机型号统计</div>
      <div class="chart-container" ref="cameraModelChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">镜头型号统计</div>
      <div class="chart-container" ref="lensChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">气象照片类型统计</div>
      <div class="chart-container" ref="subjectChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">照片分级统计</div>
      <div class="chart-container" ref="typeChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">焦距分布统计</div>
      <div class="chart-container" ref="focalLengthChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">ISO分布</div>
      <div class="chart-container" ref="isoChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">快门分布</div>
      <div class="chart-container" ref="shutterChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">光圈分布</div>
      <div class="chart-container" ref="apertureChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">月度拍摄数量统计</div>
      <div class="chart-container" ref="monthChartRef"></div>
    </div>
    <div class="visualization-card">
      <div class="chart-title">年度拍摄数量统计</div>
      <div class="chart-container" ref="yearChartRef"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import * as echarts from 'echarts';
import { getCommonChartConfig, getBarColors, colorList } from './ChartUtils';

const props = defineProps({
  stats: {
    type: Object,
    required: true,
    default: () => ({
      typeStats: {},
      subjectStats: {},
      cameraStats: [],
      lensStats: [],
      isoStats: [],
      shutterStats: [],
      apertureStats: [],
      monthStats: {},
      yearStats: {},
      focalLengthStats: []
    })
  }
});

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
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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

    const { morningGlow, eveningGlow, sunrise, sunset } = props.stats.subjectStats;

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: ['朝霞', '晚霞', '日出', '日落'],
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'

        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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

    const { starred, normal, meteorology, hidden } = props.stats.typeStats;

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: ['精选', '普通', '气象', '隐藏'],
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
    const topCameras = [...props.stats.cameraStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topCameras.map(item => item.name),
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
    const topLenses = [...props.stats.lensStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topLenses.map(item => item.name),
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
    const topIsos = [...props.stats.isoStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topIsos.map(item => item.name),
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
    const topShutters = [...props.stats.shutterStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topShutters.map(item => item.name),
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
    const topApertures = [...props.stats.apertureStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topApertures.map(item => item.name),
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
      return props.stats.monthStats[month] || 0;
    });

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: monthLabels,
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
    const yearData = Object.entries(props.stats.yearStats || {})
      .sort(([yearA], [yearB]) => Number(yearA) - Number(yearB))
      .map(([year, count]) => ({ year, count }));

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: yearData.map(item => `${item.year}年`),
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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
    const topFocalLengths = [...props.stats.focalLengthStats]
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

    const option = {
      ...getCommonChartConfig(),
      xAxis: {
        type: 'category',
        data: topFocalLengths.map(item => item.name),
        axisLabel: {
          show: false,
          color: 'var(--qd-color-primary-light-6)'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: false,
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
            color: 'transparent'
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

// 防抖定时器
let resizeTimer: ReturnType<typeof setTimeout> | null = null;

// 监听stats变化，重新渲染图表
watch(() => props.stats, () => {
  nextTick(() => {
    initCharts();
  });
}, { deep: true });

onMounted(() => {
  // 初始化图表
  nextTick(() => {
    initCharts();
  });

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

  window.removeEventListener('resize', handleResize);
});

// 向父组件暴露刷新方法
defineExpose({
  refreshCharts: initCharts
});
</script>

<style scoped>
.visualization-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.visualization-card {
  background-color: var(--qd-color-bg);
  border: 1px solid var(--qd-color-border);
  border-radius: 4px;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.2);
  padding: 8px;
  height: 370px;
  transition: all 0.3s ease;
}

.chart-title {
  text-align: center;
  font-size: 16px;
  margin: 5px 0;
  color: var(--qd-color-text-primary);
}

.chart-container {
  width: 100%;
  height: 280px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .visualization-container {
    grid-template-columns: 1fr;
    gap: 4px;
  }

  .visualization-card {
    padding: 4px;
  }
}

/* 夜间模式下的卡片样式 */
.dark .visualization-card {
  background-color: var(--qd-color-primary-dark-9);
  border: 1px solid var(--qd-color-primary-dark-7);
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);
}

.dark .chart-title {
  color: var(--qd-color-primary-light-8);
}

/* 图表横轴隐藏数值 */
:deep(.el-chart .x-axis .tick text) {
  display: none !important;
}

[data-theme="dark"] .visualization-card,
.dark .visualization-card {
  background: var(--qd-color-dark-8);
  border-color: var(--qd-color-dark-6);
}

[data-theme="dark"] .chart-title,
.dark .chart-title {
  color: var(--qd-color-dark-2);
}
</style> 