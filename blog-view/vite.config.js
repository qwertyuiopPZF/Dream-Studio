import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const devProxyTarget = process.env.VITE_DEV_PROXY_TARGET || 'http://localhost:8080'

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
        target: devProxyTarget,
        changeOrigin: true,
      },
      '/admin': {
        target: devProxyTarget,
        changeOrigin: true,
      },
      '/upload': {
        target: devProxyTarget,
        changeOrigin: true,
      },
      '/images': {
        target: devProxyTarget,
        changeOrigin: true,
      },
    },
  },

  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return undefined

          if (id.includes('mermaid') || id.includes('katex')) return 'editor-rich'

          if (id.includes('@codemirror/lang-')) return 'editor-lang'

          if (id.includes('@lezer')) return 'editor-lezer'

          if (id.includes('codemirror') || id.includes('@codemirror')) {
            return 'editor-codemirror'
          }

          if (id.includes('highlight.js') || id.includes('prismjs')) return 'editor-highlight'

          if (id.includes('md-editor-v3') || id.includes('markdown-it')) return 'editor-core'

          if (id.includes('@element-plus/icons-vue')) return 'element-plus-icons'

          if (id.includes('element-plus')) {
            if (
              id.includes('/components/table') ||
              id.includes('/components/pagination') ||
              id.includes('/components/empty') ||
              id.includes('/components/image') ||
              id.includes('/components/upload') ||
              id.includes('/components/avatar')
            ) {
              return 'element-plus-data'
            }

            if (
              id.includes('/components/form') ||
              id.includes('/components/input') ||
              id.includes('/components/select') ||
              id.includes('/components/switch') ||
              id.includes('/components/dialog') ||
              id.includes('/components/button') ||
              id.includes('/components/tag') ||
              id.includes('/components/message') ||
              id.includes('/components/message-box')
            ) {
              return 'element-plus-form'
            }

            return 'element-plus-core'
          }

          if (id.includes('vue-advanced-chat')) {
            return 'chat-vendor'
          }

          if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) {
            return 'vue-vendor'
          }

          if (id.includes('axios')) {
            return 'network-vendor'
          }

          return 'vendor'
        },
      },
    },
  },
})
