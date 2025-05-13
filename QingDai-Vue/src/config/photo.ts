// 照片星级常量定义
export const PhotoStarRating = {
  VISIBLE: -2,     // 可见照片  包含NORMAL与STAR  数据库并不存在此类型
  HIDDEN: -1,     // 隐藏照片
  NORMAL: 0,      // 普通照片
  STAR: 1,        // 星标照片
  METEOROLOGY: 2, // 气象照片
  GROUP_ONLY: 3,  // 仅组图照片
};

// 获取照片星级对应的颜色
export const getStarColor = (startVal: number): string => {
  switch (startVal) {
    case PhotoStarRating.STAR:
      return 'gold'; // 星标
    case PhotoStarRating.METEOROLOGY:
      return 'darkturquoise'; // 气象
    case PhotoStarRating.GROUP_ONLY:
      return 'mediumorchid'; // 仅组图
    case PhotoStarRating.HIDDEN:
    case PhotoStarRating.NORMAL:
    default:
      return 'var(--qd-color-primary-light-7)'; // 隐藏、普通和默认
  }
};

// 获取照片星级的文本描述
export const getStarLabel = (startVal: number): string => {
  switch (startVal) {
    case PhotoStarRating.STAR:
      return '星标照片';
    case PhotoStarRating.NORMAL:
      return '普通照片';
    case PhotoStarRating.METEOROLOGY:
      return '气象照片';
    case PhotoStarRating.GROUP_ONLY:
      return '仅组图照片';
    case PhotoStarRating.HIDDEN:
      return '隐藏照片';
    default:
      return '未知状态';
  }
};

// 获取简化的照片星级标签（不含"照片"二字）
export const getShortStarLabel = (startVal: number): string => {
  switch (startVal) {
    case PhotoStarRating.STAR:
      return '星标';
    case PhotoStarRating.NORMAL:
      return '普通';
    case PhotoStarRating.METEOROLOGY:
      return '气象';
    case PhotoStarRating.GROUP_ONLY:
      return '组图';
    case PhotoStarRating.HIDDEN:
      return '隐藏';
    default:
      return '未知';
  }
}; 