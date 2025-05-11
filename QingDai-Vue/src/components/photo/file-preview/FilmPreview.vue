<template>
    <div class="film-preview" v-if="visible" @click="handleBackgroundClick" @touchstart="handleTouchStart"
        @touchmove="handleTouchMove" @touchend="handleTouchEnd">
        <!-- 水平胶片条 -->
        <div class="film-strip" ref="filmStripRef">
            <!-- 左侧卡片(上一张) -->
            <div class="film-container side-film left" v-if="prevPhotoId" @click.stop="navigateToPrev" ref="leftFilmRef">
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'prev-top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <div class="preview-wrapper">
                        <img v-for="(url, id) in preloadedImages" :key="'prev-' + id" :src="url" class="preview-image"
                            v-show="id === prevPhotoId" alt="上一张照片">
                        <!-- 预加载图片的占位，但不显示 -->
                        <img v-if="!prevPhotoId || !preloadedImages[prevPhotoId]" src=""
                            class="preview-image placeholder-image" alt="预加载占位">
                    </div>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'prev-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container" v-loading="isInfoLoading" element-loading-text="正在加载信息...">

                </div>
            </div>

            <!-- 中间卡片(当前) -->
            <div class="film-container current-film" @click.stop ref="currentFilmRef">
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <div class="preview-wrapper" v-loading="isLoading" element-loading-text="正在加载图片..."
                        element-loading-background="rgba(0, 0, 0, 0.8)">
                        <img v-for="(url, id) in preloadedImages" :key="'current-' + id" :src="url"
                            class="preview-image" v-show="id === props.photoId" @click="handleImageClick" alt="当前照片">
                        <!-- 预加载图片的占位，但不显示 -->
                        <img v-if="!props.photoId || !preloadedImages[props.photoId]" src=""
                            class="preview-image placeholder-image" alt="预加载占位">
                    </div>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container" v-loading="isInfoLoading" element-loading-text="正在加载信息...">
                    <div class="info-content">
                        <div class="title-row">
                            <div class="tag-container">
                                <el-tag v-if="previewData.startRating === 1" :type="'warning'" size="small">精选</el-tag>
                                <el-tag v-if="previewData.startRating === 0" :type="'success'" size="small">普通</el-tag>
                                <el-tag v-if="previewData.startRating === -1" :type="'info'" size="small">隐藏</el-tag>
                                <el-tag v-if="previewData.startRating === 2" :type="'primary'" size="small">气象</el-tag>
                            </div>
                            <p class="title">{{ previewData.title || '无标题' }}</p>

                        </div>
                        <el-tooltip effect="dark" :content="previewData.introduce || '暂无介绍'" placement="top"
                            :show-after="500">
                            <p class="description">{{ previewData.introduce || '暂无介绍' }}</p>
                        </el-tooltip>
                        <div class="metadata">
                            <span class="metadata-item">{{ previewData.shootTime || '未知时间' }}</span>
                            <span class="metadata-item">{{ previewData.camera || '未知相机' }}</span>
                            <span class="metadata-item">{{ previewData.lens || '未知镜头' }}</span>
                            <span class="metadata-item">{{ previewData.focalLength || '未知' }}mm</span>
                            <span class="metadata-item">F{{ previewData.aperture || '未知' }}</span>
                            <span class="metadata-item">{{ previewData.shutter || '快门未知' }}</span>
                            <span class="metadata-item">ISO{{ previewData.iso || '未知' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 右侧卡片(下一张) -->
            <div class="film-container side-film right" v-if="nextPhotoId" @click.stop="navigateToNext" ref="rightFilmRef">
                <!-- 上胶片孔 -->
                <div class="film-holes top">
                    <div v-for="n in 18" :key="'next-top-' + n" class="hole"></div>
                </div>

                <!-- 照片展示区域 -->
                <div class="photo-container">
                    <div class="preview-wrapper">
                        <img v-for="(url, id) in preloadedImages" :key="'next-' + id" :src="url" class="preview-image"
                            v-show="id === nextPhotoId" alt="下一张照片">
                        <!-- 预加载图片的占位，但不显示 -->
                        <img v-if="!nextPhotoId || !preloadedImages[nextPhotoId]" src=""
                            class="preview-image placeholder-image" alt="预加载占位">
                    </div>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'next-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container" v-loading="isInfoLoading" element-loading-text="正在加载信息...">

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
                    <div class="preview-wrapper">
                        <img v-for="(url, id) in preloadedImages" :key="'extremePrev-' + id" :src="url"
                            class="preview-image" v-show="id === extremePrevPhotoId" alt="最左侧照片">
                        <!-- 预加载图片的占位，但不显示 -->
                        <img v-if="!extremePrevPhotoId || !preloadedImages[extremePrevPhotoId]" src=""
                            class="preview-image placeholder-image" alt="预加载占位">
                    </div>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'extreme-prev-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container" v-loading="isInfoLoading" element-loading-text="正在加载信息...">

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
                    <div class="preview-wrapper">
                        <img v-for="(url, id) in preloadedImages" :key="'extremeNext-' + id" :src="url"
                            class="preview-image" v-show="id === extremeNextPhotoId" alt="最右侧照片">
                        <!-- 预加载图片的占位，但不显示 -->
                        <img v-if="!extremeNextPhotoId || !preloadedImages[extremeNextPhotoId]" src=""
                            class="preview-image placeholder-image" alt="预加载占位">
                    </div>
                </div>

                <!-- 下胶片孔 -->
                <div class="film-holes bottom">
                    <div v-for="n in 18" :key="'extreme-next-bottom-' + n" class="hole"></div>
                </div>

                <!-- 照片信息区域 -->
                <div class="info-container" v-loading="isInfoLoading" element-loading-text="正在加载信息...">

                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted, computed, onMounted } from 'vue'
import { ElTag, ElTooltip, ElMessage } from 'element-plus'
import { get1000KPhoto, getPhotoDetailInfo, type EnhancedWaterfallItem } from '@/utils/photo'
import gsap from 'gsap'

const props = defineProps<{
    photoId: string,
    modelValue: boolean,
    photoIds: string[] // 所有照片ID的数组
}>()

const emit = defineEmits(['close', 'image-click', 'update:modelValue', 'navigate'])

const visible = ref(props.modelValue)

// 对象映射存储预加载的图片，key为photoId
const preloadedImages = ref<Record<string, string>>({})
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
    focalLength: "",
    shootTime: "",
    startRating: 0,
    title: "",
    introduce: "",
    start: 0,
})

