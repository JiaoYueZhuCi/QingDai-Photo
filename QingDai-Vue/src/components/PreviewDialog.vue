<template>
    <el-dialog v-model="visible" width="80%" top="5vh" align-center class="preview-dialog" @open="handleOpen"
        @close="handleClose">
        <div class="dialog-content">
            <!-- 左侧图片区域 -->
            <div class="image-container">
                <el-image :src="thumbnailUrl" fit="contain" class="preview-image" v-loading="isLoading"
                    element-loading-text="正在加载图片..." element-loading-background="rgba(0, 0, 0, 0.8)"
                    @click="$emit('image-click', photoId)">
                </el-image>
            </div>

            <!-- 右侧信息区域 -->
            <div class="info-container">
                <el-descriptions title="照片信息" :column="1" border class="metadata-descriptions">
                    <el-descriptions-item label="标题">
                        {{ previewData.title || '无标题' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="介绍">
                        {{ previewData.introduce || '暂无介绍' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="相机型号">
                        {{ previewData.camera || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="镜头型号">
                        {{ previewData.lens || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="拍摄时间">
                        {{ previewData.time || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="光圈">
                        {{ previewData.aperture || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="快门">
                        {{ previewData.shutter || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="ISO">
                        {{ previewData.iso || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="级别">
                        {{ previewData.start === 1 ? '精选' : previewData.start === 0 ? '普通' : '私密' }}
                    </el-descriptions-item>
                </el-descriptions>
            </div>
        </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { ElDialog, ElImage, ElDescriptions, ElDescriptionsItem, ElIcon, ElLoading } from 'element-plus'
import { Picture as IconPicture, Loading } from '@element-plus/icons-vue'
import type { WaterfallItem } from '@/types'
import axios from 'axios'



const props = defineProps<{
    photoId: string
}>()

const emit = defineEmits(['close', 'image-click'])

const visible = ref(false)
const thumbnailUrl = ref('')
const emptyData = ref<WaterfallItem>({
    id: "",          // 照片id 
    fileName: "",     // 照片名称
    author: "",    // 作者信息
    width: 0,   // 原始宽度（像素）
    height: 0,  // 原始高度（像素）
    aperture: "", // 光圈
    iso: "", // 感光度
    shutter: "",  // 快门
    camera: "",  // 相机
    lens: "",  // 镜头
    time: "",  // 拍摄时间
    title: "",  // 标题
    introduce: "",  // 介绍
    start: 0, //代表作
})
const previewData = emptyData

const isLoading = ref(false)

// 打开对话框时获取数据
const handleOpen = async () => {
    isLoading.value = true
    try {
        // 获取缩略图
        const thumbRes = await axios.get(`/api/QingDai/photo/getThumbnail1000KPhoto?id=${props.photoId}`, {
            responseType: 'blob'
        })
        thumbnailUrl.value = URL.createObjectURL(thumbRes.data)

        // 获取元数据
        const infoRes = await axios.get(`/api/QingDai/photo/getPhotoInfo?id=${props.photoId}`)
        previewData.value = { ...previewData.value, ...infoRes.data }
    } catch (error) {
        console.error('数据加载失败:', error)
    } finally {
        isLoading.value = false
    }
}

const handleClose = () => {
    thumbnailUrl.value = ''
    previewData.value = emptyData.value
    emit('close')
    URL.revokeObjectURL(thumbnailUrl.value)
}
</script>

<style scoped>
.preview-dialog {
    border-radius: 12px;
    overflow: hidden;
}

.dialog-content {
    display: flex;
    gap: 18px;
    height: 85vh;
}

.image-container {
    flex: 1;
    min-width: 0;

    background: black;
    border-radius: 8px;
    padding: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.preview-image {
    max-width: 100%;
    height: 100%;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;
}

.preview-image:hover {
    transform: scale(1.02);
}

.info-container {
    flex: 0 0 320px;
    overflow-y: auto;
    padding-right: 8px;
}

.metadata-descriptions {
    --el-descriptions-item-bordered-label-background: #f8f9fa;
}

.image-loading,
.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--el-color-info);
    height: 300px;
}

.image-loading .el-icon {
    margin-bottom: 8px;
    font-size: 32px;
    animation: rotating 2s linear infinite;
}

@keyframes rotating {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

@media (max-width: 768px) {
    .dialog-content {
        flex-direction: column;
        height: auto;
    }

    .info-container {
        flex: none;
        width: 100%;
    }
}

</style>