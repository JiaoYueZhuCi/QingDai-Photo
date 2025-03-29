import type { WaterfallItem } from '@/types';

export interface GroupPhoto {
    id: string; 
    title: string;
    introduce: string;
    coverPhotoId: string;

}

export interface GroupPhotoDTO {
    groupPhoto: GroupPhoto;
    photoIds: string[];
}