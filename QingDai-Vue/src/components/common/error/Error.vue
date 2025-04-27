<template>
    <div class="error-page">
        <div class="error-container">
            <div class="error-icon">
                <el-icon><WarningFilled /></el-icon>
            </div>
            <h1 class="error-title">页面不存在</h1>
            <p class="error-message">抱歉，您访问的页面不存在或已被移除</p>
            <div class="error-timer">
                <div class="timer-circle">
                    <svg viewBox="0 0 100 100">
                        <circle class="timer-circle-bg" cx="50" cy="50" r="45"></circle>
                        <circle class="timer-circle-progress" cx="50" cy="50" r="45" 
                            :style="{ strokeDashoffset: dashOffset }"></circle>
                    </svg>
                    <div class="timer-count">{{ countdown }}</div>
                </div>
            </div>
            <p class="redirect-text">{{ countdown }}秒后自动返回首页</p>
            <el-button type="primary" @click="goHome" class="home-button">
                立即返回首页
            </el-button>
            <div class="error-box">
                <div class="error-box-lid"></div>
                <div class="error-box-bottom"></div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { WarningFilled } from '@element-plus/icons-vue';

const router = useRouter();
const countdown = ref(5);
let timer: number | null = null;

// 计算圆环进度
const dashOffset = computed(() => {
    // 圆的周长约为 2πr = 2 * 3.14 * 45 ≈ 283
    const circumference = 2 * Math.PI * 45;
    // 计算当前进度
    const progress = countdown.value / 5;
    // 计算偏移量
    return circumference * (1 - progress);
});

// 跳转到首页
const goHome = () => {
    if (timer) {
        clearInterval(timer);
        timer = null;
    }
    router.push('/');
};

onMounted(() => {
    // 设置倒计时
    timer = window.setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
            if (timer) {
                clearInterval(timer);
                timer = null;
            }
            goHome();
        }
    }, 1000);
});

onUnmounted(() => {
    // 清除定时器
    if (timer) {
        clearInterval(timer);
        timer = null;
    }
});
</script>

<style scoped>
.error-page {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: linear-gradient(135deg, var(--qd-color-primary-light-9) 0%, var(--qd-color-primary-light-8) 100%);
    position: relative;
    overflow: hidden;
}

.error-container {
    max-width: 600px;
    margin: 0 auto;
    padding: 40px;
    text-align: center;
    background-color: white;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    position: relative;
    z-index: 2;
    animation: fadeIn 0.8s ease-out, float 6s ease-in-out infinite;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes float {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-10px);
    }
}

.error-icon {
    font-size: 80px;
    color: var(--qd-color-primary);
    margin-bottom: 20px;
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% {
        transform: scale(1);
    }
    50% {
        transform: scale(1.1);
    }
    100% {
        transform: scale(1);
    }
}

.error-title {
    font-size: 36px;
    color: var(--qd-color-primary);
    margin-bottom: 16px;
    font-weight: 600;
}

.error-message {
    font-size: 18px;
    color: var(--qd-color-primary-light-3);
    margin-bottom: 30px;
}

.error-timer {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.timer-circle {
    width: 100px;
    height: 100px;
    position: relative;
}

.timer-circle svg {
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
}

.timer-circle-bg {
    fill: none;
    stroke: var(--qd-color-primary-light-8);
    stroke-width: 8;
}

.timer-circle-progress {
    fill: none;
    stroke: var(--qd-color-primary);
    stroke-width: 8;
    stroke-dasharray: 283;
    transition: stroke-dashoffset 1s linear;
}

.timer-count {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32px;
    font-weight: bold;
    color: var(--qd-color-primary);
}

.redirect-text {
    font-size: 16px;
    color: var(--qd-color-primary-light-4);
    margin-bottom: 24px;
}

.home-button {
    padding: 12px 24px;
    font-size: 16px;
    transition: all 0.3s;
}

.home-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

/* 装饰性元素 */
.error-box {
    position: absolute;
    bottom: -50px;
    left: 50%;
    transform: translateX(-50%);
    perspective: 600px;
}

.error-box-lid {
    width: 200px;
    height: 20px;
    background-color: var(--qd-color-primary-light-5);
    transform-origin: bottom;
    animation: openLid 4s ease-in-out infinite;
    position: relative;
    z-index: 2;
    border-radius: 4px 4px 0 0;
}

.error-box-bottom {
    width: 200px;
    height: 50px;
    background: linear-gradient(to bottom, var(--qd-color-primary-light-6), var(--qd-color-primary-light-4));
    transform-origin: bottom;
    position: relative;
    z-index: 1;
    border-radius: 0 0 4px 4px;
}

@keyframes openLid {
    0%, 30%, 70%, 100% {
        transform: rotateX(0);
    }
    40%, 60% {
        transform: rotateX(-40deg);
    }
}

/* 响应式调整 */
@media (max-width: 768px) {
    .error-container {
        max-width: 90%;
        padding: 30px;
    }

    .error-icon {
        font-size: 60px;
    }

    .error-title {
        font-size: 30px;
    }

    .error-message {
        font-size: 16px;
    }

    .timer-circle {
        width: 80px;
        height: 80px;
    }

    .timer-count {
        font-size: 28px;
    }
}
</style> 