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

<<<<<<< HEAD
=======
await router.isReady()

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
app.mount('#app')
