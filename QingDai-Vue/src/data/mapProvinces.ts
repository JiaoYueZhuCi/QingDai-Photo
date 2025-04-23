/**
 * 地图相关数据管理
 */

// 省份数据对象数组，包含代码、名称和是否点亮状态
export interface ProvinceData {
  adcode: number;
  name: string;
  isActive: boolean;
}

// 所有省份数据
export const allProvinces: ProvinceData[] = [
  { adcode: 110000, name: '北京市', isActive: true },
  { adcode: 120000, name: '天津市', isActive: true },
  { adcode: 130000, name: '河北省', isActive: true },
  { adcode: 140000, name: '山西省', isActive: true },
  { adcode: 150000, name: '内蒙古自治区', isActive: true },
  { adcode: 210000, name: '辽宁省', isActive: true },
  { adcode: 220000, name: '吉林省', isActive: false },
  { adcode: 230000, name: '黑龙江省', isActive: false },
  { adcode: 310000, name: '上海市', isActive: true },
  { adcode: 320000, name: '江苏省', isActive: true },
  { adcode: 330000, name: '浙江省', isActive: true },
  { adcode: 340000, name: '安徽省', isActive: false },
  { adcode: 350000, name: '福建省', isActive: true },
  { adcode: 360000, name: '江西省', isActive: false },
  { adcode: 370000, name: '山东省', isActive: true },
  { adcode: 410000, name: '河南省', isActive: false },
  { adcode: 420000, name: '湖北省', isActive: false },
  { adcode: 430000, name: '湖南省', isActive: true },
  { adcode: 440000, name: '广东省', isActive: true },
  { adcode: 450000, name: '广西壮族自治区', isActive: true },
  { adcode: 460000, name: '海南省', isActive: true },
  { adcode: 500000, name: '重庆市', isActive: true },
  { adcode: 510000, name: '四川省', isActive: true },
  { adcode: 520000, name: '贵州省', isActive: false },
  { adcode: 530000, name: '云南省', isActive: false },
  { adcode: 540000, name: '西藏自治区', isActive: false },
  { adcode: 610000, name: '陕西省', isActive: true },
  { adcode: 620000, name: '甘肃省', isActive: false },
  { adcode: 630000, name: '青海省', isActive: false },
  { adcode: 640000, name: '宁夏回族自治区', isActive: false },
  { adcode: 650000, name: '新疆维吾尔自治区', isActive: false },
  { adcode: 710000, name: '台湾省', isActive: false },
  { adcode: 810000, name: '香港特别行政区', isActive: false },
  { adcode: 820000, name: '澳门特别行政区', isActive: false }
];

// 获取已激活的省份代码列表（兼容旧版使用）
export const activatedProvinces = allProvinces
  .filter(province => province.isActive)
  .map(province => province.adcode);

// 省份代码与名称映射（兼容旧版使用）
export const provinceCodeToName: Record<number, string> = allProvinces.reduce((acc, province) => {
  acc[province.adcode] = province.name;
  return acc;
}, {} as Record<number, string>); 