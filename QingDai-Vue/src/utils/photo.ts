import JSZip from 'jszip';
import { ElMessage } from 'element-plus';
import { getThumbnail100KPhotos, getPhotosByIds, getThumbnail1000KPhoto, getFullSizePhoto, getPhotoInfo } from '@/api/photo';
import type { WaterfallItem } from '@/types';
import { get100KPhotoFromDB, save100KPhotoToDB, get1000KPhotoFromDB, save1000KPhotoToDB, getFullPhotoFromDB, saveFullPhotoToDB } from "@/utils/indexedDB";
import { toRaw, triggerRef } from 'vue';

// 增强的瀑布流项类型，支持动态属性
export interface EnhancedWaterfallItem extends WaterfallItem {
  [key: string]: any;
}

/**
 * 批量获取100K照片缩略图
 * @param items 需要获取缩略图的照片项列表
 * @param srcKey 缩略图URL字段名，默认为"compressedSrc"
 * @param idKey 照片ID字段名，默认为"id"
 * @param fileNameKey 文件名字段名，默认为"fileName"
 * @returns 处理后的照片项列表
 */
export const get100KPhotos = async (
  items: EnhancedWaterfallItem[],
  srcKey: string = 'compressedSrc',
  idKey: string = 'id',
  fileNameKey: string = 'fileName'
): Promise<EnhancedWaterfallItem[]> => {
  if (!items || items.length === 0) return items;

  // 复制一份数据，避免修改原始数据
  const processedItems = [...items];
  
  // 尝试从IndexedDB获取所有照片的缓存
  let uncachedItems: EnhancedWaterfallItem[] = [];
  await Promise.all(processedItems.map(async item => {
    try {
      const cachedBlob = await get100KPhotoFromDB(item[idKey]);
      if (cachedBlob) {
        const cachedBlobUrl = URL.createObjectURL(cachedBlob);
        item[srcKey] = cachedBlobUrl;
      } else {
        uncachedItems.push(item);
      }
    } catch (error) {
      console.error('从缓存加载失败:', error);
      uncachedItems.push(item);
    }
  }));

  if (uncachedItems.length === 0) {
    return processedItems;
  }

  try {
    // 获取需要请求的照片ID
    const photoIds = uncachedItems.map(item => item[idKey]);

    // 创建ID到项目的映射
    const idToItemsMap = new Map<string, EnhancedWaterfallItem[]>();
    uncachedItems.forEach((item) => {
      const id = item[idKey];
      if (!idToItemsMap.has(id)) {
        idToItemsMap.set(id, []);
      }
      idToItemsMap.get(id)!.push(item);
    });

    // 去重后的照片ID
    const uniquePhotoIds = [...new Set(photoIds)];

    // 调用API获取缩略图，只传递唯一的ID
    const response = await getThumbnail100KPhotos(uniquePhotoIds.join(','));

    const zip = await JSZip.loadAsync(response.data);

    // 处理每个唯一的照片ID
    for (const photoId of uniquePhotoIds) {
      const items = idToItemsMap.get(photoId) || [];
      if (items.length > 0) {
        const fileName = items[0][fileNameKey];
        const file = zip.file(`${fileName}`);

        if (file) {
          const blob = await file.async('blob');
          const url = URL.createObjectURL(blob);
          // 存储到IndexedDB并设置URL
          await save100KPhotoToDB(photoId, blob);

          // 更新所有对应项目的缩略图
          items.forEach(item => {
            item[srcKey] = url;
          });

        } else {
          // 尝试通过模糊匹配文件名查找
          const matchedFile = Object.values(zip.files).find(f =>
            !f.dir && (f.name.includes(photoId) || (fileName && f.name.includes(fileName)))
          );

          if (matchedFile) {
            const blob = await matchedFile.async('blob');
            const url = URL.createObjectURL(blob);
            await save100KPhotoToDB(photoId, blob);

            items.forEach(item => {
              item[srcKey] = url;
            });
          } else {
            // 文件未找到的情况
            items.forEach(item => {
              item[srcKey] = '';
            });
            console.error('ZIP包中未找到照片:', photoId);
          }
        }
      }
    }
  } catch (error) {
    console.error('批量获取压缩图失败:', error);
    ElMessage.warning('部分缩略图加载失败');
    uncachedItems.forEach(item => {
      item[srcKey] = '';
    });
  }

  return processedItems;
};

