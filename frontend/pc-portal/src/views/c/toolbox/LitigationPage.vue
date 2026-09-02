<template>
  <div class="litigation-page">
    <div class="page-header">
      <h2>诉讼智能助手</h2>
      <p>案件分析 · 诉讼时间轴 · 庭审提纲 · 质证要点 · 判决预测</p>
    </div>

    <el-card class="input-card">
      <el-form label-width="90px">
        <el-form-item label="案由">
          <el-input v-model="cause" placeholder="如：买卖合同纠纷、劳动争议、民间借贷..." />
        </el-form-item>
        <el-form-item label="案情描述">
          <el-input type="textarea" v-model="description" :rows="6" placeholder="请描述案件基本情况，例如：甲方与乙方签订买卖合同，乙方拖欠货款10万元，多次催告未果..." />
        </el-form-item>
      </el-form>
      <div style="text-align:center">
        <el-button type="primary" size="large" :loading="loading" @click="analyze">智能分析</el-button>
      </div>
    </el-card>

    <template v-if="result">
      <el-card v-if="result.prediction" class="result-block">
        <template #header><strong>判决预测</strong></template>
        <div class="prediction">
          <el-tag :type="predictionTag" size="large">{{ result.prediction.outcome }}</el-tag>
          <span class="prob">胜诉可能性：<strong>{{ result.prediction.probability }}</strong></span>
        </div>
        <p class="prediction-reason">{{ result.prediction.reason }}</p>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-card class="result-block">
            <template #header><strong>争议焦点</strong></template>
            <ul class="item-list"><li v-for="(f, i) in result.focus" :key="i">{{ f }}</li></ul>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-card class="result-block">
            <template #header><strong>证据建议</strong></template>
            <ul class="item-list"><li v-for="(e, i) in result.evidence" :key="i">{{ e }}</li></ul>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="result-block">
        <template #header><strong>诉讼时间轴</strong></template>
        <el-timeline>
          <el-timeline-item v-for="(t, i) in result.timeline" :key="i" :timestamp="t.stage" placement="top">
            <span v-if="typeof t === 'string'">{{ t }}</span>
            <span v-else>{{ t.desc }}</span>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-card class="result-block">
            <template #header><strong>庭审提纲</strong></template>
            <ul class="item-list"><li v-for="(t, i) in result.trialOutline" :key="i">{{ typeof t === 'string' ? t : t.desc }}</li></ul>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-card class="result-block">
            <template #header><strong>质证要点</strong></template>
            <ul class="item-list"><li v-for="(c, i) in result.crossExam" :key="i">{{ typeof c === 'string' ? c : c.desc }}</li></ul>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { caseApi } from '@/api'

const cause = ref('')
const description = ref('')
const loading = ref(false)
const result = ref(null)

const predictionTag = computed(() => {
  const o = result.value?.prediction?.outcome
  if (o === '胜诉') return 'success'
  if (o === '败诉') return 'danger'
  if (o === '部分胜诉') return 'warning'
  return 'info'
})

async function analyze() {
  if (!description.value.trim()) {
    ElMessage.warning('请填写案情描述')
    return
  }
  loading.value = true
  try {
    result.value = await caseApi.litigation(cause.value.trim(), description.value.trim())
  } catch {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.litigation-page { max-width: 1000px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.input-card { margin-bottom: 24px; }
.result-block { margin-bottom: 16px; }
.item-list { padding-left: 18px; }
.item-list li { margin-bottom: 8px; color: #444; }
.prediction { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
.prob { color: #666; }
.prediction-reason { color: #888; font-size: 14px; }
</style>
