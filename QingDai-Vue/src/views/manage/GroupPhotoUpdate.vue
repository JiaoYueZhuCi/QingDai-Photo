<template>
    <el-dialog v-model="dialogVisible" :title="editMode ? '编辑组图' : '创建组图'" top="5vh" width="87%"
        :before-close="handleClose">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
            <el-form-item label="标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入组图标题"></el-input>
            </el-form-item>
            <el-form-item label="介绍" prop="introduce">
                <el-input v-model="form.introduce" type="textarea" :rows="3" placeholder="请输入组图介绍"></el-input>
            </el-form-item>
            <el-form-item label="选择照片" prop="photos">
                <el-select v-model="form.photos" multiple filterable remote reserve-keyword placeholder="请选择照片"
                    :remote-method="searchPhotos" :loading="loading" style="width: 100%">
                    <el-option v-for="item in photoOptions" :key="item.id" :label="item.title || item.fileName"
                        :value="item.id">
                        <div style="display: flex; align-items: center;">
                            <el-image :src="item.thumbnailSrc" style="width: 50px; height: 50px; margin-right: 10px;"
                                fit="cover" />
                            <span>{{ item.title || item.fileName }}</span>
                        </div>
                    </el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="封面照片" prop="cover">
                <el-select v-model="form.cover" placeholder="请选择封面照片" :disabled="!form.photos.length"
                    style="width: 100%">
                    <el-option v-for="(photoId, index) in form.photos" :key="photoId" :label="getPhotoLabel(photoId)"
                        :value="index">
                        <div style="display: flex; align-items: center;">
                            <el-image :src="getPhotoThumbnail(photoId)"
                                style="width: 50px; height: 50px; margin-right: 10px;" fit="cover" />
                            <span>{{ getPhotoLabel(photoId) }}</span>
                        </div>
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitForm">{{ editMode ? '保存' : '提交' }}</el-button>
                <el-button @click="resetForm">重置</el-button>
            </el-form-item>
        </el-form>

        <!-- 预览区域 -->
        <div class="preview-container" v-if="form.photos.length > 0">
            <h3>已选照片预览</h3>
            <draggable v-model="form.photos" item-key="id" class="photo-preview-list" animation="300"
                ghost-class="ghost-item" handle=".drag-handle" @end="onDragEnd">
                <template #item="{ element: photoId, index }">
                    <div class="photo-preview-item">
                        <el-image :src="getPhotoThumbnail(photoId)" fit="cover" />
                        <div class="photo-preview-info">
                            <span class="photo-preview-title">{{ getPhotoLabel(photoId) }}</span>
                            <el-tag v-if="index === form.cover" size="small" type="success">封面</el-tag>
                        </div>
                        <div class="photo-preview-actions">
                            <el-button type="danger" size="small" circle @click="removePhoto(index)">
                                <el-icon>
                                    <DeleteFilled />
                                </el-icon>
                            </el-button>
                        </div>
                        <div class="drag-handle">
                            <el-icon>
                                <Rank />
                            </el-icon>
                        </div>
                    </div>
                </template>
            </draggable>
        </div>
    </el-dialog>
</template>



<script setup lang="ts">
import { ref, reactive, watch, defineProps, defineEmits, defineExpose } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getPhotosByPage, getThumbnail100KPhotos } from '@/api/photo'
import { addGroupPhoto, updateGroupPhoto } from '@/api/groupPhoto'
import type { WaterfallItem } from '@/types'
import type { GroupPhoto } from '@/types/groupPhoto'
import JSZip from 'jszip'
import { DeleteFilled, Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

// 定义props和emit用于支持v-model
const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    editMode: {
        type: Boolean,
        default: false
    },
    editData: {
        type: Object as () => GroupPhoto | null,
        default: null
    }
});

const emit = defineEmits(['update:modelValue', 'group-photo-added', 'group-photo-updated']);

// 本地的对话框可见性控制
const dialogVisible = ref(false);

// 处理拖拽结束事件
const onDragEnd = () => {
    // 当拖拽导致封面索引变化时自动调整
    if (form.cover >= form.photos.length) {
        form.cover = form.photos.length - 1
    }
}

