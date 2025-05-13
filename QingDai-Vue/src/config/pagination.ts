// 分页和数据加载配置

// 首页照片瀑布流加载配置
export const PhotoPagination = {
  HOME_WATERFALL_PAGE_SIZE: 30,  // 首页照片瀑布流每页加载数量
  GROUP_PHOTOS_PAGE_SIZE: 20,    // 组图列表每页加载数量
  TIMELINE_PAGE_SIZE: 20,        // 时间线每页加载数量
  METEOROLOGY_TIMELINE_PAGE_SIZE: 20, // 气象时间线每页加载数量
};

// 管理页面表格加载配置
export const ManagePagination = {
  PHOTO_MANAGE_PAGE_SIZE: 30,        // 照片管理每页加载数量
  GROUP_PHOTOS_MANAGE_PAGE_SIZE: 30, // 组图管理每页加载数量
  TIMELINE_MANAGE_PAGE_SIZE: 30,     // 时间线管理每页加载数量
  SHARE_MANAGE_PAGE_SIZE: 30,        // 分享管理每页加载数量
};

// 无限滚动和懒加载配置
export const InfiniteScrollConfig = {
  // 触发加载下一页的距离（像素）
  TRIGGER_DISTANCE: 300,
  // 滚动加载节流时间（毫秒）
  THROTTLE_DELAY: 300,
};

// 照片瀑布流布局配置
export const WaterfallLayoutConfig = {
  // 桌面端行高配置
  DESKTOP_ROW_HEIGHT_MAX: 300,
  DESKTOP_ROW_HEIGHT_MIN: 150,
  DESKTOP_GAP: 10,
  DESKTOP_SIDE_MARGIN: 8,
  
  // 移动端行高配置
  MOBILE_ROW_HEIGHT_MAX: 200,
  MOBILE_ROW_HEIGHT_MIN: 100,
  MOBILE_GAP: 4,
  MOBILE_SIDE_MARGIN: 4,
}; 