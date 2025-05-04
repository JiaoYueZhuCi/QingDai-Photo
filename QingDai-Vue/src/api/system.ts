import request from './request';

// 基础路径
const BASE_URL = '/api/QingDai/system';

/**
 * 获取系统信息
 * @returns 系统信息对象，包含后端启动时间等信息
 */
export const getBackendInfo = async () => {
  try {
    const response = await request.get(`${BASE_URL}/info`);
    return response;
  } catch (error) {
    console.error('获取系统信息失败', error);
    return null;
  }
};

