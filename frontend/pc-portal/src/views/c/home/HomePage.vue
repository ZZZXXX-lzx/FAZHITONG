<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-content">
        <h1>让每家企业都拥有自己的法务部</h1>
        <p>AI智能法律服务平台 · 7×15小时专业法律咨询 · 300,000+企业信赖</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/consultation')">免费法律咨询</el-button>
          <el-button size="large" @click="$router.push('/documents')">生成法律文书</el-button>
          <el-button size="large" @click="$router.push('/lawyers')">找律师</el-button>
        </div>
      </div>
    </section>

    <section v-if="userStore.isLoggedIn && roleWorkspace" class="workspace-section">
      <div class="section-title">
        <h2>{{ roleWorkspace.title }}</h2>
        <p>{{ roleWorkspace.desc }}</p>
      </div>
      <el-row :gutter="24">
        <el-col :span="8" v-for="item in roleWorkspace.items" :key="item.title">
          <el-card class="feature-card" shadow="hover" @click="$router.push(item.path)">
            <div class="feature-icon">{{ item.icon }}</div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="features-section">
      <div class="section-title">
        <h2>核心服务</h2>
        <p>AI驱动的一站式法律解决方案</p>
      </div>
      <el-row :gutter="24">
        <el-col :span="6" v-for="item in features" :key="item.title">
          <el-card class="feature-card" shadow="hover" @click="goFeature(item)">
            <div class="feature-icon">{{ item.icon }}</div>
            <h3>{{ item.title }}
              <el-tag v-if="item.premium && !userStore.isLoggedIn" size="small" type="warning" style="vertical-align:middle;margin-left:4px">需登录</el-tag>
            </h3>
            <p>{{ item.desc }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="stats-section">
      <el-row :gutter="24" justify="center">
        <el-col :span="6" v-for="s in stats" :key="s.label" class="stat-item">
          <div class="stat-num">{{ s.num }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const features = [
  { icon: '📝', title: '文书生成', desc: 'AI智能生成起诉状、答辩状等法律文书', path: '/documents' },
  { icon: '💬', title: '法律咨询', desc: '7×15小时在线咨询，5分钟快速响应', path: '/consultation' },
  { icon: '🔍', title: '案例检索', desc: '千万级裁判文书智能检索', path: '/cases' },
  { icon: '📖', title: '法规检索', desc: '法律法规、司法解释快速查询', path: '/regulations' },
  { icon: '📋', title: '合同模板', desc: '海量合同模板一键套用', path: '/templates' },
  { icon: '📚', title: '法律知识库', desc: '法律法规、法律常识、专业解读', path: '/knowledge' },
  { icon: '👨‍⚖️', title: '找律师', desc: '认证律师大厅，按专长精准匹配', path: '/lawyers' },
  { icon: '🛠️', title: '法律工具箱', desc: '诉讼费、利息、工伤赔偿计算器', path: '/toolbox' },
  { icon: '🤝', title: '法律援助', desc: '为经济困难群众提供免费法律服务', path: '/legal-aid', premium: true },
]

const roleWorkspace = computed(() => {
  const type = userStore.userType
  if (type === 'LAWYER') return { title: '律师工作台', desc: 'AI辅助办案、文书生成、案件管理', items: [
    { icon: '⚖️', title: 'AI文书生成', desc: '智能生成起诉状、辩护词等', path: '/lawyer/templates' },
    { icon: '📋', title: '律师资料', desc: '管理个人执业信息', path: '/lawyer/profile' },
  ]}
  if (type === 'ENTERPRISE') return { title: '企业工作台', desc: '合同管理、合规体检、法律风控', items: [
    { icon: '📄', title: '合同管理', desc: '合同审批、归档、到期提醒', path: '/enterprise/contracts' },
    { icon: '🛡️', title: '合规体检', desc: '企业合规风险智能检测', path: '/enterprise/compliance' },
  ]}
  if (type === 'ADMIN') return null
  return null
})

const stats = [
  { num: '300,000+', label: '服务企业' },
  { num: '1,000,000+', label: '交互服务' },
  { num: '200+', label: '覆盖区县' },
  { num: '99.9%', label: '满意度' },
]

function goFeature(item) {
  if (!userStore.isLoggedIn) {
    ElMessage.info('请先登录使用完整功能')
    router.push('/login')
    return
  }
  router.push(item.path)
}
</script>

<style scoped>
.hero-section { background: linear-gradient(135deg, #1a56db 0%, #0d9488 100%); color: #fff; padding: 100px 20px; text-align: center; }
.hero-content { max-width: 800px; margin: 0 auto; }
.hero-content h1 { font-size: 42px; margin-bottom: 16px; }
.hero-content p { font-size: 18px; opacity: .9; margin-bottom: 32px; }
.hero-actions { display: flex; gap: 16px; justify-content: center; }
.features-section, .workspace-section { max-width: 1200px; margin: 60px auto; padding: 0 20px; }
.section-title { text-align: center; margin-bottom: 40px; }
.section-title h2 { font-size: 28px; color: #1a1a2e; }
.section-title p { color: #666; margin-top: 8px; }
.el-col { display: flex; }
.feature-card { text-align: center; padding: 32px 16px; cursor: pointer; height: 100%; display: flex; flex-direction: column; justify-content: center; }
.feature-icon { font-size: 48px; margin-bottom: 16px; }
.feature-card h3 { font-size: 18px; margin-bottom: 8px; }
.feature-card p { color: #666; font-size: 14px; }
.stats-section { background: #fff; padding: 60px 20px; }
.stat-item { text-align: center; }
.stat-num { font-size: 36px; font-weight: 700; color: #1a56db; }
.stat-label { color: #666; margin-top: 8px; font-size: 16px; }

@media (max-width: 768px) {
  .hero-section { padding: 60px 16px; }
  .hero-content h1 { font-size: 26px; }
  .hero-content p { font-size: 14px; }
  .hero-actions { flex-direction: column; align-items: center; }
  .section-title h2 { font-size: 22px; }
  .stat-num { font-size: 24px; }
}

@media (max-width: 480px) {
  .hero-section { padding: 40px 12px; }
  .hero-content h1 { font-size: 22px; }
}
</style>
