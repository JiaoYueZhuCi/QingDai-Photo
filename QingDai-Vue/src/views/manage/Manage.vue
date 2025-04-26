<template>
  <Header />
  
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
          <el-menu-item index="/manage/shareList" style="width: 100px;">
            分享管理
          </el-menu-item>
          <el-menu-item index="/manage/userInfo" style="width: 100px;">
            用户信息设置
          </el-menu-item>
          <el-menu-item index="/manage/developer" style="width: 100px;">
            开发者方法
          </el-menu-item>
          <el-menu-item class="logout" style="width: 100px;" @click="handleLogout">
            注销
          </el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
  
  
  <Footer />
</template>

<style scoped>
.el-main {
  padding: 0;
  position: relative;
}

:deep(.logout) {
  color: rgb(255, 43, 43) !important;
  background-color: rgb(255, 238, 238);
}
</style>

<script setup>
import Header from '@/components/common/Header.vue';
import Footer from '@/components/common/Footer.vue';

const handleLogout = () => {
  localStorage.clear('token');
  window.location.href = '/';
};
</script>