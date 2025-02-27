<template>
  <div v-show="mapVisible" ref="mapContainer" style="width: 100%; height: 600px"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';
import chinaJson from '/public/map-data/china.json';

// 响应式数据
const mapVisible = ref(false);
const activatedProvinces = ref([
  110000, // 北京市
  310000, // 上海市
  440000, // 广东省
  120000, // 天津市
  130000, // 河北省
  150000, // 内蒙古自治区
  210000, // 辽宁省
  140000, // 山西省
  610000, // 陕西省
  510000, // 四川省
  320000, // 江苏省
  330000, // 浙江省
  350000, // 福建省
  440000, // 广东省
  500000, // 重庆市
  450000, // 广西自治区
  460000, // 海南省
  430000, // 湖南省
  370000, // 山东省
]);

const mapContainer = ref(null); // DOM 引用
let chart = null; // ECharts 实例

// 生命周期
onMounted(async () => {
  // 注册地图数据
  echarts.registerMap('china', chinaJson);

  // 显示容器
  mapVisible.value = true;

  // 等待 DOM 更新
  await nextTick();

  // 初始化图表
  chart = echarts.init(mapContainer.value);

  // 配置项
  const option = {
    geo: {
      map: 'china',
      roam: false,
      itemStyle: {
        areaColor: '#EEEEEE',
        borderColor: '#FFFFFF',
        borderWidth: 1
      },
      emphasis: { // 独立的高亮配置
        itemStyle: {
          areaColor: '#FF0000'
        }
      },
      regions: getRegionStyles()
    }
  };

  chart.setOption(option);

  // 窗口 resize 监听
  window.addEventListener('resize', handleResize);
});

// 清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (chart) chart.dispose();
});

// 方法
const getRegionStyles = () => {
  return activatedProvinces.value.map(adcode => ({
    name: getProvinceName(adcode),
    itemStyle: {
      areaColor: '#FF0000',
      borderColor: '#FFFFFF'
    }
  }));
};

const getProvinceName = (adcode) => {
  const feature = chinaJson.features.find(f => f.properties.adcode === adcode);
  return feature?.properties?.name || '';
};

// 事件处理
const handleResize = () => {
  if (chart) chart.resize();
};
</script>