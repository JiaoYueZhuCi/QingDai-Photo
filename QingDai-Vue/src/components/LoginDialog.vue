<template>
    <el-dialog v-model="visible" title="用户登录" width="30%" center>
        <el-form :model="form" label-width="80px" :rules="rules" ref="loginForm">
            <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input v-model="form.password" type="password" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="submitForm">登录</el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 配置axios请求拦截器
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 配置axios响应拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.reload();
    }
    return Promise.reject(error);
  }
);

const visible = ref(false)
const loginForm = ref()

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
    await loginForm.value.validate()
    const response = await axios.post('/api/QingDai/user/login', form, {
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      },
      validateStatus: (status) => status < 500
    })

    if (response.status === 200) {
      localStorage.setItem('token', response.data.token);
      ElMessage.success('登录成功')
      visible.value = false
      console.log(response.data.token)
      console.log(visible.value)
    } else if (response.status === 401) {
      ElMessage.error('用户名密码错误')
    } else {
      ElMessage.error(response.data?.msg || '请求异常')
    }
  } catch (error) {
    ElMessage.error('网络连接失败') 
  }
}

defineExpose({
    visible
})
</script>