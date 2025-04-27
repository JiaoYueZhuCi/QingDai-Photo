<template>
    <div :class="['introduce-container', { 'dark-theme': isDarkTheme }]">
        <!-- 背景图静态展示 -->
        <div class="cover-container" :style="{ backgroundImage: `url(${backgroundImageUrl})` }"
            @click="openPreview(backgroundImageUrl)">
        </div>

        <!-- 头像和用户信息部分 -->
        <ProfileInfo :avatar-image-url="avatarImageUrl" :avatar-size="avatarSize" />

        <!-- 用户详情描述部分 -->
        <UserDescription />

        <PhotoViewer v-model="previewVisible" :urlList="[previewImageUrl]" @close="previewVisible = false" />
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PhotoViewer from '@/components/photo/photo-viewer/PhotoViewer.vue'
import ProfileInfo from '@/views/home/introduce/profile-info/ProfileInfo.vue'
import UserDescription from '@/views/home/introduce/user-description/UserDescription.vue'
import { homeImages } from '@/data/imageUrls'
import { useThemeStore } from '@/stores/theme'

const { backgroundImageUrl, avatarImageUrl } = homeImages;
const themeStore = useThemeStore();
const avatarSize = 120;

// 计算是否为暗色主题
const isDarkTheme = computed(() => {
    return themeStore.theme === 'dark';
});

// 添加预览相关变量
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 添加打开预览的方法
const openPreview = (url: string) => {
    previewImageUrl.value = url
    previewVisible.value = true
}
</script>

<style scoped>
.introduce-container {
    background-color: transparent;
    transition: background-color 0.3s ease;
}

.dark-theme {
    background-color: var(--qd-color-bg);
}

.cover-container {
    height: 400px;
    background-size: cover;
    background-position: 50% 50%;
    background-repeat: no-repeat;
    cursor: pointer;
    position: relative;
}

/* 暗色主题特定样式 */
.dark-theme .cover-container {
    filter: brightness(0.8);
}
</style> 