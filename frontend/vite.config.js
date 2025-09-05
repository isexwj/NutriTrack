import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173,
    open: false,
    proxy: {
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      },
      '/images': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/dashboard': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/meal': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/meals': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/notifications': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/community': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ai': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})

// export default {
//   server: {
//     port: 5173, // 默认是 5173
//     open: true, // 启动自动打开浏览器
//     proxy: {
//       '/api': 'http://localhost:8080'
//     }
//   }
// }

