<template>
  <div class="features-section" ref="featuresRef">
    <h2 class="features-title">核心功能</h2>
    <div class="feature-cards">
      <div class="feature-card" v-for="(feature, index) in features" :key="index">
        <div class="card-icon">
          <i :class="feature.icon"></i>
        </div>
        <h3 class="card-title">{{ feature.title }}</h3>
        <p class="card-description">{{ feature.description }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import gsap from 'gsap';

const featuresRef = ref(null);

// 功能数据
const features = [
  {
    icon: 'el-icon-lock',
    title: '"照片/组图/时间轴"展示功能',
    description: '主页瀑布流展示，详细信息通过胶片样式展示，时间轴展示个人足迹'
  },
  {
    icon: 'el-icon-picture',
    title: '"照片/组图/时间轴/分享"管理功能',
    description: '"照片/组图/时间轴/分享"进行详细编辑与管理'
  },
  {
    icon: 'el-icon-share',
    title: '分享功能',
    description: '灵活的照片分享选项，支持生成专属链接'
  },
  {
    icon: 'el-icon-data-analysis',
    title: '数据分析功能',
    description: '提供照片拍摄数据、足迹统计'
  }
];

// 初始化方法
const initAnimations = () => {
  gsap.set('.feature-card', { opacity: 0, y: 30 });
};

// 为卡片设置动画
const setupCardAnimations = () => {
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
};

// 开始观察滚动
const observeScroll = (animationDelay) => {
  // 创建 Intersection Observer
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
  
  // 开始观察
  featuresObserver.observe(featuresRef.value);
};

defineExpose({
  featuresRef,
  initAnimations,
  setupCardAnimations,
  observeScroll
});
</script>

<style scoped>
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

.card-icon {
  margin-bottom: 1.5rem;
}

.card-icon i {
  font-size: 2rem;
  color: var(--qd-color-primary-light-3);
  transition: all 0.3s ease;
}

.card-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--qd-color-primary);
  margin: 0 0 1rem 0;
}

.card-description {
  font-size: 1rem;
  color: var(--qd-color-primary-light-2);
  line-height: 1.5;
  flex-grow: 1;
}

/* 优化夜间模式下的功能卡片样式 */
[data-theme="dark"] .features-title,
.dark .features-title {
  color: var(--qd-color-dark-3);
}

[data-theme="dark"] .features-title::after,
.dark .features-title::after {
  background: linear-gradient(90deg, var(--qd-color-dark-3) 0%, var(--qd-color-dark-5) 100%);
}

[data-theme="dark"] .feature-card,
.dark .feature-card {
  background-color: var(--qd-color-dark-8);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.4);
}

[data-theme="dark"] .feature-card::before,
.dark .feature-card::before {
  background: linear-gradient(135deg, var(--qd-color-dark-7) 0%, transparent 70%);
}

[data-theme="dark"] .card-icon i,
.dark .card-icon i {
  color: var(--qd-color-dark-4);
}

[data-theme="dark"] .card-title,
.dark .card-title {
  color: var(--qd-color-dark-3);
}

[data-theme="dark"] .card-description,
.dark .card-description {
  color: var(--qd-color-dark-2);
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 768px) {
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
}
</style> 