// 同步内外部的状态
watch(() => props.modelValue, (val) => {
    dialogVisible.value = val;
    if (val && props.editMode && props.editData) {
        // 编辑模式下，填充表单数据
        form.id = props.editData.id || '';
        form.title = props.editData.title || '';
        form.introduce = props.editData.introduce || '';
        form.photos = Array.isArray(props.editData.photos)
            ? [...props.editData.photos]
            : (props.editData.photos || '').split(',').filter(id => id.trim() !== '');
        form.cover = props.editData.cover || 0;

        // 保存原始数据用于重置
        originalData.value = {
            id: form.id,
            title: form.title,
            introduce: form.introduce,
            photos: [...form.photos],
            cover: form.cover
        };
    } else if (val && !props.editMode) {
        // 新建模式下，清空表单
        form.id = '';
        form.title = '';
        form.introduce = '';
        form.photos = [];
        form.cover = 0;
        originalData.value = null;
    }
});

watch(dialogVisible, (val) => {
    emit('update:modelValue', val);
});

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
    id: '',
    title: '',
    introduce: '',
    photos: [] as string[],
    cover: 0
});

const rules = reactive<FormRules>({
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
    ],
    photos: [
        { type: 'array', required: true, message: '请至少选择一张照片', trigger: 'change' }
    ]
});

// 照片选择相关
const photoOptions = ref<(WaterfallItem & { thumbnailSrc: string })[]>([]);
const photoCache = ref<Map<string, WaterfallItem & { thumbnailSrc: string }>>(new Map());
const thumbnailUrlCache = ref<Map<string, string>>(new Map());

// 搜索照片
const searchPhotos = async (query: string) => {
    if (query === '') {
        await fetchPhotos();
        return;
    }

    loading.value = true;
    try {
        const response = await getPhotosByPage({ page: 1, pageSize: 50 });
        if (response && response.records) {
            const filteredRecords = response.records.filter((item: WaterfallItem) => {
                return (item.title && item.title.toLowerCase().includes(query.toLowerCase())) ||
                    (item.fileName && item.fileName.toLowerCase().includes(query.toLowerCase()));
            });

            // 获取所有照片的缩略图
            const photoIds = filteredRecords.map((item: WaterfallItem) => item.id);
            if (photoIds.length > 0) {
                const thumbnailResponse = await getThumbnail100KPhotos(photoIds.join(','));
                if (thumbnailResponse && thumbnailResponse.data) {
                    const zip = await JSZip.loadAsync(thumbnailResponse.data);
                    await Promise.all(photoIds.map(async (id: string) => {
                        const photo = filteredRecords.find((p: WaterfallItem) => p.id === id);
                        if (photo && photo.fileName) {
                            const file = zip.file(photo.fileName);
                            if (file) {
                                const blob = await file.async('blob');
                                thumbnailUrlCache.value.set(id, URL.createObjectURL(blob));
                            }
                        }
                    }));
                }
            }

            photoOptions.value = filteredRecords.map((item: WaterfallItem) => ({
                ...item,
                thumbnailSrc: thumbnailUrlCache.value.get(item.id) || ''
            }));

            // 更新缓存
            photoOptions.value.forEach(photo => {
                photoCache.value.set(photo.id, photo);
            });
        }
    } catch (error) {
        console.error('搜索照片失败:', error);
        ElMessage.error('搜索照片失败');
    } finally {
        loading.value = false;
    }
};

// 获取照片列表
const fetchPhotos = async () => {
    loading.value = true;
    try {
        const response = await getPhotosByPage({ page: 1, pageSize: 50 });
        if (response && response.records) {
            // 获取所有照片的缩略图
            const photoIds = response.records.map((item: WaterfallItem) => item.id);
            if (photoIds.length > 0) {
                const thumbnailResponse = await getThumbnail100KPhotos(photoIds.join(','));
                if (thumbnailResponse && thumbnailResponse.data) {
                    const zip = await JSZip.loadAsync(thumbnailResponse.data);
                    await Promise.all(photoIds.map(async (id: string) => {
                        const photo = response.records.find((p: WaterfallItem) => p.id === id);
                        if (photo && photo.fileName) {
                            const file = zip.file(photo.fileName);
                            if (file) {
                                const blob = await file.async('blob');
                                thumbnailUrlCache.value.set(id, URL.createObjectURL(blob));
                            }
                        }
                    }));
                }
            }

            photoOptions.value = response.records.map((item: WaterfallItem) => ({
                ...item,
                thumbnailSrc: thumbnailUrlCache.value.get(item.id) || ''
            }));

            // 更新缓存
            photoOptions.value.forEach(photo => {
                photoCache.value.set(photo.id, photo);
            });
        }
    } catch (error) {
        console.error('获取照片列表失败:', error);
        ElMessage.error('获取照片列表失败');
    } finally {
        loading.value = false;
    }
};

