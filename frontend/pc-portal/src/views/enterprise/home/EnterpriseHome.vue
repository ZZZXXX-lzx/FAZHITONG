<template>
  <div class="enterprise-home">
    <div class="page-header">
      <h2>企业工作台</h2>
      <p>企业法律合规管理 · 全链条法务支持</p>
    </div>
    <el-row :gutter="24">
      <el-col :span="6" v-for="item in enterpriseFeatures" :key="item.title">
        <el-card class="feature-card" shadow="hover" @click="$router.push(item.path)">
          <div class="feature-icon">{{ item.icon }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>
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
import { ref } from 'vue'

const enterprise = ref({
  name: '示例科技有限公司',
  verified: false,
  creditCode: '91110108MA****',
  scale: '小型企业',
})

const enterpriseFeatures = [
  { icon: '📄', title: '合同管理', desc: '合同全生命周期管理，智能审查风险', path: '/enterprise/contracts' },
  { icon: '🔍', title: '合规体检', desc: 'AI智能扫描企业法律风险', path: '/enterprise/compliance' },
  { icon: '💬', title: '企业咨询', desc: '专属律师团队，不限次咨询', path: '/consultation' },
  { icon: '📋', title: '合同模板', desc: '企业专属合同模板库', path: '/templates' },
]
</script>

<style scoped>
.enterprise-home { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.feature-card { text-align: center; padding: 24px 16px; cursor: pointer; }
.feature-icon { font-size: 40px; margin-bottom: 12px; }
.feature-card h3 { font-size: 16px; margin-bottom: 4px; }
.feature-card p { color: #999; font-size: 13px; }
</style>
