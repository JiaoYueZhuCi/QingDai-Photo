<template>
    <div class="main-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-screen">
        <div class="loading-progress">{{ progress }}%</div>
      </div>
  
      <!-- 错误提示 -->
      <div v-if="error" class="error-screen">
        <h2>加载失败</h2>
        <p>{{ errorMessage }}</p>
        <button @click="retry">重试</button>
      </div>
  
      <!-- 主内容 -->
      <div v-show="!loading && !error">
        <!-- 相机模块 -->
        <section class="fullscreen-section">
          <div class="camera-wrapper" ref="cameraWrapper">
            <div class="camera-body" ref="cameraBody">
              <div class="camera-lens" ref="cameraLens"></div>
              <canvas ref="photoCanvas" class="photo-canvas"></canvas>
            </div>
            <button class="shutter-btn" @click="simulateShutter">
              <div class="shutter-icon"></div>
            </button>
          </div>
        </section>
  
        <!-- 胶片模块 -->
        <section class="film-section" v-show="showFilm">
          <div class="film-track" ref="filmTrack">
            <div v-for="(photo, index) in photos" 
                 :key="index"
                 class="film-cell"
                 @click="openLightbox(index)">
              <img :src="photo" class="film-image" :style="parallaxStyle(index)">
            </div>
          </div>
        </section>
  
        <!-- 时间轴模块 -->
        <section class="timeline-section" ref="timelineSection">
          <div class="timeline-map" ref="mapContainer"></div>
          <div class="timeline-rail">
            <div v-for="(event, index) in timeline" 
                 :key="index"
                 class="timeline-marker"
                 :style="markerPosition(index)">
              <h3 class="year">{{ event.year }}</h3>
              <p class="location">{{ event.location }}</p>
              <div class="connector"></div>
            </div>
          </div>
        </section>
  
        <!-- Three.js容器 -->
        <div v-if="threeReady" ref="threeContainer" class="three-viewport"></div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue'
  import { gsap, ScrollTrigger } from 'gsap/all'
  import * as THREE from 'three'
  import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js'
  import { OrbitControls } from 'three/addons/controls/OrbitControls.js'
  
  // GSAP配置
  gsap.registerPlugin(ScrollTrigger)
  
  // 响应式状态
  const loading = ref(true)
  const progress = ref(0)
  const error = ref(false)
  const errorMessage = ref('')
  const threeReady = ref(false)
  const photos = ref([])
  const showFilm = ref(false)
  
  // Three.js相关引用
  let scene, camera, renderer, controls, airplane, mixer
  
  // 时间轴数据
  const timeline = ref([
    { year: '2023', location: 'Paris', coordinates: [48.8566, 2.3522] },
    { year: '2022', location: 'Tokyo', coordinates: [35.6762, 139.6503] }
  ])
  
  // 相机模块初始化
  const initCameraModule = () => {
    gsap.from('.camera-body', {
      scrollTrigger: {
        trigger: '.fullscreen-section',
        start: 'top center',
        end: 'bottom center',
        scrub: 1
      },
      scale: 0.5,
      rotate: 15,
      opacity: 0,
      duration: 2.5,
      ease: 'power4.out'
    })
  }
  
  // 模拟快门效果
  const simulateShutter = () => {
    const lens = document.querySelector('.camera-lens')
    gsap.to(lens, {
      scale: 0.8,
      duration: 0.2,
      repeat: 1,
      yoyo: true,
      onComplete: capturePhoto
    })
  }
  
  // 照片拍摄逻辑
  const capturePhoto = async () => {
    try {
      const canvas = document.createElement('canvas')
      const texture = await new THREE.TextureLoader().loadAsync('/img/introduce/background.jpg')
      const ctx = canvas.getContext('2d')
      canvas.width = texture.image.width
      canvas.height = texture.image.height
      ctx.drawImage(texture.image, 0, 0)
      photos.value.push(canvas.toDataURL())
      developFilm()
    } catch (err) {
      handleError('照片生成失败', err)
    }
  }
  
  // 胶片显影动画
  const developFilm = () => {
    showFilm.value = true
    gsap.from('.film-cell', {
      x: 300,
      opacity: 0,
      stagger: 0.15,
      duration: 0.8,
      ease: 'elastic.out(1, 0.5)'
    })
  }
  
  // Three.js场景初始化
  const initThreeScene = async () => {
    try {
      // 场景设置
      scene = new THREE.Scene()
      camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
      renderer.setSize(window.innerWidth, window.innerHeight)
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
      document.querySelector('.three-viewport').appendChild(renderer.domElement)
  
      // 光照设置
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.5)
      scene.add(ambientLight)
      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
      directionalLight.position.set(5, 5, 5)
      scene.add(directionalLight)
  
      // 加载地球模型
      const globe = await loadModel('/models/earth.glb')
      scene.add(globe.scene)
  
      // 加载飞机模型
      airplane = (await loadModel('/models/airplane.glb')).scene
      airplane.position.set(-10, 0, 0)
      scene.add(airplane)
  
      // 相机控制
      controls = new OrbitControls(camera, renderer.domElement)
      camera.position.set(0, 0, 15)
      controls.enableZoom = false
  
      // 动画循环
      const animate = () => {
        requestAnimationFrame(animate)
        controls.update()
        renderer.render(scene, camera)
      }
      animate()
  
      threeReady.value = true
      initScrollAnimations()
    } catch (err) {
      handleError('3D场景初始化失败', err)
    }
  }
  
  // 模型加载器
  const loadModel = (path) => {
    return new Promise((resolve, reject) => {
      const loader = new GLTFLoader()
      loader.load(
        path,
        (gltf) => resolve(gltf),
        (xhr) => {
          progress.value = Math.round((xhr.loaded / xhr.total) * 100)
        },
        (err) => reject(err)
      )
    })
  }
  
  // 滚动动画初始化
  const initScrollAnimations = () => {
    // 飞机飞行路径
    gsap.to(airplane.position, {
      scrollTrigger: {
        trigger: '.timeline-section',
        start: 'top center',
        end: 'bottom center',
        scrub: 1
      },
      x: 10,
      y: 5,
      z: 0,
      duration: 8,
      ease: 'power2.inOut'
    })
  
    // 地球旋转
    gsap.to(scene.children[0].rotation, {
      scrollTrigger: {
        trigger: '.film-section',
        start: 'top bottom',
        end: 'bottom top',
        scrub: 1
      },
      y: Math.PI * 2,
      duration: 30
    })
  }
  
  // 错误处理
  const handleError = (message, error) => {
    console.error(message, error)
    error.value = true
    errorMessage.value = `${message}: ${error.message}`
    loading.value = false
  }
  
  // 重试机制
  const retry = () => {
    error.value = false
    loading.value = true
    initialize()
  }
  
  // 主初始化流程
  const initialize = async () => {
    try {
      await initThreeScene()
      initCameraModule()
      loading.value = false
    } catch (err) {
      handleError('初始化失败', err)
    }
  }
  
  // 生命周期
  onMounted(initialize)
  onUnmounted(() => {
    if (renderer) {
      renderer.dispose()
      renderer.forceContextLoss()
      renderer.domElement = null
      renderer = null
    }
    scene?.dispose()
    controls?.dispose()
  })
  
  // 响应式样式
  const parallaxStyle = (index) => ({
    transform: `translateY(${index % 2 === 0 ? '-' : ''}${window.scrollY * 0.2}px)`
  })
  
  const markerPosition = (index) => ({
    left: `${(index / (timeline.value.length - 1)) * 100}%`
  })
  </script>
  
  <style scoped>
  .main-container {
    position: relative;
    overflow-x: hidden;
  }
  
  .fullscreen-section {
    height: 100vh;
    background: radial-gradient(circle at center, #1a1a1a, #000);
  }
  
  .camera-wrapper {
    position: relative;
    width: 80vw;
    max-width: 800px;
    height: 60vh;
    margin: 0 auto;
    perspective: 1000px;
  }
  
  .camera-body {
    position: absolute;
    width: 100%;
    height: 100%;
    background: #333;
    border-radius: 2rem;
    transform-style: preserve-3d;
    box-shadow: 0 0 50px rgba(0, 0, 0, 0.5);
  }
  
  .camera-lens {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 30%;
    height: 30%;
    background: radial-gradient(circle, #3a3a3a 0%, #000 80%);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: all 0.3s ease;
  }
  
  .shutter-btn {
    position: absolute;
    bottom: 10%;
    left: 50%;
    transform: translateX(-50%);
    background: none;
    border: none;
    cursor: pointer;
  }
  
  .shutter-icon {
    width: 50px;
    height: 50px;
    border: 3px solid #fff;
    border-radius: 50%;
    transition: all 0.3s ease;
  }
  
  .film-section {
    height: 100vh;
    background: linear-gradient(45deg, #0a0a0a, #1a1a1a);
  }
  
  .film-track {
    display: flex;
    height: 100vh;
    align-items: center;
    padding: 0 10vw;
    gap: 5vw;
  }
  
  .film-cell {
    flex: 0 0 30vw;
    height: 40vh;
    background: #000;
    border-radius: 1rem;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.3s ease;
  }
  
  .film-cell:hover {
    transform: scale(1.05) rotateZ(2deg);
  }
  
  .timeline-section {
    position: relative;
    height: 300vh;
    background: #0f0f0f;
  }
  
  .three-viewport {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    pointer-events: none;
    z-index: -1;
  }
  
  /* 加载和错误样式 */
  .loading-screen,
  .error-screen {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: #000;
    color: #fff;
    z-index: 1000;
  }
  
  .loading-progress {
    font-size: 2rem;
    margin-top: 1rem;
  }
  </style>