<template>
  <div class="share-manage-container">
    <el-button class="refresh-button" type="primary" @click="refreshShares">刷新分享列表</el-button>

    <el-table :data="shareList" stripe style="width: 100%"
      :empty-text="loading ? '加载中...' : '暂无分享数据'" :max-height="tableHeight">
      <el-table-column prop="id" label="分享ID" width="150" fixed="left"></el-table-column>
      <el-table-column label="照片" width="130">
        <template #default="scope">
          <div class="photo-ids-container">
            <div>
              <span class="photo-count">{{ scope.row.photoIds ? scope.row.photoIds.length : 0 }} 张</span>
              <el-button type="text" @click="togglePhotoIds(scope.row.id)">
                {{ expandedRows.includes(scope.row.id) ? '收起' : '展开' }}
              </el-button>
            </div>
            <div v-if="expandedRows.includes(scope.row.id)" class="photo-ids-list">
              <el-tag v-for="photoId in scope.row.photoIds" :key="photoId" size="small"
                style="margin-right: 5px; margin-bottom: 5px;">
                {{ photoId }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170"></el-table-column>
      <el-table-column prop="expireTime" label="过期时间" width="170"></el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.isExpired ? 'danger' : 'success'">
            {{ scope.row.isExpired ? '已过期' : '有效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分享链接" min-width="280">
        <template #default="scope">
          <div class="share-link">
            <el-input :value="getShareUrl(scope.row.id)" readonly></el-input>
            <el-button type="primary" size="small" @click="copyShareLink(scope.row.id)">复制</el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="scope">
          <el-popconfirm title="确定要删除这个分享吗？" @confirm="handleDelete(scope.row.id)" confirm-button-text="确定"
            cancel-button-text="取消">
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { getAllShares, deleteShare } from '@/api/share';

// 数据状态
const loading = ref(false);
const shareList = ref([]);
const expandedRows = ref([]);
const tableHeight = ref(window.innerHeight - 65)

// 生命周期钩子
onMounted(() => {
  fetchShares();
});

// 获取所有分享
const fetchShares = async () => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '加载分享中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  
  try {
    const response = await getAllShares();
    shareList.value = response || [];
  } catch (error) {
    console.error('获取分享列表失败:', error);
    ElMessage.error('获取分享列表失败');
  } finally {
    loadingInstance.close();
  }
};

// 刷新分享列表
const refreshShares = () => {
  fetchShares();
};

// 获取分享URL
const getShareUrl = (shareId) => {
  return `${window.location.origin}/share?id=${shareId}`;
};

// 复制分享链接
const copyShareLink = async (shareId) => {
  const shareUrl = getShareUrl(shareId);
  try {
    await navigator.clipboard.writeText(shareUrl);
    ElMessage.success('分享链接已复制到剪贴板');
  } catch (err) {
    console.error('复制失败:', err);
    ElMessage.error('复制失败，请手动复制');
  }
};

// 切换展开/收起photoIds
const togglePhotoIds = (shareId) => {
  const index = expandedRows.value.indexOf(shareId);
  if (index > -1) {
    expandedRows.value.splice(index, 1);
  } else {
    expandedRows.value.push(shareId);
  }
};

// 删除分享
const handleDelete = async (shareId) => {
  try {
    await deleteShare(shareId);
    ElMessage.success('删除分享成功');
    // 刷新列表
    fetchShares();
  } catch (error) {
    console.error('删除分享失败:', error);
    ElMessage.error('删除分享失败');
  }
};
</script>

<style scoped>
.share-manage-container {
  height: 100%;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.share-link {
  display: flex;
  align-items: center;
}

.share-link .el-input {
  margin-right: 10px;
}

.photo-ids-container {
  display: flex;
  flex-direction: column;
}

.photo-count {
  margin-right: 10px;
}

.photo-ids-list {
  margin-top: 8px;
  max-height: 150px;
  overflow-y: auto;
  padding: 5px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f9fafc;
}

.refresh-button {
  position: fixed;
  right: 20px;
  top: 15px;
}
</style>