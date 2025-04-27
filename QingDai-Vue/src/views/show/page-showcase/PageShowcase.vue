<template>
  <div class="page-showcase" ref="showcaseRef">
    <h2 class="showcase-title">页面展示</h2>
    <div class="showcase-container">
      <div class="showcase-tabs">
        <div 
          v-for="(tab, index) in showImages" 
          :key="index"
          :class="['showcase-tab', { active: activeTabIndex === index }]"
          @click="setActiveTab(index)"
        >
          {{ tab.name }}
        </div>
      </div>
      <div class="showcase-content">
        <!-- 预加载所有图片 -->
        <div style="display: none;">
          <img v-for="(tab, index) in showImages" :key="`preload-${index}`" :src="tab.image" />
        </div>
        
        <transition name="fade">
          <div class="showcase-image-container" :key="activeTabIndex">
            <img :src="showImages[activeTabIndex].image" alt="页面截图" class="showcase-image" />
            <div class="showcase-description">
              <h3>{{ showImages[activeTabIndex].name }}</h3>
              <p>{{ showImages[activeTabIndex].description }}</p>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import gsap from 'gsap';
import { showImages } from '@/data/imageUrls';

const showcaseRef = ref(null);
const activeTabIndex = ref(0);
const autoplayInterval = ref(3000);
let autoplayTimer = null;

const setActiveTab = (index) => {
  activeTabIndex.value = index;
  if (autoplayTimer) {
    clearInterval(autoplayTimer);
    startAutoplay();
  }
};

const startAutoplay = () => {
  autoplayTimer = setInterval(() => {
    activeTabIndex.value = (activeTabIndex.value + 1) % showImages.length;
  }, autoplayInterval.value);
};

const initAnimations = () => {
  gsap.set('.page-showcase', { opacity: 0, y: 30 });
};

const setupObserver = (animationDelay) => {
  // 为页面展示区域创建单独的观察者
  const showcaseObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        // 元素进入视窗，延迟显示
        setTimeout(() => {
          gsap.to(entry.target, {
            opacity: 1,
            y: 0,
            duration: 1,
            ease: "power2.out"
          });
        }, animationDelay);
        
        // 一旦触发动画，就取消观察
        showcaseObserver.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });
  
  // 开始观察
  showcaseObserver.observe(showcaseRef.value);
};

onMounted(() => {
  startAutoplay();
});

onUnmounted(() => {
  if (autoplayTimer) {
    clearInterval(autoplayTimer);
  }
});

defineExpose({
  showcaseRef,
  initAnimations,
  setupObserver,
  startAutoplay,
  clearAutoplay: () => {
    if (autoplayTimer) {
      clearInterval(autoplayTimer);
    }
  }
});
</script>

<style scoped>
/* 页面展示区域 */
.page-showcase {
  width: 100%;
  max-width: 1200px;
  margin: 4rem auto 2rem;
  opacity: 0;
  transform: translateY(30px);
}

.showcase-title {
  text-align: center;
  font-size: 2rem;
  color: var(--qd-color-primary);
  margin-bottom: 2.5rem;
  font-weight: 600;
  position: relative;
}

.showcase-title::after {
  content: '';
  position: absolute;
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, var(--qd-color-primary) 0%, var(--qd-color-primary-light-4) 100%);
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 2px;
}

.showcase-container {
  display: flex;
  background-color: var(--qd-color-primary-light-10);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  height: 92vh;
  position: relative; /* 确保定位正确 */
}

.showcase-tabs {
  width: 200px;
  background-color: var(--qd-color-primary-light-8);
  display: flex;
  flex-direction: column;
  padding: 2rem 0;
}

.showcase-tab {
  padding: 1rem 2rem;
  font-size: 1.2rem;
  font-weight: 500;
  color: var(--qd-color-primary-light-3);
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.showcase-tab:hover {
  background-color: var(--qd-color-primary-light-7);
  color: var(--qd-color-primary);
}

.showcase-tab.active {
  background-color: var(--qd-color-primary-light-7);
  color: var(--qd-color-primary);
  border-left: 4px solid var(--qd-color-primary);
  font-weight: 600;
}

.showcase-content {
  flex: 1;
  position: relative;
  background-color: var(--qd-color-primary-light-9);
}

.showcase-image-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: absolute;
  inset: 0;
  /* 添加背景色作为加载占位 */
  background-color: var(--qd-color-primary-light-8);
}

