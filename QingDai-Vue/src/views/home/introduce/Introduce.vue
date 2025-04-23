<template>
    <div>
        <!-- 背景图静态展示 -->
        <div class="cover-container" :style="{ backgroundImage: `url(${backgroundImageUrl})` }"
            @click="openPreview(backgroundImageUrl)">
        </div>

        <!-- 头像静态展示 -->
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
                    <el-button class="logout" v-if="hasToken" type="danger" @click="handleLogout"
                        style="margin-top: 10px;">
                        注销
                    </el-button>
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

        <ScrollReveal>
            <el-descriptions class="margin-top" :column="columnCount" border>
                <el-descriptions-item label-class-name="my-label" class-name="my-content">
                    <template #label>
                        <div class="cell-item">
                            <el-icon :style="iconStyle">
                                <user />
                            </el-icon>
                            同名账号
                        </div>
                    </template>
                    <div class="device">
                        <el-tag v-for="account in userInfo.accounts" :key="account.name" size="large" class="link-tag"
                            @click="openLink(account.url)">
                            {{ account.name }}
                            <el-icon class="link-icon">
                                <Link />
                            </el-icon>
                        </el-tag>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label-class-name="my-label" class-name="my-content">
                    <template #label>
                        <div class="cell-item">
                            <el-icon :style="iconStyle">
                                <location />
                            </el-icon>
                            地点
                        </div>
                    </template>
                    <div class="device">
                        <el-tag v-for="location in userInfo.locations" :key="location" size="large">{{ location }}</el-tag>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label-class-name="my-label" class-name="my-content">
                    <template #label>
                        <div class="cell-item">
                            <el-icon :style="iconStyle">
                                <tickets />
                            </el-icon>
                            领域
                        </div>
                    </template>
                    <div class="device">
                        <el-tag v-for="domain in userInfo.domains" :key="domain" size="large">{{ domain }}</el-tag>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label-class-name="my-label" class-name="my-content">
                    <template #label>
                        <div class="cell-item">
                            <el-icon :style="iconStyle">
                                <Camera />
                            </el-icon>
                            设备
                        </div>
                    </template>
                    <div class="device">
                        <el-tag v-for="device in userInfo.devices" :key="device" size="large">{{ device }}</el-tag>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label-class-name="my-label" class-name="my-content">
                    <template #label>
                        <div class="cell-item">
                            <el-icon :style="iconStyle">
                                <iphone />
                            </el-icon>
                            微信
                        </div>
                    </template>
                    <div class="device">
                        <el-tag size="large" class="link-tag" @click="copyText(userInfo.wechat)">
                            {{ userInfo.wechat }}
                            <el-icon class="link-icon">
                                <CopyDocument />
                            </el-icon>
                        </el-tag>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label-class-name="my-label" class-name="my-content">
                    <template #label>
                        <div class="cell-item">
                            <el-icon :style="iconStyle">
                                <Message />
                            </el-icon>
                            邮箱
                        </div>
                    </template>
                    <div class="device">
                        <el-tag size="large" class="link-tag" @click="sendEmail(userInfo.email)">
                            {{ userInfo.email }}
                            <el-icon class="link-icon">
                                <Link />
                            </el-icon>
                        </el-tag>
                    </div>
                </el-descriptions-item>
            </el-descriptions>
        </ScrollReveal>

        <PhotoViewer v-model="previewVisible" :urlList="[previewImageUrl]" @close="previewVisible = false" />
    </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { ElIcon, ElMessage } from 'element-plus';
import {
    Iphone,
    Location,
    Camera,
    Tickets,
    User,
    Link,
    Message,
    CopyDocument
} from '@element-plus/icons-vue'
import PhotoViewer from '@/components/photo/PhotoViewer.vue'
import ScrollReveal from '@/components/util/ScrollReveal.vue'
import { userInfo } from '@/data/userInfo'
import { homeImages } from '@/data/imageUrls'
import gsap from 'gsap';

const { backgroundImageUrl, avatarImageUrl } = homeImages;

