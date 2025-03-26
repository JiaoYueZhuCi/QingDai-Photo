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
                <el-affix :offset="10">
                    <el-button class="sort-btn" @click="toggleSortOrder" :style="{ opacity: opacity }">
                        {{ isAscending ? '切换为最新优先' : '切换为最早优先' }}
                    </el-button>
                </el-affix>
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
        <group-photo-preview v-if="selectedPhotoId" :group-id="selectedGroupId"
            :initial-photo-id="selectedPhotoId || undefined" :photos="sortedPhotos" @close="closeGroupPhotoPreview" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed ,watch} from 'vue'
import { getGroupPhoto } from '@/api/groupPhoto'
import { getThumbnail100KPhotos, getPhotosByIds } from '@/api/photo'
import type { GroupPhoto } from '@/types'
import type { WaterfallItem } from '@/types'
import JSZip from 'jszip'
import GroupPhotoPreview from '@/components/GroupPhotoPreview.vue'

const loading = ref(true)
const groupInfo = ref<GroupPhoto | null>(null)
const photos = ref<WaterfallItem[]>([])

// 预览相关状态
const props = defineProps<{
    meteorologyType: string
}>()
const selectedGroupId = ref<string>(props.meteorologyType.toString())
const selectedPhotoId = ref<string | undefined>(undefined)


// 打开照片预览
const handleImageClick = (photo: WaterfallItem) => {
    selectedPhotoId.value = photo.id
}

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedPhotoId.value = undefined
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
        loading.value = true

        // 获取组图信息
        const response = await getGroupPhoto(selectedGroupId.value) // 
        groupInfo.value = response as unknown as GroupPhoto
        if (groupInfo.value && groupInfo.value.photos) {
            // 获取照片ID列表
            const photoIds = groupInfo.value.photos.split(',').filter(id => id.trim().length > 0)
            if (photoIds.length > 0) {
                // 获取照片信息
                const photosData = await getPhotosByIds(photoIds.join(','))

                // 获取照片缩略图
                const thumbnailResponse = await getThumbnail100KPhotos(photoIds.join(','))

                // 处理照片数据 - 解压缩ZIP文件
                const zip = await JSZip.loadAsync(thumbnailResponse.data)

                // 获取所有图片文件
                const files = Object.values(zip.files).filter(file => !file.dir)

                // 确保已经获取到照片信息和缩略图
                if (photosData && photosData.length > 0 && files.length > 0) {
                    // 创建照片数组
                    const photoList: WaterfallItem[] = []

                    // 处理每张照片
                    for (const photo of photosData) {
                        // 根据文件名查找对应的缩略图文件
                        const file = files.find(f => f.name.includes(photo.fileName) || f.name.includes(photo.id))

                        if (file) {
                            // 获取图片blob并创建URL
                            const blob = await file.async('blob')
                            const url = URL.createObjectURL(blob)

                            // 创建照片对象并添加缩略图URL
                            photoList.push({
                                ...photo,
                                compressedSrc: url
                            })
                        } else {
                            // 如果没有找到对应的缩略图，仍然添加照片信息
                            photoList.push(photo)
                        }
                    }

                    // 确保照片顺序与photoIds一致
                    photos.value = photoIds.map(id =>
                        photoList.find(photo => photo.id === id)
                    ).filter(photo => photo !== undefined) as WaterfallItem[]
                }
            }
        }
    } catch (error) {
        console.error('加载照片失败:', error)
    } finally {
        loading.value = false
    }
}

watch(() => props.meteorologyType, (newVal: string) => {
    selectedGroupId.value = newVal.toString()
    loadGroupPhotos()
})

onMounted(() => {
    loadGroupPhotos()
    window.addEventListener('resize', handleResize)
    window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.sunglow-timeline-container {
    width: 100%;
    margin: 0 auto;
    min-height: 500px;
    background-color: rgb(12, 12, 12);
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
    border: 5px solid rgba(249, 202, 36, 0.2);
    border-radius: 50%;
    border-top-color: #f9ca24;
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

.timeline-header h2 {
    font-size: 2rem;
    margin-bottom: 10px;
    color: #333;
}

.timeline-header p {
    font-size: 1rem;
    color: #666;
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
    /* 固定宽度 */
    margin: 15px;
}

.timeline-dot {
    position: absolute;
    width: 20px;
    height: 20px;
    background-color: #f9ca24;
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
    color: #666;
    z-index: 3;
    background-color: white;
    padding: 2px 6px;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    white-space: nowrap;
    transform: translateX(-50%);
    left: 50%;
}

.timeline-card {
    padding: 5px;
    background-color: white;
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
    /* border-radius: 4px; */
    cursor: pointer;
    /* 添加鼠标指针样式，提示可点击 */
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
    background-color: #f0f0f0;
    border-radius: 4px;
}

/* 响应式布局 - 仅保留移动端垂直布局的处理 */
@media screen and (max-width: 768px) {
    .timeline-photos {
        flex-direction: column;
        align-items: center;
    }

    .timeline-item {
        width: 280px;
        /* 移动端稍微窄一点 */
        margin-bottom: 30px;
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
    padding: 10px 0;
    color: #f9ca24;
    font-size: 18px;
}

.photo-count {
    font-weight: bold;
    font-size: 22px;
    margin: 0 5px;
}

.sort-controls {
    padding-top: 6px;
    position: absolute;
    top: 5px;
    right: 150px;
}

.sort-btn {
    background-color: #f9ca24;
    color: #333;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    transition: all 0.3s ease;
}

.sort-btn:hover {
    background-color: #e6b913;
    transform: translateY(-2px);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    opacity: 1 !important;
}

.sort-btn:active {
    transform: translateY(0);
}
</style>