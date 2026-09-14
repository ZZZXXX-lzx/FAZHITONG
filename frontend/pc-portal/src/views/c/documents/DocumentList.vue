<template>
  <div class="document-list">
    <div class="page-header">
      <h2>法律文书生成</h2>
      <p>选择文书类型，AI 智能生成专业法律文书 · 生成记录可随时查看、下载</p>
      <div class="header-actions">
        <div class="mode-switch">
          <button :class="{ active: viewMode === 'templates' }" @click="switchMode('templates')">模板库</button>
          <button :class="{ active: viewMode === 'records' }" @click="switchMode('records')">我的文书</button>
        </div>
        <el-button type="primary" @click="openDraft">AI 智能起草</el-button>
      </div>
    </div>

    <!-- 模板库 -->
    <template v-if="viewMode === 'templates'">
      <el-tabs v-model="activeCategory" @tab-change="loadTemplates">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane v-for="cat in categories" :key="cat.code" :label="cat.name" :name="cat.code" />
      </el-tabs>
      <el-input v-model="keyword" placeholder="搜索文书模板" clearable class="tpl-search" @input="loadTemplates" />
      <el-row :gutter="12">
        <el-col :xs="12" :sm="6" v-for="tpl in templates" :key="tpl.id">
          <el-card class="template-card" shadow="hover" @click="$router.push(`/documents/${tpl.id}`)">
            <h3>{{ tpl.name }}</h3>
            <p class="template-type">{{ tpl.category }}</p>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && templates.length === 0" description="暂无匹配模板" />
      <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="onPageChange" style="margin-top:24px;text-align:center" />
    </template>

    <!-- 我的文书 -->
    <template v-else>
      <div class="records-toolbar">
        <span class="records-count" v-if="records.length">共 {{ records.length }} 条生成/起草记录</span>
        <el-button text type="primary" size="small" @click="loadRecords">刷新</el-button>
      </div>
      <div v-loading="recordsLoading" class="record-list">
        <el-empty v-if="!recordsLoading && records.length === 0" description="暂无文书记录，去模板库生成或 AI 起草一份吧" />
        <div v-for="r in records" :key="r.id" class="record-card">
          <div class="record-main">
            <div class="record-title" :title="r.docName">{{ r.docName || (r.templateId ? '历史文书' : 'AI 起草文书') }}</div>
            <div class="record-meta">
              <span>{{ formatTime(r.createTime) }}</span>
              <el-tag v-if="!r.templateId" size="small" type="warning" effect="plain">AI 起草</el-tag>
              <el-tag v-else size="small" type="success" effect="plain">模板生成</el-tag>
            </div>
          </div>
          <div class="record-actions">
            <el-button size="small" @click="previewRecord(r)">预览</el-button>
            <el-button size="small" type="primary" plain @click="downloadRecord(r)">下载 Word</el-button>
            <el-button size="small" type="danger" plain @click="removeRecord(r)">删除</el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- AI 智能起草 -->
    <el-dialog v-model="showDraft" title="AI 智能起草" width="720px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="文书类型">
          <el-input v-model="draftType" placeholder="如：劳动合同、借款协议、民事起诉状..." />
        </el-form-item>
        <el-form-item label="需求描述">
          <el-input type="textarea" v-model="draftDesc" :rows="8" placeholder="请描述您的需求，例如：甲方委托乙方开发小程序，约定开发周期、费用与验收标准..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDraft = false">取消</el-button>
        <el-button type="primary" :loading="draftLoading" @click="submitDraft">生成文书</el-button>
      </template>
    </el-dialog>

    <!-- 起草结果 -->
    <el-dialog v-model="showDraftResult" title="生成结果" width="720px">
      <div class="draft-box">{{ draftResult }}</div>
      <template #footer>
        <el-button @click="copyDraft">复制文本</el-button>
        <el-button @click="downloadDraft">下载 Word</el-button>
        <el-button type="primary" :loading="saving" @click="saveDraft">保存到我的文书</el-button>
        <el-button @click="showDraftResult = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 记录预览 -->
    <el-dialog v-model="showPreview" :title="previewItem?.docName || '文书预览'" width="720px">
      <div class="draft-box">{{ previewItem?.data }}</div>
      <template #footer>
        <el-button @click="showPreview = false">关闭</el-button>
        <el-button type="primary" @click="downloadRecord(previewItem)">下载 Word</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { documentApi } from '@/api'
import { buildDocx, downloadBlob } from '@/utils/docx'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const viewMode = ref('templates')
const activeCategory = ref('')
const keyword = ref('')
const categories = ref([])
const templates = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const showDraft = ref(false)
const showDraftResult = ref(false)
const draftType = ref('')
const draftDesc = ref('')
const draftLoading = ref(false)
const draftResult = ref('')
const saving = ref(false)

