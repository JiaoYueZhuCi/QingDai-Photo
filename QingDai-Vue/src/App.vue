<template>
  <el-backtop :right="50" :bottom="50" />

  <Introduce />

  <!-- <tabs /> -->
  <el-tabs type="border-card" @tab-click="handleTabClick">
    <el-tab-pane label="精选">
      <waterfall :rowHeightMax="300" :rowHeightMin="150" :gap="10" @open-preview="openPreview"
        @open-full-preview="openFullImg" />
    </el-tab-pane>
    <el-tab-pane label="照片">
      <!-- <waterfall :rowHeightMax="300" :rowHeightMin="150" :gap="10" @open-preview="openPreview"
        @open-full-preview="openFullImg" /> -->
    </el-tab-pane>
    <el-tab-pane label="时间轴">
      <timeline />
    </el-tab-pane>
    <el-tab-pane label="数据" name="data">
      <photoData />
      <mapData :is-active="activeTabKey" />
    </el-tab-pane>
  </el-tabs>


  <!-- <el-dialog v-model="previewVisible" :before-close="handlePreviewClose" width="80%">
    <img :src="previewImage" alt="预览图片" class="preview-image" style="width: 100%; height: auto;" />
  </el-dialog> -->

  <el-image-viewer :teleported="true" v-if="fullImgShow" :url-list="fullImgList" :initial-index="currentIndex"
    @close="fullImgShow = false" />



</template>

<script setup lang="ts">
import { watch, ref, onMounted } from 'vue';
import axios from 'axios'; // 引入 axios
import type { WaterfallItem } from './types';
import { ElImageViewer } from 'element-plus';
import type { TabsPaneContext } from 'element-plus';
import waterfall from '@/components/waterfall.vue';
import Introduce from '@/components/introduce.vue';
import timeline from '@/components/timeline.vue';
import photoData from '@/components/photoData.vue';
import mapData from '@/components/mapData.vue';


// 预览弹窗
const previewVisible = ref(false);
const previewImage = ref(0);
///打开关闭图片预览卡片
const handlePreviewClose = () => {
  previewVisible.value = false;
};
const openPreview = (item: WaterfallItem) => {
  previewImage.value = item.id; // 修改为使用存储路径
  previewVisible.value = true;

};

//// 全屏显示
const fullImgShow = ref(false);
const fullImgList = ref<string[]>([]);
const currentIndex = ref(0);
// 打开全屏
const openFullImg = (item: WaterfallItem) => {
  //!!!!!
  // fullImgList.value = images.value.map(img => item.id); // 修改为使用存储路径
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
  activeTabKey.value = true;
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