// 动画相关状态
const filmStripRef = ref<HTMLElement | null>(null)
const currentFilmRef = ref<HTMLElement | null>(null)
const leftFilmRef = ref<HTMLElement | null>(null)
const rightFilmRef = ref<HTMLElement | null>(null)
const isAnimating = ref(false)

// 触摸相关状态
const touchStartX = ref(0)
const touchEndX = ref(0)
const minSwipeDistance = 10 // 最小滑动距离，单位像素
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

const isLoading = ref(false)
const isInfoLoading = ref(false)  // 新增：专门用于控制信息加载状态

// 预加载图片，并添加到预加载对象中
const preloadImage = async (photoId: string | null): Promise<void> => {
    if (!photoId) return

    // 如果已经预加载过，就跳过
    if (preloadedImages.value[photoId]) return

    try {
        const imageResult = await get1000KPhoto(photoId)
        if (imageResult) {
            // Vue的响应式系统不会自动检测到对象属性的添加
            // 使用这种方式确保响应式更新
            preloadedImages.value = { ...preloadedImages.value, [photoId]: imageResult.url }
        }
    } catch (error) {
        console.error(`预加载图片 ${photoId} 失败:`, error)
    }
}

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
    visible.value = newVal
    // 仅更新可见状态，不触发加载
    if (newVal) {
        // 锁定滚动
        document.body.style.overflow = 'hidden'
        // 预览变为可见时，确保至少有预览数据
        ensureDataLoaded()
    }
})

// 监听 visible 变化
watch(visible, (newVal) => {
    emit('update:modelValue', newVal)
    if (!newVal) {
        emit('close')
    }
})

watch(() => props.photoId, (newVal) => {
    ensureDataLoaded()
})

