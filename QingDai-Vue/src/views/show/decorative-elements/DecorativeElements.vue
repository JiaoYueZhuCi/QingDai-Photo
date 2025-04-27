<template>
  <div class="decorative-elements">
    <div class="floating-circle circle-1" ref="circle1Ref"></div>
    <div class="floating-circle circle-2" ref="circle2Ref"></div>
    <div class="floating-circle circle-3" ref="circle3Ref"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import gsap from 'gsap';

const circle1Ref = ref(null);
const circle2Ref = ref(null);
const circle3Ref = ref(null);

// 初始化动画
const initAnimations = () => {
  gsap.set([circle1Ref.value, circle2Ref.value, circle3Ref.value], { scale: 0.5, opacity: 0 });
};

// 运行初始动画
const runInitialAnimations = () => {
  // 添加到动画时间线
  return gsap.to([circle1Ref.value, circle2Ref.value, circle3Ref.value], { 
    opacity: 1, 
    scale: 1, 
    duration: 1.5,
    stagger: 0.2
  });
};

// 设置浮动动画
const setupFloatingAnimations = () => {
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
};

// 设置鼠标移动效果
const setupMouseMoveEffect = (container) => {
  if (!container) return;
  
  // 添加鼠标移动视差效果，增强效果
  container.addEventListener('mousemove', (e) => {
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
};

defineExpose({
  circle1Ref,
  circle2Ref,
  circle3Ref,
  initAnimations,
  runInitialAnimations,
  setupFloatingAnimations,
  setupMouseMoveEffect
});
</script>

<style scoped>
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

/* 优化夜间模式下的装饰元素 */
[data-theme="dark"] .floating-circle,
.dark .floating-circle {
  background: linear-gradient(45deg, rgba(146, 148, 174, 0.3) 0%, rgba(194, 197, 223, 0.2) 100%);
  box-shadow: 0 15px 40px rgba(146, 148, 174, 0.25);
}
</style> 