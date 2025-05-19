<template>
  <div class="user-info-container">
    <!-- 管理员信息卡片 -->
    <el-card class="user-info-card">
      <template #header>
        <div class="card-header">
          <span>摄影师信息设置</span>
        </div>
      </template>
      <el-form :model="adminForm" :rules="rules" ref="adminFormRef" label-width="100px">
        <!-- 头像上传 -->
        <el-form-item label="头像">
          <div class="avatar-uploader">
            <el-upload
              class="avatar-upload"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>
        </el-form-item>
        <!-- 背景图上传 -->
        <el-form-item label="背景图">
          <div class="background-uploader">
            <el-upload
              class="background-upload"
              :show-file-list="false"
              :before-upload="beforeBackgroundUpload"
              :http-request="handleBackgroundUpload"
            >
              <img v-if="backgroundUrl" :src="backgroundUrl" class="background" />
              <el-icon v-else class="background-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="新用户名" prop="username">
          <el-input v-model="adminForm.username" placeholder="请输入新用户名"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="adminForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="adminForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="adminForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="个人介绍" prop="description">
          <el-input v-model="adminForm.description" type="textarea" :rows="4" placeholder="请输入个人介绍"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitAdminForm">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 访客信息卡片 -->
    <el-card class="user-info-card">
      <template #header>
        <div class="card-header">
          <span>访客信息设置</span>
        </div>
      </template>
      <el-form :model="guestForm" :rules="rules" ref="guestFormRef" label-width="100px">
        <el-form-item label="新用户名" prop="username">
          <el-input v-model="guestForm.username" placeholder="请输入新用户名"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="guestForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="guestForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitGuestForm">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { updateUserInfo, getIntroduceInfo, uploadAvatar, uploadBackground, getAvatar, getBackground } from '@/api/user';
import { PHOTOGRAPHER_ID, GUEST_ID } from '@/config/user';

const adminFormRef = ref();
const guestFormRef = ref();

// 管理员表单数据
const adminForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: '',
  nickname: '',
  description: ''
});

// 访客表单数据
const guestForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
});

// 头像和背景图URL
const avatarUrl = ref('');
const backgroundUrl = ref('');

// 添加文件引用
const avatarFile = ref<File | null>(null);
const backgroundFile = ref<File | null>(null);

const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback();
  } else {
    // 根据表单类型验证对应的确认密码字段
    if (rule.field.includes('admin') && adminForm.confirmPassword !== '') {
      adminFormRef.value.validateField('confirmPassword');
    } else if (rule.field.includes('guest') && guestForm.confirmPassword !== '') {
      guestFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback();
  } else if (
    (rule.field.includes('admin') && value !== adminForm.newPassword) ||
    (rule.field.includes('guest') && value !== guestForm.newPassword)
  ) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ]
};

// 处理头像上传
const handleAvatarUpload = async (options: any) => {
  const formData = new FormData();
  formData.append('file', options.file);
  try {
    await uploadAvatar(formData);
    ElMessage.success('头像上传成功');
    // 刷新头像显示
    const response = await getAvatar();
    avatarUrl.value = URL.createObjectURL(response);
  } catch (error) {
    ElMessage.error('头像上传失败');
  }
};

// 处理背景图上传
const handleBackgroundUpload = async (options: any) => {
  const formData = new FormData();
  formData.append('file', options.file);
  try {
    await uploadBackground(formData);
    ElMessage.success('背景图上传成功');
    // 刷新背景图显示
    const response = await getBackground();
    backgroundUrl.value = URL.createObjectURL(response);
  } catch (error) {
    ElMessage.error('背景图上传失败');
  }
};

// 头像上传前的验证
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!');
    return false;
  }
  avatarFile.value = file;
  // 创建预览URL
  avatarUrl.value = URL.createObjectURL(file);
  return false; // 阻止自动上传
};

// 背景图上传前的验证
const beforeBackgroundUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('上传背景图片只能是图片格式!');
    return false;
  }
  backgroundFile.value = file;
  // 创建预览URL
  backgroundUrl.value = URL.createObjectURL(file);
  return false; // 阻止自动上传
};

