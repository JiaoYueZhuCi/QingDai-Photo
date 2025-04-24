<template>
  <div class="map-group">
    <div class="Container">
      <div class="mapCard">
        <div class="province-list-title">拍摄足迹</div>
        <div ref="mapContainer" class="map-container"></div>
      </div>
      <div class="province-list-card">
        <h3 class="province-list-title">已拍摄省份</h3>
        <ul class="province-list">
          <li v-for="province in activeProvinces" :key="province.adcode" class="province-item">
            {{ province.name }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue';
import * as echarts from 'echarts';
import chinaJson from '@/data/china.json';
import { allProvinces } from '@/data/mapProvinces';

const mapContainer = ref(null);
let chart = null;
let hasRegisteredMap = false; // 防止重复注册地图

// 计算已激活的省份列表
const activeProvinces = computed(() => {
  return allProvinces.filter(province => province.isActive)
    .sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'));
});

// 统一处理图表初始化
const initChart = async () => {
  try {
    // 等待容器渲染完成
    await nextTick();

    // 确保容器存在
    if (!mapContainer.value) return;

    // 单次注册地图
    if (!hasRegisteredMap) {
      try {
        // 确保chinaJson是有效的GeoJSON格式
        if (typeof chinaJson === 'string') {
          // 如果是字符串，尝试解析
          const parsedJson = JSON.parse(chinaJson);
          echarts.registerMap('china', parsedJson);
        } else {
          // 如果已经是对象，直接使用
          echarts.registerMap('china', chinaJson);
        }
        hasRegisteredMap = true;
      } catch (error) {
        console.error('注册地图失败:', error);
        throw new Error('地图数据格式错误，无法注册地图');
      }
    }

    // 初始化图表
    chart = echarts.init(mapContainer.value);

    // 配置项
    const option = {
      backgroundColor: '#000000',
      title: {
        text: '',
        left: 'center',
        textStyle: {
          color: '#fff',
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}',
        textStyle: {
          color: '#fff' // 设置提示框文字颜色为白色
        },
        backgroundColor: 'rgba(0, 0, 0, 0.7)', // 设置提示框背景色
        borderColor: '#45465E', // 设置提示框边框颜色
        borderWidth: 1
      },
      series: [
        {
          name: '中国',
          type: 'map',
          map: 'china',
          selectedMode: false,
          aspectScale: 0.75,
          zoom: 1.2,
          roam: false, // 禁用地图缩放和平移
          label: {
            show: false // 不显示省份名称
          },
          itemStyle: {
            areaColor: '#FAFBFF', 
            borderColor: '#45465E',
            borderWidth: 1
          },
          emphasis: {
            disabled: false,
            scale: true,
            label: {
              show: true,
              color: '#fff', // 悬浮时省份名称颜色为白色
              fontSize: 14 // 悬浮时字体稍大
            },
            itemStyle: {
              areaColor: '#c2c5df',
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          data: getMapData()
        }
      ]
    };

    chart.setOption(option);
    window.addEventListener('resize', handleResize);
  } catch (error) {
    console.error('地图初始化失败:', error);
  }
};

// 获取地图数据
const getMapData = () => {
  return allProvinces.map(province => {
    // 颜色设置
    const activeColor = '#c2c5df'; // 已拍摄地区颜色，使用主题色
    const inactiveColor = '#222222'; // 未拍摄地区颜色
    
    return {
      name: province.name,
      value: province.isActive ? 100 : 0,
      itemStyle: {
        areaColor: province.isActive ? activeColor : inactiveColor,
        borderColor: '#45465E' // 修改为统一的描边颜色
      }
    };
  });
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

</script>

<style scoped>
.map-group {
  background-color: #000000;
  padding: 10px;
}

.Container {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.map-container {
  width: 100%;
  height: 100%;
}

.mapCard {
  width: 80%;
  /* height: 300px; */
  background-color: #000000;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.4);
  border: 1px solid var(--qd-color-primary-light-3);
  border-radius: 4px;
}

.province-list-card {
  width: 20%;
  /* height: 300px; */
  background-color: #000000;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.4);
  border: 1px solid var(--qd-color-primary-light-3);
  border-radius: 4px;
  overflow-y: auto;
}

.province-list-title {
  color: #fff;
  text-align: center;
  font-size: 18px;
  margin: 0;
  padding: 10px 0;
  border-bottom: 1px solid #45465E;
}

.province-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.province-item {
  color: #fff;
  padding: 8px 15px;
  border-bottom: 1px solid rgba(69, 70, 94, 0.2);
  font-size: 16px;
  transition: background-color 0.3s;
}

.province-item:hover {
  background-color: rgba(69, 70, 94, 0.3);
}

@media (max-width: 768px) {
  .map-container{
    width: 95%;
    height: 100vw;
    margin-top: 3vh;
    left: 2.5vw;
  }
  .Container {
    flex-direction: column;
  }
  
  .mapCard {
    width: 100%;
    height: 120vw;
    margin-bottom: 10px;
  }
  
  .province-list-card {
    width: 100%;
    height: 35vh;
  }
  
  .province-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 5px;
    padding: 10px;
  }
  
  .province-item {
    text-align: center;
    padding: 8px 5px;
    font-size: 14px;
  }
}
</style>