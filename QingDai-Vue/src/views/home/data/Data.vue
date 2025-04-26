<template>
    <PhotoData />
    <MapData />
</template>

<script setup lang="ts">
import MapData from '@/components/data/MapData.vue';
import PhotoData from '@/components/data/PhotoData.vue';

defineProps<{
    photoType?: number
    meteorologyType?: string
}>();
</script> 