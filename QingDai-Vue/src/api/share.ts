import request from './request';
import type { AxiosResponse } from 'axios';

const BASE_URL = '/api/QingDai/shares';

// 创建分享链接的请求参数接口
export interface CreateShareLinkRequest {
    photoIds: string[];
    expireDays: number;
}

// 创建分享链接的响应接口
export interface CreateShareLinkResponse {
    shareId: string;
}

// 分享信息接口
export interface Share {
    id: string;
    photoIds: string[];
    createTime: string;
    expireTime: string;
    isExpired: boolean;
}

// 分页查询参数
export interface ShareQueryParams {
    page: number;
    pageSize: number;
}

// 分页响应接口
export interface SharePageResponse {
    records: Share[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

// 创建分享链接
export const createShareLink = async (data: CreateShareLinkRequest): Promise<any> => {
    const response = await request.post<CreateShareLinkResponse>(`${BASE_URL}`, data);
    return response;
};

// 获取分享的照片
export const getSharePhotos = async (shareId: string): Promise<any> => {
    const response = await request.get<string[]>(`${BASE_URL}/${shareId}/photos`);
    return response;
};

// 验证分享链接
export const validateShareLink = async (shareId: string): Promise<any> => {
    const response = await request.get<boolean>(`${BASE_URL}/${shareId}/validate`);
    return response;
};

// 获取所有分享
export const getAllShares = async (): Promise<any> => {
    const response = await request.get<Share[]>(`${BASE_URL}`);
    return response;
};

// 分页获取分享
export const getSharesByPage = async (params: ShareQueryParams): Promise<any> => {
    const response = await request.get<SharePageResponse>(`${BASE_URL}/page`, { params });
    return response;
};

// 删除分享
export const deleteShare = async (shareId: string): Promise<any> => {
    const response = await request.delete<boolean>(`${BASE_URL}/${shareId}`);
    return response;
}; 