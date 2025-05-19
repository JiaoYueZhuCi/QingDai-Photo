import { ElMessage } from 'element-plus';
import request from './request';

// 基础路径
const BASE_URL = '/api/QingDai/users';

// 类型定义
export interface LoginParams {
  username: string;
  password: string;
}

// 介绍信息接口
export interface IntroduceInfo {
  nickname?: string;
  description?: string;
  [key: string]: any;
}

// 扩展登录响应接口，兼容多种可能的返回结构
export interface LoginResponse {
  message: string;
  token?: string;
}

// 用户登录
export const login = async (data: LoginParams): Promise<any> => {
  try {
    const response = await request.post<LoginResponse>(`${BASE_URL}/login`, data, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    return response;
  } catch (error) {
    console.error('登录请求失败', error);
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
  username?: string;
  password?: string;
  nickname?: string;
  description?: string;
}

export const updateUserInfo = async (id: string, userInfo: UpdateUserInfoParams): Promise<any> => {
  try {
    const response = await request.put<any>(`${BASE_URL}/${id}`, userInfo);
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
    });
    return response;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    return null;
  }
};

/**
 * 获取介绍信息
 */
export function getIntroduceInfo(): Promise<any> {
  return request({
    url: `${BASE_URL}/introduce`,
    method: 'get'
  })
}

/**
 * 获取头像
 * @returns 头像请求
 */
export function getAvatar(): Promise<any> {
  return request({
    url: `${BASE_URL}/introduce/avatar`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取背景图
 * @returns 背景图请求
 */
export function getBackground(): Promise<any> {
  return request({
    url: `${BASE_URL}/introduce/background`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 更新介绍信息
 * @param data 介绍信息
 */
export function updateIntroduceInfo(data: IntroduceInfo) {
  return request({
    url: `${BASE_URL}/introduce`,
    method: 'put',
    data
  })
}

/**
 * 上传头像
 * @param formData 包含头像文件的FormData
 */
export function uploadAvatar(formData: FormData): Promise<any> {
  return request({
    url: `${BASE_URL}/introduce/avatar`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传背景图
 * @param formData 包含背景图文件的FormData
 */
export function uploadBackground(formData: FormData): Promise<any> {
  return request({
    url: `${BASE_URL}/introduce/background`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}