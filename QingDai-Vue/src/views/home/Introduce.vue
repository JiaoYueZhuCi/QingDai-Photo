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

            <!-- 用户信息保持原样 -->
            <div class="profile-info">
                <div class="profile-info-in">
                    <div class="username">皎月祝辞</div>
                    <div class="description">吾生本无乡 心安是归处</div>
                </div>
            </div>
        </div>

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
                    <el-tag size="large" class="link-tag" @click="openLink('https://500px.com.cn/jiaoyuezhuci')">
                        500px
                        <el-icon class="link-icon">
                            <Link />
                        </el-icon>
                    </el-tag>
                    <el-tag size="large" class="link-tag"
                        @click="openLink('https://www.xiaohongshu.com/user/profile/6513978c0000000002013ba8')">
                        小红书
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
                    <el-tag size="large">北京通州</el-tag>
                    <el-tag size="large">天津滨海</el-tag>
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
                    <el-tag size="large">风光</el-tag>
                    <el-tag size="large">人文</el-tag>
                    <el-tag size="large">动植物</el-tag>
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
                    <el-tag size="large">Nikon Z50</el-tag>
                    <el-tag size="large">Nikkor 16mm-50mm f3.5-6.3</el-tag>
                    <el-tag size="large">Nikkor 50mm-250mm f4.5-6.3</el-tag>
                    <el-tag size="large">Nikkor 50mm f1.8</el-tag>
                    <el-tag size="large">DJI Air3S</el-tag>
                    <el-tag size="large">TTArtisan 10mm f2</el-tag>
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
                    <el-tag size="large">L3335308825</el-tag>
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
                    <el-tag size="large" class="link-tag" @click="sendEmail('jiaoyuezhuci@163.com')">
                        3335308825@qq.com
                        <el-icon class="link-icon">
                            <Link />
                        </el-icon>
                    </el-tag>
                </div>
            </el-descriptions-item>
        </el-descriptions>

        <PhotoViewer v-if="previewVisible" :urlList="[previewImageUrl]" @close="previewVisible = false" />
    </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { ElIcon } from 'element-plus';
import {
    Iphone,
    Location,
    Camera,
    Tickets,
    User,
    Link,
    Message,
} from '@element-plus/icons-vue'
import PhotoViewer from '@/components/PhotoViewer.vue'

const backgroundImageUrl = '/img/introduce/background.jpg'
const avatarImageUrl = '/img/introduce/avatar.jpg'

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
}

.username {
    color: var(--qd-color-primary-dark-2);
    font-size: 48px;
    margin: 5px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}
.description {
    color: var(--qd-color-primary);
}
.username:hover {
    transform: scale(1.05);
    text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.5);
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
</style>