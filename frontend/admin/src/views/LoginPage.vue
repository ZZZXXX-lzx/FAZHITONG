<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">法智通管理后台</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="请输入管理员账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useAdminStore } from '@/store/admin'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ account: '', password: '' })
const rules = { account: [{ required: true, message: '请输入账号' }], password: [{ required: true, message: '请输入密码' }] }

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await request.post('/auth/login', form)
    if (data.userType !== 'ADMIN') {
      ElMessage.error('该账号不是管理员，无法登录管理后台')
      return
    }
    adminStore.setUser(data)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); }
.login-card { background: #fff; padding: 40px; border-radius: 12px; width: 400px; box-shadow: 0 20px 60px rgba(0,0,0,.3); }
.login-title { text-align: center; margin-bottom: 30px; font-size: 22px; color: #1a1a2e; font-weight: 700; }
</style>
