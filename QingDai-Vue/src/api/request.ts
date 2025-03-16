import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const service: AxiosInstance = axios.create({
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    
    // 如果token存在，则在请求头中添加token
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  <T>(response: AxiosResponse<T>) => {
    // 对于二进制数据(arraybuffer/blob)，返回完整的响应对象
    const contentType = response.headers['content-type'];
    if (
      response.config.responseType === 'arraybuffer' || 
      response.config.responseType === 'blob' ||
      contentType?.includes('application/octet-stream') ||
      contentType?.includes('application/zip')
    ) {
      return response;
    }
    
    // 对于登录请求，保留完整的响应对象以便获取headers中的token
    if (response.config.url?.includes('/login')) {
      console.log('登录请求响应:', response);
      return response;
    }
    
    // 对于其他响应，直接返回数据部分
    return response.data as T;
  },
  (error) => {
    let message = '';
    
    // 根据HTTP状态码处理错误
    if (error.response) {
      const status = error.response.status;
      
      switch (status) {
        case 400:
          message = '请求错误';
          break;
        case 401:
          message = '未授权，请重新登录';
          // 清除token并跳转到登录页
          localStorage.removeItem('token');
          // window.location.href = '/login';
          break;
        case 403:
          message = '拒绝访问,请重新登录';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        default:
          message = `请求失败: ${status}`;
      }
    } else {
      message = '网络连接异常';
    }
    
    // 显示错误消息
    ElMessage.error(message);
    
    return Promise.reject(error);
  }
);

// 导出axios实例
export default service; 