const records = ref([])
const recordsLoading = ref(false)
const showPreview = ref(false)
const previewItem = ref(null)

onMounted(async () => {
  const cat = route.query.category
  if (cat) activeCategory.value = String(cat)
  try {
    categories.value = await documentApi.categories()
  } catch { /* 分类加载失败不阻塞 */ }
  loadTemplates()
})

function switchMode(mode) {
  viewMode.value = mode
  if (mode === 'records' && userStore.isLoggedIn) loadRecords()
}

async function loadTemplates() {
  loading.value = true
  try {
    const data = await documentApi.templates({ category: activeCategory.value || undefined, keyword: keyword.value || undefined, page: page.value, size: size.value })
    templates.value = data.list
    total.value = data.total
  } catch {
    ElMessage.error('加载模板失败')
  } finally {
    loading.value = false
  }
}

function onPageChange(p) { page.value = p; loadTemplates() }

async function loadRecords() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录后查看我的文书'); return }
  recordsLoading.value = true
  try {
    records.value = await documentApi.records(userStore.userInfo.userId)
  } catch {
    ElMessage.error('加载记录失败')
  } finally {
    recordsLoading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

function openDraft() {
  draftResult.value = ''
  showDraft.value = true
}

async function submitDraft() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录后再使用 AI 智能起草'); return }
  if (!draftDesc.value.trim()) { ElMessage.warning('请填写需求描述'); return }
  draftLoading.value = true
  try {
    draftResult.value = await documentApi.aiDraft(draftType.value.trim() || '法律文书', draftDesc.value.trim())
    showDraft.value = false
    showDraftResult.value = true
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    draftLoading.value = false
  }
}

async function copyDraft() {
  try {
    await navigator.clipboard.writeText(draftResult.value)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本复制')
  }
}

async function downloadDraft() {
  const name = draftType.value.trim() || 'AI起草文书'
  const blob = await buildDocx(name, draftResult.value)
  downloadBlob(blob, `${name}.docx`)
}

async function downloadRecord(r) {
  try {
    const name = r.docName || (r.templateId ? '文书记录' : 'AI起草文书')
    const blob = await buildDocx(name, r.data || '')
    downloadBlob(blob, `${name}.docx`)
    ElMessage.success('已开始下载')
  } catch {
    ElMessage.error('下载失败，请稍后重试')
  }
}

async function saveDraft() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  if (!draftResult.value.trim()) { ElMessage.warning('内容为空，无法保存'); return }
  saving.value = true
  try {
    await documentApi.saveDraft({
      userId: userStore.userInfo.userId,
      docName: draftType.value.trim() || 'AI 起草文书',
      content: draftResult.value,
    })
    ElMessage.success('已保存到我的文书')
    showDraftResult.value = false
    if (viewMode.value === 'records') loadRecords()
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

function previewRecord(r) {
  previewItem.value = r
  showPreview.value = true
}

async function removeRecord(r) {
  try {
    await ElMessageBox.confirm(`确定删除文书「${r.docName || '该记录'}」吗？删除后不可恢复。`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await documentApi.deleteRecord(r.id, userStore.userInfo.userId)
    ElMessage.success('已删除')
    loadRecords()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.document-list { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; margin-bottom: 16px; }
.header-actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.mode-switch { display: inline-flex; border: 1px solid #c6d4f7; border-radius: 8px; overflow: hidden; }
.mode-switch button { padding: 8px 20px; font-size: 14px; border: none; background: #fff; color: #555; cursor: pointer; transition: all .2s; }
.mode-switch button.active { background: #1a56db; color: #fff; font-weight: 600; }
.tpl-search { width: 300px; margin-bottom: 20px; }
.template-card { cursor: pointer; margin-bottom: 12px; }
.template-card h3 { font-size: 16px; margin-bottom: 8px; }
.template-type { color: #999; font-size: 13px; }
.records-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.records-count { color: #666; font-size: 13px; }
.record-list { display: flex; flex-direction: column; gap: 12px; min-height: 200px; }
.record-card { display: flex; align-items: center; justify-content: space-between; gap: 16px; background: #fff; border: 1px solid #eef0f4; border-radius: 12px; padding: 14px 18px; flex-wrap: wrap; }
.record-main { min-width: 0; }
.record-title { font-size: 15px; font-weight: 600; color: #1a3a8f; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 520px; }
.record-meta { display: flex; align-items: center; gap: 10px; color: #999; font-size: 12px; }
.record-actions { display: flex; gap: 8px; flex-wrap: nowrap; }
.draft-box { white-space: pre-wrap; background: #f5f7fa; border-radius: 8px; padding: 16px; line-height: 1.7; max-height: 440px; overflow-y: auto; }
@media (max-width: 560px) { .document-list { padding: 16px 12px; } .tpl-search { width: 100%; } .record-card { flex-direction: column; align-items: flex-start; } }
</style>