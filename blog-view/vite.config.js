import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

<<<<<<< HEAD
=======
const devProxyTarget = process.env.VITE_DEV_PROXY_TARGET || 'http://localhost:8080'
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8

export default defineConfig({
  plugins: [vue(), vueDevTools()],

  define: {
    'process.env': {},
  },

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

  server: {
    port: 3000,
    host: '0.0.0.0',
    open: true,

    proxy: {
      '/api': {
<<<<<<< HEAD
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/admin': {
        target: 'http://localhost:8081',
=======
        target: devProxyTarget,
        changeOrigin: true,
      },
      '/admin': {
        target: devProxyTarget,
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        changeOrigin: true,
      },
    },
  },
})
