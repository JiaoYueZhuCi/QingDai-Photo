<template>
    <div class="container" ref="containerRef">
        <!-- 瀑布流组件 -->
        <PhotoWaterfall
            :images="images"
            :isShareMode="false"
            :selectedPhotos="[]"
            :useAlternativeStyle="true"
            :layoutOptions="layoutOptions"
            @imageClick="openGroupPhotoPreview"
        >
            <!-- 不需要提供操作按钮，所以actions插槽为空 -->
        </PhotoWaterfall>

        <!-- 组图预览组件 -->
        <GroupFilmPreview
            v-model="groupFilmPreviewVisable"
            :group-id="selectedGroupId || ''"
            :initial-photo-id="selectedPhotoId || undefined"
            @close="closeGroupPhotoPreview"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import type { WaterfallItem } from '@/types';
import { ElMessage } from 'element-plus';
import { getPhotosByIds } from '@/api/photo';
import { getAllGroupPhotos } from '@/api/groupPhoto';
import { debounce } from 'lodash';
import type { GroupPhotoDTO } from '@/types/groupPhoto';
import GroupFilmPreview from '@/components/group-photos/group-film-preview/GroupFilmPreview.vue';
import PhotoWaterfall from '@/components/photo/photo-waterfall/PhotoWaterfall.vue';
import { get100KPhotos, processPhotoData } from '@/utils/photo';
import { useRouter, useRoute } from 'vue-router';
import type { WaterfallLayoutOptions } from '@/components/photo/photo-viewer/useWaterfallLayout';

// 添加路由
const router = useRouter();
const route = useRoute();

// 定义组图瀑布流布局选项
const layoutOptions: WaterfallLayoutOptions = {
    rowHeightMax: 300,
    rowHeightMin: 150,
    defaultGap: 10,
    defaultSideMargin: 8,
    mobileRowHeightMax: 200,
    mobileRowHeightMin: 100,
    mobileGap: 4,
    mobileSideMargin: 4
};

// 添加新的状态保存选中的组图和照片
const selectedGroupId = ref<string | null>(null);
const selectedPhotoId = ref<string | null>(null);
const groupFilmPreviewVisable = ref(false);

// 更新URL中的组图ID和照片ID参数
const updateUrlWithGroupId = (groupId: string | null, photoId: string | null = null) => {
    // 构建新的查询参数对象
    const query = { ...route.query };
    
    if (groupId) {
        query.groupId = groupId;
        if (photoId) {
            query.groupPhotoId = photoId;
        } else {
            delete query.groupPhotoId;
        }
        
        // 如果URL中有其他预览参数，清除它们
        if (query.photoId) delete query.photoId;
        if (query.viewerId) delete query.viewerId;
    } else {
        // 如果groupId为null，则删除相关参数
        delete query.groupId;
        delete query.groupPhotoId;
    }
    
    // 更新路由，保留当前路径，仅修改查询参数
    router.replace({
        path: route.path,
        query: query
    });
};

// 打开组图预览的方法
const openGroupPhotoPreview = (item: WaterfallItem) => {
    // 在处理后的数据中，我们需要找到这个照片对应的组图ID
    const groupId = item.groupId; 
    
    if (groupId) {
        selectedGroupId.value = groupId;
        selectedPhotoId.value = item.id;
        groupFilmPreviewVisable.value = true;
        // 更新URL
        updateUrlWithGroupId(groupId, item.id);
    } else {
        console.error('缺少组图ID，item:', item);
        ElMessage.warning('无法找到对应的组图信息');
    }
};

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedGroupId.value = null;
    selectedPhotoId.value = null;
    groupFilmPreviewVisable.value = false;
    // 清除URL参数
    updateUrlWithGroupId(null);
};

// 添加 props 来接收父组件传递的值
const props = defineProps({
    photoType: {
        type: Number,
        default: 0,
    },
});

//// 照片流数据
const images = ref<WaterfallItem[]>([]);
const currentPage = ref(1);
const hasMore = ref(true);
const containerRef = ref<HTMLElement | null>(null);

// 获取组图照片
const getPhotos = async () => {
    if (!hasMore.value) return;

    try {
        const response = await getAllGroupPhotos();
        // 获取所有封面图ID
        const coverPhotoIds = response.map((item: GroupPhotoDTO) => item.groupPhoto.coverPhotoId).filter(Boolean);

        // 批量获取封面图信息
        let coverPhotos: WaterfallItem[] = [];
        if (coverPhotoIds.length > 0) {
            const coverResponse = await getPhotosByIds(coverPhotoIds);
            coverPhotos = coverResponse;
        }
        
        // 预处理数据
        const processedData = response.map((item: GroupPhotoDTO) => {
            const groupPhoto = item.groupPhoto;
            const coverPhoto = coverPhotos.find(p => p.id === groupPhoto.coverPhotoId);
            if (!coverPhoto) {
                console.error('未找到封面图');
                return null;
            }
            
            return processPhotoData({
                ...coverPhoto,
                title: groupPhoto.title || '',
                introduce: groupPhoto.introduce || '',
                groupId: groupPhoto.id
            });
        }).filter(Boolean);

        // 更新数据时保留原有数据
        images.value = [...images.value, ...processedData];

        const imgs = images.value.slice(images.value.length - processedData.length);
        // 加载缩略图
        await get100KPhotos(imgs);

        // 由于API一次性返回所有数据，设置hasMore为false
        hasMore.value = false;
    } catch (error) {
        console.error('获取照片数据失败:', error);
        ElMessage.error('获取组图数据失败');
    }
};

// 新增滚动事件处理
const handleScroll = debounce(() => {
    const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
    const scrollBottom = scrollHeight - (scrollTop + clientHeight);

    // 当距离底部小于 50px
    if (scrollBottom < 50 && hasMore.value) {
        currentPage.value++;
        getPhotos();
    }
}, 200);

// 在 onMounted 中添加滚动监听
onMounted(async () => {
    await getPhotos();
    
    // 检查URL中是否有组图ID参数
    if (route.query.groupId) {
        const groupIdFromUrl = route.query.groupId as string;
        const photoIdFromUrl = route.query.groupPhotoId as string || undefined;
        
        // 设置当前预览的组图ID和照片ID，并打开预览
        selectedGroupId.value = groupIdFromUrl;
        selectedPhotoId.value = photoIdFromUrl || null;
        groupFilmPreviewVisable.value = true;
    }
    
    window.addEventListener('scroll', handleScroll);
});

// 在 onUnmounted 中移除监听
onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});

// 监听 photoType 的变化
watch(() => props.photoType, (newVal, oldVal) => {
    if (newVal !== oldVal) {
        currentPage.value = 1;
        images.value = [];
        hasMore.value = true;
        getPhotos();
    }
});
</script>

<style>
.el-image-viewer__mask {
    opacity: 1 !important;
}

.body-no-scroll {
    overflow: hidden;
}
</style>

<style scoped>
.container {
    width: 100%;
    margin: 0 0;
    padding: 0 0;
    background-color: var(--qd-color-bg-dark);
}
</style>
