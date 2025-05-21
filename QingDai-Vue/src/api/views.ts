import request from './request';

const BASE_URL = '/api/QingDai/views';
// 获取网站总浏览量
export const getTotalViewCount = async (): Promise<any> => {
    const response = await request.get<number>(`${BASE_URL}/total`);
    return response;
};

// 增加网站浏览量
export const incrementViewCount = async (): Promise<any> => {
    const response = await request.post<number>(`${BASE_URL}/increment`);
    return response;
}; 