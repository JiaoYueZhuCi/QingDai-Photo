import request from './request';
import type { WaterfallItem } from '@/types';

// 基础路径
const BASE_URL = '/api/QingDai/photos';
const CDN_URL = import.meta.env.PROD ? 'https://img.qingdai.art/api/QingDai/photos' : BASE_URL;
// const CDN_URL = 'https://img.qingdai.art/api/QingDai/photos';


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
  startRating: number;
}

export interface PhotoInfoUpdateParams extends WaterfallItem { }

// 获取100K压缩照片(批量)
export const getThumbnail100KPhotos = async (ids: string): Promise<any> => {
  // return await request.get(`${BASE_URL}/thumbnails/small`, {
    return await request.get(`${CDN_URL}/cdn/thumbnails/small`, {
    params: { ids },
    responseType: 'arraybuffer'
  });
};

// 获取100K压缩照片(单张)
export const getThumbnail100KPhoto = async (id: string): Promise<any> => {
  return await request.get(`${CDN_URL}/cdn/thumbnail/small`, {
    params: { id },
    responseType: 'blob'
  });
};

// 获取1000K压缩照片
export const getThumbnail1000KPhoto = async (id: string) => {
  return await request.get(`${CDN_URL}/cdn/thumbnail/medium`, {
    params: { id },
    responseType: 'blob'
  });
};

// 获取原图
export const getFullSizePhoto = async (id: string) => {
  return await request.get(`${CDN_URL}/cdn/fullsize`, {
    params: { id },
    responseType: 'blob'
  });
};

// 上传照片
export const processPhotosFromFrontend = async (formData: FormData): Promise<any> => {
  return await request.post(`${BASE_URL}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000 // 设置30秒超时时间，因为使用队列后上传接口会立即返回
  });
};

// 检查照片处理状态
export const checkPhotoProcessStatus = async (messageId: string): Promise<any> => {
  return await request.get(`${BASE_URL}/${messageId}/upload/status`);
};

// 获取照片分页数据
export const getPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/page`, { params });
};

// 获取可见照片数据
export const getVisiblePhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/visible`, { params });
};

// 获取星标照片数据
export const getStartPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/starred/page`, { params });
};

// 获取隐藏照片数据
export const getHiddenPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/hidden/page`, { params });
};

// 获取气象照片数据
export const getMeteorologyPhotosByPage = async (params: PhotoQueryParams): Promise<any> => {
  return await request.get<PhotoResponse>(`${BASE_URL}/meteorology/page`, { params });
};

// 获取照片详细信息
export const getPhotoInfo = async (id: string) => {
  return await request.get(`${BASE_URL}/${id}`);
};

// 更新照片信息
export const updatePhotoInfo = async (data: PhotoInfoUpdateParams) => {
  return await request.put(`${BASE_URL}/${data.id}`, data);
};

// 更新照片星标状态
export const updatePhotoStartStatus = async (data: PhotoStatusUpdateParams) => {
  return await request.put(`${BASE_URL}/${data.id}/starred`, data);
};

// 删除照片
export const deletePhotoById = async (id: string) => {
  return await request.delete(`${BASE_URL}/${id}`);
};

// 获取星标照片统计数据
export const getStartPhotoCount = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/count/starred`);
};

// 根据类型获取照片数量统计
export const getPhotoCountByTypes = async (types: number | number[]): Promise<number> => {
  const typesParam = Array.isArray(types) ? types.join(',') : types;
  return await request.get(`${BASE_URL}/count/by-types`, {
    params: { types: typesParam }
  });
};

// 获取月度星标照片变化
export const getMonthlyStartPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/count/starred/monthly-change`);
};

// 获取年度星标照片变化
export const getYearlyStartPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/count/starred/yearly-change`);
};

// 获取照片总数
export const getPhotoCount = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/count`);
};

// 获取月度照片变化
export const getMonthlyPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/count/monthly-change`);
};

// 获取年度照片变化
export const getYearlyPhotoCountChange = async (): Promise<number> => {
  return await request.get(`${BASE_URL}/count/yearly-change`);
};

// 根据ID列表获取多个照片对象
export const getPhotosByIds = async (ids: string[]): Promise<WaterfallItem[]> => {
  return await request.get(`${BASE_URL}/ids`, {
    params: { ids: ids.join(',') }
  });
};

