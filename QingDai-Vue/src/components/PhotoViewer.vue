<template>
    <div class="showViewer">
        <el-image-viewer v-if="urlList.length > 0" :url-list="urlList" :initial-index="initialIndex" :hide-on-click-modal="true"
            @close="handleClose" ref="imageViewerRef" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
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
    photoId?: string;
    initialIndex?: number;
    urlList?: string[];
}>()

const emit = defineEmits(['close'])
const urlList = ref<string[]>([])
const imageViewerRef = ref(null)


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
        if(props.urlList?.length){
            urlList.value = props.urlList
        }else if (props.photoId) {
            // 获取用户角色决定加载哪种质量的图片
            const userRoles = await getUserRoles()
            const hasViewPermission = await canViewFullSizePhoto()
            console.log('用户角色:', userRoles, '是否可查看全尺寸:', hasViewPermission)
            
            // 根据权限获取不同质量的图片
            let imageResult
            if (hasViewPermission) {
                imageResult = await getFullPhoto(props.photoId)
            } else {
                imageResult = await get1000KPhoto(props.photoId)
            }
            
            if (imageResult?.url) {
                    urlList.value = [imageResult.url]
            } else if(imageResult?.blob){
                try {
                const blobUrl = URL.createObjectURL(imageResult.blob)
                    urlList.value = [blobUrl]
                    console.log('创建的blob URL:', blobUrl)
                } catch (error) {
                    console.error('创建图片URL失败:', error)
                    ElMessage.error('图片加载失败')
                    handleClose()
                }
            }else {
                console.error('获取照片失败, 返回数据:', imageResult)
                ElMessage.error('获取照片失败')
                handleClose()
            }

        }  else {
            ElMessage.error('未提供照片信息')
            handleClose()
        }
    } catch (error) {
        console.error('获取照片失败:', error)
        if (error instanceof Error) {
            console.error('错误详情:', error.message, error.stack)
        }
        ElMessage.error(`获取照片失败: ${error instanceof Error ? error.message : '未知错误'}`)
        handleClose()
    } finally {
        loading.close()
    }
})

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

/* 修复Element Plus图片查看器的显示问题 */
.el-image-viewer__img {
    display: block !important;
}
</style> 