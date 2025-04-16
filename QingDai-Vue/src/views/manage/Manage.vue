<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <el-menu :default-active="$route.path" router mode="horizontal">
          <el-menu-item index="/home">
            我的主页
          </el-menu-item>
          <el-menu-item index="/manage/photoList" style="width: 100px;">
            照片管理
          </el-menu-item>
          <el-menu-item index="/manage/groupPhotosList" style="width: 100px;">
            组图管理
          </el-menu-item>
          <el-menu-item index="/manage/timelineList" style="width: 100px;">
            时间线管理
          </el-menu-item>
          <el-menu-item style="width: 100px; color: #F56C6C;" @click="handleLogout">
            注销
          </el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.el-main {
  padding: 0;
  position: relative;
}
</style>

<script setup>
const handleLogout = () => {
  localStorage.clear('token');
  window.location.href = '/';
};
</script>