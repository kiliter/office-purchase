import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue2'

// 开发时把 /api 转发到 Spring Boot，并去掉前缀，后端路径与论文接口保持一致
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true
  }
})
