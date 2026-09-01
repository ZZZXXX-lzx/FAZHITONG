<template>
  <div class="case-search">
    <div class="page-header">
      <h2>案例检索</h2>
      <p>千万级裁判文书智能检索，支持多维筛选</p>
    </div>
    <el-card class="search-box">
      <el-form :inline="true">
        <el-form-item>
          <el-input v-model="keyword" placeholder="搜索关键词（案由、法院、当事人等）" style="width:400px" clearable @clear="search" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="causeName" placeholder="案由" style="width:160px" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="courtName" placeholder="法院" style="width:160px" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="courtLevel" placeholder="法院层级" style="width:140px" clearable>
            <el-option label="基层法院" value="BASE" />
            <el-option label="中级法院" value="INTERMEDIATE" />
            <el-option label="高级法院" value="HIGH" />
            <el-option label="最高法院" value="SUPREME" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="lawArticle" placeholder="法条（如 民法典577条）" style="width:200px" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="caseYear" placeholder="年份" style="width:120px" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">检索</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <div class="search-examples">
      <span class="example-label">搜索示例：</span>
      <el-tag v-for="ex in examples" :key="ex.label" :type="ex.type || ''" class="example-tag" @click="applyExample(ex)">
        {{ ex.label }}
      </el-tag>
    </div>
    <el-table :data="cases" stripe style="margin-top:20px" @row-click="showDetail">
      <el-table-column label="匹配度" width="100" align="center">
        <template #default="{ row }">
          <el-progress :percentage="matchPercent(row.score)" :stroke-width="12" :format="() => matchPercent(row.score) + '%'" />
        </template>
      </el-table-column>
      <el-table-column label="案由" width="160">
        <template #default="{ row }">
          <span v-html="highlightText(row.causeName)"></span>
        </template>
      </el-table-column>
      <el-table-column prop="courtName" label="法院" width="200" />
      <el-table-column prop="caseYear" label="年份" width="80" />
      <el-table-column label="摘要" width="300">
        <template #default="{ row }">
          <span v-html="highlightText(row.abstractText)"></span>
        </template>
      </el-table-column>
      <el-table-column prop="judgmentResult" label="判决结果" width="120" />
    </el-table>
    <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="onPageChange" style="margin-top:20px;text-align:center" />
    <el-dialog v-model="detailVisible" title="案例详情" width="800px">
      <template v-if="currentCase">
        <h3 v-html="highlightText(currentCase.causeName)"></h3>
        <p><strong>法院：</strong>{{ currentCase.courtName }} <strong>年份：</strong>{{ currentCase.caseYear }}</p>
        <p><strong>关键词：</strong>{{ currentCase.keywords }}</p>
        <p><strong>匹配度：</strong><el-progress :percentage="matchPercent(currentCase.score)" :stroke-width="16" :format="() => matchPercent(currentCase.score) + '%'" style="display:inline-flex;width:200px;vertical-align:middle;margin-left:8px" /></p>
        <el-divider />
        <h4>摘要</h4>
        <p v-html="highlightText(currentCase.abstractText)"></p>
        <el-divider />
        <h4>争议焦点</h4>
        <p v-html="highlightText(currentCase.focusPoints)"></p>
        <el-divider />
        <h4>判决依据</h4>
        <p v-if="currentCase.judgmentBasis" v-html="highlightText(currentCase.judgmentBasis)"></p>
        <p v-else style="color:#999">暂无判决依据提炼</p>
        <el-divider />
        <h4>判决结果</h4>
        <p v-html="highlightText(currentCase.judgmentResult)"></p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { caseApi } from '@/api'

const keyword = ref('')
const causeName = ref('')
const courtName = ref('')
const caseYear = ref('')
const lawArticle = ref('')
const courtLevel = ref('')
const cases = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const detailVisible = ref(false)
const currentCase = ref(null)

const kw = computed(() => keyword.value?.trim())

const examples = [
  { label: '买卖合同纠纷', keyword: '买卖合同' },
  { label: '交通事故赔偿', keyword: '交通事故' },
  { label: '租赁合同', keyword: '租赁合同' },
  { label: '劳动合同争议', keyword: '劳动合同' },
  { label: '北京市法院案例', keyword: '', courtName: '北京' },
  { label: '2024年案例', keyword: '', caseYear: '2024' },
]

function applyExample(ex) {
  keyword.value = ex.keyword || ''
  causeName.value = ex.causeName || ''
  courtName.value = ex.courtName || ''
  caseYear.value = ex.caseYear || ''
  search()
}

async function search() {
  page.value = 1
  try {
    const data = await caseApi.search({
      keyword: kw.value || undefined,
      causeName: causeName.value || undefined,
      courtName: courtName.value || undefined,
      caseYear: caseYear.value || undefined,
      lawArticle: lawArticle.value || undefined,
      courtLevel: courtLevel.value || undefined,
      page: page.value,
      size: size.value,
    })
    cases.value = data.list || []
    total.value = data.total
  } catch {
    cases.value = []
    total.value = 0
  }
}

function onPageChange(p) { page.value = p; search() }

async function showDetail(row) {
  try {
    currentCase.value = await caseApi.getById(row.id)
    detailVisible.value = true
  } catch {
    // ignore
  }
}

function matchPercent(score) {
  if (!score || score <= 0) return 0
  const max = Math.max(...cases.value.map(c => c.score || 0), 1)
  return Math.min(Math.round((score / max) * 100), 100)
}

function highlightText(text) {
  if (!kw.value || !text) return text || ''
  const regex = new RegExp('(' + escapeRegex(kw.value) + ')', 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

function escapeRegex(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
</script>

<style scoped>
.case-search { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.search-box { margin-bottom: 0; }
.search-examples { margin-top: 12px; display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.example-label { font-size: 13px; color: #999; white-space: nowrap; }
.example-tag { cursor: pointer; }
.example-tag:hover { opacity: .8; }
:deep(.highlight) { color: #e74c3c; font-weight: 700; background: #fff3cd; padding: 0 2px; border-radius: 2px; }
</style>
