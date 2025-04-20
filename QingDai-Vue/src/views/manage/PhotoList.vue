<template>
    <div class="photo-list-container">
        <el-table :data="tableData" style="width: 100%" border stripe>
            <el-table-column label="缩略图" width="220" fixed>
                <template #default="scope">
                    <el-image :src="scope.row.compressedSrc" style="height: 150px" fit="contain"
                        @click="openPreview(scope.row)" />
                </template>
            </el-table-column>

            <el-table-column prop="start" label="精选标记" width="110">
                <template #default="scope">
                    <el-select v-model="scope.row.start" :disabled="!scope.row.isEditing" placeholder="选择状态"
                        style="width: 80px">
                        <el-option label="精选" :value="1">
                            <el-tag :type="'warning'">
                                精选
                            </el-tag>
                        </el-option>
                        <el-option label="普通" :value="0">
                            <el-tag :type="'success'">
                                普通
                            </el-tag>
                        </el-option>
                        <el-option label="私密" :value="-1">
                            <el-tag :type="'info'">
                                隐藏
                            </el-tag>
                        </el-option>
                        <el-option label="气象" :value="2">
                            <el-tag :type="'primary'">
                                气象
                            </el-tag>
                        </el-option>
                    </el-select>
                </template>
            </el-table-column>
            
            <el-table-column prop="fileName" label="文件名" width="95">
                <template #default="scope">
                    <span>{{ scope.row.fileName }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="author" label="作者">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.author" />
                    <span v-else>{{ scope.row.author }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="camera" label="相机型号">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.camera" />
                    <span v-else>{{ scope.row.camera }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="lens" label="镜头型号">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.lens" />
                    <span v-else>{{ scope.row.lens }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="iso" label="ISO">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.iso" />
                    <span v-else>{{ scope.row.iso }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="shutter" label="快门">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.shutter" />
                    <span v-else>{{ scope.row.shutter }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="aperture" label="光圈">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.aperture" />
                    <span v-else>{{ scope.row.aperture }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="time" label="拍摄时间" width="105">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.time" />
                    <span v-else>{{ scope.row.time }}</span>
                </template>
            </el-table-column>

            <el-table-column prop="title" label="标题">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.title" />
                    <span v-else>{{ scope.row.title || '无标题' }}</span>
                </template>
            </el-table-column>

            <el-table-column prop="introduce" label="介绍">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.introduce" type="textarea"
                        :autosize="{ minRows: 1, maxRows: 3 }" />
                    <span v-else>{{ scope.row.introduce || '无介绍' }}</span>
                </template>
            </el-table-column>

            <el-table-column label="原始尺寸" width="120">
                <template #default="scope">
                    {{ scope.row.width }}×{{ scope.row.height }}
                </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                    <div v-if="!scope.row.isEditing">
                        <el-button size="small" @click="startEdit(scope.row)">编辑</el-button>
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
            <el-button type="primary" round @click="showPhotoUpload">
                <el-icon>
                    <Upload />
                </el-icon>上传照片
            </el-button>
        </div>

        <PhotoUpdate v-model="photoUploadVisible" ref="photoUpdateRef" @photo-uploaded="fetchData"/>

        <PhotoPreview v-model="previewVisible" :photo-id="currentPreviewId"/>

        <div class="pagination-wrapper">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                @current-change="handleCurrentChange" @size-change="handleSizeChange" />
        </div>
    </div>
</template>

<script setup lang="ts">
import PhotoUpdate from '@/views/manage/PhotoUpdate.vue'
import PhotoPreview from '@/components/PhotoPreview.vue'
import { ref, watchEffect } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getPhotosByPage, updatePhotoInfo, deletePhotoById, updatePhotoStartStatus } from '@/api/photo'
import type { PhotoResponse } from '@/api/photo'
import { get100KPhotos, processPhotoData, type EnhancedWaterfallItem } from '@/utils/photo'
import type { WaterfallItem } from '@/types'

const photoUpdateRef = ref()
const photoUploadVisible = ref(false)

// 表格数据
const tableData = ref<EnhancedWaterfallItem[]>([])
const editOriginData = ref<Record<string, EnhancedWaterfallItem>>({})
const currentPage = ref(1)
const pageSize = ref(50)
const total = ref(0)

// 预览相关
const previewVisible = ref(false)
const currentPreviewId = ref('')

// 打开照片上传对话框
const showPhotoUpload = () => {
    photoUploadVisible.value = true
}

// 获取照片列表数据
const fetchData = async () => {
    try {
        const response: PhotoResponse = await getPhotosByPage({
            page: currentPage.value,
            pageSize: pageSize.value
        });

        // 使用工具函数处理数据
        const processedData = response.records.map(item => 
            processPhotoData({
                ...item,
                isEditing: false,
            })
        );
        
        tableData.value = processedData;
        total.value = response.total;
        
        // 批量获取缩略图
        await get100KPhotos(tableData.value);
    } catch (error) {
        console.error('获取照片数据失败:', error);
        ElMessage.error('照片数据加载失败');
    }
};

// 分页相关方法
const handleCurrentChange = (val: number) => {
    currentPage.value = val;
    fetchData();
}

const handleSizeChange = (val: number) => {
    pageSize.value = val;
    fetchData();
}

// 开始编辑照片
const startEdit = (row: EnhancedWaterfallItem) => {
    row.isEditing = true;
    editOriginData.value[row.id] = { ...row };
}

// 取消编辑
const cancelEdit = (row: EnhancedWaterfallItem) => {
    Object.assign(row, editOriginData.value[row.id]);
    row.isEditing = false;
    delete editOriginData.value[row.id];
}

// 提交编辑
const submitEdit = async (row: EnhancedWaterfallItem) => {
    try {
        await updatePhotoInfo(row);
        ElMessage.success('更新成功');
        row.isEditing = false;
        delete editOriginData.value[row.id];
        await fetchData();
    } catch (error) {
        console.error('更新失败:', error);
        ElMessage.error('更新失败');
    }
}

// 删除照片
const handleDelete = async (row: EnhancedWaterfallItem) => {
    try {
        await ElMessageBox.confirm(
            '确定要删除这张照片吗？',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        );
        await deletePhotoById(row.id);
        ElMessage.success('删除成功');
        await fetchData();
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除失败:', error);
            ElMessage.error('删除失败');
        }
    }
}

// 打开预览
const openPreview = (item: WaterfallItem) => {
    previewVisible.value = true;
    currentPreviewId.value = item.id;
}

// 初始加载
watchEffect(() => {
    fetchData();
});
</script>

<style scoped>
.photo-list-container {
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
