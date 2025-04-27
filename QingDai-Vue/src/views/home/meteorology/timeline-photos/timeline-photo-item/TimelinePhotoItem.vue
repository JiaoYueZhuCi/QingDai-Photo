<template>
    <div class="timeline-item">
        <div class="timeline-dot"></div>
        <div class="time-label">{{ photo.time }}</div>
        <div class="timeline-card">
            <div class="photo-container" @click="handleClick">
                <img v-if="photo.compressedSrc" :src="photo.compressedSrc" alt="照片" />
                <div v-else class="photo-placeholder"></div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { EnhancedWaterfallItem } from '@/utils/photo';

const props = defineProps<{
    photo: EnhancedWaterfallItem;
}>();

const emit = defineEmits(['click']);

const handleClick = () => {
    emit('click', props.photo);
};
</script>

<style scoped>
.timeline-item {
    position: relative;
    width: 240px;
    margin: 15px;
}

.timeline-dot {
    position: absolute;
    width: 20px;
    height: 20px;
    background-color: var(--qd-color-border);
    border-radius: 50%;
    left:25px;
    top: -10px;
    transform: translateX(-50%);
    z-index: 3;
}

.time-label {
    position: absolute;
    top: -11px;
    font-size: 15px;
    color: var(--qd-color-primary);
    z-index: 3;
    background-color: var(--qd-color-bg);
    padding: 2px 6px;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    white-space: nowrap;
    transform: translateX(-50%);
    left: 50%;
}

.timeline-card {
    padding: 5px;
    background-color: var(--qd-color-bg);
    border-radius: 6px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    margin-top: 20px;
}

.timeline-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.photo-container {
    width: 100%;
    overflow: hidden;
    cursor: pointer;
}

.photo-container img {
    width: 100%;
    height: 230px;
    object-fit: contain;
    transition: transform 0.5s ease;
    aspect-ratio: 4/3;
}

.photo-container img:hover {
    transform: scale(1.05);
}

.photo-placeholder {
    width: 100%;
    height: 200px;
    background-color: var(--qd-color-bg-light);
    border-radius: 4px;
}

/* 响应式布局 - 移动端垂直布局 */
@media screen and (max-width: 768px) {
    .timeline-item {
        width: 320px;
    }

    .timeline-dot {
        left: 50%;
        top: 10px;
    }

    .time-label {
        top: 10px;
        left: calc(50% + 15px);
        transform: none;
    }
}
</style> 