import request from './request';
import type { TimelineItem } from '@/types';

// 基础路径
const BASE_URL = '/api/QingDai/timeline';

// 类型定义
export interface TimelineDTO {
  time: string;
  title: string;
  text: string;
}

// 获取所有时间轴数据
export const getAllTimelines = async (): Promise<TimelineItem[]> => {
  return request.get<TimelineItem[]>(`${BASE_URL}/getAllTimelines`);
};

// 添加时间轴
export const addTimeline = async (data: TimelineDTO): Promise<TimelineItem> => {
  return request.post<TimelineItem>(`${BASE_URL}/addTimeline`, data);
};

// 更新时间轴
export const updateTimeline = async (data: TimelineItem): Promise<TimelineItem> => {
  return request.put<TimelineItem>(`${BASE_URL}/updateTimeline`, data);
};

// 删除时间轴
export const deleteTimeline = async (id: string | number): Promise<any> => {
  return request.delete(`${BASE_URL}/deleteTimeline/${id}`);
}; 