<template>
    <div class="photo-list-container">
        <el-button class="refresh-button" type="primary" @click="fetchData">刷新照片列表</el-button>
        <el-button class="no-metadata-button" type="warning" @click="fetchNoMetadataData">无元数据照片列表</el-button>
        
        <el-table :data="tableData" style="width: 100%" border stripe :max-height="tableHeight">
            <el-table-column label="缩略图" width="180" fixed>
                <template #default="scope">
                    <el-image :src="scope.row.compressedSrc" style="height: 120px" fit="contain"
                        @click="openPreview(scope.row)" />
                </template>
            </el-table-column>

            <el-table-column prop="start" label="标记" width="95">
                <template #default="scope">
                    <el-select v-model="scope.row.start" :disabled="!scope.row.isEditing" placeholder="状态"
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
            
            <el-table-column prop="fileName" label="文件名" width="90">
                <template #default="scope">
                    <span>{{ scope.row.fileName }}</span>
                </template>
            </el-table-column>
            
            <el-table-column label="拍摄信息" width="350">
                <el-table-column prop="camera" label="相机" width="100">
                    <template #default="scope">
                        <el-input v-if="scope.row.isEditing" v-model="scope.row.camera" />
                        <span v-else>{{ scope.row.camera }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="lens" label="镜头" width="100">
                    <template #default="scope">
                        <el-input v-if="scope.row.isEditing" v-model="scope.row.lens" />
                        <span v-else>{{ scope.row.lens }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="参数" width="100">
                    <template #default="scope">
                        <div v-if="scope.row.isEditing">
                            <div class="param-row">
                                <span class="param-label">ISO:</span>
                                <el-input v-model="scope.row.iso" size="small" class="param-input" />
                            </div>
                            <div class="param-row">
                                <span class="param-label">快门:</span>
                                <el-input v-model="scope.row.shutter" size="small" class="param-input" />
                            </div>
                            <div class="param-row">
                                <span class="param-label">光圈:</span>
                                <el-input v-model="scope.row.aperture" size="small" class="param-input" />
                            </div>
                            <div class="param-row">
                                <span class="param-label">焦距:</span>
                                <el-input v-model="scope.row.focalLength" size="small" class="param-input" />
                            </div>
                        </div>
                        <div v-else>
                            <div class="params">
                                <span>ISO: {{scope.row.iso || '未知'}}</span>
                                <span>快门: {{scope.row.shutter || '未知'}}</span>
                                <span >光圈: {{scope.row.aperture || '未知'}}</span>
                                <span>焦距: {{scope.row.focalLength || '未知'}}</span>
                            </div>
                        </div>
                    </template>
                </el-table-column>
            </el-table-column>
            
            <el-table-column prop="author" label="作者" width="100">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.author" />
                    <span v-else>{{ scope.row.author }}</span>
                </template>
            </el-table-column>
            
            <el-table-column prop="time" label="拍摄时间" width="100">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.time" />
                    <span v-else>{{ scope.row.time }}</span>
                </template>
            </el-table-column>

            <el-table-column prop="title" label="标题" min-width="120">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.title" />
                    <span v-else>{{ scope.row.title || '无标题' }}</span>
                </template>
            </el-table-column>

            <el-table-column prop="introduce" label="介绍" min-width="170">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.introduce" type="textarea"
                        :autosize="{ minRows: 1, maxRows: 3 }" />
                    <span v-else>{{ scope.row.introduce || '无介绍' }}</span>
                </template>
            </el-table-column>

            <el-table-column label="尺寸" width="90">
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
import PhotoUpdate from '@/components/photo/PhotoUpdate.vue'
import PhotoPreview from '@/components/photo/PhotoPreview.vue'
import { ref, watchEffect, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getPhotosByPage, updatePhotoInfo, deletePhotoById, getVisiblePhotosByPage, getNoMetadataPhotosByPage } from '@/api/photo'
import { getRolesPermissions } from '@/api/user'
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
const userRole = ref('')
const loading = ref(false)
const tableHeight = ref(window.innerHeight - 65)

// 预览相关
const previewVisible = ref(false)
const currentPreviewId = ref('')

// 获取用户角色
const fetchUserRole = async () => {
    try {
        const response = await getRolesPermissions();
        // 优先判断是否有ADMIN角色
        if (response.roles.includes('ADMIN')) {
            userRole.value = 'ADMIN';
        } else if (response.roles.includes('VIEWER')) {
            userRole.value = 'VIEWER';
        } else {
            userRole.value = response.roles[0]; // 如果没有，则使用第一个角色
        }
    } catch (error) {
        console.error('获取用户角色失败:', error);
        ElMessage.error('获取用户角色失败');
    }
};

// 获取照片列表数据
const fetchData = async () => {
    const loadingInstance = ElLoading.service({
        lock: true,
        text: '加载照片中...',
        background: 'rgba(0, 0, 0, 0.7)'
    });
    
    try {
        let response: PhotoResponse;
        
        if (userRole.value === 'ADMIN') {
            response = await getPhotosByPage({
                page: currentPage.value,
                pageSize: pageSize.value
            });
            ElMessage.success('显示所有照片');
        } else {
            response = await getVisiblePhotosByPage({
                page: currentPage.value,
                pageSize: pageSize.value
            });
            ElMessage.info('仅显示可见照片');
        }

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
    } finally {
        loadingInstance.close();
    }
};

// 获取无元数据照片列表数据
const fetchNoMetadataData = async () => {
    const loadingInstance = ElLoading.service({
        lock: true,
        text: '加载无元数据照片中...',
        background: 'rgba(0, 0, 0, 0.7)'
    });
    
    try {
        const response = await getNoMetadataPhotosByPage({
            page: currentPage.value,
            pageSize: pageSize.value
        });

        console.log(response);
        // 使用工具函数处理数据
        const processedData = response.records.map((item: EnhancedWaterfallItem) => 
            processPhotoData({
                ...item,
                isEditing: false,
            })
        );
        
        tableData.value = processedData;
        total.value = response.total;
        
        // 批量获取缩略图
        await get100KPhotos(tableData.value);
        ElMessage.success('已切换到无元数据照片列表');
    } catch (error) {
        console.error('获取无元数据照片数据失败:', error);
        ElMessage.error('无元数据照片加载失败');
    } finally {
        loadingInstance.close();
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
        await updatePhotoInfo({
            id: row.id,
            title: row.title,
            introduce: row.introduce,
            start: row.start,
            camera: row.camera,
            lens: row.lens,
            aperture: row.aperture,
            shutter: row.shutter,
            iso: row.iso,
            focalLength: row.focalLength,
            fileName: row.fileName,
            author: row.author,
            width: row.width,
            height: row.height,
            time: row.time
        });
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

// 打开照片上传对话框
const showPhotoUpload = () => {
    photoUploadVisible.value = true
}

// 初始加载
watchEffect(async () => {
    await fetchUserRole();
    await fetchData();
});

// 监听窗口大小变化调整表格高度
onMounted(() => {
    window.addEventListener('resize', adjustTableHeight)
})

onUnmounted(() => {
    window.removeEventListener('resize', adjustTableHeight)
})

const adjustTableHeight = () => {
    tableHeight.value = window.innerHeight - 130
}
</script>

<style scoped>
.photo-list-container {
    background: #fff;
    height: 100%;
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

.refresh-button {
    position: fixed;
    right: 20px;
    top: 15px;
}

.no-metadata-button {
    position: fixed;
    right: 150px;
    top: 15px;
}

.params {
    display: flex;
    flex-direction: column;
    font-size: 12px;
}

.params span {
    margin-bottom: 3px;
}

.param-row {
    display: flex;
    align-items: center;
    margin-bottom: 5px;
}

.param-label {
    width: 40px;
    font-size: 12px;
}

.param-input {
    width: 85px;
}

:deep(.el-table .cell) {
    padding-left: 5px;
    padding-right: 5px;
}

:deep(.el-table--border .el-table__cell) {
    padding: 6px 0;
}
</style>
