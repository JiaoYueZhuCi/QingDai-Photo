<template>
    <div class="container" ref="containerRef">
        <!-- 分享组件 -->
        <PhotoShare 
            v-model:isShareMode="isShareMode" 
            v-model:selectedPhotos="selectedPhotos"
            @removePhoto="removeSelectedPhoto"
        />

        <!-- 预览和查看组件 -->
        <FilmPreview 
            v-model="previewVisible" 
            :photo-id="currentPreviewId" 
            :photo-ids="extractedPhotoIds"
            @close="handlePreviewClose" 
            @image-click="handleFilmPreviewImageClick" 
            @navigate="handleNavigate" 
        />

        <PhotoViewer 
            v-model="viewerVisible" 
            :photo-id="currentPreviewId" 
            :initial-index="currentIndex"
            :use-direct-render="true" 
            @close="handleViewerClose" 
        />

        <PhotoEditor 
            v-model="editorVisible" 
            :photo-id="currentPreviewId" 
            @updated="handlePhotoUpdated" 
            @close="handleEditorClose"
        />

        <!-- 空状态显示 - 仅在数据加载完成且为空时显示 -->
        <el-empty v-if="!loading && images.length === 0" description="暂无照片数据"></el-empty>

        <!-- 瀑布流组件 -->
        <PhotoWaterfall
            v-if="images.length > 0"
            :images="images"
            :isShareMode="isShareMode"
            :selectedPhotos="selectedPhotos"
            @imageClick="handleImageClick"
            @togglePhotoSelection="togglePhotoSelection"
        >
            <template #actions="{ item }">
                <PhotoActions 
                    :photo="item" 
                    @statusUpdated="handlePhotoUpdated" 
                    @edit="openPhotoEditor" 
                    @fullscreen="openFullImg"
                />
            </template>
        </PhotoWaterfall>

        <!-- 加载更多指示器 -->
        <LoadMoreIndicator v-if="hasMore" text="加载更多照片..." />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { getVisiblePhotosByPage, getStartPhotosByPage, getHiddenPhotosByPage, getMeteorologyPhotosByPage } from '@/api/photo';
import { debounce } from 'lodash';
import FilmPreview from "@/components/photo/file-preview/FilmPreview.vue";
import PhotoViewer from "@/components/photo/photo-viewer/PhotoViewer.vue";
import PhotoEditor from "@/views/home/photo/photo-editor/PhotoEditor.vue";
import PhotoWaterfall from "@/components/photo/photo-waterfall/PhotoWaterfall.vue";
import PhotoActions from "@/views/home/photo/photo-actions/PhotoActions.vue";
import PhotoShare from "@/views/home/photo/photo-share/PhotoShare.vue";
import { get100KPhotos, processPhotoData, type EnhancedWaterfallItem } from '@/utils/photo';
import { useRouter, useRoute } from 'vue-router';
import { PhotoPagination, InfiniteScrollConfig } from '@/config/pagination';
import { PhotoStarRating } from '@/config/photo';
import LoadMoreIndicator from '@/components/common/loading/LoadMoreIndicator.vue'

// 添加路由
const router = useRouter();
const route = useRoute();

// 添加 props 来接收父组件传递的值
const props = defineProps({
    photoType: {
        type: Number,
        default: PhotoStarRating.NORMAL,
    },
});

//// 照片流数据
const images = ref<EnhancedWaterfallItem[]>([]);
const currentPage = ref(1);
const pageSize = ref(PhotoPagination.HOME_WATERFALL_PAGE_SIZE);
const hasMore = ref(true);
const containerRef = ref<HTMLElement | null>(null);

// 添加loading状态
const loading = ref(false);

//  getPhotos 方法
const getPhotos = async () => {
    if (!hasMore.value || loading.value) return;
    
    loading.value = true;

    try {
        let apiEndpoint;

        // 根据photoType选择不同的API
        switch (props.photoType) {
            case PhotoStarRating.VISIBLE: // 可见照片
                apiEndpoint = getVisiblePhotosByPage;
                break;
            case PhotoStarRating.STAR: // 星标照片
                apiEndpoint = getStartPhotosByPage;
                break;
            case PhotoStarRating.HIDDEN: // 隐藏照片
                apiEndpoint = getHiddenPhotosByPage;
                break;
            case PhotoStarRating.METEOROLOGY: // 气象照片
                apiEndpoint = getMeteorologyPhotosByPage;
                break;
            case PhotoStarRating.GROUP_ONLY: // 仅组图照片
                apiEndpoint = getVisiblePhotosByPage; // ！！！！仅组图照片暂时使用可见照片API
                break;
            default:
                apiEndpoint = getVisiblePhotosByPage;
        }

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
    await getPhotos();

    // 检查URL中是否有photoId或viewerId参数
    if (route.query.photoId) {
        const photoIdFromUrl = route.query.photoId as string;
        // 设置当前预览的照片ID并打开预览
        currentPreviewId.value = photoIdFromUrl;
        previewVisible.value = true;
    } else if (route.query.viewerId) {
        const viewerIdFromUrl = route.query.viewerId as string;
        // 设置当前查看的照片ID并打开查看器
        currentPreviewId.value = viewerIdFromUrl;
        // 查找当前索引
        const index = images.value.findIndex(img => img.id === viewerIdFromUrl);
        if (index !== -1) {
            currentIndex.value = index;
        }
        // 等待照片加载完成后再打开查看器
        setTimeout(() => {
            viewerVisible.value = true;
        }, 500); // 给一点时间让照片加载
    }

    window.addEventListener('scroll', handleScroll);
});

