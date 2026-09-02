<template>
  <div class="compliance-page">
    <div class="page-header">
      <h2>企业合规体检</h2>
      <p>问卷式自检 · 逐题作答 · 自动生成合规报告</p>
    </div>

    <!-- 报告概览 -->
    <el-card v-if="report && !answering" class="report-overview" shadow="never">
      <div class="overview-head">
        <div>
          <div class="total-score">{{ report.totalScore }}<span>分</span></div>
          <el-tag :type="levelTag(report.level)" size="large">{{ levelText(report.level) }}</el-tag>
        </div>
        <el-button type="primary" @click="startAnswer">重新体检</el-button>
      </div>
      <el-row :gutter="16" class="domain-grid">
        <el-col :xs="12" :sm="8" v-for="d in report.domains" :key="d.domain">
          <div class="domain-card">
            <div class="domain-name">{{ d.name }}</div>
            <div class="domain-score" :style="{ color: scoreColor(d.score) }">{{ d.score }}分</div>
          </div>
        </el-col>
      </el-row>
      <div v-if="report.highRiskItems && report.highRiskItems.length" class="risk-list">
        <h4>高风险项</h4>
        <ul>
          <li v-for="(item, i) in report.highRiskItems" :key="i">{{ item.content }}</li>
        </ul>
      </div>
      <div v-if="report.suggestions && report.suggestions.length" class="suggestions">
        <h4>整改建议</h4>
        <ul>
          <li v-for="(s, i) in report.suggestions" :key="i">{{ s }}</li>
        </ul>
      </div>
    </el-card>

    <!-- 问卷作答 -->
    <template v-if="answering">
      <el-card v-for="(d, di) in domains" :key="d.domain" class="domain-block" shadow="never">
        <template #header>
          <div class="domain-header">
            <strong>{{ d.name }}</strong>
            <span class="progress">已答 {{ answeredCount(d) }} / {{ d.questions.length }}</span>
          </div>
        </template>
        <div v-for="q in d.questions" :key="q.id" class="question-item">
          <div class="question-content">{{ q.content }}</div>
          <el-radio-group v-model="q.answer" @change="onAnswer(d, q)">
            <el-radio-button label="YES">是</el-radio-button>
            <el-radio-button label="NO">否</el-radio-button>
            <el-radio-button label="NA">不适用</el-radio-button>
          </el-radio-group>
        </div>
      </el-card>
      <div class="action-bar">
        <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReport">提交并生成报告</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { complianceApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const domains = ref([])
const answering = ref(false)
const report = ref(null)
const saving = ref(false)
const submitting = ref(false)

function enterpriseId() {
  return userStore.userInfo?.enterpriseId || 1
}

function levelText(level) {
  if (level === 'LOW') return '低风险'
  if (level === 'MEDIUM') return '中风险'
  return '高风险'
}
function levelTag(level) {
  if (level === 'LOW') return 'success'
  if (level === 'MEDIUM') return 'warning'
  return 'danger'
}
function scoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}
function answeredCount(d) {
  return d.questions.filter(q => q.answer).length
}

onMounted(async () => {
  try {
    report.value = await complianceApi.report(enterpriseId())
  } catch {
    // 无历史报告，直接进入问卷
    report.value = null
  }
  if (!report.value) {
    startAnswer()
  }
})

async function startAnswer() {
  const data = await complianceApi.questions(enterpriseId())
  domains.value = data.domains || []
  answering.value = true
}

function onAnswer() {
  // 前端即时状态，提交时统一持久化
}

function collectAnswers() {
  const answers = []
  for (const d of domains.value) {
    for (const q of d.questions) {
      if (q.answer) {
        answers.push({ questionId: q.id, answer: q.answer })
      }
    }
  }
  return answers
}

async function saveDraft() {
  saving.value = true
  try {
    await complianceApi.submitAnswers({ enterpriseId: enterpriseId(), answers: collectAnswers(), submit: false })
    ElMessage.success('草稿已保存')
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function submitReport() {
  const unanswered = collectAnswers().length
  if (unanswered === 0) {
    ElMessage.warning('请至少作答一道题后再提交')
    return
  }
  submitting.value = true
  try {
    const res = await complianceApi.submitAnswers({ enterpriseId: enterpriseId(), answers: collectAnswers(), submit: true })
    report.value = res.report
    answering.value = false
    ElMessage.success('报告生成成功')
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.compliance-page { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }

.report-overview { margin-bottom: 24px; }
.overview-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.total-score { font-size: 40px; font-weight: 700; color: #1a56db; line-height: 1; margin-bottom: 8px; }
.total-score span { font-size: 16px; font-weight: 400; color: #666; margin-left: 4px; }
.domain-grid { margin-top: 16px; }
.domain-card { background: #f5f7fa; border-radius: 8px; padding: 16px; text-align: center; }
.domain-name { font-size: 14px; color: #666; margin-bottom: 8px; }
.domain-score { font-size: 24px; font-weight: 700; }
.risk-list, .suggestions { margin-top: 20px; }
.risk-list h4, .suggestions h4 { font-size: 15px; margin-bottom: 10px; }
.risk-list ul, .suggestions ul { padding-left: 20px; }
.risk-list li { color: #f56c6c; margin-bottom: 6px; }
.suggestions li { color: #555; margin-bottom: 6px; }

.domain-block { margin-bottom: 20px; }
.domain-header { display: flex; justify-content: space-between; align-items: center; }
.progress { font-size: 13px; color: #999; }
.question-item { padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.question-item:last-child { border-bottom: none; }
.question-content { margin-bottom: 12px; font-size: 15px; color: #333; }
.action-bar { text-align: center; padding: 24px 0; }
</style>
