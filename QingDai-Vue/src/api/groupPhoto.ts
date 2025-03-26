import request from './request';
import type { GroupPhoto, GroupPhotoDTO } from '@/types/groupPhoto';

const BASE_URL = '/api/QingDai/groupPhoto';

export const getAllGroupPhotos = async () => {
  return await request.get<GroupPhoto[]>(`${BASE_URL}/getAllGroupPhotos`);
};

export const getAllGroupPhotoPreviews = async () => {
  return await request.get<GroupPhotoDTO[]>(`${BASE_URL}/previews`);
}

export const getGroupPhoto = async (id: string) => {
  return await request.get(`${BASE_URL}/getGroupPhoto/${id}`);
}

export const addGroupPhoto = async (data: GroupPhoto) => {
  return await request.post<GroupPhoto>(`${BASE_URL}/addGroupPhoto`, data);
};

export const updateGroupPhoto = async (data: GroupPhoto) => {
  return await request.put<GroupPhoto>(`${BASE_URL}/updateGroupPhoto`, data);
};

export const deleteGroupPhoto = async (id: string) => {
  return await request.delete(`${BASE_URL}/deleteGroupPhoto/${id}`);
};

export const getGroupPhotoCount = async (id: string) => {
  return await request.get<number>(`${BASE_URL}/getPhotoCount/${id}`);
};