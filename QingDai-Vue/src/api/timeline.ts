import request from './request';
import type { TimelineItem } from '@/types';

// 基础路径
const BASE_URL = '/api/QingDai/timelines';

// 类型定义
export interface TimelineDTO {
  time: string;
  title: string;
  text: string;
}

// 分页查询参数
export interface TimelineQueryParams {
  page: number;
  pageSize: number;
}

// 分页响应接口
export interface TimelinePageResponse {
  records: TimelineItem[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 获取所有时间轴数据
export const getAllTimelines = async (): Promise<any> => {
  return await request.get<TimelineItem[]>(`${BASE_URL}`);
};

// 分页获取时间轴数据
export const getTimelinesByPage = async (params: TimelineQueryParams): Promise<any> => {
  return await request.get<TimelinePageResponse>(`${BASE_URL}/page`, { params });
};

// 添加时间轴
export const addTimeline = async (data: TimelineDTO): Promise<any> => {
  return await request.post<TimelineItem>(`${BASE_URL}`, data);
};

// 更新时间轴
export const updateTimeline = async (data: TimelineItem): Promise<any> => {
  return await request.put<TimelineItem>(`${BASE_URL}/${data.id}`, data);
};

// 删除时间轴
export const deleteTimeline = async (id: string | number): Promise<any> => {
  return await request.delete(`${BASE_URL}/${id}`);
}; 