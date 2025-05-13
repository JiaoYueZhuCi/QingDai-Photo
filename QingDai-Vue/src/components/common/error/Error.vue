<template>
    <div class="error-page">
        <div class="error-container">
            <div class="error-icon">
                <el-icon><CircleCloseFilled /></el-icon>
            </div>
            <h1 class="error-title">组件加载错误</h1>
            <p class="error-message">抱歉，组件加载失败，请稍后重试或联系管理员</p>
            <p class="error-detail" v-if="errorMessage">
                错误详情：{{ errorMessage }}
            </p>
            <el-button type="primary" @click="reload" class="action-button">
                重新加载
            </el-button>
            <el-button @click="goBack" class="action-button">
                返回上一页
            </el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps } from 'vue';
import { useRouter } from 'vue-router';
import { CircleCloseFilled } from '@element-plus/icons-vue';

const props = defineProps({
    errorMessage: {
        type: String,
        default: ''
    }
});

const router = useRouter();

// 重新加载页面
const reload = () => {
    window.location.reload();
};

// 返回上一页
const goBack = () => {
    router.go(-1);
};
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
    animation: fadeIn 0.8s ease-out;
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

.error-icon {
    font-size: 80px;
    color: #f56c6c;
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
    color: #f56c6c;
    margin-bottom: 16px;
    font-weight: 600;
}

.error-message {
    font-size: 18px;
    color: var(--qd-color-primary-light-3);
    margin-bottom: 20px;
}

.error-detail {
    font-size: 14px;
    color: #909399;
    margin-bottom: 30px;
    padding: 10px;
    background-color: #f8f8f8;
    border-radius: 4px;
    word-break: break-word;
    text-align: left;
    max-height: 150px;
    overflow-y: auto;
}

.action-button {
    margin: 0 10px;
    padding: 12px 24px;
    font-size: 16px;
    transition: all 0.3s;
}

.action-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}
</style>