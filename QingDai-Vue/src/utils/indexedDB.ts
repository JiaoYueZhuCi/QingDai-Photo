// 根据错误提示，需要使用仅类型导入来导入 DBSchema 和 IDBPDatabase
import { openDB } from 'idb';
import type { DBSchema, IDBPDatabase } from 'idb';

interface PhotoDB extends DBSchema {
  photos: {
    key: string;
    value: string;
  };
}

export const initDB = async (): Promise<IDBPDatabase<PhotoDB>> => {
  return await openDB<PhotoDB>('photo-cache', 1, {
    upgrade(db) {
      if (!db.objectStoreNames.contains('photos')) {
        db.createObjectStore('photos', { keyPath: 'key' });
      }
    },
  });
};

export const getPhotoFromDB = async (id: string): Promise<string | undefined> => {
  const db = await initDB();
  const tx = db.transaction('photos', 'readonly');
  const store = tx.objectStore('photos');
  const result = await store.get(id);
  await tx.done;
  return result?.value;
};

export const savePhotoToDB = async (id: string, url: string): Promise<void> => {
  const db = await initDB();
  const tx = db.transaction('photos', 'readwrite');
  const store = tx.objectStore('photos');
  // Bug修复：原代码尝试将对象传递给需要字符串类型参数的方法，现修改为正确传递对象
  await store.put({ key: id, value: url });
  await tx.done;
};

export const clearPhotoDB = async (): Promise<void> => {
  const db = await initDB();
  const tx = db.transaction('photos', 'readwrite');
  const store = tx.objectStore('photos');
  await store.clear();
  await tx.done;
};