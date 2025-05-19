<template>
  <div class="back" :style="{ backgroundImage: `url(${backgroundUrl})` }">
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { login } from '@/api/user'
import { getBackground } from '@/api/user'

const router = useRouter()
const loginForm = ref<FormInstance>()
const backgroundUrl = ref('')

// 获取背景图
const fetchBackground = async () => {
    try {
        const response = await getBackground();
        backgroundUrl.value = URL.createObjectURL(response.data);
    } catch (error) {
        console.error('获取背景图失败:', error);
    }
}

onMounted(() => {
    fetchBackground();
})

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

    // 从响应中获取token
    const token = response.data?.token;
    if (token) {
      localStorage.setItem('token', token);
      ElMessage.success(response.data?.message || '登录成功');
      router.push('/manage')
    } else {
      ElMessage.warning(response.data?.message || '登录失败');
    }
  } catch (error: any) {
    console.error('登录失败:', error);
    if (error.response?.status === 401) {
      ElMessage.warning('用户名或密码错误');
    } else {
      ElMessage.error('登录失败，请稍后重试');
    }
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
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
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