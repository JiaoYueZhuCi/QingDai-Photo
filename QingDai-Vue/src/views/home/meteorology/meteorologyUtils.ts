import { getMeteorologyGroupName } from '@/config/groupPhotos';

/**
 * 将气象类型ID转换为对应的名称
 * @param meteorologyType 气象类型ID
 * @returns 气象类型名称
 */
export const getMeteorologyTypeName = (meteorologyType: string): string => {
    return getMeteorologyGroupName(meteorologyType);
};

/**
 * 按时间排序照片
 * @param photos 照片列表
 * @param isAscending 是否升序排列
 * @returns 排序后的照片列表
 */
export const sortPhotosByTime = <T extends { shootTime: string }>(
    photos: T[],
    isAscending: boolean
): T[] => {
    if (!photos.length) return [];

    // 复制数组，避免修改原始数据
    const photosToSort = [...photos];

    // 按时间排序
    return photosToSort.sort((a, b) => {
        // 将时间字符串转为 Date 对象进行比较
        const timeA = new Date(a.shootTime).getTime();
        const timeB = new Date(b.shootTime).getTime();

        // 根据排序顺序返回结果
        return isAscending ? timeA - timeB : timeB - timeA;
    });
}; 