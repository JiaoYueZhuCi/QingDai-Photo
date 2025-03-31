import { openDB } from 'idb';
import type { DBSchema, IDBPDatabase } from 'idb';

// 正确定义数据结构
interface PhotoDB extends DBSchema {
  photos: {
    key: string;        // 主键类型
    value: {            // 存储值类型
      photoId: string;      // 对应 keyPath
      fullBlob: Blob;      // 原始尺寸照片数据
      blob100K: Blob;      // 100K尺寸照片数据
      blob1000K: Blob;     // 1000K尺寸照片数据
    };
    // indexes: { 
    //   'by-key': string; // 索引
    // };
  };
}

export const initDB = async (): Promise<IDBPDatabase<PhotoDB>> => {
  return await openDB<PhotoDB>('photo-cache', 1, {
    upgrade(db) {
      // 创建对象存储库
      const store = db.createObjectStore('photos', {
        keyPath: 'photoId', // 指定主键为 photoId 字段
      });
      
      // 创建索引（可选）
    //   store.createIndex('by-key', 'key', { unique: true });
    },
  });
};

// 获取原始尺寸照片
export const getFullPhotoFromDB = async (photoId: string): Promise<Blob | undefined> => {
  try {
    const db = await initDB();
    const record = await db.get('photos', photoId);
    return record?.fullBlob;
  } catch (error) {
    console.error('获取原始尺寸照片失败:', error);
    return undefined;
  }
};

// 获取100K尺寸照片
export const get100KPhotoFromDB = async (photoId: string): Promise<Blob | undefined> => {
  try {
    const db = await initDB();
    const record = await db.get('photos', photoId);
    return record?.blob100K;
  } catch (error) {
    console.error('获取100K尺寸照片失败:', error);
    return undefined;
  }
};

// 获取1000K尺寸照片
export const get1000KPhotoFromDB = async (photoId: string): Promise<Blob | undefined> => {
  try {
    const db = await initDB();
    const record = await db.get('photos', photoId);
    return record?.blob1000K;
  } catch (error) {
    console.error('获取1000K尺寸照片失败:', error);
    return undefined;
  }
};

// 保存原始尺寸照片
export const saveFullPhotoToDB = async (photoId: string, fullBlob: Blob): Promise<void> => {
  try {
    const db = await initDB();
    const existing = await db.get('photos', photoId);
    await db.put('photos', {
      photoId: photoId,
      fullBlob: fullBlob,
      blob100K: existing?.blob100K || new Blob(), // 确保 blob100K 不为 undefined
      blob1000K: existing?.blob1000K || new Blob() // 确保 blob1000K 不为 undefined
    });
  } catch (error) {
    console.error('保存原始尺寸照片失败:', {
      error: error,
      photoId: photoId,
      fullBlob: fullBlob
    });
    throw new Error(`无法保存原始尺寸照片 ${photoId}`);
  }
};

// 保存100K尺寸照片
export const save100KPhotoToDB = async (photoId: string, blob100K: Blob): Promise<void> => {
  try {
    const db = await initDB();
    const existing = await db.get('photos', photoId);
    await db.put('photos', {
      photoId: photoId,
      blob100K: blob100K,
      fullBlob: existing?.fullBlob || new Blob(), 
      blob1000K: existing?.blob1000K || new Blob()
    });
  } catch (error) {
    console.error('保存100K尺寸照片失败:', {
      error: error,
      photoId: photoId,
      blob100K: blob100K
    });
    throw new Error(`无法保存100K尺寸照片 ${photoId}`);
  }
};

// 保存1000K尺寸照片
export const save1000KPhotoToDB = async (photoId: string, blob1000K: Blob): Promise<void> => {
  try {
    const db = await initDB();
    const existing = await db.get('photos', photoId);
    await db.put('photos', {
      ...existing,
      photoId: photoId,
      blob1000K: blob1000K,
      fullBlob: existing?.fullBlob || new Blob(),
      blob100K: existing?.blob100K || new Blob() 
    });
  } catch (error) {
    console.error('保存1000K尺寸照片失败:', {
      error: error,
      photoId: photoId,
      blob1000K: blob1000K
    });
    throw new Error(`无法保存1000K尺寸照片 ${photoId}`);
  }
};

// 清理缓存
export const clearPhotoDB = async (): Promise<void> => {
  const db = await initDB();
  await db.clear('photos');
};