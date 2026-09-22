import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'

/**
 * 全局系统配置（后台「系统配置」页可动态调整，前端全站生效）
 * 配置键：platform_name / service_phone / ai_model / register_methods 等
 */
export const useAppStore = defineStore('app', () => {
  const config = ref({})
  const loaded = ref(false)

  const platformName = computed(() => config.value.platform_name || '法智通')
  const servicePhone = computed(() => config.value.service_phone || '')

  async function loadConfig() {
    try {
      const cfg = await userApi.systemConfig()
      if (cfg && typeof cfg === 'object') config.value = cfg
    } catch {
      /* 拉取失败保持默认值，不影响页面 */
    } finally {
      loaded.value = true
      applyTitle()
    }
  }

  function applyTitle() {
    document.title = platformName.value + ' - 智能法律服务平台'
  }

  return { config, loaded, platformName, servicePhone, loadConfig, applyTitle }
})
