<template>
  <div class="mobile-services">
    <div class="ms-banner">
      <h2>全部服务</h2>
      <p>AI 驱动的一站式法律解决方案</p>
    </div>

    <div class="ms-grid">
      <div
        v-for="item in services"
        :key="item.title"
        class="ms-cell"
        @click="goService(item)"
      >
        <div class="ms-icon" :style="{ background: item.bg, color: item.color }">
          <el-icon :size="26"><component :is="item.icon" /></el-icon>
        </div>
        <span class="ms-label">{{ item.title }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { allServicesFor } from '@/config/services'

const router = useRouter()
const userStore = useUserStore()

const services = computed(() => allServicesFor(userStore.userType, userStore.isLoggedIn))

function goService(item) {
  if (item.roles && !userStore.isLoggedIn) {
    ElMessage.info('请先登录')
    router.push('/login')
    return
  }
  router.push(item.path)
}
</script>

<style scoped>
.mobile-services {
  padding: 16px 12px 24px;
}
.ms-banner {
  background: linear-gradient(135deg, #1a56db, #3b82f6);
  color: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  margin-bottom: 16px;
}
.ms-banner h2 { font-size: 20px; margin-bottom: 6px; }
.ms-banner p { font-size: 13px; opacity: .9; }
.ms-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.ms-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 4px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: transform .15s;
}
.ms-cell:active { transform: scale(.95); }
.ms-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ms-label { font-size: 12px; color: #303133; }
</style>
