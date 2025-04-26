import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  // 主题状态，默认为light
  const theme = ref(localStorage.getItem('theme') || 'light')
  
  // 切换主题方法
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    // 保存到localStorage
    localStorage.setItem('theme', theme.value)
    // 更新HTML根元素的data-theme属性
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  // 设置初始主题
  function initTheme() {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  return { theme, toggleTheme, initTheme }
}) 