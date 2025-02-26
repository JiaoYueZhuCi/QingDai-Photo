<template>
    <!-- 触发按钮 -->
    <el-button @click="showViewer = true">预览图片</el-button>
  
    <!-- 图片预览组件 -->
    <el-image-viewer
      v-if="showViewer"
      :url-list="imageList"
      :initial-index="currentIndex"
      :z-index="3000"
      teleported
      @close="showViewer = false"
    />
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { ElImageViewer } from 'element-plus';
  
  // 图片列表（支持多图切换）
  const imageList = ref([
    'https://fuss10.elemecdn.com/1/8e/aeffeb4de74e2fde4bd74fc7b4486jpeg.jpeg',
    'https://fuss10.elemecdn.com/8/27/f01c15bb73e1ef3793e64e6b7bbccjpeg.jpeg'
  ]);
  
  // 控制显隐 & 初始索引
  const showViewer = ref(false);
  const currentIndex = ref(0); // 默认从第一张开始预览
  </script>