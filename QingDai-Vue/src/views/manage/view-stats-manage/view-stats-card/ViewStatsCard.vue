<template>
    <el-card class="view-stats-card">
        <template #header>
            <div class="card-header">
                <span>照片浏览量统计</span>
                <el-button type="primary" @click="fetchViewStats">
                    刷新数据
                </el-button>
            </div>
        </template>
        <div class="view-stats-content">
            <el-table :data="paginatedData" style="width: 100%" border stripe >
                <el-table-column label="缩略图" fixed>
                    <template #default="scope">
                        <el-image :src="scope.row.compressedSrc" style="height: 100px" fit="contain" />
                    </template>
                </el-table-column>
                <el-table-column prop="photoId" label="照片ID" width="300" />
                <el-table-column prop="viewCount" label="浏览量" width="100" sortable/>
            </el-table>

            <div class="pagination-wrapper">
                <el-pagination
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="viewStats.length"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                />
            </div>
        </div>
    </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { get100KPhotos, type EnhancedWaterfallItem } from '@/utils/photo';
import { getPhotosByIds, getAllPhotoViewStats } from '@/api/photo';
import { ElMessage, ElLoading } from 'element-plus';
import { ManagePagination } from '@/config/pagination';

interface TableData {
    photoId: string;
    viewCount: number;
    compressedSrc: string;
}

const viewStats = ref<TableData[]>([]);
const currentPage = ref(1);
const pageSize = ref(ManagePagination.VIEW_STATS_PAGE_SIZE);

// 计算当前页的数据
const paginatedData = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return viewStats.value.slice(start, end);
});

// 处理页码变化
const handleCurrentChange = (val: number) => {
    currentPage.value = val;
};

// 处理每页条数变化
const handleSizeChange = (val: number) => {
    pageSize.value = val;
    currentPage.value = 1; // 重置到第一页
};

const fetchViewStats = async () => {
    const loadingInstance = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)'
    });
    
    try {
        const response = await getAllPhotoViewStats();
        // 过滤掉浏览量为0的数据
        viewStats.value = response.filter((data: TableData) => data.viewCount > 0);
        // 重置分页
        currentPage.value = 1;
    } catch (error) {
        ElMessage.error('获取浏览量统计失败');
        console.error('获取浏览量统计失败:', error);
    } finally {
        loadingInstance.close();
    }
};

// 加载缩略图
const loadThumbnails = async () => {
    if (!viewStats.value.length) return;
    
    try {
        // 只获取当前页的照片ID
        const start = (currentPage.value - 1) * pageSize.value;
        const end = start + pageSize.value;
        const currentPageStats = viewStats.value.slice(start, end);
        const photoIds = currentPageStats.map(stat => stat.photoId);
        
        // 获取照片信息
        const photosData = await getPhotosByIds(photoIds);
        
        // 获取缩略图
        await get100KPhotos(photosData);
        
        // 将获取到的缩略图URL赋值回原始数据
        currentPageStats.forEach((stat, index) => {
            if (photosData[index]?.compressedSrc) {
                stat.compressedSrc = photosData[index].compressedSrc;
            }
        });
    } catch (error) {
        console.error('加载缩略图失败:', error);
    }
};

// 监听页码变化
watch([currentPage, pageSize], () => {
    loadThumbnails();
});

// 监听 viewStats 变化
watch(() => viewStats.value, () => {
    loadThumbnails();
}, { immediate: true });

onMounted(() => {
    fetchViewStats();
});

onUnmounted(() => {

});
</script>

<style scoped>
.view-stats-card {
    margin-bottom: 20px;
    height: calc(100vh - 140px);
    display: flex;
    flex-direction: column;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.view-stats-content {
    overflow: hidden;
    display: flex;
    flex-direction: column;
    height: 100%;
}

:deep(.el-card__body) {
    height: 100%;
    padding: 0;
}

:deep(.el-table) {
    height: calc(100% - 50px);
}

:deep(.el-table .cell) {
    padding-left: 5px;
    padding-right: 5px;
}

:deep(.el-table--border .el-table__cell) {
    padding: 6px 0;
}

.pagination-wrapper {
    padding: 10px;
    display: flex;
    justify-content: flex-end;
}
</style> 