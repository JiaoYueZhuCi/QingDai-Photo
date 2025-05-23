<template>
    <div class="group-photos-list-container">
        <div class="header-actions">
            <el-button type="primary" @click="fetchData">刷新组图列表</el-button>
        </div>
        
        <el-table :data="tableData" style="width: 100%" border stripe :max-height="tableHeight">
            <el-table-column label="封面图" width="220" fixed>
                <template #default="scope">
                    <el-image :src="scope.row.coverImage" style="height: 150px" fit="contain"
                        @click="openPreview(scope.row)" />
                </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" width="180">
                <template #default="scope">
                    <span>{{ scope.row.groupPhoto.title || '无标题' }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="introduce" label="介绍">
                <template #default="scope">
                    <span>{{ scope.row.groupPhoto.introduce || '无介绍' }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="photoCount" label="照片数量" width="100">
                <template #default="scope">
                    {{ scope.row.photoIds ? scope.row.photoIds.length : 0 }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                    <div>
                        <el-button size="small" @click="handleManagePhotos(scope.row)">管理组图</el-button>
                        <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
            :edit-mode="editMode"
            :edit-data="currentEditData"
            @group-photo-added="fetchData" 
            @group-photo-updated="fetchData" 
            @close="closeGroupPhotoUpdate"
        />

        <div class="pagination-wrapper">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                @current-change="handleCurrentChange" @size-change="handleSizeChange" />
        </div>

        <!-- 组图预览组件 -->
        <GroupPhotoPreview
            v-model="previewVisible"
            :group-id="selectedGroupId || ''"
            :initial-photo-id="selectedPhotoId || undefined"
            @close="closeGroupPhotoPreview"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllGroupPhotos, deleteGroupPhoto, getGroupPhotosByPage } from '@/api/groupPhoto'
import type { GroupPhotoDTO } from '@/types/groupPhoto'
import GroupPhotoUpdate from '@/components/group-photos/group-photos-update/GroupPhotosUpdate.vue'
import GroupPhotoPreview from '@/views/manage/group-photos-manage/group-photo-preview/GroupPhotosPreview.vue'
import { get100KPhotos, type EnhancedWaterfallItem } from '@/utils/photo'
import { ManagePagination } from '@/config/pagination'

const groupPhotoDialogVisible = ref(false)
const editMode = ref(false)
const currentEditData = ref<GroupPhotoDTO | null>(null)

const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(ManagePagination.GROUP_PHOTOS_MANAGE_PAGE_SIZE)
const total = ref(0)
const loading = ref(false)

// 组图预览相关
const selectedGroupId = ref<string | null>(null)
const selectedPhotoId = ref<string | null>(null)
const previewVisible = ref(false)
const tableHeight = ref(window.innerHeight - 65)
// 打开组图预览
const openPreview = (item: GroupPhotoDTO) => {
    selectedGroupId.value = item.groupPhoto.id;
    selectedPhotoId.value = item.groupPhoto.coverPhotoId; 
    previewVisible.value = true;
}

// 关闭组图预览
const closeGroupPhotoPreview = () => {
    selectedGroupId.value = null
    selectedPhotoId.value = null
    previewVisible.value = false
}

// 编辑组图
const handleManagePhotos = (row: any) => {
    editMode.value = true
    currentEditData.value = row;
    groupPhotoDialogVisible.value = true
}

// 添加组图
const showGroupPhotoAdd = () => {
    editMode.value = false
    currentEditData.value = null
    groupPhotoDialogVisible.value = true
}

// 关闭组图编辑
const closeGroupPhotoUpdate = () => {
    groupPhotoDialogVisible.value = false
}

// 获取组图数据
const fetchData = async () => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '加载组图中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  
  try {
    const response = await getGroupPhotosByPage({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    
    if (response && response.records) {
      tableData.value = response.records.map((item: GroupPhotoDTO) => ({
        ...item,
        isEditing: false,
        coverImage: '',
      }));
      
      total.value = response.total;

      // 获取所有封面图ID对应的项
      const coverItems: EnhancedWaterfallItem[] = tableData.value
        .filter(item => item.groupPhoto.coverPhotoId)
        .map(item => ({
          id: item.groupPhoto.coverPhotoId,
          fileName: '',
          author: '',
          width: 0,
          height: 0,
          aperture: '',
          iso: '',
          shutter: '',
          camera: '',
          lens: '',
          focalLength: '',
          shootTime: '',
          title: '',
          introduce: '',
          startRating: 0,
          compressedSrc: ''
        }));

      if (coverItems.length > 0) {
        // 使用工具函数获取缩略图
        const processedItems = await get100KPhotos(coverItems);
        
        // 更新表格数据中的封面图URL
        tableData.value = tableData.value.map(item => {
          const coverItem = processedItems.find(cover => cover.id === item.groupPhoto.coverPhotoId);
          return {
            ...item,
            coverImage: coverItem?.compressedSrc || ''
          };
        });
      }
    }
  } catch (error) {
    console.error('获取组图数据失败:', error);
    ElMessage.error('组图数据加载失败');
  } finally {
    loadingInstance.close();
  }
};

const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchData();
}

const handleSizeChange = (val: number) => {
    pageSize.value = val
    fetchData();
}

// 初始加载
onMounted(() => {
    fetchData()
})

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

</script>

<style scoped>
.group-photos-list-container {
    background: #fff;
}

.header-actions {
    display: flex;
    gap: 5px;
    padding: 10px;
    background-color: var(--qd-color-bg-light);
}

.floating-action {
    position: fixed;
    right: 20px;
    bottom: 80px;
    z-index: 2000;
}

.el-button {
    margin: 0 2px;
}
</style>
