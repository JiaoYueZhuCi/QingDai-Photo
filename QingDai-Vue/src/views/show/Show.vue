<template>
  <div class="show-container" ref="containerRef">
    <!-- 添加切换主题按钮 -->
    <button class="theme-switch" @click="toggleTheme">
      <el-icon>
        <Sunny v-if="isDark" />
        <Moon v-else />
      </el-icon>
    </button>

    <!-- 英雄区域 -->
    <HeroSection ref="heroSectionRef" />

    <div class="animation-container">
      <div class="buttons-container" ref="buttonsRef">
        <router-link to="/home" class="nav-button primary-button">
          <i class="el-icon-home"></i> 我的摄影主页
        </router-link>
        <a href="https://github.com/JiaoYueZhuCi/QingDai-Photo" target="_blank" class="nav-button secondary-button">
          <i class="el-icon-share"></i> 开源项目
        </a>
      </div>

      <!-- 功能卡片区域 -->
      <FeatureCards ref="featureCardsRef" />

      <!-- 页面展示区域 -->
      <PageShowcase ref="pageShowcaseRef" />
    </div>

    <!-- 装饰性元素 -->
    <DecorativeElements ref="decorativeElementsRef" />
  </div>

  <!-- Footer组件 -->
  <Footer />
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import gsap from 'gsap';
import HeroSection from './hero-section/HeroSection.vue';
import FeatureCards from './feature-cards/FeatureCards.vue';
import PageShowcase from './page-showcase/PageShowcase.vue';
import DecorativeElements from './decorative-elements/DecorativeElements.vue';
import Footer from '@/components/common/footer/Footer.vue';
import { useThemeStore } from '@/stores/theme';
import { Sunny, Moon } from '@element-plus/icons-vue';

// 主题相关
const themeStore = useThemeStore();
const isDark = computed(() => themeStore.theme === 'dark');
const toggleTheme = () => {
  themeStore.toggleTheme();
};

// 各组件引用
const containerRef = ref(null);
const heroSectionRef = ref(null);
const buttonsRef = ref(null);
const featureCardsRef = ref(null);
const pageShowcaseRef = ref(null);
const decorativeElementsRef = ref(null);

// 动画延迟时间
const animationDelay = 500;

onMounted(() => {
  // 初始化各组件的动画
  heroSectionRef.value.initAnimations();
  gsap.set('.animation-container', { opacity: 0, y: 50 });
  featureCardsRef.value.initAnimations();
  pageShowcaseRef.value.initAnimations();
  decorativeElementsRef.value.initAnimations();

  // 创建时间线动画
  const tl = gsap.timeline({ defaults: { ease: "power3.out" } });

  // 添加动画序列
  tl.add(heroSectionRef.value.runAnimations())
    .add(decorativeElementsRef.value.runInitialAnimations(), "-=0.5");

  // 添加按钮动画
  setupButtonAnimations();

  // 设置装饰元素动画
  decorativeElementsRef.value.setupFloatingAnimations();
  decorativeElementsRef.value.setupMouseMoveEffect(containerRef.value);

  // 设置特性卡片动画
  featureCardsRef.value.setupCardAnimations();

  // 监听滚动触发按钮和卡片动画
  const animationContainer = document.querySelector('.animation-container');

  // 创建 Intersection Observer
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        // 元素进入视窗，使用变量控制延迟时间
        setTimeout(() => {
          gsap.to(entry.target, {
            opacity: 1,
            y: 0,
            duration: 1.2,
            ease: "power2.out"
          });
        }, animationDelay);

        // 一旦触发动画，就取消观察
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.2 }); // 当20%的元素可见时触发

  // 开始观察各个元素
  observer.observe(animationContainer);

  // 设置组件特定的观察器
  featureCardsRef.value.observeScroll(animationDelay);
  pageShowcaseRef.value.setupObserver(animationDelay);

  // 运行打字机动画
  heroSectionRef.value.runTypingAnimation();
});

// 设置按钮动画
const setupButtonAnimations = () => {
  // 为按钮添加悬停动效
  const buttons = document.querySelectorAll('.nav-button');
  buttons.forEach(button => {
    button.addEventListener('mouseenter', () => {
      gsap.to(button, {
        y: -5,
        scale: 1.05,
        duration: 0.3,
        boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)'
      });
    });

    button.addEventListener('mouseleave', () => {
      gsap.to(button, {
        y: 0,
        scale: 1,
        duration: 0.3,
        boxShadow: '0 4px 15px rgba(0, 0, 0, 0.1)'
      });
    });
  });
};
</script>

