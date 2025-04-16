<template>
    <el-backtop :right="50" :bottom="50" />

    <Introduce />

    <el-tabs type="border-card" v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="精选" name="featured"></el-tab-pane>
        <el-tab-pane label="照片" name="photos"></el-tab-pane>
        <el-tab-pane label="组图" name="groupPhotos"></el-tab-pane>
        <el-tab-pane label="时间轴" name="timeline"></el-tab-pane>
        <el-tab-pane label="数据" name="data"></el-tab-pane>
        <el-tab-pane label="朝霞" name="sunriseGlow"></el-tab-pane>
        <el-tab-pane label="晚霞" name="sunsetGlow"></el-tab-pane>
        <el-tab-pane label="日出" name="sunrise"></el-tab-pane>
        <el-tab-pane label="日落" name="sunset"></el-tab-pane>

    </el-tabs>

    <router-view v-slot="{ Component }">
        <component :is="Component" :meteorologyType="meteorologyType" />
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

const meteorologyType = ref('')
// 处理标签切换
const handleTabClick = (tab: TabsPaneContext) => {
    router.push(`/home/${tab.paneName}`)
    if (tab.paneName === 'sunriseGlow') {
        meteorologyType.value = '1'
    } else if (tab.paneName === 'sunsetGlow') {
        meteorologyType.value = '2'
    } else if (tab.paneName === 'sunrise') {
        meteorologyType.value = '3'
    } else if (tab.paneName === 'sunset') {
        meteorologyType.value = '4'
    }
}
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