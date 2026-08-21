<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>消息中心</h2>
      <p>查看您的通知消息，及时了解平台动态</p>
      <div class="header-actions">
        <el-button type="primary" @click="readAll" :disabled="unreadCount === 0">
          全部已读
        </el-button>
      </div>
    </div>

    <div class="notification-list" v-loading="loading">
      <el-card
        v-for="item in notifications"
        :key="item.id"
        class="notification-card"
        :class="{ unread: item.isRead === 0 }"
        shadow="hover"
        @click="markRead(item)"
      >
        <div class="notification-row">
          <div class="notification-type">
            <el-tag :type="typeColor(item.type)" size="small">
              {{ typeLabel(item.type) }}
            </el-tag>
          </div>
          <div class="notification-content">
            <div class="notification-title">
              {{ item.title }}
              <el-badge v-if="item.isRead === 0" is-dot class="unread-dot" />
            </div>
            <div class="notification-text">{{ item.content }}</div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
          </div>
        </div>
      </el-card>

      <el-empty v-if="!loading && notifications.length === 0" description="暂无消息" />
    </div>

    <el-pagination
      v-if="total > 0"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="size"
      :current-page="page"
      @current-change="onPageChange"
      style="margin-top: 20px; text-align: center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { notificationApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const notifications = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const unreadCount = ref(0)

const typeMap = {
  SYSTEM: { label: '系统', color: '' },
  ORDER: { label: '订单', color: 'success' },
  CONSULT: { label: '咨询', color: 'warning' },
  LAWYER: { label: '律师', color: 'primary' },
  OTHER: { label: '其他', color: 'info' },
}

function typeLabel(type) {
  return typeMap[type]?.label || '消息'
}

function typeColor(type) {
  return typeMap[type]?.color || 'info'
}

async function fetchNotifications() {
  loading.value = true
  try {
    const res = await notificationApi.list({
      userId: userStore.userInfo.userId,
      page: page.value,
      size: size.value,
    })
    notifications.value = res.list || []
    total.value = res.total || 0
  } catch {
    notifications.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const count = await notificationApi.unreadCount(userStore.userInfo.userId)
    unreadCount.value = count || 0
  } catch {
    unreadCount.value = 0
  }
}

async function markRead(item) {
  if (item.isRead === 1) return
  try {
    await notificationApi.read(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {
    ElMessage.error('标记已读失败')
  }
}

async function readAll() {
  try {
    await notificationApi.readAll(userStore.userInfo.userId)
    notifications.value.forEach(n => { n.isRead = 1 })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

function onPageChange(p) {
  page.value = p
  fetchNotifications()
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style scoped>
.notification-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px;
}
.page-header {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}
.page-header p {
  color: #666;
}
.header-actions {
  margin-left: auto;
}
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.notification-card {
  cursor: pointer;
  transition: all 0.2s;
}
.notification-card.unread {
  border-left: 3px solid #409eff;
}
.notification-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.notification-type {
  flex-shrink: 0;
  padding-top: 2px;
}
.notification-content {
  flex: 1;
  min-width: 0;
}
.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.unread-dot {
  margin-left: 4px;
}
.notification-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}
.notification-time {
  font-size: 12px;
  color: #999;
}
</style>
