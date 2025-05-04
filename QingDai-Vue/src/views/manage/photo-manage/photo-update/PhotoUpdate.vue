<template>
    <el-dialog v-model="visible" width="972px" @close="handleClose">
        <div class="dialog-content">
            <span class="uploadD">
                <el-upload class="upload" drag :auto-upload="false" :on-change="uploadFile" :disabled="uploadLoading"
                    :show-file-list="false" multiple accept="image/jpeg, image/png">
                    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                    <div class="el-upload__text">
                        点击或拖拽照片到此处上传
                    </div>
                    <template #tip>
                        <div class="el-upload__tip">
                            支持单次上传多个照片文件
                        </div>
                    </template>
                </el-upload>
            </span>

            <div class="file-preview-area">
                <div v-for="(file, index) in fileList" :key="file.name" class="preview-item">
                    <el-image :src="previewUrls[index]" :preview-src-list="previewUrls" fit="cover"
                        class="preview-image" />
                    <div class="file-info">
                        <span class="file-name">{{ file.name }}</span>
                        <el-icon class="delete-icon" @click="handleRemove(index)">
                            <Delete />
                        </el-icon>
                    </div>
                </div>
            </div>

            <!-- 上传进度组件 -->
            <div class="progress-container">
                <upload-progress :messageId="messageId" @close="showProgress = false"
                    @complete="handleUploadComplete" />
            </div>

            <div class="submit-area">
                <el-select v-model="startValue" placeholder="选择照片级别" style="margin-right: 10px;">
                    <el-option label="精选" :value="1">
                        <el-tag :type="'warning'">精选</el-tag>
                    </el-option>
                    <el-option label="普通" :value="0">
                        <el-tag :type="'success'">普通</el-tag>
                    </el-option>
                    <el-option label="私密" :value="-1">
                        <el-tag :type="'info'">隐藏</el-tag>
                    </el-option>
                    <el-option label="气象" :value="2">
                        <el-tag :type="'primary'">气象</el-tag>
                    </el-option>
                </el-select>
                <el-button @click="clearFileList" :disabled="uploadLoading || !fileList.length">
                    清空照片列表
                </el-button>
                <el-button type="primary" @click="handleSubmit" :disabled="uploadLoading || !fileList.length">
                    提交全部照片
                </el-button>
            </div>
        </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { UploadFilled, Delete } from '@element-plus/icons-vue'
import { ref, defineProps, defineEmits, watch, defineExpose, onMounted } from 'vue';
import { processPhotosFromFrontend } from '@/api/photo';
import { clearPhotoDB } from '@/utils/indexedDB';
import UploadProgress from '../photo-upload-progress/UploadProgress.vue';

const props = defineProps<{
    modelValue: boolean
}>()

const visible = ref(props.modelValue)
const emit = defineEmits(['photo-uploaded', 'update:modelValue', 'close'])

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

// 组件挂载时设置初始值
onMounted(() => {
    visible.value = props.modelValue;
});

const startValue = ref(0);

const uploadLoading = ref(false);
const previewUrls = ref<string[]>([]);
const fileList = ref<any[]>([]);
const messageId = ref('photo-upload-progress');
const showProgress = ref(true);

const handleRemove = (index: number) => {
    fileList.value.splice(index, 1);
    URL.revokeObjectURL(previewUrls.value[index]);
    previewUrls.value.splice(index, 1);
};

const uploadFile = (file: any) => {
    const allowedExtensions = ['jpg', 'jpeg', 'png'];
    const fileExtension = file.name.split('.').pop().toLowerCase();

    if (!allowedExtensions.includes(fileExtension) || !file.raw.type.startsWith('image/')) {
        ElMessage.error('仅支持 JPG/JPEG/PNG 格式的图片文件');
        return false;
    }

    previewUrls.value.push(URL.createObjectURL(file.raw));
    fileList.value.push(file.raw);
    return false; // 阻止自动上传

};

const clearFileList = () => {
    fileList.value.forEach((_, index) => {
        URL.revokeObjectURL(previewUrls.value[index]);
    });
    fileList.value = [];
    previewUrls.value = [];
};

const handleClose = () => {
    clearFileList();
    visible.value = false;
};

const handleUploadComplete = () => {
    uploadLoading.value = false;
    handleClose();
    // 发送照片上传完成事件
    emit('photo-uploaded');
};

const handleSubmit = async () => {
    if (!fileList.value.length) {
        ElMessage.warning('请先选择要上传的照片');
        return;
    }

    const formData = new FormData();
    fileList.value.forEach(file => {
        formData.append('files', file);
    });
    formData.append('start', startValue.value.toString());
    formData.append('overwrite', 'true');

    try {
        uploadLoading.value = true;

        // 发送上传请求
        const response = await processPhotosFromFrontend(formData);

        if (response) {
            // 获取消息ID并显示进度条
            messageId.value = response.messageId;
            // 显示成功消息
            ElMessage.success(`成功提交${response.files || 0}张照片任务,请等待处理完成`);
        }
    } catch (error) {
        console.error('上传失败:', error);
        ElMessage.error('文件上传失败');
        uploadLoading.value = false;
    }
};
</script>

<style scoped>
.dialog-content {
    position: relative;
    min-height: 200px;
}

.file-preview-area {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 15px;
}

.preview-item {
    position: relative;
    width: 150px;
    height: 150px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-image {
    width: 100%;
    height: 100%;
}

.file-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.6);
    padding: 3px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.file-name {
    color: white;
    font-size: 12px;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    word-break: break-all;
}

.delete-icon {
    color: white;
    cursor: pointer;
    margin-left: auto;
    flex-shrink: 0;
}

.preview-item {
    position: relative;
    width: 180px;
    min-height: 180px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.upload {
    width: 560px;
    /* min-height: 400px; */
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.submit-area {
    display: flex;
    justify-content: right;
}

.uploadD {
    display: flex;
    justify-content: center;
}

.el-upload__tip {
    display: flex;
    justify-content: center;
}

.progress-container {
    margin: 20px 0;
}
</style>
