<template>
  <div class="knowledge-detail" v-loading="loading">
    <template v-if="article">
      <el-card class="article-card">
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-info">
            <span class="info-item" v-if="article.author">
              <el-icon><User /></el-icon>
              {{ article.author }}
            </span>
            <span class="info-item" v-if="article.source">
              <el-icon><Document /></el-icon>
              来源：{{ article.source }}
            </span>
            <span class="info-item">
              <el-icon><View /></el-icon>
              {{ article.viewCount || 0 }} 浏览
            </span>
            <span class="info-item" v-if="article.createTime">
              <el-icon><Clock /></el-icon>
              {{ formatTime(article.createTime) }}
            </span>
          </div>
          <div class="article-tags" v-if="parseTags(article.tags).length">
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
        </div>

        <el-divider />

        <div class="article-content" v-html="article.content"></div>
      </el-card>

      <div class="related-section" v-if="relatedArticles.length">
        <h3 class="section-title">相关推荐</h3>
        <div class="related-list">
          <el-card
            v-for="item in relatedArticles"
            :key="item.id"
            class="related-card"
            shadow="hover"
            @click="goDetail(item.id)"
          >
            <div class="related-title">{{ item.title }}</div>
            <div class="related-time">{{ formatTime(item.createTime) }}</div>
          </el-card>
        </div>
      </div>

      <div class="back-btn">
        <el-button @click="$router.push('/knowledge')">返回知识库</el-button>
      </div>
    </template>

    <el-empty v-if="!loading && !article" description="文章不存在或已下架" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Document, View, Clock } from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api'

const route = useRoute()
const router = useRouter()

const article = ref(null)
const relatedArticles = ref([])
const loading = ref(false)

async function fetchArticle(id) {
  loading.value = true
  try {
    const res = await knowledgeApi.getArticle(id)
    article.value = res
    if (res && res.categoryId) {
      fetchRelated(res.categoryId, id)
    }
  } catch {
    article.value = null
  } finally {
    loading.value = false
  }
}

async function fetchRelated(categoryId, currentId) {
  try {
    const res = await knowledgeApi.articles({
      categoryId,
      page: 1,
      size: 4,
    })
    const list = res.list || []
    relatedArticles.value = list.filter(a => String(a.id) !== String(currentId)).slice(0, 3)
  } catch {
    relatedArticles.value = []
  }
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

watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchArticle(newId)
  }
})

onMounted(() => {
  if (route.params.id) {
    fetchArticle(route.params.id)
  }
})
</script>

<style scoped>
.knowledge-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px;
}
.article-card {
  margin-bottom: 24px;
}
.article-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.4;
  margin-bottom: 16px;
}
.article-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}
.article-content :deep(p) {
  margin-bottom: 16px;
}
.article-content :deep(h2),
.article-content :deep(h3) {
  margin: 24px 0 12px;
  font-weight: 600;
}
.article-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}
.related-section {
  margin-bottom: 24px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1a1a2e;
}
.related-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.related-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.related-card:hover {
  transform: translateY(-2px);
}
.related-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.related-time {
  font-size: 12px;
  color: #999;
}
.back-btn {
  text-align: center;
}
@media (max-width: 768px) {
  .related-list {
    grid-template-columns: 1fr;
  }
}
</style>
