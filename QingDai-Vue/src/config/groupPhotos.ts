// 气象组图类型常量定义
export const MeteorologyGroupType = {
  SUNRISE_GLOW: '1',  // 朝霞
  SUNSET_GLOW: '2',   // 晚霞
  SUNRISE: '3',       // 日出
  SUNSET: '4',        // 日落
};

// 获取气象组图类型对应的名称
export const getMeteorologyGroupName = (type: string): string => {
  switch (type) {
    case MeteorologyGroupType.SUNRISE_GLOW:
      return '朝霞';
    case MeteorologyGroupType.SUNSET_GLOW:
      return '晚霞';
    case MeteorologyGroupType.SUNRISE:
      return '日出';
    case MeteorologyGroupType.SUNSET:
      return '日落';
    default:
      return '未知类型';
  }
};

