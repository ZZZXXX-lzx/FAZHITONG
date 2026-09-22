import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/assets/mobile.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const pinia = createPinia()
const app = createApp(App)
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: undefined })

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 应用启动时同步一次用户可见服务模块权限，反映后台角色权限调整
import { useUserStore } from '@/store/user'
useUserStore(pinia).loadPerms()

// 应用启动时加载全局系统配置（平台名称、客服电话等），并写入页面标题
import { useAppStore } from '@/store/app'
useAppStore(pinia).loadConfig()

app.mount('#app')
