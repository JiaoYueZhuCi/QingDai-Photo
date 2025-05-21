<template>
  <Header />
  
  <div class="manage">
    <el-container>
      <el-header>
        <el-menu :default-active="$route.path" router mode="horizontal">
          <el-menu-item index="/home">
            我的主页
          </el-menu-item>
          <el-menu-item index="/manage/photoList" style="width: 100px;">
            照片编辑
          </el-menu-item>
          <el-menu-item index="/manage/groupPhotosList" style="width: 100px;">
            组图编辑
          </el-menu-item>
          <el-menu-item index="/manage/timelineList" style="width: 100px;">
            时间线编辑
          </el-menu-item>
          <el-menu-item index="/manage/shareList" style="width: 100px;">
            分享编辑
          </el-menu-item>
          <el-menu-item index="/manage/userInfo" style="width: 100px;">
            用户设置
          </el-menu-item>
          <el-menu-item index="/manage/control" style="width: 100px;">
            照片管理
          </el-menu-item>
          <el-menu-item index="/manage/view" style="width: 100px;">
            浏览数据
          </el-menu-item>
          <el-menu-item index="/manage/develop" style="width: 100px;">
            开发
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


</style>

<script setup>
import Header from '@/components/common/header/Header.vue';
import Footer from '@/components/common/footer/Footer.vue';

</script>