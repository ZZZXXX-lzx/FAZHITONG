<template>
  <div class="main-layout">
    <header class="site-header">
      <div class="header-inner">
        <button class="menu-toggle" @click="menuOpen = !menuOpen" aria-label="菜单">
          <span class="hamburger" :class="{ open: menuOpen }"></span>
        </button>
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">⚖️</span>
          <span class="logo-text">法保通</span>
        </div>
        <nav class="main-nav" :class="{ open: menuOpen }">
          <router-link to="/" @click="menuOpen = false">首页</router-link>
          <router-link to="/knowledge" @click="menuOpen = false">法律知识库</router-link>
          <router-link to="/lawyers" @click="menuOpen = false">找律师</router-link>
          <router-link to="/consultation" @click="menuOpen = false">法律咨询</router-link>
          <router-link to="/documents" @click="menuOpen = false">文书生成</router-link>
          <router-link to="/cases" @click="menuOpen = false">案例检索</router-link>
          <el-dropdown trigger="hover">
            <span class="nav-more">更多服务</span>
            <template #dropdown>
              <el-dropdown-item @click="$router.push('/templates'); menuOpen = false">合同模板</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/toolbox'); menuOpen = false">法律工具箱</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/legal-aid'); menuOpen = false">法律援助</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/lawyer-service'); menuOpen = false">律师委托</el-dropdown-item>
            </template>
          </el-dropdown>
          <template v-if="userStore.isLoggedIn">
            <router-link v-if="userStore.userType === 'LAWYER'" to="/lawyer" @click="menuOpen = false">律师工作台</router-link>
            <router-link v-if="userStore.userType === 'ENTERPRISE'" to="/enterprise" @click="menuOpen = false">企业工作台</router-link>
          </template>
        </nav>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notify-badge" v-if="userStore.isLoggedIn">
            <el-icon class="notify-icon" @click="$router.push('/notifications')"><Bell /></el-icon>
          </el-badge>
          <template v-if="userStore.isLoggedIn">
            <el-dropdown>
              <span class="user-info">
                <el-avatar :size="32">{{ userStore.userInfo.nickname?.[0] }}</el-avatar>
                <span class="username">{{ userStore.userInfo.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-item @click="$router.push('/feedback')">意见反馈</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="site-main">
      <router-view />
    </main>
    <footer class="site-footer">
      <div class="footer-inner">
        <p>© 2026 法保通 - 让每家企业都拥有自己的法务部</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Bell } from '@element-plus/icons-vue'
import { notificationApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const menuOpen = ref(false)
const unreadCount = ref(0)

async function fetchUnread() {
  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    try {
      unreadCount.value = await notificationApi.unreadCount(userStore.userInfo.id) || 0
    } catch { /* ignore */ }
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}

onMounted(() => {
  fetchUnread()
})
</script>

<style scoped>
.main-layout { min-height: 100vh; display: flex; flex-direction: column; }
.site-header { background: #fff; border-bottom: 1px solid #e4e7ed; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; height: 64px; padding: 0 20px; }
.logo { display: flex; align-items: center; cursor: pointer; margin-right: 40px; }
.logo-icon { font-size: 28px; margin-right: 8px; }
.logo-text { font-size: 22px; font-weight: 700; color: #1a56db; }
.main-nav { flex: 1; display: flex; gap: 24px; }
.main-nav a { text-decoration: none; color: #333; font-size: 15px; transition: color .2s; white-space: nowrap; }
.main-nav a:hover, .main-nav a.router-link-active { color: #1a56db; }
.header-right { display: flex; align-items: center; gap: 12px; }
.notify-badge { margin-right: 4px; }
.notify-icon { font-size: 20px; cursor: pointer; color: #606266; transition: color .2s; }
.notify-icon:hover { color: #1a56db; }
.nav-more { color: #333; font-size: 15px; cursor: pointer; white-space: nowrap; }
.nav-more:hover { color: #1a56db; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; color: #333; }
.site-main { flex: 1; background: #f5f7fa; }
.site-footer { background: #1a1a2e; color: #999; text-align: center; padding: 24px; font-size: 13px; }
.footer-inner { max-width: 1200px; margin: 0 auto; }

.menu-toggle { display: none; background: none; border: none; padding: 8px; cursor: pointer; }

@media (max-width: 768px) {
  .header-inner { padding: 0 12px; }
  .menu-toggle { display: flex; align-items: center; justify-content: center; margin-right: 8px; }
  .hamburger { display: block; width: 22px; height: 2px; background: #333; position: relative; transition: background .2s; }
  .hamburger::before, .hamburger::after { content: ''; display: block; width: 22px; height: 2px; background: #333; position: absolute; left: 0; transition: transform .2s; }
  .hamburger::before { top: -7px; }
  .hamburger::after { top: 7px; }
  .hamburger.open { background: transparent; }
  .hamburger.open::before { transform: translateY(7px) rotate(45deg); }
  .hamburger.open::after { transform: translateY(-7px) rotate(-45deg); }
  .main-nav { display: none; position: fixed; top: 64px; left: 0; right: 0; background: #fff; flex-direction: column; gap: 0; padding: 8px 0; border-bottom: 1px solid #e4e7ed; box-shadow: 0 4px 12px rgba(0,0,0,.1); z-index: 99; }
  .main-nav.open { display: flex; }
  .main-nav a { padding: 14px 20px; font-size: 16px; border-bottom: 1px solid #f0f0f0; }
  .main-nav a:last-child { border-bottom: none; }
  .logo { margin-right: auto; }
  .logo-text { font-size: 18px; }
  .username { display: none; }
}

@media (max-width: 480px) {
  .header-inner { height: 56px; }
  .main-nav { top: 56px; }
  .site-footer { padding: 16px; font-size: 12px; }
}
</style>
