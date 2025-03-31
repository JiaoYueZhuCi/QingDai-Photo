<template>
    <div class="simple-photo-viewer" v-loading="loading" element-loading-text="加载中...">
        <div class="photo-container" v-if="photoUrl">
            <img :src="photoUrl" alt="照片预览" />
        </div>
        <div class="photo-info" v-if="photoInfo">
            <h3>{{ photoInfo.title || '无标题' }}</h3>
            <p>{{ photoInfo.introduce || '无介绍' }}</p>
            <div class="photo-metadata">
                <p v-if="photoInfo.camera"><strong>相机:</strong> {{ photoInfo.camera }}</p>
                <p v-if="photoInfo.lens"><strong>镜头:</strong> {{ photoInfo.lens }}</p>
                <p v-if="photoInfo.aperture"><strong>光圈:</strong> {{ photoInfo.aperture }}</p>
                <p v-if="photoInfo.shutter"><strong>快门:</strong> {{ photoInfo.shutter }}</p>
                <p v-if="photoInfo.iso"><strong>ISO:</strong> {{ photoInfo.iso }}</p>
                <p v-if="photoInfo.time"><strong>时间:</strong> {{ photoInfo.time }}</p>
            </div>
        </div>
        <div class="photo-controls">
            <el-button type="primary" @click="onClose">关闭</el-button>
            <el-button type="success" @click="onDownload" v-if="isDownloadable">下载原图</el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElButton } from 'element-plus';
import { get1000KPhotos, getFullPhotos, getPhotoDetailInfo, type EnhancedWaterfallItem } from '@/utils/photo';
import { getRolesPermissions } from '@/api/user';

const props = defineProps<{
    photoId: string;
    defaultQuality?: 'high' | 'full';  // high = 1000K, full = 原图
}>();

const emit = defineEmits(['close', 'download']);

const loading = ref(true);
const photoUrl = ref('');
const photoInfo = ref<EnhancedWaterfallItem | null>(null);
const isDownloadable = ref(false);
let fullImageUrl = '';

// 加载照片
onMounted(async () => {
    loading.value = true;
    
    try {
        // 检查下载权限
        const userRoles = await getUserRoles();
        isDownloadable.value = userRoles.includes('VIEWER');
        
        // 根据指定质量或权限加载不同质量照片
        const qualityToUse = props.defaultQuality === 'full' || isDownloadable.value ? 'full' : 'high';
        
        // 加载照片
        const imageResult = qualityToUse === 'full' 
            ? await getFullPhotos(props.photoId) 
            : await get1000KPhotos(props.photoId);
            
        if (imageResult) {
            photoUrl.value = imageResult.url;
            
            // 如果有权限，预加载原图用于下载
            if (isDownloadable.value && qualityToUse !== 'full') {
                const fullResult = await getFullPhotos(props.photoId);
                if (fullResult) {
                    fullImageUrl = fullResult.url;
                }
            }
        } else {
            ElMessage.error('照片加载失败');
            onClose();
            return;
        }
        
        // 加载照片信息
        const info = await getPhotoDetailInfo(props.photoId);
        if (info) {
            photoInfo.value = info;
        }
    } catch (error) {
        console.error('加载照片失败:', error);
        ElMessage.error('加载照片失败');
        onClose();
    } finally {
        loading.value = false;
    }
});

// 获取用户角色
const getUserRoles = async (): Promise<string[]> => {
    const token = localStorage.getItem('token');
    if (!token) return [];
    
    try {
        const response = await getRolesPermissions();
        return response?.roles || [];
    } catch (error) {
        console.error('获取用户角色失败:', error);
        return [];
    }
};

// 下载照片
const onDownload = () => {
    if (!isDownloadable.value) {
        ElMessage.warning('您没有下载权限');
        return;
    }
    
    const url = fullImageUrl || photoUrl.value;
    if (!url) {
        ElMessage.error('下载链接不可用');
        return;
    }
    
    const a = document.createElement('a');
    a.href = url;
    a.download = photoInfo.value?.fileName || `photo-${props.photoId}.jpg`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    
    emit('download', props.photoId);
};

// 关闭预览
const onClose = () => {
    emit('close');
};

// 清理资源
onUnmounted(() => {
    if (photoUrl.value) {
        URL.revokeObjectURL(photoUrl.value);
    }
    if (fullImageUrl) {
        URL.revokeObjectURL(fullImageUrl);
    }
});
</script>

<style scoped>
.simple-photo-viewer {
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #1a1a1a;
    color: #fff;
    padding: 20px;
    min-height: 400px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.photo-container {
    width: 100%;
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.photo-container img {
    max-width: 100%;
    max-height: 70vh;
    object-fit: contain;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.photo-info {
    width: 100%;
    max-width: 800px;
    padding: 15px;
    background-color: rgba(0, 0, 0, 0.3);
    border-radius: 4px;
    margin-bottom: 20px;
}

.photo-info h3 {
    margin-top: 0;
    margin-bottom: 10px;
    font-size: 1.5rem;
}

.photo-metadata {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 10px;
    margin-top: 15px;
}

.photo-metadata p {
    margin: 0;
}

.photo-controls {
    display: flex;
    gap: 10px;
    margin-top: 20px;
}

@media (max-width: 768px) {
    .photo-metadata {
        grid-template-columns: 1fr;
    }
}
</style> 