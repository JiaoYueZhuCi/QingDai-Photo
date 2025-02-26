<template>
    <div class="container" ref="container">
        <div class="image-row" v-for="(row, rowIndex) in rows" :key="rowIndex">
            <div class="image-item" v-for="(item, index) in row" :key="item.id" @click="openFatherFullImg(item)"
                :style="{ width: item.calcWidth + 'px', height: rowHeight + 'px' }">
                <!-- :fit="'contain'" -->
                <el-image :src="item.thumbnail" :key="item.id" lazy v-lazy-style
                    :style="{ width: item.calcWidth + 'px', height: rowHeight + 'px' }">
                    <template #error>
                        <!-- <div class="image-slot" :style="{ width: rowHeight + 'px', height: rowHeight + 'px' }"> -->
                        <div class="error-image" :style="{ width: rowHeight + 'px', height: rowHeight + 'px' }">
                            <el-icon><icon-picture /></el-icon>
                        </div>
                    </template>
                </el-image>
                <div class="image-info">
                    <div>{{ item.title }}</div>
                    <div>{{ item.author || '摄影师' }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, onBeforeMount } from 'vue';
import type { WaterfallItem } from '../types';
import { ElImage, ElIcon } from 'element-plus';
import { Picture as IconPicture } from '@element-plus/icons-vue'

// 定义 Props
const props = defineProps({
    images: {
        type: Array as PropType<WaterfallItem[]>,
        required: true,
    },
    rowHeight: {
        type: Number,
        default: 300,
    },
});

// 定义 Emits
const emit = defineEmits<{
    (e: 'open-preview', item: WaterfallItem): void
    (e: 'open-full-preview', item: WaterfallItem): void
}>();

// 打开预览方法
const openFatherPreview = (item: WaterfallItem) => {
    console.log('请求预览打开图片地址为:', item.fullSize);
    emit('open-preview', item); // 触发事件
};

// 打开全屏
const openFatherFullImg = (item: WaterfallItem) => {
    console.log('请求全屏打开图片地址为:', item.fullSize);
    emit('open-full-preview', item); // 触发事件
};



////计算图片宽度
const container = ref<HTMLElement | null>(null);
const rows = ref<any[]>([]);

const calculateLayout = () => {
    if (!container.value) return;

    // const containerWidth = container.value.offsetWidth;
    const containerWidth = window.innerWidth - 20;   //总宽度减去瀑布流两侧20px

    const gap = 20; // 图片间隙

    rows.value = []; // 清空之前的数据
    let currentRow: any[] = [];
    let currentRowWidth = 0;

    props.images.forEach((img, index) => {
        const aspectRatio = img.width / img.height;
        const itemWidth = props.rowHeight * aspectRatio; // 基础宽度（未缩放）

        // 计算加入当前行后的总宽度（包括间隙）
        const newWidth = currentRowWidth + itemWidth + (currentRow.length > 0 ? gap : 0);

        // 若超过容器宽度且当前行非空，则换行
        if (newWidth > containerWidth && currentRow.length > 0) {
            rows.value.push(currentRow);
            currentRow = [img];
            currentRowWidth = itemWidth;
        } else {
            currentRow.push(img);
            currentRowWidth += itemWidth + (currentRow.length > 1 ? gap : 0);
        }

        // 处理最后一张图片
        if (index === props.images.length - 1) {
            rows.value.push(currentRow);
        }
    });

    // 调整每行图片宽度以适应容器
    rows.value.forEach(row => {
        const totalItemsWidth = row.reduce((sum, item) => {
            return sum + (props.rowHeight * (item.width / item.height));
        }, 0);
        const totalGap = (row.length - 1) * gap;
        const targetWidth = containerWidth - totalGap;
        const scale = targetWidth / totalItemsWidth;

        row.forEach((item: any) => {
            item.calcWidth = (props.rowHeight * (item.width / item.height)) * scale;
        });
    });

};

// 监听图片数组的变化并重新计算布局
watch(() => props.images, calculateLayout, { immediate: true });


// // 组件挂载时计算一次布局，并监听窗口resize事件
var image = ref({})
onMounted(() => {
    calculateLayout();

    window.addEventListener('resize', calculateLayout);  // 监听resize事件
});



// 新增自定义指令
const vLazyStyle = {
    mounted(el: HTMLElement) {
        const img = el.querySelector('.el-image__inner') as HTMLImageElement
        if (img) {
            img.style.width = '100%'
            img.style.height = '100%'
            img.style.objectFit = 'cover'
        }
    },
    updated(el: HTMLElement) {
        // 确保动态更新后重新应用样式
        const img = el.querySelector('.el-image__inner') as HTMLImageElement
        img && (img.style.objectFit = 'cover')
    }
}
</script>


<style scoped>
.container {
    /* max-width: 1280px; */
    width: 100%;
    margin: 20px auto;
    padding: 0 10px;
}

.image-row {
    display: flex;
    margin-bottom: 10px;
    gap: 10px;
}

.image-item {
    position: relative;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    background: #fff;
    transition: transform 0.3s;
    cursor: pointer;
}

.image-item:hover {
    transform: translateY(-5px);
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
    /* width: 100% !important; */
    height: 100% !important;
    /* object-fit: cover !important; */
}

</style>