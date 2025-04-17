<template>
  <div class="back">
    <div class="loginBox" align-center>
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="el-icon-user" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="el-icon-lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()

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
      router.push('/manage')
    } else {
      console.error('无效的响应数据:', response);
      ElMessage.error('登录失败：无效的响应数据');
    }
  } catch (error) {
    console.error('登录失败:', error);
    ElMessage.error('登录失败，请检查用户名和密码');
  }
}


</script>

<style scoped>
.login-form {
  width: 90%; 
  max-width: 400px;
  margin: auto;
  padding: 20px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.el-form-item {
  margin-bottom: 18px;
}

.el-button {
  margin-top: 10px;
  transition: all 0.3s ease;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.loginBox {
  opacity: 0.9; 
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.back {
  background: url('/public/img/introduce/background.jpg') no-repeat center center;
  background-size: cover;
  height: 100vh;
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .login-form {
    width: 85%;
    padding: 15px;
  }
  
  .el-form-item {
    margin-bottom: 15px;
  }
  
  .el-input {
    font-size: 14px;
  }
  
  .el-button {
    font-size: 14px;
  }

  .back {
    background-position: -720px center;
  }
}
</style>