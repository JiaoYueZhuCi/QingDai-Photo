// 瀑布流项基础类型[4,7](@ref)
export interface WaterfallItem {
  id: number
  thumbnail: string    // 缩略图路径（需存储于public或assets目录）
  fullSize: string     // 原图路径
  title: string
  author?: string      // 可选作者信息[4](@ref)
  width: number        // 原始宽度（像素）
  height: number       // 原始高度（像素）
  aspectRatio?: number // 自动计算的宽高比
  calcWidth?: number      // 自动计算的宽度（像素）
}

