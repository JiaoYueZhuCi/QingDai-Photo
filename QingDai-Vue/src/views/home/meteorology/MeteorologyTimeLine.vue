<template>
    <div class="sunglow-timeline-container">
        <div class="loading" v-if="loading">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
        </div>
        <div v-else class="timeline-content">
            <!-- 统计信息展示 -->
            <div class="stats-container">
                <div class="photo-stats">
                    现已记录{{ typeName }}：<span class="photo-count">{{ photos.length }}</span> 次
                </div>
            </div>

            <!-- 排序按钮 -->
            <div class="sort-controls">
                    <el-button class="sort-btn" @click="toggleSortOrder" :style="{ opacity: opacity }">
                        {{ isAscending ? '切换为最新优先' : '切换为最早优先' }}
                    </el-button>
            </div>
            <div class="timeline-container">
                <!-- 照片列表 -->
                <div class="timeline-photos">
                    <div v-for="photo in sortedPhotos" :key="photo.id" class="timeline-item">
                        <div class="timeline-dot"></div>
                        <div class="time-label">{{ photo.time }}</div>
                        <div class="timeline-card">
                            <div class="photo-container" @click="handleImageClick(photo)">
                                <img v-if="photo.compressedSrc" :src="photo.compressedSrc" alt="照片" />
                                <div v-else class="photo-placeholder"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
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

const loading = ref(true)
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
    const typeMap: Record<number, string> = {
        1: '朝霞',
        2: '晚霞',
        3: '日出',
        4: '日落'
    }
    // 将 props.meteorologyType 转换为 number 类型以匹配 typeMap 的索引类型
    const meteorologyTypeNumber = parseInt(props.meteorologyType, 10);
    return typeMap[meteorologyTypeNumber] || '气象异常'
})

// 排序后的照片列表
const sortedPhotos = computed(() => {
    if (!photos.value.length) return []

    // 复制数组，避免修改原始数据
    const photosToSort = [...photos.value]

    // 按时间排序
    return photosToSort.sort((a, b) => {
        // 将时间字符串转为 Date 对象进行比较
        const timeA = new Date(a.time).getTime()
        const timeB = new Date(b.time).getTime()

        // 根据排序顺序返回结果
        return isAscending.value ? timeA - timeB : timeB - timeA
    })
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

// 监听窗口大小变化 - 仅用于触发CSS响应式
const handleResize = () => {
    // 不需要做任何事情，CSS会自动处理响应式
}

// 加载组图信息和照片
const loadGroupPhotos = async () => {
    try {
        loading.value = true;

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
    } finally {
        loading.value = false;
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
    window.addEventListener('resize', handleResize);
    window.addEventListener('scroll', handleScroll);
});

// 组件卸载时清理事件监听
onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
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

.loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300px;
}

.timeline-content {
    position: relative;
    margin: 0 5px;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    border: 5px solid var(--qd-color-primary);
    border-radius: 50%;
    border-top-color: var(--qd-color-text-secondary);
    animation: spin 1s ease-in-out infinite;
    margin-bottom: 15px;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.timeline-header {
    text-align: center;
    margin-bottom: 40px;
}

.timeline-container {
    position: relative;
}

/* 照片容器 - 使用flex布局 */
.timeline-photos {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    position: relative;
    z-index: 2;
}

.timeline-item {
    position: relative;
    width: 240px;
    margin: 15px;
}

.timeline-dot {
    position: absolute;
    width: 20px;
    height: 20px;
    background-color: var(--qd-color-border);
    border-radius: 50%;
    left:25px;
    top: -10px;
    transform: translateX(-50%);
    z-index: 3;
}

.time-label {
    position: absolute;
    top: -11px;
    font-size: 15px;
    color: var(--qd-color-primary);
    z-index: 3;
    background-color: var(--qd-color-bg);
    padding: 2px 6px;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    white-space: nowrap;
    transform: translateX(-50%);
    left: 50%;
}

.timeline-card {
    padding: 5px;
    background-color: var(--qd-color-bg);
    border-radius: 6px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    margin-top: 20px;
}

.timeline-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.photo-container {
    width: 100%;
    overflow: hidden;
    cursor: pointer;
}

.photo-container img {
    width: 100%;
    height: 230px;
    object-fit: contain;
    transition: transform 0.5s ease;
    aspect-ratio: 4/3;
}

.photo-container img:hover {
    transform: scale(1.05);
}

.photo-placeholder {
    width: 100%;
    height: 200px;
    background-color: var(--qd-color-bg-light);
    border-radius: 4px;
}

/* 响应式布局 - 仅保留移动端垂直布局的处理 */
@media screen and (max-width: 768px) {
    .timeline-photos {
        flex-direction: column;
        align-items: center;
    }

    .timeline-item {
        width: 320px;
    }

    .timeline-line {
        top: 0;
        height: 100%;
        width: 4px;
        left: 50%;
        margin-left: -2px;
    }

    .timeline-dot {
        left: 50%;
        top: 10px;
    }

    .time-label {
        top: 10px;
        left: calc(50% + 15px);
        transform: none;
    }
}

/* 统计信息样式 */
.stats-container {
    text-align: center;
    padding: 10px 0 0 0;
    color: var(--qd-color-text-primary) !important;
    font-size: 18px;
}

.photo-count {
    font-weight: bold;
    font-size: 22px;
    margin: 0 5px;
    color: var(--qd-color-primary) !important;
}

.sort-controls {
    text-align: center;
}

.sort-btn {
    background-color: var(--qd-color-text-regular);
    color: var(--qd-color-bg);
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    transition: all 0.3s ease;
}

.sort-btn:hover {
    background-color: var(--qd-color-primary-light-4);
    transform: translateY(-2px);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    opacity: 1 !important;
}

.sort-btn:active {
    transform: translateY(0);
}
</style>