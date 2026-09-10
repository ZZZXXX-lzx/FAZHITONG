<template>
  <div class="case-search">
    <div class="page-header">
      <h2>案例检索</h2>
      <p>裁判文书智能检索，支持多维筛选 · 引用法条可溯源</p>
    </div>
    <section v-loading="statsLoading" class="stats-panel">
      <div class="stats-total">
        <div class="stats-num">{{ stats?.total ?? 0 }}</div>
        <div class="stats-cap">收录参考案例</div>
      </div>
      <div class="stats-col">
        <div class="stats-title">热门案由</div>
        <div class="stats-chips">
          <span v-for="c in stats?.causeTop || []" :key="c.name" class="chip" @click="quickSearch(c.name)">{{ c.name }} {{ c.count }}</span>
        </div>
      </div>
      <div class="stats-col">
        <div class="stats-title">常见受理法院</div>
        <div class="stats-chips">
          <span v-for="c in stats?.courtTop || []" :key="c.name" class="chip" @click="quickCourt(c.name)">{{ c.name }} {{ c.count }}</span>
        </div>
      </div>
      <div class="stats-col">
        <div class="stats-title">年份分布</div>
        <div class="stats-chips">
          <span v-for="c in stats?.yearDist || []" :key="c.name" class="chip chip-year">{{ c.name }} {{ c.count }}</span>
        </div>
      </div>
    </section>
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
          <span v-html="highlightTerms(row.causeName)"></span>
        </template>
      </el-table-column>
      <el-table-column prop="courtName" label="法院" width="200" />
      <el-table-column prop="caseYear" label="年份" width="80" />
      <el-table-column label="命中片段" min-width="300">
        <template #default="{ row }">
          <span class="snippet" v-html="snippet(row)"></span>
        </template>
      </el-table-column>
      <el-table-column label="判决结果" width="120">
        <template #default="{ row }">
          <span v-html="highlightTerms(row.judgmentResult)"></span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="onPageChange" style="margin-top:20px;text-align:center" />
    <el-dialog v-model="detailVisible" title="案例详情" width="800px">
      <template v-if="currentCase">
        <h3 v-html="highlightTerms(currentCase.causeName)"></h3>
        <p><strong>法院：</strong>{{ currentCase.courtName }} <strong>年份：</strong>{{ currentCase.caseYear }}</p>
        <p><strong>关键词：</strong><span v-html="highlightTerms(currentCase.keywords)"></span></p>
        <p><strong>匹配度：</strong><el-progress :percentage="matchPercent(currentCase.score)" :stroke-width="16" :format="() => matchPercent(currentCase.score) + '%'" style="display:inline-flex;width:200px;vertical-align:middle;margin-left:8px" /></p>
        <el-divider />
        <h4>摘要</h4>
        <p v-html="highlightTerms(currentCase.abstractText)"></p>
        <el-divider />
        <h4>争议焦点</h4>
        <p v-html="highlightTerms(currentCase.focusPoints)"></p>
        <el-divider />
        <h4>判决依据</h4>
        <p v-if="currentCase.judgmentBasis" v-html="highlightTerms(currentCase.judgmentBasis)"></p>
        <p v-else style="color:#999">暂无判决依据提炼</p>
        <div v-if="refLoading" class="ref-tip">正在解析关联法条…</div>
        <div v-else-if="lawRefs.length" class="ref-block">
          <div class="ref-head">关联法条（可点击跳转法规库查看全文）</div>
          <div v-for="(r, i) in lawRefs" :key="i" class="ref-item">
            <button class="ref-no" @click="openRef(r)">{{ r.articleNo }}</button>
            <div class="ref-main">
              <div class="ref-law">{{ r.lawTitle }}</div>
              <div v-if="r.matched" class="ref-content" :title="r.content">{{ r.content }}</div>
              <el-tag v-else size="small" type="info">该法规尚未收录</el-tag>
            </div>
          </div>
        </div>
        <el-divider />
        <h4>判决结果</h4>
        <p v-html="highlightTerms(currentCase.judgmentResult)"></p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { caseApi, regulationApi } from '@/api'

const router = useRouter()
const stats = ref(null)
const statsLoading = ref(false)
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
const refLoading = ref(false)
const lawRefs = ref([])

const kw = computed(() => keyword.value?.trim())

const terms = computed(() => {
  const t = keyword.value?.trim()
  if (!t) return []
  return t.split(/[\s,，、;；]+/).filter(x => !!x)
})

function escapeHtmlText(s) {
  if (s == null) return ''
  return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;')
}

/** 将所有关键词（空格分隔）逐词高亮，先转义防 XSS */
function highlightTerms(text) {
  if (text == null || !text) return ''
  let html = escapeHtmlText(text)
  for (const term of terms.value) {
    const esc = escapeHtmlText(term)
    if (!esc) continue
    html = html.split(esc).join('<span class="highlight">' + esc + '</span>')
  }
  return html
}

