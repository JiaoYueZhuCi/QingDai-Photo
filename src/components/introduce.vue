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
                        Username
                    </div>
                </template>
                kooriookami
            </el-descriptions-item>
            <el-descriptions-item label-class-name="my-label" class-name="my-content">
                <template #label>
                    <div class="cell-item">
                        <el-icon :style="iconStyle">
                            <iphone />
                        </el-icon>
                        Telephone
                    </div>
                </template>
                18100000000
            </el-descriptions-item>
            <el-descriptions-item label-class-name="my-label" class-name="my-content">
                <template #label>
                    <div class="cell-item">
                        <el-icon :style="iconStyle">
                            <location />
                        </el-icon>
                        Place
                    </div>
                </template>
                Suzhou
            </el-descriptions-item>
            <el-descriptions-item label-class-name="my-label" class-name="my-content">
                <template #label>
                    <div class="cell-item">
                        <el-icon :style="iconStyle">
                            <tickets />
                        </el-icon>
                        Remarks
                    </div>
                </template>
                <el-tag size="small">School</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label-class-name="my-label" class-name="my-content">
                <template #label>
                    <div class="cell-item">
                        <el-icon :style="iconStyle">
                            <office-building />
                        </el-icon>
                        Address
                    </div>
                </template>
                No.1188, Wuzhong Avenue, Wuzhong District, Suzhou, Jiangsu Province
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
    OfficeBuilding,
    Tickets,
    User,
} from '@element-plus/icons-vue'


const backgroundImageUrl = '/public/img/introduce/background.jpg'
const avatarImageUrl = '/public/img/introduce/avatar.jpg'

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
}

.description {
    font-size: 18px;
    color: rgb(90, 90, 90);
}
</style>