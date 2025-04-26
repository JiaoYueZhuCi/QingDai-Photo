<template>
  <div class="user-info-container">
    <!-- 管理员信息卡片 -->
    <el-card class="user-info-card">
      <template #header>
        <div class="card-header">
          <span>管理员信息设置</span>
        </div>
      </template>
      <el-form :model="adminForm" :rules="rules" ref="adminFormRef" label-width="100px">
        <el-form-item label="新用户名" prop="username">
          <el-input v-model="adminForm.username" placeholder="请输入新用户名"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="adminForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="adminForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitAdminForm">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 访客信息卡片 -->
    <el-card class="user-info-card">
      <template #header>
        <div class="card-header">
          <span>访客信息设置</span>
        </div>
      </template>
      <el-form :model="guestForm" :rules="rules" ref="guestFormRef" label-width="100px">
        <el-form-item label="新用户名" prop="username">
          <el-input v-model="guestForm.username" placeholder="请输入新用户名"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="guestForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="guestForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitGuestForm">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { updateUserInfo } from '@/api/user';

const adminFormRef = ref();
const guestFormRef = ref();

// 管理员表单数据
const adminForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
});

// 访客表单数据
const guestForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
});

const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else {
    // 根据表单类型验证对应的确认密码字段
    if (rule.field.includes('admin') && adminForm.confirmPassword !== '') {
      adminFormRef.value.validateField('confirmPassword');
    } else if (rule.field.includes('guest') && guestForm.confirmPassword !== '') {
      guestFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (
    (rule.field.includes('admin') && value !== adminForm.newPassword) ||
    (rule.field.includes('guest') && value !== guestForm.newPassword)
  ) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
};

// 提交管理员表单
const submitAdminForm = async () => {
  if (!adminFormRef.value) return;
  
  await adminFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await updateUserInfo('1', {
          username: adminForm.username,
          password: adminForm.newPassword
        });
        ElMessage.success('管理员信息修改成功');
        // 清空表单
        adminForm.username = '';
        adminForm.newPassword = '';
        adminForm.confirmPassword = '';
      } catch (error) {
        ElMessage.error('修改失败');
      }
    }
  });
};

// 提交访客表单
const submitGuestForm = async () => {
  if (!guestFormRef.value) return;
  
  await guestFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await updateUserInfo('2', {
          username: guestForm.username,
          password: guestForm.newPassword
        });
        ElMessage.success('访客信息修改成功');
        // 清空表单
        guestForm.username = '';
        guestForm.newPassword = '';
        guestForm.confirmPassword = '';
      } catch (error) {
        ElMessage.error('修改失败');
      }
    }
  });
};
</script>

<style scoped>
.user-info-container {
  background-color: var(--qd-color-bg-dark);
  padding: 20px;
  /* min-height: 80vh; */
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.user-info-card {
  height: 100%;
  transition: all 0.3s;
}

.user-info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .user-info-container {
    grid-template-columns: 1fr;
  }
}
</style> 