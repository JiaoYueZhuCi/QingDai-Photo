<template>
    <div class="container-in" :style="{ padding: `${containerPadding}px` }" v-if="images.length > 0">
        <div class="image-row" v-for="(row, rowIndex) in rows" :key="rowIndex" :style="{
            height: `${row.height}px`, width: `${rowWidth}px`,
            flex: '0 0 auto', 
            margin: useAlternativeStyle ? `0 ${sideMarginStyle} ${sideMarginStyle} ${sideMarginStyle}` : `0 0 ${sideMarginStyle} 0`, 
            gap: `${gap}px`
        }">
            <div 
                class="image-item" 
                v-for="(item, index) in row.items" 
                :key="item.id"
                @click="handleImageClick(item, $event)"
                :class="{ 
                    'selected': isShareMode && selectedPhotos.includes(item),
                    'group-photo-style': useAlternativeStyle
                }"
            >
                <slot name="actions" :item="item"></slot>
                
                <!-- 添加选择框 -->
                <div class="selection-checkbox" v-if="isShareMode">
                    <el-checkbox :model-value="selectedPhotos.includes(item)" @click.stop
                        @change="(val) => togglePhotoSelection(item, Boolean(val))" />
                </div>
                
                <el-image :src="item.compressedSrc" :key="item.id" lazy
                    :style="{ width: item.calcWidth + 'px', height: item.calcHeight + 'px' }">
                    <template #error>
                        <div class="image-slot">
                            <el-icon><icon-picture /></el-icon>
                        </div>
                    </template>
                </el-image>
                
                <div class="image-info">
                    <div>{{ item.title || '标题' }}</div>
                    <div>{{ item.introduce || '文字介绍' }}</div>
                </div>
            </div>
        </div>
    </div>
    <el-empty v-else description="暂无照片数据"></el-empty>
</template>

<script setup lang="ts">
import { ElImage, ElIcon, ElEmpty, ElCheckbox } from 'element-plus';
import { Picture as IconPicture } from '@element-plus/icons-vue';
import { computed, watch } from 'vue';
import type { EnhancedWaterfallItem } from '@/utils/photo';
import { useWaterfallLayout, type WaterfallLayoutOptions, type PhotoItem } from '@/composables/useWaterfallLayout';

// Props 定义
const props = defineProps({
    images: {
        type: Array as () => PhotoItem[],
        required: true
    },
    isShareMode: {
        type: Boolean,
        default: false
    },
    selectedPhotos: {
        type: Array as () => PhotoItem[],
        default: () => []
    },
    // 是否使用组图样式
    useAlternativeStyle: {
        type: Boolean,
        default: false
    },
    // 瀑布流布局选项
    layoutOptions: {
        type: Object as () => WaterfallLayoutOptions,
        default: () => ({})
    }
});

// Emits 定义
const emit = defineEmits(['imageClick', 'togglePhotoSelection']);

// 根据是否使用组图样式设置不同的布局选项
const getLayoutOptions = computed(() => {
    const baseOptions = props.layoutOptions;
    
    if (props.useAlternativeStyle) {
        return {
            ...baseOptions,
            // 组图样式使用略大的间隙
            defaultGap: baseOptions.defaultGap || 10,
            defaultSideMargin: baseOptions.defaultSideMargin || 8,
            mobileGap: baseOptions.mobileGap || 4,
            mobileSideMargin: baseOptions.mobileSideMargin || 4
        };
    }
    
    return baseOptions;
});

// 使用布局计算 hook
const { 
    rows, 
    gap, 
    rowWidth, 
    sideMarginStyle,
    containerPadding, 
    calculateLayout 
} = useWaterfallLayout(getLayoutOptions.value);

// 图片点击处理
const handleImageClick = (item: PhotoItem, event: MouseEvent) => {
    if (props.isShareMode) {
        event.stopPropagation();
        emit('togglePhotoSelection', item, !props.selectedPhotos.includes(item));
    } else {
        emit('imageClick', item);
    }
};

// 切换照片选择状态
const togglePhotoSelection = (photo: PhotoItem, selected: boolean) => {
    emit('togglePhotoSelection', photo, selected);
};

// 监听图片数据变化，重新计算布局
watch(() => props.images, (newImages) => {
    if (newImages.length > 0) {
        calculateLayout(newImages);
    }
}, { deep: true, immediate: true });
</script>

<style scoped>
.container-in {}

.image-row {
    width: 100%;
    display: flex;
    justify-content: space-between;
}

.image-item {
    position: relative;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;
}

/* 添加选中照片的边框样式 */
.image-item.selected {
    border: 3px solid var(--qd-color-primary);
    box-shadow: 0 0 12px var(--qd-color-primary);
}

/* 夜间模式下的选中边框样式 */
.dark .image-item.selected {
    border: 3px solid var(--qd-color-primary-light-4);
    box-shadow: 0 0 12px var(--qd-color-primary-light-2);
}

.image-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
    z-index: 1;
}

/* 组图样式 */
.image-item.group-photo-style {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    background: var(--qd-color-bg-light);
}

.image-item.group-photo-style:hover {
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.image-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    color: white;
    font-size: 14px;
    opacity: 0;
    transition: opacity 0.3s;
}

.image-item:hover .image-info {
    opacity: 1;
}

/* el-image内部img配置适应容器 */
:deep(.el-image__inner) {
    height: 100% !important;
}

.image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
    font-size: 30px;
}

.image-slot .el-icon {
    font-size: 30px;
}

.selection-checkbox {
    position: absolute;
    top: 0px;
    right: 0px;
    z-index: 3;
    border-radius: 4px;
    padding: 8px;
    cursor: pointer;
    transition: all 0.3s;
}

.selection-checkbox:hover {
    background-color: rgba(0, 0, 0, 0.7);
    transform: scale(1.1);
}

.selection-checkbox :deep(.el-checkbox) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
}

.selection-checkbox :deep(.el-checkbox__input) {
    width: 24px;
    height: 24px;
}

.selection-checkbox :deep(.el-checkbox__inner) {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    background-color: transparent;
    border-color: white;
}

.selection-checkbox :deep(.el-checkbox__inner::after) {
    height: 12px;
    left: 7px;
    top: 3px;
    width: 6px;
    border-color: var(--qd-color-primary);
}

.selection-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: var(--qd-color-border-light);
    border-color: var(--qd-color-border-light);
}

@media (max-width: 600px) {
    .container-in {
        width: calc(100vw - 2px);
    }
}
</style> 