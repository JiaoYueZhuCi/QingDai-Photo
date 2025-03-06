<template>
    <div class="container" ref="containerRef">
        <el-empty v-if="images.length === 0" description="暂无照片数据"></el-empty>

        <div class="container-in" v-else>
            <div class="image-row" v-for="(row, rowIndex) in rows" :key="rowIndex"
                :style="{ height: `${row.height}px`, width: `${rowWidth}px`, flex: '0 0 auto', margin: `0 ${sideMarginStyle} ${sideMarginStyle} ${sideMarginStyle}` }">
                <div class="image-item" v-for="(item, index) in row.items" :key="item.id"
                    @click="openFatherFullImg(item.id)">
                    <el-image :src="item.compressedSrc" :key="item.id" lazy
                        :style="{ width: item.calcWidth + 'px', height: item.calcHeight + 'px' }">
                        <template #error>
                            <div class="image-slot">
                                <el-icon><icon-picture /></el-icon>
                            </div>
                        </template>
                    </el-image>
                    <div class="image-info">
                        <div>{{ item.title || '标题' }}</div>
                        <div>{{ item.introduce || '文字介绍' }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue';
import type { Ref } from 'vue';
import type { WaterfallItem } from '@/types';
import { ElImage, ElIcon } from 'element-plus';
import { Picture as IconPicture } from '@element-plus/icons-vue'
import axios from 'axios'; // 引入 axios
import { debounce } from 'lodash';

// 添加 props 来接收父组件传递的值
const props = defineProps({
    rowHeightMax: {
        type: Number,
        default: 300,
    },
    rowHeightMin: {
        type: Number,
        default: 150,
    },
    gap: {
        type: Number,
        default: 10,
    },
    photoType: {
        type: Number,
        default: 0,
    },
});

//// 照片流数据
const images = ref<WaterfallItem[]>([]);

//
const currentPage = ref(1);
const pageSize = ref(100);
const hasMore = ref(true);
const containerRef = ref<HTMLElement | null>(null);

// 修改后的 getPhotos 方法
const getPhotos = async () => {
    if (!hasMore.value) return;

    try {
        const apiEndpoint = props.photoType === 0
            ? '/api/QingDai/photo/getVisiblePhotosByPage'
            : '/api/QingDai/photo/getStartPhotosByPage';

        const response = await axios.get(apiEndpoint, {
            params: {
                page: currentPage.value,
                pageSize: pageSize.value
            }
        });

        // 预处理数据，确保所有字段类型匹配
        const processedData = response.data.records.map((item: any) => ({
            id: item.id,
            fileName: item.fileName,
            author: item.author || '未知作者', // 设置默认值
            width: item.width || 0,
            height: item.height || 0,
            aperture: item.aperture || '',
            iso: item.iso || '',
            shutter: item.shutter || '',
            camera: item.camera || '', // 设置默认值
            lens: item.lens || '', // 设置默认值
            time: item.time || '',
            title: item.title || '', // 设置默认值
            introduce: item.introduce || '', // 设置默认值
            start: item.start || 0,
            aspectRatio: item.width / item.height, // 计算宽高比
            calcWidth: 0, // 初始化
            calcHeight: 0, // 初始化
            compressedSrc: '' // 初始化
        }));

        // 记录添加前的长度
        const previousLength = images.value.length;
        // 更新数据时保留原有数据
        images.value = [...images.value, ...processedData];
        // 计算布局
        calculateLayout();

        // 仅处理新增的数据项
        await getThumbnailPhotos(previousLength, images.value.length - 1);

        // 检查是否还有更多数据
        if (response.data.current >= response.data.pages) {
            hasMore.value = false;
        }
    } catch (error) {
        console.error('获取照片数据失败:', error);
    }
};

// 新增滚动事件处理
const handleScroll = debounce(() => {
    const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
    const scrollBottom = scrollHeight - (scrollTop + clientHeight);

    // 当距离底部小于 50px
    if (scrollBottom < 50 && hasMore.value) {
        currentPage.value++;
        getPhotos();
    }
}, 200);

// 新增响应式变量来存储动态的行高
const dynamicRowHeightMax = ref<number>(props.rowHeightMax);
const dynamicRowHeightMin = ref<number>(props.rowHeightMin);

const sideMargin = ref(8); // 边距

const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
const gap = ref(props.gap); // 因为每行设置了justify-content: space-between;  所以gap实际为最小间隙（当照片+间隙刚好填满一行时）
const rowWidth = ref(window.innerWidth - scrollbarWidth - 2 * sideMargin.value);   


// // 监听窗口大小变化
const handleResize = () => {
    if (window.innerWidth <= 600) {
        dynamicRowHeightMax.value = 200;
        dynamicRowHeightMin.value = 100;
        gap.value = 4; // 图片间隙
        sideMargin.value = 4; // 更新 sideMargin 变量
        rowWidth.value = window.innerWidth - 2 * sideMargin.value; // 调整 rowWidth   
    } else {
        dynamicRowHeightMax.value = props.rowHeightMax;
        dynamicRowHeightMin.value = props.rowHeightMin;
        gap.value = props.gap; // 图片间隙
        sideMargin.value = 8; // 更新 sideMargin 变量
        rowWidth.value = window.innerWidth - scrollbarWidth - 2 * sideMargin.value; // 恢复 rowWidth
    }
};

// 在 onMounted 中添加滚动监听
onMounted(async () => {
    handleResize();
    await getPhotos();
    window.addEventListener('scroll', handleScroll);
    window.addEventListener('resize', handleResize); // 添加 resize 事件监听
});

// 在 onUnmounted 中移除监听
onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
    window.removeEventListener('resize', handleResize); // 移除 resize 事件监听
});

