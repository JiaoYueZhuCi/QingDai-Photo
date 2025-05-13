<template>
    <div class="container" ref="containerRef">
        <!-- 空状态显示 - 仅在数据加载完成且为空时显示 -->
        <el-empty v-if="!loading && images.length === 0" description="暂无组图数据"></el-empty>

        <!-- 瀑布流组件 -->
        <PhotoWaterfall
            v-if="images.length > 0"
            :images="images"
            :isShareMode="false"
            :selectedPhotos="[]"
            :useAlternativeStyle="true"
            :layoutOptions="layoutOptions"
            @imageClick="openGroupPhotoPreview"
        >
            <!-- 添加编辑按钮 -->
            <template #actions="{ item }">
                <div class="image-actions">
                    <div class="actions-container">
                        <div class="action-icon" @click.stop="handleEdit(item)">
                            <el-icon class="icon-color">
                                <Edit />
                            </el-icon>
                        </div>
                    </div>
                </div>
            </template>
        </PhotoWaterfall>

        <!-- 组图预览组件 -->
        <GroupFilmPreview
            v-model="groupFilmPreviewVisable"
            :group-id="selectedGroupId || ''"
            :initial-photo-id="selectedPhotoId || undefined"
            @close="closeGroupPhotoPreview"
        />

        <!-- 添加组图编辑组件 -->
        <GroupPhotoUpdate
            v-model="groupPhotoUpdateVisible"
            :edit-mode="true"
            :edit-data="currentEditData"
            @group-photo-updated="handleGroupPhotoUpdated"
            @close="closeGroupPhotoUpdate"
        />

        <!-- 添加加载指示器 -->
        <LoadMoreIndicator v-if="hasMore" text="加载更多组图..." />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import type { WaterfallItem } from '@/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getPhotosByIds } from '@/api/photo';
import { getGroupPhotosByPage, getGroupPhoto } from '@/api/groupPhoto';
import { debounce } from 'lodash';
import type { GroupPhotoDTO } from '@/types/groupPhoto';
import GroupFilmPreview from '@/components/group-photos/group-film-preview/GroupFilmPreview.vue';
import GroupPhotoUpdate from '@/components/group-photos/group-photos-update/GroupPhotosUpdate.vue';
import PhotoWaterfall from '@/components/photo/photo-waterfall/PhotoWaterfall.vue';
import { get100KPhotos, processPhotoData } from '@/utils/photo';
import { useRouter, useRoute } from 'vue-router';
import type { WaterfallLayoutOptions } from '@/components/photo/photo-viewer/useWaterfallLayout';
import { Edit } from '@element-plus/icons-vue';
import { PhotoPagination, InfiniteScrollConfig, WaterfallLayoutConfig } from '@/config/pagination';
import LoadMoreIndicator from '@/components/common/loading/LoadMoreIndicator.vue'

// 添加路由
const router = useRouter();
const route = useRoute();

// 定义组图瀑布流布局选项
const layoutOptions: WaterfallLayoutOptions = {
    rowHeightMax: WaterfallLayoutConfig.DESKTOP_ROW_HEIGHT_MAX,
    rowHeightMin: WaterfallLayoutConfig.DESKTOP_ROW_HEIGHT_MIN,
    defaultGap: WaterfallLayoutConfig.DESKTOP_GAP,
    defaultSideMargin: WaterfallLayoutConfig.DESKTOP_SIDE_MARGIN,
    mobileRowHeightMax: WaterfallLayoutConfig.MOBILE_ROW_HEIGHT_MAX,
    mobileRowHeightMin: WaterfallLayoutConfig.MOBILE_ROW_HEIGHT_MIN,
    mobileGap: WaterfallLayoutConfig.MOBILE_GAP,
    mobileSideMargin: WaterfallLayoutConfig.MOBILE_SIDE_MARGIN
};

// 添加新的状态保存选中的组图和照片
const selectedGroupId = ref<string | null>(null);
const selectedPhotoId = ref<string | null>(null);
const groupFilmPreviewVisable = ref(false);

// 添加组图编辑相关的状态
const groupPhotoUpdateVisible = ref(false);
const currentEditData = ref<GroupPhotoDTO | null>(null);

// 更新URL中的组图ID和照片ID参数
const updateUrlWithGroupId = (groupId: string | null, photoId: string | null = null) => {
    // 构建新的查询参数对象
    const query = { ...route.query };
    
    if (groupId) {
        query.groupId = groupId;
        if (photoId) {
            query.groupPhotoId = photoId;
        } else {
            delete query.groupPhotoId;
        }
        
        // 如果URL中有其他预览参数，清除它们
        if (query.photoId) delete query.photoId;
        if (query.viewerId) delete query.viewerId;
    } else {
        // 如果groupId为null，则删除相关参数
        delete query.groupId;
        delete query.groupPhotoId;
    }
    
    // 更新路由，保留当前路径，仅修改查询参数
    router.replace({
        path: route.path,
        query: query
    });
};

// 打开组图预览的方法
const openGroupPhotoPreview = (item: WaterfallItem) => {
    // 在处理后的数据中，我们需要找到这个照片对应的组图ID
    const groupId = item.groupId; 
    
    if (groupId) {
        selectedGroupId.value = groupId;
        selectedPhotoId.value = item.id;
        groupFilmPreviewVisable.value = true;
        // 更新URL
        updateUrlWithGroupId(groupId, item.id);
    } else {
        console.error('缺少组图ID，item:', item);
        ElMessage.warning('无法找到对应的组图信息');
    }
};

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedGroupId.value = null;
    selectedPhotoId.value = null;
    groupFilmPreviewVisable.value = false;
    // 清除URL参数
    updateUrlWithGroupId(null);
};

