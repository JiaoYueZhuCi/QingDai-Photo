<template>
  <header class="header-container" ref="headerRef">
    <div class="header-content">
      <div class="logo-container" ref="logoRef">
        <router-link to="/show" class="logo-link">
          <img src="@/assets/logo.png" alt="青黛影像" class="logo-image" />
          <span class="logo-text">青黛影像</span>
        </router-link>
      </div>

      <div class="nav-container" ref="navRef">
        <router-link 
          to="/home" 
          class="nav-item" 
          ref="homeRef"
          @mouseenter="handleNavHover($event, true)"
          @mouseleave="handleNavHover($event, false)"
        >
          <i class="el-icon-home"></i>
          <span>我的摄影主页</span>
        </router-link>
      
        <router-link 
          to="/show" 
          class="nav-item" 
          ref="showRef"
          @mouseenter="handleNavHover($event, true)"
          @mouseleave="handleNavHover($event, false)"
        >
          <i class="el-icon-picture"></i>
          <span>网站介绍</span>
        </router-link>

        <a 
          href="https://gitee.com/liuziming33/qingdai-photo" 
          target="_blank"
          class="nav-item" 
          @mouseenter="handleNavHover($event, true)"
          @mouseleave="handleNavHover($event, false)"
        >
          <i class="el-icon-picture"></i>
          <span>开源项目</span>
        </a>
      </div>

      <div class="user-container" ref="userRef">
        <button 
          v-if="hasToken" 
          class="logout-button"
          @click="handleLogout"
        >
          <i class="el-icon-switch-button"></i>
          <span>注销</span>
        </button>
        <button class="theme-switch" @click="toggleTheme" ref="themeRef">
          <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useThemeStore } from '@/stores/theme';
import { Sunny, Moon } from '@element-plus/icons-vue';
import gsap from 'gsap';
import { CSSPlugin } from 'gsap/CSSPlugin';

// 注册 GSAP 插件
gsap.registerPlugin(CSSPlugin);

const router = useRouter();
const themeStore = useThemeStore();
const isDark = computed(() => themeStore.theme === 'dark');

// 引用DOM元素
const headerRef = ref<HTMLElement | null>(null);
const logoRef = ref<HTMLElement | null>(null);
const navRef = ref<HTMLElement | null>(null);
const userRef = ref<HTMLElement | null>(null);
const themeRef = ref<HTMLElement | null>(null);
const homeRef = ref<HTMLElement | null>(null);
const showRef = ref<HTMLElement | null>(null);

// 用户相关状态
const hasToken = computed(() => {
    return !!localStorage.getItem('token');
});
const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
};

const toggleTheme = () => {
  themeStore.toggleTheme();
  if (themeRef.value) {
    gsap.to(themeRef.value, {
      scale: 1.2,
      duration: 0.3,
      yoyo: true,
      repeat: 1
    });
  }
};

// 处理导航项悬停效果
const handleNavHover = (event: MouseEvent, isEnter: boolean) => {
  const target = event.currentTarget as HTMLElement;
  if (isEnter) {
    gsap.to(target, {
      opacity: 0.8,
      scale: 1.05,
      y: -2,
      duration: 0.3,
      ease: "power2.out"
    });
  } else {
    gsap.to(target, {
      opacity: 1,
      scale: 1,
      y: 0,
      duration: 0.3,
      ease: "power2.out"
    });
  }
};


// 处理导航滚动提示
const handleNavScrollHint = () => {
  const navContainer = navRef.value;
  if (!navContainer) return;

  // 延迟后执行滚动动画
  setTimeout(() => {
    // 先滚动到最右
    navContainer.scrollTo({
      left: navContainer.scrollWidth,
      behavior: 'smooth'
    });

    // 1秒后滚动回最左
    setTimeout(() => {
      navContainer.scrollTo({
        left: 0,
        behavior: 'smooth'
      });
    }, 1000);
  }, 1500);
};

