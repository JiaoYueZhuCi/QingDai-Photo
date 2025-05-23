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
      '@components': path.resolve(__dirname, './src/components'),
      '@views': path.resolve(__dirname, './src/views'),
      '@types': path.resolve(__dirname, './src/types'),
      '@store': path.resolve(__dirname, './src/store'),
      '@api': path.resolve(__dirname, './src/api'),
      '@utils': path.resolve(__dirname, './src/utils')
    },
  },
  server: {
    host: '0.0.0.0', // 允许所有 IP 访问
    port: 80, 
    proxy: {
      '/api': {
        // target: process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : 'https://qingdai.art:8080',
        target: process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : 'http://qingdai-sp:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      }
    }
  }
})
