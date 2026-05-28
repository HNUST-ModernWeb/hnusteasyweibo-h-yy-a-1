import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

// 必须 use 路由！
app.use(router)
app.use(ElementPlus)
app.mount('#app')