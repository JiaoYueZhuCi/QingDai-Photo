<template>
    <div class="container" ref="containerRef">
        <PhotoPreview v-model="previewVisible" :photo-id="currentPreviewId" @close="previewVisible = false"
            @image-click="openFullImg" />

        <PhotoViewer v-if="fullImgShow" :photo-id="currentPreviewId" :initial-index="currentIndex"
            :use-direct-render="true" @close="fullImgShow = false" />

        <el-empty v-if="images.length === 0" description="暂无照片数据"></el-empty>

        <div class="container-in" v-else>
            <div class="image-row" v-for="(row, rowIndex) in rows" :key="rowIndex"
                :style="{ height: `${row.height}px`, width: `${rowWidth}px`, flex: '0 0 auto', margin: `0 ${sideMarginStyle} ${sideMarginStyle} ${sideMarginStyle}` }">
                <div class="image-item" v-for="(item, index) in row.items" :key="item.id"
                    @click="handleImageClick(item)">
                    <div class="image-actions">
                        <div class="left-actions">
                            <el-popover placement="top" :width="200" trigger="click" title="设置星标状态"
                                :popper-style="{ padding: '12px' }" :teleported="true">
                                <template #reference>
                                    <div class="action-icon" @click.stop>
                                        <el-icon :color="getStarColor(item.start)">
                                            <StarFilled v-if="item.start === 1" />
                                            <Star v-else :class="{ 'star-disabled': item.start === -1 }" />
                                        </el-icon>
                                    </div>
                                </template>
                                <div class="star-selection">
                                    <!-- <p>请选择此照片的星标状态：</p> -->
                                    <div class="star-option" @click="updateStarStatus(item, 1)">
                                        <span style="color: #e6a23c; font-size: 16px;">★</span> 星标照片
                                    </div>
                                    <div class="star-option" @click="updateStarStatus(item, 0)">
                                        <span style="color: #ffffff; font-size: 16px;">☆</span> 普通照片
                                    </div>
                                    <div class="star-option" @click="updateStarStatus(item, -1)">
                                        <span style="color: #c0c4cc; font-size: 16px;">☆</span> 隐藏照片
                                    </div>
                                </div>
                            </el-popover>
                            <div class="group-icon" @click.stop>
                                <el-popover placement="top" :width="200" trigger="click" title="添加到组图"
                                    :popper-style="{ padding: '12px' }" :teleported="true">
                                    <template #reference>
                                        <div class="action-icon">
                                            <el-icon>
                                                <Collection />
                                            </el-icon>
                                        </div>
                                    </template>
                                    <div class="group-selection">
                                        <div v-if="groupPhotos.length === 0" class="no-groups">
                                            暂无组图，请先创建组图
                                        </div>
                                        <div v-else class="group-options">
                                            <div v-for="group in groupPhotos" :key="group.groupPhoto.id"
                                                class="group-option" @click="addToGroup(item, group)">
                                                <div class="group-option-left">
                                                    <el-icon v-if="group.photoIds?.includes(item.id)"
                                                        class="check-icon">
                                                        <Check />
                                                    </el-icon>
                                                    <span>{{ group.groupPhoto.title || '未命名组图' }}</span>
                                                </div>
                                                <span class="photo-count">({{ group.photoIds?.length || 0 }}张)</span>
                                            </div>
                                        </div>
                                    </div>
                                </el-popover>
                            </div>

                        </div>
                        <div class="right-actions">
                            <div class="action-icon fullscreen-icon" @click.stop="openFullImg(item.id)">
                                <el-icon>
                                    <FullScreen />
                                </el-icon>
                            </div>

                        </div>
                    </div>
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
import { ref, onMounted, onUnmounted, watch, computed, toRaw } from 'vue';
import { ElImage, ElIcon, ElMessage, ElPopover, ElEmpty } from 'element-plus';
import { Picture as IconPicture, FullScreen, Star, StarFilled, Collection, Check } from '@element-plus/icons-vue';
import { getVisiblePhotosByPage, getStartPhotosByPage, updatePhotoStartStatus as updatePhotoStart } from '@/api/photo';
import { getAllGroupPhotos, updateGroupPhoto } from '@/api/groupPhoto';
import { debounce } from 'lodash';
import PhotoPreview from "@/components/PhotoPreview.vue";
import PhotoViewer from "@/components/PhotoViewer.vue";
import { get100KPhotos, processPhotoData, type EnhancedWaterfallItem } from '@/utils/photo';
import type { GroupPhotoDTO } from '@/types/groupPhoto';

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
const images = ref<EnhancedWaterfallItem[]>([]);
const currentPage = ref(1);
const pageSize = ref(30);
const hasMore = ref(true);
const containerRef = ref<HTMLElement | null>(null);

//  getPhotos 方法
const getPhotos = async () => {
    if (!hasMore.value) return;

    try {
        const apiEndpoint = props.photoType === 0
            ? getVisiblePhotosByPage
            : getStartPhotosByPage;

        const response = await apiEndpoint({
            page: currentPage.value,
            pageSize: pageSize.value
        });

        // 记录添加前的长度
        const previousLength = images.value.length;

        // 预处理数据，使用工具函数
        const processedData = response.records.map(processPhotoData);
        // 更新数据时保留原有数据
        images.value = [...images.value, ...processedData];
        // 计算布局
        calculateLayout();
        const sliceImages = images.value.slice(previousLength)
        // 使用统一工具函数加载缩略图
        await get100KPhotos(sliceImages);

        // 更新分页信息
        hasMore.value = response.records.length >= pageSize.value;
        if (hasMore.value) {
            currentPage.value++;
        }
    } catch (error) {
        console.error('获取照片数据失败:', error);
        ElMessage.error('照片加载失败');
    }
};

