<template>
    <div class="image-actions">
        <div class="actions-container">
            <!-- 星标状态 -->
            <el-popover placement="top" :width="200" trigger="click" title="设置星标状态"
                :popper-style="{ padding: '12px' }" :teleported="true">
                <template #reference>
                    <div class="action-icon" @click.stop>
                        <el-icon :color="getStarColor(photo.startRating)">
                            <Star v-if="photo.startRating === -1" />
                            <StarFilled v-else />
                        </el-icon>
                    </div>
                </template>
                <div class="star-selection">
                    <div class="star-option" @click="updateStarStatus(photo, 1)">
                        <el-icon :color="getStarColor(1)">
                            <StarFilled />
                        </el-icon>
                        <span>星标照片</span>
                    </div>
                    <div class="star-option" @click="updateStarStatus(photo, 0)">
                        <el-icon :color="getStarColor(0)">
                            <StarFilled />
                        </el-icon>
                        <span>普通照片</span>
                    </div>
                    <div class="star-option" @click="updateStarStatus(photo, 2)">
                        <el-icon :color="getStarColor(2)">
                            <StarFilled />
                        </el-icon>
                        <span>气象照片</span>
                    </div>
                    <div class="star-option" @click="updateStarStatus(photo, -1)">
                        <el-icon :color="getStarColor(-1)">
                            <Star />
                        </el-icon>
                        <span>隐藏照片</span>
                    </div>
                </div>
            </el-popover>

            <!-- 添加到组图 -->
            <div class="group-icon" @click.stop>
                <el-popover placement="top" :width="200" trigger="click" title="添加到组图"
                    :popper-style="{ padding: '12px' }" :teleported="true" @show="fetchGroupPhotos">
                    <template #reference>
                        <div class="action-icon">
                            <el-icon class="icon-color">
                                <Collection />
                            </el-icon>
                        </div>
                    </template>
                    <div class="group-selection">
                        <div v-if="groupPhotos.length === 0" class="no-groups">
                            暂无组图，请先创建组图
                        </div>
                        <div v-else class="group-options">
                            <div v-for="group in groupPhotos" :key="group.groupPhoto.id"
                                class="group-option" @click="addToGroup(photo, group)">
                                <div class="group-option-left">
                                    <el-icon v-if="group.photoIds?.includes(photo.id)"
                                        class="check-icon">
                                        <Check />
                                    </el-icon>
                                    <span>{{ group.groupPhoto.title || '未命名组图' }}</span>
                                </div>
                                <span class="photo-count">({{ group.photoIds?.length || 0 }}张)</span>
                            </div>
                        </div>
                    </div>
                </el-popover>
            </div>

            <!-- 编辑按钮 -->
            <div class="action-icon" @click.stop="$emit('edit', photo)">
                <el-icon class="icon-color">
                    <Edit />
                </el-icon>
            </div>

            <!-- 全屏按钮 -->
            <div class="action-icon fullscreen-icon" @click.stop="$emit('fullscreen', photo.id)">
                <el-icon class="icon-color">
                    <FullScreen />
                </el-icon>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElIcon, ElPopover, ElMessage } from 'element-plus';
import { Star, StarFilled, Collection, Check, Edit, FullScreen } from '@element-plus/icons-vue';
import { updatePhotoStartStatus as updatePhotoStart } from '@/api/photo';
import { getAllGroupPhotos, updateGroupPhoto } from '@/api/groupPhoto';
import type { EnhancedWaterfallItem } from '@/utils/photo';
import type { GroupPhotoDTO } from '@/types/groupPhoto';

const props = defineProps({
    photo: {
        type: Object as () => EnhancedWaterfallItem,
        required: true
    }
});

const emit = defineEmits(['statusUpdated', 'edit', 'fullscreen']);

// 获取星星颜色
const getStarColor = (startVal: number) => {
    if (startVal === 1) return 'gold'; // 星标
    if (startVal === -1) return 'var(--qd-color-primary-light-7)'; // 隐藏
    if (startVal === 0) return 'var(--qd-color-primary-light-7)'; // 普通
    if (startVal === 2) return 'darkturquoise'; // 气象
    return 'var(--qd-color-primary-light-7)'; // 默认
};

