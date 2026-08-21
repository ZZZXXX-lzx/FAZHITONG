<template>
  <div class="compliance-page">
    <div class="page-header">
      <h2>企业合规体检</h2>
      <p>AI智能扫描企业法律风险，全面合规诊断</p>
    </div>
    <el-row :gutter="24">
      <el-col :span="8" v-for="item in checkItems" :key="item.name">
        <el-card class="check-card" shadow="hover">
          <div class="check-status" :class="item.status">
            <el-icon v-if="item.status === 'safe'" color="#67c23a" :size="32"><SuccessFilled /></el-icon>
            <el-icon v-else-if="item.status === 'warning'" color="#e6a23c" :size="32"><WarningFilled /></el-icon>
            <el-icon v-else color="#f56c6c" :size="32"><CloseBold /></el-icon>
          </div>
          <h3>{{ item.name }}</h3>
          <p>{{ item.desc }}</p>
          <p class="check-score">评分：<strong>{{ item.score }}</strong></p>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:24px">
      <template #header><strong>合规报告</strong></template>
      <p>上次体检时间：2026-05-28</p>
      <el-button type="primary" size="large" style="margin-top:16px" @click="runCheck">开始全面体检</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const checkItems = ref([
  { name: '劳动用工', desc: '劳动合同签订、社保缴纳、用工制度', score: 92, status: 'safe' },
  { name: '合同管理', desc: '合同签订流程、履约跟踪、档案管理', score: 78, status: 'warning' },
  { name: '知识产权', desc: '商标注册、专利保护、版权登记', score: 65, status: 'danger' },
  { name: '数据合规', desc: '个人信息保护、数据安全、隐私政策', score: 85, status: 'safe' },
  { name: '税务合规', desc: '纳税申报、发票管理、税务筹划', score: 88, status: 'safe' },
  { name: '安全生产', desc: '安全制度、应急预案、事故报告', score: 72, status: 'warning' },
])

function runCheck() {
  ElMessage.success('合规体检已启动，预计3-5分钟完成')
}
</script>

<style scoped>
.compliance-page { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.check-card { text-align: center; padding: 24px; cursor: pointer; margin-bottom: 20px; }
.check-status { margin-bottom: 16px; }
.check-card h3 { font-size: 16px; margin-bottom: 8px; }
.check-card p { color: #666; font-size: 13px; }
.check-score { margin-top: 12px; }
</style>
