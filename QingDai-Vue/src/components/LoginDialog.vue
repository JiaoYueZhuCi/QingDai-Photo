<template>
  <el-dialog v-model="visible" title="登录" width="380px" align-center :before-close="handleClose">
    <el-form ref="loginForm" :model="form" :rules="rules" label-width="0" class="login-form">
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="用户名" prefix-icon="el-icon-user" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="el-icon-lock"
          show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm" style="width: 100%">登录</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/user'
import type { LoginResponse } from '@/api/user'

const router = useRouter()

const visible = ref(false)
const loginForm = ref<FormInstance>()

// 定义表单数据
const form = reactive({
    username: '',
    password: ''
})

const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
}

const submitForm = async () => {
  try {
    await loginForm.value?.validate()
    const response = await login({
      username: form.username,
      password: form.password
    });

    console.log('登录响应:', response);

    // 检查响应是否有token，支持多种可能的结构
    let token = null;
    if (response && response.token) {
      // 标准Response: { token: 'xxx', userInfo: {...} }
      token = response.token;
    } else if (response && response.headers && response.headers.authorization) {
      // 后端可能通过headers返回: response.headers.authorization
      token = response.headers.authorization;
    } else if (response && response.data && response.data.token) {
      // 可能后端返回套嵌结构: { data: { token: 'xxx' } }
      token = response.data.token;
    }

    if (token) {
      // 如果是Bearer token，提取token部分
      if (typeof token === 'string' && token.startsWith('Bearer ')) {
        token = token.substring(7);
      }
      localStorage.setItem('token', token);
      ElMessage.success('登录成功');
      visible.value = false;
      router.push('/home')
    } else {
      console.error('无效的响应数据:', response);
      ElMessage.error('登录失败：无效的响应数据');
    }
  } catch (error) {
    console.error('登录失败:', error);
    ElMessage.error('登录失败，请检查用户名和密码');
  }
}

const handleClose = (done: () => void) => {
  done();
};

defineExpose({
  visible
})
</script>

<style scoped>
.login-form {
  padding: 0 20px;
}
</style>