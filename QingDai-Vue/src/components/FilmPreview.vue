<template>
    <div class="film-preview" v-if="visible" @click="handleBackgroundClick" @touchstart="handleTouchStart"
        @touchmove="handleTouchMove" @touchend="handleTouchEnd">
        <!-- 水平胶片条 -->
        <div class="film-strip" ref="filmStripRef">
            <!-- 左侧卡片(上一张) -->
            <div class="film-container side-film left" v-if="prevPhotoId" @click.stop="navigateToPrev">
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'prev-top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <el-image :src="prevThumbnailUrl" fit="contain" class="preview-image"></el-image>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'prev-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container">

                </div>
            </div>

            <!-- 中间卡片(当前) -->
            <div class="film-container current-film" @click.stop>
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <el-image :src="thumbnailUrl" fit="contain" class="preview-image" v-loading="isLoading"
                        element-loading-text="正在加载图片..." element-loading-background="rgba(0, 0, 0, 0.8)"
                        @click="handleImageClick">
                    </el-image>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container">
                    <div class="info-content">
                        <div class="title-row">
                            <div class="tag-container">
                                <el-tag v-if="previewData.start === 1" :type="'warning'" size="small">精选</el-tag>
                                <el-tag v-if="previewData.start === 0" :type="'success'" size="small">普通</el-tag>
                                <el-tag v-if="previewData.start === -1" :type="'info'" size="small">隐藏</el-tag>
                                <el-tag v-if="previewData.start === 2" :type="'primary'" size="small">气象</el-tag>
                            </div>
                            <p class="title">{{ previewData.title || '无标题' }}</p>

                        </div>
                        <el-tooltip effect="dark" :content="previewData.introduce || '暂无介绍'" placement="top"
                            :show-after="500">
                            <p class="description">{{ previewData.introduce || '暂无介绍' }}</p>
                        </el-tooltip>
                        <div class="metadata">
                            <span class="metadata-item">{{ previewData.camera || '未知' }}</span>
                            <span class="metadata-item">{{ previewData.lens || '未知' }}</span>
                            <span class="metadata-item">{{ previewData.time || '未知' }}</span>
                            <span class="metadata-item">
                                <span v-if="previewData.aperture">光圈：{{ previewData.aperture }}</span>
                                <span v-if="previewData.shutter"> 快门：{{ previewData.shutter }}</span>
                                <span v-if="previewData.iso"> ISO：{{ previewData.iso }}</span>
                                <span v-if="!previewData.aperture && !previewData.shutter && !previewData.iso">未知</span>
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 右侧卡片(下一张) -->
            <div class="film-container side-film right" v-if="nextPhotoId" @click.stop="navigateToNext">
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'next-top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <el-image :src="nextThumbnailUrl" fit="contain" class="preview-image"></el-image>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'next-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container">

                </div>
            </div>

            <!-- 极左卡片 -->
            <div class="film-container side-film extreme-left" v-if="extremePrevPhotoId"
                @click.stop="navigateToExtremePrev">
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'extreme-prev-top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <el-image :src="extremePrevThumbnailUrl" fit="contain" class="preview-image"></el-image>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'extreme-prev-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container">

                </div>
            </div>

            <!-- 极右卡片 -->
            <div class="film-container side-film extreme-right" v-if="extremeNextPhotoId"
                @click.stop="navigateToExtremeNext">
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'extreme-next-top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <el-image :src="extremeNextThumbnailUrl" fit="contain" class="preview-image"></el-image>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'extreme-next-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container">

                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted, computed, onMounted } from 'vue'
import { ElImage, ElTag, ElTooltip } from 'element-plus'
import { get1000KPhoto, getPhotoDetailInfo, type EnhancedWaterfallItem } from '@/utils/photo'
import gsap from 'gsap'

const props = defineProps<{
    photoId: string,
    modelValue: boolean,
    photoIds: string[] // 所有照片ID的数组
}>()

const emit = defineEmits(['close', 'image-click', 'update:modelValue', 'navigate'])