// 确保数据加载 - 统一的加载入口
const ensureDataLoaded = async () => {
    if (isLoading.value) {
        return
    }
    
    if (!props.photoId) {
        ElMessage({
            type: 'error',
            message: '照片ID无效，无法加载预览',
            duration: 3000
        })
        handleClose()
        return
    }
    
    // 如果当前展示的照片ID与请求的相同，且已有数据，则无需重新加载
    if (previewData.value.id === props.photoId && previewData.value.title) {
        applyAnimations() // 只应用动画效果
        return
    }
    
    // 执行实际的数据加载
    await handleOpen()
}

// 应用动画效果
const applyAnimations = () => {
    // 卡片打开的动画效果
    if (currentFilmRef.value) {
        gsap.fromTo(currentFilmRef.value, 
            {
                scale: 0.8,
                opacity: 0,
                y: 50
            },
            {
                scale: 1,
                opacity: 1,
                y: 0,
                duration: 0.5,
                ease: "back.out(1.7)"
            }
        )
    }

    // 左右两侧卡片的滑入动画
    if (leftFilmRef.value) {
        gsap.fromTo(leftFilmRef.value,
            {
                x: -100,
                opacity: 0
            },
            {
                x: 0,
                opacity: 0.6,
                duration: 0.5,
                ease: "power2.out",
                delay: 0.2
            }
        )
    }

    if (rightFilmRef.value) {
        gsap.fromTo(rightFilmRef.value,
            {
                x: 100,
                opacity: 0
            },
            {
                x: 0,
                opacity: 0.6,
                duration: 0.5,
                ease: "power2.out",
                delay: 0.2
            }
        )
    }
}

// 打开对话框时获取数据
const handleOpen = async () => {
    if (!props.photoId) {
        ElMessage({
            type: 'error',
            message: '照片ID无效，无法加载数据',
            duration: 3000
        })
        handleClose()
        return
    }
    
    isLoading.value = true
    isInfoLoading.value = true

    // 锁定滚动
    document.body.style.overflow = 'hidden'

    try {
        // 只获取一次照片详细信息
        const photoInfo = await getPhotoDetailInfo(props.photoId)
        if (photoInfo) {
            previewData.value = photoInfo
        }
        
        // 先加载当前照片
        await preloadImage(props.photoId)

        // 应用动画效果
        applyAnimations()

        // 后台异步加载前后四张照片
        setTimeout(async () => {
            // 前一张和后一张的优先级高
            const highPriorityIds = [
                prevPhotoId.value,      // 上一张
                nextPhotoId.value,      // 下一张
            ].filter(Boolean) as string[]
            
            // 异步加载高优先级照片
            for (const id of highPriorityIds) {
                await preloadImage(id)
            }
            
            // 前前一张和后后一张的优先级低
            const lowPriorityIds = [
                extremePrevPhotoId.value,  // 前前一张
                extremeNextPhotoId.value,  // 后后一张
            ].filter(Boolean) as string[]
            
            // 异步加载低优先级照片
            for (const id of lowPriorityIds) {
                await preloadImage(id)
            }
        }, 50)
    } catch (error) {
        console.error('数据加载失败:', error)
    } finally {
        isLoading.value = false
        isInfoLoading.value = false
    }
}

const handleClose = () => {
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
        focalLength: "",
        shootTime: "",
        title: "",
        introduce: "",
        startRating: 0,
    }
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
    if (!props.photoId) {
        ElMessage({
            type: 'error',
            message: '照片ID无效，无法打开查看器',
            duration: 3000
        })
        return
    }
    emit('image-click', props.photoId)
}

// 动画切换到下一张照片
const animateToNext = () => {
    if (!filmStripRef.value || isAnimating.value) return

    isAnimating.value = true
    isInfoLoading.value = true  // 添加信息加载状态

    gsap.to(filmStripRef.value, {
        x: `-${getAnimationDistance()}`,
        duration: 0.5,
        ease: 'power2.out',
        onComplete: () => {
            // 动画完成后切换照片
            if (nextPhotoId.value) {
                // 通知父组件更新当前照片ID
                emit('navigate', nextPhotoId.value)
                
                // 重置位置
                gsap.set(filmStripRef.value, { x: 0 })
                isAnimating.value = false
                isInfoLoading.value = false  // 重置信息加载状态
            } else {
                gsap.set(filmStripRef.value, { x: 0 })
                isAnimating.value = false
                isInfoLoading.value = false  // 重置信息加载状态
            }
        }
    })
}

