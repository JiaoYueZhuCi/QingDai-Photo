<template>
    <el-backtop :right="50" :bottom="50" />

    <Introduce />

    <el-tabs type="border-card" v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="精选" name="featured"></el-tab-pane>
        <el-tab-pane label="照片" name="photos"></el-tab-pane>
        <el-tab-pane label="时间轴" name="timeline"></el-tab-pane>
        <el-tab-pane label="数据" name="data"></el-tab-pane>
    </el-tabs>

    <router-view v-slot="{ Component }">
        <component :is="Component" @open-preview="openPreview" @open-full-preview="openFullImg"
            :photoType="photoType" />
    </router-view>

    <el-image-viewer :teleported="true" v-if="fullImgShow" :url-list="fullImgList" :initial-index="currentIndex"
        @close="fullImgShow = false" />
</template>

<script setup lang="ts">
import { watch, ref, onMounted } from 'vue';
import type { Ref } from 'vue';
import axios from 'axios'; // 引入 axios
import type { WaterfallItem } from '@/types';
import { ElImageViewer } from 'element-plus';
import type { TabsPaneContext } from 'element-plus';
import Introduce from '@/components/introduce.vue';
import { useRouter, useRoute } from 'vue-router'
import { ElLoading, ElMessage } from 'element-plus'; // 引入 ElLoading

const router = useRouter()
const route = useRoute()
const objectUrl = ref<string>('');
// 当前激活的标签页
const activeTab = ref('featured')

// 同步路由状态
watch(
    () => route.path,
    (newPath) => {
        activeTab.value = newPath.split('/').pop() || 'featured'
    },
    { immediate: true }
)

// 处理标签切换
const handleTabClick = (tab: TabsPaneContext) => {
    router.push(`/home/${tab.paneName}`)
    if (tab.paneName === 'featured') {
        photoType.value = 1
    } else if (tab.paneName === 'photos') {
        photoType.value = 0
    }
}

// 预览弹窗
const previewVisible = ref(false);
const previewImage = ref(0);
///打开关闭图片预览卡片
const handlePreviewClose = () => {
    previewVisible.value = false;
};
const openPreview = (item: WaterfallItem) => {
    // previewImage.value = item.id; // 修改为使用存储路径
    previewVisible.value = true;
};

//// 全屏显示
const fullImgShow = ref(false);
const fullImgList = ref<string[]>([]);
const currentIndex = ref(0);
// 打开全屏
const openFullImg = async (id: string, images: Ref<WaterfallItem[]>) => {
    const loading = ElLoading.service({
        fullscreen: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
    });
    try {
        fullImgList.value = []
        // const response = await axios.get('/api/QingDai/photo/getCompressedPhoto', {
        const response = await axios.get('/api/QingDai/photo/getFullSizePhoto', {
            params: { id, _: new Date().getTime() },
            responseType: 'blob',
        });
        fullImgList.value = [URL.createObjectURL(response.data)];
        fullImgShow.value = true;
    } catch (error) {
        console.error('获取全尺寸照片失败:', error);
        ElMessage.error('获取全尺寸照片失败');
        fullImgList.value = [];
        fullImgShow.value = false;
    } finally {
        loading.close();
    }
    currentIndex.value = 0;

};
//页面打开时禁止滚动
watch(fullImgShow, (newVal: boolean) => {
    if (newVal === true) {
        document.body.classList.add('body-no-scroll');
    } else {
        document.body.classList.remove('body-no-scroll');
    }
});

// 定义 photoType 变量
const photoType = ref(0);

// 初始化时根据当前标签页设置 photoType
watch(activeTab, (newTab) => {
    if (newTab === 'featured') {
        photoType.value = 1
    } else if (newTab === 'photos') {
        photoType.value = 0
    }
}, { immediate: true })
</script>

<style>
.body-no-scroll {
    overflow: hidden;
}

.el-tabs--border-card>.el-tabs__content {
    padding: 0
}

/* .el-tabs__nav-scroll {
    background-color: rgb(250, 250, 250);
}

.el-tabs--border-card>.el-tabs__header .el-tabs__item {
    color: white;
    background-color: rgb(100, 100, 100);
    border-right-color: white;
}

.el-tabs--border-card>.el-tabs__header .el-tabs__item.is-active {
    background-color: black;
    border-left-color: white;
    border-right-color: white;
    color: var(--el-color-primary);
} */
</style>