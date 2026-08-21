<template>
  <div class="dashboard-page">
    <h2 style="margin-bottom:20px">数据看板</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="s in statsCards" :key="s.label">
        <el-card shadow="hover">
          <div class="stat-icon" :style="{ background: s.color }">{{ s.icon }}</div>
          <div class="stat-body">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="16">
        <el-card>
          <template #header><strong>用户类型分布</strong></template>
          <div ref="userChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><strong>认证统计</strong></template>
          <div ref="certChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <template #header><strong>律师认证状态</strong></template>
          <div class="cert-stats">
            <div class="cert-item">
              <div class="cert-num">{{ lawyerStats.total || 0 }}</div>
              <div class="cert-label">总申请</div>
            </div>
            <div class="cert-item">
              <div class="cert-num" style="color:#67c23a">{{ lawyerStats.certified || 0 }}</div>
              <div class="cert-label">已认证</div>
            </div>
            <div class="cert-item">
              <div class="cert-num" style="color:#e6a23c">{{ lawyerStats.pending || 0 }}</div>
              <div class="cert-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><strong>企业认证状态</strong></template>
          <div class="cert-stats">
            <div class="cert-item">
              <div class="cert-num">{{ enterpriseStats.total || 0 }}</div>
              <div class="cert-label">总申请</div>
            </div>
            <div class="cert-item">
              <div class="cert-num" style="color:#67c23a">{{ enterpriseStats.certified || 0 }}</div>
              <div class="cert-label">已认证</div>
            </div>
            <div class="cert-item">
              <div class="cert-num" style="color:#e6a23c">{{ enterpriseStats.pending || 0 }}</div>
              <div class="cert-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:20px">
      <template #header><strong>最新动态</strong></template>
      <el-timeline>
        <el-timeline-item v-for="log in logs" :key="log.time" :timestamp="log.time" placement="top">
          {{ log.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { userApi } from '@/api'

const statsCards = ref([
  { icon: '👥', label: '注册用户', value: '0', color: '#409eff' },
  { icon: '⚖️', label: '认证律师', value: '0', color: '#e6a23c' },
  { icon: '🏢', label: '入驻企业', value: '0', color: '#67c23a' },
  { icon: '🔧', label: '管理员', value: '0', color: '#f56c6c' },
])

const lawyerStats = ref({})
const enterpriseStats = ref({})
const userChartRef = ref(null)
const certChartRef = ref(null)
let userChart = null
let certChart = null

const logs = ref([])

async function fetchStats() {
  try {
    const data = await userApi.dashboardStats()
    statsCards.value = [
      { icon: '👥', label: '注册用户', value: String(data.totalUsers || 0), color: '#409eff' },
      { icon: '⚖️', label: '认证律师', value: String(data.lawyers || 0), color: '#e6a23c' },
      { icon: '🏢', label: '入驻企业', value: String(data.enterprises || 0), color: '#67c23a' },
      { icon: '🔧', label: '管理员', value: String(data.adminUsers || 0), color: '#f56c6c' },
    ]
    lawyerStats.value = data.lawyerInfo || {}
    enterpriseStats.value = data.enterpriseInfo || {}

    await nextTick()
    renderUserChart(data)
    renderCertChart(data)
  } catch (e) {
    // keep default values
  }
}

function renderUserChart(data) {
  if (!userChartRef.value) return
  if (userChart) userChart.dispose()
  userChart = echarts.init(userChartRef.value)
  userChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 10, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}' },
      data: [
        { value: data.totalUsers || 0, name: '普通用户', itemStyle: { color: '#409eff' } },
        { value: data.lawyers || 0, name: '律师', itemStyle: { color: '#e6a23c' } },
        { value: data.enterprises || 0, name: '企业', itemStyle: { color: '#67c23a' } },
        { value: data.adminUsers || 0, name: '管理员', itemStyle: { color: '#f56c6c' } },
      ],
    }],
  })
}

function renderCertChart(data) {
  if (!certChartRef.value) return
  if (certChart) certChart.dispose()
  certChart = echarts.init(certChartRef.value)
  const li = data.lawyerInfo || {}
  const ei = data.enterpriseInfo || {}
  certChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 10 },
    grid: { left: '8%', right: '8%', top: '10%' },
    xAxis: { type: 'category', data: ['总申请', '已认证', '待审核'] },
    yAxis: { type: 'value' },
    series: [
      { name: '律师', type: 'bar', data: [li.total || 0, li.certified || 0, li.pending || 0], itemStyle: { color: '#e6a23c' } },
      { name: '企业', type: 'bar', data: [ei.total || 0, ei.certified || 0, ei.pending || 0], itemStyle: { color: '#67c23a' } },
    ],
  })
}

function updateLogs() {
  const now = new Date()
  const time = now.toLocaleString('zh-CN')
  logs.value = [
    { time, content: '数据看板已加载' },
  ]
}

function handleResize() {
  userChart?.resize()
  certChart?.resize()
}

onMounted(() => {
  fetchStats()
  updateLogs()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userChart?.dispose()
  certChart?.dispose()
})
</script>

<style scoped>
.stat-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 24px; float: left; margin-right: 16px; }
.stat-body { overflow: hidden; }
.stat-value { font-size: 28px; font-weight: 700; color: #1a1a2e; }
.stat-label { color: #999; font-size: 14px; margin-top: 4px; }
.cert-stats { display: flex; justify-content: space-around; padding: 20px 0; }
.cert-item { text-align: center; }
.cert-num { font-size: 32px; font-weight: 700; color: #1a56db; }
.cert-label { color: #666; margin-top: 8px; font-size: 14px; }
</style>