const visible = ref(props.modelValue)
const thumbnailUrl = ref('')
const prevThumbnailUrl = ref('')
const nextThumbnailUrl = ref('')
const previewData = ref<EnhancedWaterfallItem>({
    id: "",
    fileName: "",
    author: "",
    width: 0,
    height: 0,
    aperture: "",
    iso: "",
    shutter: "",
    camera: "",
    lens: "",
    time: "",
    title: "",
    introduce: "",
    start: 0,
})

// 动画相关状态
const filmStripRef = ref<HTMLElement | null>(null)
const isAnimating = ref(false)

// 获取动画移动距离
const getAnimationDistance = () => {
    // 检测是否为移动设备
    return window.innerWidth <= 768 ? '41vh' : '91vh'
}

// 触摸相关状态
const touchStartX = ref(0)
const touchEndX = ref(0)
const minSwipeDistance = 100 // 最小滑动距离，单位像素
const isDragging = ref(false)

// 当前照片在数组中的索引
const currentIndex = computed(() => {
    return props.photoIds.findIndex(id => id === props.photoId)
})

// 上一张照片的ID
const prevPhotoId = computed(() => {
    if (currentIndex.value > 0) {
        return props.photoIds[currentIndex.value - 1]
    }
    return null
})

// 下一张照片的ID
const nextPhotoId = computed(() => {
    if (currentIndex.value < props.photoIds.length - 1) {
        return props.photoIds[currentIndex.value + 1]
    }
    return null
})

// 极左照片的ID (prevPhotoId的前一张)
const extremePrevPhotoId = computed(() => {
    if (currentIndex.value > 1) {
        return props.photoIds[currentIndex.value - 2]
    }
    return null
})

// 极右照片的ID (nextPhotoId的后一张)
const extremeNextPhotoId = computed(() => {
    if (currentIndex.value < props.photoIds.length - 2) {
        return props.photoIds[currentIndex.value + 2]
    }
    return null
})

// 存储极左和极右照片的URL
const extremePrevThumbnailUrl = ref('')
const extremeNextThumbnailUrl = ref('')

const isLoading = ref(false)

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
    visible.value = newVal
    if (newVal) {
        handleOpen()
    }
})

// 监听 visible 变化
watch(visible, (newVal) => {
    emit('update:modelValue', newVal)
    if (!newVal) {
        handleClose()
    } else {
        // 锁定滚动
        document.body.style.overflow = 'hidden'
    }
})

// 监听 photoId 变化
watch(() => props.photoId, (newVal, oldVal) => {
    if (newVal !== oldVal && visible.value) {
        handleOpen()
    }
})

// 打开对话框时获取数据
const handleOpen = async () => {
    isLoading.value = true

    // 锁定滚动
    document.body.style.overflow = 'hidden'

    try {
        // 加载主图片
        const imageResult = await get1000KPhoto(props.photoId, (loading) => isLoading.value = loading)
        if (imageResult) {
            thumbnailUrl.value = imageResult.url
        }

        // 获取照片详细信息
        const photoInfo = await getPhotoDetailInfo(props.photoId)
        if (photoInfo) {
            previewData.value = photoInfo
        }

        // 加载前一张图片（如果有）
        if (prevPhotoId.value) {
            const prevImageResult = await get1000KPhoto(prevPhotoId.value)
            if (prevImageResult) {
                prevThumbnailUrl.value = prevImageResult.url
            }
        } else {
            prevThumbnailUrl.value = ''
        }

        // 加载后一张图片（如果有）
        if (nextPhotoId.value) {
            const nextImageResult = await get1000KPhoto(nextPhotoId.value)
            if (nextImageResult) {
                nextThumbnailUrl.value = nextImageResult.url
            }
        } else {
            nextThumbnailUrl.value = ''
        }

        // 加载极左图片（如果有）
        if (extremePrevPhotoId.value) {
            const extremePrevImageResult = await get1000KPhoto(extremePrevPhotoId.value)
            if (extremePrevImageResult) {
                extremePrevThumbnailUrl.value = extremePrevImageResult.url
            }
        } else {
            extremePrevThumbnailUrl.value = ''
        }

        // 加载极右图片（如果有）
        if (extremeNextPhotoId.value) {
            const extremeNextImageResult = await get1000KPhoto(extremeNextPhotoId.value)
            if (extremeNextImageResult) {
                extremeNextThumbnailUrl.value = extremeNextImageResult.url
            }
        } else {
            extremeNextThumbnailUrl.value = ''
        }
    } catch (error) {
        console.error('数据加载失败:', error)
    } finally {
        isLoading.value = false
    }
}

