<template>
  <el-dialog
    v-model="dialogVisible"
    title="编辑照片信息"
    width="70%"
    :before-close="handleClose"
    class="photo-editor-dialog"
    :append-to-body="true"
    center
    align-center
  >
    <div class="editor-container">
      <div class="preview-image">
        <el-image
          :src="photoInfo.compressedSrc"
          fit="contain"
          :style="{ maxHeight: '70vh' }"
          :preview-src-list="[]"
          v-loading="imageLoading"
          element-loading-text="正在加载图片..."
          element-loading-background="rgba(69, 70, 94, 0.7)"
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
              <div>图片加载失败</div>
            </div>
          </template>
        </el-image>
      </div>
      <div class="editor-form-container">
        <el-form :model="form" label-width="80px" class="editor-form">
            <div class="form-section-title">照片参数</div>
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="请输入照片标题" />
          </el-form-item>
          <el-form-item label="介绍">
            <el-input
              v-model="form.introduce"
              type="textarea"
              :rows="3"
              placeholder="请输入照片介绍"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.start" placeholder="请选择照片状态">
              <el-option :value="1" label="星标照片">
                <div style="display: flex; align-items: center">
                  <el-icon style="color: gold; margin-right: 8px">
                    <StarFilled />
                  </el-icon>
                  <span>星标照片</span>
                </div>
              </el-option>
              <el-option :value="0" label="普通照片">
                <div style="display: flex; align-items: center">
                  <el-icon style="color: var(--qd-color-primary); margin-right: 8px">
                    <StarFilled />
                  </el-icon>
                  <span>普通照片</span>
                </div>
              </el-option>
              <el-option :value="2" label="气象照片">
                <div style="display: flex; align-items: center">
                  <el-icon style="color: darkturquoise; margin-right: 8px">
                    <StarFilled />
                  </el-icon>
                  <span>气象照片</span>
                </div>
              </el-option>
              <el-option :value="-1" label="隐藏照片">
                <div style="display: flex; align-items: center">
                  <el-icon style="color: var(--qd-color-primary-light-8); margin-right: 8px">
                    <Star />
                  </el-icon>
                  <span>隐藏照片</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          
          <div class="divider"></div>
          
          <div class="form-section-title">相机参数</div>
          <el-form-item label="相机">
            <el-input v-model="form.camera" placeholder="相机型号" />
          </el-form-item>
          <el-form-item label="镜头">
            <el-input v-model="form.lens" placeholder="镜头型号" />
          </el-form-item>
          <el-form-item label="光圈">
            <el-input v-model="form.aperture" placeholder="光圈值" />
          </el-form-item>
          <el-form-item label="快门">
            <el-input v-model="form.shutter" placeholder="快门速度" />
          </el-form-item>
          <el-form-item label="ISO">
            <el-input v-model="form.iso" placeholder="ISO值" />
          </el-form-item>
        </el-form>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue';
import { ElMessage, ElDialog, ElButton, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElImage, ElIcon } from 'element-plus';
import { Star, StarFilled, Picture } from '@element-plus/icons-vue';
import { getPhotoDetailInfo, processPhotoData, type EnhancedWaterfallItem, get100KPhoto } from '@/utils/photo';
import { updatePhotoInfo } from '@/api/photo';

const props = defineProps<{
  photoId: string,
  modelValue: boolean
}>();

const emit = defineEmits(['update:modelValue', 'updated']);

// 对话框显示状态
const dialogVisible = ref(props.modelValue);

// 监听props.modelValue变化
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val;
  if (val) {
    loadPhotoData();
  }
});

// 监听dialogVisible变化
watch(() => dialogVisible.value, (val) => {
  emit('update:modelValue', val);
});

// 照片信息
const photoInfo = ref<EnhancedWaterfallItem>({} as EnhancedWaterfallItem);
const submitting = ref(false);
const imageLoading = ref(false);

// 表单数据
const form = reactive({
  id: '',
  title: '',
  introduce: '',
  start: 0,
  camera: '',
  lens: '',
  aperture: '',
  shutter: '',
  iso: '',
  fileName: '',
  author: '',
  width: 0,
  height: 0,
  time: ''
});

