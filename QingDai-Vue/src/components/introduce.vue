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

        <el-descriptions class="margin-top" :column="3" border>
            <el-descriptions-item label-class-name="my-label" class-name="my-content">
                <template #label>
                    <div class="cell-item">
                        <el-icon :style="iconStyle">
                            <user />
                        </el-icon>
                        各平台同名账号
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
                北京通州,天津滨海
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
                风光,人文,动植物
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
                    <el-tag size="small">Nikon Z50</el-tag>
                    <el-tag size="small">Nikkor 16mm-50mm f3.5-6.3</el-tag>
                    <el-tag size="small">Nikkor 50mm-250mm f4.5-6.3</el-tag>
                    <el-tag size="small">Nikkor 50mm f1.8</el-tag>
                    <el-tag size="small">DJI Air3S</el-tag>
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
.device{
    display: flex;
    gap: 5px;
}
</style>