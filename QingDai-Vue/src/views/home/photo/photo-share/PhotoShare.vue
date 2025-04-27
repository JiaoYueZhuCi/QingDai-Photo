<template>
    <div>
        <!-- 选择模式按钮 -->
        <div class="action-button select-mode" v-if="!isShareMode" @click="toggleShareMode">
            <el-icon><Select /></el-icon>
            <span class="button-text">分享照片</span>
        </div>

        <!-- 分享模式下的按钮组 -->
        <div class="share-buttons-container" v-else>
            <!-- 取消按钮 -->
            <div class="action-button cancel-button" @click="cancelShareMode">
                <el-icon>
                    <Close />
                </el-icon>
                <span class="button-text">取消</span>
            </div>

            <!-- 确认分享按钮 -->
            <div class="action-button share-button" @click="openShareDialog">
                <el-icon>
                    <Share />
                </el-icon>
                <span class="button-text">确认分享({{ selectedPhotos.length }})</span>
            </div>
        </div>

        <!-- 分享对话框 -->
        <el-dialog v-model="shareDialogVisible" title="分享照片" width="400px" :close-on-click-modal="false">
            <div class="share-dialog-content">
                <div class="share-options">
                    <el-radio-group v-model="shareExpireDays">
                        <el-radio :value="1" label="1天" />
                        <el-radio :value="3" label="3天" />
                        <el-radio :value="7" label="7天" />
                    </el-radio-group>
                </div>
                <div class="selected-photos">
                    <div class="selected-count">已选择 {{ selectedPhotos.length }} 张照片</div>
                    <div class="photo-grid">
                        <div v-for="photo in selectedPhotos" :key="photo.id" class="selected-photo-item">
                            <el-image :src="photo.compressedSrc" fit="cover" />
                            <el-icon class="remove-icon" @click="removeSelectedPhoto(photo.id)">
                                <Close />
                            </el-icon>
                        </div>
                    </div>
                </div>
                
                <div class="share-link" v-if="shareLink">
                    <el-input v-model="shareLink" readonly>
                        <template #append>
                            <el-button @click="copyShareLink">复制</el-button>
                        </template>
                    </el-input>
                </div>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="closeShareDialog">取消</el-button>
                    <el-button type="primary" @click="showVerificationDialog"
                        :disabled="selectedPhotos.length === 0 || isGenerating">
                        {{ isGenerating ? '生成中...' : '生成链接' }}
                    </el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 添加独立的验证码对话框 -->
        <VerificationCode 
            v-model="verificationDialogVisible"
            @verified="handleVerificationResult" 
            @close="handleVerificationClose" 
        />
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElIcon, ElDialog, ElRadio, ElRadioGroup, ElImage, ElInput, ElButton, ElMessage } from 'element-plus';
import { Select, Share, Close } from '@element-plus/icons-vue';
import { createShareLink } from '@/api/share';
import type { EnhancedWaterfallItem } from '@/utils/photo';
import VerificationCode from '@/components/common/verification-code/VerificationCode.vue';

const props = defineProps({
    selectedPhotos: {
        type: Array as () => EnhancedWaterfallItem[],
        required: true
    },
    isShareMode: {
        type: Boolean,
        required: true
    }
});

const emit = defineEmits(['update:isShareMode', 'update:selectedPhotos', 'removePhoto']);

// 分享相关状态
const shareDialogVisible = ref(false);
const shareExpireDays = ref(1);
const shareLink = ref('');
const isGenerating = ref(false);
const verificationPassed = ref(false);
const verificationDialogVisible = ref(false);

// 切换分享模式
const toggleShareMode = () => {
    emit('update:isShareMode', !props.isShareMode);
};

// 打开分享对话框
const openShareDialog = () => {
    shareDialogVisible.value = true;
};

// 显示验证码对话框
const showVerificationDialog = () => {
    verificationDialogVisible.value = true;
};

// 处理验证码验证结果
const handleVerificationResult = (result: boolean) => {
    verificationPassed.value = result;
    if (result) {
        // 验证成功后生成分享链接
        generateShareLink();
    }
};

// 验证码对话框关闭回调
const handleVerificationClose = () => {
    // 如果验证未通过，则重置验证状态
    if (!verificationPassed.value) {
        verificationPassed.value = false;
    }
};

// 关闭分享对话框
const closeShareDialog = () => {
    shareDialogVisible.value = false;
    emit('update:isShareMode', false);
    emit('update:selectedPhotos', []);
    shareLink.value = '';
};

