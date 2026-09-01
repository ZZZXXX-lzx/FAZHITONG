<template>
  <div class="dd-page">
    <div class="page-header">
      <h2>企业尽职调查</h2>
      <p>工商信息 · 涉诉风险 · 经营合规 · 尽调建议</p>
    </div>

    <el-card class="input-card">
      <el-form label-width="90px">
        <el-form-item label="目标企业">
          <el-input v-model="companyName" placeholder="请输入目标企业名称" />
        </el-form-item>
        <el-form-item label="尽调重点">
          <el-input v-model="focus" placeholder="如：股权收购、投资合作、重大合同签订（可留空）" />
        </el-form-item>
      </el-form>
      <div style="text-align:center">
        <el-button type="primary" size="large" :loading="loading" @click="generate">生成尽调报告</el-button>
      </div>
    </el-card>

    <template v-if="result">
      <el-card class="result-block">
        <template #header><strong>整体结论</strong></template>
        <p>{{ result.summary }}</p>
      </el-card>

      <el-card class="result-block">
        <template #header><strong>企业信息</strong></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="企业名称">{{ result.company?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{ result.company?.creditCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册资本">{{ result.company?.registeredCapital || '-' }}</el-descriptions-item>
          <el-descriptions-item label="法定代表人">{{ result.company?.legalRepresentative || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="result-block">
        <template #header><strong>风险清单</strong></template>
        <el-table :data="result.risks" stripe>
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag :type="riskTag(row.level)" size="small">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="desc" label="风险描述" />
          <el-table-column prop="suggestion" label="建议" width="280" />
        </el-table>
      </el-card>

      <el-card class="result-block">
        <template #header><strong>涉诉情况</strong></template>
        <el-table :data="result.litigation" stripe>
          <el-table-column prop="type" label="类型" width="160" />
          <el-table-column prop="count" label="数量" width="120" />
          <el-table-column prop="note" label="说明" />
        </el-table>
      </el-card>

      <el-card class="result-block">
        <template #header><strong>尽调建议</strong></template>
        <ul class="item-list"><li v-for="(a, i) in result.advice" :key="i">{{ typeof a === 'string' ? a : a.desc }}</li></ul>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { caseApi } from '@/api'

const companyName = ref('')
const focus = ref('')
const loading = ref(false)
const result = ref(null)

function riskTag(level) {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  return 'success'
}

async function generate() {
  if (!companyName.value.trim()) {
    ElMessage.warning('请填写目标企业名称')
    return
  }
  loading.value = true
  try {
    result.value = await caseApi.dueDiligence(companyName.value.trim(), focus.value.trim())
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dd-page { max-width: 1000px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.input-card { margin-bottom: 24px; }
.result-block { margin-bottom: 16px; }
.item-list { padding-left: 18px; }
.item-list li { margin-bottom: 8px; color: #444; }
</style>
