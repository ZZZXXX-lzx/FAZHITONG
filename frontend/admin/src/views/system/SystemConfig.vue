<template>
  <div>
    <h2 style="margin-bottom:20px">系统配置</h2>
    <el-card>
      <el-form label-width="120px" style="max-width:500px">
        <el-form-item label="平台名称"><el-input v-model="config.platformName" /></el-form-item>
        <el-form-item label="客服电话"><el-input v-model="config.servicePhone" /></el-form-item>
        <el-form-item label="AI模型选择">
          <el-select v-model="config.aiModel">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="文心一言" value="ernie" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册方式">
          <el-checkbox-group v-model="config.registerMethods">
            <el-checkbox label="手机号注册" />
            <el-checkbox label="邮箱注册" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const config = reactive({
  platformName: '法智通法律服务平台',
  servicePhone: '400-xxx-xxxx',
  aiModel: 'deepseek',
  registerMethods: ['手机号注册'],
})
</script>