// 生成分享链接
const generateShareLink = async () => {
    if (props.selectedPhotos.length === 0) return;

    isGenerating.value = true;
    try {
        const response = await createShareLink({
            photoIds: props.selectedPhotos.map(photo => photo.id),
            expireDays: shareExpireDays.value
        });
        // 获取当前域名
        const baseUrl = window.location.origin;
        shareLink.value = `${baseUrl}/share?id=${response}`;
        ElMessage.success('分享链接生成成功');
    } catch (error) {
        console.error('生成分享链接失败:', error);
        ElMessage.error('生成分享链接失败');
        shareLink.value = '';
    } finally {
        isGenerating.value = false;
    }
};

// 复制分享链接
const copyShareLink = () => {
    if (!shareLink.value) return;
    
    // 创建临时的兼容方法，当navigator.clipboard API不可用时使用
    const fallbackCopyTextToClipboard = (text: string) => {
        try {
            // 创建临时文本区域
            const textArea = document.createElement('textarea');
            // 设置文本区域的样式，使其不可见
            textArea.style.position = 'fixed';
            textArea.style.top = '0';
            textArea.style.left = '0';
            textArea.style.width = '2em';
            textArea.style.height = '2em';
            textArea.style.padding = '0';
            textArea.style.border = 'none';
            textArea.style.outline = 'none';
            textArea.style.boxShadow = 'none';
            textArea.style.background = 'transparent';
            // 设置文本区域的值为要复制的内容
            textArea.value = text;
            // 将文本区域添加到文档
            document.body.appendChild(textArea);
            // 选中文本区域的内容
            textArea.focus();
            textArea.select();
            // 尝试执行复制命令
            const successful = document.execCommand('copy');
            if (successful) {
                ElMessage.success('链接已复制到剪贴板');
                closeShareDialog();
            } else {
                ElMessage.error('复制失败');
            }
            // 移除临时文本区域
            document.body.removeChild(textArea);
        } catch (err) {
            console.error('复制失败:', err);
            ElMessage.error('复制失败，请手动复制链接');
        }
    };
    
    // 如果支持现代Clipboard API，则使用它
    if (navigator.clipboard) {
        navigator.clipboard.writeText(shareLink.value)
            .then(() => {
                ElMessage.success('链接已复制到剪贴板');
                closeShareDialog();
            })
            .catch(() => {
                // 如果现代方法失败，回退到传统方法
                fallbackCopyTextToClipboard(shareLink.value);
            });
    } else {
        // 如果不支持现代Clipboard API，直接使用传统方法
        fallbackCopyTextToClipboard(shareLink.value);
    }
};

// 移除已选择的照片
const removeSelectedPhoto = (photoId: string) => {
    emit('removePhoto', photoId);
};

// 取消分享模式
const cancelShareMode = () => {
    emit('update:isShareMode', false);
    emit('update:selectedPhotos', []);
};
</script>

<style scoped>
.action-button {
    position: fixed;
    right: 15px;
    width: 120px;
    height: 40px;
    bottom: 20px;
    background-color: var(--qd-color-primary);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;
    z-index: 1000;
    color: white;
    gap: 8px;
}

.action-button:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.action-button .el-icon {
    font-size: 20px;
}

.button-text {
    font-size: 14px;
}

.select-mode.active {
    background-color: var(--el-color-success);
}

.share-button {
    bottom: 20px;
    background-color: #67c23a;
}

.share-dialog-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.share-options {
    display: flex;
    justify-content: center;
}

.selected-photos {
    max-height: 300px;
    overflow-y: auto;
}

.selected-count {
    margin-bottom: 10px;
    color: var(--qd-color-text-secondary);
}

.photo-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
}

.selected-photo-item {
    position: relative;
    aspect-ratio: 1;
    border-radius: 4px;
    overflow: hidden;
}

.selected-photo-item .el-image {
    width: 100%;
    height: 100%;
}

.remove-icon {
    position: absolute;
    top: 4px;
    right: 4px;
    color: white;
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    padding: 4px;
    cursor: pointer;
    transition: all 0.3s;
}

.remove-icon:hover {
    background-color: rgba(0, 0, 0, 0.8);
    transform: scale(1.1);
}

.share-link {
    margin-top: 10px;
}

.share-buttons-container {
    position: fixed;
    right: 15px;
    bottom: 20px;
    display: flex;
    gap: 10px;
    z-index: 1000;
}

.cancel-button {
    background-color: var(--el-color-danger);
    right: 150px;
}
</style> 