// 获取照片标签
const getPhotoLabel = (photoId: string) => {
    const photo = photoCache.value.get(photoId);
    return photo ? (photo.title || photo.fileName) : `加载中`;
};

// 获取照片缩略图
const getPhotoThumbnail = (photoId: string) => {
    return thumbnailUrlCache.value.get(photoId) || '';
};

// 移除照片
const removePhoto = (index: number) => {
    // 如果删除的是封面照片
    if (index === form.cover) {
        // 重置封面为第一张照片
        form.cover = form.photos.length > 1 ? 0 : 0;
    } else if (index < form.cover) {
        // 如果删除的照片在封面前面，需要调整封面索引
        form.cover--;
    }

    // 删除照片
    form.photos.splice(index, 1);

    // 如果没有照片了，封面索引设为0
    if (form.photos.length === 0) {
        form.cover = 0;
    }
};

// 提交表单
const submitForm = async () => {
    if (!formRef.value) return;

    await formRef.value.validate(async (valid, fields) => {
        if (valid) {
            try {
                const formData = {
                    id: form.id,
                    title: form.title,
                    introduce: form.introduce,
                    photos: form.photos.join(','),  // 将数组转换为字符串
                    cover: form.cover
                };

                if (props.editMode) {
                    // 编辑模式
                    await updateGroupPhoto(formData);
                    ElMessage.success('更新组图成功');
                    emit('group-photo-updated');
                } else {
                    // 新建模式
                    await addGroupPhoto(formData);
                    ElMessage.success('创建组图成功');
                    emit('group-photo-added');
                }

                resetForm();
                dialogVisible.value = false;
            } catch (error) {
                console.error(props.editMode ? '更新组图失败:' : '创建组图失败:', error);
                ElMessage.error(props.editMode ? '更新组图失败' : '创建组图失败');
            }
        } else {
            console.log('验证失败:', fields);
        }
    });
};

// 添加原始数据引用
const originalData = ref<{
    id: string;
    title: string;
    introduce: string;
    photos: string[];
    cover: number;
} | null>(null);

// 重置表单
const resetForm = () => {
    if (!formRef.value) return;

    if (props.editMode && originalData.value) {
        // 编辑模式下，恢复到原始数据
        form.id = originalData.value.id;
        form.title = originalData.value.title;
        form.introduce = originalData.value.introduce;
        form.photos = [...originalData.value.photos];
        form.cover = originalData.value.cover;
        formRef.value.clearValidate();
    } else {
        // 新建模式下，清空表单
        formRef.value.resetFields();
        form.id = '';
        form.photos = [];
        form.cover = 0;
    }
};

// 关闭对话框
const handleClose = (done: () => void) => {
    resetForm();
    done();
};

// 组件挂载时获取照片列表
watch(dialogVisible, (val) => {
    if (val) {
        fetchPhotos();
    }
});

defineExpose({
    dialogVisible
});
</script>

<style scoped>
.preview-container {
    margin-top: 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 6px;
}

h3 {
    margin-top: 0;
    margin-bottom: 15px;
    color: #606266;
}

.photo-preview-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.photo-preview-item {
    position: relative;
    width: 120px;
    border-radius: 4px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;
}

.photo-preview-item.is-cover {
    border: 2px solid #67c23a;
    transform: scale(1.05);
}

.photo-preview-item .el-image {
    width: 100%;
    height: 100px;
    display: block;
}

.photo-preview-info {
    padding: 5px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #f0f0f0;
}

.photo-preview-title {
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
}

.photo-preview-actions {
    position: absolute;
    top: 5px;
    right: 5px;
    display: none;
}

.photo-preview-item:hover .photo-preview-actions {
    display: block;
}
</style>