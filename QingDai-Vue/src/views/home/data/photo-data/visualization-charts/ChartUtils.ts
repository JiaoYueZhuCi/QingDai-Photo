import * as echarts from 'echarts';

// 定义统一的图表配置和样式
export const getCommonChartConfig = () => {
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
      bottom: '1%',
      top: '0%',
      containLabel: true
    },
    xAxis: {
      axisLabel: {
        show: false
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      axisLabel: {
        show: false
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        show: false
      }
    }
  };
};

// 定义统一的颜色列表
export const colorList = [
  '#fc605d', '#fdd555', '#3fb27f', '#4992ff',
  '#b18fd6', '#ff7741', '#fcac52', '#5ac8fa',
  '#ef6aa7', '#66c654', '#67ace6', '#cc5325'
];

// 获取柱状图通用配色函数
export const getBarColors = (index: number) => {
  return colorList[index % colorList.length];
};