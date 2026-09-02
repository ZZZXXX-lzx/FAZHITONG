<template>
  <div class="mobile-home">
    <div class="mh-banner">
      <h1>让每家企业都拥有自己的法务部</h1>
      <p>AI 智能法律服务 · 7×15 小时在线咨询</p>
    </div>

    <div class="mh-stats">
      <div class="mh-stat">
        <div class="mh-stat-num">30万+</div>
        <div class="mh-stat-label">服务企业</div>
      </div>
      <div class="mh-stat">
        <div class="mh-stat-num">100万+</div>
        <div class="mh-stat-label">交互服务</div>
      </div>
      <div class="mh-stat">
        <div class="mh-stat-num">99.9%</div>
        <div class="mh-stat-label">满意度</div>
      </div>
    </div>

    <div class="mh-section">
      <div class="mh-section-title">核心服务</div>
      <div class="mh-grid">
        <div
          v-for="item in services"
          :key="item.title"
          class="mh-cell"
          @click="goService(item)"
        >
          <div class="mh-icon" :style="{ background: item.bg, color: item.color }">
            <el-icon :size="26"><component :is="item.icon" /></el-icon>
          </div>
          <span class="mh-label">{{ item.title }}</span>
        </div>
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
  User, Tools, ScaleToOriginal, OfficeBuilding, Connection,
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const services = [
  { icon: Document, color: '#1a56db', bg: '#e8effc', title: '文书生成', path: '/documents' },
  { icon: ChatDotRound, color: '#0d9488', bg: '#e0f4f1', title: '法律咨询', path: '/consultation' },
  { icon: Search, color: '#2563eb', bg: '#e6edfe', title: '案例检索', path: '/cases' },
  { icon: Notebook, color: '#7c3aed', bg: '#f1e9fe', title: '法规检索', path: '/regulations' },
  { icon: DocumentChecked, color: '#b45309', bg: '#fbf0dd', title: '合同模板', path: '/templates' },
  { icon: Reading, color: '#0e9f6e', bg: '#e2f6ec', title: '知识库', path: '/knowledge' },
  { icon: Connection, color: '#0891b2', bg: '#e0f2f7', title: '知识图谱', path: '/knowledge-graph' },
  { icon: User, color: '#be185d', bg: '#fbe7f0', title: '找律师', path: '/lawyers' },
  { icon: Tools, color: '#d97706', bg: '#fdf1dd', title: '工具箱', path: '/toolbox' },
  { icon: ScaleToOriginal, color: '#7c3aed', bg: '#f1e9fe', title: '诉讼智能', path: '/litigation' },
  { icon: OfficeBuilding, color: '#0d9488', bg: '#e0f4f1', title: '尽职调查', path: '/due-diligence' },
]

function goService(item) {
  if (!userStore.isLoggedIn) {
    ElMessage.info('请先登录使用完整功能')
    router.push('/login')
    return
  }
  router.push(item.path)
}
</script>

<style scoped>
.mobile-home {
  padding: 16px 12px 24px;
}
.mh-banner {
  background: linear-gradient(135deg, #1a56db, #3b82f6);
  color: #fff;
  border-radius: 14px;
  padding: 22px 18px;
  margin-bottom: 14px;
}
.mh-banner h1 { font-size: 20px; margin-bottom: 8px; line-height: 1.4; }
.mh-banner p { font-size: 13px; opacity: .9; }
.mh-stats {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 16px 0;
  margin-bottom: 16px;
}
.mh-stat { flex: 1; text-align: center; }
.mh-stat-num { font-size: 18px; font-weight: 700; color: #1a56db; }
.mh-stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.mh-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.mh-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.mh-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 4px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: transform .15s;
}
.mh-cell:active { transform: scale(.95); }
.mh-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.mh-label { font-size: 12px; color: #303133; }
</style>
