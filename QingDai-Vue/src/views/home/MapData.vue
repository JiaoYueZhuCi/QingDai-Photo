<template>
  <div class="map-group">
      <el-affix :offset="10">
      <div class="card mapCard">
        <div ref="mapContainer" class="map-container"></div>
      </div>
    </el-affix>
    <div class="card timelineCard">
      <Timeline />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import * as echarts from 'echarts';
import chinaJson from '@/map-data/china.json';
import Timeline from '@/views/home/Timeline.vue';

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

    // 配置项
    const option = {
      geo: {
        map: 'china',
        // width: '40vh',
        // height: '40vh',
        roam: false,
        itemStyle: {
          areaColor: '#EEEEEE',
          borderColor: 'black',
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

// 生命周期处理
onMounted(() => {
  initChart();
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

<style scoped>
.map-group {
  background-color: black;
  display: flex;
  padding: 0 10px 10px 10px;
  justify-content: space-around;
  gap: 10px;
  /* min-width: 520px; */
}

.map-container {
  /* width: 40vw;
  height: 70vh; */
  width: 100%;
  height: 100%;
  padding: 10px 0 0 0;
  position: relative;
  top: -1vh;
}

.mapCard {
  padding: 10px;
  width: 40vw;
  height: 94vh;
}

.timelineCard {
  width: 54vw;
}

.card {
  border-radius: 4px;
  background-color: rgb(250, 250, 250);
}

/* 添加媒体查询，当屏幕宽度小于等于 768px 时（通常为手机屏幕），调整 mapCard 的样式 */
@media (max-width: 600px) {
  .map-group {
    flex-direction: column;
  }

  .map-container {
    top: -5vh;
  }

  .map-container {
    width: 90vw;
    height: 300px;
  }

  .mapCard {
    width: 90vw;
    height: 230px;
  }

  .timelineCard {
    width: 100%;
    margin-top: 10px;
  }
}
</style>