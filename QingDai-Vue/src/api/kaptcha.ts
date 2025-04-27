import request from './request';

// 验证码API基础路径
const BASE_URL = '/api/QingDai/captcha';

/**
 * 获取验证码图片
 * @returns {Promise<string>} 返回验证码图片的Base64编码
 */
export const getCaptchaImage = async (): Promise<string> => {
    try {
        // 后端直接返回Base64字符串，不再是对象
        return await request.get(`${BASE_URL}/generate`, {
            withCredentials: true // 确保请求包含凭证（cookies）
        });
    } catch (error) {
        console.error('获取验证码失败:', error);
        throw error;
    }
};

/**
 * 验证验证码
 * @param {string} captcha 用户输入的验证码
 * @returns {Promise} 验证结果
 */
export const verifyCaptcha = async (captcha: string) => {
    return request.post(`${BASE_URL}/verify`, { captcha }, {
        withCredentials: true // 确保请求包含凭证（cookies）
    });
};

