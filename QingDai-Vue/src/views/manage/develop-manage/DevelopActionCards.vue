<template>
  <div class="develop-container">
    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>待处理图片导入数据库</span>
        </div>
      </template>
      <div class="card-content">
        <el-button type="primary" @click="handleFullSizePhotoToMysql">
          执行导入
        </el-button>
        <div class="description">
          将Pending目录中的所有图片信息导入到数据库
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>压缩待处理图片到指定大小</span>
        </div>
      </template>
      <div class="card-content">
        <div class="action-group">
          <el-button type="primary" @click="handleThumbnailImages">
            执行压缩
          </el-button>
          <div class="param-group">
            <div class="param-item">
            <el-switch v-model="thumbnailOverwrite" active-text="覆盖" inactive-text="不覆盖" class="param-switch" />
            </div>
            <div class="param-item">
              最大文件大小:
              <el-input-number v-model="maxSizeKB" :min="10" :max="10240" class="param-input" />
              (KB)
            </div>
          </div>
        </div>
        <div class="description">
          将Pending目录中的图片按指定大小压缩到Thumbnail目录
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>待处理图片上传数据库并压缩</span>
        </div>
      </template>
      <div class="card-content">
        <div class="action-group">
          <el-button type="primary" @click="handleProcessPendingPhotos">
            执行处理
          </el-button>
          <div class="param-item">
          <el-switch v-model="processOverwrite" active-text="覆盖" inactive-text="不覆盖" class="param-switch" />
          </div>
        </div>
        <div class="description">
          将Pending目录中的图片压缩到Thumbnail目录,并更新数据库
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>验证数据库照片文件存在性</span>
        </div>
      </template>
      <div class="card-content">
        <div class="action-group">
          <el-button type="primary" @click="handleValidatePhotoExistence">
            开始检测
          </el-button>
        </div>
        <div class="description">
          检查数据库中的照片记录，验证原图、100K压缩图和1000K压缩图文件是否存在
        </div>
        <div class="result-summary">
          <template v-if="photoExistenceResult">
            <p>总照片数: {{ photoExistenceResult.totalCount }}</p>
            <p>缺失原图: {{ photoExistenceResult.missingFullSize }}</p>
            <p>缺失100K压缩图: {{ photoExistenceResult.missing100K }}</p>
            <p>缺失1000K压缩图: {{ photoExistenceResult.missing1000K }}</p>
            <el-collapse v-if="photoExistenceResult.missingDetails && photoExistenceResult.missingDetails.length > 0">
              <el-collapse-item>
                <template #title>
                  缺失详情 ({{ photoExistenceResult.missingDetails.length }})
                </template>
                <div class="missing-details">
                  <p v-for="(item, index) in photoExistenceResult.missingDetails" :key="index">{{ item }}</p>
                </div>
              </el-collapse-item>
            </el-collapse>

            <!-- 添加删除丢失照片记录的按钮 -->
            <div class="action-container"
              v-if="photoExistenceResult.missingDetails && photoExistenceResult.missingDetails.length > 0">
              <el-button type="danger" @click="handleDeleteMissingPhotoRecords" class="delete-missing-button">
                一键删除丢失(原图、100K、1000K)图片的数据库记录
              </el-button>
            </div>

            <!-- 显示删除结果 -->
            <div v-if="missingPhotoDeleteResult" class="result-summary delete-result">
              <h3>删除结果</h3>
              <p>总共删除: {{ missingPhotoDeleteResult.totalDeleted }} 条记录</p>

              <el-collapse
                v-if="missingPhotoDeleteResult.deletedRecords && missingPhotoDeleteResult.deletedRecords.length > 0">
                <el-collapse-item>
                  <template #title>
                    成功删除的记录 ({{ missingPhotoDeleteResult.deletedRecords.length }})
                  </template>
                  <div class="file-list">
                    <div v-for="(item, index) in missingPhotoDeleteResult.deletedRecords" :key="index"
                      class="file-item success-item">
                      {{ item }}
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>

              <el-collapse
                v-if="missingPhotoDeleteResult.errorRecords && missingPhotoDeleteResult.errorRecords.length > 0">
                <el-collapse-item>
                  <template #title>
                    删除失败的记录 ({{ missingPhotoDeleteResult.errorRecords.length }})
                  </template>
                  <div class="file-list">
                    <div v-for="(item, index) in missingPhotoDeleteResult.errorRecords" :key="index"
                      class="file-item error-item">
                      {{ item }}
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </template>
          <template v-else>
            <p>总照片数: <span class="pending-data">待检测</span></p>
            <p>缺失原图: <span class="pending-data">待检测</span></p>
            <p>缺失100K压缩图: <span class="pending-data">待检测</span></p>
            <p>缺失1000K压缩图: <span class="pending-data">待检测</span></p>

            <!-- 添加删除丢失照片记录的按钮（未检测前也显示） -->
            <div class="action-container">
              <el-button type="danger" @click="handleDeleteMissingPhotoRecords" class="delete-missing-button"
                disabled>
                一键删除丢失(原图、100K、1000K)图片的数据库记录
              </el-button>
            </div>
          </template>
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>验证文件系统照片存在于数据库</span>
        </div>
      </template>
      <div class="card-content">
        <div class="action-group">
          <el-button type="primary" @click="handleValidateFileSystemPhotos">
            开始检测
          </el-button>
        </div>
        <div class="description">
          检查文件系统中的照片文件，验证是否在数据库中有对应记录
        </div>
        <div class="result-summary">
          <template v-if="fileSystemPhotosResult">
            <p>数据库照片数: {{ fileSystemPhotosResult.dbPhotoCount }}</p>
            <p>原图目录文件数: {{ fileSystemPhotosResult.fullSizeCount }}</p>
            <p>100K压缩图目录文件数: {{ fileSystemPhotosResult.thumbnail100KCount }}</p>
            <p>1000K压缩图目录文件数: {{ fileSystemPhotosResult.thumbnail1000KCount }}</p>

            <el-collapse
              v-if="fileSystemPhotosResult.fullSizeNotInDb && fileSystemPhotosResult.fullSizeNotInDb.length > 0">
              <el-collapse-item>
                <template #title>
                  原图不在数据库中 ({{ fileSystemPhotosResult.fullSizeNotInDb.length }})
                </template>
                <div class="list-header">
                  <el-input v-model="fullSizeFilter" placeholder="搜索文件名" prefix-icon="el-icon-search" clearable
                    size="small" class="filter-input" />
                </div>
                <div class="file-list">
                  <div v-for="(item, index) in paginatedFullSizeFiles" :key="index" class="file-item">
                    {{ item }}
                  </div>
                </div>
                <div class="pagination-container">
                  <el-pagination layout="prev, pager, next" :total="filteredFullSizeFiles.length"
                    :page-size="pageSize" :current-page="fullSizePage"
                    @current-change="(page: number) => fullSizePage = page" small />
                </div>
              </el-collapse-item>
            </el-collapse>

            <el-collapse
              v-if="fileSystemPhotosResult.thumbnail100KNotInDb && fileSystemPhotosResult.thumbnail100KNotInDb.length > 0">
              <el-collapse-item>
                <template #title>
                  100K压缩图不在数据库中 ({{ fileSystemPhotosResult.thumbnail100KNotInDb.length }})
                </template>
                <div class="list-header">
                  <el-input v-model="thumbnail100KFilter" placeholder="搜索文件名" prefix-icon="el-icon-search" clearable
                    size="small" class="filter-input" />
                </div>
                <div class="file-list">
                  <div v-for="(item, index) in paginatedThumbnail100KFiles" :key="index" class="file-item">
                    {{ item }}
                  </div>
                </div>
                <div class="pagination-container">
                  <el-pagination layout="prev, pager, next" :total="filteredThumbnail100KFiles.length"
                    :page-size="pageSize" :current-page="thumbnail100KPage"
                    @current-change="(page: number) => thumbnail100KPage = page" small />
                </div>
              </el-collapse-item>
            </el-collapse>

            <el-collapse
              v-if="fileSystemPhotosResult.thumbnail1000KNotInDb && fileSystemPhotosResult.thumbnail1000KNotInDb.length > 0">
              <el-collapse-item>
                <template #title>
                  1000K压缩图不在数据库中 ({{ fileSystemPhotosResult.thumbnail1000KNotInDb.length }})
                </template>
                <div class="list-header">
                  <el-input v-model="thumbnail1000KFilter" placeholder="搜索文件名" prefix-icon="el-icon-search" clearable
                    size="small" class="filter-input" />
                </div>
                <div class="file-list">
                  <div v-for="(item, index) in paginatedThumbnail1000KFiles" :key="index" class="file-item">
                    {{ item }}
                  </div>
                </div>
                <div class="pagination-container">
                  <el-pagination layout="prev, pager, next" :total="filteredThumbnail1000KFiles.length"
                    :page-size="pageSize" :current-page="thumbnail1000KPage"
                    @current-change="(page: number) => thumbnail1000KPage = page" small />
                </div>
              </el-collapse-item>
            </el-collapse>

            <!-- 添加删除按钮和删除结果 -->
            <div class="action-container"
              v-if="fileSystemPhotosResult &&
                ((fileSystemPhotosResult.fullSizeNotInDb && fileSystemPhotosResult.fullSizeNotInDb.length > 0) ||
                  (fileSystemPhotosResult.thumbnail100KNotInDb && fileSystemPhotosResult.thumbnail100KNotInDb.length > 0) ||
                  (fileSystemPhotosResult.thumbnail1000KNotInDb && fileSystemPhotosResult.thumbnail1000KNotInDb.length > 0))">
              <el-button type="danger" @click="handleDeletePhotosNotInDatabase">
                一键删除未在数据库中记录的图片
              </el-button>
            </div>

            <!-- 显示删除结果 -->
            <div v-if="deleteResult" class="result-summary delete-result">
              <h3>删除结果</h3>
              <p>总共删除: {{ deleteResult.totalDeleted }} 个文件</p>
              <p>删除原图: {{ deleteResult.deletedFullSize }} 个</p>
              <p>删除100K压缩图: {{ deleteResult.deleted100K }} 个</p>
              <p>删除1000K压缩图: {{ deleteResult.deleted1000K }} 个</p>

              <el-collapse v-if="deleteResult.deletedFiles && deleteResult.deletedFiles.length > 0">
                <el-collapse-item>
                  <template #title>
                    成功删除的文件 ({{ deleteResult.deletedFiles.length }})
                  </template>
                  <div class="file-list">
                    <div v-for="(item, index) in deleteResult.deletedFiles" :key="index"
                      class="file-item success-item">
                      {{ item }}
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>

              <el-collapse v-if="deleteResult.errorFiles && deleteResult.errorFiles.length > 0">
                <el-collapse-item>
                  <template #title>
                    删除失败的文件 ({{ deleteResult.errorFiles.length }})
                  </template>
                  <div class="file-list">
                    <div v-for="(item, index) in deleteResult.errorFiles" :key="index" class="file-item error-item">
                      {{ item }}
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </template>
          <template v-else>
            <p>数据库照片数: <span class="pending-data">待检测</span></p>
            <p>原图目录文件数: <span class="pending-data">待检测</span></p>
            <p>100K压缩图目录文件数: <span class="pending-data">待检测</span></p>
            <p>1000K压缩图目录文件数: <span class="pending-data">待检测</span></p>

            <!-- 添加删除按钮（未检测前也显示） -->
            <div class="action-container">
              <el-button type="danger" @click="handleDeletePhotosNotInDatabase" disabled>
                一键删除未在数据库中记录的图片
              </el-button>
            </div>
          </template>
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>token查验用户信息</span>
        </div>
      </template>
      <div class="card-content">
        <div class="action-group">
          <el-input v-model="debugToken" placeholder="请输入token进行查验" class="debug-input" />
          <el-button type="primary" @click="handleDebugUserInfo">
            开始检测
          </el-button>
        </div>
        <div class="description">
          查验token的用户信息
        </div>
        <div class="result-summary">
          <template v-if="userInfoResult">
            <h3>用户基本信息</h3>
            <p>id: {{ userInfoResult.user.id }}</p>
            <p>用户名: {{ userInfoResult.user.username }}</p>
            <p>密码: {{ userInfoResult.user.password }}</p>
            <p>状态: {{ userInfoResult.user.status === 1 ? '正常' : '禁用' }}</p>
            <p>创建时间: {{ userInfoResult.user.createdTime }}</p>
            <p>更新时间: {{ userInfoResult.user.updatedTime }}</p>

            <h3>用户角色</h3>
            <div class="role-list">
              <el-tag v-for="(role, index) in userInfoResult.roles" :key="index" type="success" class="role-tag">
                {{ role }}
              </el-tag>
            </div>

            <h3>用户权限</h3>
            <div class="permission-list">
              <el-tag v-for="(permission, index) in userInfoResult.permissions" :key="index" type="info"
                class="permission-tag">
                {{ permission }}
              </el-tag>
            </div>
          </template>
          <template v-else>
            <h3>用户基本信息</h3>
            <p>用户ID: <span class="pending-data">待检测</span></p>
            <p>用户名: <span class="pending-data">待检测</span></p>
            <p>密码: <span class="pending-data">待检测</span></p>
            <p>状态: <span class="pending-data">待检测</span></p>
            <p>创建时间: <span class="pending-data">待检测</span></p>
            <p>更新时间: <span class="pending-data">待检测</span></p>

            <h3>用户角色</h3>
            <p><span class="pending-data">待检测</span></p>

            <h3>用户权限</h3>
            <p><span class="pending-data">待检测</span></p>
          </template>
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>相机型号替换</span>
        </div>
      </template>
      <div class="card-content">
        <div class="equipment-manager">
          <el-button type="primary" @click="loadCameras" :loading="loadingCameras" class="load-button">
            获取相机型号列表
          </el-button>
          
          <div class="equipment-list-container">
            <div v-if="cameras.length > 0" class="equipment-list">
              <div class="list-header">
                <span>共有 {{ cameras.length }} 种相机型号</span>
              </div>
              <div class="list-content">
                <div v-for="camera in cameras" :key="camera" 
                     class="list-item"
                     :class="{ 'selected': selectedCamera === camera }"
                     @click="handleCameraSelect(camera)">
                  <span class="item-name">{{ camera }}</span>
                  <span v-if="selectedCamera === camera" class="item-count">
                    使用数量: {{ cameraCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="empty-list">
              <span>暂无相机型号数据</span>
            </div>
          </div>

          <div class="equipment-update">
            <el-input v-model="newCameraName" placeholder="输入新的相机型号名称" :disabled="!selectedCamera" />
            <el-button type="primary" @click="handleUpdateCamera" :disabled="!selectedCamera || !newCameraName">
              更新名称
            </el-button>
          </div>
        </div>
        <div class="description">
          点击按钮获取相机型号列表，选择要修改的相机型号
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>镜头型号替换</span>
        </div>
      </template>
      <div class="card-content">
        <div class="equipment-manager">
          <el-button type="primary" @click="loadLenses" :loading="loadingLenses" class="load-button">
            获取镜头型号列表
          </el-button>
          
          <div class="equipment-list-container">
            <div v-if="lenses.length > 0" class="equipment-list">
              <div class="list-header">
                <span>共有 {{ lenses.length }} 种镜头型号</span>
              </div>
              <div class="list-content">
                <div v-for="lens in lenses" :key="lens" 
                     class="list-item"
                     :class="{ 'selected': selectedLens === lens }"
                     @click="handleLensSelect(lens)">
                  <span class="item-name">{{ lens }}</span>
                  <span v-if="selectedLens === lens" class="item-count">
                    使用数量: {{ lensCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="empty-list">
              <span>暂无镜头型号数据</span>
            </div>
          </div>

          <div class="equipment-update">
            <el-input v-model="newLensName" placeholder="输入新的镜头型号名称" :disabled="!selectedLens" />
            <el-button type="primary" @click="handleUpdateLens" :disabled="!selectedLens || !newLensName">
              更新名称
            </el-button>
          </div>
        </div>
        <div class="description">
          点击按钮获取镜头型号列表，选择要修改的镜头型号
        </div>
      </div>
    </el-card>

    <el-card class="develop-card">
      <template #header>
        <div class="card-header">
          <span>焦距替换</span>
        </div>
      </template>
      <div class="card-content">
        <div class="equipment-manager">
          <el-button type="primary" @click="loadFocalLengths" :loading="loadingFocalLengths" class="load-button">
            获取焦距列表
          </el-button>
          
          <div class="equipment-list-container">
            <div v-if="focalLengths.length > 0" class="equipment-list">
              <div class="list-header">
                <span>共有 {{ focalLengths.length }} 种焦距</span>
              </div>
              <div class="list-content">
                <div v-for="focalLength in focalLengths" :key="focalLength" 
                     class="list-item"
                     :class="{ 'selected': selectedFocalLength === focalLength }"
                     @click="handleFocalLengthSelect(focalLength)">
                  <span class="item-name">{{ focalLength }}</span>
                  <span v-if="selectedFocalLength === focalLength" class="item-count">
                    使用数量: {{ focalLengthCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="empty-list">
              <span>暂无焦距数据</span>
            </div>
          </div>

          <div class="equipment-update">
            <el-input v-model="newFocalLength" placeholder="输入新的焦距值" :disabled="!selectedFocalLength" />
            <el-button type="primary" @click="handleUpdateFocalLength" :disabled="!selectedFocalLength || !newFocalLength">
              更新焦距
            </el-button>
          </div>
        </div>
        <div class="description">
          点击按钮获取焦距列表，选择要修改的焦距值
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fullSizePhotoToMysql, thumbnailImages, processPendingPhotos, validatePhotoExistence, validateFileSystemPhotos, deletePhotosNotInDatabase, deleteMissingPhotoRecords, getAllCameras, getAllLenses, updateCameraName, updateLensName, getPhotoCountByCamera, getPhotoCountByLens, getAllFocalLengths, updateFocalLengthValue, getPhotoCountByFocalLength } from '@/api/photo';
import { getUserInfo } from '@/api/user';

// 图片处理相关
const thumbnailOverwrite = ref(false);
const processOverwrite = ref(false);
const maxSizeKB = ref(1024);

// 验证结果
interface PhotoExistenceResult {
  totalCount: number;
  missingFullSize: number;
  missing100K: number;
  missing1000K: number;
  missingDetails: string[];
}

interface FileSystemPhotosResult {
  dbPhotoCount: number;
  fullSizeCount: number;
  thumbnail100KCount: number;
  thumbnail1000KCount: number;
  fullSizeNotInDb: string[];
  thumbnail100KNotInDb: string[];
  thumbnail1000KNotInDb: string[];
}

interface MissingPhotoDeleteResult {
  totalDeleted: number;
  deletedRecords: string[];
  errorRecords: string[];
}

const photoExistenceResult = ref<PhotoExistenceResult | null>(null);
const fileSystemPhotosResult = ref<FileSystemPhotosResult | null>(null);
const deleteResult = ref<any | null>(null);
const missingPhotoDeleteResult = ref<MissingPhotoDeleteResult | null>(null);

// Token查验
const debugToken = ref('');
const userInfoResult = ref<any | null>(null);

// 文件过滤和分页
const fullSizeFilter = ref('');
const thumbnail100KFilter = ref('');
const thumbnail1000KFilter = ref('');
const pageSize = ref(10);
const fullSizePage = ref(1);
const thumbnail100KPage = ref(1);
const thumbnail1000KPage = ref(1);

// 设备管理
const cameras = ref<string[]>([]);
const lenses = ref<string[]>([]);
const selectedCamera = ref('');
const selectedLens = ref('');
const newCameraName = ref('');
const newLensName = ref('');
const cameraCount = ref<number | null>(null);
const lensCount = ref<number | null>(null);
const loadingCameras = ref(false);
const loadingLenses = ref(false);
const focalLengths = ref<string[]>([]);
const selectedFocalLength = ref('');
const newFocalLength = ref('');
const focalLengthCount = ref<number | null>(null);
const loadingFocalLengths = ref(false);

// 图片处理方法
const handleFullSizePhotoToMysql = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将把原图目录中的图片信息导入数据库，是否继续？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const response = await fullSizePhotoToMysql();
    ElMessage.success(response || '操作成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error as Error).message);
    }
  }
};

const handleThumbnailImages = async () => {
  try {
    await ElMessageBox.confirm(
      `此操作将压缩Pending目录图片到Thumbnail目录，${thumbnailOverwrite.value ? '并覆盖已存在的文件' : '不覆盖已存在的文件'}，最大文件大小为${maxSizeKB.value}KB，是否继续？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const response = await thumbnailImages(maxSizeKB.value, thumbnailOverwrite.value);
    ElMessage.success(response || '操作成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error as Error).message);
    }
  }
};

const handleProcessPendingPhotos = async () => {
  try {
    await ElMessageBox.confirm(
      `此操作将压缩Pending目录图片到Thumbnail目录,并更新数据库，${processOverwrite.value ? '并覆盖已存在的文件' : '不覆盖已存在的文件'}，是否继续？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const response = await processPendingPhotos(processOverwrite.value);
    ElMessage.success(response || '操作成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error as Error).message);
    }
  }
};

// 验证方法
const handleValidatePhotoExistence = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将检查数据库中的照片记录在文件系统中是否存在，可能需要一些时间，是否继续？',
      '确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    );

    // 清空之前的结果
    photoExistenceResult.value = null;

    // 调用API
    const response = await validatePhotoExistence();
    photoExistenceResult.value = response;

    ElMessage.success('验证完成！');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('验证失败：' + (error as Error).message);
    }
  }
};

const handleValidateFileSystemPhotos = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将检查文件系统中的照片文件在数据库中是否有记录，可能需要一些时间，是否继续？',
      '确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    );

    // 清空之前的结果
    fileSystemPhotosResult.value = null;

    // 清空过滤和分页
    fullSizeFilter.value = '';
    thumbnail100KFilter.value = '';
    thumbnail1000KFilter.value = '';
    fullSizePage.value = 1;
    thumbnail100KPage.value = 1;
    thumbnail1000KPage.value = 1;

    // 调用API
    const response = await validateFileSystemPhotos();
    fileSystemPhotosResult.value = response;

    ElMessage.success('验证完成！');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('验证失败：' + (error as Error).message);
    }
  }
};

