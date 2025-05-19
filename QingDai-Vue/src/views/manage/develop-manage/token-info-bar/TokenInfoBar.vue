<template>
  <div class="token-info-bar">
    <div class="token-display">
      <span class="info-label">当前用户Token：</span>
      <span class="token-text">{{ token }}</span>
      <el-button type="primary" @click="copyToken" class="copy-btn">
        复制
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

const token = ref(localStorage.getItem('token') || '未获取到Token');

const copyToken = () => {
  navigator.clipboard.writeText(token.value).then(() => {
    ElMessage.success('Token已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
};
</script>

<style scoped>
.token-info-bar {
  background: var(--qd-color-bg-light);
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 6px;
  border: 1px solid var(--qd-color-border);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.token-display {
  display: flex;
  align-items: center;
  font-size: 16px;
  gap: 10px;
}

.info-label {
  color: var(--qd-color-text-secondary);
  white-space: nowrap;
}

.token-text {
  flex: 1;
  color: var(--qd-color-primary);
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: monospace;
}

.copy-btn {
  flex-shrink: 0;
}

/* 媒体查询适配移动端 */
@media (max-width: 768px) {
  .token-display {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .token-text {
    width: 100%;
  }
  
  .copy-btn {
    align-self: flex-end;
  }
}
</style> 