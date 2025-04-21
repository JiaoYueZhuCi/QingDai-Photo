<template>
    <el-dialog v-model="visible" :title="editMode ? '编辑组图' : '创建组图'" top="5vh" width="87%" :before-close="handleClose">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
            <el-form-item label="标题" prop="groupPhoto.title">
                <el-input v-model="form.groupPhoto.title" placeholder="请输入组图标题"></el-input>
            </el-form-item>
            <el-form-item label="介绍" prop="introduce">
                <el-input v-model="form.groupPhoto.introduce" type="textarea" :rows="3"
                    placeholder="请输入组图介绍"></el-input>
            </el-form-item>
            <el-form-item label="选择照片" prop="photoIds">
                <el-select v-model="form.photoIds" multiple filterable remote reserve-keyword placeholder="请选择照片"
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
                <el-select v-model="form.groupPhoto.coverPhotoId" placeholder="请选择封面照片"
                    :disabled="!form.photoIds.length" style="width: 100%">
                    <el-option v-for="(photoId, index) in form.photoIds" :key="photoId" :label="getPhotoLabel(photoId)"
                        :value="photoId">
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
        <div class="preview-container" v-if="form.photoIds.length > 0">
            <h3>已选照片预览</h3>
            <draggable v-model="form.photoIds" item-key="id" class="photo-preview-list" animation="300"
                ghost-class="ghost-item" handle=".drag-handle" @end="onDragEnd">
                <template #item="{ element: photoId, index }">
                    <div class="photo-preview-item">
                        <el-image :src="getPhotoThumbnail(photoId)" fit="cover" />
                        <div class="photo-preview-info">
                            <span class="photo-preview-title">{{ getPhotoLabel(photoId) }}</span>
                            <el-tag v-if="photoId === form.groupPhoto.coverPhotoId" size="small"
                                type="success">封面</el-tag>
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
import { ref, reactive, watch, defineProps, defineEmits, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getPhotosByPage } from '@/api/photo'
import { addGroupPhoto, updateGroupPhoto } from '@/api/groupPhoto'
import type { WaterfallItem } from '@/types'
import type { GroupPhotoDTO, GroupPhoto } from '@/types/groupPhoto'
import { DeleteFilled, Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { get100KPhotos, processPhotoData } from '@/utils/photo'

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
        type: Object as () => GroupPhotoDTO | null,
        default: null
    }
});

const emit = defineEmits(['group-photo-added', 'group-photo-updated', 'update:modelValue', 'close']);

// 本地的对话框可见性控制
const visible = ref(props.modelValue);

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
    visible.value = newVal;
});

// 监听对话框可见性状态变化
watch(() => visible.value, (newVal) => {
    emit('update:modelValue', newVal);
    if (newVal == false) {
        emit('close')
    }
});

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
    groupPhoto: {
        id: '',
        title: '',
        introduce: '',
        coverPhotoId: '',
    },
    photoIds: [] as string[],
});

const rules = reactive<FormRules>({
    'groupPhoto.title': [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
    ],
    photoIds: [
        { type: 'array', required: true, message: '请至少选择一张照片', trigger: 'change' }
    ]
});

// 添加原始数据引用
const originalData = ref<{
    groupPhoto: GroupPhoto,
    photoIds: string[],
} | null>(null);

// 同步内外部的状态
watch(() => props.editMode, (newEditMode) => {
    if (newEditMode && props.editData) {
        // 编辑模式下，填充表单数据
        form.groupPhoto = { ...props.editData.groupPhoto };
        form.photoIds = [...props.editData.photoIds];

        // 保存原始数据用于重置
        originalData.value = {
            groupPhoto: { ...props.editData.groupPhoto },
            photoIds: [...props.editData.photoIds]
        };
    } else if (!newEditMode) {
        // 新建模式下，清空表单
        form.groupPhoto = {
            id: '',
            title: '',
            introduce: '',
            coverPhotoId: '',
        };
        form.photoIds = [];
        originalData.value = null;
    }
}, { immediate: true });

// 单独监听editData的变化
watch(() => props.editData, (newEditData) => {
    if (props.editMode && newEditData) {
        form.groupPhoto = { ...newEditData.groupPhoto };
        form.photoIds = [...newEditData.photoIds];
    }
}, { immediate: true });

