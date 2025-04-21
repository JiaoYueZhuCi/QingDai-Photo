<template>
    <el-dialog v-model="visible" width="80%" top="5vh" align-center class="preview-dialog" @close="handleClose">
        <div class="dialog-content">
            <!-- 左侧图片区域 -->
            <div class="image-container">
                <el-image :src="thumbnailUrl" fit="contain" class="preview-image" v-loading="isLoading"
                    element-loading-text="正在加载图片..." element-loading-background="rgba(0, 0, 0, 0.8)"
                    @click="openFullScreen">
                </el-image>
            </div>

            <!-- 右侧信息区域 -->
            <div class="info-container">
                <el-descriptions title="照片信息" :column="1" border class="metadata-descriptions">
                    <el-descriptions-item label="标题">
                        {{ previewData.title || '无标题' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="介绍">
                        <div class="description-content">{{ previewData.introduce || '暂无介绍' }}</div>
                    </el-descriptions-item>
                    <el-descriptions-item label="文件">
                        {{ previewData.fileName || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="相机">
                        {{ previewData.camera || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="镜头">
                        {{ previewData.lens || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="拍摄时间">
                        {{ previewData.time || '未知' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="参数">
                        <span v-if="previewData.aperture">光圈：{{ previewData.aperture }}</span>
                        <span v-if="previewData.shutter"> 快门：{{ previewData.shutter }}</span>
                        <span v-if="previewData.iso"> ISO：{{ previewData.iso }}</span>
                        <span v-if="!previewData.aperture && !previewData.shutter && !previewData.iso">未知</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="级别">
                        <el-tag v-if="previewData.start === 1" :type="'warning'">精选</el-tag>
                        <el-tag v-if="previewData.start === 0" :type="'success'">普通</el-tag>
                        <el-tag v-if="previewData.start === -1" :type="'info'">隐藏</el-tag>
                        <el-tag v-if="previewData.start === 2" :type="'primary'">气象</el-tag>
                    </el-descriptions-item>
                </el-descriptions>
            </div>
        </div>
        <PhotoViewer v-model="showFullScreen" :photo-id="props.photoId" />
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted, onMounted } from 'vue'
import { ElDialog, ElImage, ElDescriptions, ElDescriptionsItem, ElTag } from 'element-plus'
import { get1000KPhoto, getPhotoDetailInfo, type EnhancedWaterfallItem } from '@/utils/photo'
import PhotoViewer from '@/components/photo/PhotoViewer.vue'

const props = defineProps<{
    photoId: string,
    modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue', 'close'])

// 添加打开全屏预览的方法
const showFullScreen = ref(false)
const openFullScreen = () => {
    showFullScreen.value = true;
}
const closeFullScreen = () => {
    showFullScreen.value = false;
}

const thumbnailUrl = ref('')
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

const isLoading = ref(false)
const visible = ref(props.modelValue)

// 监听 modelValue 的变化
watch(() => props.modelValue, (newVal) => {
    visible.value = newVal
    if (newVal) {
        // 每次打开时都重新加载数据
        loadData()
    }
})

// 监听 dialogVisible 的变化
watch(() => visible.value, (newVal) => {
    emit('update:modelValue', newVal)
    if (newVal == false) {
        emit('close')
    }
})

// 加载数据
const loadData = async () => {
    if (!props.photoId) return

    isLoading.value = true

    try {
        const imageResult = await get1000KPhoto(props.photoId, (loading) => isLoading.value = loading)
        if (imageResult) {
            thumbnailUrl.value = imageResult.url
        }

        // 获取照片详细信息
        const photoInfo = await getPhotoDetailInfo(props.photoId)
        if (photoInfo) {
            previewData.value = photoInfo
        }
    } catch (error) {
        console.error('数据加载失败:', error)
    } finally {
        isLoading.value = false
    }
}

const handleClose = () => {
    if (thumbnailUrl.value) {
        URL.revokeObjectURL(thumbnailUrl.value)
        thumbnailUrl.value = ''
    }

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
}

// 组件卸载时确保清理资源
onUnmounted(() => {
    if (thumbnailUrl.value) {
        URL.revokeObjectURL(thumbnailUrl.value)
    }
})
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
    word-break: break-word;
}

.metadata-descriptions {
    --el-descriptions-item-bordered-label-background: #f8f9fa;
}

.description-content {
    max-height: 120px;
    overflow-y: auto;
    white-space: pre-wrap;
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