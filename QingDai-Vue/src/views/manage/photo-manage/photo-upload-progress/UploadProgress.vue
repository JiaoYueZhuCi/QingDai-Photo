<template>
  <div class="upload-progress">
    <el-card shadow="hover" class="progress-card">
      <template #header>
        <div class="card-header">
          <h3>照片处理进度</h3> 
        </div>
      </template>
      
      <div class="progress-content">
        <div class="status-message">{{statusMessage}}</div>
        <el-progress 
          :percentage="progress" 
          :status="progressStatus"
          :stroke-width="18"
          :format="format"
        />
        <div class="message-container">
          <p class="message">{{message}}</p>
          <p class="time" v-if="lastUpdateTime">更新时间: {{formatTime(lastUpdateTime)}}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { checkPhotoProcessStatus } from '@/api/photo';

const props = defineProps<{
  messageId: string
}>();

const emit = defineEmits(['close', 'complete']);

// 进度状态
const progress = ref(0);
const statusMessage = ref('等待上传...');
const message = ref('请选择照片并点击提交按钮开始上传');
const lastUpdateTime = ref<number | null>(null);
const isChecking = ref(false);
const checkInterval = ref<number | null>(null);

// 计算进度状态
const progressStatus = computed(() => {
  if (progress.value === 100) return 'success';
  if (statusMessage.value === 'FAILED') return 'exception';
  return '';
});

// 格式化进度显示
const format = (percentage: number) => {
  if (percentage === 100) return '处理完成';
  if (statusMessage.value === 'FAILED') return '处理失败';
  if (percentage === 0 && !isChecking.value) return '等待上传';
  return `${percentage}%`;
};

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp);
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
};

// 检查上传状态
const checkStatus = async () => {
  try {
    if (!props.messageId || props.messageId === 'photo-upload-progress') {
      // 默认状态，不执行检查
      return;
    }

    isChecking.value = true;
    const response = await checkPhotoProcessStatus(props.messageId);
    
    if (response) {
      const data = response;
      progress.value = data.progress || 0;
      statusMessage.value = data.status || 'PROCESSING';
      message.value = data.message || '';
      lastUpdateTime.value = data.updateTime || Date.now();
      
      // 如果已完成或失败，停止检查
      if (data.status === 'COMPLETED') {
        stopChecking();
        emit('complete');
        ElMessage.success('照片处理完成');
      } else if (data.status === 'FAILED') {
        stopChecking();
        emit('complete');
        ElMessage.error(`照片处理失败: ${data.message || '未知错误'}`);
      }
    }
  } catch (error) {
    console.error('检查上传状态失败:', error);
    message.value = '检查状态失败，请稍后再试';
  }
};

// 开始检查进度
const startChecking = () => {
  if (!props.messageId || props.messageId === 'photo-upload-progress') {
    // 默认状态，不启动检查
    resetStatus();
    return;
  }
  
  // 先执行一次
  checkStatus();
  
  // 设置定时检查
  checkInterval.value = window.setInterval(() => {
    checkStatus();
  }, 3000);
  
  isChecking.value = true;
};

// 重置状态
const resetStatus = () => {
  progress.value = 0;
  statusMessage.value = '等待上传...';
  message.value = '请选择照片并点击提交按钮开始上传';
  lastUpdateTime.value = null;
  isChecking.value = false;
};

// 停止检查进度
const stopChecking = () => {
  if (checkInterval.value) {
    clearInterval(checkInterval.value);
    checkInterval.value = null;
  }
  
  isChecking.value = false;
  emit('close');
};

// 监听messageId的变化
watch(() => props.messageId, (newVal) => {
  if (newVal && newVal !== 'photo-upload-progress') {
    startChecking();
  } else {
    resetStatus();
  }
});

// 组件挂载时开始检查
onMounted(() => {
  if (props.messageId && props.messageId !== 'photo-upload-progress') {
    startChecking();
  } else {
    resetStatus();
  }
});

// 组件卸载前停止检查
onBeforeUnmount(() => {
  stopChecking();
});

// 暴露方法
defineExpose({
  startChecking,
  stopChecking,
  resetStatus
});
</script>

<style scoped>
.upload-progress {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.progress-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.card-header h3 {
  margin: 0;
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.progress-content {
  padding: 10px 0;
}

.status-message {
  margin-bottom: 10px;
  font-weight: 500;
  font-size: 16px;
}

.message-container {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.message {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.time {
  margin: 0;
  font-size: 12px;
  color: #909399;
  text-align: right;
}
</style> 