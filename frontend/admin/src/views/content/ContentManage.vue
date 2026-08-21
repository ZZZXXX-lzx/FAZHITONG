<template>
  <div>
    <h2 style="margin-bottom:20px">内容管理</h2>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="文书模板" name="templates">
        <el-card>
          <div style="margin-bottom:16px;display:flex;gap:12px;align-items:center">
            <el-input v-model="tplKeyword" placeholder="搜索模板" clearable style="width:240px" @input="fetchTemplates" />
            <el-button type="primary" @click="showTemplateDialog">新增模板</el-button>
          </div>
          <el-table :data="templates" stripe v-loading="templateLoading">
            <el-table-column prop="name" label="模板名称" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewTemplate(row)">查看</el-button>
                <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleTemplateStatus(row)">
                  {{ row.status === 1 ? '下架' : '上架' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="案例数据" name="cases">
        <el-card>
          <div style="margin-bottom:16px;display:flex;gap:12px;align-items:center">
            <el-input v-model="caseKeyword" placeholder="搜索案例" clearable style="width:240px" @input="fetchCases" />
          </div>
          <el-table :data="caseList" stripe v-loading="caseLoading">
            <el-table-column prop="causeName" label="案由" />
            <el-table-column prop="courtName" label="法院" />
            <el-table-column prop="caseYear" label="年份" width="80" />
            <el-table-column prop="source" label="来源" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewCase(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="caseTotal > 0" background layout="prev, pager, next" :total="caseTotal" :page-size="20" @current-change="onCasePageChange" style="margin-top:16px;text-align:center" />
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="咨询管理" name="consultations">
        <el-card>
          <div style="margin-bottom:16px;display:flex;gap:12px">
            <el-select v-model="consultFilter" placeholder="筛选状态" clearable style="width:160px" @change="fetchConsultations">
              <el-option label="全部" value="" />
              <el-option label="待回复" :value="0" />
              <el-option label="已回复" :value="1" />
            </el-select>
          </div>
          <el-table :data="consultations" stripe v-loading="consultLoading">
            <el-table-column prop="title" label="咨询标题" />
            <el-table-column prop="consultationType" label="类型" width="80">
              <template #default="{ row }">{{ row.consultationType === 'AI' ? 'AI' : '律师' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已回复' : '待回复' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewConsultation(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="consultTotal > 0" background layout="prev, pager, next" :total="consultTotal" :page-size="20" @current-change="onConsultPageChange" style="margin-top:16px;text-align:center" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="templateDialog" title="模板详情" width="700px">
      <template v-if="currentTemplate">
        <p><strong>名称：</strong>{{ currentTemplate.name }}</p>
        <p><strong>类型：</strong>{{ currentTemplate.type }}</p>
        <p><strong>分类：</strong>{{ currentTemplate.category }}</p>
        <el-divider />
        <h4>模板内容</h4>
        <div style="background:#f5f7fa;padding:16px;border-radius:8px;max-height:400px;overflow-y:auto;white-space:pre-wrap">{{ currentTemplate.content }}</div>
      </template>
    </el-dialog>

    <el-dialog v-model="caseDialog" title="案例详情" width="700px">
      <template v-if="currentCase">
        <p><strong>案由：</strong>{{ currentCase.causeName }}</p>
        <p><strong>法院：</strong>{{ currentCase.courtName }}</p>
        <p><strong>年份：</strong>{{ currentCase.caseYear }}</p>
        <p><strong>来源：</strong>{{ currentCase.source }}</p>
        <el-divider />
        <h4>摘要</h4>
        <p style="background:#f5f7fa;padding:12px;border-radius:6px;white-space:pre-wrap">{{ currentCase.abstractText }}</p>
        <h4>争议焦点</h4>
        <p style="background:#f5f7fa;padding:12px;border-radius:6px;white-space:pre-wrap">{{ currentCase.focusPoints }}</p>
      </template>
    </el-dialog>

    <el-dialog v-model="consultDialog" title="咨询详情" width="700px">
      <template v-if="currentConsult">
        <p><strong>标题：</strong>{{ currentConsult.title }}</p>
        <p><strong>类型：</strong>{{ currentConsult.consultationType === 'AI' ? 'AI咨询' : '律师咨询' }}</p>
        <p><strong>状态：</strong><el-tag :type="currentConsult.status === 1 ? 'success' : 'warning'" size="small">{{ currentConsult.status === 1 ? '已回复' : '待回复' }}</el-tag></p>
        <p><strong>提交时间：</strong>{{ formatTime(currentConsult.createTime) }}</p>
        <el-divider />
        <h4>问题描述</h4>
        <p style="background:#f5f7fa;padding:12px;border-radius:6px;white-space:pre-wrap">{{ currentConsult.question }}</p>
        <template v-if="currentConsult.answer">
          <el-divider />
          <h4>回复内容</h4>
          <div style="background:#f0f9ff;padding:12px;border-radius:6px;white-space:pre-wrap">{{ currentConsult.answer }}</div>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { documentApi, consultationApi, caseApi } from '@/api'

const activeTab = ref('templates')
const templates = ref([])
const consultations = ref([])
const caseList = ref([])
const templateLoading = ref(false)
const consultLoading = ref(false)
const caseLoading = ref(false)
const tplKeyword = ref('')
const caseKeyword = ref('')
const consultFilter = ref('')
const caseTotal = ref(0)
const consultTotal = ref(0)
const casePage = ref(1)
const consultPage = ref(1)

const templateDialog = ref(false)
const caseDialog = ref(false)
const consultDialog = ref(false)
const currentTemplate = ref(null)
const currentCase = ref(null)
const currentConsult = ref(null)

async function fetchTemplates() {
  templateLoading.value = true
  try {
    const res = await documentApi.templates({ keyword: tplKeyword.value || undefined, page: 1, size: 100 })
    templates.value = res.list || []
  } finally {
    templateLoading.value = false
  }
}

async function fetchConsultations() {
  consultLoading.value = true
  try {
    const res = await consultationApi.list({ page: consultPage.value, size: 20 })
    let list = res.list || []
    if (consultFilter.value !== '' && consultFilter.value !== null) {
      list = list.filter(c => c.status === consultFilter.value)
    }
    consultations.value = list
    consultTotal.value = res.total || 0
  } finally {
    consultLoading.value = false
  }
}

async function fetchCases() {
  caseLoading.value = true
  try {
    const res = await caseApi.search({ keyword: caseKeyword.value || undefined, page: casePage.value, size: 20 })
    caseList.value = res.list || []
    caseTotal.value = res.total || 0
  } finally {
    caseLoading.value = false
  }
}

function onTabChange(tab) {
  if (tab === 'templates' && !templates.value.length) fetchTemplates()
  if (tab === 'cases' && !caseList.value.length) fetchCases()
  if (tab === 'consultations' && !consultations.value.length) fetchConsultations()
}

function onCasePageChange(p) { casePage.value = p; fetchCases() }
function onConsultPageChange(p) { consultPage.value = p; fetchConsultations() }

async function viewTemplate(row) {
  try {
    currentTemplate.value = await documentApi.getTemplate(row.id)
    templateDialog.value = true
  } catch { ElMessage.error('加载详情失败') }
}

async function viewCase(row) {
  currentCase.value = row
  caseDialog.value = true
}

async function viewConsultation(row) {
  try {
    currentConsult.value = await consultationApi.getById(row.id)
    consultDialog.value = true
  } catch { ElMessage.error('加载详情失败') }
}

async function toggleTemplateStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await documentApi.toggleStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  } catch {
    ElMessage.error('操作失败')
  }
}

function showTemplateDialog() {
  ElMessage.info('模板编辑功能开发中，请通过API添加模板')
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

onMounted(() => {
  fetchTemplates()
  fetchConsultations()
  fetchCases()
})
</script>