const handleClose = () => {
    // 清理主图片资源
    if (thumbnailUrl.value) {
        URL.revokeObjectURL(thumbnailUrl.value)
        thumbnailUrl.value = ''
    }

    // 清理侧边图片资源
    if (prevThumbnailUrl.value) {
        URL.revokeObjectURL(prevThumbnailUrl.value)
        prevThumbnailUrl.value = ''
    }

    if (nextThumbnailUrl.value) {
        URL.revokeObjectURL(nextThumbnailUrl.value)
        nextThumbnailUrl.value = ''
    }

    // 解除滚动锁定
    document.body.style.overflow = ''

    visible.value = false
    previewData.value = {
        id: "",
        fileName: "",
        author: "",
        width: 0,
        height: 0,
        aperture: "",
        iso: "",
        shutter: "",
        camera: "",
        lens: "",
        time: "",
        title: "",
        introduce: "",
        start: 0,
    }
    emit('close')
}

// 处理触摸开始
const handleTouchStart = (event: TouchEvent) => {
    touchStartX.value = event.touches[0].clientX
    touchEndX.value = event.touches[0].clientX
}

// 处理触摸移动
const handleTouchMove = (event: TouchEvent) => {
    if (isAnimating.value) return
    touchEndX.value = event.touches[0].clientX
}

// 处理触摸结束
const handleTouchEnd = () => {
    if (isAnimating.value) return

    const swipeDistance = touchEndX.value - touchStartX.value

    // 判断滑动方向和距离
    if (Math.abs(swipeDistance) > minSwipeDistance) {
        if (swipeDistance > 0) {
            // 向右滑动，显示上一张
            navigateToPrev()
        } else {
            // 向左滑动，显示下一张
            navigateToNext()
        }
    }

    // 重置触摸状态
    touchStartX.value = 0
    touchEndX.value = 0
}

// 处理背景点击
const handleBackgroundClick = (event: MouseEvent) => {
    // 如果是拖拽结束，不要关闭预览
    if (isDragging.value) {
        isDragging.value = false
        return
    }

    // 如果点击的是背景区域（不是film-container或其子元素），则关闭预览
    if (event.target === event.currentTarget) {
        handleClose()
    }
}

// 处理图片点击
const handleImageClick = () => {
    emit('image-click', props.photoId)
}

// 动画切换到下一张照片
const animateToNext = () => {
    if (!filmStripRef.value || isAnimating.value) return

    isAnimating.value = true

    gsap.to(filmStripRef.value, {
        x: `-${getAnimationDistance()}`, // 向左移动一个卡片的宽度
        duration: 0.5,
        ease: 'power2.out',
        onComplete: () => {
            // 动画完成后切换照片
            if (nextPhotoId.value) {
                emit('navigate', nextPhotoId.value)
                // 重置位置
                gsap.set(filmStripRef.value, { x: 0 })
            }
            isAnimating.value = false
        }
    })
}

// 动画切换到上一张照片
const animateToPrev = () => {
    if (!filmStripRef.value || isAnimating.value) return

    isAnimating.value = true

    gsap.to(filmStripRef.value, {
        x: getAnimationDistance(), // 向右移动一个卡片的宽度
        duration: 0.5,
        ease: 'power2.out',
        onComplete: () => {
            // 动画完成后切换照片
            if (prevPhotoId.value) {
                emit('navigate', prevPhotoId.value)
                // 重置位置
                gsap.set(filmStripRef.value, { x: 0 })
            }
            isAnimating.value = false
        }
    })
}

// 导航到上一张
const navigateToPrev = () => {
    if (prevPhotoId.value && !isAnimating.value) {
        animateToPrev()
    }
}