// 更新星标状态
const updateStarStatus = async (item: EnhancedWaterfallItem, startVal: number) => {
    try {
        await updatePhotoStart({
            id: item.id,
            startRating: startVal
        });
        // 创建一个新对象以便通知父组件状态已更新
        const updatedPhoto = { ...item, startRating: startVal };
        emit('statusUpdated', updatedPhoto);
        ElMessage.success('更新状态成功');
    } catch (error) {
        console.error('更新状态失败:', error);
        ElMessage.error('更新状态失败');
    }
};

// 组图相关
const groupPhotos = ref<GroupPhotoDTO[]>([]);

// 获取所有组图
const fetchGroupPhotos = async () => {
    try {
        const response = await getAllGroupPhotos();
        groupPhotos.value = response;
    } catch (error) {
        console.error('获取组图列表失败:', error);
        ElMessage.error('获取组图列表失败');
    }
};

// 添加到组图或从组图中移除
const addToGroup = async (photo: EnhancedWaterfallItem, group: GroupPhotoDTO) => {
    try {
        // 检查照片是否已经在组图中
        const isInGroup = group.photoIds?.includes(photo.id);

        // 更新组图数据
        const updatedGroup: GroupPhotoDTO = {
            groupPhoto: group.groupPhoto,
            photoIds: isInGroup
                ? group.photoIds.filter(id => id !== photo.id) // 如果已在组图中，则移除
                : [...(group.photoIds || []), photo.id] // 如果不在组图中，则添加
        };

        await updateGroupPhoto(updatedGroup);
        ElMessage.success(isInGroup ? '从组图中移除照片成功' : '添加照片到组图成功');

        // 更新本地组图数据
        const groupIndex = groupPhotos.value.findIndex(g => g.groupPhoto.id === group.groupPhoto.id);
        if (groupIndex !== -1) {
            groupPhotos.value[groupIndex] = updatedGroup;
        }
    } catch (error) {
        console.error('操作失败:', error);
        ElMessage.error('操作失败');
    }
};
</script>

<style scoped>
.icon-color {
    color: var(--qd-color-border-light);
}

.image-actions {
    position: absolute;
    top: 8px;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
    padding: 0 8px;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.3s;
}

.image-item:hover .image-actions {
    opacity: 1;
}

.actions-container {
    display: flex;
    flex-wrap: wrap;
    justify-content: left;
    gap: 8px;
    width: 100%;
}

.action-icon {
    cursor: pointer;
    border-radius: 4px;
    padding: 4px;
    transition: all 0.3s;
    color: white;
}

.action-icon:hover {
    background: rgba(0, 0, 0, 0.7);
    transform: scale(1.1);
}

.star-selection {
    padding: 0;
}

/* 添加夜间模式下的popover样式 */
.dark :deep(.el-popover) {
    background-color: var(--qd-color-primary-dark-8);
    border: 1px solid var(--qd-color-primary-dark-6);
}

.dark :deep(.el-popover__title) {
    color: var(--qd-color-primary-light-9);
}

.star-option {
    padding: 10px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 8px;
    transition: background-color 0.3s;
    display: flex;
    align-items: center;
    gap: 8px;
}

/* 添加夜间模式下的选项样式 */
.dark .star-option {
    color: var(--qd-color-primary-light-8);
}

.star-option:hover {
    background-color: var(--qd-color-hover);
}

/* 添加夜间模式下选项悬停样式 */
.dark .star-option:hover {
    background-color: var(--qd-color-primary-dark-5);
}

.group-selection {
    padding: 0;
}

/* 添加夜间模式下组图选项样式 */
.dark .group-option {
    color: var(--qd-color-primary-light-8);
}

.dark .group-option:hover {
    background-color: var(--qd-color-primary-dark-5);
}

.dark .photo-count {
    color: var(--qd-color-primary-light-6);
}

.dark .no-groups {
    color: var(--qd-color-primary-light-6);
}

.group-options {
    max-height: 300px;
    overflow-y: auto;
}

.group-option {
    padding: 10px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 8px;
    transition: background-color 0.3s;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.group-option:hover {
    background-color: var(--qd-color-hover);
}

.photo-count {
    color: var(--qd-color-text-secondary);
    font-size: 12px;
}

.no-groups {
    padding: 10px;
    color: var(--qd-color-text-secondary);
    text-align: center;
}

.group-option-left {
    display: flex;
    align-items: center;
    gap: 8px;
}

.check-icon {
    color: #67c23a;
    font-size: 16px;
}

@media (max-width: 600px) {
    .actions-container {
        gap: 4px;
    }

    .action-icon {
        padding: 3px;
    }
}
</style> 