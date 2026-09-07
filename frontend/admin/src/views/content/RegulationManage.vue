<template>
  <div>
    <h2 style="margin-bottom: 20px">法规管理</h2>

    <el-card>
      <div style="margin-bottom: 16px; display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-input v-model="keyword" placeholder="搜索标题/关键词/条文内容" clearable style="width: 260px" @input="fetchList" />
        <el-select v-model="filterType" placeholder="法规类型" clearable style="width: 160px" @change="fetchList">
          <el-option v-for="t in lawTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button type="primary" @click="openRegDialog(null)">新增法规</el-button>
        <el-button @click="openImportDialog">批量导入</el-button>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="title" label="法规名称" min-width="260" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.lawType)">{{ row.lawType || '法律' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issuingAuthority" label="制定机关" min-width="150" show-overflow-tooltip />
        <el-table-column prop="publishDate" label="发布日期" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '失效' ? 'info' : 'success'">{{ row.status || '现行有效' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="openArticles(row)">条文({{ row.articleCount || 0 }})</el-button>
            <el-button link type="primary" @click="openRegDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteReg(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
        style="margin-top: 16px; text-align: center"
      />
    </el-card>

    <!-- 法规编辑 -->
    <el-dialog v-model="regDialogVisible" :title="editingRegId ? '编辑法规' : '新增法规'" width="640px">
      <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-width="90px">
        <el-form-item label="法规名称" prop="title">
          <el-input v-model="regForm.title" placeholder="请输入完整法规名称" />
        </el-form-item>
        <el-form-item label="法规类型" prop="lawType">
          <el-select v-model="regForm.lawType" placeholder="请选择类型" style="width: 100%">
            <el-option v-for="t in lawTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="制定机关" prop="issuingAuthority">
          <el-input v-model="regForm.issuingAuthority" placeholder="如：全国人民代表大会" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="发布日期" prop="publishDate">
              <el-input v-model="regForm.publishDate" placeholder="2020-05-28" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="施行日期" prop="effectiveDate">
              <el-input v-model="regForm.effectiveDate" placeholder="2021-01-01" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="效力状态" prop="status">
          <el-select v-model="regForm.status" style="width: 100%">
            <el-option label="现行有效" value="现行有效" />
            <el-option label="已修订" value="已修订" />
            <el-option label="失效" value="失效" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <el-input v-model="regForm.keywords" placeholder="多个关键词用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="内容摘要" prop="content">
          <el-input v-model="regForm.content" type="textarea" :rows="4" placeholder="法规简介/摘要" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="regDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReg">确定</el-button>
      </template>
    </el-dialog>

    <!-- 条文管理 -->
    <el-drawer v-model="articlesDrawer" :title="currentReg?.title || '条文管理'" size="640px">
      <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center">
        <el-button type="primary" size="small" @click="openArticleDialog(null)">新增条文</el-button>
        <el-input v-model="articleKeyword" placeholder="在本法规内搜条文" clearable size="small" style="width: 180px" />
      </div>
      <el-empty v-if="filteredArticles.length === 0" description="暂无匹配条文" />
      <el-card v-for="a in filteredArticles" :key="a.id" shadow="never" style="margin-bottom: 10px">
        <div style="display: flex; justify-content: space-between; align-items: center">
          <strong>{{ a.articleNo || '条文' }}</strong>
          <div>
            <el-button link type="primary" size="small" @click="openArticleDialog(a)">编辑</el-button>
            <el-button link type="danger" size="small" @click="deleteArticle(a)">删除</el-button>
          </div>
        </div>
        <div style="margin-top: 6px; color: #555; font-size: 13px; white-space: pre-wrap">{{ a.content }}</div>
      </el-card>
    </el-drawer>

    <!-- 条文编辑 -->
    <el-dialog v-model="articleDialogVisible" :title="editingArticleId ? '编辑条文' : '新增条文'" width="640px">
      <el-form :model="articleForm" label-width="90px">
        <el-form-item label="条文序号">
          <el-input v-model="articleForm.articleNo" placeholder="如：第一条 / 第二章" />
        </el-form-item>
        <el-form-item label="条文内容">
          <el-input v-model="articleForm.content" type="textarea" :rows="8" placeholder="请输入条文全文" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="articleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingArticle" @click="submitArticle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入 -->
    <el-dialog v-model="importDialogVisible" title="批量导入法规" width="720px">
      <p style="color:#666;margin-bottom:8px;font-size:13px">
        粘贴 JSON 数组，每条含 title/lawType/issuingAuthority/publishDate/effectiveDate/status/keywords/content，可选 articles（条文数组：articleNo/content）。更多数据可从国家法律法规数据库等权威源整理后导入。
      </p>
      <el-input v-model="importText" type="textarea" :rows="12" placeholder='[{"title":"中华人民共和国××法","lawType":"法律","issuingAuthority":"全国人民代表大会","publishDate":"2020-01-01","effectiveDate":"2020-07-01","status":"现行有效","keywords":"","content":"摘要","articles":[{"articleNo":"第一条","content":"条文全文"}]}]' />
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { regulationApi } from '@/api'

const lawTypes = ['法律', '行政法规', '司法解释', '部门规章', '地方性法规', '国际条约']

