import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') },
  },
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  server: {
    port: 8888,
    strictPort: true,
    proxy: {
      // 后端 Spring Boot 运行在 localhost:8080
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 商品图片静态资源（FileStorageService 映射到 /static/**）
      '/static': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