const handleDeletePhotosNotInDatabase = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将删除文件系统中存在但数据库中没有记录的所有照片文件，删除后无法恢复，是否继续？',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true,
        closeOnClickModal: false
      }
    );

    // 清空之前的结果
    deleteResult.value = null;

    // 调用API
    const response = await deletePhotosNotInDatabase();
    deleteResult.value = response;

    // 刷新验证结果
    await handleValidateFileSystemPhotos();

    ElMessage.success('删除操作完成！');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error as Error).message);
    }
  }
};

const handleDeleteMissingPhotoRecords = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将删除数据库中丢失了全部三种图片（原图、100K和1000K）的记录，删除后无法恢复，是否继续？',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true,
        closeOnClickModal: false
      }
    );

    // 清空之前的结果
    missingPhotoDeleteResult.value = null;

    // 调用API
    const response = await deleteMissingPhotoRecords();
    missingPhotoDeleteResult.value = response;

    // 刷新验证结果
    await handleValidatePhotoExistence();

    ElMessage.success('删除操作完成！');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error as Error).message);
    }
  }
};

// 计算属性：文件过滤和分页
const filteredFullSizeFiles = computed(() => {
  if (!fileSystemPhotosResult.value || !fileSystemPhotosResult.value.fullSizeNotInDb) {
    return [];
  }
  const filter = fullSizeFilter.value.toLowerCase();
  if (!filter) {
    return fileSystemPhotosResult.value.fullSizeNotInDb;
  }
  return fileSystemPhotosResult.value.fullSizeNotInDb.filter(
    (item: string) => item.toLowerCase().includes(filter)
  );
});

