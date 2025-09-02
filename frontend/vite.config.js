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

