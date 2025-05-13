<template>
  <div class="group-combiner">
    <el-popover placement="top" :width="200" trigger="click" title="添加到组图"
        :popper-style="{ padding: '12px' }" :teleported="true" @show="fetchGroupPhotos">
      <template #reference>
        <slot>
          <!-- 默认触发元素 -->
          <div class="action-icon" @click.stop>
            <el-icon class="icon-color">
              <Collection />
            </el-icon>
          </div>
        </slot>
      </template>
      <div class="group-selection">
        <div v-if="groupPhotos.length === 0" class="no-groups">
          暂无组图，请先创建组图
        </div>
        <div v-else class="group-options">
          <div v-for="group in groupPhotos" :key="group.groupPhoto.id"
              class="group-option" @click="addToGroup(photoId, group)">
            <div class="group-option-left">
              <el-icon v-if="group.photoIds?.includes(photoId)"
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
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElIcon, ElPopover, ElMessage } from 'element-plus';
import { Collection, Check } from '@element-plus/icons-vue';
import { getAllGroupPhotos, updateGroupPhoto } from '@/api/groupPhoto';
import type { GroupPhotoDTO } from '@/types/groupPhoto';

const props = defineProps({
  photoId: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['update']);

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
const addToGroup = async (photoId: string, group: GroupPhotoDTO) => {
  if (!photoId) {
    ElMessage.error('照片ID不能为空');
    return;
  }
  
  try {
    // 检查照片是否已经在组图中
    const isInGroup = group.photoIds?.includes(photoId);

    // 更新组图数据
    const updatedGroup: GroupPhotoDTO = {
      groupPhoto: group.groupPhoto,
      photoIds: isInGroup
          ? group.photoIds.filter(id => id !== photoId) // 如果已在组图中，则移除
          : [...(group.photoIds || []), photoId] // 如果不在组图中，则添加
    };

    await updateGroupPhoto(updatedGroup);
    ElMessage.success(isInGroup ? '从组图中移除照片成功' : '添加照片到组图成功');

    // 更新本地组图数据
    const groupIndex = groupPhotos.value.findIndex(g => g.groupPhoto.id === group.groupPhoto.id);
    if (groupIndex !== -1) {
      groupPhotos.value[groupIndex] = updatedGroup;
    }
    
    // 通知父组件更新完成
    emit('update');
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败');
  }
};
</script>

<style scoped>
.group-combiner {
  display: inline-block;
}

.action-icon {
  cursor: pointer;
  border-radius: 4px;
  padding: 4px;
  transition: all 0.3s;
  color: white;
}

.icon-color {
  color: var(--qd-color-border-light);
}

.action-icon:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

.group-selection {
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

/* 添加夜间模式下组图选项样式 */
.dark .group-option {
  color: var(--qd-color-primary-light-8);
}

.dark .group-option:hover {
  background-color: var(--qd-color-primary-dark-5);
}

.photo-count {
  color: var(--qd-color-text-secondary);
  font-size: 12px;
}

.dark .photo-count {
  color: var(--qd-color-primary-light-6);
}

.no-groups {
  padding: 10px;
  color: var(--qd-color-text-secondary);
  text-align: center;
}

.dark .no-groups {
  color: var(--qd-color-primary-light-6);
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
</style> 