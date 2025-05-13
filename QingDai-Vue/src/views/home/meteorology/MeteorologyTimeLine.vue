<template>
    <div class="sunglow-timeline-container">
        <div class="timeline-content">
            <!-- 空状态显示 - 仅在数据加载完成且为空时显示 -->
            <el-empty v-if="!loading && photos.length === 0" description="暂无气象照片"></el-empty>
            
            <!-- 仅在有数据时显示统计信息和照片列表 -->
            <template v-if="photos.length > 0">
                <!-- 统计信息展示 -->
                <photo-stats :type-name="typeName" :photo-count="photos.length" />

                <!-- 照片列表 -->
                <timeline-photos :photos="photos" @photoClick="handleImageClick" />
            </template>

            <!-- 加载更多指示器 -->
            <load-more-indicator v-if="hasMore" text="加载更多气象组图..." />
        </div>

        <!-- 组图预览对话框 -->
        <group-film-preview 
            v-model="groupFilmPreviewVisable"
            :group-id="selectedGroupId"
            :initial-photo-id="selectedPhotoId || undefined" 
            @close="closeGroupPhotoPreview"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch} from 'vue'
import { getGroupPhoto } from '@/api/groupPhoto'
import { getPhotosByIds } from '@/api/photo'
import type { GroupPhotoDTO } from '@/types/groupPhoto'
import type { WaterfallItem } from '@/types'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash'
import GroupFilmPreview from '@/components/group-photos/group-film-preview/GroupFilmPreview.vue'
import { get100KPhotos, processPhotoData, type EnhancedWaterfallItem } from '@/utils/photo'
import PhotoStats from '@/views/home/meteorology/photo-stats/PhotoStats.vue'
import TimelinePhotos from '@/views/home/meteorology/timeline-photos/TimelinePhotos.vue'
import { getMeteorologyTypeName } from '@/views/home/meteorology/meteorologyUtils'
import { useRoute, useRouter } from 'vue-router'
import { PhotoPagination, InfiniteScrollConfig } from '@/config/pagination'
import LoadMoreIndicator from '@/components/common/loading/LoadMoreIndicator.vue'

