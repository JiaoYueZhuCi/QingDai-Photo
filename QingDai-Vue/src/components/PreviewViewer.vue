<template>
    <el-image-viewer :teleported="true" :url-list="urlList" :initial-index="initialIndex" :hide-on-click-modal="true"
        @close="handleClose" />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElImageViewer, ElLoading, ElMessage } from 'element-plus'
import { getThumbnail1000KPhoto } from '@/api/photo';

let originalBodyPadding = ''
let scrollbarWidth = 0

const getScrollbarWidth = () => {
    const scrollDiv = document.createElement('div')
    scrollDiv.style.cssText = 'width: 100px;height: 100px;overflow: scroll;position: absolute;top: -9999px;'
    document.body.appendChild(scrollDiv)
    const width = scrollDiv.offsetWidth - scrollDiv.clientWidth
    document.body.removeChild(scrollDiv)
    return width
}

onMounted(async () => {
    originalBodyPadding = document.body.style.paddingRight
    scrollbarWidth = getScrollbarWidth()
    document.body.style.overflow = 'hidden'
    document.body.style.paddingRight = `${scrollbarWidth}px`

    const loading = ElLoading.service({
        fullscreen: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
    });

    if (props.photoId) {
        try {
            const response = await getThumbnail1000KPhoto(props.photoId);
            urlList.value = [URL.createObjectURL(response.data)];
        } catch (error) {
            console.error('获取全尺寸照片失败:', error);
            // 引入 ElMessage
            ElMessage.error('获取全尺寸照片失败');
            handleClose();
        }
    } else if (props.urlList?.length) {
        urlList.value = props.urlList;
    }

    loading.close();

})

onUnmounted(() => {
    document.body.style.overflow = ''
    document.body.style.paddingRight = originalBodyPadding
})

const props = defineProps<{
    photoId?: string;
    initialIndex?: number;
    urlList?: string[];
}>();


const urlList = ref<string[]>([]);

const emit = defineEmits(['close'])

const handleClose = () => {
    emit('close')
}
</script>

<style scoped>
.no-scroll {
    overflow: hidden;
}
</style>

<style>
body {
    transition: padding-right 0.3s ease;
}
</style>