// 导航到下一张
const navigateToNext = () => {
    if (nextPhotoId.value && !isAnimating.value) {
        animateToNext()
    }
}

// 导航到极左照片
const navigateToExtremePrev = () => {
    if (extremePrevPhotoId.value && !isAnimating.value) {
        emit('navigate', extremePrevPhotoId.value)
    }
}

// 导航到极右照片
const navigateToExtremeNext = () => {
    if (extremeNextPhotoId.value && !isAnimating.value) {
        emit('navigate', extremeNextPhotoId.value)
    }
}

// 组件卸载时确保清理资源
onUnmounted(() => {
    if (thumbnailUrl.value) {
        URL.revokeObjectURL(thumbnailUrl.value)
    }
    if (prevThumbnailUrl.value) {
        URL.revokeObjectURL(prevThumbnailUrl.value)
    }
    if (nextThumbnailUrl.value) {
        URL.revokeObjectURL(nextThumbnailUrl.value)
    }
    if (extremePrevThumbnailUrl.value) {
        URL.revokeObjectURL(extremePrevThumbnailUrl.value)
    }
    if (extremeNextThumbnailUrl.value) {
        URL.revokeObjectURL(extremeNextThumbnailUrl.value)
    }
})
</script>

<style scoped>
.film-preview {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.9);
    z-index: 2000;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    overflow: hidden;
}

.film-strip {
    /* display: flex; */
    width: 100%;
    height: 90vh;
    position: relative;
    overflow: visible;
}

.film-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    background: #000;
    width: 90vh;
    height: 90vh;
    border-radius: 8px;
    position: absolute;
    overflow: hidden;
    cursor: default;
}

.side-film {
    opacity: 0.6;
    filter: brightness(50%);
    transition: all 0.3s ease;
    cursor: pointer;
}

.side-film:hover {
    opacity: 0.8;
    filter: brightness(70%);
}

.current-film {
    z-index: 10;
    left: 50%;
    transform: translateX(-50%);
}

.side-film.left {
    right: calc(50% + 46vh);
}

.side-film.right {
    left: calc(50% + 46vh);
}

.side-film.extreme-left {
    right: calc(50% + 137vh);
}

.side-film.extreme-right {
    left: calc(50% + 137vh);
}

.film-holes {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 0 12px;
    margin: 10px 0;
    flex-shrink: 0;
    min-height: 20px;
}

.hole {
    width: 16px;
    height: 16px;
    background: #333;
    border-radius: 50%;
    border: 2px solid #666;
    margin: 0 4px;
    flex-shrink: 0;
}

.photo-container {
    flex: 1;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #000;
    overflow: hidden;
}

.preview-image {
    height: 100%;
    /* width: auto; */
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: transform 0.3s;
    cursor: zoom-in;
}

.current-film .preview-image:hover {
    transform: scale(1.02);
}

.info-container {
    height: 10vh;
    width: 100%;
    background: #000;
    color: #fff;
    padding: 0;
    flex-shrink: 0;
}

.info-content {
    max-width: 800px;
    margin: 0 auto;
}

.title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 0;
}

.title {
    font-size: 18px;
    margin: 0;
    color: #fff;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
}

.tag-container {
    margin-right: 3px;
    flex-shrink: 0;
}

.description {
    font-size: 14px;
    margin: 0;
    color: #ccc;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.metadata {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    font-size: 14px;
    color: #888;
}

.metadata-item {
    white-space: nowrap;
}

@media (max-width: 768px) {
    .film-strip {
        height: 80vh;
    }

    .film-container {
        width: 40vh;
        height: 80vh;
    }

    .metadata {
        gap: 8px;
    }

    .hole {
        width: 10px;
        height: 10px;
        border-width: 1px;
    }

    .navigation-buttons {
        display: none;
    }

    .nav-button {
        display: none;
    }

    .side-film.left {
        right: calc(50% + 21vh);
    }

    .side-film.right {
        left: calc(50% + 21vh);
    }

    .side-film.extreme-left {
        right: calc(50% + 61vh);
    }

    .side-film.extreme-right {
        left: calc(50% + 61vh);
    }
}
</style>