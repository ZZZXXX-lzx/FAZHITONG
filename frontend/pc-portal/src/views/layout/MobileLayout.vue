<template>
  <div class="mobile-layout">
    <header class="mobile-header">
      <div class="mh-logo" @click="$router.push('/')">
        <span class="mh-logo-icon">⚖️</span>
        <span class="mh-logo-text">法智通</span>
      </div>
      <div class="mh-right">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" v-if="userStore.isLoggedIn">
          <el-icon class="mh-icon" @click="$router.push('/notifications')"><Bell /></el-icon>
        </el-badge>
        <el-avatar v-if="userStore.isLoggedIn" :size="28" @click="$router.push('/mine')">{{ userStore.userInfo.nickname?.[0] }}</el-avatar>
        <el-button v-else size="small" type="primary" @click="$router.push('/login')">登录</el-button>
      </div>
    </header>

    <main class="mobile-main">
      <router-view />
    </main>

    <nav class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: isActive(tab.path) }"
        @click="goTab(tab)"
      >
        <el-icon class="tab-icon"><component :is="tab.icon" /></el-icon>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Bell, HomeFilled, ChatDotRound, Document, User, Grid } from '@element-plus/icons-vue'
import { notificationApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)

const tabs = [
  { path: '/', label: '首页', icon: HomeFilled },
  { path: '/services', label: '服务', icon: Grid },
  { path: '/consultation', label: '咨询', icon: ChatDotRound },
  { path: '/documents', label: '文书', icon: Document },
  { path: '/mine', label: '我的', icon: User },
]

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function goTab(tab) {
  router.push(tab.path)
}

async function fetchUnread() {
  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    try {
      unreadCount.value = await notificationApi.unreadCount(userStore.userInfo.id) || 0
    } catch { /* ignore */ }
  }
}

onMounted(fetchUnread)
</script>

<style scoped>
.mobile-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}
.mobile-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 16px;
  background: #1a56db;
  color: #fff;
}
.mh-logo {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.mh-logo-icon { font-size: 20px; margin-right: 6px; }
.mh-logo-text { font-size: 18px; font-weight: 700; }
.mh-right { display: flex; align-items: center; gap: 14px; }
.mh-icon { font-size: 20px; color: #fff; cursor: pointer; }
.mobile-main {
  flex: 1;
  padding-bottom: 60px;
  overflow-x: hidden;
}
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  height: 56px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding-bottom: env(safe-area-inset-bottom, 0);
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  color: #909399;
  transition: color .2s;
}
.tab-item.active { color: #1a56db; }
.tab-icon { font-size: 22px; }
.tab-label { font-size: 11px; }
</style>
