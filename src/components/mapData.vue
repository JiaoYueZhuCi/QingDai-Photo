<template>
  <div v-if="isActive" ref="mapContainer" style="width: 100%; height: 600px"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, defineProps } from 'vue';
import * as echarts from 'echarts';
import chinaJson from '/public/map-data/china.json';

// 接收父组件参数
const props = defineProps({
  isActive: Boolean
});

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


const mapContainer = ref(null);
let chart = null;
let hasRegisteredMap = false; // 防止重复注册地图

// 统一处理图表初始化
const initChart = async () => {
  try {
    // 等待容器渲染完成
    await nextTick();
    
    // 确保容器存在
    if (!mapContainer.value) return;

    // 单次注册地图
    if (!hasRegisteredMap) {
      echarts.registerMap('china', chinaJson);
      hasRegisteredMap = true;
    }

    // 初始化图表
    chart = echarts.init(mapContainer.value);
    
    // 配置项（保持原有配置）
    const option = {
      geo: {
        map: 'china',
        roam: false,
        itemStyle: {
          areaColor: '#EEEEEE',
          borderColor: '#FFFFFF',
          borderWidth: 1
        },
        emphasis: {
          itemStyle: {
            areaColor: '#FF0000'
          }
        },
        regions: getRegionStyles()
      }
    };

    chart.setOption(option);
    window.addEventListener('resize', handleResize);
  } catch (error) {
    console.error('地图初始化失败:', error);
  }
};

// 清理资源
const disposeChart = () => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
  window.removeEventListener('resize', handleResize);
};

// 自适应处理
const handleResize = () => {
  if (chart) chart.resize();
};

// 监听激活状态变化
watch(() => props.isActive, (newVal) => {
  if (newVal) {
    initChart();
  } else {
    disposeChart();
  }
});

// 生命周期处理
onMounted(() => {
  if (props.isActive) initChart();
});

onBeforeUnmount(() => {
  disposeChart();
});

const getRegionStyles = () => {  // 返回选定省份样式
  return activatedProvinces.value.map(adcode => ({
    name: getProvinceName(adcode),
    itemStyle: {
      areaColor: '#FF0000',
      borderColor: '#FFFFFF'
    }
  }));
};

const getProvinceName = (adcode) => {   //根据省份代码返回省份名称
  const feature = chinaJson.features.find(f => f.properties.adcode === adcode);
  return feature?.properties?.name || '';
};
</script>