// 新增滚动事件处理
const handleScroll = debounce(() => {
    const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
    const scrollBottom = scrollHeight - (scrollTop + clientHeight);

    // 当距离底部小于 50px
    if (scrollBottom < 50 && hasMore.value) {
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
    await fetchGroupPhotos();
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

//// 预览相关状态
const previewVisible = ref(false);
const currentPreviewId = ref('');
const handleImageClick = (item: EnhancedWaterfallItem) => {
    currentPreviewId.value = item.id;
    previewVisible.value = true;
};

////计算图片宽度
interface RowData {
    items: EnhancedWaterfallItem[];
    height: number;
}
const rows = ref<RowData[]>([]);
// 定义计算属性
const sideMarginStyle = computed(() => `${sideMargin.value}px`);
const calculateLayout = () => {
    const rowsData: RowData[] = [];       // 存储所有行数据
    let currentRow: EnhancedWaterfallItem[] = []; // 当前行的图片集合
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

const fullImgShow = ref(false);
const currentIndex = ref(0);

const openFullImg = (id: string) => {
    currentPreviewId.value = id;
    fullImgShow.value = true;
    // 查找当前索引
    const index = images.value.findIndex(img => img.id === id);
    if (index !== -1) {
        currentIndex.value = index;
    }
};

// 获取星星颜色
const getStarColor = (startVal: number) => {
    if (startVal === 1) return '#e6a23c'; // 星标
    if (startVal === -1) return '#c0c4cc'; // 隐藏
    if (startVal === 2) return '#409eff'; // 气象
    return '#ffffff'; // 普通
};

// 更新星标状态
const updateStarStatus = async (item: EnhancedWaterfallItem, startVal: number) => {
    try {
        await updatePhotoStart({
            id: item.id,
            start: startVal
        });
        item.start = startVal;
        ElMessage.success('更新状态成功');
    } catch (error) {
        console.error('更新状态失败:', error);
        ElMessage.error('更新状态失败');
    }
};

// 组图相关
const groupPhotos = ref<GroupPhotoDTO[]>([]);

// 获取所有组图
const fetchGroupPhotos = async () => {
    try {
        const response = await getAllGroupPhotos();
        groupPhotos.value = response;
    } catch (error) {
        console.error('获取组图列表失败:', error);
        ElMessage.error('获取组图列表失败');
    }
};

// 添加到组图或从组图中移除
const addToGroup = async (photo: EnhancedWaterfallItem, group: GroupPhotoDTO) => {
    try {
        // 检查照片是否已经在组图中
        const isInGroup = group.photoIds?.includes(photo.id);

        // 更新组图数据
        const updatedGroup: GroupPhotoDTO = {
            groupPhoto: group.groupPhoto,
            photoIds: isInGroup
                ? group.photoIds.filter(id => id !== photo.id) // 如果已在组图中，则移除
                : [...(group.photoIds || []), photo.id] // 如果不在组图中，则添加
        };

        await updateGroupPhoto(updatedGroup);
        ElMessage.success(isInGroup ? '从组图中移除照片成功' : '添加照片到组图成功');

        // 更新本地组图数据
        const groupIndex = groupPhotos.value.findIndex(g => g.groupPhoto.id === group.groupPhoto.id);
        if (groupIndex !== -1) {
            groupPhotos.value[groupIndex] = updatedGroup;
        }
    } catch (error) {
        console.error('操作失败:', error);
        ElMessage.error('操作失败');
    }
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

.image-actions {
    position: absolute;
    top: 8px;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    padding: 0 8px;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.3s;
}

.image-item:hover .image-actions {
    opacity: 1;
}

.left-actions {
    display: flex;
    gap: 8px;
}

.right-actions {
    display: flex;
    gap: 8px;
}

.action-icon {
    cursor: pointer;
    border-radius: 4px;
    padding: 4px;
    transition: all 0.3s;
    color: white;
}

.action-icon:hover {
    background: rgba(0, 0, 0, 0.7);
    transform: scale(1.1);
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

.star-disabled {
    opacity: 0.5;
}

.star-selection {
    padding: 0;
}

.star-option {
    padding: 10px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 8px;
    transition: background-color 0.3s;
}

.star-option:hover {
    background-color: #f5f7fa;
}

.group-selection {
    padding: 0;
}

.group-options {
    max-height: 300px;
    overflow-y: auto;
}

.group-option {
    padding: 10px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 8px;
    transition: background-color 0.3s;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.group-option:hover {
    background-color: #f5f7fa;
}

.photo-count {
    color: #909399;
    font-size: 12px;
}

.no-groups {
    padding: 10px;
    color: #909399;
    text-align: center;
}

.group-option-left {
    display: flex;
    align-items: center;
    gap: 8px;
}

.check-icon {
    color: #67c23a;
    font-size: 16px;
}
</style>
