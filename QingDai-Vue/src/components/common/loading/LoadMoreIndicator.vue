<template>
    <div class="load-more-indicator">
        <div class="loading-spinner"></div>
        <span class="loading-text">{{ props.text }}</span>
    </div>
</template>

<script setup lang="ts">
// 属性定义
const props = defineProps({
    text: {
        type: String,
        default: '加载更多...'
    }
});
</script>

<style scoped>
.load-more-indicator {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 15px 0;
}

.loading-spinner {
    width: 24px;
    height: 24px;
    border: 2px solid transparent;
    border-top-color: var(--el-color-primary, #409eff);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-bottom: 8px;
}

.loading-text {
    font-size: 14px;
    color: var(--el-color-primary, #409eff);
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>