import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/theme.scss'

import App from './App.vue'
import router from './router'
import { useUserStore } from './store/user'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

useUserStore(pinia).hydrateProfile(localStorage.getItem('accessToken') || '')

await router.isReady()

app.mount('#app')
