<template>
  <div class="star-selector">
    <el-popover placement="top" :width="200" trigger="click" title="设置星标状态"
        :popper-style="{ padding: '12px' }" :teleported="true">
      <template #reference>
        <div class="action-icon" @click.stop>
          <el-icon :color="getStarColor(photo.startRating)">
            <Star v-if="photo.startRating === PhotoStarRating.HIDDEN" />
            <StarFilled v-else />
          </el-icon>
        </div>
      </template>
      <div class="star-selection">
        <div class="star-option" @click="updateStarStatus(photo, PhotoStarRating.STAR)">
          <el-icon :color="getStarColor(PhotoStarRating.STAR)">
            <StarFilled />
          </el-icon>
          <span>{{ getStarLabel(PhotoStarRating.STAR) }}</span>
        </div>
        <div class="star-option" @click="updateStarStatus(photo, PhotoStarRating.NORMAL)">
          <el-icon :color="getStarColor(PhotoStarRating.NORMAL)">
            <StarFilled />
          </el-icon>
          <span>{{ getStarLabel(PhotoStarRating.NORMAL) }}</span>
        </div>
        <div class="star-option" @click="updateStarStatus(photo, PhotoStarRating.METEOROLOGY)">
          <el-icon :color="getStarColor(PhotoStarRating.METEOROLOGY)">
            <StarFilled />
          </el-icon>
          <span>{{ getStarLabel(PhotoStarRating.METEOROLOGY) }}</span>
        </div>
        <div class="star-option" @click="updateStarStatus(photo, PhotoStarRating.GROUP_ONLY)">
          <el-icon :color="getStarColor(PhotoStarRating.GROUP_ONLY)">
            <StarFilled />
          </el-icon>
          <span>{{ getStarLabel(PhotoStarRating.GROUP_ONLY) }}</span>
        </div>
        <div class="star-option" @click="updateStarStatus(photo, PhotoStarRating.HIDDEN)">
          <el-icon :color="getStarColor(PhotoStarRating.HIDDEN)">
            <Star />
          </el-icon>
          <span>{{ getStarLabel(PhotoStarRating.HIDDEN) }}</span>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ElIcon, ElPopover, ElMessage } from 'element-plus';
import { Star, StarFilled } from '@element-plus/icons-vue';
import { updatePhotoStartStatus as updatePhotoStart } from '@/api/photo';
import { PhotoStarRating, getStarColor, getStarLabel } from '@/config/photo';
import type { EnhancedWaterfallItem } from '@/utils/photo';

const props = defineProps({
  photo: {
    type: Object as () => EnhancedWaterfallItem,
    required: true
  }
});

const emit = defineEmits(['statusUpdated']);

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
</script>

<style scoped>
.star-selector {
  display: inline-block;
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
</style> 