<template>
    <div class="photo-list-container">
        <el-table :data="tableData" style="width: 100%" border stripe>
            <el-table-column label="缩略图" width="220" fixed>
                <template #default="scope">
                    <el-image :src="scope.row.compressedSrc" style="height: 150px" fit="contain"
                        @click="openPreview(scope.row.id)" />
                </template>
            </el-table-column>
            <el-table-column prop="fileName" label="文件名" width="95">
                <template #default="scope">
                    <!-- <el-input v-if="scope.row.isEditing" v-model="scope.row.fileName" /> -->
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

            <el-table-column prop="start" label="精选标记" width="110">
                <template #default="scope">
                    <el-select v-model="scope.row.start" :disabled="!scope.row.isEditing" placeholder="选择状态"
                        style="width: 80px">
                        <el-option label="精选" :value="1" />
                        <el-option label="普通" :value="0" />
                        <el-option label="私密" :value="-1" />
                    </el-select>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="75" fixed>
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

        <PhotoUpdate v-model="photoUploadVisible" ref="photoUpdateRef" />

        <PreviewViewer v-if="previewVisible" :photo-id="currentPreviewId" @close="previewVisible = false" />

        <div class="pagination-wrapper">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                @current-change="handleCurrentChange" @size-change="handleSizeChange" />
        </div>
    </div>
</template>

<script setup lang="ts">
import PhotoUpdate from '@/views/manage/PhotoUpdate.vue'
import PreviewViewer from '@/components/PreviewViewer.vue'
import { ref, watchEffect } from 'vue'
import JSZip from 'jszip'
import type { WaterfallItem } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getPhotosByPage, updatePhotoInfo, deletePhotoById, updatePhotoStartStatus, getThumbnail100KPhotos } from '@/api/photo'
import type { PhotoResponse } from '@/api/photo'

const photoUpdateRef = ref()
const photoUploadVisible = ref(false)

const tableData = ref<WaterfallItem[]>([])
const editOriginData = ref<any>({})
const currentPage = ref(1)
const pageSize = ref(50)
const total = ref(0)

const showPhotoUpload = () => {
    photoUploadVisible.value = true
}

const fetchData = async () => {
    try {
        const response: PhotoResponse = await getPhotosByPage({
            page: currentPage.value,
            pageSize: pageSize.value
        })

        tableData.value = response.records.map((item: any) => ({
            isEditing: false,
            id: item.id,
            fileName: item.fileName,
            author: item.author || '未知作者',
            camera: item.camera || '',
            lens: item.lens || '',
            iso: item.iso || '',
            shutter: item.shutter || '',
            aperture: item.aperture || '',
            time: item.time || '',
            start: item.start || 0,
            title: item.title || '',
            introduce: item.introduce || '',
            width: item.width || 0,
            height: item.height || 0,
            compressedSrc: ''
        }))

        total.value = response.total
        await getThumbnailPhotos()
    } catch (error) {
        console.error('获取数据失败:', error)
        ElMessage.error('数据加载失败')
    }
}

const getThumbnailPhotos = async () => {
    try {
        const response = await getThumbnail100KPhotos(
            tableData.value.map(item => item.id).join(',')
        )

        const zip = await JSZip.loadAsync(response.data)

        await Promise.all(tableData.value.map(async item => {
            const file = zip.file(`${item.fileName}`)
            if (file) {
                const blob = await file.async('blob')
                item.compressedSrc = URL.createObjectURL(blob)
            } else {
                console.warn('ZIP包中未找到照片:', item.fileName)
            }
        }))
    } catch (error) {
        console.error('批量获取缩略图失败:', error)
        ElMessage.error('缩略图加载失败')
    }
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
}

const handleSizeChange = (val: number) => {
    pageSize.value = val
}

watchEffect(() => {
    fetchData()
})

// 编辑功能逻辑
const startEdit = (row: any) => {
    row.isEditing = true
    editOriginData.value[row.id] = { ...row }
}

const cancelEdit = (row: any) => {
    Object.assign(row, editOriginData.value[row.id])
    row.isEditing = false
    delete editOriginData.value[row.id]
}

const submitEdit = async (row: any) => {
    try {
        await updatePhotoInfo(row)
        ElMessage.success('更新成功')
        row.isEditing = false
        delete editOriginData.value[row.id]
        fetchData()
    } catch (error) {
        console.error('更新失败:', error)
        ElMessage.error('更新失败')
    }
}

// 删除功能
const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm(
            '确定要删除这张照片吗？',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        )
        await deletePhotoById(row.id)
        ElMessage.success('删除成功')
        fetchData()
    } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error('取消删除')
    }
}


// 添加预览相关变量
const previewVisible = ref(false)
const currentPreviewId = ref('')
// 添加打开预览的方法
const openPreview = (id: string) => {
    previewVisible.value = true
    currentPreviewId.value = id
}
</script>

<style scoped>
.photo-list-container {
    padding: 0px;
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

:global(.el-image-viewer__wrapper) {
    z-index: 2000 !important;
}

.el-button {
    margin: 0;
}
</style>
