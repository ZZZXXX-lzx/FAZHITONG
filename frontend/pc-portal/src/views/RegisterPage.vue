<template>
  <div class="register-page">
    <div class="register-card">
      <h2 class="register-title">法保通注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="请输入账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" prefix-icon="Edit" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio value="USER">个人用户</el-radio>
            <el-radio value="LAWYER">律师</el-radio>
            <el-radio value="ENTERPRISE">企业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <el-button link type="primary" @click="$router.push('/login')">立即登录</el-button>
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
const form = reactive({ account: '', password: '', confirmPassword: '', nickname: '', phone: '', userType: 'USER' })
const rules = {
  account: [{ required: true, message: '请输入账号' }],
  password: [{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6位' }],
  confirmPassword: [{ required: true, message: '请确认密码' }, {
    validator: (rule, value) => value === form.password || '两次密码不一致'
  }],
  nickname: [{ required: true, message: '请输入昵称' }],
  phone: [{ required: true, message: '请输入手机号' }],
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await authApi.register({
      account: form.account,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone,
      userType: form.userType,
    })
    userStore.setUser(data)
    ElMessage.success('注册成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a56db 0%, #0d9488 100%); }
.register-card { background: #fff; padding: 40px; border-radius: 12px; width: 440px; box-shadow: 0 20px 60px rgba(0,0,0,.15); }
.register-title { text-align: center; margin-bottom: 30px; font-size: 24px; color: #1a56db; }
.register-footer { text-align: center; margin-top: 16px; color: #666; font-size: 14px; }

@media (max-width: 480px) {
  .register-card { width: 90%; padding: 28px 20px; border-radius: 8px; }
  .register-title { font-size: 20px; }
}
</style>
