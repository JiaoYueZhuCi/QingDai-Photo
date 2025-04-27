<template>
  <el-dialog 
    v-model="visible" 
    title="验证码" 
    width="400px" 
    :close-on-click-modal="false"
    :before-close="handleClose"
    center
    align-center
    class="verification-dialog"
  >
    <div class="verification-code-container">
      <div class="verification-code-wrapper">
        <div class="captcha-image-container" @click="refreshCaptcha">
          <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="captcha-image" />
          <div v-else class="captcha-loading">加载中...</div>
          <el-icon class="refresh-icon"><Refresh /></el-icon>
        </div>
        <el-input
          v-model="captchaInput"
          placeholder="请输入验证码"
          class="captcha-input"
          maxlength="4"
          @keyup.enter="handleVerify"
        />
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button :disabled="!captchaInput || verifying" @click="handleVerify" type="primary">
          {{ verifying ? '验证中...' : '验证' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, defineExpose, watch } from 'vue';
import { ElInput, ElButton, ElIcon, ElMessage, ElDialog } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import { getCaptchaImage, verifyCaptcha } from '@/api/kaptcha';

// 定义props
const props = defineProps({
  // 是否在验证成功后自动关闭对话框
  autoClose: {
    type: Boolean,
    default: true
  },
  // 使用modelValue代替dialogVisible
  modelValue: {
    type: Boolean,
    default: false
  }
});

// 定义事件
const emit = defineEmits<{
  (e: 'verified', result: boolean): void;
  (e: 'close'): void;
  (e: 'update:modelValue', value: boolean): void;
}>();

// 状态变量
const visible = ref(props.modelValue);
const captchaImage = ref<string>('');
const captchaInput = ref<string>('');
const verifying = ref<boolean>(false);

// 监听modelValue属性变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal;
  if (newVal) {
    // 当对话框打开时，刷新验证码
    resetVerification();
    fetchCaptcha();
  }
});

// 监听内部visible值的变化，同步到modelValue
watch(() => visible.value, (newVal) => {
  emit('update:modelValue', newVal);
  if (!newVal) {
    emit('close');
  }
});

// 获取验证码
const fetchCaptcha = async () => {
  try {
    captchaImage.value = await getCaptchaImage();
  } catch (error) {
    ElMessage.error('获取验证码失败，请稍后重试');
    console.error('获取验证码失败:', error);
  }
};

// 刷新验证码
const refreshCaptcha = () => {
  captchaInput.value = '';
  fetchCaptcha();
};

// 重置验证状态
const resetVerification = () => {
  captchaInput.value = '';
  verifying.value = false;
};

// 验证验证码
const handleVerify = async () => {
  if (!captchaInput.value) {
    ElMessage.warning('请输入验证码');
    return;
  }

  verifying.value = true;
  
  try {
    await verifyCaptcha(captchaInput.value);
    
    // 验证成功
    ElMessage.success('验证成功');
    emit('verified', true);
    
    if (props.autoClose) {
      // 自动关闭对话框
      setTimeout(() => {
        visible.value = false;
      }, 1000);
    }
  } catch (error: any) {
    // 验证失败，处理特定错误
    if (error.response) {
      if (error.response.status === 401) {
        ElMessage.error('验证码已过期，请刷新');
      } else if (error.response.status === 400) {
        ElMessage.error('验证码错误，请重新输入');
      } else {
        ElMessage.error('验证失败，请重试');
      }
    } else {
      ElMessage.error('验证失败，请重试');
    }
    
    emit('verified', false);
    refreshCaptcha();
  } finally {
    verifying.value = false;
  }
};

// 处理对话框关闭
const handleClose = () => {
  visible.value = false;
};

// 组件挂载时初始化
onMounted(() => {
  visible.value = props.modelValue;
  if (visible.value) {
    fetchCaptcha();
  }
});

// 对外暴露方法
defineExpose({
  // 保留show方法用于兼容
  show: () => {
    resetVerification();
    visible.value = true;
    fetchCaptcha();
  }
});
</script>

<style scoped>
/* 对话框居中样式 */
:deep(.verification-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-dialog) {
  margin-top: 0 !important;
  margin-bottom: 0;
}

/* 其他样式保持不变 */
.verification-code-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.verification-code-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-image-container {
  position: relative;
  width: 110px;
  height: 40px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.refresh-icon {
  position: absolute;
  right: 5px;
  bottom: 5px;
  font-size: 16px;
  color: rgba(0, 0, 0, 0.5);
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  padding: 2px;
  transition: all 0.3s;
}

.captcha-image-container:hover .refresh-icon {
  transform: rotate(90deg);
}

.captcha-input {
  flex: 1;
}
</style>

