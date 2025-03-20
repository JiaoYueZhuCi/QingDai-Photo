import request from './request';
import type { GroupPhoto, GroupPhotoDTO } from '@/types/groupPhoto';

const BASE_URL = '/api/QingDai/groupPhoto';

export const getAllGroupPhotos = async (): Promise<GroupPhoto[]> => {
  return (await request.get<GroupPhoto[]>(`${BASE_URL}/getAllGroupPhotos`)).data;
};

export const getAllGroupPhotoPreviews = async (): Promise<GroupPhotoDTO[]> => {
  return (await request.get<GroupPhotoDTO[]>(`${BASE_URL}/previews`)).data;
}

export const getGroupPhoto = async (id: string): Promise<GroupPhoto> => {
  return (await request.get(`${BASE_URL}/getGroupPhoto/${id}`)).data;
}

export const addGroupPhoto = async (data: GroupPhoto): Promise<GroupPhoto> => {
  return (await request.post<GroupPhoto>(`${BASE_URL}/addGroupPhoto`, data)).data;
};

export const updateGroupPhoto = async (data: GroupPhoto): Promise<GroupPhoto> => {
  return (await request.put<GroupPhoto>(`${BASE_URL}/updateGroupPhoto`, data)).data;
};

export const deleteGroupPhoto = async (id: string): Promise<void> => {
  return (await request.delete(`${BASE_URL}/deleteGroupPhoto/${id}`)).data;
};