// 在组件挂载时检查登录状态
onMounted(() => {
    // 创建时间线
  const tl = gsap.timeline({ defaults: { ease: "power3.out" } });

  // 设置初始状态
  if (logoRef.value && navRef.value && userRef.value) {
    gsap.set([logoRef.value, navRef.value, userRef.value], { 
      opacity: 0,
      y: -20,
      scale: 0.8
    });

    // 添加动画序列
    tl.to(logoRef.value, { 
      opacity: 1,
      y: 0,
      scale: 1,
      duration: 0.8,
      ease: "elastic.out(1, 0.5)"
    })
    .to(navRef.value, { 
      opacity: 1,
      y: 0,
      scale: 1,
      duration: 0.8,
      ease: "elastic.out(1, 0.5)"
    }, "-=0.4")
    .to(userRef.value, { 
      opacity: 1,
      y: 0,
      scale: 1,
      duration: 0.8,
      ease: "elastic.out(1, 0.5)"
    }, "-=0.4");
  }

  // 为"我的摄影主页"添加特殊引导效果
  if (homeRef.value) {
    // 创建脉冲动画
    const pulseTl = gsap.timeline({ repeat: -1 });
    pulseTl.to(homeRef.value, {
      opacity: 0.8,
      scale: 1.05,
      duration: 1,
      ease: "power2.inOut"
    })
    .to(homeRef.value, {
      opacity: 1,
      scale: 1,
      duration: 1,
      ease: "power2.inOut"
    });
  }

  // 执行导航滚动提示
  handleNavScrollHint();
});
</script>

<style scoped>
.header-container {
  position: relative;
  width: 100%;
  z-index: 1000;
  background-color: var(--qd-color-primary-light-9);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

[data-theme="dark"] .header-container,
.dark .header-container {
  background-color: var(--qd-color-primary-light-9);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

/* 移除全局 body padding */
:global(body) {
  padding-top: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0.5rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--qd-color-primary);
  font-weight: 600;
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

.logo-image {
  width: 24px;
  height: 24px;
  margin-right: 0.5rem;
}

.logo-text {
  font-size: 1rem;
  font-weight: 600;
  color: var(--qd-color-primary);
}

.nav-container {
  display: flex;
  gap: 1.5rem;
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding: 0 0.5rem;
}

.nav-container::-webkit-scrollbar {
  display: none;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: var(--qd-color-light-5);
  font-weight: 600;
  padding: 0.3rem 0.8rem;
  border-radius: 6px;
  transition: all 0.3s ease;
  position: relative;
  opacity: 1;
  transform-origin: center;
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--qd-color-primary);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.nav-item:hover::after {
  width: 80%;
}

[data-theme="dark"] .nav-item::after {
  background: var(--qd-color-dark-2);
}

.nav-item i {
  font-size: 1rem;
}

.user-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.theme-switch {
  background: var(--qd-color-primary-light-10);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  transform-origin: center;
}

[data-theme="dark"] .theme-switch,
.dark .theme-switch {
  background: var(--qd-color-dark-8);
}

.theme-switch i {
  font-size: 16px;
  color: var(--qd-color-primary);
  transition: all 0.3s ease;
  transform-origin: center;
}

[data-theme="dark"] .theme-switch i,
.dark .theme-switch i {
  color: var(--qd-color-dark-2);
}

.theme-switch:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .theme-switch:hover,
.dark .theme-switch:hover {
  box-shadow: 0 4px 15px rgba(146, 148, 174, 0.3);
}

.theme-switch:hover i {
  transform: rotate(180deg);
}

.logout-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.3rem 0.8rem;
  border-radius: 6px;
  background: var(--qd-color-primary-light-9);
  color: var(--qd-color-primary);
  border: 1px solid var(--qd-color-primary-light-5);
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

[data-theme="dark"] .logout-button,
.dark .logout-button {
  background: var(--qd-color-dark-8);
  color: var(--qd-color-dark-2);
  border-color: var(--qd-color-dark-5);
}

.logout-button:hover {
  background: var(--qd-color-primary-light-8);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

[data-theme="dark"] .logout-button:hover,
.dark .logout-button:hover {
  background: var(--qd-color-dark-7);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header-content {
    padding: 0.5rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .logo-container {
    flex-shrink: 0;
  }

  .nav-container {
    flex: 1;
    padding: 0 0.5rem;
  }

  .nav-item {
    margin-right: 0.5rem;
  }

  .user-container {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .logout-button {
    padding: 0.3rem 0.6rem;
    font-size: 0.9rem;
  }
}
</style> 