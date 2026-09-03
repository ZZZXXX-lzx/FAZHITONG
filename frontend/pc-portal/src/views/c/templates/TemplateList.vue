<template>
  <div class="template-list" v-loading="loading">
    <div class="page-header">
      <h2>合同模板</h2>
      <p>海量常用法律文书模板，一键套用</p>
    </div>
    <el-row :gutter="20">
      <el-col :xs="12" :sm="6" v-for="(item, idx) in templateCategories" :key="item.code">
        <el-card class="template-cat-card" shadow="hover" @click="goCategory(item)">
          <div class="cat-icon" :style="{ background: palette[idx % palette.length] }">{{ item.name.charAt(0) }}</div>
          <h3>{{ item.name }}<span class="cat-count">{{ item.count }} 个模板</span></h3>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && templateCategories.length === 0" description="暂无模板分类" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { documentApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const templateCategories = ref([])
const palette = ['#1a56db', '#0d9488', '#d97706', '#7c3aed', '#be185d', '#b45309', '#0891b2', '#0e9f6e']

onMounted(async () => {
  loading.value = true
  try {
    const [categories, tplData] = await Promise.all([
      documentApi.categories(),
      documentApi.templates({ page: 1, size: 500 }),
    ])
    const countByType = {}
    for (const t of (tplData.list || [])) {
      countByType[t.type] = (countByType[t.type] || 0) + 1
    }
    const items = (categories || []).map(c => ({
      code: c.code,
      name: c.name,
      count: countByType[c.code] || 0,
    }))
    templateCategories.value = items.filter(i => i.count > 0)
  } catch {
    templateCategories.value = []
  } finally {
    loading.value = false
  }
})

function goCategory(item) {
  router.push({ path: '/documents', query: { category: item.code } })
}
</script>

<style scoped>
.template-list { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.template-cat-card { text-align: center; padding: 24px 16px; cursor: pointer; margin-bottom: 20px; }
.cat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  margin: 0 auto 12px;
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.template-cat-card h3 { font-size: 16px; margin-bottom: 4px; display: flex; align-items: center; justify-content: center; gap: 6px; }
.cat-count { font-size: 12px; color: #999; font-weight: 400; }
</style>
