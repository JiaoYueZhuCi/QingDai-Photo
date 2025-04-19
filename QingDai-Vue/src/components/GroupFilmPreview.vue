<template>
    <div class="group-film-preview">
        <!-- 引用FilmPreview组件 -->
        <FilmPreview :photo-id="photoId" :model-value="visible" :photo-ids="photoIds" @close="handleClose"
            @navigate="handleNavigate" @image-click="openFullScreen" @update:model-value="updateVisible" />

        <!-- 全屏预览组件 -->
        <PhotoViewer v-if="showFullScreen" :photo-id="fullScreenPhotoId" @close="closeFullScreen" />

        <!-- 组图标题和信息按钮 -->
        <div class="group-title-container" v-if="visible">
            <div class="group-title-box" @click="toggleGroupInfo">
                <span class="group-title">{{ groupPhotoData.title || '无标题组图' }}</span>
                <el-icon><arrow-down v-if="!showGroupInfo" /><arrow-up v-if="showGroupInfo" /></el-icon>
            </div>

            <!-- 组图信息弹出层 -->
            <transition name="slide-fade">
                <div class="group-info-panel" v-if="showGroupInfo">
                    <div class="panel-content">
                        <div class="custom-info-box">
                            <div class="info-row">
                                <div class="info-label">标题</div>
                                <div class="info-value">{{ groupPhotoData.title || '无标题' }}</div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">介绍</div>
                                <div class="info-value description-content">{{ groupPhotoData.introduce || '暂无介绍' }}</div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">数量</div>
                                <div class="info-value">{{ photoIds.length }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </transition>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElIcon } from 'element-plus'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import FilmPreview from './FilmPreview.vue'
import PhotoViewer from '@/components/PhotoViewer.vue'
import { getGroupPhoto } from '@/api/groupPhoto'
import type { GroupPhotoDTO } from '@/types/groupPhoto'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps<{
    groupId: string,
    initialPhotoId?: string,
    modelValue: boolean
}>()

const emit = defineEmits(['close', 'update:modelValue', 'navigate'])

// 路由实例
const router = useRouter()
const route = useRoute()

// 状态变量
const visible = ref(props.modelValue)
const photoId = ref(props.initialPhotoId || '')
const photoIds = ref<string[]>([])
const showGroupInfo = ref(false)

// 添加全屏预览相关的状态
const showFullScreen = ref(false)
const fullScreenPhotoId = ref('')

// 组图数据
const groupPhotoData = ref({
    id: '',
    title: '',
    introduce: '',
    coverPhotoId: ''
})

// 更新URL中的组图预览参数
const updateUrlWithGroupParams = (showPreview: boolean, groupId: string | null = null, photoId: string | null = null, isFullScreen: boolean = false) => {
    // 构建新的查询参数对象
    const query = { ...route.query }
    
    if (showPreview && groupId) {
        query.groupId = groupId
        
        if (isFullScreen) {
            // 如果是全屏模式
            if (photoId) {
                query.groupViewerId = photoId
            } else {
                delete query.groupViewerId
            }
            delete query.groupPhotoId
        } else {
            // 如果是普通预览模式
            if (photoId) {
                query.groupPhotoId = photoId
            } else {
                delete query.groupPhotoId
            }
            delete query.groupViewerId
        }
        
        // 确保其他预览参数被清除
        delete query.photoId
        delete query.viewerId
    } else {
        // 清除所有组图相关参数
        delete query.groupId
        delete query.groupPhotoId
        delete query.groupViewerId
    }
    
    // 更新路由，保留当前路径，仅修改查询参数
    router.replace({
        path: route.path,
        query: query
    })
}

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
    visible.value = newVal
    if (newVal) {
        // 如果是显示组件，更新URL
        updateUrlWithGroupParams(true, props.groupId, photoId.value)
    } else {
        // 如果是隐藏组件，清除URL参数
        updateUrlWithGroupParams(false)
    }
})

// 监听 visible 变化
watch(visible, (newVal) => {
    emit('update:modelValue', newVal)
    if (!newVal) {
        updateUrlWithGroupParams(false)
    }
})

