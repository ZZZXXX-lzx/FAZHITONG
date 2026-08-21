<template>
  <div class="knowledge-page">
    <div class="page-header">
      <h2>法律知识库</h2>
      <p>专业法律知识文章，助您了解法律常识与实务要点</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索文章标题、关键词"
        clearable
        style="width: 360px"
        @clear="fetchArticles"
        @keyup.enter="fetchArticles"
      >
        <template #append>
          <el-button @click="fetchArticles">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="content-wrapper">
      <aside class="category-aside">
        <el-menu :default-active="String(activeCategory)" @select="onCategorySelect">
          <el-menu-item index="0">
            <el-icon><Files /></el-icon>
            <span>全部文章</span>
          </el-menu-item>
          <el-menu-item
            v-for="cat in categories"
            :key="cat.id"
            :index="String(cat.id)"
          >
            {{ cat.name }}
          </el-menu-item>
        </el-menu>
      </aside>

      <div class="article-list" v-loading="loading">
        <el-card
          v-for="article in articles"
          :key="article.id"
          class="article-card"
          shadow="hover"
          @click="goDetail(article.id)"
        >
          <div class="article-title">{{ article.title }}</div>
          <div class="article-summary">{{ article.summary }}</div>
          <div class="article-meta">
            <div class="article-tags">
              <el-tag
                v-for="tag in parseTags(article.tags)"
                :key="tag"
                size="small"
                type="info"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="article-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ article.viewCount || 0 }} 浏览
              </span>
              <span class="stat-time">{{ formatTime(article.createTime) }}</span>
            </div>
          </div>
        </el-card>

        <el-empty v-if="!loading && articles.length === 0" description="暂无文章" />

        <el-pagination
          v-if="total > 0"
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPageChange"
          style="margin-top: 20px; text-align: center"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Files, View } from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api'

const router = useRouter()

const keyword = ref('')
const categories = ref([])
const activeCategory = ref(0)
const articles = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const loading = ref(false)

async function fetchCategories() {
  try {
    const res = await knowledgeApi.categories()
    categories.value = res || []
  } catch {
    categories.value = []
  }
}

async function fetchArticles() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (activeCategory.value && activeCategory.value !== 0) {
      params.categoryId = activeCategory.value
    }
    const res = await knowledgeApi.articles(params)
    articles.value = res.list || []
    total.value = res.total || 0
  } catch {
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onCategorySelect(index) {
  activeCategory.value = Number(index)
  page.value = 1
  fetchArticles()
}

function onPageChange(p) {
  page.value = p
  fetchArticles()
}

function goDetail(id) {
  router.push(`/knowledge/${id}`)
}

function parseTags(tags) {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  return String(tags).split(/[,，]/).map(t => t.trim()).filter(Boolean)
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 10)
  return String(time)
}

onMounted(() => {
  fetchCategories()
  fetchArticles()
})
</script>

<style scoped>
.knowledge-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}
.page-header p {
  color: #666;
}
.search-bar {
  margin-bottom: 24px;
}
.content-wrapper {
  display: flex;
  gap: 24px;
}
.category-aside {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.category-aside .el-menu {
  border-right: none;
}
.article-list {
  flex: 1;
  min-width: 0;
}
.article-card {
  cursor: pointer;
  margin-bottom: 16px;
  transition: transform 0.2s;
}
.article-card:hover {
  transform: translateY(-2px);
}
.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}
.article-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.article-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.article-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  .category-aside {
    width: 100%;
  }
}
</style>
