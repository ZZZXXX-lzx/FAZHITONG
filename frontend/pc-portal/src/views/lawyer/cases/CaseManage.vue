<template>
  <div class="case-manage">
    <div class="page-header">
      <h2>案件管理</h2>
      <p>承办案件台账 · 状态流转 · 进度跟踪</p>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6"><el-card shadow="never"><div class="stat"><span class="num">{{ stats.total || 0 }}</span><span class="lbl">全部案件</span></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat"><span class="num">{{ stats.inProgress || 0 }}</span><span class="lbl">承办中</span></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat"><span class="num">{{ stats.closed || 0 }}</span><span class="lbl">已结案</span></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat"><span class="num">{{ clients.length }}</span><span class="lbl">客户数</span></div></el-card></el-col>
    </el-row>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <strong>案件列表</strong>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width:130px;margin-left:12px" @change="load">
              <el-option label="承办中" value="IN_PROGRESS" />
              <el-option label="已结案" value="CLOSED" />
              <el-option label="已归档" value="ARCHIVED" />
            </el-select>
          </div>
          <el-button type="primary" @click="openCreate">新建案件</el-button>
        </div>
      </template>
      <el-table :data="cases" stripe v-loading="loading">
        <el-table-column prop="caseName" label="案件名称" />
        <el-table-column prop="clientName" label="当事人" width="150" />
        <el-table-column prop="caseType" label="案由" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS'" size="small" type="success" @click="transition(row, 'CLOSE')">结案</el-button>
            <el-button v-if="row.status === 'CLOSED'" size="small" @click="transition(row, 'ARCHIVE')">归档</el-button>
            <el-button v-if="row.status === 'ARCHIVED'" size="small" type="primary" @click="transition(row, 'REOPEN')">重新承办</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑案件' : '新建案件'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="案件名称"><el-input v-model="form.caseName" /></el-form-item>
        <el-form-item label="当事人"><el-input v-model="form.clientName" /></el-form-item>
        <el-form-item label="案由">
          <el-input v-model="form.caseType" placeholder="如：合同纠纷、劳动争议..." />
        </el-form-item>
        <el-form-item label="案情描述"><el-input type="textarea" v-model="form.description" :rows="4" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { consultationApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const cases = ref([])
const clients = ref([])
const stats = ref({})
const loading = ref(false)
const filterStatus = ref('')
const showDialog = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ caseName: '', clientName: '', caseType: '', description: '', remark: '' })

function lawyerId() {
  return userStore.userInfo?.userId || 1
}
function statusText(s) {
  return { IN_PROGRESS: '承办中', CLOSED: '已结案', ARCHIVED: '已归档' }[s] || s
}
function statusTag(s) {
  return { IN_PROGRESS: 'success', CLOSED: 'info', ARCHIVED: 'warning' }[s] || 'info'
}

onMounted(() => { load(); loadStats(); loadClients() })

async function load() {
  loading.value = true
  try {
    const data = await consultationApi.caseList({ lawyerId: lawyerId(), status: filterStatus.value || undefined, page: 1, size: 100 })
    cases.value = data.list || []
  } catch {
    ElMessage.error('加载案件失败')
  } finally {
    loading.value = false
  }
}
async function loadStats() {
  try {
    stats.value = await consultationApi.caseStats(lawyerId())
  } catch { stats.value = {} }
}
async function loadClients() {
  try {
    const data = await consultationApi.clientList({ lawyerId: lawyerId(), page: 1, size: 100 })
    clients.value = data.list || []
  } catch { clients.value = [] }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { caseName: '', clientName: '', caseType: '', description: '', remark: '' })
  showDialog.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { caseName: row.caseName, clientName: row.clientName, caseType: row.caseType, description: row.description, remark: row.remark })
  showDialog.value = true
}
async function save() {
  if (!form.caseName) { ElMessage.warning('请填写案件名称'); return }
  saving.value = true
  try {
    const payload = { ...form, lawyerId: lawyerId() }
    if (editingId.value) {
      await consultationApi.caseUpdate({ ...payload, id: editingId.value })
    } else {
      await consultationApi.caseCreate(payload)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    load(); loadStats()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
async function transition(row, action) {
  try {
    await consultationApi.caseTransition(row.id, action)
    ElMessage.success('操作成功')
    load(); loadStats()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '操作失败')
  }
}
</script>

<style scoped>
.case-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.stat { display: flex; flex-direction: column; align-items: center; padding: 8px 0; }
.stat .num { font-size: 28px; font-weight: 700; color: #1a56db; }
.stat .lbl { color: #666; font-size: 13px; margin-top: 4px; }
</style>
