import { ref, computed, onMounted, onUnmounted } from 'vue';
import type { EnhancedWaterfallItem } from '@/utils/photo';
import type { WaterfallItem } from '@/types';

// 定义通用的照片项类型，可以是 EnhancedWaterfallItem 或 WaterfallItem
export type PhotoItem = EnhancedWaterfallItem | WaterfallItem;

export interface RowData<T extends PhotoItem = PhotoItem> {
  items: T[];
  height: number;
}

export interface WaterfallLayoutOptions {
  rowHeightMax?: number;  // 最大行高
  rowHeightMin?: number;  // 最小行高
  defaultGap?: number;    // 默认间隙
  defaultSideMargin?: number; // 默认边距
  defaultContainerPadding?: number; // 默认容器内边距
  mobileBreakpoint?: number; // 移动端断点
  mobileRowHeightMax?: number; // 移动端最大行高
  mobileRowHeightMin?: number; // 移动端最小行高
  mobileGap?: number; // 移动端间隙
  mobileSideMargin?: number; // 移动端边距
  mobileContainerPadding?: number; // 移动端容器内边距
}

export function useWaterfallLayout<T extends PhotoItem = PhotoItem>(options?: WaterfallLayoutOptions) {
  // 设置默认值和从选项中获取的值
  const defaultOptions: WaterfallLayoutOptions = {
    rowHeightMax: 300,
    rowHeightMin: 150,
    defaultGap: 8,
    defaultSideMargin: 8,
    defaultContainerPadding: 8,
    mobileBreakpoint: 600,
    mobileRowHeightMax: 200,
    mobileRowHeightMin: 100,
    mobileGap: 1,
    mobileSideMargin: 1,
    mobileContainerPadding: 1
  };

  // 合并默认选项和用户选项
  const mergedOptions = { ...defaultOptions, ...options };

  // 响应式变量来存储动态的行高
  const rowHeightMax = ref<number>(mergedOptions.rowHeightMax!);
  const rowHeightMin = ref<number>(mergedOptions.rowHeightMin!);

  const sideMargin = ref(mergedOptions.defaultSideMargin!); // 边距
  const containerPadding = ref(mergedOptions.defaultContainerPadding!); // container-in的padding

  const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
  const gap = ref(mergedOptions.defaultGap!); // 因为每行设置了justify-content: space-between; 所以gap实际为最小间隙
  
  // 考虑container-in的padding
  const rowWidth = ref(window.innerWidth - scrollbarWidth - 2 * containerPadding.value);
  const rows = ref<RowData<T>[]>([]);
  
  // 定义计算属性
  const sideMarginStyle = computed(() => `${sideMargin.value}px`);
  
  // 监听窗口大小变化
  const handleResize = () => {
    if (window.innerWidth <= mergedOptions.mobileBreakpoint!) {
      rowHeightMax.value = mergedOptions.mobileRowHeightMax!;
      rowHeightMin.value = mergedOptions.mobileRowHeightMin!;
      gap.value = mergedOptions.mobileGap!; // 图片间隙
      sideMargin.value = mergedOptions.mobileSideMargin!; // 更新 sideMargin 变量
      containerPadding.value = mergedOptions.mobileContainerPadding!; // 小屏幕下container-in的padding
      rowWidth.value = window.innerWidth - 2 * containerPadding.value;  
    } else {
      rowHeightMax.value = mergedOptions.rowHeightMax!;
      rowHeightMin.value = mergedOptions.rowHeightMin!;
      gap.value = mergedOptions.defaultGap!; // 图片间隙
      sideMargin.value = mergedOptions.defaultSideMargin!; // 更新 sideMargin 变量
      containerPadding.value = mergedOptions.defaultContainerPadding!; // 大屏幕下container-in的padding
      rowWidth.value = window.innerWidth - scrollbarWidth - 2 * sideMargin.value - 2 * containerPadding.value;
    }
  };

  const calculateLayout = (images: T[]) => {
    const rowsData: RowData<T>[] = [];       // 存储所有行数据
    let currentRow: T[] = []; // 当前行的图片集合
    let currentAspectRatioSum = 0;        // 当前行所有图片宽高比之和

    images.forEach((item) => {
      const aspectRatio = item.aspectRatio ?? item.width / item.height;// 计算宽高比（若未预计算）
      const newAspectSum = currentAspectRatioSum + aspectRatio;// 当前行所有图片宽高比之和
      const newGap = (currentRow.length) * gap.value; // 当前行图片的间隙总和

      const idealH = (rowWidth.value - newGap) / newAspectSum;// 计算理想行高
      let clampedH = Math.min(rowHeightMax.value, Math.max(rowHeightMin.value, idealH));// 限制行高

      const totalWidth = newAspectSum * clampedH + newGap; // 计算当前行总宽度

      if (totalWidth > rowWidth.value && currentRow.length > 0) { // 如果当前行总宽度超页面总宽度，则将当前行加入结果并开始新行
        const rowHeight = (rowWidth.value - (currentRow.length - 1) * gap.value) / currentAspectRatioSum;// 计算当前行总高度
        rowsData.push({
          items: [...currentRow],
          height: Math.min(rowHeightMax.value, Math.max(rowHeightMin.value, rowHeight)) // 限制行高
        });

        // 重置当前行，以当前 item 开始新行
        currentRow = [item];
        currentAspectRatioSum = aspectRatio;   //更新当前行宽高比
      } else {
        // 将 item 加入当前行
        currentRow.push(item);
        currentAspectRatioSum = newAspectSum;
      }
    });

    // 处理最后一行
    if (currentRow.length > 0) {
      const rowHeight = (rowWidth.value - (currentRow.length - 1) * gap.value) / currentAspectRatioSum;// 计算当前行总高度
      rowsData.push({
        items: currentRow,
        height: Math.min(rowHeightMax.value, Math.max(rowHeightMin.value, rowHeight))
      });
    }

    // 计算每张图片的 calcWidth 和 calcHeight
    rowsData.forEach(row => {
      const rowHeight = row.height;
      row.items.forEach(item => {
        const aspectRatio = item.aspectRatio ?? item.width / item.height;
        item.calcWidth = rowHeight * aspectRatio;
        item.calcHeight = rowHeight;
      });
    });

    rows.value = rowsData;
  };

  onMounted(() => {
    window.addEventListener('resize', handleResize);
    handleResize();
  });

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });

  return {
    rows,
    gap,
    rowWidth,
    sideMarginStyle,
    containerPadding,
    calculateLayout,
    handleResize
  };
} 