// 提交管理员表单
const submitAdminForm = async () => {
  if (!adminFormRef.value) return;
  
  await adminFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      // 检查是否有任何修改
      const hasChanges = 
        adminForm.username.trim() !== '' ||
        adminForm.newPassword.trim() !== '' ||
        adminForm.nickname.trim() !== '' ||
        adminForm.description.trim() !== '' ||
        avatarFile.value !== null ||
        backgroundFile.value !== null;

      if (!hasChanges) {
        ElMessage.warning('请至少修改一项信息');
        return;
      }

      // 显示处理中的提示
      const loadingMessage = ElMessage({
        message: '正在处理，请稍候...',
        duration: 0,
        type: 'info'
      });
      
      try {
        // 先上传头像和背景图
        if (avatarFile.value) {
          const avatarFormData = new FormData();
          avatarFormData.append('file', avatarFile.value);
          await uploadAvatar(avatarFormData);
        }
        
        if (backgroundFile.value) {
          const backgroundFormData = new FormData();
          backgroundFormData.append('file', backgroundFile.value);
          await uploadBackground(backgroundFormData);
        }
        
        // 构建更新数据，只包含有修改的字段
        const updateData: any = {};
        if (adminForm.username.trim() !== '') {
          updateData.username = adminForm.username;
        }
        if (adminForm.newPassword.trim() !== '') {
          updateData.password = adminForm.newPassword;
        }
        if (adminForm.nickname.trim() !== '') {
          updateData.nickname = adminForm.nickname;
        }
        if (adminForm.description.trim() !== '') {
          updateData.description = adminForm.description;
        }

        // 只有在有需要更新的字段时才发送请求
        if (Object.keys(updateData).length > 0) {
          await updateUserInfo(PHOTOGRAPHER_ID, updateData);
        }
        
        // 关闭处理中的提示
        loadingMessage.close();
        ElMessage.success('管理员信息修改成功');
        
        // 清空所有表单数据
        adminForm.username = '';
        adminForm.newPassword = '';
        adminForm.confirmPassword = '';
        adminForm.nickname = '';
        adminForm.description = '';
        
        // 清空文件引用
        avatarFile.value = null;
        backgroundFile.value = null;
        
        // 清空预览图片
        if (avatarUrl.value) {
          URL.revokeObjectURL(avatarUrl.value);
          avatarUrl.value = '';
        }
        if (backgroundUrl.value) {
          URL.revokeObjectURL(backgroundUrl.value);
          backgroundUrl.value = '';
        }
        
        // 重置表单验证状态
        adminFormRef.value.resetFields();
      } catch (error) {
        // 关闭处理中的提示
        loadingMessage.close();
        ElMessage.error('修改失败');
      }
    }
  });
};

// 提交访客表单
const submitGuestForm = async () => {
  if (!guestFormRef.value) return;
  
  await guestFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      // 检查是否有任何修改
      const hasChanges = 
        guestForm.username.trim() !== '' ||
        guestForm.newPassword.trim() !== '';

      if (!hasChanges) {
        ElMessage.warning('请至少修改一项信息');
        return;
      }

      // 显示处理中的提示
      const loadingMessage = ElMessage({
        message: '正在处理，请稍候...',
        duration: 0,
        type: 'info'
      });
      
      try {
        // 构建更新数据，只包含有修改的字段
        const updateData: any = {};
        if (guestForm.username.trim() !== '') {
          updateData.username = guestForm.username;
        }
        if (guestForm.newPassword.trim() !== '') {
          updateData.password = guestForm.newPassword;
        }

        await updateUserInfo(GUEST_ID, updateData);
        
        // 关闭处理中的提示
        loadingMessage.close();
        ElMessage.success('访客信息修改成功');
        
        // 清空所有表单数据
        guestForm.username = '';
        guestForm.newPassword = '';
        guestForm.confirmPassword = '';
        
        // 重置表单验证状态
        guestFormRef.value.resetFields();
      } catch (error) {
        // 关闭处理中的提示
        loadingMessage.close();
        ElMessage.error('修改失败');
      }
    }
  });
};

// 组件挂载时
onMounted(() => {
});

// 组件卸载时清理预览URL
onUnmounted(() => {
  if (avatarUrl.value) {
    URL.revokeObjectURL(avatarUrl.value);
  }
  if (backgroundUrl.value) {
    URL.revokeObjectURL(backgroundUrl.value);
  }
});
</script>

<style scoped>
.user-info-container {
  background-color: var(--qd-color-bg-dark);
  padding: 20px;
  /* min-height: 80vh; */
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin: 0 auto;
}

.user-info-card {
  height: 100%;
  transition: all 0.3s;
}

.user-info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .user-info-container {
    grid-template-columns: 1fr;
  }
}

.avatar-uploader,
.background-uploader {
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-upload,
.background-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-upload:hover,
.background-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon,
.background-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.background {
  width: 200px;
  height: 100px;
  display: block;
  object-fit: cover;
}
</style> 