.showcase-image {
  flex: 1;
  width: 100%;
  height: calc(100% - 80px);
  object-fit: cover;
  border-radius: 0 0 20px 0;
  /* 确保图片不会导致容器变形 */
  min-height: 200px;
  background-color: var(--qd-color-primary-light-8);
}

.showcase-description {
  background-color: var(--qd-color-primary-light-8);
  padding: 1rem 2rem;
  height: 80px;
}

.showcase-description h3 {
  font-size: 1.3rem;
  margin: 0 0 0.5rem 0;
  color: var(--qd-color-primary);
}

.showcase-description p {
  font-size: 1rem;
  margin: 0;
  color: var(--qd-color-primary-light-2);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 优化夜间模式下的展示区域样式 */
[data-theme="dark"] .showcase-title,
.dark .showcase-title {
  color: var(--qd-color-dark-3);
}

[data-theme="dark"] .showcase-title::after,
.dark .showcase-title::after {
  background: linear-gradient(90deg, var(--qd-color-dark-3) 0%, var(--qd-color-dark-5) 100%);
}

[data-theme="dark"] .showcase-container,
.dark .showcase-container {
  background-color: var(--qd-color-dark-8);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
}

[data-theme="dark"] .showcase-tabs,
.dark .showcase-tabs {
  background-color: var(--qd-color-dark-7);
}

[data-theme="dark"] .showcase-tab,
.dark .showcase-tab {
  color: var(--qd-color-dark-4);
}

[data-theme="dark"] .showcase-tab:hover,
.dark .showcase-tab:hover {
  background-color: var(--qd-color-dark-6);
  color: var(--qd-color-dark-2);
}

[data-theme="dark"] .showcase-tab.active,
.dark .showcase-tab.active {
  background-color: var(--qd-color-dark-6);
  color: var(--qd-color-dark-2);
  border-left: 4px solid var(--qd-color-dark-3);
}

[data-theme="dark"] .showcase-content,
.dark .showcase-content {
  background-color: var(--qd-color-dark-7);
}

[data-theme="dark"] .showcase-image-container,
.dark .showcase-image-container {
  background-color: var(--qd-color-dark-7);
}

[data-theme="dark"] .showcase-image,
.dark .showcase-image {
  background-color: var(--qd-color-dark-7);
}

[data-theme="dark"] .showcase-description,
.dark .showcase-description {
  background-color: var(--qd-color-dark-6);
}

[data-theme="dark"] .showcase-description h3,
.dark .showcase-description h3 {
  color: var(--qd-color-dark-3);
}

[data-theme="dark"] .showcase-description p,
.dark .showcase-description p {
  color: var(--qd-color-dark-2);
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .showcase-container {
    flex-direction: column;
    height: auto;
    height: 400px;
    min-height: unset;
    max-height: 400px;
  }

  .showcase-tabs {
    width: 100%;
    flex-direction: row;
    padding: 0;
    overflow-x: auto;
  }

  .showcase-tab {
    padding: 1rem;
    border-left: none;
    border-bottom: 4px solid transparent;
    white-space: nowrap;
  }

  .showcase-tab.active {
    border-left: none;
    border-bottom: 4px solid var(--qd-color-primary);
  }

  .showcase-content {
    position: relative;
    flex: 1;
    height: 330px;
    min-height: 330px;
  }

  .showcase-image-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    height: 330px;
    min-height: 330px;
    max-height: 330px;
  }

  .showcase-image {
    height: 250px; 
    min-height: 250px;
    max-height: 250px;
    object-fit: contain;
    background-color: var(--qd-color-primary-light-8);
  }

  .showcase-description {
    height: 80px;
    min-height: 80px;
    max-height: 80px;
    overflow: hidden;
  }
}
</style> 