// 在 onUnmounted 中移除监听
onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
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
const editorVisible = ref(false);
const currentPreviewId = ref('');
const handleImageClick = (item: EnhancedWaterfallItem, event?: MouseEvent) => {
    if (isShareMode.value) {
        if (event) event.stopPropagation();
        togglePhotoSelection(item, !selectedPhotos.value.includes(item));
    } else {
        currentPreviewId.value = item.id;
        previewVisible.value = true;
        viewerVisible.value = false;  
        updateUrlWithPhotoId(item.id);
    }
};

// 添加编辑器关闭的处理函数
const handleEditorClose = () => {
    editorVisible.value = false;
    // 确保关闭PhotoEditor后解锁页面滚动
    document.body.style.overflow = '';
};

// 处理FilmPreview组件中图片点击事件
const handleFilmPreviewImageClick = (photoId: string) => {
    if (!photoId) {
        ElMessage({
            type: 'error',
            message: '照片ID无效，无法打开查看器',
            duration: 3000
        })
        return
    }
    
    currentPreviewId.value = photoId
    viewerVisible.value = true
    
    // 查找当前索引
    const index = images.value.findIndex(img => img.id === photoId)
    if (index !== -1) {
        currentIndex.value = index
    }
    // 更新URL
    updateUrlWithViewerId(photoId)
}

// 处理预览关闭
const handlePreviewClose = () => {
    previewVisible.value = false;
    // 清除URL中的照片ID参数
    updateUrlWithPhotoId(null);
};

// 更新URL中的照片ID参数
const updateUrlWithPhotoId = (photoId: string | null) => {
    // 构建新的查询参数对象
    const query = { ...route.query };

    if (photoId) {
        query.photoId = photoId;
        // 如果同时打开了全屏查看器，则清除viewerId参数避免冲突
        if (query.viewerId) {
            delete query.viewerId;
        }
    } else {
        // 如果photoId为null，则删除该参数
        delete query.photoId;
    }

    // 更新路由，保留当前路径，仅修改查询参数
    router.replace({
        path: route.path,
        query: query
    });
};

// 更新URL中的查看器ID参数
const updateUrlWithViewerId = (viewerId: string | null) => {
    // 构建新的查询参数对象
    const query = { ...route.query };

    if (viewerId) {
        query.viewerId = viewerId;
        // 如果同时有预览窗口打开，则清除photoId参数避免冲突
        if (query.photoId) {
            delete query.photoId;
        }
    } else {
        // 如果viewerId为null，则删除该参数
        delete query.viewerId;
    }

    // 更新路由，保留当前路径，仅修改查询参数
    router.replace({
        path: route.path,
        query: query
    });
};

const viewerVisible = ref(false);
const currentIndex = ref(0);

const openFullImg = (id: string) => {
    if (!id) {
        ElMessage({
            type: 'error',
            message: '照片ID无效，无法打开查看器',
            duration: 3000
        })
        return
    }
    
    currentPreviewId.value = id;
    viewerVisible.value = true;
    // 查找当前索引
    const index = images.value.findIndex(img => img.id === id);
    if (index !== -1) {
        currentIndex.value = index;
    }
    // 更新URL，添加查看器ID参数
    updateUrlWithViewerId(id);
};

// 处理PhotoViewer关闭
const handleViewerClose = () => {
    viewerVisible.value = false;
    // 清除URL中的viewerId参数
    updateUrlWithViewerId(null);
};

// 提取所有照片ID，供FilmPreview组件使用
const extractedPhotoIds = computed(() => {
    return images.value.map(image => image.id);
});

// 处理FilmPreview的导航事件
const handleNavigate = (photoId: string) => {
    currentPreviewId.value = photoId;
    // 更新URL中的照片ID
    updateUrlWithPhotoId(photoId);
};

// 添加图片编辑图标
const openPhotoEditor = (item: EnhancedWaterfallItem) => {
    // 先确保页面滚动状态被重置，避免多次锁定
    document.body.style.overflow = '';
    
    currentPreviewId.value = item.id;
    editorVisible.value = true;
};

// 处理照片信息更新
const handlePhotoUpdated = (updatedPhoto: any) => {
    // 更新本地数据
    const index = images.value.findIndex(img => img.id === updatedPhoto.id);
    if (index !== -1) {
        // 更新对应字段
        images.value[index].title = updatedPhoto.title;
        images.value[index].introduce = updatedPhoto.introduce;
        images.value[index].start = updatedPhoto.start;
        images.value[index].camera = updatedPhoto.camera;
        images.value[index].lens = updatedPhoto.lens;
        images.value[index].aperture = updatedPhoto.aperture;
        images.value[index].shutter = updatedPhoto.shutter;
        images.value[index].iso = updatedPhoto.iso;
    }
    
    // 确保关闭PhotoEditor后解锁页面滚动
    document.body.style.overflow = '';
};

// 分享相关状态
const isShareMode = ref(false);
const selectedPhotos = ref<EnhancedWaterfallItem[]>([]);

// 切换照片选择状态
const togglePhotoSelection = (photo: EnhancedWaterfallItem, selected: boolean) => {
    if (selected) {
        if (!selectedPhotos.value.includes(photo)) {
            selectedPhotos.value = [...selectedPhotos.value, photo];
        }
    } else {
        selectedPhotos.value = selectedPhotos.value.filter(p => p.id !== photo.id);
    }
};

// 移除已选择的照片
const removeSelectedPhoto = (photoId: string) => {
    const index = selectedPhotos.value.findIndex(photo => photo.id === photoId);
    if (index !== -1) {
        selectedPhotos.value.splice(index, 1);
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
    background-color: var(--qd-color-bg-dark);
}
</style>
