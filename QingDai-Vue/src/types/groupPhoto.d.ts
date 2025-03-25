import type { WaterfallItem } from '@/types';

export interface GroupPhoto {
    id: string;
    photos: string ;  
    cover: number;
    title: string;
    introduce: string;
}

export interface GroupPhotoDTO {
    groupPhoto: GroupPhoto;
    photo: WaterfallItem;
}