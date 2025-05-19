<template>
    <div v-if="visible" class="showViewer">
        <el-image-viewer v-if="urlList.length > 0" :url-list="urlList" :initial-index="initialIndex"
            :hide-on-click-modal="true" @close="handleClose" ref="imageViewerRef" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, nextTick, watch, computed } from 'vue'
import { ElImageViewer, ElLoading, ElMessage } from 'element-plus'
import { get1000KPhoto, getFullPhoto } from '@/utils/photo'
import { getUserRoles, canViewFullSizePhoto } from '@/utils/auth'

// 保存原始body样式以便恢复
let originalBodyPadding = ''
let scrollbarWidth = 0

// 计算滚动条宽度
const getScrollbarWidth = () => {
    const scrollDiv = document.createElement('div')
    scrollDiv.style.cssText = 'width: 100px;height: 100px;overflow: scroll;position: absolute;top: -9999px;'
    document.body.appendChild(scrollDiv)
    const width = scrollDiv.offsetWidth - scrollDiv.clientWidth
    document.body.removeChild(scrollDiv)
    return width
}

const props = defineProps<{
    modelValue: boolean;
    photoId?: string;
    initialIndex?: number;
    urlList?: string[];
}>()

const emit = defineEmits(['update:modelValue', 'close'])
const urlList = ref<string[]>([])
const imageViewerRef = ref<any>(null)
const visible = ref(props.modelValue)

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
    visible.value = newVal
})

watch(() => visible.value, (newVal) => {
    emit('update:modelValue', newVal)
    if (newVal == false) {
        emit('close')
    }
})

// 监听 urlList 变化
watch(() => props.urlList, (newVal) => {
    if (newVal && newVal.length > 0) {
        urlList.value = newVal
    }
}, { immediate: true })

// 组件挂载时处理滚动条和body样式
onMounted(() => {
    visible.value = props.modelValue
    originalBodyPadding = document.body.style.paddingRight
    scrollbarWidth = getScrollbarWidth()

    if (visible.value) {
        document.body.style.overflow = 'hidden'
        document.body.style.paddingRight = `${scrollbarWidth}px`
        if (props.photoId) {
            loadImages()
        }
    }
})

// 组件卸载时恢复body样式
onUnmounted(() => {
    document.body.style.overflow = ''
    document.body.style.paddingRight = originalBodyPadding
})

// 监听visible变化来设置body样式
watch(() => visible.value, (newVal) => {
    if (newVal) {
        document.body.style.overflow = 'hidden'
        document.body.style.paddingRight = `${scrollbarWidth}px`
    } else {
        document.body.style.overflow = ''
        document.body.style.paddingRight = originalBodyPadding
    }
})

// 监听initialIndex变化
watch(() => props.initialIndex, (newVal, oldVal) => {
    if (newVal !== oldVal && imageViewerRef.value) {
        // 使用nextTick确保DOM已更新
        nextTick(() => {
            // 设置当前索引
            if (imageViewerRef.value && typeof imageViewerRef.value.setActiveIndex === 'function') {
                imageViewerRef.value.setActiveIndex(newVal || 0);
            }
        })
    }
})

// 加载图片
const loadImages = async () => {
    if (!props.photoId) return

    const loading = ElLoading.service({
        target: '.showViewer',
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)'
    })

    try {
        // 检查用户权限
        const userRoles = await getUserRoles()
        const canViewFull = await canViewFullSizePhoto()

        // 根据权限获取不同质量的图片
        // const imageResult = canViewFull
        //     ? await getFullPhoto(props.photoId)
        //     : await get1000KPhoto(props.photoId)

        const imageResult = await get1000KPhoto(props.photoId)

        if (imageResult) {
            urlList.value = [imageResult.url]
        }
    } catch (error) {
        console.error('加载图片失败:', error)
        ElMessage.error('加载图片失败')
    } finally {
        loading.close()
    }
}

// 处理关闭
const handleClose = () => {
    visible.value = false
}
</script>

<style scoped>
.showViewer {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    background-color: rgba(0, 0, 0, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>

<style>
body {
    transition: padding-right 0.3s ease;
}

/* 修复Element Plus图片查看器的显示问题 */
.el-image-viewer__img {
    display: block !important;
}
</style>