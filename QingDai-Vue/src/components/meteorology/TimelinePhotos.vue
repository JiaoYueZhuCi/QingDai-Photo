<template>
    <div class="timeline-container">
        <div class="timeline-photos">
            <timeline-photo-item 
                v-for="photo in photos" 
                :key="photo.id" 
                :photo="photo"
                @click="handlePhotoClick"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import TimelinePhotoItem from './TimelinePhotoItem.vue';
import type { EnhancedWaterfallItem } from '@/utils/photo';

const props = defineProps<{
    photos: EnhancedWaterfallItem[];
}>();

const emit = defineEmits<{
    (e: 'photoClick', photo: EnhancedWaterfallItem): void;
}>();

const handlePhotoClick = (photo: EnhancedWaterfallItem) => {
    emit('photoClick', photo);
};
</script>

<style scoped>
.timeline-container {
    position: relative;
}

.timeline-photos {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    position: relative;
    z-index: 2;
}

/* 响应式布局 - 移动端垂直布局 */
@media screen and (max-width: 768px) {
    .timeline-photos {
        flex-direction: column;
        align-items: center;
    }
}
</style> 