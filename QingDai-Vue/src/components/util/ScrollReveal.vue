<template>
    <div class="scroll-reveal" ref="containerRef">
        <slot></slot>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import gsap from 'gsap';

const props = defineProps({
    threshold: {
        type: Number,
        default: 50
    },
    duration: {
        type: Number,
        default: 3
    }
});

const containerRef = ref<HTMLElement | null>(null);

const checkCenter = () => {
    const rect = containerRef.value?.getBoundingClientRect();
    if (!rect) return;

    const elementCenter = rect.top + rect.height / 2;
    const viewportCenter = window.innerHeight / 2;
    const distanceFromCenter = Math.abs(elementCenter - viewportCenter);


    if (distanceFromCenter < props.threshold ) {
        // 添加 visible 类
        containerRef.value?.classList.add('visible');
        
        // 动画效果
        gsap.to(containerRef.value, {
            duration: props.duration,
            height: 'auto',
            opacity: 1,
            ease: "expo.out",
            onComplete: () => {
                // 动画完成后移除 will-change
                gsap.set(containerRef.value, {
                    willChange: 'auto'
                });
            }
        });

        window.removeEventListener('scroll', checkCenter);
    }
};

onMounted(() => {
    if (containerRef.value) {
        // 设置初始状态为完全隐藏
        gsap.set(containerRef.value, {
            height: 0,
            overflow: 'hidden',
            opacity: 0,
            willChange: 'height, opacity'
        });

        window.addEventListener('scroll', checkCenter);
        checkCenter();
    }
});

onBeforeUnmount(() => {
    window.removeEventListener('scroll', checkCenter);
});
</script>

<style scoped>
.scroll-reveal {
    overflow: hidden;
    transform-origin: center;
    position: relative;
    backface-visibility: hidden;
    -webkit-backface-visibility: hidden;
    transform: translateZ(0);
    -webkit-transform: translateZ(0);
    visibility: hidden;
}

.scroll-reveal.visible {
    visibility: visible;
}
</style> 