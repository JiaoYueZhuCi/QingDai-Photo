<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <el-menu :default-active="$route.path" router active-text-color="#409EFF" mode="horizontal">
          <el-menu-item index="/home">
            我的主页
          </el-menu-item>
          <el-menu-item index="/manage/photoList" style="width: 100px;">
            照片管理
          </el-menu-item>
          <el-menu-item index="/manage/timelineList" style="width: 100px;">
            时间线管理
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
}
</style>