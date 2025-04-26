<template>
    <div class="sort-controls">
        <el-button class="sort-btn" @click="toggleSort" :style="{ opacity: opacity }">
            {{ isAscending ? '切换为最新优先' : '切换为最早优先' }}
        </el-button>
    </div>
</template>

<script setup lang="ts">
const props = defineProps<{
    isAscending: boolean;
    opacity: number;
}>();

const emit = defineEmits(['toggle']);

const toggleSort = () => {
    emit('toggle');
};
</script>

<style scoped>
.sort-controls {
    text-align: center;
}

.sort-btn {
    background-color: var(--qd-color-text-regular);
    color: var(--qd-color-bg);
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    transition: all 0.3s ease;
}

.sort-btn:hover {
    background-color: var(--qd-color-primary-light-4);
    transform: translateY(-2px);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    opacity: 1 !important;
}

.sort-btn:active {
    transform: translateY(0);
}
</style> 