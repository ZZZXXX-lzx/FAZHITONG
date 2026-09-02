<template>
  <div class="mine-page">
    <div class="mine-profile">
      <template v-if="userStore.isLoggedIn">
        <el-avatar :size="56">{{ userStore.userInfo.nickname?.[0] }}</el-avatar>
        <div class="mine-info">
          <div class="mine-name">{{ userStore.userInfo.nickname }}</div>
          <div class="mine-type">{{ typeLabel }}</div>
        </div>
      </template>
      <template v-else>
        <el-avatar :size="56">客</el-avatar>
        <div class="mine-info">
          <div class="mine-name">未登录</div>
          <el-button type="primary" size="small" @click="$router.push('/login')">登录 / 注册</el-button>
        </div>
      </template>
    </div>

    <div class="mine-menu">
      <div class="mine-group-title">常用功能</div>
      <div class="mine-cell" @click="$router.push('/notifications')">
        <el-icon class="mc-icon" style="color:#64748b"><Bell /></el-icon>
        <span class="mc-label">消息中心</span>
        <el-icon class="mc-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="mine-cell" @click="$router.push('/feedback')">
        <el-icon class="mc-icon" style="color:#0e9f6e"><EditPen /></el-icon>
        <span class="mc-label">意见反馈</span>
        <el-icon class="mc-arrow"><ArrowRight /></el-icon>
      </div>
      <div v-if="userStore.isLoggedIn && userStore.userType === 'LAWYER'" class="mine-group-title" style="margin-top:16px">律师服务</div>
      <div v-if="userStore.isLoggedIn && userStore.userType === 'LAWYER'" class="mine-cell" @click="$router.push('/lawyer')">
        <el-icon class="mc-icon" style="color:#1a56db"><EditPen /></el-icon>
        <span class="mc-label">律师工作台</span>
        <el-icon class="mc-arrow"><ArrowRight /></el-icon>
      </div>
      <div v-if="userStore.isLoggedIn && userStore.userType === 'LAWYER'" class="mine-cell" @click="$router.push('/lawyer/cases')">
        <el-icon class="mc-icon" style="color:#0d9488"><Folder /></el-icon>
        <span class="mc-label">案件管理</span>
        <el-icon class="mc-arrow"><ArrowRight /></el-icon>
      </div>
      <div v-if="userStore.isLoggedIn && userStore.userType === 'ENTERPRISE'" class="mine-group-title" style="margin-top:16px">企业服务</div>
      <div v-if="userStore.isLoggedIn && userStore.userType === 'ENTERPRISE'" class="mine-cell" @click="$router.push('/enterprise')">
        <el-icon class="mc-icon" style="color:#2563eb"><OfficeBuilding /></el-icon>
        <span class="mc-label">企业工作台</span>
        <el-icon class="mc-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <el-button
      v-if="userStore.isLoggedIn"
      type="danger"
      plain
      style="width:100%;margin-top:24px"
      @click="handleLogout"
    >退出登录</el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Bell, EditPen, ArrowRight, Folder, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const typeLabel = computed(() => {
  const t = userStore.userType
  if (t === 'LAWYER') return '律师账号'
  if (t === 'ENTERPRISE') return '企业账号'
  if (t === 'ADMIN') return '管理员'
  return '普通用户'
})

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.mine-page {
  padding: 16px 12px 24px;
}
.mine-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #1a56db, #3b82f6);
  border-radius: 14px;
  padding: 20px 16px;
  color: #fff;
  margin-bottom: 16px;
}
.mine-info { display: flex; flex-direction: column; gap: 8px; }
.mine-name { font-size: 18px; font-weight: 700; }
.mine-type { font-size: 13px; opacity: .9; }
.mine-menu {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
.mine-group-title {
  padding: 14px 16px 6px;
  font-size: 13px;
  color: #909399;
}
.mine-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
}
.mine-cell:last-child { border-bottom: none; }
.mine-cell:active { background: #f5f7fa; }
.mc-icon { font-size: 20px; }
.mc-label { flex: 1; font-size: 15px; color: #303133; }
.mc-arrow { font-size: 14px; color: #c0c4cc; }
</style>
