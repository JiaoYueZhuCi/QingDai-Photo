<template>
    <PhotoData />
    <MapData />
</template>

<script setup lang="ts">
import MapData from '@/views/home/MapData.vue';
import PhotoData from '@/views/home/PhotoData.vue';

// 定义props，即使不使用也要定义以避免警告
defineProps<{
    photoType?: number
}>();
</script> 