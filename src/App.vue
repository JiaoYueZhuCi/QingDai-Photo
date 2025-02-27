<template>
  <el-backtop :right="50" :bottom="50" />

  <Introduce />

  <!-- <tabs /> -->
  <el-tabs type="border-card" @tab-click="handleTabClick">
    <el-tab-pane label="精选">
      <waterfall :images="images" :rowHeightMax="300" :rowHeightMin="150" :gap="10" @open-preview="openPreview"
        @open-full-preview="openFullImg" />
    </el-tab-pane>
    <el-tab-pane label="照片">
      <waterfall :images="images" :rowHeightMax="300" :rowHeightMin="150" :gap="10" @open-preview="openPreview"
        @open-full-preview="openFullImg" />
    </el-tab-pane>
    <el-tab-pane label="时间轴">
      <timeline />
    </el-tab-pane>
    <el-tab-pane label="数据" name="data">
      <photoData />
      <mapData :is-active="activeTabKey"/>
    </el-tab-pane>
  </el-tabs>


  <el-dialog v-model="previewVisible" :before-close="handlePreviewClose" width="80%">
    <img :src="previewImage" alt="预览图片" class="preview-image" style="width: 100%; height: auto;" />
  </el-dialog>

  <el-image-viewer :teleported="true" v-if="fullImgShow" :url-list="fullImgList" :initial-index="currentIndex"
    @close="fullImgShow = false" />



</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import type { WaterfallItem } from './types';
import { ElImageViewer } from 'element-plus';
import type { TabsPaneContext } from 'element-plus';
import waterfall from '@/components/waterfall.vue';
import Introduce from '@/components/introduce.vue';
import tabs from '@/components/tabs.vue';
import timeline from '@/components/timeline.vue';
import photoData from './components/photoData.vue';
import mapData from './components/mapData.vue';

const ImgPath = '/img/';

//// 照片流数据
const images: WaterfallItem[] = [
  {
    id: 0,
    thumbnail: ImgPath + 'xx.jpg',
    fullSize: ImgPath + 'xx.jpg',
    width: 300,
    height: 300,
    title: 'aa_',
    author: 'xx'
  }, {
    id: 1,
    thumbnail: ImgPath + '20250122-082408-DJI_20250122082408_0144_D-编辑.jpg',
    fullSize: ImgPath + '20250122-082408-DJI_20250122082408_0144_D-编辑.jpg',
    width: 15392,
    height: 3712,
    title: '航拍作品-0144_D',
    author: '无人机摄影师'
  },
  {
    id: 2,
    thumbnail: ImgPath + '20250122-141032-DSC_1696.jpg',
    fullSize: ImgPath + '20250122-141032-DSC_1696.jpg',
    width: 5568,
    height: 3712,
    title: '人像特写-DSC_1696',
    author: '人像摄影师'
  },
  {
    id: 3,
    thumbnail: ImgPath + '20250122-143055-DJI_20250122143055_0226_D.jpg',
    fullSize: ImgPath + '20250122-143055-DJI_20250122143055_0226_D.jpg',
    width: 4800,
    height: 11726,
    title: '全景航拍-0226_D',
    author: '无人机摄影师'
  },
  {
    id: 4,
    thumbnail: ImgPath + '20250122-145121-DJI_20250122145121_0263_D.jpg',
    fullSize: ImgPath + '20250122-145121-DJI_20250122145121_0263_D.jpg',
    width: 6458,
    height: 5203,
    title: '建筑航拍-0263_D',
    author: '无人机摄影师'
  },
  {
    id: 5,
    thumbnail: ImgPath + '20250122-171345-DJI_20250122171345_0302_D.jpg',
    fullSize: ImgPath + '20250122-171345-DJI_20250122171345_0302_D.jpg',
    width: 12192,
    height: 3836,
    title: '城市全景-0302_D',
    author: '无人机摄影师'
  },
  {
    id: 6,
    thumbnail: ImgPath + '20250122-172023-DJI_20250122172023_0310_D.jpg',
    fullSize: ImgPath + '20250122-172023-DJI_20250122172023_0310_D.jpg',
    width: 6601,
    height: 5954,
    title: '自然风光-0310_D',
    author: '无人机摄影师'
  },
  {
    id: 7,
    thumbnail: ImgPath + '20250129-002253-DSC_1803.jpg',
    fullSize: ImgPath + '20250129-002253-DSC_1803.jpg',
    width: 5356,
    height: 3086,
    title: '夜景摄影-DSC_1803',
    author: '人像摄影师'
  },
  {
    id: 8,
    thumbnail: ImgPath + '20250129-002328-DSC_1805.jpg',
    fullSize: ImgPath + '20250129-002328-DSC_1805.jpg',
    width: 5568,
    height: 3514,
    title: '人文纪实-DSC_1805',
    author: '纪实摄影师'
  },
  {
    id: 9,
    thumbnail: ImgPath + '20250202-113253-DSC_1827.jpg',
    fullSize: ImgPath + '20250202-113253-DSC_1827.jpg',
    width: 3712,
    height: 5466,
    title: '建筑特写-DSC_1827',
    author: '建筑摄影师'
  }
];

// 预览弹窗
const previewVisible = ref(false);
const previewImage = ref('');
///打开关闭图片预览卡片
const handlePreviewClose = () => {
  previewVisible.value = false;
};
const openPreview = (item: WaterfallItem) => {
  previewImage.value = item.fullSize;
  previewVisible.value = true;

};



//// 全屏显示
const fullImgShow = ref(false);
const fullImgList = ref<string[]>([]);
const currentIndex = ref(0);
// 打开全屏
const openFullImg = (item: WaterfallItem) => {
  fullImgList.value = images.map(img => img.fullSize);
  currentIndex.value = item.id;
  fullImgShow.value = true;
};
//页面打开时禁止滚动
watch(fullImgShow, (newVal: boolean) => {
  if (newVal === true) {
    document.body.classList.add('body-no-scroll');
  } else {
    document.body.classList.remove('body-no-scroll');
  }
});

////打开数据页才渲染地图
// 定义当前激活标签的标识
const activeTabKey = ref(false) 

// 标签点击事件处理
const handleTabClick = (tab: TabsPaneContext) => {
  if (tab.paneName === 'data') {
    selectMapShop()
  }
}
// 具体业务逻辑
const selectMapShop = () => {
  activeTabKey.value=true;
}
</script>

<style>
.body-no-scroll {
  overflow: hidden;
}

.el-tabs--border-card>.el-tabs__content {
  padding: 0
}

.el-tabs__nav-scroll {
  background-color: rgb(250, 250, 250);
}

.el-tabs--border-card>.el-tabs__header .el-tabs__item {
  color: white;
  background-color: rgb(100, 100, 100);
  border-right-color: white;
}

.el-tabs--border-card>.el-tabs__header .el-tabs__item.is-active {
  background-color: black;
  border-left-color: white;
  border-right-color: white;
  color: var(--el-color-primary);
}
</style>