// 处理拖拽结束事件
const onDragEnd = () => {
    // 当拖拽导致封面照片ID不在当前列表中时自动调整
    if (!form.photoIds.includes(form.groupPhoto.coverPhotoId)) {
        form.groupPhoto.coverPhotoId = form.photoIds[form.photoIds.length - 1] || ''
    }
}

// 照片选择相关
interface PhotoOptionItem extends WaterfallItem {
    thumbnailSrc: string;
}

const photoOptions = ref<PhotoOptionItem[]>([]);
const photoCache = ref<Map<string, PhotoOptionItem>>(new Map());
const thumbnailUrlCache = ref<Map<string, string>>(new Map());

// 获取基础照片数据
const fetchPhotos = async () => {
    loading.value = true;
    try {
        const response = await getPhotosByPage({ page: 1, pageSize: 50 });
        if (response?.records) {
            // 处理照片数据
            photoOptions.value = response.records.map((item: any) => ({
                ...processPhotoData(item),
                thumbnailSrc: ''
            }));

            // 获取缩略图
            const updatedOptions = await get100KPhotos(photoOptions.value as any, 'thumbnailSrc');

            // 更新缓存
            updatedOptions.forEach(photo => {
                photoCache.value.set(photo.id, photo as PhotoOptionItem);
                if (photo.thumbnailSrc) {
                    thumbnailUrlCache.value.set(photo.id, photo.thumbnailSrc);
                }
            });
        }
    } catch (error) {
        console.error('获取照片列表失败:', error);
        ElMessage.error('获取照片列表失败');
    } finally {
        loading.value = false;
    }
};

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

            // 处理照片数据
            photoOptions.value = filteredRecords.map((item: any) => ({
                ...processPhotoData(item),
                thumbnailSrc: thumbnailUrlCache.value.get(item.id) || ''
            }));

            // 获取缩略图
            if (filteredRecords.length > 0) {
                const updatedOptions = await get100KPhotos(photoOptions.value as any, 'thumbnailSrc');

                // 更新缓存
                updatedOptions.forEach(photo => {
                    photoCache.value.set(photo.id, photo as PhotoOptionItem);
                    if (photo.thumbnailSrc) {
                        thumbnailUrlCache.value.set(photo.id, photo.thumbnailSrc);
                    }
                });
            }
        }
    } catch (error) {
        console.error('搜索照片失败:', error);
        ElMessage.error('搜索照片失败');
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
    const removedPhotoId = form.photoIds[index];
    if (removedPhotoId === form.groupPhoto.coverPhotoId) {
        // 重置封面为第一张照片
        form.groupPhoto.coverPhotoId = form.photoIds.length > 1 ? form.photoIds[0] : '';
    }

    // 删除照片
    form.photoIds.splice(index, 1);

    // 如果没有照片了，清空封面
    if (form.photoIds.length === 0) {
        form.groupPhoto.coverPhotoId = '';
    }
};

// 提交表单
const submitForm = async () => {
    if (!formRef.value) return;

    await formRef.value.validate(async (valid, fields) => {
        if (valid) {
            try {
                const formData = form;

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
            } catch (error) {
                console.error(props.editMode ? '更新组图失败:' : '创建组图失败:', error);
                ElMessage.error(props.editMode ? '更新组图失败' : '创建组图失败');
            }
        } else {
            console.log('验证失败:', fields);
        }
    });
};

// 重置表单
const resetForm = () => {
    if (!formRef.value) return;

    if (props.editMode && originalData.value) {
        // 编辑模式下，恢复到原始数据
        form.groupPhoto = originalData.value.groupPhoto;
        form.photoIds = originalData.value.photoIds;
        formRef.value.clearValidate();
    } else {
        // 新建模式下，清空表单
        formRef.value.resetFields();
        form.groupPhoto = {
            id: '',
            title: '',
            introduce: '',
            coverPhotoId: '',
        }
        form.photoIds = [];
    }
};

// 处理关闭
const handleClose = () => {
    visible.value = false;
};

// 组件挂载时设置初始值
onMounted(() => {
    visible.value = props.modelValue;
    fetchPhotos();
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