// 修改 photoType 的 watch 处理
watch(() => props.photoType, (newVal, oldVal) => {
    if (newVal !== oldVal) {
        currentPage.value = 1;
        images.value = [];
        hasMore.value = true;
        getPhotos();
    }
});

// 缩略图获取方法（并行请求）
const getThumbnailPhotos = async (start: number, end: number) => {
    const requests = [];
    for (let i = start; i <= end; i++) {
        const item = images.value[i];
        if (!item.compressedSrc) { // 避免重复请求
            requests.push(
                axios.get('/api/QingDai/photo/getCompressedPhoto', {
                    params: { id: item.id },
                    responseType: 'blob',
                }).then(response => {
                    item.compressedSrc = URL.createObjectURL(response.data);
                }).catch(error => {
                    console.error('未找到照片:', error);
                    item.compressedSrc = ''; // 设置默认错误
                })
            );
        }
    }
    await Promise.all(requests);
};

//// 定义 Emits
const emit = defineEmits<{
    (e: 'open-preview', item: WaterfallItem): void
    (e: 'open-full-preview', id: string, images: Ref<WaterfallItem[]>): void
}>();

//// 打开预览方法
const openFatherPreview = (item: WaterfallItem) => {
    // console.log('请求预览打开图片地址为:', item.fullSize);
    emit('open-preview', item); // 触发事件
};

//// 打开全屏
const openFatherFullImg = (id: string) => {
    emit('open-full-preview', id, images); // 触发事件
};



////计算图片宽度

interface RowData {
    items: WaterfallItem[];
    height: number;
}

const rows = ref<RowData[]>([]);

// 定义计算属性
const sideMarginStyle = computed(() => `${sideMargin.value}px`);

const calculateLayout = () => {
    const rowsData: RowData[] = [];       // 存储所有行数据
    let currentRow: WaterfallItem[] = []; // 当前行的图片集合
    let currentAspectRatioSum = 0;        // 当前行所有图片宽高比之和

    images.value.forEach((item) => {
        const aspectRatio = item.aspectRatio ?? item.width / item.height;// 计算宽高比（若未预计算）
        const newAspectSum = currentAspectRatioSum + aspectRatio;// 当前行所有图片宽高比之和
        const newGap = (currentRow.length) * gap.value; // 当前行图片的间隙总和

        const idealH = (rowWidth.value - newGap) / newAspectSum;// 计算理想行高
        let clampedH = Math.min(dynamicRowHeightMax.value, Math.max(dynamicRowHeightMin.value, idealH));// 限制行高

        const totalWidth = newAspectSum * clampedH + newGap; // 计算当前行总宽度

        if (totalWidth > rowWidth.value && currentRow.length > 0) { // 如果当前行总宽度超页面总宽度，则将当前行加入结果并开始新行
            const rowHeight = (rowWidth.value - (currentRow.length - 1) * gap.value) / currentAspectRatioSum;// 计算当前行总高度
            rowsData.push({
                items: [...currentRow],
                height: Math.min(dynamicRowHeightMax.value, Math.max(dynamicRowHeightMin.value, rowHeight)) // 限制行高
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
            height: Math.min(dynamicRowHeightMax.value, Math.max(dynamicRowHeightMin.value, rowHeight))
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

//组件挂载时计算一次布局，并监听窗口resize事件
// onMounted(() => {
//     fetchCompressedPhotos(); // 添加此行
//     calculateLayout();

//     window.addEventListener('resize', calculateLayout);
// });

</script>


<style scoped>
.container {
    width: 100%;
    margin: 0 0;
    padding: 0 0;
    background-color: black;
}


.image-item {
    position: relative;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    background: #fff;
    transition: transform 0.3s;
    cursor: pointer;
}

.image-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}


.image-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    color: white;
    font-size: 14px;
    opacity: 0;
    transition: opacity 0.3s;
}

.image-item:hover .image-info {
    opacity: 1;
}

/* el-image内部img配置适应容器 */
:deep(.el-image__inner) {
    /* width: 100% !important; */
    height: 100% !important;
    /* object-fit: cover !important; */
}

.image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
    font-size: 30px;
}

.image-slot .el-icon {
    font-size: 30px;
}

.container-in {
    padding: 8px 0;
}

.image-row {
    display: flex;
    justify-content: space-between;
}

@media (max-width: 600px) {
    .container-in {
        padding: 4px 0;
    }

    .image-row {}
}
</style>
