<template>
    <div class="container" ref="containerRef">

        <el-empty v-if="images.length === 0" description="暂无照片数据"></el-empty>

        <div class="container-in" v-else>
            <div class="image-row" v-for="(row, rowIndex) in rows" :key="rowIndex"
                :style="{ height: `${row.height}px`, width: `${rowWidth}px`, flex: '0 0 auto', margin: `0 ${sideMarginStyle} ${sideMarginStyle} ${sideMarginStyle}` }">
                <div class="image-item" v-for="(item, index) in row.items" :key="item.id"
                    @click="openGroupPhotoPreview(item)">
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

        <!-- 组图预览组件 -->
        <group-photo-preview
            v-if="selectedGroupId !== null"
            :group-id="selectedGroupId || ''"
            :initial-photo-id="selectedPhotoId || undefined"
            @close="closeGroupPhotoPreview"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import type { WaterfallItem } from '@/types';
import { ElImage, ElIcon, ElMessage, ElLoading, ElPopover, ElEmpty } from 'element-plus';
import { Picture as IconPicture, FullScreen, Star, StarFilled } from '@element-plus/icons-vue'
import { getVisiblePhotosByPage, getPhotosByIds, getThumbnail100KPhotos, updatePhotoStartStatus as updatePhotoStart  } from '@/api/photo';
import { getAllGroupPhotos } from '@/api/groupPhoto';
import { debounce } from 'lodash';
import JSZip from 'jszip';
import type { GroupPhotoDTO } from '@/types/groupPhoto';
import GroupPhotoPreview from '@/components/GroupPhotoPreview.vue';

// 添加新的状态保存选中的组图和照片
const selectedGroupId = ref<string | null>(null);
const selectedPhotoId = ref<string | null>(null);

// 打开组图预览的方法
const openGroupPhotoPreview = (item: WaterfallItem) => {
    // 在处理后的数据中，我们需要找到这个照片对应的组图ID
    const groupId = item.groupId; 
    
    if (groupId) {
        selectedGroupId.value = groupId;
        selectedPhotoId.value = item.id;
    } else {
        console.error('缺少组图ID，item:', item);
        ElMessage.warning('无法找到对应的组图信息');
    }
};

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedGroupId.value = null;
    selectedPhotoId.value = null;
};

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
const currentPage = ref(1);
const pageSize = ref(50);
const hasMore = ref(true);
const containerRef = ref<HTMLElement | null>(null);

//  getPhotos 方法
const getPhotos = async () => {
    if (!hasMore.value) return;

    try {
        const response = await getAllGroupPhotos();
        // 获取所有封面图ID
        const coverPhotoIds = response.map((item: GroupPhotoDTO) => item.groupPhoto.coverPhotoId).filter(Boolean);

        
        // 批量获取封面图信息
        let coverPhotos:WaterfallItem[] = [];
        if (coverPhotoIds.length > 0) {
            const coverResponse = await getPhotosByIds(coverPhotoIds);
            coverPhotos = coverResponse;
        }
        
        // 预处理数据
        const processedData = response.map((item: GroupPhotoDTO) => {
            const groupPhoto = item.groupPhoto;
            const coverPhoto = coverPhotos.find(p => p.id === groupPhoto.coverPhotoId);
            if (!coverPhoto) {
                console.error('未找到封面图');
                return null;
            }
            
            return {
                id: coverPhoto.id || '',
                fileName: coverPhoto.fileName || '',
                author: coverPhoto.author || '未知作者',
                width: coverPhoto.width || 0,
                height: coverPhoto.height || 0,
                aperture: coverPhoto.aperture || '',
                iso: coverPhoto.iso || '',
                shutter: coverPhoto.shutter || '',
                camera: coverPhoto.camera || '',
                lens: coverPhoto.lens || '',
                time: coverPhoto.time || '',
                title: groupPhoto.title || '',
                introduce: groupPhoto.introduce || '',
                start: coverPhoto.start || 0,
                aspectRatio: coverPhoto.width / coverPhoto.height ,
                calcWidth: 0,
                calcHeight: 0,
                compressedSrc: coverPhoto.compressedSrc || '',
                groupId: groupPhoto.id
            };
        });

        // 记录添加前的长度
        const previousLength = images.value.length;
        // 更新数据时保留原有数据
        images.value = [...images.value, ...processedData];
        // 计算布局
        calculateLayout();

        // 仅处理新增的数据项
        await getThumbnailPhotos(previousLength, images.value.length - 1);

        // 由于API一次性返回所有数据，设置hasMore为false
        hasMore.value = false;
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

// 响应式变量来存储动态的行高
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
    calculateLayout(); // 重新计算布局
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

// 缩略图获取方法
const getThumbnailPhotos = async (start: number, end: number) => {
    const newItems = images.value.slice(start, end + 1).filter(item => !item.compressedSrc);
    if (newItems.length === 0) return;

    try {
        // 获取需要请求的照片ID
        const photoIds = newItems.map(item => item.id);
        
        // 创建ID到项目的映射，用于后续查找
        const idToItemsMap = new Map<string, WaterfallItem[]>();
        newItems.forEach((item: WaterfallItem) => {
            if (!idToItemsMap.has(item.id)) {
                idToItemsMap.set(item.id, []);
            }
            idToItemsMap.get(item.id)!.push(item);
        });
        
        // 去重后的照片ID
        const uniquePhotoIds = [...new Set(photoIds)];
        
        // 调用API获取缩略图，只传递唯一的ID
        const response = await getThumbnail100KPhotos(
            uniquePhotoIds.join(',')
        );

        const zip = await JSZip.loadAsync(response.data);
        
        // 创建从照片ID到URL的映射
        const photoIdToUrlMap = new Map();
        
        // 处理每个唯一的照片ID
        for (const photoId of uniquePhotoIds) {
            const items = idToItemsMap.get(photoId) || [];
            if (items.length > 0) {
                const fileName = items[0].fileName;
                const file = zip.file(`${fileName}`);
                
                if (file) {
                    const blob = await file.async('blob');
                    const url = URL.createObjectURL(blob);
                    // 存储ID到URL的映射
                    photoIdToUrlMap.set(photoId, url);
                    
                    // 为所有使用该ID的项设置相同的URL
                    items.forEach(item => {
                        item.compressedSrc = url;
                    });
                } else {
                    // 文件未找到的情况
                    items.forEach(item => {
                        item.compressedSrc = '';
                    });
                    console.error('ZIP包中未找到照片:', photoId);
                }
            }
        }
    } catch (error) {
        console.error('批量获取压缩图失败:', error);
        newItems.forEach(item => item.compressedSrc = '');
    }
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
</script>

<style>
.el-image-viewer__mask {
    opacity: 1 !important;
}

.body-no-scroll {
    overflow: hidden;
}
</style>

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


.image-item:hover .fullscreen-icon {
    background: rgba(0, 0, 0, 0.7);
    transform: scale(1.1);
    opacity: 1;
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

}

</style>