const keyword = ref('')
const filterType = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

function typeTag(t) {
  if (t === '司法解释') return 'warning'
  if (t === '行政法规') return 'primary'
  if (t === '部门规章' || t === '地方性法规') return 'info'
  return 'success'
}

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (filterType.value) params.lawType = filterType.value
    const res = await regulationApi.search(params)
    list.value = res.list.map((r) => ({ ...r, articleCount: r.articleCount || 0 }))
    total.value = res.total || 0
    // 补充条文数量
    list.value.forEach((r) => countArticles(r))
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function countArticles(r) {
  try {
    const arr = await regulationApi.articles(r.id)
    r.articleCount = (arr || []).length
  } catch {
    r.articleCount = 0
  }
}

function onPageChange(p) {
  page.value = p
  fetchList()
}

// ---------- 法规增删改 ----------
const regDialogVisible = ref(false)
const submitting = ref(false)
const regFormRef = ref(null)
const editingRegId = ref(null)
const regForm = reactive({
  title: '', lawType: '法律', issuingAuthority: '', publishDate: '',
  effectiveDate: '', status: '现行有效', keywords: '', content: '',
})
const regRules = {
  title: [{ required: true, message: '请输入法规名称', trigger: 'blur' }],
  lawType: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

function openRegDialog(row) {
  editingRegId.value = row ? row.id : null
  Object.assign(regForm, {
    title: row?.title || '', lawType: row?.lawType || '法律', issuingAuthority: row?.issuingAuthority || '',
    publishDate: row?.publishDate || '', effectiveDate: row?.effectiveDate || '', status: row?.status || '现行有效',
    keywords: row?.keywords || '', content: row?.content || '',
  })
  regDialogVisible.value = true
}

async function submitReg() {
  if (!regFormRef.value) return
  try {
    await regFormRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (editingRegId.value) {
      await regulationApi.update({ id: editingRegId.value, ...regForm })
      ElMessage.success('编辑成功')
    } else {
      await regulationApi.create({ ...regForm })
      ElMessage.success('新增成功')
    }
    regDialogVisible.value = false
    fetchList()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function deleteReg(row) {
  await ElMessageBox.confirm(`确定删除法规「${row.title}」吗？其下全部条文将一并删除。`, '提示', { type: 'warning' })
  try {
    await regulationApi.remove(row.id)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    ElMessage.error('删除失败')
  }
}

// ---------- 条文管理 ----------
const articlesDrawer = ref(false)
const currentReg = ref(null)
const allArticles = ref([])
const articleKeyword = ref('')
const filteredArticles = computed(() => {
  const k = articleKeyword.value.trim()
  if (!k) return allArticles.value
  return allArticles.value.filter((a) => (a.articleNo || '').includes(k) || (a.content || '').includes(k))
})

async function openArticles(row) {
  currentReg.value = row
  articlesDrawer.value = true
  articleKeyword.value = ''
  try {
    allArticles.value = (await regulationApi.articles(row.id)) || []
  } catch {
    allArticles.value = []
  }
}

const articleDialogVisible = ref(false)
const submittingArticle = ref(false)
const editingArticleId = ref(null)
const articleForm = reactive({ articleNo: '', content: '' })

function openArticleDialog(article) {
  editingArticleId.value = article ? article.id : null
  articleForm.articleNo = article?.articleNo || ''
  articleForm.content = article?.content || ''
  articleDialogVisible.value = true
}

async function submitArticle() {
  if (!articleForm.content.trim()) {
    ElMessage.warning('请输入条文内容')
    return
  }
  submittingArticle.value = true
  try {
    if (editingArticleId.value) {
      await regulationApi.updateArticle(editingArticleId.value, { ...articleForm })
      ElMessage.success('条文已更新')
    } else {
      await regulationApi.addArticle(currentReg.value.id, { ...articleForm })
      ElMessage.success('条文已添加')
    }
    articleDialogVisible.value = false
    await openArticles(currentReg.value)
    countArticles(currentReg.value)
    fetchList()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submittingArticle.value = false
  }
}

async function deleteArticle(article) {
  await ElMessageBox.confirm(`确定删除条文「${article.articleNo || '未命名'}」吗？`, '提示', { type: 'warning' })
  try {
    await regulationApi.deleteArticle(article.id)
    ElMessage.success('已删除')
    await openArticles(currentReg.value)
    countArticles(currentReg.value)
    fetchList()
  } catch {
    ElMessage.error('删除失败')
  }
}

// ---------- 批量导入 ----------
const importDialogVisible = ref(false)
const importing = ref(false)
const importText = ref('')

function openImportDialog() {
  importText.value = ''
  importDialogVisible.value = true
}

async function doImport() {
  let parsed
  try {
    parsed = JSON.parse(importText.value)
  } catch {
    ElMessage.error('JSON 格式错误，请检查')
    return
  }
  if (!Array.isArray(parsed) || parsed.length === 0) {
    ElMessage.error('请粘贴一个非空 JSON 数组')
    return
  }
  importing.value = true
  try {
    const count = await regulationApi.importRegulations(parsed)
    ElMessage.success(`成功导入 ${count} 条法规`)
    importDialogVisible.value = false
    page.value = 1
    fetchList()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped></style>