import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  build: {
    // Element Plus + ECharts 全量引入较大，已通过 manualChunks 拆分
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          'vendor-element': ['element-plus'],
          'vendor-chart': ['echarts'],
          'vendor-axios': ['axios'],
        },
      },
    },
  },
  server: {
    port: 5174,
    proxy: {
      '/api/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
