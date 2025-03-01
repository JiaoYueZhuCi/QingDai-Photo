<template>
    <div class="container" ref="container">
        <div class="container-in">
            <div class="image-row" v-for="(row, rowIndex) in rows" :key="rowIndex"
                :style="{ height: `${row.height}px`, flex: '0 0 auto' }">
                <div class="image-item" v-for="(item, index) in row.items" :key="item.id"
                    @click="openFatherFullImg(item.id, images)">
                    <el-image :src="item.compressedSrc" :key="item.id" lazy
                        :style="{ width: item.calcWidth + 'px', height: item.calcHeight + 'px' }">
                        <template #error>
                            <div class="error-image">
                                <el-icon :size="row.height"><icon-picture /></el-icon>
                            </div>
                        </template>
                    </el-image>
                    <div class="image-info">
                        <div>{{ item.title }}</div>
                        <div>{{ item.author || '摄影师' }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, onBeforeMount } from 'vue';
import type { Ref } from 'vue';
import type { WaterfallItem } from '../types';
import { ElImage, ElIcon } from 'element-plus';
import { Picture as IconPicture } from '@element-plus/icons-vue'
import { useImageStore } from '@/stores/imageStore'; // 引入 imageStore
import axios from 'axios'; // 引入 axios
import { debounce } from 'lodash';


//// 照片流数据
const images = ref<WaterfallItem[]>([]);

const fetchPhotos = async () => {
    try {
        const response = await axios.get('/api/QingDai/photo/getAllPhotos');
        // 预处理数据，确保所有字段类型匹配
        const processedData = response.data.map((item: any) => ({
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

        images.value = processedData;
    } catch (error) {
        console.error('获取照片数据失败:', error);
    }
};

// 在组件挂载时调用 fetchPhotos
onMounted(() => {
    fetchPhotos();
});

// 获取压缩图片
const fetchCompressedPhotos = async () => {
    for (let item of images.value) {
        try {
            const response = await axios.get('/api/QingDai/photo/getCompressedPhoto', {
                params: { id: item.id },
                responseType: 'blob',
            });
            item.compressedSrc = URL.createObjectURL(response.data);
        } catch (error) {
            console.error('未找到照片:', error);
        }
    }
};

const debouncedFetchCompressedPhotos = debounce(fetchCompressedPhotos, 300);

watch(images, async (newVal, oldVal) => {
    if (newVal.length > 0 && newVal !== oldVal) {
        await debouncedFetchCompressedPhotos();
        calculateLayout();
    }
}, { immediate: true, deep: true });


//// 定义 Emits
const emit = defineEmits<{
    (e: 'open-preview', item: WaterfallItem): void
    (e: 'open-full-preview', id: string, images:Ref<WaterfallItem[]>): void
}>();

//// 打开预览方法
const openFatherPreview = (item: WaterfallItem) => {
    // console.log('请求预览打开图片地址为:', item.fullSize);
    emit('open-preview', item); // 触发事件
};

//// 打开全屏
const openFatherFullImg = (id: string, images:Ref<WaterfallItem[]>) => {
    emit('open-full-preview', id, images); // 触发事件
};



////计算图片宽度
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

});

interface RowData {
    items: WaterfallItem[];
    height: number;
}

const rows = ref<RowData[]>([]);

const rowWidth = window.innerWidth - 20;   //总宽度减去瀑布流两侧20px
// 因为每行设置了justify-content: space-around;  所以gap实际为最小间隙（当照片+间隙刚好填满一行时）
const gap = props.gap; // 图片间隙

const calculateLayout = () => {
    const rowsData: RowData[] = [];       // 存储所有行数据
    let currentRow: WaterfallItem[] = []; // 当前行的图片集合
    let currentAspectRatioSum = 0;        // 当前行所有图片宽高比之和

    images.value.forEach((item) => {
        const aspectRatio = item.aspectRatio ?? item.width / item.height;// 计算宽高比（若未预计算）
        const newAspectSum = currentAspectRatioSum + aspectRatio;// 当前行所有图片宽高比之和
        const newGap = (currentRow.length) * gap; // 当前行图片的间隙总和

        const idealH = (rowWidth - newGap) / newAspectSum;// 计算理想行高
        let clampedH = Math.min(props.rowHeightMax, Math.max(props.rowHeightMin, idealH));// 限制行高

        const totalWidth = newAspectSum * clampedH + newGap; // 计算当前行总宽度

        if (totalWidth > rowWidth && currentRow.length > 0) { // 如果当前行总宽度超页面总宽度，则将当前行加入结果并开始新行
            const rowHeight = (rowWidth - (currentRow.length - 1) * gap) / currentAspectRatioSum;// 计算当前行总高度
            rowsData.push({
                items: [...currentRow],
                height: Math.min(props.rowHeightMax, Math.max(props.rowHeightMin, rowHeight)) // 限制行高
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
        const rowHeight = (rowWidth - (currentRow.length - 1) * gap) / currentAspectRatioSum;// 计算当前行总高度
        rowsData.push({
            items: currentRow,
            height: Math.min(props.rowHeightMax, Math.max(props.rowHeightMin, rowHeight))
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

.container-in {
    padding: 10px 0;
}

.image-row {
    display: flex;
    margin-bottom: 10px;
    /* gap: 10px; */
    justify-content: space-around;
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
</style>