/**
 * 通过ID批量获取照片基本信息并加载缩略图
 * @param photoIds 照片ID数组
 * @returns 处理后的照片数据
 */
export const getPhotosByIdsWithThumbnail = async (photoIds: string[]): Promise<EnhancedWaterfallItem[]> => {
  if (!photoIds || photoIds.length === 0) return [];

  try {
    // 获取照片基本信息
    const photosData = await getPhotosByIds(photoIds);

    // 加载缩略图
    return await get100KPhotos(photosData);
  } catch (error) {
    console.error('获取照片信息失败:', error);
    ElMessage.error('照片数据加载失败');
    return [];
  }
};

/**
 * 处理照片基础数据，确保所有字段都有默认值
 * @param item 原始照片数据
 * @returns 处理后的照片数据
 */
export const processPhotoData = (item: any): EnhancedWaterfallItem => {

  const result: EnhancedWaterfallItem = {
    id: item.id || '',
    fileName: item.fileName || '',
    author: item.author || '未知作者',
    width: item.width || 0,
    height: item.height || 0,
    aperture: item.aperture || '',
    iso: item.iso || '',
    shutter: item.shutter || '',
    camera: item.camera || '',
    lens: item.lens || '',
    time: item.time || '',
    title: item.title || '',
    introduce: item.introduce || '',
    start: item.start || 0,
    aspectRatio: (item.width && item.height) ? item.width / item.height : 1.5,
    calcWidth: item.calcWidth || 0,
    calcHeight: item.calcHeight || 0,
    compressedSrc: item.compressedSrc ?? '',
  };

  // 如果存在groupId，添加到结果中
  if (item.groupId) {
    result.groupId = item.groupId;
  }

  return result;
};

/**
 * 获取1000K图片
 * @param photoId 照片ID
 * @param setLoading 设置加载状态的函数，可选
 * @returns Blob对象和URL
 */
export const get1000KPhoto = async (
  photoId: string,
  setLoading?: (loading: boolean) => void
): Promise<{ blob: Blob, url: string } | null> => {
  try {
    setLoading?.(true);

    // 优先从缓存获取
    const cachedBlob = await get1000KPhotoFromDB(photoId);

    if (cachedBlob && cachedBlob.size) {
      // 从缓存加载
      return {
        blob: cachedBlob,
        url: URL.createObjectURL(cachedBlob)
      };
    } else {
      // 从API加载
      const res = await getThumbnail1000KPhoto(photoId);
      const blob = res.data;
      // 存储到数据库
      await save1000KPhotoToDB(photoId, blob);
      return {
        blob,
        url: URL.createObjectURL(blob)
      };
    }
  } catch (error) {
    console.error('获取1000K图片失败:', error);
    return null;
  } finally {
    setLoading?.(false);
  }
};

/**
 * 获取原图
 * @param photoId 照片ID
 * @param setLoading 设置加载状态的函数，可选
 * @returns Blob对象和URL
 */
export const getFullPhoto = async (
  photoId: string,
  setLoading?: (loading: boolean) => void
): Promise<{ blob: Blob, url: string } | null> => {
  try {
    setLoading?.(true);

    // 优先从缓存获取
    const cachedBlob = await getFullPhotoFromDB(photoId);

    if (cachedBlob && cachedBlob.size) {
      // 从缓存加载
      return {
        blob: cachedBlob,
        url: URL.createObjectURL(cachedBlob)
      };
    } else {
      // 从API加载
      const res = await getFullSizePhoto(photoId);
      const blob = res.data;
      // 存储到数据库
      await saveFullPhotoToDB(photoId, blob);
      return {
        blob,
        url: URL.createObjectURL(blob)
      };
    }
  } catch (error) {
    console.error('获取原图失败:', error);
    return null;
  } finally {
    setLoading?.(false);
  }
};

/**
 * 获取照片详细信息
 * @param photoId 照片ID
 * @returns 照片详细信息
 */
export const getPhotoDetailInfo = async (photoId: string): Promise<EnhancedWaterfallItem | null> => {
  try {
    const photoInfo = await getPhotoInfo(photoId);
    return photoInfo ? processPhotoData(photoInfo) : null;
  } catch (error) {
    console.error('获取照片信息失败:', error);
    return null;
  }
}; 