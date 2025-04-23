import request from './request';
import type { GroupPhotoDTO } from '@/types/groupPhoto';

const BASE_URL = '/api/QingDai/group-photos';

export const getAllGroupPhotos = async (): Promise<any> => {
  return await request.get<GroupPhotoDTO[]>(`${BASE_URL}`);
};

export const getGroupPhoto = async (id: string): Promise<any> => {
  return await request.get<GroupPhotoDTO>(`${BASE_URL}/${id}`);
}

export const addGroupPhoto = async (data: GroupPhotoDTO) => {
  return await request.post<GroupPhotoDTO>(`${BASE_URL}`, data);
};

export const updateGroupPhoto = async (data: GroupPhotoDTO) => {
  return await request.put<GroupPhotoDTO>(`${BASE_URL}/${data.groupPhoto.id}`, data);
};

export const deleteGroupPhoto = async (id: string) => {
  return await request.delete(`${BASE_URL}/${id}`);
};

export const getGroupPhotoCount = async (id: string) => {
  return await request.get<number>(`${BASE_URL}/${id}/photos/count`);
};