/** 命中片段：从首个命中词处截取上下文窗口，两端省略 */
function snippet(row) {
  if (!row) return ''
  const candidates = [row.focusPoints, row.abstractText, row.fullText, row.judgmentResult].filter(Boolean)
  for (const raw of candidates) {
    const t = String(raw).replace(/\s+/g, ' ').trim()
    if (!t) continue
    if (!terms.value.length) {
      return escapeHtmlText(t.slice(0, 84)) + (t.length > 84 ? '…' : '')
    }
    let best = -1
    let bestLen = 0
    const lower = t.toLowerCase()
    for (const term of terms.value) {
      if (!term) continue
      const idx = lower.indexOf(term.toLowerCase())
      if (idx >= 0 && (best < 0 || idx < best)) { best = idx; bestLen = term.length }
    }
    if (best >= 0) {
      const before = Math.max(0, best - 20)
      const after = Math.min(t.length, best + bestLen + 52)
      let html = (before > 0 ? '…' : '') + escapeHtmlText(t.slice(before, after)) + (after < t.length ? '…' : '')
      for (const term of terms.value) {
        const esc = escapeHtmlText(term)
        if (!esc) continue
        html = html.split(esc).join('<span class="highlight">' + esc + '</span>')
      }
      return html
    }
  }
  return ''
}

const examples = [
  { label: '买卖合同纠纷', keyword: '买卖合同' },
  { label: '交通事故赔偿', keyword: '交通事故' },
  { label: '离婚 财产分割', keyword: '离婚 财产分割' },
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

async function loadStats() {
  statsLoading.value = true
  try {
    stats.value = await caseApi.stats()
  } catch {
    stats.value = null
  } finally {
    statsLoading.value = false
  }
}

function quickSearch(name) {
  keyword.value = name
  causeName.value = ''
  courtName.value = ''
  caseYear.value = ''
  search()
}

function quickCourt(name) {
  courtName.value = name
  keyword.value = ''
  caseYear.value = ''
  search()
}

onMounted(() => { loadStats() })

async function showDetail(row) {
  try {
    currentCase.value = await caseApi.getById(row.id)
    detailVisible.value = true
    resolveRefs(currentCase.value.judgmentBasis)
  } catch {
    // ignore
  }
}

async function resolveRefs(quote) {
  lawRefs.value = []
  if (!quote) { refLoading.value = false; return }
  refLoading.value = true
  try {
    const data = await regulationApi.resolveRefs(quote)
    lawRefs.value = data || []
  } catch {
    lawRefs.value = []
  } finally {
    refLoading.value = false
  }
}

function openRef(r) {
  if (!r.regulationId) return
  router.push({ path: '/regulations', query: { law: r.regulationId, article: r.articleNo } })
}

function matchPercent(score) {
  if (!score || score <= 0) return 0
  const max = Math.max(...cases.value.map(c => c.score || 0), 1)
  return Math.min(Math.round((score / max) * 100), 100)
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
.stats-panel { background: linear-gradient(135deg, #1a3a8f 0%, #1a56db 100%); border-radius: 14px; padding: 22px 24px; margin-bottom: 18px; color: #fff; display: flex; align-items: flex-start; gap: 28px; flex-wrap: wrap; }
.stats-total { text-align: center; min-width: 120px; }
.stats-num { font-size: 40px; font-weight: 800; line-height: 1; }
.stats-cap { font-size: 13px; opacity: .85; margin-top: 8px; }
.stats-col { min-width: 0; }
.stats-title { font-size: 13px; opacity: .9; margin-bottom: 10px; font-weight: 600; }
.stats-chips { display: flex; flex-wrap: wrap; gap: 8px; max-width: 460px; }
.chip { font-size: 12px; padding: 4px 10px; background: rgba(255,255,255,.16); border: 1px solid rgba(255,255,255,.3); border-radius: 14px; color: #fff; cursor: pointer; }
.stats-col .chip:hover { background: #fff; color: #1a56db; }
.chip-year { cursor: default; }
@media (max-width: 760px) { .stats-panel { flex-direction: column; gap: 18px; } .stats-chips { max-width: 100%; } }
:deep(.highlight) { color: #e74c3c; font-weight: 700; background: #fff3cd; padding: 0 2px; border-radius: 2px; }
.snippet { font-size: 13px; color: #444; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.ref-tip { color: #999; font-size: 13px; padding: 6px 0; }
.ref-block { background: #f7f9ff; border: 1px solid #dfe7fb; border-radius: 10px; padding: 12px 14px; margin-top: 6px; }
.ref-head { font-size: 12px; color: #1a56db; font-weight: 600; margin-bottom: 10px; }
.ref-item { display: flex; gap: 10px; align-items: flex-start; padding: 8px 0; border-top: 1px dashed #e8edfb; }
.ref-item:first-of-type { border-top: none; padding-top: 0; }
.ref-no { flex: none; font-size: 13px; font-weight: 700; color: #1a56db; background: #fff; border: 1px solid #1a56db; border-radius: 6px; padding: 4px 8px; cursor: pointer; min-width: 88px; text-align: center; transition: all .2s; }
.ref-no:hover { background: #1a56db; color: #fff; }
.ref-main { min-width: 0; }
.ref-law { font-size: 12px; color: #666; margin-bottom: 4px; }
.ref-content { font-size: 13px; color: #333; line-height: 1.7; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
</style>