// 动画切换到上一张照片
const animateToPrev = () => {
    if (!filmStripRef.value || isAnimating.value) return

    isAnimating.value = true
    isInfoLoading.value = true  // 添加信息加载状态

    gsap.to(filmStripRef.value, {
        x: getAnimationDistance(),
        duration: 0.5,
        ease: 'power2.out',
        onComplete: () => {
            // 动画完成后切换照片
            if (prevPhotoId.value) {
                // 通知父组件更新当前照片ID
                emit('navigate', prevPhotoId.value)
                
                // 重置位置
                gsap.set(filmStripRef.value, { x: 0 })
                isAnimating.value = false
                isInfoLoading.value = false  // 重置信息加载状态
            } else {
                gsap.set(filmStripRef.value, { x: 0 })
                isAnimating.value = false
                isInfoLoading.value = false  // 重置信息加载状态
            }
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

// 导航到极左
const navigateToExtremePrev = () => {
    if (extremePrevPhotoId.value && !isAnimating.value) {
        ElMessage({
            type: 'warning',
            message: '请等当前照片加载完成',
            duration: 2000
        })
    }
}

// 导航到极右
const navigateToExtremeNext = () => {
    if (extremeNextPhotoId.value && !isAnimating.value) {
        ElMessage({
            type: 'warning',
            message: '请等当前照片加载完成',
            duration: 2000
        })
    }
}

// 获取动画移动距离
const getAnimationDistance = () => {
    // 检测是否为移动设备
    return window.innerWidth <= 768 ? '81vw' : '71vw'
}

// 组件卸载时确保清理资源
onUnmounted(() => {
    // 清理所有图片资源
    Object.values(preloadedImages.value).forEach(url => {
        if (url.startsWith('blob:')) {
            URL.revokeObjectURL(url)
        }
    })

    // 清空状态
    preloadedImages.value = {}
})

// 组件挂载后初始化
onMounted(() => {
    if (visible.value && props.photoId) {
        ensureDataLoaded()
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
    width: 70vw;
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
    right: calc(50% + 36vw);
}

.side-film.right {
    left: calc(50% + 36vw);
}

.side-film.extreme-left {
    right: calc(50% + 107vw);
}

.side-film.extreme-right {
    left: calc(50% + 107vw);
}

.film-holes {
    display: flex;
    justify-content: space-between;
    width: 100%;
    margin: 7px 0;
    flex-shrink: 0;
    min-height: 20px;
}

.hole {
    width: 14px;
    height: 14px;
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

.preview-wrapper {
    width: 100%;
    height: 100%;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
}

.preview-image {
    max-height: 100%;
    max-width: 100%;
    object-fit: contain;
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

.side-film .info-container {
    opacity: 1 !important;
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
    font-size: calc(1.2vw + 0.6vh);
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
    font-size: calc(0.8vw + 0.4vh);
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
    font-size: calc(0.8vw + 0.4vh);
    color: #888;
}

.metadata-item {
    white-space: nowrap;
}

.placeholder-image {
    opacity: 0;
}

@media (max-width: 768px) {
    .film-strip {
        height: 85vh;
    }

    .film-container {
        width: 80vw;
        height: 85vh;
    }

    .metadata {
        gap: 8px;
    }

    .hole {
        width: 10px;
        height: 10px;
        border-width: 1px;
    }

    .side-film.left {
        right: calc(50% + 41vw);
    }

    .side-film.right {
        left: calc(50% + 41vw);
    }

    .side-film.extreme-left {
        right: calc(50% + 122vw);
    }

    .side-film.extreme-right {
        left: calc(50% + 122vw);
    }

    .info-container {
        height: 20vh;
    }

    .title {
        font-size: calc(2vw + 1vh);
    }

    .description {
        font-size: calc(1.5vw + 0.7vh);
    }

    .metadata {
        font-size: calc(1.4vw + 0.6vh);
    }
}
</style>