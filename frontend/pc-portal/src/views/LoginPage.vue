<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">法保通登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="请输入账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ account: '', password: '' })
const rules = { account: [{ required: true, message: '请输入账号' }], password: [{ required: true, message: '请输入密码' }] }

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await authApi.login(form)
    userStore.setUser(data)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a56db 0%, #0d9488 100%); }
.login-card { background: #fff; padding: 40px; border-radius: 12px; width: 400px; box-shadow: 0 20px 60px rgba(0,0,0,.15); }
.login-title { text-align: center; margin-bottom: 30px; font-size: 24px; color: #1a56db; }
.login-footer { text-align: center; margin-top: 16px; color: #666; font-size: 14px; }

@media (max-width: 480px) {
  .login-card { width: 90%; padding: 28px 20px; border-radius: 8px; }
  .login-title { font-size: 20px; }
}
</style>
