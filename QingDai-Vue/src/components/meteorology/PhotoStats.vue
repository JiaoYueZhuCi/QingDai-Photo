<template>
    <div class="stats-container">
        <div class="photo-stats">
            现已记录{{ typeName }}：<span class="photo-count">{{ photoCount }}</span> 次
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    typeName: string;
    photoCount: number;
}>();
</script>

<style scoped>
.stats-container {
    text-align: center;
    padding: 10px 0 0 0;
    color: var(--qd-color-text-primary) !important;
    font-size: 18px;
}

.photo-count {
    font-weight: bold;
    font-size: 22px;
    margin: 0 5px;
    color: var(--qd-color-primary) !important;
}
</style> 