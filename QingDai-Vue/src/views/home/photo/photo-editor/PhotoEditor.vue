<template>
  <el-dialog v-model="visible" title="编辑照片信息" width="70%" :before-close="handleClose" class="photo-editor-dialog"
    :append-to-body="true" center align-center>
    <div class="editor-container">
      <div class="preview-image">
        <el-image :src="photoInfo.compressedSrc" fit="contain" :style="{ maxHeight: '70vh' }" :preview-src-list="[]"
          v-loading="imageLoading" element-loading-text="正在加载图片..." element-loading-background="rgba(69, 70, 94, 0.7)">
          <template #error>
            <div class="image-error">
              <el-icon>
                <Picture />
              </el-icon>
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
            <el-input v-model="form.introduce" type="textarea" :rows="3" placeholder="请输入照片介绍" />
          </el-form-item>
          <el-form-item label="等级">
            <el-select v-model="form.startRating" placeholder="请选择照片等级">
              <el-option :value="PhotoStarRating.STAR" :label="getStarLabel(PhotoStarRating.STAR)">
                <div style="display: flex; align-items: center">
                  <el-icon :style="{ color: getStarColor(PhotoStarRating.STAR), marginRight: '8px' }">
                    <StarFilled />
                  </el-icon>
                  <span>{{ getStarLabel(PhotoStarRating.STAR) }}</span>
                </div>
              </el-option>
              <el-option :value="PhotoStarRating.NORMAL" :label="getStarLabel(PhotoStarRating.NORMAL)">
                <div style="display: flex; align-items: center">
                  <el-icon :style="{ color: getStarColor(PhotoStarRating.NORMAL), marginRight: '8px' }">
                    <StarFilled />
                  </el-icon>
                  <span>{{ getStarLabel(PhotoStarRating.NORMAL) }}</span>
                </div>
              </el-option>
              <el-option :value="PhotoStarRating.METEOROLOGY" :label="getStarLabel(PhotoStarRating.METEOROLOGY)">
                <div style="display: flex; align-items: center">
                  <el-icon :style="{ color: getStarColor(PhotoStarRating.METEOROLOGY), marginRight: '8px' }">
                    <StarFilled />
                  </el-icon>
                  <span>{{ getStarLabel(PhotoStarRating.METEOROLOGY) }}</span>
                </div>
              </el-option>
              <el-option :value="PhotoStarRating.GROUP_ONLY" :label="getStarLabel(PhotoStarRating.GROUP_ONLY)">
                <div style="display: flex; align-items: center">
                  <el-icon :style="{ color: getStarColor(PhotoStarRating.GROUP_ONLY), marginRight: '8px' }">
                    <StarFilled />
                  </el-icon>
                  <span>{{ getStarLabel(PhotoStarRating.GROUP_ONLY) }}</span>
                </div>
              </el-option>
              <el-option :value="PhotoStarRating.HIDDEN" :label="getStarLabel(PhotoStarRating.HIDDEN)">
                <div style="display: flex; align-items: center">
                  <el-icon :style="{ color: getStarColor(PhotoStarRating.HIDDEN), marginRight: '8px' }">
                    <Star />
                  </el-icon>
                  <span>{{ getStarLabel(PhotoStarRating.HIDDEN) }}</span>
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
          <el-form-item label="焦距">
            <el-input v-model="form.focalLength" placeholder="焦距" />
          </el-form-item>
        </el-form>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="danger" @click="showDeleteConfirm = true">删除</el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 删除确认对话框 -->
  <el-dialog v-model="showDeleteConfirm" title="确认删除" width="30%" :append-to-body="true">
    <span>确定要删除这张照片吗？此操作不可恢复。</span>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showDeleteConfirm = false">取消</el-button>
        <el-button type="danger" @click="handleDelete" :loading="submitting">
          确认删除
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive, onMounted } from 'vue';
import { ElMessage, ElDialog, ElButton, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElImage, ElIcon } from 'element-plus';
import { Star, StarFilled, Picture } from '@element-plus/icons-vue';
import { getPhotoDetailInfo, processPhotoData, type EnhancedWaterfallItem, get100KPhoto } from '@/utils/photo';
import { PhotoStarRating, getStarColor, getStarLabel } from '@/config/photo';
import { updatePhotoInfo, deletePhotoById } from '@/api/photo';

const props = defineProps<{
  modelValue: boolean;
  photoId: string
}>();

const emit = defineEmits(['updated', 'update:modelValue', 'close', 'deleted']);

// 内部对话框可见性状态
const visible = ref(props.modelValue);

// 照片信息
const photoInfo = ref<EnhancedWaterfallItem>({} as EnhancedWaterfallItem);
const submitting = ref(false);
const imageLoading = ref(false);
const showDeleteConfirm = ref(false);
const isDataLoaded = ref(false);

// 表单数据
const form = reactive({
  title: '',
  introduce: '',
  startRating: 0,
  camera: '',
  lens: '',
  aperture: '',
  shutter: '',
  iso: '',
  focalLength: ''
});

// 加载照片数据
const loadPhotoData = async () => {
  if (!props.photoId || isDataLoaded.value) return;

  imageLoading.value = true;
  try {
    const photoData = await getPhotoDetailInfo(props.photoId);
    if (photoData) {
      photoInfo.value = photoData;

      // 填充表单数据
      form.title = photoData.title || '';
      form.introduce = photoData.introduce || '';
      form.startRating = photoData.startRating || 0;
      form.camera = photoData.camera || '';
      form.lens = photoData.lens || '';
      form.aperture = photoData.aperture || '';
      form.shutter = photoData.shutter || '';
      form.iso = photoData.iso || '';
      form.focalLength = photoData.focalLength || '';

      // 加载缩略图
      const thumbnailResult = await get100KPhoto(props.photoId);
      if (thumbnailResult) {
        photoInfo.value.compressedSrc = thumbnailResult.url;
      }
      isDataLoaded.value = true;
    }
  } catch (error) {
    console.error('加载照片数据失败:', error);
    ElMessage.error('加载照片数据失败');
  } finally {
    imageLoading.value = false;
  }
};

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal;
  if (newVal && props.photoId) {
    loadPhotoData();
  } else {
    isDataLoaded.value = false;
  }
});

