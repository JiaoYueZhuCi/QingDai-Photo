<template>
    <div class="timeline-list-container">
        <div class="header-actions">
            <el-button type="primary" @click="fetchData">刷新时间线列表</el-button>
        </div>
        
        <el-table :data="tableData" style="width: 100%" border stripe :max-height="tableHeight">
            <el-table-column prop="time" label="时间" width="180">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.recordTime" />
                    <span v-else>{{ scope.row.recordTime }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" width="180">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.title" />
                    <div  v-else>{{ scope.row.title || '无标题' }}</div>
                </template>
            </el-table-column>
            <el-table-column prop="text" label="内容">
                <template #default="scope">
                    <el-input v-if="scope.row.isEditing" v-model="scope.row.text" type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4 }" />
                    <div  v-else>{{ scope.row.text || '无内容' }}</div>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
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

        <!-- 卡片预览 -->
        <!-- <div class="timeline-preview">
            <h3>时间轴预览</h3>
            <el-timeline>
                <el-timeline-item v-for="(item, index) in tableData" :key="index" :timestamp="item.time" placement="top">
                    <el-card>
                        <h4>{{ item.title || '无标题' }}</h4>
                        <p>{{ item.text || '无内容' }}</p>
                    </el-card>
                </el-timeline-item>
            </el-timeline>
        </div> -->

        <div class="floating-action">
            <el-button type="primary" round @click="showTimelineAdd">
                <el-icon>
                    <Plus />
                </el-icon>添加时间轴
            </el-button>
        </div>
        <TimelineUpdate v-model="timelineAddVisible" ref="timelineUpdateRef" @timeline-added="fetchData" />

        <div class="pagination-wrapper">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                @current-change="handleCurrentChange" @size-change="handleSizeChange" />
        </div>
    </div>
</template>

<script setup lang="ts">
import TimelineUpdate from '@/views/manage/timeline-manage/timeline-update/TimelineUpdate.vue'
import { ref, onMounted } from 'vue'
import type { TimelineItem } from '@/types'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllTimelines, updateTimeline, deleteTimeline, getTimelinesByPage } from '@/api/timeline'
import { ManagePagination } from '@/config/pagination'

const timelineUpdateRef = ref()
const timelineAddVisible = ref(false)
const tableData = ref<TimelineItem[]>([])
const editOriginData = ref<any>({})
const currentPage = ref(1)
const pageSize = ref(ManagePagination.TIMELINE_MANAGE_PAGE_SIZE)
const total = ref(0)
const loading = ref(false)
const tableHeight = ref(window.innerHeight - 65)

const showTimelineAdd = () => {
    timelineAddVisible.value = true
}

const fetchData = async () => {
    try {
        loading.value = true
        
        const loadingInstance = ElLoading.service({
            lock: true,
            text: '加载时间线数据中...',
            background: 'rgba(0, 0, 0, 0.7)',
            fullscreen: true
        });
        
        const response = await getTimelinesByPage({
            page: currentPage.value,
            pageSize: pageSize.value
        })

        // 处理响应数据
        if (response && response.records) {
            tableData.value = response.records.map((item: any) => ({
                isEditing: false,
                id: item.id,
                recordTime: item.recordTime || '',
                title: item.title || '',
                text: item.text || ''
            }))

            total.value = response.total
        } else {
            tableData.value = []
            total.value = 0
        }
        
        loadingInstance.close();
    } catch (error) {
        console.error('获取时间轴数据失败:', error)
        ElMessage.error('时间轴数据加载失败')
    } finally {
        loading.value = false
    }
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchData()
}

const handleSizeChange = (val: number) => {
    pageSize.value = val
    fetchData()
}

// 初始加载
onMounted(() => {
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
        const loadingInstance = ElLoading.service({
            lock: true,
            text: '保存中...',
            background: 'rgba(0, 0, 0, 0.7)',
            fullscreen: true
        });
        
        await updateTimeline({
            id: row.id,
            recordTime: row.recordTime,
            title: row.title,
            text: row.text
        })

        loadingInstance.close();
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
            '确定要删除这条时间轴记录吗？',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        )

        const loadingInstance = ElLoading.service({
            lock: true,
            text: '删除中...',
            background: 'rgba(0, 0, 0, 0.7)',
            fullscreen: true
        });
        
        await deleteTimeline(row.id)
        
        loadingInstance.close();
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
.timeline-list-container {
    background: #fff;
}

.header-actions {
    display: flex;
    gap: 5px;
    padding: 10px;
    background-color: var(--qd-color-bg-light);
}

.timeline-preview {
    margin-top: 30px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
}

.floating-action {
    position: fixed;
    right: 20px;
    bottom: 80px;
    z-index: 2000;
}

:global(.el-image-viewer__wrapper) {
    z-index: 2000 !important;
}

.el-button {
    margin: 0;
}

.refresh-button {
    display: none;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin: 20px 0;
}
</style>