// 添加 props 来接收父组件传递的值
const props = defineProps({
    photoType: {
        type: Number,
        default: 0,
    },
});

//// 照片流数据
const images = ref<WaterfallItem[]>([]);
const currentPage = ref(1);
const pageSize = ref(PhotoPagination.GROUP_PHOTOS_PAGE_SIZE); // 每页加载的组图数量
const hasMore = ref(true);
const containerRef = ref<HTMLElement | null>(null);
const loading = ref(false);

// 获取组图照片
const getPhotos = async () => {
    if (!hasMore.value || loading.value) return;
    
    loading.value = true;

    try {
        const response = await getGroupPhotosByPage({
            page: currentPage.value,
            pageSize: pageSize.value
        });
        
        // 处理分页数据
        if (response && response.records) {
        // 获取所有封面图ID
            const coverPhotoIds = response.records
                .map((item: GroupPhotoDTO) => item.groupPhoto.coverPhotoId)
                .filter(Boolean);

        // 批量获取封面图信息
        let coverPhotos: WaterfallItem[] = [];
        if (coverPhotoIds.length > 0) {
            const coverResponse = await getPhotosByIds(coverPhotoIds);
            coverPhotos = coverResponse;
        }
        
        // 预处理数据
            const processedData = response.records.map((item: GroupPhotoDTO) => {
            const groupPhoto = item.groupPhoto;
            const coverPhoto = coverPhotos.find(p => p.id === groupPhoto.coverPhotoId);
            if (!coverPhoto) {
                console.error('未找到封面图');
                return null;
            }
            
            return processPhotoData({
                ...coverPhoto,
                title: groupPhoto.title || '',
                introduce: groupPhoto.introduce || '',
                groupId: groupPhoto.id
            });
        }).filter(Boolean);

        // 更新数据时保留原有数据
        images.value = [...images.value, ...processedData];

            // 获取新加载的图片并加载缩略图
            const newImgs = images.value.slice(images.value.length - processedData.length);
            await get100KPhotos(newImgs);

            // 判断是否还有更多数据
            hasMore.value = currentPage.value < response.pages;
            
            // 自动增加页码，为下次加载做准备
            if (hasMore.value) {
                currentPage.value++;
            }
        } else {
        hasMore.value = false;
        }
    } catch (error) {
        console.error('获取照片数据失败:', error);
        ElMessage.error('获取组图数据失败');
    } finally {
        loading.value = false;
    }
};

// 新增滚动事件处理
const handleScroll = debounce(() => {
    const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
    const scrollBottom = scrollHeight - (scrollTop + clientHeight);

    // 当距离底部小于配置的触发距离
    if (scrollBottom < InfiniteScrollConfig.TRIGGER_DISTANCE && hasMore.value) {
        getPhotos();
    }
}, InfiniteScrollConfig.THROTTLE_DELAY);

// 在 onMounted 中添加滚动监听
onMounted(async () => {
    currentPage.value = 1;
    images.value = [];
    hasMore.value = true;
    await getPhotos();
    
    // 检查URL中是否有组图ID参数
    if (route.query.groupId) {
        const groupIdFromUrl = route.query.groupId as string;
        const photoIdFromUrl = route.query.groupPhotoId as string || undefined;
        
        // 设置当前预览的组图ID和照片ID，并打开预览
        selectedGroupId.value = groupIdFromUrl;
        selectedPhotoId.value = photoIdFromUrl || null;
        groupFilmPreviewVisable.value = true;
    }
    
    window.addEventListener('scroll', handleScroll);
});

// 在 onUnmounted 中移除监听
onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});

// 监听 photoType 的变化
watch(() => props.photoType, (newVal, oldVal) => {
    if (newVal !== oldVal) {
        currentPage.value = 1;
        images.value = [];
        hasMore.value = true;
        getPhotos();
    }
});

// 处理编辑按钮点击
const handleEdit = async (item: WaterfallItem) => {
    if (!item.groupId) return;
    
    try {
        // 获取组图详细信息
        const groupPhoto = await getGroupPhoto(item.groupId);
        
        if (groupPhoto) {
            currentEditData.value = groupPhoto;
            groupPhotoUpdateVisible.value = true;
        } else {
            ElMessage.error('未找到组图信息');
        }
    } catch (error) {
        console.error('获取组图信息失败:', error);
        ElMessage.error('获取组图信息失败');
    }
};

// 处理组图更新完成
const handleGroupPhotoUpdated = () => {
    // 刷新数据
    currentPage.value = 1;
    images.value = [];
    hasMore.value = true;
    getPhotos();
};

// 关闭组图编辑
const closeGroupPhotoUpdate = () => {
    groupPhotoUpdateVisible.value = false;
    currentEditData.value = null;
};
</script>

<style>
.el-image-viewer__mask {
    opacity: 1 !important;
}

.body-no-scroll {
    overflow: hidden;
}

.image-actions {
    position: absolute;
    top: 8px;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
    padding: 0 8px;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.3s;
}

.image-item:hover .image-actions {
    opacity: 1;
}

.actions-container {
    display: flex;
    flex-wrap: wrap;
    justify-content: left;
    gap: 8px;
    width: 100%;
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

.icon-color {
    color: var(--qd-color-border-light);
}
</style>

<style scoped>
.container {
    width: 100%;
    margin: 0 0;
    padding: 0 0;
    background-color: var(--qd-color-bg-dark);
}
</style>
