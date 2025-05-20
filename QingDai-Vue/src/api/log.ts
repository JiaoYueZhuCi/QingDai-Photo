import request from './request';

// 基础路径
const BASE_URL = '/api/QingDai/log';

/**
 * 获取SQL日志状态
 * @returns SQL日志是否启用
 */
export const getSqlLogStatus = async (): Promise<any> => {
  try {
    const response = await request.get<boolean>(`${BASE_URL}/sql/status`);
    return response;
  } catch (error) {
    console.error('获取SQL日志状态失败', error);
    return false;
  }
};

/**
 * 切换SQL日志状态
 * @param enabled 是否启用SQL日志
 * @returns 操作结果消息
 */
export const toggleSqlLog = async (enabled: boolean): Promise<any> => {
  try {
    const response = await request.put<string>(`${BASE_URL}/sql/toggle`, null, {
      params: { enabled }
    });
    return response;
  } catch (error) {
    console.error('切换SQL日志状态失败', error);
    throw error;
  }
}; 