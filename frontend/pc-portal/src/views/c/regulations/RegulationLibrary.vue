<template>
  <div class="regulation-library">
    <!-- Hero -->
    <section class="library-hero">
      <div class="hero-inner">
        <h1>法律法规库</h1>
        <p>收录法律法规、行政法规、司法解释等权威文件，全文检索 · 条文可溯源引用</p>
        <div class="hero-search">
          <el-input
            v-model="keyword"
            size="large"
            placeholder="输入关键词，如：试用期、LPR、损害赔偿…"
            clearable
            @keyup.enter="applySearch"
          >
            <template #append>
              <el-button type="primary" @click="applySearch">检索</el-button>
            </template>
          </el-input>
        </div>
        <div class="hero-stats">
          <div v-for="s in stats" :key="s.label" class="stat-item">
            <span class="stat-num">{{ s.count }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 分类导航 + 结果 -->
    <section class="library-body">
      <div class="category-tabs">
        <span
          v-for="c in categoryTabs"
          :key="c.value"
          class="cat-tab"
          :class="{ active: lawType === c.value }"
          @click="onCategory(c.value)"
        >
          {{ c.label }}
        </span>
      </div>

      <div v-loading="loading" class="result-area">
        <el-empty v-if="!loading && list.length === 0" description="暂无相关法规，换个关键词试试" />
        <div class="law-grid" v-else>
          <div v-for="law in list" :key="law.id" class="law-card" @click="openDetail(law)">
            <div class="law-card-top">
              <span class="law-type" :class="'t-' + typeClass(law.lawType)">{{ law.lawType || '法律' }}</span>
              <el-tag v-if="law.status && law.status !== '现行有效'" size="small" type="info">{{ law.status }}</el-tag>
              <el-tag v-else size="small" type="success">现行有效</el-tag>
            </div>
            <h3 class="law-title">{{ law.title }}</h3>
            <p class="law-desc">{{ law.content || '暂无简介' }}</p>
            <div v-if="law.matchArticles && law.matchArticles.length" class="hit-block">
              <div v-for="(m, i) in law.matchArticles" :key="'m' + i" class="hit-item">
                <span class="hit-no">{{ m.articleNo }}</span>
                <span class="hit-text">{{ snippet(m.content) }}</span>
              </div>
            </div>
            <div class="law-meta">
              <span>{{ law.issuingAuthority || '—' }}</span>
              <span>{{ law.publishDate || '—' }}</span>
              <span class="law-articles">{{ law.articleCount || 0 }} 条条文</span>
            </div>
          </div>
        </div>

        <el-pagination
          v-if="total > size"
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPageChange"
          style="margin-top: 24px; justify-content: center"
        />
      </div>
    </section>

    <!-- 法规详情：目录式条文浏览 -->
    <el-dialog v-model="detailOpen" :title="detail?.title" width="860px" top="5vh" destroy-on-close>
      <template v-if="detail">
        <div class="detail-meta">
          <el-tag size="small" :type="'primary'">{{ detail.lawType }}</el-tag>
          <span>{{ detail.issuingAuthority }}</span>
          <span>发布 {{ detail.publishDate || '—' }}</span>
          <span>施行 {{ detail.effectiveDate || '—' }}</span>
          <el-tag size="small" :type="detail.status === '现行有效' ? 'success' : 'info'">{{ detail.status || '现行有效' }}</el-tag>
        </div>
        <p v-if="detail.content" class="detail-summary">{{ detail.content }}</p>

        <div class="article-toolbar">
          <span class="toolbar-title">条文目录</span>
          <el-input v-model="articleKeyword" placeholder="在本法规内搜索条文" clearable size="small" style="width: 220px" />
        </div>

        <div v-if="detailLoading" class="article-tip">条文加载中…</div>
        <div v-else-if="filteredArticles.length === 0" class="article-tip">暂未录入全文条文</div>
        <div v-else class="article-jump">
          <button
            v-for="a in filteredArticles"
            :key="a.id"
            class="jump-chip"
            @click="scrollToArticle(a.id)"
          >{{ a.articleNo }}</button>
        </div>

        <div class="article-list">
          <div v-for="a in filteredArticles" :id="'art-' + a.id" :key="a.id" :class="highlightClass(a)">
            <div class="article-no">{{ a.articleNo }}</div>
            <div class="article-body">{{ a.content }}</div>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { regulationApi } from '@/api'

const categoryTabs = [
  { label: '全部', value: '' },
  { label: '法律', value: '法律' },
  { label: '行政法规', value: '行政法规' },
  { label: '司法解释', value: '司法解释' },
  { label: '部门规章', value: '部门规章' },
  { label: '地方性法规', value: '地方性法规' },
  { label: '国际条约', value: '国际条约' },
]

const keyword = ref('')
const lawType = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(9)
const loading = ref(false)

const stats = ref([
  { label: '法律', count: 0 },
  { label: '行政法规', count: 0 },
  { label: '司法解释', count: 0 },
  { label: '部门规章', count: 0 },
  { label: '地方性法规', count: 0 },
])
const statTypes = ['法律', '行政法规', '司法解释', '部门规章', '地方性法规']

async function fetchCounts() {
  for (let i = 0; i < statTypes.length; i++) {
    try {
      const r = await regulationApi.search({ lawType: statTypes[i], page: 1, size: 1 })
      stats.value[i].count = r.total || 0
    } catch { /* ignore */ }
  }
}

function typeClass(t) {
  if (t === '司法解释') return 'jieshi'
  if (t === '行政法规') return 'xingzheng'
  if (t === '部门规章' || t === '地方性法规') return 'guizhang'
  return 'falu'
}

function onCategory(v) {
  lawType.value = v
  page.value = 1
  load()
}

function applySearch() {
  page.value = 1
  load()
}

function onPageChange(p) {
  page.value = p
  load()
}

async function load() {
  loading.value = true
  try {
    const data = await regulationApi.search({
      keyword: keyword.value.trim() || undefined,
      lawType: lawType.value || undefined,
      page: page.value,
      size: size.value,
    })
    // 后端已一次返回 articleCount 与命中条文片段，无需逐卡请求
    list.value = (data.list || []).map(d => ({ ...d, articleCount: d.articleCount || d.articles?.length || 0, matchArticles: d.matchArticles || [] }))
    total.value = data.total || 0
  } catch {
    ElMessage.error('加载法规失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/** 命中条文片段：截取到关键词附近 */
function snippet(text) {
  if (!text) return ''
  const t = text.replace(/\s+/g, ' ').trim()
  if (t.length <= 60) return t
  return t.slice(0, 60) + '…'
}

// ---------- 详情 ----------
const detailOpen = ref(false)
const detail = ref(null)
const allArticles = ref([])
const articleKeyword = ref('')
const highlightedId = ref(null)
const detailLoading = ref(false)
const filteredArticles = computed(() => {
  const k = articleKeyword.value.trim()
  if (!k) return allArticles.value
  return allArticles.value.filter(a => (a.articleNo || '').includes(k) || (a.content || '').includes(k))
})

async function openDetail(law) {
  detail.value = law
  allArticles.value = law.articles || []
  articleKeyword.value = ''
  detailLoading.value = true
  detailOpen.value = true
  try {
    const data = await regulationApi.getDetail(law.id)
    if (data) {
      detail.value = data
      allArticles.value = data.articles || []
    }
  } catch {
    allArticles.value = []
  } finally {
    detailLoading.value = false
    // 有检索关键词时，定位到命中的第一条并高亮
    const k = keyword.value.trim()
    if (k && allArticles.value.length) {
      const idx = allArticles.value.findIndex(a => (a.content || '').includes(k))
      if (idx >= 0) {
        scrollToArticle(allArticles.value[idx].id)
        highlightedId.value = allArticles.value[idx].id
      }
    }
  }
}

function highlightClass(a) {
  return a.id === highlightedId.value ? 'article-item highlight' : 'article-item'
}

function scrollToArticle(id) {
  highlightedId.value = id
  nextTick(() => {
    const el = document.getElementById('art-' + id)
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

onMounted(() => {
  load()
  fetchCounts()
})
</script>

<style scoped>
.regulation-library { min-height: calc(100vh - 64px); }
.library-hero { background: linear-gradient(135deg, #1a3a8f 0%, #1a56db 60%, #3b7dff 100%); color: #fff; padding: 56px 20px 44px; }
.hero-inner { max-width: 1000px; margin: 0 auto; text-align: center; }
.hero-inner h1 { font-size: 34px; margin: 0 0 8px; }
.hero-inner p { opacity: .9; margin: 0 0 28px; font-size: 15px; }
.hero-search { max-width: 560px; margin: 0 auto; }
.hero-search :deep(.el-input__wrapper) { border-radius: 24px 0 0 24px; }
.hero-search :deep(.el-input-group__append) { border-radius: 0 24px 24px 0; }
.hero-stats { display: flex; justify-content: center; gap: 40px; margin-top: 34px; flex-wrap: wrap; }
.stat-num { display: block; font-size: 26px; font-weight: 700; }
.stat-label { display: block; opacity: .85; font-size: 13px; margin-top: 4px; }

.library-body { max-width: 1200px; margin: 0 auto; padding: 28px 20px; }
.category-tabs { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 20px; }
.cat-tab { padding: 8px 18px; border-radius: 20px; background: #fff; border: 1px solid #e4e7ed; color: #555; cursor: pointer; font-size: 14px; transition: all .2s; }
.cat-tab:hover { border-color: #1a56db; color: #1a56db; }
.cat-tab.active { background: #1a56db; border-color: #1a56db; color: #fff; }

.result-area { min-height: 300px; }
.law-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.law-card { background: #fff; border: 1px solid #eef0f4; border-radius: 12px; padding: 18px; cursor: pointer; transition: all .2s; display: flex; flex-direction: column; }
.law-card:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(26,86,219,.12); border-color: #c6d4f7; }
.law-card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.law-type { font-size: 12px; padding: 3px 10px; border-radius: 12px; }
.t-falu { background: #e8effc; color: #1a56db; }
.t-xingzheng { background: #e0f4f1; color: #0d8a7c; }
.t-jieshi { background: #f1e9fe; color: #7c3aed; }
.t-guizhang { background: #fdf1dd; color: #b45309; }
.law-title { font-size: 16px; margin: 0 0 8px; color: #1a3a8f; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.law-desc { font-size: 13px; color: #888; margin: 0 0 14px; line-height: 1.6; height: 42px; overflow: hidden; }
.hit-block { background: #fff8e9; border: 1px solid #f3dfae; border-radius: 8px; padding: 8px 10px; margin-bottom: 12px; }
.hit-item { display: flex; gap: 8px; font-size: 12.5px; color: #7a5c1e; line-height: 1.6; }
.hit-item + .hit-item { margin-top: 6px; }
.hit-no { flex: none; font-weight: 700; color: #b45309; }
.hit-text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.law-meta { margin-top: auto; display: flex; justify-content: space-between; color: #999; font-size: 12px; border-top: 1px dashed #eef0f4; padding-top: 10px; }
.law-articles { color: #1a56db; font-weight: 600; }

.detail-meta { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; color: #666; font-size: 13px; margin-bottom: 10px; }
.detail-summary { background: #f8faff; border-left: 3px solid #1a56db; padding: 12px 14px; border-radius: 6px; color: #444; line-height: 1.7; }
.article-toolbar { display: flex; justify-content: space-between; align-items: center; margin: 16px 0 10px; }
.toolbar-title { font-weight: 700; color: #1a3a8f; }
.article-tip { color: #999; padding: 20px; text-align: center; }
.article-jump { display: flex; flex-wrap: wrap; gap: 6px; max-height: 96px; overflow: auto; padding: 8px; background: #f6f7fb; border-radius: 8px; margin-bottom: 12px; }
.jump-chip { font-size: 12px; padding: 3px 8px; border: 1px solid #dbe2f3; border-radius: 12px; background: #fff; color: #1a56db; cursor: pointer; }
.jump-chip:hover { background: #1a56db; color: #fff; }
.article-list { max-height: 48vh; overflow-y: auto; border-top: 1px solid #eee; }
.article-item { display: flex; gap: 14px; padding: 12px 2px; border-bottom: 1px solid #f0f0f0; }
.article-item.highlight { background: #fff8e9; border-radius: 8px; padding: 12px 8px; box-shadow: inset 3px 0 0 #f5a623; }
.article-item.highlight .article-body { color: #4a3a10; }
.article-no { flex: none; font-weight: 700; color: #1a56db; width: 90px; }
.article-body { line-height: 1.8; color: #333; white-space: pre-wrap; }

@media (max-width: 900px) { .law-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 560px) { .law-grid { grid-template-columns: 1fr; } .hero-inner h1 { font-size: 26px; } }
</style>