<template>
    <div class="show-container" ref="containerRef">
      <!-- 添加切换主题按钮 -->
      <button class="theme-switch" @click="toggleTheme">
        <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
      </button>
      
      <div class="hero-section">
        <div class="title-box" ref="titleBoxRef">
          <h1 class="site-title">青黛影像</h1>
          <div class="underline" ref="underlineRef"></div>
        </div>
        <p class="site-description" ref="descriptionRef">
          捕捉瞬间，珍藏永恒。青黛影像致力于为您提供优质的摄影管理解决方案，让每一张照片都能讲述一个故事。
        </p>
      </div>
  
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
        <div class="features-section" ref="featuresRef">
          <h2 class="features-title">核心功能</h2>
          <div class="feature-cards">
            <div class="feature-card">
              <div class="card-icon">
                <i class="el-icon-lock"></i>
              </div>
              <h3 class="card-title">"照片/组图/时间轴"展示功能</h3>
              <p class="card-description">主页瀑布流展示，详细信息通过胶片样式展示，时间轴展示个人足迹</p>
            </div>
            
            <div class="feature-card">
              <div class="card-icon">
                <i class="el-icon-picture"></i>
              </div>
              <h3 class="card-title">"照片/组图/时间轴/分享"管理功能</h3>
              <p class="card-description">"照片/组图/时间轴/分享"进行详细编辑与管理</p>
            </div>
            
            <div class="feature-card">
              <div class="card-icon">
                <i class="el-icon-share"></i>
              </div>
              <h3 class="card-title">分享功能</h3>
              <p class="card-description">灵活的照片分享选项，支持生成专属链接</p>
            </div>
            
            <div class="feature-card">
              <div class="card-icon">
                <i class="el-icon-data-analysis"></i>
              </div>
              <h3 class="card-title">数据分析功能</h3>
              <p class="card-description">提供照片拍摄数据、足迹统计</p>
            </div>
          </div>
        </div>
        
        <!-- 页面展示区域 -->
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
      </div>
  
      <div class="decorative-elements">
        <div class="floating-circle circle-1" ref="circle1Ref"></div>
        <div class="floating-circle circle-2" ref="circle2Ref"></div>
        <div class="floating-circle circle-3" ref="circle3Ref"></div>
      </div>
    </div>
  
    <!-- 引入通用Footer组件 -->
    <Footer />
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted, computed } from 'vue';
  import gsap from 'gsap';
  import { showImages } from '@/data/imageUrls';
  import Footer from '@/components/common/Footer.vue';
  import { useThemeStore } from '@/stores/theme';
  import { Sunny, Moon } from '@element-plus/icons-vue';
  
  const themeStore = useThemeStore();
  const isDark = computed(() => themeStore.theme === 'dark');
  
  const toggleTheme = () => {
    themeStore.toggleTheme();
  };
  
  const currentYear = ref(new Date().getFullYear());
  const animationDelay = 500;
  const autoplayInterval = ref(3000);
  const containerRef = ref(null);
  const titleBoxRef = ref(null);
  const underlineRef = ref(null);
  const descriptionRef = ref(null);
  const buttonsRef = ref(null);
  const featuresRef = ref(null);
  const showcaseRef = ref(null);
  const circle1Ref = ref(null);
  const circle2Ref = ref(null);
  const circle3Ref = ref(null);
  const activeTabIndex = ref(0);
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
  
  onMounted(() => {
    // 创建初始状态
    gsap.set(titleBoxRef.value, { opacity: 0, y: -50 });
    gsap.set(underlineRef.value, { width: 0, opacity: 0 });
    gsap.set(descriptionRef.value, { opacity: 0 });
    
    // 将需要滚动触发的元素设置为初始透明状态
    gsap.set('.animation-container', { opacity: 0, y: 50 });
    
    // 设置卡片和页面展示区域的初始状态
    gsap.set('.feature-card', { opacity: 0, y: 30 });
    gsap.set('.page-showcase', { opacity: 0, y: 30 });
    
    gsap.set([circle1Ref.value, circle2Ref.value, circle3Ref.value], { scale: 0.5, opacity: 0 });
  
    // 创建时间线动画
    const tl = gsap.timeline({ defaults: { ease: "power3.out" } });
  
    // 添加动画序列
    tl.to(titleBoxRef.value, { opacity: 1, y: 0, duration: 1 })
      .to(underlineRef.value, { width: 120, opacity: 1, duration: 0.8 }, "-=0.5")
      .to(descriptionRef.value, { opacity: 1, duration: 1 }, "-=0.3")
      .to([circle1Ref.value, circle2Ref.value, circle3Ref.value], { 
        opacity: 1, 
        scale: 1, 
        duration: 1.5,
        stagger: 0.2
      }, "-=0.5");
  
    // 监听滚动触发按钮和卡片动画
    const animationContainer = document.querySelector('.animation-container');
    const featuresSection = document.querySelector('.features-section');
    const pageShowcase = document.querySelector('.page-showcase');
    
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
    
    // 为核心功能区域创建单独的观察者
    const featuresObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          // 元素进入视窗，延迟显示
          setTimeout(() => {
            // 为卡片添加交错动画
            const cards = document.querySelectorAll('.feature-card');
            gsap.to(cards, {
              opacity: 1,
              y: 0,
              duration: 0.8,
              stagger: 0.15,
              ease: "power2.out"
            });
          }, animationDelay);
          
          // 一旦触发动画，就取消观察
          featuresObserver.unobserve(entry.target);
        }
      });
    }, { threshold: 0.2 });
    
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
    
    // 开始观察各个元素
    observer.observe(animationContainer);
    featuresObserver.observe(featuresSection);
    showcaseObserver.observe(pageShowcase);
  
    // 为圆形元素创建无限浮动动画
    gsap.to(circle1Ref.value, {
      x: "random(-30, 30)",
      y: "random(-30, 30)",
      duration: 20,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut"
    });
  
    gsap.to(circle2Ref.value, {
      x: "random(-40, 40)",
      y: "random(-40, 40)",
      duration: 25,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut"
    });
  
    gsap.to(circle3Ref.value, {
      x: "random(-50, 50)",
      y: "random(-50, 50)",
      duration: 30,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut"
    });
  
    // 添加鼠标移动视差效果，增强效果
    containerRef.value.addEventListener('mousemove', (e) => {
      // 增大移动倍数，使效果更明显
      const xPos = (e.clientX / window.innerWidth - 0.5) * 60;
      const yPos = (e.clientY / window.innerHeight - 0.5) * 60;
  
      gsap.to([circle1Ref.value, circle2Ref.value, circle3Ref.value], {
        x: (i) => (i + 1) * xPos / 2, // 增大移动幅度
        y: (i) => (i + 1) * yPos / 2, // 增大移动幅度
        duration: 0.8, // 减少动画时长，使响应更迅速
        ease: "power2.out" // 修改缓动函数，使动画更有弹性
      });
    });
  
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
  
    // 为卡片添加悬停动效
    const featureCards = document.querySelectorAll('.feature-card');
    featureCards.forEach(card => {
      card.addEventListener('mouseenter', () => {
        gsap.to(card, {
          y: -10,
          scale: 1.03,
          boxShadow: '0 15px 30px rgba(0, 0, 0, 0.15)',
          duration: 0.3
        });
        // 图标动画
        const icon = card.querySelector('.card-icon i');
        gsap.to(icon, {
          scale: 1.2,
          color: 'var(--qd-color-primary)',
          duration: 0.3
        });
      });
      
      card.addEventListener('mouseleave', () => {
        gsap.to(card, {
          y: 0,
          scale: 1,
          boxShadow: '0 5px 15px rgba(0, 0, 0, 0.1)',
          duration: 0.3
        });
        // 图标回到原始状态
        const icon = card.querySelector('.card-icon i');
        gsap.to(icon, {
          scale: 1,
          color: 'var(--qd-color-primary-light-3)',
          duration: 0.3
        });
      });
    });
  
    // 为标题添加文字打字机效果
    const titleText = "青黛影像";
    const titleElement = document.querySelector('.site-title');
    titleElement.innerHTML = '';
    
    gsap.set(titleElement, { opacity: 1 });
    
    let tl2 = gsap.timeline({ delay: 0.5 });
    
    for (let i = 0; i < titleText.length; i++) {
      tl2.to(titleElement, {
        innerHTML: titleText.substring(0, i + 1) + '<span class="cursor">|</span>',
        duration: 0.15,
        ease: "none"
      });
    }
    
    tl2.to(titleElement, {
      innerHTML: titleText,
      duration: 0.15,
      ease: "none"
    });

    // 启动自动轮播
    startAutoplay();
  });

  // 组件卸载时清除定时器
  onUnmounted(() => {
    if (autoplayTimer) {
      clearInterval(autoplayTimer);
    }
  });
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
  
  .hero-section {
    text-align: center;
    max-width: 800px;
    margin: 2rem auto;
    z-index: 2;
  }
  
  .animation-container {
    width: 100%;
    opacity: 0;
    transform: translateY(50px);
    z-index: 2;
  }
  
  .title-box {
    margin-bottom: 2rem;
  }
  
  .site-title {
    font-size: 4rem;
    font-weight: 700;
    color: var(--qd-color-primary);
    margin: 0;
    letter-spacing: 0.1em;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
    position: relative;
  }
  
  .underline {
    height: 4px;
    background: linear-gradient(90deg, var(--qd-color-primary) 0%, var(--qd-color-primary-light-4) 100%);
    margin: 0.5rem auto;
    border-radius: 2px;
  }
  
  .site-description {
    font-size: 1.2rem;
    color: var(--qd-color-primary-light-2);
    line-height: 1.6;
    margin-bottom: 2rem;
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
  
  /* 功能卡片区域样式 */
  .features-section {
    width: 100%;
    max-width: 1200px;
    z-index: 2;
    padding-top: 1rem;
  }
  
  .features-title {
    text-align: center;
    font-size: 2rem;
    color: var(--qd-color-primary);
    margin-bottom: 2.5rem;
    font-weight: 600;
    position: relative;
  }
  
  .features-title::after {
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
  
  .feature-cards {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    width: 100%;
    /* 移除溢出隐藏属性，允许卡片悬浮显示 */
    overflow: visible;
  }
  
  .feature-card {
    background-color: var(--qd-color-primary-light-10);
    border-radius: 16px;
    padding: 1.5rem;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
    /* 固定宽度为22%，确保一行放下四个 */
    width: 20%;
    margin-bottom: 1rem;
    aspect-ratio: 1/1;
    position: relative;
    overflow: hidden;
    opacity: 0;
    transform: translateY(30px);
  }
  
  /* 优化夜间模式下的功能卡片样式 */
  [data-theme="dark"] .feature-card,
  .dark .feature-card {
    background-color: var(--qd-color-dark-8);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.4);
  }
  
  .feature-card::before {
    content: '';
    position: absolute;
    top: -50px;
    right: -50px;
    width: 100px;
    height: 100px;
    background: linear-gradient(135deg, var(--qd-color-primary-light-7) 0%, transparent 70%);
    border-radius: 50%;
    opacity: 0.6;
  }
  
  /* 优化夜间模式下的功能卡片样式 */
  [data-theme="dark"] .feature-card::before,
  .dark .feature-card::before {
    background: linear-gradient(135deg, var(--qd-color-dark-7) 0%, transparent 70%);
  }
  
  .card-icon {
    margin-bottom: 1.5rem;
  }
  
  .card-icon i {
    font-size: 2rem;
    color: var(--qd-color-primary-light-3);
    transition: all 0.3s ease;
  }
  
  /* 优化夜间模式下的功能卡片样式 */
  [data-theme="dark"] .card-icon i,
  .dark .card-icon i {
    color: var(--qd-color-dark-4);
  }
  
  .card-title {
    font-size: 1.4rem;
    font-weight: 600;
    color: var(--qd-color-primary);
    margin: 0 0 1rem 0;
  }
  
  /* 优化夜间模式下的功能卡片样式 */
  [data-theme="dark"] .card-title,
  .dark .card-title {
    color: var(--qd-color-dark-3);
  }
  
  .card-description {
    font-size: 1rem;
    color: var(--qd-color-primary-light-2);
    line-height: 1.5;
    flex-grow: 1;
  }
  
  /* 优化夜间模式下的功能卡片样式 */
  [data-theme="dark"] .card-description,
  .dark .card-description {
    color: var(--qd-color-dark-2);
    font-weight: 500;
  }
  
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
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-container,
  .dark .showcase-container {
    background-color: var(--qd-color-dark-8);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
  }
  
  .showcase-tabs {
    width: 200px;
    background-color: var(--qd-color-primary-light-8);
    display: flex;
    flex-direction: column;
    padding: 2rem 0;
  }
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-tabs,
  .dark .showcase-tabs {
    background-color: var(--qd-color-dark-7);
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
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-tab,
  .dark .showcase-tab {
    color: var(--qd-color-dark-4);
  }
  
  .showcase-tab:hover {
    background-color: var(--qd-color-primary-light-7);
    color: var(--qd-color-primary);
  }
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-tab:hover,
  .dark .showcase-tab:hover {
    background-color: var(--qd-color-dark-6);
    color: var(--qd-color-dark-2);
  }
  
  .showcase-tab.active {
    background-color: var(--qd-color-primary-light-7);
    color: var(--qd-color-primary);
    border-left: 4px solid var(--qd-color-primary);
    font-weight: 600;
  }
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-tab.active,
  .dark .showcase-tab.active {
    background-color: var(--qd-color-dark-6);
    color: var(--qd-color-dark-2);
    border-left: 4px solid var(--qd-color-dark-3);
  }
  
  .showcase-content {
    flex: 1;
    position: relative;
    background-color: var(--qd-color-primary-light-9);
  }
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-content,
  .dark .showcase-content {
    background-color: var(--qd-color-dark-7);
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
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-image-container,
  .dark .showcase-image-container {
    background-color: var(--qd-color-dark-7);
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
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-image,
  .dark .showcase-image {
    background-color: var(--qd-color-dark-7);
  }
  
  .showcase-description {
    background-color: var(--qd-color-primary-light-8);
    padding: 1rem 2rem;
    height: 80px;
  }
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-description,
  .dark .showcase-description {
    background-color: var(--qd-color-dark-6);
  }
  
  .showcase-description h3 {
    font-size: 1.3rem;
    margin: 0 0 0.5rem 0;
    color: var(--qd-color-primary);
  }
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-description h3,
  .dark .showcase-description h3 {
    color: var(--qd-color-dark-3);
  }
  
  .showcase-description p {
    font-size: 1rem;
    margin: 0;
    color: var(--qd-color-primary-light-2);
  }
  
  /* 优化夜间模式下的展示区域样式 */
  [data-theme="dark"] .showcase-description p,
  .dark .showcase-description p {
    color: var(--qd-color-dark-2);
    font-weight: 500;
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
  
  .decorative-elements {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
  }
  
  .floating-circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(45deg, rgba(69, 70, 94, 0.2) 0%, rgba(172, 175, 202, 0.2) 100%);
    box-shadow: 0 15px 40px rgba(69, 70, 94, 0.15); /* 增强阴影效果 */
    backdrop-filter: blur(5px); /* 添加模糊效果 */
  }
  
  /* 优化夜间模式下的装饰元素 */
  [data-theme="dark"] .floating-circle,
  .dark .floating-circle {
    background: linear-gradient(45deg, rgba(146, 148, 174, 0.3) 0%, rgba(194, 197, 223, 0.2) 100%);
    box-shadow: 0 15px 40px rgba(146, 148, 174, 0.25);
  }
  
  .circle-1 {
    width: 400px; /* 增大尺寸 */
    height: 400px;
    top: -200px;
    right: -150px;
    z-index: 1;
  }
  
  .circle-2 {
    width: 300px; /* 增大尺寸 */
    height: 300px;
    bottom: 5%;
    left: -50px;
    z-index: 1;
  }
  
  .circle-3 {
    width: 350px; /* 增大尺寸 */
    height: 350px;
    top: 25%;
    right: 10%;
    z-index: 1;
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
  
  .cursor {
    display: inline-block;
    width: 2px;
    background-color: var(--qd-color-primary);
    animation: blink 0.7s infinite;
  }
  
  @keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
  }
  
  /* 响应式调整 */
  @media (max-width: 768px) {
    .site-title {
      font-size: 3rem;
    }
    
    .site-description {
      font-size: 1rem;
    }
    
    .buttons-container {
      flex-direction: column;
      gap: 1rem;
    }
    
    .feature-card {
      width: 40%; /* 一行两个卡片 */
      padding: 1rem;
    }
    
    .card-title {
      font-size: 1.1rem;
      margin-bottom: 0.5rem;
    }
    
    .card-description {
      font-size: 0.85rem;
    }

    .showcase-container {
      flex-direction: column;
      height: auto;
      height: 400px; /* 增加高度确保足够空间 */
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
      height: 330px; /* 固定高度 */
      min-height: 330px;
    }

    .showcase-image-container {
      position: absolute; /* 绝对定位 */
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      /* 使用固定高度 */
      height: 330px;
      min-height: 330px;
      max-height: 330px;
    }

    .showcase-image {
      /* 使用绝对值而不是百分比 */
      height: 250px; 
      min-height: 250px;
      max-height: 250px;
      /* 使用object-fit确保图片正确显示 */
      object-fit: contain;
      background-color: var(--qd-color-primary-light-8);
    }

    .showcase-description {
      height: 80px;
      min-height: 80px;
      max-height: 80px;
      overflow: hidden; /* 防止内容溢出 */
    }
  }
  
  /* 添加从main.css移除的样式 */
  .photo-card {
    background-color: var(--qd-color-bg-light) !important;
    color: var(--qd-color-text-primary) !important;
  }
  
  .photo-card-title {
    color: var(--qd-color-primary) !important;
  }
  
  .photo-card-text {
    color: var(--qd-color-text-regular) !important;
  }
  
  /* 优化夜间模式下的标题和描述文本 */
  [data-theme="dark"] .site-title,
  .dark .site-title {
    color: var(--qd-color-dark-3);
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.4);
  }
  
  [data-theme="dark"] .site-description,
  .dark .site-description {
    color: var(--qd-color-dark-4);
  }
  
  [data-theme="dark"] .features-title,
  .dark .features-title {
    color: var(--qd-color-dark-3);
  }
  
  [data-theme="dark"] .showcase-title,
  .dark .showcase-title {
    color: var(--qd-color-dark-3);
  }
  
  [data-theme="dark"] .features-title::after,
  [data-theme="dark"] .showcase-title::after,
  .dark .features-title::after,
  .dark .showcase-title::after {
    background: linear-gradient(90deg, var(--qd-color-dark-3) 0%, var(--qd-color-dark-5) 100%);
  }
  
  /* 优化夜间模式下的下划线 */
  [data-theme="dark"] .underline,
  .dark .underline {
    background: linear-gradient(90deg, var(--qd-color-dark-3) 0%, var(--qd-color-dark-5) 100%);
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
  </style>