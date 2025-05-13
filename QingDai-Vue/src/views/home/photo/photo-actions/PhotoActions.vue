<template>
    <div class="image-actions">
        <div class="actions-container">
            <PhotoStarSelector :photo="photo" @status-updated="$emit('statusUpdated', $event)" />

            <GroupPhotoCombiner :photoId="photo.id" />

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
import { ElIcon } from 'element-plus';
import { Edit, FullScreen } from '@element-plus/icons-vue';
import type { EnhancedWaterfallItem } from '@/utils/photo';
import PhotoStarSelector from '@/components/photo/photo-star-selector/PhotoStarSelector.vue';
import GroupPhotoCombiner from '@/components/group-photos/group-photo-combiner/GroupPhotoCombiner.vue';

const props = defineProps({
    photo: {
        type: Object as () => EnhancedWaterfallItem,
        required: true
    }
});

const emit = defineEmits(['statusUpdated', 'edit', 'fullscreen']);
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

@media (max-width: 600px) {
    .actions-container {
        gap: 4px;
    }

    .action-icon {
        padding: 3px;
    }
}
</style> 