<style scoped>
.show-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 3rem 1rem;
  background: linear-gradient(135deg, var(--qd-color-primary-light-9) 0%, var(--qd-color-primary-light-7) 100%);
  position: relative;
  overflow: hidden;
}

/* 添加夜间模式下的容器背景 */
[data-theme="dark"] .show-container,
.dark .show-container {
  background: linear-gradient(135deg, var(--qd-color-dark-9) 0%, var(--qd-color-dark-7) 100%);
}

.animation-container {
  width: 100%;
  opacity: 0;
  transform: translateY(50px);
  z-index: 2;
}

.buttons-container {
  display: flex;
  gap: 1.5rem;
  margin: 2rem 0;
  z-index: 2;
  justify-content: center;
  flex-wrap: wrap;
}

.nav-button {
  padding: 0.8rem 2rem;
  border-radius: 30px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.primary-button {
  background: linear-gradient(45deg, var(--qd-color-primary) 0%, var(--qd-color-primary-light-2) 100%);
  color: var(--qd-color-primary-light-10);
}

.primary-button:hover {
  box-shadow: 0 7px 20px rgba(69, 70, 94, 0.3);
}

.secondary-button {
  background: var(--qd-color-primary-light-10);
  color: var(--qd-color-primary);
  border: 2px solid var(--qd-color-primary-light-5);
}

.secondary-button:hover {
  box-shadow: 0 7px 20px rgba(69, 70, 94, 0.2);
  background: var(--qd-color-primary-light-9);
}

/* 优化夜间模式下的按钮样式 */
[data-theme="dark"] .primary-button,
.dark .primary-button {
  background: linear-gradient(45deg, var(--qd-color-dark-1) 0%, var(--qd-color-dark-3) 100%);
  color: var(--qd-color-dark-10);
}

[data-theme="dark"] .primary-button:hover,
.dark .primary-button:hover {
  box-shadow: 0 7px 20px rgba(146, 148, 174, 0.4);
}

[data-theme="dark"] .secondary-button,
.dark .secondary-button {
  background: var(--qd-color-dark-10);
  color: var(--qd-color-dark-3);
  border: 2px solid var(--qd-color-dark-5);
}

[data-theme="dark"] .secondary-button:hover,
.dark .secondary-button:hover {
  box-shadow: 0 7px 20px rgba(146, 148, 174, 0.3);
  background: var(--qd-color-dark-9);
}

[data-theme="dark"] .login-button,
.dark .login-button {
  background: var(--qd-color-dark-10);
  color: var(--qd-color-dark-4);
  border: 2px solid var(--qd-color-dark-6);
}

[data-theme="dark"] .login-button:hover,
.dark .login-button:hover {
  box-shadow: 0 7px 20px rgba(146, 148, 174, 0.3);
  background: var(--qd-color-dark-9);
}

/* 添加额外装饰元素 */
.show-container::before,
.show-container::after {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(69, 70, 94, 0.1) 0%, rgba(172, 175, 202, 0.05) 100%);
  z-index: 1;
}

/* 优化夜间模式下的装饰元素 */
[data-theme="dark"] .show-container::before,
[data-theme="dark"] .show-container::after,
.dark .show-container::before,
.dark .show-container::after {
  background: linear-gradient(135deg, rgba(146, 148, 174, 0.2) 0%, rgba(194, 197, 223, 0.1) 100%);
}

.show-container::before {
  top: -250px;
  left: -250px;
}

.show-container::after {
  bottom: -250px;
  right: -250px;
}

/* 添加切换日夜模式按钮 */
.theme-switch {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  background: var(--qd-color-primary-light-10);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.theme-switch i {
  font-size: 20px;
  color: var(--qd-color-primary);
  transition: all 0.3s ease;
}

[data-theme="dark"] .theme-switch,
.dark .theme-switch {
  background: var(--qd-color-dark-8);
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

/* 响应式调整 */
@media (max-width: 768px) {
  .buttons-container {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>