const iconStyle = computed(() => {
    const marginMap = {
        large: '8px',
        default: '6px',
        small: '4px',
    }
    return {
        marginRight: marginMap.large,
    }
})

const avatarSize = 120

// 添加计算属性来动态设置列数
const columnCount = computed(() => {
    if (window.innerWidth < 600) {
        return 1; // 手机屏幕显示1列
    } else if (window.innerWidth < 900) {
        return 2; // 平板屏幕显示2列
    } else {
        return 3; // 大屏幕显示3列
    }
})
const manageOpacity = computed(() => {
    return scrollY > 650 ? 0.6 : 1
})

const hasToken = computed(() => {
    return !!localStorage.getItem('token');
});
const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
};

// 添加预览相关变量
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 添加打开预览的方法
const openPreview = (url: string) => {
    previewImageUrl.value = url
    previewVisible.value = true
}

// 添加打开链接的方法
const openLink = (url: string) => {
    window.open(url, '_blank');
}

// 添加发送邮件的方法
const sendEmail = (email: string) => {
    window.location.href = `mailto:${email}`;
}

// 添加复制文本的方法
const copyText = (text: string) => {
    navigator.clipboard.writeText(text).then(() => {
        ElMessage.success('微信号复制成功');
    }).catch(() => {
        ElMessage.error('复制失败，请手动复制');
    });
}

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
.el-descriptions {
    margin-bottom: 5px !important;
}
.cover-container {
    height: 400px;
    background-size: cover;
    background-position: 50% 50%;
    background-repeat: no-repeat;
    cursor: pointer;
    /* 添加鼠标指针样式 */
}

.avatar-static {
    margin-top: -66px;
    cursor: pointer;
    /* 添加鼠标指针样式 */
}

.avatar-container {
    display: flex;
    justify-content: center;
}

.profile-info {
    color: black;
}

.profile-info-in {
    text-align: center;
}

.margin-top {
    margin: 10px auto;
    color: white;
    transform-origin: center;
}

.username {
    color: var(--qd-color-primary-dark-2);
    font-size: 48px;
    margin: 5px;
}

.description {
    color: var(--qd-color-primary);
    margin-top: 10px;
}

/* 添加媒体查询来调整列数 */
@media (max-width: 600px) {
    .margin-top {
        width: 100%;
    }
}

@media (min-width: 601px) and (max-width: 900px) {
    .margin-top {
        width: 100%;
    }
}

@media (min-width: 901px) {
    .margin-top {
        width: 100%;
    }
}


.device {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    margin-bottom: -5px;
}

.el-tag {
    margin-right: 5px;
    margin-bottom: 5px;
}

.manage {
    position: absolute;
    right: 10px;
    top: 410px;
    opacity: v-bind(manageOpacity);
    z-index: 9999;
    transition: opacity 0.8s ease;
}

@media (max-width: 600px) {
    .device {
        gap: 3px;
    }

    .el-tag {
        margin-right: 3px;
        margin-bottom: 3px;
    }
}

.logout {
    position: absolute;
    right: 10px;
}

.link-tag {
    cursor: pointer;
    transition: transform 0.2s ease;
    display: inline-flex;
    align-items: center;
}

.link-tag:hover {
    transform: scale(1.05);
}

.link-icon {
    margin-left: 4px;
    font-size: 14px;
}

.descriptions-wrapper {
    overflow: hidden;
    transform-origin: center;
    position: relative;
    backface-visibility: hidden;
    -webkit-backface-visibility: hidden;
    transform: translateZ(0);
    -webkit-transform: translateZ(0);
    visibility: hidden; /* 初始状态不可见 */
}

.descriptions-wrapper.visible {
    visibility: visible; /* 动画触发后可见 */
}

.descriptions-wrapper::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--el-border-color);
    transform: scaleX(0);
    transform-origin: center;
    transition: transform 0.3s ease;
    will-change: transform;
}

.descriptions-wrapper:hover::before {
    transform: scaleX(1);
}
</style>