<template>
    <div class="showViewer">
        <el-image-viewer :url-list="urlList" :initial-index="initialIndex" :hide-on-click-modal="true"
            @close="handleClose" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElImageViewer, ElLoading, ElMessage } from 'element-plus'
import { getRolesPermissions } from '@/api/user'
import { get1000KPhotos, getFullPhotos } from '@/utils/photo'

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
    photoId?: string;
    initialIndex?: number;
    urlList?: string[];
}>()

const emit = defineEmits(['close'])
const urlList = ref<string[]>([])

// 组件挂载时的处理
onMounted(async () => {
    // 处理滚动条和body样式
    originalBodyPadding = document.body.style.paddingRight
    scrollbarWidth = getScrollbarWidth()
    document.body.style.overflow = 'hidden'
    document.body.style.paddingRight = `${scrollbarWidth}px`

    // 显示加载中
    const loading = ElLoading.service({
        fullscreen: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
    });

    try {
        if (props.photoId) {
            // 获取用户角色决定加载哪种质量的图片
            const userRoles = await getUserRoles()
            const canViewFullSize = userRoles.includes('VIEWER')
            
            // 根据权限获取不同质量的图片
            let imageResult
            if (canViewFullSize) {
                imageResult = await getFullPhotos(props.photoId)
            } else {
                imageResult = await get1000KPhotos(props.photoId)
            }
            
            if (imageResult?.url) {
                urlList.value = [imageResult.url]
            } else {
                ElMessage.error('获取照片失败')
                handleClose()
            }
        } else if (props.urlList?.length) {
            // 如果直接提供了URL列表，直接使用
            urlList.value = props.urlList
        } else {
            ElMessage.error('未提供照片信息')
            handleClose()
        }
    } catch (error) {
        console.error('获取照片失败:', error)
        ElMessage.error('获取照片失败')
        handleClose()
    } finally {
        loading.close()
    }
})

// 获取用户角色权限
const getUserRoles = async (): Promise<string[]> => {
    const token = localStorage.getItem('token')
    if (!token) return []
    
    try {
        const userRoleResponse = await getRolesPermissions()
        return userRoleResponse?.roles || []
    } catch (error) {
        console.error('获取用户角色失败:', error)
        return []
    }
}

// 关闭预览
const handleClose = () => {
    emit('close')
}

// 组件卸载时清理资源
onUnmounted(() => {
    // 恢复body样式
    document.body.style.overflow = ''
    document.body.style.paddingRight = originalBodyPadding
    
    // 清理URL资源
    urlList.value.forEach(url => {
        try {
            URL.revokeObjectURL(url)
        } catch (e) {
            // 忽略非ObjectURL
        }
    })
})
</script>

<style scoped>
.showViewer {
    z-index: 9999;
}
</style>

<style>
body {
    transition: padding-right 0.3s ease;
}
</style>