// 加载照片数据
const loadPhotoData = async () => {
  try {
    console.log(props.photoId);
    
    // 加载照片基本信息
    const data = await getPhotoDetailInfo(props.photoId);
    if (data) {
      photoInfo.value = processPhotoData(data);
      
      // 填充表单
      form.id = data.id;
      form.title = data.title || '';
      form.introduce = data.introduce || '';
      form.start = data.start !== undefined ? data.start : 0;
      form.camera = data.camera || '';
      form.lens = data.lens || '';
      form.aperture = data.aperture || '';
      form.shutter = data.shutter || '';
      form.iso = data.iso || '';
      form.fileName = data.fileName || '';
      form.author = data.author || '';
      form.width = data.width || 0;
      form.height = data.height || 0;
      form.time = data.time || '';
      
      // 加载照片缩略图
      const photo = await get100KPhoto(data.id, (loading) => {
        imageLoading.value = loading;
      });
      
      if (photo) {
        photoInfo.value.compressedSrc = photo.url;
      }
    }
  } catch (error) {
    console.error('获取照片详情失败:', error);
    ElMessage.error('获取照片详情失败');
    handleClose();
  }
};

// 提交表单
const submitForm = async () => {
  submitting.value = true;
  try {
    await updatePhotoInfo({
      id: form.id,
      title: form.title,
      introduce: form.introduce,
      start: form.start,
      camera: form.camera,
      lens: form.lens,
      aperture: form.aperture,
      shutter: form.shutter,
      iso: form.iso,
      fileName: form.fileName,
      author: form.author,
      width: form.width,
      height: form.height,
      time: form.time
    });
    
    ElMessage.success('更新照片信息成功');
    emit('updated', form); // 发送更新成功事件
    handleClose();
  } catch (error) {
    console.error('更新照片信息失败:', error);
    ElMessage.error('更新照片信息失败');
  } finally {
    submitting.value = false;
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
};
</script>

<style scoped>
.photo-editor-dialog {
  --el-dialog-padding-primary: 10px;
}

.photo-editor-dialog :deep(.el-dialog) {
  margin: 0 auto !important;
  height: auto;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.photo-editor-dialog :deep(.el-dialog__body) {
  padding-top: 0;
  flex: 1;
  overflow: hidden;
}

.photo-editor-dialog :deep(.el-dialog__header) {
  padding-bottom: 10px;
  margin-right: 0;
}

.editor-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
  overflow: hidden;
  border-radius: 8px;
}

.preview-image {
  display: flex;
  justify-content: center;
  background-color: var(--qd-color-primary-light-10);
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.preview-image::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  z-index: 0;
}

.preview-image :deep(.el-image) {
  position: relative;
  z-index: 1;
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--qd-color-primary);
  width: 100%;
  height: 200px;
  position: relative;
  z-index: 1;
}

.image-error .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
  color: var(--qd-color-primary-light-5);
}

.editor-form-container {
  height: 70vh;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.editor-form-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  z-index: 0;
}

.editor-form {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.divider {
  height: 1px;
  background-color: var(--qd-color-primary-light-7);
  margin: 10px 0 15px 0;
}

.form-section-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--qd-color-primary);
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid var(--qd-color-primary);
}

:deep(.el-form-item__label) {
  color: var(--qd-color-primary-light-2);
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select) {
  box-shadow: 0 0 0 1px var(--qd-color-primary-light-6) !important;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover),
:deep(.el-select:hover) {
  box-shadow: 0 0 0 1px var(--qd-color-primary-light-4) !important;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus),
:deep(.el-select.is-focus) {
  box-shadow: 0 0 0 1px var(--qd-color-primary) !important;
}

@media (min-width: 768px) {
  .editor-container {
    flex-direction: row;
  }
  
  .preview-image {
    flex: 0 0 40%;
    height: auto;
  }
  
  .editor-form-container {
    flex: 1;
    overflow-y: auto;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-button) {
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-dialog__title) {
  color: var(--qd-color-primary);
  font-weight: bold;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--qd-color-primary);
}
</style> 