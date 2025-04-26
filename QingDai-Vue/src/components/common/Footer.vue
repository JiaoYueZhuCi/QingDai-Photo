<template>
  <footer class="site-footer">
    <p>© {{ currentYear }} <a href="/" target="_blank" class="beian-link">青黛影像</a> | 版权所有</p>
    <div class="beian-container">
      <img src="https://beian.mps.gov.cn/img/logo01.dd7ff50e.png" class="public-net-icon">
      <p class="icp-info">
        <a :href="beianInfo.link" target="_blank" class="beian-link">备案号:{{ beianInfo.number }}</a>
      </p>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue';
import { beianInfo } from '@/data/beian';

const currentYear = ref(new Date().getFullYear());
</script>

<style scoped>
.site-footer {
  position: relative;
  bottom: 0;
  left: 0;
  width: 100%;
  text-align: center;
  padding: 8px 0;
  background-color: var(--qd-color-primary-light-9);
  backdrop-filter: blur(5px);
  color: var(--qd-color-primary-light-3);
  font-size: 0.8rem;
  z-index: 100;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.05);
}
.public-net-icon {
  width: 0.8rem;
  height: 0.8rem;
  padding-right: 2px;
}
.beian-container {
  display: flex;
  align-items: center;
  justify-content: center;
}
.site-footer p {
  margin: 2px 0;
}

.icp-info {
  font-size: 0.7rem;
}

.beian-link {
  color: var(--qd-color-primary-light-3);
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.beian-link::after {
  content: '';
  position: absolute;
  width: 0;
  height: 1px;
  bottom: -1px;
  left: 0;
  background-color: var(--qd-color-primary);
  transition: width 0.3s ease;
}

.beian-link:hover {
  color: var(--qd-color-primary);
}

.beian-link:hover::after {
  width: 100%;
}

/* 添加从main.css移除的样式 */
footer {
  color: var(--qd-color-text-primary) !important;
}
</style> 