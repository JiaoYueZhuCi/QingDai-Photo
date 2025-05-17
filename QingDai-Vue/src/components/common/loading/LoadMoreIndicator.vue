<template>
    <div class="load-more-indicator">
        <div ref="spinnerRef" class="loading-spinner"></div>
        <span class="loading-text">{{ props.text }}</span>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import gsap from 'gsap';

// 属性定义
const props = defineProps({
    text: {
        type: String,
        default: '加载更多...'
    }
});

const spinnerRef = ref<HTMLElement | null>(null);
let spinnerAnimation: gsap.core.Tween | null = null;

onMounted(() => {
    if (spinnerRef.value) {
        // 创建旋转动画
        spinnerAnimation = gsap.to(spinnerRef.value, {
            rotation: 360,
            duration: 0.8,
            repeat: -1,
            ease: 'none',
            transformOrigin: 'center center'
        });
    }
});

onBeforeUnmount(() => {
    // 清理动画
    if (spinnerAnimation) {
        spinnerAnimation.kill();
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
    border: 2px solid rgba(0, 0, 0, 0.1);
    border-top-color: var(--el-color-primary, #409eff);
    border-radius: 50%;
    margin-bottom: 8px;
}

.loading-text {
    font-size: 14px;
    color: var(--el-color-primary, #409eff);
}
</style>