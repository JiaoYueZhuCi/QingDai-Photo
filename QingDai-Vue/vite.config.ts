import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import path from 'path'

// 开发环境才引入devtools
const plugins = process.env.NODE_ENV === 'development' 
  ? [vueDevTools(), vue()] 
  : [vue()]

export default defineConfig({
  plugins: plugins,
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@/types': path.resolve(__dirname, './src/types')
    },
  },
  server: {
    host: '0.0.0.0', // 允许所有 IP 访问
    port: 80, 
    proxy: {
      '/api': {
        target: process.env.NODE_ENV === 'development' ?   'http://localhost:8080':'http://qingdai-sp:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
