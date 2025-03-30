import { openDB } from 'idb';
import type { DBSchema, IDBPDatabase } from 'idb';

// 正确定义数据结构
interface PhotoDB extends DBSchema {
  photos: {
    key: string;        // 主键类型
    value: {            // 存储值类型
      photoId: string;      // 对应 keyPath
      blob: Blob;       // 实际存储的二进制数据
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

// 获取照片
export const getPhotoFromDB = async (photoId: string): Promise<Blob | undefined> => {
  try {
    const db = await initDB();
    const record = await db.get('photos', photoId);
    return record?.blob;
  } catch (error) {
    console.error('获取照片失败:', error);
    return undefined;
  }
};

// 保存照片（支持批量）
export const savePhotoToDB = async (photoId: string, blob: Blob): Promise<void> => {
  try {
    const db = await initDB();
    await db.put('photos', {
      photoId: photoId,  
      blob: blob
    });
  } catch (error) {
    console.error('保存照片失败:', {
      error: error,
      photoId: photoId,
      blob: blob
    });
    throw new Error(`无法保存照片 ${photoId}`);
  }
};

// 清理缓存
export const clearPhotoDB = async (): Promise<void> => {
  const db = await initDB();
  await db.clear('photos');
};