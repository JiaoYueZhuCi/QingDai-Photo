<template>
    <PhotoData />
    <MapData />
</template>

<script setup lang="ts">
import MapData from '@/views/home/MapData.vue';
import PhotoData from '@/views/home/PhotoData.vue';

defineProps<{
    photoType?: number
    meteorologyType?: string
}>();
</script> 