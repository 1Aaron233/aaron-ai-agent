import { createApp } from 'vue'
import { createHead } from '@vueuse/head'
import App from './App.vue'
import { restoreSession } from './auth'
import router from './router'
import './style.css'

const app = createApp(App)
const head = createHead()

restoreSession()
app.use(router)
app.use(head)
app.mount('#app')
