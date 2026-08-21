<template>
  <div v-if="isLoginPage" class="login-wrapper">
    <router-view />
  </div>
  <el-container v-else class="admin-layout">
    <el-aside width="220px" class="admin-sidebar">
      <div class="sidebar-logo" @click="$router.push('/')">
        <span>⚖️ 法智通</span>
      </div>
      <el-menu router :default-active="route.path" background-color="#1a1a2e" text-color="#fff" active-text-color="#409eff">
        <el-menu-item index="/">
          <el-icon><data-board /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><user /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/lawyers">
          <el-icon><briefcase /></el-icon>
          <span>律师管理</span>
        </el-menu-item>
        <el-menu-item index="/enterprises">
          <el-icon><office-building /></el-icon>
          <span>企业管理</span>
        </el-menu-item>
        <el-menu-item index="/content">
          <el-icon><document /></el-icon>
          <span>内容管理</span>
        </el-menu-item>
        <el-menu-item index="/knowledge-manage">
          <el-icon><reading /></el-icon>
          <span>知识库管理</span>
        </el-menu-item>
        <el-menu-item index="/finance">
          <el-icon><money /></el-icon>
          <span>财务管理</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/system/roles">角色权限</el-menu-item>
          <el-menu-item index="/system/config">系统配置</el-menu-item>
          <el-menu-item index="/system/logs">操作日志</el-menu-item>
          <el-menu-item index="/system/feedback">反馈管理</el-menu-item>
          <el-menu-item index="/system/legal-aid">法律援助审核</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span>管理后台</span>
        <el-dropdown>
          <span class="admin-user">
            <el-avatar :size="28">{{ adminStore.userInfo.nickname?.[0] || '管' }}</el-avatar>
            <span>{{ adminStore.userInfo.nickname || '管理员' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/store/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const isLoginPage = computed(() => route.path === '/login')

function handleLogout() {
  adminStore.logout()
  router.push('/login')
}
</script>

<style>
.admin-layout { height: 100vh; }
.admin-sidebar { background: #1a1a2e; overflow-y: auto; }
.sidebar-logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: 700; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,.1); }
.admin-header { background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; border-bottom: 1px solid #e4e7ed; font-size: 16px; font-weight: 600; }
.admin-user { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.admin-main { background: #f5f7fa; padding: 20px; }
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>
