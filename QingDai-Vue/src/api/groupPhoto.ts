import request from './request';
import type { GroupPhotoDTO } from '@/types/groupPhoto';

const BASE_URL = '/api/QingDai/groupPhoto';

export const getAllGroupPhotos = async (): Promise<any> => {
  return await request.get<GroupPhotoDTO[]>(`${BASE_URL}/getAllGroupPhotos`);
};

export const getGroupPhoto = async (id: string): Promise<any> => {
  return await request.get<GroupPhotoDTO>(`${BASE_URL}/getGroupPhoto/${id}`);
}

export const addGroupPhoto = async (data: GroupPhotoDTO) => {
  return await request.post<GroupPhotoDTO>(`${BASE_URL}/addGroupPhoto`, data);
};

export const updateGroupPhoto = async (data: GroupPhotoDTO) => {
  return await request.put<GroupPhotoDTO>(`${BASE_URL}/updateGroupPhoto`, data);
};

export const deleteGroupPhoto = async (id: string) => {
  return await request.delete(`${BASE_URL}/deleteGroupPhoto/${id}`);
};

export const getGroupPhotoCount = async (id: string) => {
  return await request.get<number>(`${BASE_URL}/getPhotoCount/${id}`);
};