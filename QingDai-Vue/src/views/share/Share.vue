<template>
    <div class="share-container">
        <!-- 添加装饰性元素 -->
        <div class="decorative-elements">
            <div class="floating-paper" v-for="i in 5" :key="i"></div>
            <div class="box-shadow"></div>
        </div>

        <div class="share-box" :class="{ 'box-open': isBoxOpen }">
            <div class="box-lid" @click="openBox">
                <div class="lid-content">
                    <el-icon class="open-icon"><ArrowDown /></el-icon>
                    <h2>点击打开分享</h2>
                </div>
                <div class="lid-decoration">
                    <div class="lid-stripe"></div>
                    <div class="lid-handle"></div>
                </div>
            </div>
            <div class="box-content">
                <div v-if="loading" class="loading-container">
                    <el-skeleton :rows="6" animated />
                </div>
                <div v-else-if="photos.length === 0" class="empty-container">
                    <el-empty description="分享链接已过期或不存在" />
                </div>
                <div v-else class="photo-grid">
                    <div v-for="photo in photos" :key="photo.id" class="photo-item" @click="openPhotoPreview(photo)">
                        <el-image :src="photo.compressedSrc" fit="contain" />
                    </div>
                </div>
            </div>
            <!-- 添加盒子侧面装饰 -->
            <div class="box-sides">
                <div class="box-side box-side-left"></div>
                <div class="box-side box-side-right"></div>
                <div class="box-side box-side-front"></div>
                <div class="box-side box-side-back"></div>
            </div>
        </div>

        <FilmPreview v-model="previewVisible" :photo-id="currentPhotoId" :photo-ids="photoIds" 
            @close="handlePreviewClose" 
            @image-click="handleImageClick"
            @navigate="handleNavigate" />

        <!-- 添加 PhotoViewer 组件 -->
        <PhotoViewer v-model="viewerVisible" :photo-id="currentPhotoId" :initial-index="currentIndex"
            :use-direct-render="true" @close="handleViewerClose" />
    </div>

    <!-- 添加Footer组件 -->
    <Footer />
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Close, ArrowDown } from '@element-plus/icons-vue';
import { getSharePhotos, validateShareLink } from '@/api/share';
import { getPhotosByIdsWithThumbnail, processPhotoData } from '@/utils/photo';
import type { EnhancedWaterfallItem } from '@/utils/photo';
import FilmPreview from '@/components/photo/FilmPreview.vue';
import PhotoViewer from '@/components/photo/PhotoViewer.vue';
import Footer from '@/components/common/Footer.vue';

const route = useRoute();
const shareId = route.query.id as string;

const isBoxOpen = ref(false);
const photos = ref<EnhancedWaterfallItem[]>([]);
const loading = ref(true);
const previewVisible = ref(false);
const currentPhotoId = ref('');
const viewerVisible = ref(false);
const currentIndex = ref(0);

// 计算所有照片ID
const photoIds = computed(() => {
    return photos.value.map(photo => photo.id);
});

// 打开盒子
const openBox = () => {
    if (!isBoxOpen.value) {
        isBoxOpen.value = true;
    }
};

// 打开照片预览
const openPhotoPreview = (photo: EnhancedWaterfallItem) => {
    currentPhotoId.value = photo.id;
    previewVisible.value = true;
};

// 处理预览关闭
const handlePreviewClose = () => {
    previewVisible.value = false;
    currentPhotoId.value = '';
};

// 处理图片点击事件
const handleImageClick = (photoId: string) => {
    currentPhotoId.value = photoId;
    // 查找当前索引
    const index = photos.value.findIndex(img => img.id === photoId);
    if (index !== -1) {
        currentIndex.value = index;
    }
    // 打开查看器
    viewerVisible.value = true;
};

// 处理查看器关闭
const handleViewerClose = () => {
    viewerVisible.value = false;
};

// 处理FilmPreview的导航事件
const handleNavigate = (photoId: string) => {
    currentPhotoId.value = photoId;
    // 查找当前索引
    const index = photos.value.findIndex(img => img.id === photoId);
    if (index !== -1) {
        currentIndex.value = index;
    }
};

// 获取分享的照片
const fetchSharePhotos = async () => {
    try {
        loading.value = true;
        // 先验证分享链接
        const isValid = await validateShareLink(shareId);
        if (!isValid) {
            ElMessage.error('分享链接已过期或不存在');
            return;
        }

        // 获取照片ID列表
        const response = await getSharePhotos(shareId);
        const photoIds = response;
        
        if (photoIds.length === 0) {
            ElMessage.warning('分享中没有照片');
            return;
        }

        // 加载缩略图
        photos.value = await getPhotosByIdsWithThumbnail(photoIds);
    } catch (error) {
        console.error('获取分享照片失败:', error);
        ElMessage.error('获取分享照片失败');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchSharePhotos();
});
</script>

<style scoped>
.share-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: linear-gradient(135deg, var(--qd-color-primary-light-9) 0%, var(--qd-color-primary-light-8) 100%);
    perspective: 1200px;
    position: relative;
    overflow: hidden;
}

/* 装饰性元素 */
.decorative-elements {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 1;
}

.floating-paper {
    position: absolute;
    width: 40px;
    height: 60px;
    background-color: white;
    border-radius: 4px;
    opacity: 0.7;
    transform-style: preserve-3d;
    animation: floatPaper 10s ease-in-out infinite;
}

.floating-paper:nth-child(1) {
    top: 15%;
    left: 10%;
    animation-delay: 0s;
}

.floating-paper:nth-child(2) {
    top: 25%;
    right: 15%;
    width: 50px;
    height: 70px;
    animation-delay: 2s;
}

