<template>
  <div>
    <h2 style="margin-bottom:20px">系统配置</h2>
    <el-card v-loading="loading">
      <el-form label-width="140px" style="max-width:520px">
        <el-form-item label="平台名称">
          <el-input v-model="config.platformName" placeholder="显示在首页标题" />
        </el-form-item>
        <el-form-item label="客服电话">
          <el-input v-model="config.servicePhone" placeholder="显示在帮助页" />
        </el-form-item>
        <el-form-item label="AI 模型选择">
          <el-select v-model="config.aiModel" placeholder="全局大模型选型">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="文心一言" value="ernie" />
          </el-select>
        </el-form-item>
        <el-form-item label="开启注册方式">
          <el-checkbox-group v-model="config.registerMethods">
            <el-checkbox label="phone">手机号注册</el-checkbox>
            <el-checkbox label="email">邮箱注册</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue'
import { userApi } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const config = reactive({
  platformName: '法智通法律服务平台',
  servicePhone: '400-xxx-xxxx',
  aiModel: 'deepseek',
  registerMethods: ['phone'],
})

async function loadConfig() {
  loading.value = true
  try {
    const map = await userApi.systemConfig()
    if (map.platform_name) config.platformName = map.platform_name
    if (map.service_phone) config.servicePhone = map.service_phone
    if (map.ai_model) config.aiModel = map.ai_model
    if (map.register_methods) {
      try {
        config.registerMethods = JSON.parse(map.register_methods)
      } catch {}
    }
  } catch { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}

async function saveConfig() {
  saving.value = true
  try {
    await userApi.saveSystemConfig({
      platform_name: config.platformName,
      service_phone: config.servicePhone,
      ai_model: config.aiModel,
      register_methods: JSON.stringify(config.registerMethods),
    })
    ElMessage.success('保存成功，配置已生效')
    await loadConfig()
  } catch { /* 拦截器已提示 */ } finally {
    saving.value = false
  }
}

onMounted(() => loadConfig())
</script>