const paginatedFullSizeFiles = computed(() => {
  const start = (fullSizePage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredFullSizeFiles.value.slice(start, end);
});

const filteredThumbnail100KFiles = computed(() => {
  if (!fileSystemPhotosResult.value || !fileSystemPhotosResult.value.thumbnail100KNotInDb) {
    return [];
  }
  const filter = thumbnail100KFilter.value.toLowerCase();
  if (!filter) {
    return fileSystemPhotosResult.value.thumbnail100KNotInDb;
  }
  return fileSystemPhotosResult.value.thumbnail100KNotInDb.filter(
    (item: string) => item.toLowerCase().includes(filter)
  );
});

const paginatedThumbnail100KFiles = computed(() => {
  const start = (thumbnail100KPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredThumbnail100KFiles.value.slice(start, end);
});

const filteredThumbnail1000KFiles = computed(() => {
  if (!fileSystemPhotosResult.value || !fileSystemPhotosResult.value.thumbnail1000KNotInDb) {
    return [];
  }
  const filter = thumbnail1000KFilter.value.toLowerCase();
  if (!filter) {
    return fileSystemPhotosResult.value.thumbnail1000KNotInDb;
  }
  return fileSystemPhotosResult.value.thumbnail1000KNotInDb.filter(
    (item: string) => item.toLowerCase().includes(filter)
  );
});

const paginatedThumbnail1000KFiles = computed(() => {
  const start = (thumbnail1000KPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredThumbnail1000KFiles.value.slice(start, end);
});

// Token查验
const handleDebugUserInfo = async () => {
  try {
    if (!debugToken.value) {
      ElMessage.warning('请输入token');
      return;
    }

    // 如果用户输入的token包含Bearer前缀，则去除它
    let tokenToUse = debugToken.value;
    if (tokenToUse.startsWith('Bearer ')) {
      tokenToUse = tokenToUse.substring(7);
    }

    const response = await getUserInfo(tokenToUse);
    userInfoResult.value = response;
    ElMessage.success('获取用户信息成功');
    console.log('用户信息:', response);
  } catch (error: any) {
    userInfoResult.value = null;
    if (error.response && error.response.status === 401) {
      ElMessage.error('Token验证失败：无效的token');
    } else {
      ElMessage.error('获取用户信息失败：' + (error as Error).message);
    }
  }
};

// 设备管理方法
const loadCameras = async () => {
  try {
    loadingCameras.value = true;
    cameras.value = await getAllCameras();
    ElMessage.success('获取相机型号列表成功');
  } catch (error) {
    ElMessage.error('获取相机列表失败：' + (error as Error).message);
  } finally {
    loadingCameras.value = false;
  }
};

const loadLenses = async () => {
  try {
    loadingLenses.value = true;
    lenses.value = await getAllLenses();
    ElMessage.success('获取镜头型号列表成功');
  } catch (error) {
    ElMessage.error('获取镜头列表失败：' + (error as Error).message);
  } finally {
    loadingLenses.value = false;
  }
};

const handleCameraSelect = async (camera: string) => {
  try {
    selectedCamera.value = camera;
    cameraCount.value = await getPhotoCountByCamera(camera);
  } catch (error) {
    ElMessage.error('获取相机照片数量失败：' + (error as Error).message);
  }
};

const handleLensSelect = async (lens: string) => {
  try {
    selectedLens.value = lens;
    lensCount.value = await getPhotoCountByLens(lens);
  } catch (error) {
    ElMessage.error('获取镜头照片数量失败：' + (error as Error).message);
  }
};

const handleUpdateCamera = async () => {
  if (!selectedCamera.value || !newCameraName.value) {
    ElMessage.warning('请选择要修改的相机并输入新名称');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要将相机型号"${selectedCamera.value}"改为"${newCameraName.value}"吗？这将影响${cameraCount.value}张照片。`,
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await updateCameraName(selectedCamera.value, newCameraName.value);
    ElMessage.success('相机型号更新成功');
    await loadCameras();
    selectedCamera.value = newCameraName.value;
    newCameraName.value = '';
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('更新相机型号失败：' + (error as Error).message);
    }
  }
};

const handleUpdateLens = async () => {
  if (!selectedLens.value || !newLensName.value) {
    ElMessage.warning('请选择要修改的镜头并输入新名称');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要将镜头型号"${selectedLens.value}"改为"${newLensName.value}"吗？这将影响${lensCount.value}张照片。`,
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await updateLensName(selectedLens.value, newLensName.value);
    ElMessage.success('镜头型号更新成功');
    await loadLenses();
    selectedLens.value = newLensName.value;
    newLensName.value = '';
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('更新镜头型号失败：' + (error as Error).message);
    }
  }
};

const loadFocalLengths = async () => {
  try {
    loadingFocalLengths.value = true;
    const response = await getAllFocalLengths();
    focalLengths.value = response;
    ElMessage.success('获取焦距列表成功');
  } catch (error) {
    ElMessage.error('获取焦距列表失败：' + (error as Error).message);
  } finally {
    loadingFocalLengths.value = false;
  }
};

const handleFocalLengthSelect = async (focalLength: string) => {
  try {
    selectedFocalLength.value = focalLength;
    const response = await getPhotoCountByFocalLength(focalLength);
    focalLengthCount.value = response;
  } catch (error) {
    ElMessage.error('获取焦距使用数量失败：' + (error as Error).message);
  }
};

const handleUpdateFocalLength = async () => {
  if (!selectedFocalLength.value || !newFocalLength.value) {
    ElMessage.warning('请选择要修改的焦距并输入新值');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要将焦距"${selectedFocalLength.value}"改为"${newFocalLength.value}"吗？这将影响${focalLengthCount.value}张照片。`,
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await updateFocalLengthValue(selectedFocalLength.value, newFocalLength.value);
    ElMessage.success('焦距更新成功');
    await loadFocalLengths();
    selectedFocalLength.value = newFocalLength.value;
    newFocalLength.value = '';
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('更新焦距失败：' + (error as Error).message);
    }
  }
};
</script>

<style scoped>
.develop-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.develop-card {
  border-radius: 6px;
  background-color: var(--qd-color-bg-light);
  border: 1px solid var(--qd-color-border);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__body) {
  padding: 0 !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: var(--qd-color-text-primary);
}

.card-content {
  padding: 10px;
  color: var(--qd-color-text-regular);
}

.description {
  margin-top: 10px;
  font-size: 14px;
  color: var(--qd-color-text-secondary);
}

.action-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

/* 添加按钮换行样式 */
:deep(.el-button) {
  white-space: normal;
  height: auto;
  padding: 8px 15px;
  line-height: 1.5;
  text-align: center;
}

/* 处理特长按钮 */
.delete-missing-button, 
:deep(.action-container .el-button) {
  max-width: 100%;
  word-break: break-word;
}

.param-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-left: 10px;
  gap: 10px;
}

.param-item {
  display: flex;
  align-items: center;
  margin-right: 10px;
  color: var(--qd-color-text-regular);
}

.param-switch {
  margin-right: 10px;
}

.param-input {
  width: 150px;
  margin: 0 5px;
}

.result-summary {
  margin-top: 15px;
  font-size: 14px;
  color: var(--qd-color-text-primary);
}

.pending-data {
  color: var(--qd-color-text-secondary);
  font-style: italic;
}

.missing-details {
  font-size: 13px;
  max-height: 200px;
  overflow-y: auto;
  color: var(--qd-color-text-regular);
}

.delete-result {
  margin-top: 15px;
  padding: 10px;
  border-radius: 5px;
  background-color: var(--qd-color-bg);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.delete-result h3 {
  margin-top: 0;
  color: var(--qd-color-text-primary);
}

.delete-missing-button {
  margin-top: 10px;
}

.file-list {
  max-height: 300px;
  overflow-y: auto;
}

.file-item {
  padding: 5px 10px;
  font-size: 12px;
  color: var(--qd-color-text-regular);
  background-color: var(--qd-color-bg);
  margin-bottom: 2px;
  border-radius: 3px;
}

.success-item {
  color: #67c23a;
}

.error-item {
  color: #f56c6c;
}

.list-header {
  margin-bottom: 10px;
}

.filter-input {
  width: 200px;
}

.pagination-container {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

.action-container {
  margin-top: 10px;
}

/* 装备管理相关样式 */
.equipment-manager {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
}

.equipment-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.equipment-info p {
  margin: 0;
  color: var(--qd-color-text-primary);
  text-align: center;
}

.equipment-update {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.load-button {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

.equipment-update .el-input {
  flex: 1;
}

.equipment-list-container {
  height: 250px;
  border: 1px solid var(--qd-color-border);
  border-radius: 4px;
  background-color: var(--qd-color-bg);
  overflow: hidden;
}

.equipment-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 8px 12px;
  background-color: var(--qd-color-bg-dark);
  border-bottom: 1px solid var(--qd-color-border);
  font-size: 14px;
  color: var(--qd-color-text-primary);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.list-item {
  padding: 8px 12px;
  border-bottom: 1px solid var(--qd-color-border-light);
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--qd-color-text-regular);
}

.list-item:last-child {
  border-bottom: none;
}

.list-item:hover {
  background-color: var(--qd-color-hover);
}

.list-item.selected {
  background-color: var(--qd-color-hover);
  color: var(--qd-color-primary);
}

.item-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-count {
  font-size: 12px;
  color: var(--qd-color-text-secondary);
  margin-left: 10px;
}

.empty-list {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--qd-color-text-secondary);
  font-size: 14px;
}

.role-tag,
.permission-tag {
  margin-right: 6px;
  margin-bottom: 6px;
  font-size: 12px;
}

.role-list,
.permission-list {
  display: flex;
  flex-wrap: wrap;
  margin-top: 5px;
  margin-bottom: 10px;
}

.debug-input {
  width: 100%;
}

/* 日夜模式适配 */
:deep(.el-collapse-item__header),
:deep(.el-collapse-item__content) {
  background-color: var(--qd-color-bg-light);
  color: var(--qd-color-text-primary);
  border-color: var(--qd-color-border);
}

:deep(.el-collapse) {
  border-color: var(--qd-color-border);
}

:deep(.el-pagination) {
  --el-pagination-bg-color: var(--qd-color-bg-light);
  --el-pagination-text-color: var(--qd-color-text-regular);
  --el-pagination-button-color: var(--qd-color-text-primary);
  --el-pagination-hover-color: var(--qd-color-primary);
}

@media (max-width: 768px) {
  .develop-container {
    grid-template-columns: 1fr;
  }
}

.result-summary p{
  margin: 0;
}

.result-summary h3{
  margin: 3px 0;
}
</style> 