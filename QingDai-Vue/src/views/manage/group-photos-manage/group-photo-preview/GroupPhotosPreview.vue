<template>
    <el-dialog v-model="visible" width="80%" top="5vh" align-center class="preview-dialog" :close-on-click-modal="true"
        :before-close="handleClose" @open="handleOpen" @close="handleClose">
        <div class="dialog-content">
            <!-- 左侧图片区域 -->
            <div class="image-container">
                <el-image :src="thumbnailUrl" fit="contain" class="preview-image" v-loading="isLoading"
                    element-loading-text="正在加载图片..." element-loading-background="rgba(0, 0, 0, 0.8)"
                    @click="openFullScreen">
                    <template #error>
                        <div class="image-error">
                            <el-icon><picture-filled /></el-icon>
                            <span>图片加载失败</span>
                        </div>
                    </template>
                </el-image>

                <!-- 图片导航按钮 -->
                <div class="navigation-buttons">
                    <el-button circle class="nav-button prev" @click="prevPhoto" :disabled="currentPhotoIndex <= 0">
                        <el-icon><arrow-left /></el-icon>
                    </el-button>
                    <el-button circle class="nav-button next" @click="nextPhoto"
                        :disabled="currentPhotoIndex >= photoIds.length - 1">
                        <el-icon><arrow-right /></el-icon>
                    </el-button>
                </div>

                <!-- 图片计数 -->
                <div class="photo-counter">
                    {{ currentPhotoIndex + 1 }} / {{ photoIds.length }}
                </div>
            </div>

            <!-- 右侧信息区域 -->
            <div class="info-container">
                <!-- 选项卡切换 -->
                <el-tabs v-model="activeTab" class="info-tabs">
                    <el-tab-pane label="组图信息" name="group">
                        <el-descriptions :column="1" border class="metadata-descriptions">
                            <el-descriptions-item label="标题">
                                {{ groupPhotoData.groupPhoto.title || '无标题' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="介绍">
                                <div class="description-content">{{ groupPhotoData.groupPhoto.introduce || '暂无介绍' }}
                                </div>
                            </el-descriptions-item>
                            <el-descriptions-item label="包含照片数">
                                {{ photoIds.length }}
                            </el-descriptions-item>
                        </el-descriptions>
                    </el-tab-pane>
                    <el-tab-pane label="照片信息" name="photo">
                        <el-descriptions :column="1" border class="metadata-descriptions">
                            <el-descriptions-item label="标题">
                                {{ currentPhotoData.title || '无标题' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="介绍">
                                <div class="description-content">{{ currentPhotoData.introduce || '暂无介绍' }}</div>
                            </el-descriptions-item>
                            <el-descriptions-item label="作者">
                                {{ currentPhotoData.author || '未知' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="文件">
                                {{ currentPhotoData.fileName || '未知' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="相机">
                                {{ currentPhotoData.camera || '未知' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="镜头">
                                {{ currentPhotoData.lens || '未知' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="拍摄时间">
                                {{ currentPhotoData.time || '未知' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="参数">
                                <span v-if="currentPhotoData.aperture">光圈：{{ currentPhotoData.aperture }}</span>
                                <span v-if="currentPhotoData.shutter"> 快门：{{ currentPhotoData.shutter }}</span>
                                <span v-if="currentPhotoData.iso"> ISO：{{ currentPhotoData.iso }}</span>
                                <span
                                    v-if="!currentPhotoData.aperture && !currentPhotoData.shutter && !currentPhotoData.iso">未知</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="级别">
                                <el-tag v-if="currentPhotoData.start === 1" :type="'warning'">
                                    精选
                                </el-tag>
                                <el-tag v-if="currentPhotoData.start === 0" :type="'success'">
                                    普通
                                </el-tag>
                                <el-tag v-if="currentPhotoData.start === -1" :type="'info'">
                                    隐藏
                                </el-tag>
                                <el-tag v-if="currentPhotoData.start === 2" :type="'primary'">
                                    气象
                                </el-tag>
                            </el-descriptions-item>
                        </el-descriptions>
                    </el-tab-pane>
                </el-tabs>

                <!-- 缩略图浏览 -->
                <div class="thumbnail-browser">
                    <h4>所有照片</h4>
                    <div class="thumbnail-list">
                        <div v-for="(id, index) in photoIds" :key="id"
                            :class="['thumbnail-item', { active: index === currentPhotoIndex }]"
                            @click="selectPhoto(index)">
                            <div class="thumbnail-index">{{ index + 1 }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <PhotoViewer v-model="showFullScreen" :photo-id="fullScreenPhotoId" @close="closeFullScreen" />
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElDialog, ElImage, ElDescriptions, ElDescriptionsItem, ElButton, ElIcon, ElTabs, ElTabPane, ElTag } from 'element-plus'
import { ArrowLeft, ArrowRight, PictureFilled } from '@element-plus/icons-vue'
import type { WaterfallItem } from '@/types'
import type { GroupPhotoDTO } from '@/types/groupPhoto'
import { getGroupPhoto } from '@/api/groupPhoto'
import PhotoViewer from '@/components/photo/photo-viewer/PhotoViewer.vue'
import { get1000KPhoto, getPhotoDetailInfo, type EnhancedWaterfallItem } from '@/utils/photo'

const props = defineProps<{
    modelValue: boolean;
    groupId: string;
    initialPhotoId?: string;
    photos?: WaterfallItem[];
}>()

const emit = defineEmits(['close', 'update:modelValue'])

// 内部可见性状态和全屏预览状态
const visible = ref(props.modelValue)
const showFullScreen = ref(false)

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
    visible.value = newVal;
    if (newVal && photoIds.value.length === 0) {
        handleOpen();
    }
});

// 监听 visible 变化
watch(() => visible.value, (newVal) => {
    emit('update:modelValue', newVal);
    if (newVal == false) {
        emit('close')
    }
});

// 组件挂载时设置初始值
onMounted(() => {
    visible.value = props.modelValue;
    if (visible.value && photoIds.value.length === 0) {
        handleOpen();
    }
    window.addEventListener('keydown', handleKeyDown);
});

// 对话框状态
const thumbnailUrl = ref('')
const isLoading = ref(false)
const activeTab = ref('group')

// 组图数据
const groupPhotoData = ref<GroupPhotoDTO>({
    groupPhoto: {
        id: '',
        title: '',
        introduce: '',
        coverPhotoId: '',
    },
    photoIds: [],
})

// 当前照片和照片列表
const photoIds = ref<string[]>([])
const currentPhotoIndex = ref(0)
const currentPhotoData = ref<EnhancedWaterfallItem>({
    id: '',
    fileName: '',
    author: '',
    width: 0,
    height: 0,
    aperture: '',
    iso: '',
    shutter: '',
    camera: '',
    lens: '',
    focalLength: '',
    shootTime: '',
    title: '',
    introduce: '',
    startRating: 0
})

// 添加全屏预览相关的状态
const fullScreenPhotoId = ref('')

// 打开对话框时获取数据
const handleOpen = async () => {
    isLoading.value = true
    try {
        // 获取组图数据
        const response = await getGroupPhoto(props.groupId)

        // 适配接口返回数据
        groupPhotoData.value = response;
        // 提取照片ID列表
        photoIds.value = groupPhotoData.value.photoIds;
        // 如果有指定初始照片，则设置为当前照片
        if (props.initialPhotoId && photoIds.value.includes(props.initialPhotoId)) {
            currentPhotoIndex.value = photoIds.value.indexOf(props.initialPhotoId)
        } else {
            currentPhotoIndex.value = 0
        }

        // 加载当前照片
        if (photoIds.value.length > 0) {
            await loadCurrentPhoto()
        }
    } catch (error) {
        console.error('组图数据加载失败:', error)
    } finally {
        isLoading.value = false
    }
}

// 加载当前照片
const loadCurrentPhoto = async () => {
    if (photoIds.value.length === 0) {
        return;
    }

    isLoading.value = true
    try {
        const currentId = photoIds.value[currentPhotoIndex.value]

        // 释放之前的URL
        if (thumbnailUrl.value) {
            URL.revokeObjectURL(thumbnailUrl.value)
            thumbnailUrl.value = ''
        }

        // 加载照片
        const imageResult = await get1000KPhoto(currentId)
        if (imageResult) {
            thumbnailUrl.value = imageResult.url
        }

        // 获取照片信息
        const photoInfo = await getPhotoDetailInfo(currentId)
        if (photoInfo) {
            currentPhotoData.value = photoInfo
            // 自动切换到照片信息标签
            activeTab.value = 'photo'
        }
    } catch (error) {
        console.error('照片加载失败:', error)
    } finally {
        isLoading.value = false
    }
}

// 上一张照片
const prevPhoto = () => {
    if (currentPhotoIndex.value > 0) {
        currentPhotoIndex.value--
    }
}

// 下一张照片
const nextPhoto = () => {
    if (currentPhotoIndex.value < photoIds.value.length - 1) {
        currentPhotoIndex.value++
    }
}

// 选择特定照片
const selectPhoto = (index: number) => {
    currentPhotoIndex.value = index
}

// 关闭对话框
const handleClose = () => {
    if (thumbnailUrl.value) {
        URL.revokeObjectURL(thumbnailUrl.value)
        thumbnailUrl.value = ''
    }
    visible.value = false;
}

// 监听当前照片索引变化
watch(currentPhotoIndex, () => {
    loadCurrentPhoto()
})

// 添加键盘导航
const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'ArrowLeft') {
        prevPhoto()
    } else if (e.key === 'ArrowRight') {
        nextPhoto()
    } else if (e.key === 'Escape') {
        handleClose()
    }
}

// 添加打开全屏预览的方法
const openFullScreen = async () => {
    if (!photoIds.value[currentPhotoIndex.value]) return;
    fullScreenPhotoId.value = photoIds.value[currentPhotoIndex.value];
    showFullScreen.value = true;
}

// 添加关闭全屏预览的方法
const closeFullScreen = () => {
    fullScreenPhotoId.value = '';
}

// 组件卸载时移除键盘监听
onUnmounted(() => {
    window.removeEventListener('keydown', handleKeyDown)
})

</script>

<style scoped>
:deep(.el-tabs__item) {
    padding: 0 10px !important;
}

.preview-dialog {
    border-radius: 12px;
    overflow: hidden;
}

.dialog-content {
    display: flex;
    gap: 18px;
    height: 80vh;
    max-height: 900px;
}

.image-container {
    position: relative;
    flex: 1;
    min-width: 0;
    background: #000;
    border-radius: 8px;
    padding: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
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

.navigation-buttons {
    position: absolute;
    width: 100%;
    display: flex;
    justify-content: space-between;
    top: 50%;
    transform: translateY(-50%);
    padding: 0 20px;
    z-index: 10;
    pointer-events: none;
}

.nav-button {
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    border: none;
    width: 40px;
    height: 40px;
    transition: all 0.3s;
    pointer-events: auto;
}

.nav-button:hover:not(:disabled) {
    background-color: rgba(0, 0, 0, 0.8);
    transform: scale(1.1);
}

.nav-button:disabled {
    opacity: 0.5;
    background-color: rgba(0, 0, 0, 0.3);
}

.photo-counter {
    position: absolute;
    bottom: 20px;
    right: 20px;
    background-color: rgba(0, 0, 0, 0.6);
    color: var(--qd-color-primary-light-8);
    padding: 5px 12px;
    border-radius: 15px;
    font-size: 14px;
}

.info-container {
    flex: 0 0 320px;
    display: flex;
    flex-direction: column;
    overflow: scroll;
}

.description-content {
    max-height: 120px;
    overflow-y: auto;
    white-space: pre-wrap;
}

.metadata-descriptions {
    --el-descriptions-item-bordered-label-background: #f8f9fa;
    word-break: break-all;
}

.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
    height: 100%;
    width: 100%;
}

.image-error .el-icon {
    font-size: 48px;
    margin-bottom: 10px;
}

/* 缩略图浏览区域 */
.thumbnail-browser {
    margin-top: 16px;
    padding-top: 8px;
    border-top: 1px solid #eee;
}

.thumbnail-browser h4 {
    margin: 0 0 8px 0;
    font-size: 14px;
    color: var(--qd-color-primary-light-2);
}

.thumbnail-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    max-height: 100px;
    overflow-y: auto;
    padding-right: 8px;
}

.thumbnail-item {
    width: 35px;
    height: 35px;
    background-color: var(--qd-color-primary-light-7);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;
}

.thumbnail-item:hover {
    background-color: var(--qd-color-primary-light-6);
}

.thumbnail-item.active {
    background-color: var(--qd-color-primary);
    color: var(--qd-color-primary-light-9);
}

.thumbnail-index {
    font-size: 13px;
    font-weight: bold;
}

@media (max-width: 768px) {
    .dialog-content {
        flex-direction: column;
        height: auto;
        max-height: none;
    }

    .image-container {
        height: 50vh;
    }

    .info-container {
        flex: none;
        max-width: none;
        width: 100%;
    }

    .navigation-buttons {
        padding: 0 10px;
    }

    .nav-button {
        width: 36px;
        height: 36px;
    }

    .thumbnail-list {
        max-height: 80px;
    }
}
</style>