.floating-paper:nth-child(3) {
    bottom: 20%;
    left: 20%;
    width: 35px;
    height: 55px;
    animation-delay: 4s;
}

.floating-paper:nth-child(4) {
    bottom: 30%;
    right: 25%;
    animation-delay: 6s;
}

.floating-paper:nth-child(5) {
    top: 50%;
    left: 30%;
    width: 45px;
    height: 65px;
    animation-delay: 8s;
}

@keyframes floatPaper {
    0%, 100% {
        transform: translateY(0) rotate(0) translateX(0);
    }
    25% {
        transform: translateY(-50px) rotate(5deg) translateX(20px);
    }
    50% {
        transform: translateY(-80px) rotate(-3deg) translateX(-10px);
    }
    75% {
        transform: translateY(-40px) rotate(2deg) translateX(15px);
    }
}

.box-shadow {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 80vw;
    height: 20px;
    background: radial-gradient(ellipse at center, rgba(0,0,0,0.2) 0%,rgba(0,0,0,0) 70%);
    border-radius: 50%;
}

.share-box {
    position: relative;
    width: 90vw;
    height: 90vh;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    transition: all 0.5s ease;
    transform-style: preserve-3d;
    z-index: 2;
    animation: arriveBox 1s ease-out;
}

@keyframes arriveBox {
    0% {
        transform: translateY(50px) scale(0.9);
        opacity: 0;
    }
    100% {
        transform: translateY(0) scale(1);
        opacity: 1;
    }
}

.box-lid {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, var(--qd-color-primary) 0%, var(--qd-color-primary-light-3) 100%);
    cursor: pointer;
    transition: transform 1.2s cubic-bezier(0.4, 0, 0.2, 1);
    transform-origin: top;
    z-index: 3;
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.2);
    border-radius: 12px;
}

.lid-decoration {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
}

.lid-stripe {
    position: absolute;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    width: 100%;
    height: 8px;
    background-color: rgba(255, 255, 255, 0.2);
}

.lid-handle {
    position: absolute;
    top: 40px;
    left: 50%;
    transform: translateX(-50%);
    width: 80px;
    height: 25px;
    background-color: rgba(255, 255, 255, 0.15);
    border-radius: 12px;
    border: 2px solid rgba(255, 255, 255, 0.3);
}

.lid-content {
    text-align: center;
    color: white;
    transform: translateY(-20px);
    animation: float 3s ease-in-out infinite;
    z-index: 2;
}

@keyframes float {
    0%, 100% {
        transform: translateY(-20px);
    }
    50% {
        transform: translateY(-35px);
    }
}

.open-icon {
    font-size: 48px;
    margin-bottom: 16px;
    animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
    0%, 100% {
        transform: scale(1);
    }
    50% {
        transform: scale(1.1);
    }
}

.box-open .box-lid {
    transform: rotateX(170deg);
}

.box-content {
    padding: 20px;
    height: 100%;
    overflow-y: auto;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
    animation: fadeIn 0.8s ease-out;
    position: relative;
    z-index: 2;
}

/* 添加盒子侧面 */
.box-sides {
    position: absolute;
    width: 100%;
    height: 100%;
    pointer-events: none;
}

.box-side {
    position: absolute;
    background: linear-gradient(to bottom, var(--qd-color-primary-light-6), var(--qd-color-primary-light-4));
    opacity: 0.7;
}

.box-side-left {
    top: 0;
    left: 0;
    width: 10px;
    height: 100%;
    transform-origin: left;
    transform: rotateY(90deg) translateX(-10px);
}

.box-side-right {
    top: 0;
    right: 0;
    width: 10px;
    height: 100%;
    transform-origin: right;
    transform: rotateY(-90deg) translateX(10px);
}

.box-side-front {
    bottom: 0;
    left: 0;
    width: 100%;
    height: 10px;
    transform-origin: bottom;
    transform: rotateX(90deg) translateY(10px);
}

.box-side-back {
    top: 0;
    left: 0;
    width: 100%;
    height: 10px;
    transform-origin: top;
    transform: rotateX(-90deg) translateY(-10px);
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.loading-container {
    padding: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
}

.empty-container {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    animation: fadeIn 0.5s ease-out;
}

.photo-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
    animation: staggerFadeIn 0.8s ease-out;
}

@keyframes staggerFadeIn {
    0% {
        opacity: 0;
        transform: translateY(30px);
    }
    100% {
        opacity: 1;
        transform: translateY(0);
    }
}

.photo-item {
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: var(--qd-color-primary-light-9);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    transform: translateY(0);
    animation: popIn 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    animation-fill-mode: both;
}

.photo-item:nth-child(3n) {
    animation-delay: 0.1s;
}

.photo-item:nth-child(3n+1) {
    animation-delay: 0.2s;
}

.photo-item:nth-child(3n+2) {
    animation-delay: 0.3s;
}

@keyframes popIn {
    0% {
        opacity: 0;
        transform: scale(0.8) translateY(20px);
    }
    70% {
        transform: scale(1.05) translateY(-5px);
    }
    100% {
        opacity: 1;
        transform: scale(1) translateY(0);
    }
}

.photo-item:hover {
    transform: scale(1.05) translateY(-5px) rotate(2deg);
    box-shadow: 0 12px 20px rgba(0, 0, 0, 0.2);
    z-index: 1;
}

.photo-item .el-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
    transition: transform 0.3s ease;
}

.photo-item .el-image :deep(img) {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.photo-item::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.photo-item:hover::after {
    opacity: 1;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .share-box {
        width: 95vw;
        height: 95vh;
    }
    
    .photo-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 15px;
    }
    
    .lid-handle {
        width: 60px;
        height: 20px;
    }
    
    .lid-stripe {
        width: 70%;
        height: 6px;
    }
}
</style> 