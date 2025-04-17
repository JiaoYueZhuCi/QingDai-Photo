<template>
  <div class="user-info-container">
    <el-card class="user-info-card">
      <template #header>
        <div class="card-header">
          <span>个人信息设置</span>
        </div>
      </template>
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
        <el-form-item label="新用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入新用户名"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="userForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { updateUserInfo } from '@/api/user';

const userFormRef = ref();

const userForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
});

const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else {
    if (userForm.confirmPassword !== '') {
      userFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== userForm.newPassword) {
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

const submitForm = async () => {
  if (!userFormRef.value) return;
  
  await userFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await updateUserInfo({
          username: userForm.username,
          password: userForm.newPassword
        });
        ElMessage.success('修改成功');
        // 清空表单
        userForm.username = '';
        userForm.newPassword = '';
        userForm.confirmPassword = '';
      } catch (error) {
        ElMessage.error('修改失败');
      }
    }
  });
};
</script>

<style scoped>
.user-info-container {
  padding: 20px;
}

.user-info-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 