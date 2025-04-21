<template>
    <PhotoData />
    <MapData />
</template>

<script setup lang="ts">
import MapData from '@/views/home/data/map-data/MapData.vue';
import PhotoData from '@/views/home/data/photo-data/PhotoData.vue';

defineProps<{
    photoType?: number
    meteorologyType?: string
}>();
</script> 