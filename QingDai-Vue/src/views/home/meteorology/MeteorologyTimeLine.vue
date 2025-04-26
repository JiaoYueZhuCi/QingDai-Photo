<template>
    <div class="sunglow-timeline-container">
        <div class="timeline-content">
            <!-- 统计信息展示 -->
            <photo-stats :type-name="typeName" :photo-count="photos.length" />

            <!-- 排序按钮 -->
            <sort-control :is-ascending="isAscending" :opacity="opacity" @toggle="toggleSortOrder" />
            
            <!-- 照片列表 -->
            <timeline-photos :photos="sortedPhotos" @photo-click="handleImageClick" />
        </div>

        <!-- 组图预览对话框 -->
        <group-film-preview 
            v-model="groupFilmPreviewVisable"
            :group-id="selectedGroupId"
            :initial-photo-id="selectedPhotoId || undefined" 
            @close="closeGroupPhotoPreview" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch} from 'vue'
import { getGroupPhoto } from '@/api/groupPhoto'
import { getPhotosByIds } from '@/api/photo'
import type { GroupPhotoDTO } from '@/types/groupPhoto'
import { ElMessage } from 'element-plus'
import GroupFilmPreview from '@/components/group-photos/GroupFilmPreview.vue'
import { get100KPhotos, processPhotoData, type EnhancedWaterfallItem } from '@/utils/photo'
import PhotoStats from '@/components/meteorology/PhotoStats.vue'
import SortControl from '@/components/meteorology/SortControl.vue'
import TimelinePhotos from '@/components/meteorology/TimelinePhotos.vue'
import { getMeteorologyTypeName, sortPhotosByTime } from '@/utils/meteorologyUtils'

const groupPhoto = ref<GroupPhotoDTO | null>(null)
const photos = ref<EnhancedWaterfallItem[]>([])

// 预览相关状态
const props = defineProps<{
    meteorologyType: string
}>()
const selectedGroupId = ref<string>(props.meteorologyType.toString())
const selectedPhotoId = ref<string | undefined>(undefined)
const groupFilmPreviewVisable = ref(false)

// 打开照片预览
const handleImageClick = (photo: EnhancedWaterfallItem) => {
    selectedPhotoId.value = photo.id
    groupFilmPreviewVisable.value = true
}

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedPhotoId.value = undefined
    groupFilmPreviewVisable.value = false
}

// 滚动透明度控制
const opacity = ref(1)
const scrollThreshold = 800

// 排序控制
const isAscending = ref(true) // 默认正序（时间从早到晚）

// 切换排序顺序
const toggleSortOrder = () => {
    isAscending.value = !isAscending.value
}

// 气象类型名称
const typeName = computed(() => {
    return getMeteorologyTypeName(props.meteorologyType)
})

// 排序后的照片列表
const sortedPhotos = computed(() => {
    return sortPhotosByTime(photos.value, isAscending.value)
})

// 处理滚动事件，调整按钮透明度
const handleScroll = () => {
    const scrollPosition = window.scrollY

    if (scrollPosition > scrollThreshold) {
        // 超过阈值，降低透明度
        opacity.value = 0.5
    } else {
        // 计算渐变透明度 1.0 -> 0.6
        opacity.value = 1 - (scrollPosition / scrollThreshold) * 0.4
    }
}

// 加载组图信息和照片
const loadGroupPhotos = async () => {
    try {
        // 获取特定气象类型组图信息
        const response = await getGroupPhoto(selectedGroupId.value);
        groupPhoto.value = response;

        if (groupPhoto.value?.photoIds?.length) {
            // 获取照片ID列表
            const photoIds = groupPhoto.value.photoIds;
            
            // 批量获取照片基础信息
            const photoDataResponse = await getPhotosByIds(photoIds);

            // 处理照片数据
            const processedPhotos = photoDataResponse.map(processPhotoData);
            photos.value = processedPhotos;
            
            // 批量获取缩略图
            await get100KPhotos(photos.value);
        } else {
            photos.value = [];
        }
    } catch (error) {
        console.error('加载照片失败:', error);
        ElMessage.error('照片加载失败，请稍后重试');
    }
};

// 监听气象类型变化，重新加载数据
watch(() => props.meteorologyType, (newVal: string) => {
    selectedGroupId.value = newVal.toString();
    loadGroupPhotos();
});

// 组件挂载时加载数据
onMounted(() => {
    loadGroupPhotos();
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