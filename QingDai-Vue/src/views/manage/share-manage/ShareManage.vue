<template>
  <div class="share-manage-container">
    <el-button class="refresh-button" type="primary" @click="refreshShares">刷新分享列表</el-button>

    <el-table :data="shareList" stripe style="width: 100%"
      :empty-text="'暂无分享数据'" :max-height="tableHeight">
      <el-table-column type="expand">
        <template #default="props">
          <div class="photo-ids-expanded">
            <p class="expanded-title">照片ID列表 ({{ props.row.photoIds ? props.row.photoIds.length : 0 }} 张)</p>
            <div class="photo-ids-tags">
              <el-tag v-for="photoId in props.row.photoIds" :key="photoId" size="small"
                style="margin-right: 5px; margin-bottom: 5px;">
                {{ photoId }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="分享ID" width="150" fixed="left"></el-table-column>
      <el-table-column label="照片数量" width="100">
        <template #default="scope">
          <span class="photo-count">{{ scope.row.photoIds ? scope.row.photoIds.length : 0 }} 张</span>
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

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
        @current-change="handleCurrentChange" @size-change="handleSizeChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { getAllShares, deleteShare, getSharesByPage } from '@/api/share';
import { ManagePagination } from '@/config/pagination';

// 数据状态
const loading = ref(false);
const shareList = ref([]);
const tableHeight = ref(window.innerHeight - 65);
const currentPage = ref(1);
const pageSize = ref(ManagePagination.SHARE_MANAGE_PAGE_SIZE);
const total = ref(0);

// 生命周期钩子
onMounted(() => {
  fetchShares();
});

// 获取分享数据
const fetchShares = async () => {
  loading.value = true;
  
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '加载分享中...',
    background: 'rgba(0, 0, 0, 0.7)',
    fullscreen: true
  });
  
  try {
    const response = await getSharesByPage({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    
    if (response && response.records) {
      shareList.value = response.records;
      total.value = response.total;
    } else {
      shareList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取分享列表失败:', error);
    ElMessage.error('获取分享列表失败');
  } finally {
    loading.value = false;
    loadingInstance.close();
  }
};

// 刷新分享列表
const refreshShares = () => {
  fetchShares();
};

// 分页事件处理
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchShares();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
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

.photo-count {
  font-weight: 500;
}

.photo-ids-expanded {
  margin: 0;
  padding: 10px 20px;
  background-color: #f9fafc;
  border-left: 4px solid #409eff;
}

.expanded-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #606266;
}

.photo-ids-tags {
  display: flex;
  flex-wrap: wrap;
  padding: 5px;
  max-height: 200px;
  overflow-y: auto;
}

.refresh-button {
  position: fixed;
  right: 20px;
  top: 70px;
  z-index: 100;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}
</style>