const groupPhoto = ref<GroupPhotoDTO | null>(null)
const photos = ref<EnhancedWaterfallItem[]>([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(PhotoPagination.METEOROLOGY_TIMELINE_PAGE_SIZE)

// 预览相关状态
const props = defineProps<{
    meteorologyType: string
}>()
const selectedGroupId = ref<string>(props.meteorologyType.toString())
const selectedPhotoId = ref<string | null>(null)
const groupFilmPreviewVisable = ref(false)

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

// 打开照片预览
const handleImageClick = (photo: EnhancedWaterfallItem) => {
    selectedPhotoId.value = photo.id;
    groupFilmPreviewVisable.value = true;
    // 更新URL
    updateUrlWithGroupId(selectedGroupId.value, photo.id);
}

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedPhotoId.value = null;
    groupFilmPreviewVisable.value = false;
    // 清除URL参数
    updateUrlWithGroupId(null);
}

// 气象类型名称
const typeName = computed(() => {
    return getMeteorologyTypeName(props.meteorologyType)
})

// 监听气象类型变化，重新加载数据
watch(() => props.meteorologyType, async (newVal: string) => {
    try {
        // 重置状态
        photos.value = [];
        currentPage.value = 1;
        hasMore.value = true;
        selectedGroupId.value = newVal.toString();
        
        loading.value = true;
        
        // 获取特定气象类型组图信息
        const response = await getGroupPhoto(selectedGroupId.value);
        groupPhoto.value = response;
        
        if (groupPhoto.value?.photoIds?.length) {
            // 强制等待下一页照片加载完成
            await loadNextPagePhotos();
            
            // 更新是否还有更多数据标记
            hasMore.value = photos.value.length < groupPhoto.value.photoIds.length;
        } else {
            photos.value = [];
            hasMore.value = false;
        }
    } catch (error) {
        console.error('切换气象类型加载失败:', error);
        ElMessage.error('加载失败，请尝试刷新页面');
    } finally {
        loading.value = false;
    }
}, { immediate: false });

// 加载下一页照片
const loadNextPagePhotos = async () => {
    if (loading.value && !hasMore.value || !groupPhoto.value?.photoIds) return;
    
    const loadingState = loading.value;
    if (!loadingState) loading.value = true;
    
    try {
        // 获取所有照片ID列表
        const allPhotoIds = groupPhoto.value.photoIds;
        
        // 计算当前页要加载的照片ID
        const startIndex = photos.value.length;
        const endIndex = Math.min(startIndex + pageSize.value, allPhotoIds.length);
        
        // 如果已经加载完所有照片
        if (startIndex >= allPhotoIds.length) {
            hasMore.value = false;
            return;
        }
        
        // 获取当前页的照片ID列表
        const currentPagePhotoIds = allPhotoIds.slice(startIndex, endIndex);
        
        // 批量获取照片基础信息
        const photoDataResponse: WaterfallItem[] = await getPhotosByIds(currentPagePhotoIds);

        // 处理照片数据
        const processedPhotos = photoDataResponse.map(processPhotoData);
        
        // 记录添加前的长度
        const previousLength = photos.value.length;
        
        // 更新照片列表
        photos.value = [...photos.value, ...processedPhotos];
        
        // 获取只包含新加载照片的数组
        const newPhotos = photos.value.slice(previousLength);
        
        // 批量获取缩略图 - 只为新加载的照片获取缩略图
        if (newPhotos.length > 0) {
            try {
                await get100KPhotos(newPhotos);
            } catch (error) {
                console.error('加载缩略图失败:', error);
            }
        }
        
        // 更新分页信息
        hasMore.value = endIndex < allPhotoIds.length;
        if (hasMore.value) {
            currentPage.value++;
        }
    } catch (error) {
        console.error('加载照片失败:', error);
        ElMessage.error('照片加载失败，请稍后重试');
    } finally {
        if (!loadingState) loading.value = false;
    }
};

// 滚动事件处理
const handleScroll = debounce(() => {
    const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
    const scrollBottom = scrollHeight - (scrollTop + clientHeight);

    // 当距离底部小于配置的触发距离且还有更多数据时，加载更多
    if (scrollBottom < InfiniteScrollConfig.TRIGGER_DISTANCE && hasMore.value && !loading.value) {
        loadNextPagePhotos();
    }
}, InfiniteScrollConfig.THROTTLE_DELAY);

// 获取路由实例
const route = useRoute();
const router = useRouter();

// 组件挂载时加载数据
onMounted(async () => {
    try {
        // 重置状态
        photos.value = [];
        currentPage.value = 1;
        hasMore.value = true;
        
        loading.value = true;
        
        // 获取特定气象类型组图信息
        const response = await getGroupPhoto(selectedGroupId.value);
        groupPhoto.value = response;
        
        if (groupPhoto.value?.photoIds?.length) {
            // 强制等待下一页照片加载完成
            await loadNextPagePhotos();
            
            // 更新是否还有更多数据标记
            hasMore.value = photos.value.length < groupPhoto.value.photoIds.length;
        } else {
            photos.value = [];
            hasMore.value = false;
        }
    } catch (error) {
        console.error('初始加载失败:', error);
        ElMessage.error('加载失败，请刷新页面重试');
    } finally {
        loading.value = false;
    }
    
    // 检查URL中是否有组图ID参数
    if (route.query.groupId) {
        const groupIdFromUrl = route.query.groupId as string;
        const photoIdFromUrl = route.query.groupPhotoId as string || undefined;
        
        // 设置当前预览的组图ID和照片ID，并打开预览
        if (groupIdFromUrl === selectedGroupId.value) {
            selectedPhotoId.value = photoIdFromUrl || null;
            groupFilmPreviewVisable.value = true;
        }
    }
    
    // 添加滚动监听
    window.addEventListener('scroll', handleScroll);
});

// 组件卸载时清理事件监听
onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
.sunglow-timeline-container {
    width: 100%;
    margin: 0 auto;
    min-height: 500px;
    background-color: var(--qd-color-bg-dark);
}

.timeline-content {
    position: relative;
    margin: 0 5px;
}
</style>