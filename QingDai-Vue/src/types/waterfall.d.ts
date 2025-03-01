export interface WaterfallItem {
  id: string            // 照片id 
  fileName: string      // 照片名称
  author: string        // 作者信息
  width: number         // 原始宽度（像素）
  height: number        // 原始高度（像素）
  aperture: string      // 光圈
  iso: string           // 感光度
  shutter: string       // 快门
  camera: string        // 相机
  lens: string          // 镜头
  time: string          // 拍摄时间
  title: string         // 标题
  introduce: string     // 介绍
  start: number         //代表作
  aspectRatio?: number  // 自动计算的宽高比
  calcWidth?: number    // 自动计算的宽度（像素）
  calcHeight?: number   // 自动计算的高度（像素）
  compressedSrc?:string //compressedSrc



}

