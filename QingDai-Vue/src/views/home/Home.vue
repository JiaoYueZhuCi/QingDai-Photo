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
        <component :is="Component" 
            :photoType="photoType" />
    </router-view>

</template>

<script setup lang="ts">
import { watch, ref, onMounted } from 'vue';
import type { TabsPaneContext } from 'element-plus';
import Introduce from '@/views/home/Introduce.vue';
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
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

// 照片类型：0-所有照片，1-星标照片
const photoType = ref(0)

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