// 原图导入数据库
export const fullSizePhotoToMysql = async (): Promise<any> => {
  return await request.post(`${BASE_URL}/import-pending`);
};

// 生成缩略图
export const thumbnailImages = async (maxSizeKB: number = 1024, overwrite: boolean = false): Promise<any> => {
  return await request.post(`${BASE_URL}/thumbnail-pending`, {
    params: { maxSizeKB, overwrite }
  });
};

// 处理待处理照片
export const processPendingPhotos = async (overwrite: boolean = false): Promise<any> => {
  return await request.post(`${BASE_URL}/process-pending`, {
    params: { overwrite }
  });
};

// 验证数据库照片在文件系统中的存在性
export const validatePhotoExistence = async (): Promise<any> => {
  return await request.get(`${BASE_URL}/validate-existence`);
};

// 验证文件系统照片在数据库中的存在性
export const validateFileSystemPhotos = async (): Promise<any> => {
  return await request.get(`${BASE_URL}/validate-filesystem`);
};

// 删除数据库中没有记录的照片
export const deletePhotosNotInDatabase = async (): Promise<any> => {
  return await request.delete(`${BASE_URL}/not-in-database`);
};

// 删除丢失了全部三种图片的数据库记录
export const deleteMissingPhotoRecords = async (): Promise<any> => {
  return await request.delete(`${BASE_URL}/missing-records`);
};

// 获取所有照片统计数据
export const getPhotoDashboardStats = async (): Promise<any> => {
  return await request.get(`${BASE_URL}/stats/dashboard`);
};

// 验证气象组图冲突
export const validateMeteorologyGroups = async (): Promise<any> => {
  return await request.get(`${BASE_URL}/validate-meteorology-groups`);
};

// 获取所有相机型号
export function getAllCameras(): Promise<any> {
  return request.get<string[]>(`${BASE_URL}/cameras`);
}

// 获取所有镜头型号
export function getAllLenses(): Promise<any> {
  return request.get<string[]>(`${BASE_URL}/lenses`);
}

// 更新相机型号
export function updateCameraName(oldCamera: string, newCamera: string): Promise<any> {
  return request({
    url: `${BASE_URL}/cameras/${encodeURI(oldCamera)}`,
    method: 'put',
    params: { newCamera }
  })
}

// 更新镜头型号
export function updateLensName(oldLens: string, newLens: string): Promise<any> {
  return request({
    url: `${BASE_URL}/lenses`,
    method: 'put',
    params: { oldLens, newLens }
  })
}

// 获取指定相机型号的照片数量
export function getPhotoCountByCamera(camera: string): Promise<any> {
  return request.get<number>(`${BASE_URL}/count/by-camera/${encodeURI(camera)}`);
}

// 获取指定镜头型号的照片数量
export function getPhotoCountByLens(lens: string): Promise<any> {
  return request.get<number>(`${BASE_URL}/count/by-lens`, {
    params: { lens }
  });
}

// 更新焦距信息
export const updateFocalLength = async (): Promise<any> => {
  return await request.post<{
    totalCount: number;
    updatedCount: number;
    failedCount: number;
    failedFiles: string[];
  }>(`${BASE_URL}/update-focal-length`);
};

// 获取所有焦距值
export function getAllFocalLengths(): Promise<any> {
  return request.get<string[]>(`${BASE_URL}/focal-lengths`);
}

// 获取指定焦距的照片数量
export function getPhotoCountByFocalLength(focalLength: string): Promise<any> {
  return request.get<number>(`${BASE_URL}/count/by-focal-length/${encodeURI(focalLength)}`);
}

// 更新焦距值
export function updateFocalLengthValue(oldFocalLength: string, newFocalLength: string): Promise<any> {
  return request.put(`${BASE_URL}/focal-lengths/${encodeURI(oldFocalLength)}`, null, {
    params: { newFocalLength }
  });
}

// 获取无元数据照片数据
export const getNoMetadataPhotosByPage = (params: { page: number; pageSize: number }): Promise<any> => {
  return request.get<PhotoResponse>(`${BASE_URL}/no-metadata/page`, { params })
}