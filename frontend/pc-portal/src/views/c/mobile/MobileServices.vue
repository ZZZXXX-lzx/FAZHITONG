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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import {
  Document, ChatDotRound, Search, Notebook, DocumentChecked, Reading,
  User, Tools, Umbrella, ScaleToOriginal, OfficeBuilding, Connection,
  Postcard, Bell, EditPen, Files, CircleCheck,
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const services = [
  { icon: Document, color: '#1a56db', bg: '#e8effc', title: '文书生成', path: '/documents' },
  { icon: ChatDotRound, color: '#0d9488', bg: '#e0f4f1', title: '法律咨询', path: '/consultation' },
  { icon: Search, color: '#2563eb', bg: '#e6edfe', title: '案例检索', path: '/cases' },
  { icon: Notebook, color: '#7c3aed', bg: '#f1e9fe', title: '法规检索', path: '/regulations' },
  { icon: DocumentChecked, color: '#b45309', bg: '#fbf0dd', title: '合同模板', path: '/templates' },
  { icon: Reading, color: '#0e9f6e', bg: '#e2f6ec', title: '法律知识库', path: '/knowledge' },
  { icon: Connection, color: '#0891b2', bg: '#e0f2f7', title: '知识图谱', path: '/knowledge-graph' },
  { icon: User, color: '#be185d', bg: '#fbe7f0', title: '找律师', path: '/lawyers' },
  { icon: Tools, color: '#d97706', bg: '#fdf1dd', title: '法律工具箱', path: '/toolbox' },
  { icon: ScaleToOriginal, color: '#7c3aed', bg: '#f1e9fe', title: '诉讼智能', path: '/litigation' },
  { icon: OfficeBuilding, color: '#0d9488', bg: '#e0f4f1', title: '尽职调查', path: '/due-diligence' },
  { icon: Umbrella, color: '#c2410c', bg: '#fdeee4', title: '法律援助', path: '/legal-aid' },
  { icon: Postcard, color: '#0e9f6e', bg: '#e2f6ec', title: '律师委托', path: '/lawyer-service' },
  { icon: Bell, color: '#64748b', bg: '#eef1f5', title: '消息中心', path: '/notifications' },
  { icon: EditPen, color: '#1a56db', bg: '#e8effc', title: '律师工作台', path: '/lawyer', role: 'LAWYER' },
  { icon: Files, color: '#2563eb', bg: '#e6edfe', title: '合同管理', path: '/enterprise/contracts', role: 'ENTERPRISE' },
  { icon: CircleCheck, color: '#0e9f6e', bg: '#e2f6ec', title: '合规体检', path: '/enterprise/compliance', role: 'ENTERPRISE' },
  { icon: OfficeBuilding, color: '#b45309', bg: '#fbf0dd', title: '知识产权', path: '/enterprise/ip', role: 'ENTERPRISE' },
]

function goService(item) {
  if (item.role && userStore.userType !== item.role) {
    ElMessage.warning('该功能仅限对应账号使用')
    return
  }
  if (item.path === '/notifications' || item.path === '/legal-aid' || item.path === '/lawyer-service' || item.role) {
    if (!userStore.isLoggedIn) {
      ElMessage.info('请先登录')
      router.push('/login')
      return
    }
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
