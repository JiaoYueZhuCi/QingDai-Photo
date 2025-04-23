import { ElMessage } from 'element-plus';
import request from './request';

// 基础路径
const BASE_URL = '/api/QingDai/users';

// 类型定义
export interface LoginParams {
  username: string;
  password: string;
}

// 扩展登录响应接口，兼容多种可能的返回结构
export interface LoginResponse {
  token?: string;
  userInfo?: {
    id: string | number;
    username: string;
    nickname?: string;
    role?: string;
  };
  // 兼容可能的响应结构
  headers?: {
    authorization?: string;
    Authorization?: string;
    [key: string]: any;
  };
  data?: {
    token?: string;
    userInfo?: any;
    [key: string]: any;
  };
  [key: string]: any; // 允许任何其他字段
}

// 用户登录
export const login = async (data: LoginParams): Promise<LoginResponse> => {
  try {
    const response = await request.post<any>(`${BASE_URL}/login`, data, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    return response;
  } catch (error) {
    console.error('登陆失败', error);
    ElMessage.error('登陆失败');
    throw error;
  }
};

export interface RolesPermissionsResponse {
  roles: string[];
  permissions: string[];
}

export const getRolesPermissions = async (): Promise<any> => {
  try {
    const response = await request.get<RolesPermissionsResponse>(
      `${BASE_URL}/me/roles-permissions`,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    return response;
  } catch (error) {
    console.error('获取角色权限失败:', error);
    throw error;
  }
};

export interface UpdateUserInfoParams {
  username: string;
  password: string;
}

export const updateUserInfo = async (id: string, data: UpdateUserInfoParams): Promise<any> => {
  try {
    const response = await request.put<any>(`${BASE_URL}/${id}`, data);
    return response;
  } catch (error) {
    console.error('更新用户信息失败:', error);
    throw error;
  }
};

export const getUserInfo = async (token: string): Promise<any> => {
  try {
    const response = await request.get<any>(`${BASE_URL}/me`, {
      params: {
        testToken: token
      },
      headers: {
        'X-Is-Testing-Token': 'true'
      }
    });
    return response;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    throw error;
  }
};