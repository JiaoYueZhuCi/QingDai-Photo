<template>
    <div class="group-photos-list-container">
        <el-table :data="tableData" style="width: 100%" border stripe>
            <el-table-column label="封面图" width="220" fixed>
                <template #default="scope">
                    <el-image :src="scope.row.coverImage" style="height: 150px" fit="contain"
                        @click="openPreview(scope.row.id)" />
                </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" width="180">
                <template #default="scope">
                    <span>{{ scope.row.title || '无标题' }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="introduce" label="介绍">
                <template #default="scope">
                    <span>{{ scope.row.introduce || '无介绍' }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="photoCount" label="照片数量" width="100">
                <template #default="scope">
                    {{ scope.row.photosArray ? scope.row.photosArray.length : 0 }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                    <div v-if="!scope.row.isEditing">
                        <el-button size="small" type="primary" @click="handleManagePhotos(scope.row)">管理照片</el-button>
                        <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
                    </div>
                    <div v-else>
                        <el-button size="small" type="success" @click="submitEdit(scope.row)">保存</el-button>
                        <el-button size="small" @click="cancelEdit(scope.row)">取消</el-button>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="floating-action">
            <el-button type="primary" round @click="showGroupPhotoAdd">
                <el-icon>
                    <Plus />
                </el-icon>创建组图
            </el-button>
        </div>

        <GroupPhotoUpdate 
            v-model="groupPhotoDialogVisible" 
            ref="groupPhotoUpdateRef" 
            :edit-mode="editMode"
            :edit-data="currentEditData"
            @group-photo-added="fetchData" 
            @group-photo-updated="fetchData" 
        />

        <div class="pagination-wrapper">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                @current-change="handleCurrentChange" @size-change="handleSizeChange" />
        </div>

        <!-- 组图预览组件 -->
        <group-photo-preview
            v-if="selectedGroupId !== null"
            :group-id="selectedGroupId || ''"
            :initial-photo-id="selectedPhotoId || undefined"
            @close="closeGroupPhotoPreview"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, watchEffect, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllGroupPhotos, updateGroupPhoto, deleteGroupPhoto } from '@/api/groupPhoto'
import {  getThumbnail100KPhotos } from '@/api/photo'
import type { GroupPhoto } from '@/types/groupPhoto'
import GroupPhotoUpdate from '@/views/manage/GroupPhotoUpdate.vue'
import GroupPhotoPreview from '@/components/GroupPhotoPreview.vue'
import JSZip from 'jszip'

const groupPhotoUpdateRef = ref()
const groupPhotoDialogVisible = ref(false)
const editMode = ref(false)
const currentEditData = ref<GroupPhoto | null>(null)

const tableData = ref<(GroupPhoto & { isEditing: boolean, coverImage: string, photosArray: string[] })[]>([])
const editOriginData = ref<any>({})
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 组图预览相关
const selectedGroupId = ref<string | null>(null)
const selectedPhotoId = ref<string | null>(null)

const showGroupPhotoAdd = () => {
    editMode.value = false
    currentEditData.value = null
    groupPhotoDialogVisible.value = true
}

const fetchData = async () => {
    try {
        const response = await getAllGroupPhotos()

        // 处理响应数据
        if (response && Array.isArray(response)) {
            tableData.value = response.map((item: GroupPhoto) => ({
                ...item,
                isEditing: false,
                coverImage: '',  // 初始化为空，后续通过getThumbnail100KPhotos获取
                photosArray: typeof item.photos === 'string' 
                    ? item.photos.split(',').filter(id => id.trim() !== '')
                    : item.photos
            }));

            // 获取所有组图的封面照片ID
            const coverPhotoIds = tableData.value
                .filter(item => item.photosArray.length > 0 && item.cover !== undefined)
                .map(item => item.photosArray[item.cover])
                .filter(id => id);  // 过滤掉空值

            // 批量获取缩略图
            if (coverPhotoIds.length > 0) {
                // 创建一个照片ID到索引位置的映射，用于后续恢复顺序
                const idPositionMap = new Map();
                coverPhotoIds.forEach((id, index) => {
                    if (!idPositionMap.has(id)) {
                        idPositionMap.set(id, []);
                    }
                    idPositionMap.get(id).push(index);
                });

                // 去重后的照片ID数组
                const uniqueCoverPhotoIds = [...new Set(coverPhotoIds)];
                
                const thumbnailResponse = await getThumbnail100KPhotos(uniqueCoverPhotoIds.join(','));
                if (thumbnailResponse && thumbnailResponse.data) {
                    const zip = await JSZip.loadAsync(thumbnailResponse.data);
                    
                    // 创建从照片ID到URL的映射
                    const photoIdToUrlMap = new Map();
                    
                    // 为每个唯一的照片ID创建URL
                    let i = 0;
                    for (const [filename, file] of Object.entries(zip.files)) {
                        if (file.dir) continue;
                        
                        const photoId = uniqueCoverPhotoIds[i++];
                        if (photoId) {
                            const blob = await file.async('blob');
                            const url = URL.createObjectURL(blob);
                            photoIdToUrlMap.set(photoId, url);
                        }
                    }

                    // 更新表格数据中的缩略图
                    tableData.value = tableData.value.map((item) => {
                        if (item.photosArray.length > 0 && item.cover !== undefined) {
                            const coverPhotoId = item.photosArray[item.cover];
                            return {
                                ...item,
                                coverImage: photoIdToUrlMap.get(coverPhotoId) || ''
                            };
                        }
                        return item;
                    });
                }
            }

            total.value = response.length;
        } else {
            tableData.value = [];
            total.value = 0;
        }
    } catch (error) {
        console.error('获取组图数据失败:', error);
        ElMessage.error('组图数据加载失败');
    }
};

const handleCurrentChange = (val: number) => {
    currentPage.value = val
}

const handleSizeChange = (val: number) => {
    pageSize.value = val
}

watchEffect(() => {
    fetchData()
})

const cancelEdit = (row: any) => {
    Object.assign(row, editOriginData.value[row.id])
    row.isEditing = false
    delete editOriginData.value[row.id]
}

const submitEdit = async (row: any) => {
    try {
        await updateGroupPhoto({
            id: row.id,
            photos: row.photosArray.join(','),
            cover: row.cover,
            title: row.title,
            introduce: row.introduce
        })

        ElMessage.success('更新成功')
        row.isEditing = false
        delete editOriginData.value[row.id]
        await fetchData()
    } catch (error) {
        console.error('更新失败:', error)
        ElMessage.error('更新失败')
    }
}

// 删除功能
const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm(
            '确定要删除这个组图吗？',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        )

        await deleteGroupPhoto(row.id)
        ElMessage.success('删除成功')
        await fetchData()
    } catch (error) {
        console.error('删除失败:', error)
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
        }
    }
}
// 照片预览
const openPreview = (id: string) => {
    selectedGroupId.value = id
    selectedPhotoId.value = null
}

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedGroupId.value = null
    selectedPhotoId.value = null
}

// 管理照片
const handleManagePhotos = (row: any) => {
    editMode.value = true
    currentEditData.value = { 
        id: row.id,
        title: row.title || '',
        introduce: row.introduce || '',
        photos: row.photosArray,
        cover: row.cover || 0
    }
    groupPhotoDialogVisible.value = true
}
</script>

<style scoped>
.group-photos-list-container {
    background: #fff;
}

.floating-action {
    position: fixed;
    right: 20px;
    bottom: 80px;
    z-index: 2000;
}

.pagination-wrapper {
    position: fixed;
    z-index: 100;
    left: 50%;
    transform: translateX(-50%);
    bottom: 10px;
    background-color: rgb(240, 240, 240);
    padding: 5px;
}

.el-button {
    margin: 0 2px;
}
</style>
