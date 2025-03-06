<template>
    <div>
        <!-- 背景图静态展示 -->
        <div class="cover-container" :style="{ backgroundImage: `url(${backgroundImageUrl})` }">
        </div>

        <!-- 头像静态展示 -->
        <div class="avatar-section">
            <div class="avatar-container">
                <el-avatar :size="avatarSize" :src="avatarImageUrl" class="avatar-static">
                    <template #default>
                        <el-icon :size="50">
                            <User />
                        </el-icon>
                    </template>
                </el-avatar>
            </div>
            <!-- 用户信息保持原样 -->
            <div class="profile-info">
                <div class="profile-info-in">
                    <div class="username">皎月祝辞</div>
                    <!-- <video src="/public/img/introduce/name.mp4" style="width: 200px;"/> -->
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
                500px,小红书
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
                L3335308825
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
                <div class="device" >
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
                <div class="device" >
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
                <div class="device" >
                    <el-tag size="large">Nikon Z50</el-tag>
                    <el-tag size="large">Nikkor 16mm-50mm f3.5-6.3</el-tag>
                    <el-tag size="large">Nikkor 50mm-250mm f4.5-6.3</el-tag>
                    <el-tag size="large">Nikkor 50mm f1.8</el-tag>
                    <el-tag size="large">DJI Air3S</el-tag>
                </div>


            </el-descriptions-item>
        </el-descriptions>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElIcon } from 'element-plus';
import {
    Iphone,
    Location,
    Camera,
    Tickets,
    User,
} from '@element-plus/icons-vue'


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
</script>


<style scoped>
p {
    color: white;
}

.cover-container {
    height: 400px;
    background-size: cover;
    background-position: 50% 50%;
    background-repeat: no-repeat;
}


.avatar-static {
    margin-top: -66px;

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

:deep(.my-label) {
    background: rgb(230, 230, 230) !important;
}

:deep(.my-content) {
    background: rgb(255, 255, 255);
}

.username {
    font-size: 40px;
    margin: 10px;
    color: rgb(239, 243, 0);
}
.el-tag{
    margin: 5px;
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

.el-descriptions__body .el-descriptions__table.is-bordered .el-descriptions__cell{
    padding: 1px 1px;
}
</style>