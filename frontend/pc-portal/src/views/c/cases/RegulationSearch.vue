<template>
  <div class="regulation-page">
    <div class="page-header">
      <h2>法规检索</h2>
      <p>查询法律法规、司法解释，快速了解法律依据</p>
    </div>

    <el-card class="search-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="keyword" placeholder="输入法规名称或关键词，如：民法典、劳动合同、民间借贷" clearable style="width: 420px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="lawType" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="法律" value="法律" />
            <el-option label="行政法规" value="行政法规" />
            <el-option label="司法解释" value="司法解释" />
            <el-option label="部门规章" value="部门规章" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">检索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-for="item in list" :key="item.id" class="reg-item" shadow="hover" @click="viewDetail(item)">
      <div class="reg-title">
        {{ item.title }}
        <el-tag v-if="item.status === '现行有效'" type="success" size="small">现行有效</el-tag>
      </div>
      <div class="reg-meta">
        <el-tag size="small" type="info">{{ item.lawType }}</el-tag>
        <span>{{ item.issuingAuthority }}</span>
        <span>施行：{{ item.effectiveDate || '—' }}</span>
      </div>
      <p class="reg-abstract">{{ item.content }}</p>
    </el-card>

    <el-empty v-if="!loading && !list.length" description="未检索到相关法规" />
    <el-pagination v-if="total > size" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="onPageChange" style="margin-top:20px;text-align:center" />

    <el-dialog v-model="detailVisible" :title="detail?.title" width="760px" top="6vh">
      <template v-if="detail">
        <div class="reg-meta" style="margin-bottom:16px">
          <el-tag size="small" type="info">{{ detail.lawType }}</el-tag>
          <span>{{ detail.issuingAuthority }}</span>
          <span>发布：{{ detail.publishDate || '—' }}</span>
          <span>施行：{{ detail.effectiveDate || '—' }}</span>
          <el-tag v-if="detail.status === '现行有效'" type="success" size="small">现行有效</el-tag>
        </div>
        <div class="reg-content" v-if="detail.content">{{ detail.content }}</div>

        <div v-if="articles.length" style="margin-top:16px">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
            <strong>条文（{{ filteredArticles.length }}/{{ articles.length }}）</strong>
            <el-input v-model="articleKeyword" placeholder="在本法条内搜索，如：试用期、赔偿" clearable size="small" style="width:200px" />
          </div>
          <div class="article-list">
            <div class="article-item" v-for="a in filteredArticles" :key="a.id">
              <div class="article-no">{{ a.articleNo }}</div>
              <div class="article-body">{{ a.content }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else-if="detailLoaded" description="暂未录入条文全文" style="margin-top:8px" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { regulationApi } from '@/api'

const keyword = ref('')
const lawType = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const articles = ref([])
const articleKeyword = ref('')
const detailLoaded = ref(false)

const filteredArticles = computed(() => {
  const k = articleKeyword.value.trim()
  if (!k) return articles.value
  const lower = k.toLowerCase()
  return articles.value.filter((a) =>
    (a.articleNo || '').toLowerCase().includes(lower) || (a.content || '').toLowerCase().includes(lower))
})

onMounted(search)

async function search() {
  page.value = 1
  await load()
}

async function load() {
  loading.value = true
  try {
    const data = await regulationApi.search({ keyword: keyword.value || undefined, lawType: lawType.value || undefined, page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    ElMessage.error('检索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function onPageChange(p) { page.value = p; load() }

async function viewDetail(item) {
  detail.value = item
  detailVisible.value = true
  articles.value = []
  articleKeyword.value = ''
  detailLoaded.value = false
  try {
    const data = await regulationApi.getDetail(item.id)
    if (data) {
      detail.value = data
      articles.value = (data && data.articles) || []
    }
  } catch {
    articles.value = []
  } finally {
    detailLoaded.value = true
  }
}
</script>

<style scoped>
.regulation-page { max-width: 1000px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.search-bar { margin-bottom: 20px; }
.reg-item { margin-bottom: 14px; cursor: pointer; }
.reg-title { font-size: 16px; font-weight: 600; color: #1a1a2e; display: flex; align-items: center; gap: 8px; }
.reg-meta { display: flex; align-items: center; gap: 14px; color: #999; font-size: 13px; margin: 8px 0; }
.reg-abstract { color: #666; font-size: 14px; line-height: 1.7; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.reg-content { white-space: pre-wrap; line-height: 1.8; color: #333; background: #f9f9f9; padding: 16px; border-radius: 8px; max-height: 460px; overflow-y: auto; }
.article-list { max-height: 480px; overflow-y: auto; border-top: 1px solid #eee; }
.article-item { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.article-no { flex: none; font-weight: 600; color: #1a56db; width: 80px; }
.article-body { line-height: 1.8; color: #333; white-space: pre-wrap; }
</style>
