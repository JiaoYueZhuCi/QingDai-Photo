<template>
  <div class="hero-section">
    <div class="title-box" ref="titleBoxRef">
      <h1 class="site-title">青黛影像</h1>
      <div class="underline" ref="underlineRef"></div>
    </div>
    <p class="site-description" ref="descriptionRef">
      捕捉瞬间，珍藏永恒。青黛影像致力于为您提供优质的摄影管理解决方案，让每一张照片都能讲述一个故事。
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import gsap from 'gsap';

const titleBoxRef = ref(null);
const underlineRef = ref(null);
const descriptionRef = ref(null);

// 初始化方法供父组件调用
const initAnimations = () => {
  // 创建初始状态
  gsap.set(titleBoxRef.value, { opacity: 0, y: -50 });
  gsap.set(underlineRef.value, { width: 0, opacity: 0 });
  gsap.set(descriptionRef.value, { opacity: 0 });
};

// 运行动画方法供父组件调用
const runAnimations = () => {
  // 创建时间线动画
  const tl = gsap.timeline({ defaults: { ease: "power3.out" } });

  // 添加动画序列
  tl.to(titleBoxRef.value, { opacity: 1, y: 0, duration: 1 })
    .to(underlineRef.value, { width: 120, opacity: 1, duration: 0.8 }, "-=0.5")
    .to(descriptionRef.value, { opacity: 1, duration: 1 }, "-=0.3");
  
  return tl;
};

// 添加文字打字机效果供父组件调用
const runTypingAnimation = () => {
  const titleText = "青黛影像";
  const titleElement = document.querySelector('.site-title');
  titleElement.innerHTML = '';
  
  gsap.set(titleElement, { opacity: 1 });
  
  let tl = gsap.timeline({ delay: 0.5 });
  
  for (let i = 0; i < titleText.length; i++) {
    tl.to(titleElement, {
      innerHTML: titleText.substring(0, i + 1) + '<span class="cursor">|</span>',
      duration: 0.15,
      ease: "none"
    });
  }
  
  tl.to(titleElement, {
    innerHTML: titleText,
    duration: 0.15,
    ease: "none"
  });

  return tl;
};

defineExpose({
  titleBoxRef,
  underlineRef,
  descriptionRef,
  initAnimations,
  runAnimations,
  runTypingAnimation
});
</script>

<style scoped>
.hero-section {
  text-align: center;
  max-width: 800px;
  margin: 2rem auto;
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

/* 优化夜间模式下的下划线 */
[data-theme="dark"] .underline,
.dark .underline {
  background: linear-gradient(90deg, var(--qd-color-dark-3) 0%, var(--qd-color-dark-5) 100%);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .site-title {
    font-size: 3rem;
  }
  
  .site-description {
    font-size: 1rem;
  }
}
</style> 