// 加载组图数据
const loadGroupData = async () => {
    try {
        // 获取组图数据
        const response = await getGroupPhoto(props.groupId)

        // 保存组图数据
        groupPhotoData.value = response.groupPhoto

        // 保存照片ID列表
        photoIds.value = response.photoIds

        // 设置初始照片ID
        if (props.initialPhotoId && photoIds.value.includes(props.initialPhotoId)) {
            photoId.value = props.initialPhotoId
            // 更新URL
            updateUrlWithGroupParams(true, props.groupId, photoId.value)
        } else if (photoIds.value.length > 0) {
            photoId.value = photoIds.value[0]
            // 更新URL
            updateUrlWithGroupParams(true, props.groupId, photoId.value)
        }
    } catch (error) {
        console.error('组图数据加载失败:', error)
    }
}

// 处理关闭事件
const handleClose = () => {
    visible.value = false
    updateUrlWithGroupParams(false)
    emit('close')
}

// 处理导航事件
const handleNavigate = (id: string) => {
    photoId.value = id
    // 更新URL
    updateUrlWithGroupParams(true, props.groupId, id)
    emit('navigate', id)
}

// 处理图片点击事件
const handleImageClick = (id: string) => {
    // 这里可以添加图片点击逻辑
}

// 添加打开全屏预览的方法
const openFullScreen = (id: string) => {
    fullScreenPhotoId.value = id
    showFullScreen.value = true
    // 更新URL为全屏查看模式
    updateUrlWithGroupParams(true, props.groupId, id, true)
}

// 添加关闭全屏预览的方法
const closeFullScreen = () => {
    showFullScreen.value = false
    fullScreenPhotoId.value = ''
    // 恢复为普通预览模式
    updateUrlWithGroupParams(true, props.groupId, photoId.value)
}

// 更新visible状态
const updateVisible = (val: boolean) => {
    visible.value = val
    if (!val) {
        updateUrlWithGroupParams(false)
    }
}

// 切换组图信息显示状态
const toggleGroupInfo = () => {
    showGroupInfo.value = !showGroupInfo.value
}

// 组件挂载时加载数据
onMounted(() => {
    if (visible.value) {
        loadGroupData()
        
        // 检查URL中是否有全屏查看的参数
        if (route.query.groupViewerId) {
            const viewerId = route.query.groupViewerId as string
            if (viewerId) {
                fullScreenPhotoId.value = viewerId
                showFullScreen.value = true
            }
        }
    }
})

// 监听 groupId 变化
watch(() => props.groupId, () => {
    if (visible.value) {
        loadGroupData()
    }
})
</script>

<style scoped>
.group-film-preview {
    position: relative;
}

.group-title-container {
    position: fixed;
    top: 1vh;
    left: 50%;
    transform: translateX(-50%);
    z-index: 2100;
    width: auto;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.group-title-box {
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 8px 16px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content:space-between;
    gap: 8px;
    cursor: pointer;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
    transition: all 0.3s;
}

.group-title-box:hover {
    background: rgba(0, 0, 0, 0.9);
}

.group-title {
    font-size: 16px;
    font-weight: bold;
}

.group-info-panel {
    margin-top: 10px;
    width: 400px;
    max-height: 60vh;
    background: rgba(0, 0, 0, 0.8);
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
    overflow-y: auto;
    backdrop-filter: blur(5px);
}

.panel-content {
    padding: 16px;
    color: white;
}

.custom-info-box {
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    overflow: hidden;
}

.info-row {
    display: flex;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.info-row:last-child {
    border-bottom: none;
}

.info-label {
    flex: 0 0 100px;
    padding: 12px;
    background: rgba(255, 255, 255, 0.1);
    font-weight: bold;
    border-right: 1px solid rgba(255, 255, 255, 0.2);
}

.info-value {
    flex: 1;
    padding: 12px;
}

.description-content {
    white-space: pre-wrap;
    overflow: auto;
    max-height: 200px;
}

/* 动画效果 */
.slide-fade-enter-active,
.slide-fade-leave-active {
    transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
    transform: translateY(-20px);
    opacity: 0;
}

@media (max-width: 768px) {
    .group-info-panel {
        width: 90vw;
    }

    .group-title-container {
        top: 2vh;
    }

    .group-title-box {
        width: 74vw;
        height: 4vh;
    }
}
</style>

<style>
.film-strip {
    top: 3vh !important;
}
</style>