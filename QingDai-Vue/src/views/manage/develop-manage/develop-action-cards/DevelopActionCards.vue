<template>
  <div class="develop-container">
    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>token查验用户信息</span>
        </div>
      </template>
      <div class="card-content">
        <div class="action-group">
          <el-input v-model="debugToken" placeholder="请输入token进行查验" class="debug-input" />
          <el-button type="primary" @click="handleDebugUserInfo">
            开始检测
          </el-button>
        </div>
        <div class="description">
          查验token的用户信息
        </div>
        <div class="result-summary">
          <template v-if="userInfoResult">
            <h3>用户基本信息</h3>
            <p>id: {{ userInfoResult.user.id }}</p>
            <p>用户名: {{ userInfoResult.user.username }}</p>
            <p>密码: {{ userInfoResult.user.password }}</p>
            <p>状态: {{ userInfoResult.user.status === 1 ? '正常' : '禁用' }}</p>
            <p>创建时间: {{ userInfoResult.user.createdTime }}</p>
            <p>更新时间: {{ userInfoResult.user.updatedTime }}</p>

            <h3>用户角色</h3>
            <div class="role-list">
              <el-tag v-for="(role, index) in userInfoResult.roles" :key="index" type="success" class="role-tag">
                {{ role }}
              </el-tag>
            </div>

            <h3>用户权限</h3>
            <div class="permission-list">
              <el-tag v-for="(permission, index) in userInfoResult.permissions" :key="index" type="info"
                class="permission-tag">
                {{ permission }}
              </el-tag>
            </div>
          </template>
          <template v-else>
            <h3>用户基本信息</h3>
            <p>用户ID: <span class="pending-data">待检测</span></p>
            <p>用户名: <span class="pending-data">待检测</span></p>
            <p>密码: <span class="pending-data">待检测</span></p>
            <p>状态: <span class="pending-data">待检测</span></p>
            <p>创建时间: <span class="pending-data">待检测</span></p>
            <p>更新时间: <span class="pending-data">待检测</span></p>

            <h3>用户角色</h3>
            <p><span class="pending-data">待检测</span></p>

            <h3>用户权限</h3>
            <p><span class="pending-data">待检测</span></p>
          </template>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getUserInfo } from '@/api/user';

// Token查验
const debugToken = ref('');
const userInfoResult = ref<any | null>(null);

// Token查验
const handleDebugUserInfo = async () => {
  try {
    if (!debugToken.value) {
      ElMessage.warning('请输入token');
      return;
    }

    // 如果用户输入的token包含Bearer前缀，则去除它
    let tokenToUse = debugToken.value;
    if (tokenToUse.startsWith('Bearer ')) {
      tokenToUse = tokenToUse.substring(7);
    }

    const response = await getUserInfo(tokenToUse);
    userInfoResult.value = response;
    ElMessage.success('获取用户信息成功');
    console.log('用户信息:', response);
  } catch (error: any) {
    userInfoResult.value = null;
    if (error.response && error.response.status === 401) {
      ElMessage.error('Token验证失败：无效的token');
    } else {
      ElMessage.error('获取用户信息失败：' + (error as Error).message);
    }
  }
};
</script>

<style scoped>
.develop-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.develop-card {
  border-radius: 6px;
  background-color: var(--qd-color-bg-light);
  border: 1px solid var(--qd-color-border);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__body) {
  padding: 0 !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: var(--qd-color-text-primary);
}

.card-content {
  padding: 10px;
  color: var(--qd-color-text-regular);
}

.description {
  margin-top: 10px;
  font-size: 14px;
  color: var(--qd-color-text-secondary);
}

.action-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.debug-input {
  width: 100%;
}

.result-summary {
  margin-top: 15px;
  font-size: 14px;
  color: var(--qd-color-text-primary);
}

.pending-data {
  color: var(--qd-color-text-secondary);
  font-style: italic;
}

.role-tag,
.permission-tag {
  margin-right: 6px;
  margin-bottom: 6px;
  font-size: 12px;
}

.role-list,
.permission-list {
  display: flex;
  flex-wrap: wrap;
  margin-top: 5px;
  margin-bottom: 10px;
}

.result-summary p{
  margin: 0;
}

.result-summary h3{
  margin: 3px 0;
}

/* 媒体查询适配移动端 */
@media (max-width: 768px) {
  .develop-container {
    grid-template-columns: 1fr;
  }
  
  .action-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style> 