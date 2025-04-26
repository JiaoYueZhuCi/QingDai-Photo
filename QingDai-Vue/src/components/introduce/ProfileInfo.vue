<template>
    <div class="avatar-section">
        <div>
            <div class="avatar-container">
                <el-avatar :size="avatarSize" :src="avatarImageUrl" class="avatar-static"
                    @click="$router.push('/manage')">
                    <template #default>
                        <el-icon :size="50">
                            <User />
                        </el-icon>
                    </template>
                </el-avatar>
            </div>
        </div>

        <!-- 用户信息 -->
        <div class="profile-info">
            <div class="profile-info-in">
                <div class="username" ref="usernameRef">{{ userInfo.username }}</div>
                <div class="description" ref="descriptionRef">{{ userInfo.description }}</div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { User } from '@element-plus/icons-vue'
import { userInfo } from '@/data/userInfo'
import gsap from 'gsap';
import { useThemeStore } from '@/stores/theme'

defineProps({
    avatarImageUrl: {
        type: String,
        required: true
    },
    avatarSize: {
        type: Number,
        default: 120
    }
})

const themeStore = useThemeStore();

// 计算是否为暗色主题
const isDarkTheme = computed(() => {
    return themeStore.theme === 'dark';
});

// 添加用户名动画
const usernameRef = ref<HTMLElement | null>(null);
const descriptionRef = ref<HTMLElement | null>(null);

onMounted(() => {
    let tl = gsap.timeline();

    if (usernameRef.value) {
        // 先执行进场动画
        tl.from(usernameRef.value, {
            duration: 1,
            y: 50,
            opacity: 0,
            delay: 0.5,
            ease: "power3.out",
        });
        
        // 进场动画完成后添加永久浮动动画
        tl.to(usernameRef.value, {
            duration: 1.5,
            y: -5,
            ease: "sine.inOut",
            repeat: -1, // 无限重复
            yoyo: true, // 来回运动
        });
    }

    if (descriptionRef.value) {
        // 先执行进场动画
        tl.from(descriptionRef.value, {
            duration: 1,
            y: 50,
            opacity: 0,
            ease: "power3.out",
        }, "-=1.5"); // 与前一个动画重叠0.7秒
        
        // 进场动画完成后添加永久浮动动画
        tl.to(descriptionRef.value, {
            duration: 1.5,
            y: -3,
            ease: "sine.inOut",
            repeat: -1, // 无限重复
            yoyo: true, // 来回运动
        });
    }
});
</script>

<style scoped>
.avatar-section {
    position: relative;
    z-index: 10; /* 提高z-index确保头像在暗色模式下不被遮挡 */
}

.avatar-static {
    margin-top: -66px;
    cursor: pointer;
    position: relative;
    z-index: 10;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
}

.avatar-container {
    display: flex;
    justify-content: center;
}

.profile-info {
    color: var(--qd-color-text-primary);
    position: relative;
    z-index: 5;
}

.profile-info-in {
    text-align: center;
}

.username {
    color: var(--qd-color-text-primary);
    font-size: 48px;
    margin: 5px;
}

.description {
    color: var(--qd-color-primary);
    margin-top: 10px;
}

/* 暗色主题特定样式 */
:deep(.dark-theme) .profile-info {
    color: var(--qd-color-text-regular);
}

:deep(.dark-theme) .username {
    color: var(--qd-color-text-regular);
}

:deep(.dark-theme) .description {
    color: var(--qd-color-primary);
}
</style> 