<template>
    <Header />
    
    <el-backtop :right="20" :bottom="70" />

    <Introduce />

    <ScrollReveal>
        <div class="tabs-container">
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
                <el-tab-pane v-if="hasToken" label="气象" name="meteorology"></el-tab-pane>
                <el-tab-pane v-if="hasToken" label="隐藏" name="hidden"></el-tab-pane>
            </el-tabs>
        </div>
    </ScrollReveal>

    <router-view v-slot="{ Component }">
        <component :is="Component" />
    </router-view>

    <Footer />
</template>

<script setup lang="ts">
import { watch, ref, onMounted, computed } from 'vue';
import type { TabsPaneContext } from 'element-plus';
import Introduce from '@/views/home/introduce/Introduce.vue';
import ScrollReveal from '@/components/util/scroll-reveal/ScrollReveal.vue';
import Footer from '@/components/common/footer/Footer.vue';
import Header from '@/components/common/header/Header.vue';
import { useRouter, useRoute } from 'vue-router'
import { incrementViewCount } from '@/api/views';

const router = useRouter()
const route = useRoute()
// 当前激活的标签页
const activeTab = ref('featured')

// 在组件挂载时增加浏览量
onMounted(async () => {
    try {
        // 只增加浏览量，不显示
        await incrementViewCount();
    } catch (error) {
        console.error('增加浏览量失败:', error);
    }
});

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
}

const hasToken = computed(() => {
    return !!localStorage.getItem('token');
});
</script>

<style>
.el-tabs--border-card>.el-tabs__content {
    padding: 0;
}
</style>

<style scoped>
.tabs-container {
    position: relative;
}

</style>