<template>
  <div>
    <h2 style="margin-bottom: 20px">知识库管理</h2>

    <el-card>
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center">
        <el-input
          v-model="keyword"
          placeholder="搜索文章标题"
          clearable
          style="width: 240px"
          @input="fetchArticles"
        />
        <el-select v-model="filterCategory" placeholder="筛选分类" clearable style="width: 160px" @change="fetchArticles">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
        <el-button type="primary" @click="openDialog(null)">新增文章</el-button>
      </div>

      <el-table :data="articles" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
        style="margin-top: 16px; text-align: center"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑文章' : '新增文章'" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入文章正文（支持HTML）" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { knowledgeApi } from '@/api'

const keyword = ref('')
const filterCategory = ref('')
const categories = ref([])
const articles = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const form = reactive({
  title: '',
  categoryId: '',
  summary: '',
  content: '',
  tags: '',
  author: '',
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }],
}

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
    const params = { page: page.value, size: size.value }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (filterCategory.value) params.categoryId = filterCategory.value
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

function onPageChange(p) {
  page.value = p
  fetchArticles()
}

function getCategoryName(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

function openDialog(row) {
  editingId.value = row ? row.id : null
  if (row) {
    form.title = row.title || ''
    form.categoryId = row.categoryId || ''
    form.summary = row.summary || ''
    form.content = row.content || ''
    form.tags = row.tags || ''
    form.author = row.author || ''
  } else {
    form.title = ''
    form.categoryId = ''
    form.summary = ''
    form.content = ''
    form.tags = ''
    form.author = ''
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await knowledgeApi.update(editingId.value, { ...form })
      ElMessage.success('编辑成功')
    } else {
      await knowledgeApi.create({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchArticles()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await knowledgeApi.toggleStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  } catch {
    ElMessage.error('操作失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

onMounted(() => {
  fetchCategories()
  fetchArticles()
})
</script>

<style scoped>
</style>