// 监听对话框可见性状态变化
watch(() => visible.value, (newVal) => {
  emit('update:modelValue', newVal);
  if (newVal == false) {
    emit('close');
    isDataLoaded.value = false;
  }
});

// 监听photoId变化
watch(() => props.photoId, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    isDataLoaded.value = false;
    if (newVal && visible.value) {
      loadPhotoData();
    }
  }
});

// 组件挂载时设置初始状态
onMounted(() => {
  visible.value = props.modelValue;
});

// 处理关闭
const handleClose = () => {
  visible.value = false;
};

// 提交表单
const submitForm = async () => {
  if (!props.photoId) return;

  submitting.value = true;
  try {
    // 更新照片信息
    await updatePhotoInfo({
      id: props.photoId,
      title: form.title,
      introduce: form.introduce,
      startRating: form.startRating,
      camera: form.camera,
      lens: form.lens,
      aperture: form.aperture,
      shutter: form.shutter,
      iso: form.iso,
      focalLength: form.focalLength,
      fileName: photoInfo.value.fileName || '',
      author: photoInfo.value.author || '',
      width: photoInfo.value.width || 0,
      height: photoInfo.value.height || 0,
      shootTime: photoInfo.value.shootTime || ''
    });

    // 更新本地数据
    photoInfo.value = {
      ...photoInfo.value,
      title: form.title,
      introduce: form.introduce,
      startRating: form.startRating,
      camera: form.camera,
      lens: form.lens,
      aperture: form.aperture,
      shutter: form.shutter,
      iso: form.iso,
      focalLength: form.focalLength
    };

    ElMessage.success('保存成功');
    handleClose();
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitting.value = false;
  }
};

// 处理删除
const handleDelete = async () => {
  if (!props.photoId) return;

  submitting.value = true;
  try {
    await deletePhotoById(props.photoId);
    ElMessage.success('删除成功');
    showDeleteConfirm.value = false;
    handleClose();
    emit('deleted', props.photoId);
  } catch (error) {
    console.error('删除失败:', error);
    ElMessage.error('删除失败');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.photo-editor-dialog {
  display: flex;
  flex-direction: column;
}

/* 添加夜间模式下的对话框样式 */
.dark :deep(.el-dialog) {
  background-color: var(--qd-color-primary-dark-9);
  border: 1px solid var(--qd-color-primary-dark-7);
}

.dark :deep(.el-dialog__title) {
  color: var(--qd-color-primary-light-8);
}

.dark :deep(.el-dialog__body) {
  color: var(--qd-color-primary-light-7);
}

.editor-container {
  display: flex;
  max-height: 70vh;
  overflow: hidden;
  gap: 20px;
}

.preview-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--qd-color-bg-light);
}

.preview-image :deep(.el-image) {
  max-width: 100%;
  height: 70vh;
  object-fit: contain;
}

/* 添加夜间模式下的预览区域样式 */
.dark .preview-image {
  background-color: var(--qd-color-primary-dark-8);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: var(--qd-color-text-secondary);
}

/* 添加夜间模式下的错误提示样式 */
.dark .image-error {
  color: var(--qd-color-primary-light-6);
}

.editor-form-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}

.editor-form {
  width: 100%;
}

/* 添加夜间模式下的表单项样式 */
.dark :deep(.el-form-item__label) {
  color: var(--qd-color-primary-light-8);
}

.dark :deep(.el-input__inner) {
  background-color: var(--qd-color-primary-dark-7);
  border-color: var(--qd-color-primary-dark-6);
  color: var(--qd-color-primary-light-8);
}

.dark :deep(.el-textarea__inner) {
  background-color: var(--qd-color-primary-dark-7);
  border-color: var(--qd-color-primary-dark-6);
  color: var(--qd-color-primary-light-8);
}

.dark :deep(.el-select-dropdown) {
  background-color: var(--qd-color-primary-dark-8);
  border: 1px solid var(--qd-color-primary-dark-6);
}

.dark :deep(.el-select-dropdown__item) {
  color: var(--qd-color-primary-light-8);
}

.dark :deep(.el-select-dropdown__item.hover), 
.dark :deep(.el-select-dropdown__item:hover) {
  background-color: var(--qd-color-primary-dark-6);
}

.dark :deep(.el-select-dropdown__item.selected) {
  color: var(--qd-color-primary-light-8);
  background-color: var(--qd-color-primary-dark-5);
}

.form-section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 10px 0;
  color: var(--qd-color-primary);
  border-left: 3px solid var(--qd-color-primary);
  padding-left: 10px;
}

/* 添加夜间模式下的表单标题样式 */
.dark .form-section-title {
  color: var(--qd-color-primary-light-8);
  border-left: 3px solid var(--qd-color-primary-light-5);
}

.divider {
  height: 1px;
  background-color: var(--qd-color-border-light);
  margin: 15px 0;
}

/* 添加夜间模式下的分隔线样式 */
.dark .divider {
  background-color: var(--qd-color-primary-dark-6);
}

/* 使对话框在移动设备上更友好 */
@media (max-width: 768px) {
  .editor-container {
    flex-direction: column;
  }
  
  .preview-image {
    max-height: 50vh;
  }
  
  .preview-image :deep(.el-image) {
    max-height: 40vh;
  }
}
</style>