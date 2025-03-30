import { defineStore } from 'pinia';

export const photoStore = defineStore('image', {
  state: () => ({
    thumbnailPath: 'E:/QingDaiPhotos/Photos/Thumbnail/' as string,
    fullSizePath: 'E:/QingDaiPhotos/Photos/FullSize/' as string,
  }),
});