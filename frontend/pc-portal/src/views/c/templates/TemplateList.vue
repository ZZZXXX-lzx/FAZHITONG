<template>
  <div class="template-list">
    <div class="page-header">
      <h2>合同模板</h2>
      <p>海量常用法律文书模板，搜索定位 · 一键进入填写并生成 Word</p>
    </div>

    <!-- 搜索 + 分类筛选 -->
    <div class="tpl-toolbar">
      <el-input v-model="keyword" placeholder="搜索模板，如：劳动合同、借款协议、房屋租赁..." clearable class="tpl-search" />
      <div class="cat-chips">
        <span
          v-for="c in categoryList"
          :key="c.code"
          class="cat-chip"
          :class="{ active: activeCat === c.code }"
          @click="toggleCat(c.code)"
        >{{ c.name }}{{ activeCat === c.code ? ' ✕' : '' }}</span>
      </div>
    </div>

    <!-- 模板网格 -->
    <div v-loading="loading" class="tpl-grid-wrap">
      <div v-if="!loading && filtered.length === 0" class="empty-wrap">
        <el-empty :description="keyword ? `未找到与「${keyword}」匹配的模板` : '暂无模板'" :image-size="80" />
      </div>
      <div v-else class="tpl-grid">
        <el-card v-for="t in filtered" :key="t.id" class="tpl-card" shadow="hover" @click="goFill(t)">
          <div class="tpl-top">
            <span class="tpl-icon" :style="{ background: palette[(hashIndex(t) ) % palette.length] }">{{ t.name.charAt(0) }}</span>
            <el-tag v-if="t.category" size="small" type="info" effect="plain">{{ t.category }}</el-tag>
          </div>
          <h3 class="tpl-name">{{ t.name }}</h3>
          <p class="tpl-desc">{{ snippet(t) }}</p>
          <div class="tpl-foot">
            <el-button size="small" type="primary" text>立即填写</el-button>
            <span class="arrow">→</span>
          </div>
        </el-card>
      </div>
    </div>

    <p v-if="!loading && filtered.length" class="result-count">共 {{ filtered.length }} 个模板</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { documentApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const activeCat = ref('')
const categories = ref([])
const templates = ref([])

const palette = ['#1a56db', '#0d9488', '#d97706', '#7c3aed', '#be185d', '#b45309', '#0891b2', '#0e9f6e']

const categoryList = computed(() => {
  const items = (categories.value || []).filter(c => (templates.value || []).some(t => t.type === c.code))
  return items.length ? items : (categories.value || [])
})

const filtered = computed(() => {
  let list = templates.value || []
  if (activeCat.value) list = list.filter(t => t.type === activeCat.value)
  const kw = keyword.value.trim()
  if (kw) {
    const lower = kw.toLowerCase()
    list = list.filter(t => (t.name || '').toLowerCase().includes(lower) || (t.category || '').toLowerCase().includes(lower))
  }
  return list
})

onMounted(async () => {
  loading.value = true
  try {
    const [cats, tplData] = await Promise.all([
      documentApi.categories(),
      documentApi.templates({ page: 1, size: 500 }),
    ])
    categories.value = cats || []
    templates.value = (tplData.list || []).map(t => ({ ...t, snippet: t.snippet || t.content || '' }))
  } catch {
    templates.value = []
  } finally {
    loading.value = false
  }
})

function toggleCat(code) {
  activeCat.value = activeCat.value === code ? '' : code
}

function hashIndex(t) {
  return (t.id || 0) + (t.name ? t.name.length : 0)
}

function snippet(t) {
  const s = t.snippet || t.content || ''
  return s.replace(/<[^>]+>/g, '').replace(/\$\{[^}]*\}/g, '').trim().slice(0, 60) || '常用法律文书模板'
}

async function goFill(t) {
  router.push(`/documents/${t.id}`)
}
</script>

<style scoped>
.template-list { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.tpl-toolbar { display: flex; flex-direction: column; gap: 14px; margin-bottom: 20px; }
.tpl-search { width: 420px; }
.cat-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.cat-chip { font-size: 13px; padding: 6px 14px; border-radius: 16px; background: #f2f3f5; color: #555; cursor: pointer; transition: all .2s; }
.cat-chip.active { background: #1a56db; color: #fff; }
.tpl-grid-wrap { min-height: 200px; }
.tpl-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; }
.tpl-card { cursor: pointer; transition: transform .15s; }
.tpl-card:hover { transform: translateY(-2px); }
.tpl-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.tpl-icon { width: 40px; height: 40px; border-radius: 10px; color: #fff; font-size: 18px; font-weight: 600; display: flex; align-items: center; justify-content: center; }
.tpl-name { font-size: 15px; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.tpl-desc { color: #999; font-size: 12px; line-height: 1.5; height: 36px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.tpl-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
.arrow { color: #1a56db; font-size: 16px; }
.empty-wrap { padding: 30px 0; }
.result-count { text-align: center; color: #999; font-size: 13px; margin-top: 18px; }
@media (max-width: 560px) { .template-list { padding: 16px 12px; } .tpl-search { width: 100%; } }
</style>