<template>
  <div class="enterprise-home">
    <div class="page-header">
      <h2>企业工作台</h2>
      <p>企业法律合规管理 · 全链条法务支持</p>
    </div>

    <!-- 数据概览 -->
    <div class="stat-grid" v-loading="loading">
      <el-card v-for="s in stats" :key="s.label" shadow="never" class="stat-card">
        <div class="stat-num" :style="{ color: s.color }">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </el-card>
    </div>

    <!-- 功能入口 -->
    <el-row :gutter="24">
      <el-col :xs="24" :sm="12" :md="8" v-for="item in enterpriseFeatures" :key="item.title">
        <el-card class="feature-card" shadow="hover" @click="$router.push(item.path)">
          <div class="feature-icon">{{ item.icon }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 企业信息 -->
    <el-card style="margin-top:24px">
      <template #header><strong>企业信息</strong></template>
      <el-descriptions :column="2">
        <el-descriptions-item label="企业名称">{{ enterprise.name || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag :type="enterprise.verified ? 'success' : 'warning'">{{ enterprise.verified ? '已认证' : '待认证' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ enterprise.creditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业规模">{{ enterprise.scale || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-button type="primary" style="margin-top:16px">完善企业信息</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { contractApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const enterprise = ref({
  name: '示例科技有限公司',
  verified: false,
  creditCode: '91110108MA****',
  scale: '小型企业',
})

const contracts = ref([])
const ipTotal = ref(0)
const reviewPending = ref(0)
const investmentTotal = ref(0)
const loading = ref(false)

const enterpriseFeatures = [
  { icon: '📄', title: '合同管理', desc: '合同全生命周期管理，智能审查风险', path: '/enterprise/contracts' },
  { icon: '🔍', title: '合规体检', desc: 'AI智能扫描企业法律风险', path: '/enterprise/compliance' },
  { icon: '⚖️', title: '知识产权', desc: '商标、专利、著作权台账管理', path: '/enterprise/ip' },
  { icon: '💰', title: '投融资管理', desc: '融资轮次与对外投资台账', path: '/enterprise/investment' },
  { icon: '🔎', title: '法律审核', desc: '合同、文件、合规事项审核', path: '/enterprise/legal-review' },
  { icon: '💬', title: '企业咨询', desc: '专属律师团队，不限次咨询', path: '/consultation' },
  { icon: '📋', title: '合同模板', desc: '企业专属合同模板库', path: '/templates' },
]

const stats = computed(() => [
  { label: '合同总数', value: contracts.value.length, color: '#1a56db' },
  { label: '待签署合同', value: contracts.value.filter(c => c.status === 'PENDING_SIGN').length, color: '#e6a23c' },
  { label: '知识产权', value: ipTotal.value, color: '#0d9488' },
  { label: '法律审核待办', value: reviewPending.value, color: '#7c3aed' },
  { label: '投融资轮次', value: investmentTotal.value, color: '#be185d' },
])

function enterpriseId() {
  return userStore.userInfo?.enterpriseId || 1
}

onMounted(loadStats)

async function loadStats() {
  loading.value = true
  try {
    const eid = enterpriseId()
    const [c, ip, lr, inv] = await Promise.all([
      contractApi.enterpriseList({ enterpriseId: eid, page: 1, size: 100 }),
      contractApi.ipList({ enterpriseId: eid, page: 1, size: 1 }),
      contractApi.legalReviewList({ enterpriseId: eid, page: 1, size: 100 }),
      contractApi.investmentList({ enterpriseId: eid, page: 1, size: 1 }),
    ])
    contracts.value = c.list || []
    ipTotal.value = ip.total || 0
    reviewPending.value = (lr.list || []).filter(r => r.status === 'PENDING' || r.status === 'REVIEWING').length
    investmentTotal.value = inv.total || 0
  } catch {
    // 统计加载失败不阻塞页面
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.enterprise-home { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card { text-align: center; }
.stat-num { font-size: 26px; font-weight: 700; }
.stat-label { color: #999; font-size: 13px; margin-top: 6px; }
.feature-card { text-align: center; padding: 24px 16px; cursor: pointer; margin-bottom: 20px; }
.feature-icon { font-size: 40px; margin-bottom: 12px; }
.feature-card h3 { font-size: 16px; margin-bottom: 4px; }
.feature-card p { color: #999; font-size: 13px; }
@media (max-width: 768px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
