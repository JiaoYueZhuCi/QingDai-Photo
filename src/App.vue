<template>
  <Waterfall :images="images" :rowHeight="200" @open-preview="openPreview" @open-full-preview="openFullImg" />

  <el-dialog v-model="previewVisible" :before-close="handlePreviewClose" width="80%">
    <img :src="previewImage" alt="预览图片" class="preview-image" style="width: 100%; height: auto;" />
  </el-dialog>

  <!-- <div class="fullImg"> -->
    <el-image-viewer class="fullImg" :teleported="true" v-if="fullImgShow" :url-list="fullImgList"
      :initial-index="currentIndex" @close="fullImgShow = false" />
  <!-- </div> -->



</template>

<script setup lang="ts">
import { defineComponent, ref } from 'vue';
import type { WaterfallItem } from './types';
import Waterfall from '@/components/Waterfall.vue';
import { ElImageViewer } from 'element-plus';

const ImgPath = '/img/';

// 照片流数据
const images: WaterfallItem[] = [
  {
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

// 预览弹窗相关状态
const previewVisible = ref(false);
const previewImage = ref('');

///打开关闭图片预览卡片
const handlePreviewClose = () => {
  previewVisible.value = false;
};

const openPreview = (item: WaterfallItem) => {
  console.log('打开图片地址为:::::', item.fullSize);
  previewImage.value = item.fullSize;
  previewVisible.value = true;

};



//// 全屏预览相关状态
// 定义插槽上下文类型
interface ImageViewerSlotProps {
  image: string;
}

const fullImgShow = ref(false);
const fullImgList = ref<string[]>([]);
const currentIndex = ref(0);
// 打开全屏
const openFullImg = (item: WaterfallItem) => {
  console.log('打开图片地址为:::::', item.fullSize);
  fullImgList.value = images.map(img => img.fullSize);
  console.log("fullImgList.value:" + fullImgList.value);
  currentIndex.value = item.id - 1;
  console.log("currentIndex.value:" + currentIndex.value);
  fullImgShow.value = true;
};
</script>

<style scoped>
.preview-image {
  width: 100%;
  height: auto;
}


.fullImg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}
</style>