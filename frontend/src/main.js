import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import '@google/model-viewer'
import App from './App.vue'
import router from './router'
import './styles/main.scss'
import { i18n, getElementLocale } from '@/i18n'
import { initTheme } from '@/utils/theme'

// 初始化主题（必须在应用挂载前执行）
initTheme()

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(ElementPlus, { locale: getElementLocale(i18n.global.locale.value) })

app.mount('#app')
