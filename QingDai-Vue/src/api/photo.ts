import request from './request';
import type { WaterfallItem } from '@/types';

// 基础路径
const BASE_URL = '/api/QingDai/photo';
// 类型定义
export interface PhotoQueryParams {
  page?: number;
  pageSize?: number;
  id?: string;
}

export interface PhotoResponse {
  records: WaterfallItem[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface PhotoStatusUpdateParams {
  id: string;
  start: number;
}

export interface PhotoInfoUpdateParams extends WaterfallItem { }// 获取100K压缩照片(批量)
export const getThumbnail100KPhotos = async (ids: string): Promise<any> => {
  return await request.get(`${BASE_URL}/getThumbnail100KPhotos`, {
    params: { ids },
    responseType: 'arraybuffer'
  });

};

// 获取100K压缩照片(单张)
export const getThumbnail100KPhoto = async (id: string): Promise<any> => {
  return await request.get(`${BASE_URL}/getThumbnail100KPhoto`, {
    params: { id },
    responseType: 'blob'
  });
};

// 获取1000K压缩照片
export const getThumbnail1000KPhoto = async (id: string) => {
  return await request.get(`${BASE_URL}/getThumbnail1000KPhoto`, {
    params: { id },
    responseType: 'blob'
  });
};

// 获取原图
export const getFullSizePhoto = async (id: string) => {
  return await request.get(`${BASE_URL}/getFullSizePhoto`, {
    params: { id },
    responseType: 'blob'
  });
};

// 上传照片
export const processPhotosFromFrontend = async (formData: FormData) => {
  return await request.post(`${BASE_URL}/processPhotosFromFrontend`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000 // 设置5分钟超时时间
  });
};
// 获取照片分页数据
export const getPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/getPhotosByPage`, { params });
};

// 获取可见照片数据
export const getVisiblePhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/getVisiblePhotosByPage`, { params });
};

// 获取星标照片数据
export const getStartPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/getStartPhotosByPage`, { params });
};

// 获取隐藏照片数据
export const getHiddenPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/getHiddenPhotosByPage`, { params });
};

// 获取气象照片数据
export const getWeatherPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/getWeatherPhotosByPage`, { params });
};

// 获取照片详细信息
export const getPhotoInfo = async (id: string) => {
  return await request.get(`${BASE_URL}/getPhotoInfo`, { params: { id } });
};

// 更新照片信息
export const updatePhotoInfo = async (data: PhotoInfoUpdateParams) => {
  return await request.put(`${BASE_URL}/updatePhotoInfo`, data);
};

// 更新照片星标状态
export const updatePhotoStartStatus = async (data: PhotoStatusUpdateParams) => {
  return await request.put(`${BASE_URL}/updatePhotoStartStatus`, data);
};

// 删除照片
export const deletePhotoById = async (id: string) => {
  return await request.delete(`${BASE_URL}/deletePhotoById`, { params: { id } });
};

// 获取星标照片统计数据
export const getStartPhotoCount = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/getStartPhotoCount`);
};

// 获取月度星标照片变化
export const getMonthlyStartPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/getMonthlyStartPhotoCountChange`);
};

// 获取年度星标照片变化
export const getYearlyStartPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/getYearlyStartPhotoCountChange`);
};

// 获取照片总数
export const getPhotoCount = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/getPhotoCount`);
};

// 获取月度照片变化
export const getMonthlyPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/getMonthlyPhotoCountChange`);
};

// 获取年度照片变化
export const getYearlyPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/getYearlyPhotoCountChange`);
};

// 根据ID列表获取多个照片对象
export const getPhotosByIds = async (ids: string[]): Promise<WaterfallItem[]> => {
  return await request.get(`${BASE_URL}/getPhotosByIds`, {
    params: { ids: ids.join(',') }
  });
};

// 开发者方法-----------------------------------------------------------------------------------------
// 原图导入数据库
export const fullSizePhotoToMysql = async (): Promise<any> => {
  return await request.post(`${BASE_URL}/toMysql`);
};

// 生成缩略图
export const thumbnailImages = async (maxSizeKB: number = 1024, overwrite: boolean = false): Promise<any> => {
  return await request.get(`${BASE_URL}/thumbnail`, {
    params: { maxSizeKB, overwrite }
  });
};

// 处理待处理照片
export const processPendingPhotos = async (overwrite: boolean = false): Promise<any> => {
  return await request.get(`${BASE_URL}/processPendingPhotos`, {
    params: { overwrite }
  });
};

// 验证数据库照片在文件系统中的存在性
export const validatePhotoExistence = async (): Promise<any> => {
  return await request.get(`${BASE_URL}/validatePhotoExistence`);
};

// 验证文件系统照片在数据库中的存在性
export const validateFileSystemPhotos = async (): Promise<any> => {
  return await request.get(`${BASE_URL}/validateFileSystemPhotos`);
};

// 删除数据库中没有记录的照片
export const deletePhotosNotInDatabase = async (): Promise<any> => {
  return await request.delete(`${BASE_URL}/deletePhotosNotInDatabase`);
};

// 删除丢失了全部三种图片的数据库记录
export const deleteMissingPhotoRecords = async (): Promise<any> => {
  return await request.delete(`${BASE_URL}/deleteMissingPhotoRecords`);
};