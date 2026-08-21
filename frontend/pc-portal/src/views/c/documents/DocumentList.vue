<template>
  <div class="document-list">
    <div class="page-header">
      <h2>法律文书生成</h2>
      <p>选择文书类型，AI智能生成专业法律文书</p>
    </div>
    <el-tabs v-model="activeCategory" @tab-change="loadTemplates">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane v-for="cat in categories" :key="cat.code" :label="cat.name" :name="cat.code" />
    </el-tabs>
    <el-input v-model="keyword" placeholder="搜索文书模板" clearable style="width:300px;margin-bottom:20px" @input="loadTemplates" />
    <el-row :gutter="20">
      <el-col :span="6" v-for="tpl in templates" :key="tpl.id">
        <el-card class="template-card" shadow="hover" @click="$router.push(`/documents/${tpl.id}`)">
          <h3>{{ tpl.name }}</h3>
          <p class="template-type">{{ tpl.type }}</p>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="onPageChange" style="margin-top:24px;text-align:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { documentApi } from '@/api'

const activeCategory = ref('')
const keyword = ref('')
const categories = ref([])
const templates = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

onMounted(async () => {
  categories.value = await documentApi.categories()
  loadTemplates()
})

async function loadTemplates() {
  const data = await documentApi.templates({ category: activeCategory.value || undefined, keyword: keyword.value || undefined, page: page.value, size: size.value })
  templates.value = data.list
  total.value = data.total
}
function onPageChange(p) { page.value = p; loadTemplates() }
</script>

<style scoped>
.document-list { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.template-card { cursor: pointer; margin-bottom: 20px; }
.template-card h3 { font-size: 16px; margin-bottom: 8px; }
.template-type { color: #999; font-size: 13px; }
</style>
