<template>
  <div class="legal-review-manage">
    <div class="page-header">
      <h2>法律审核</h2>
      <p>合同 · 文件 · 合规事项审核台账</p>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.pending }}</span><span class="lbl">待审核</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.reviewing }}</span><span class="lbl">审核中</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.approved }}</span><span class="lbl">已通过</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.rejected }}</span><span class="lbl">已驳回</span></div></el-card></el-col>
    </el-row>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <strong>审核台账</strong>
            <el-select v-model="filterType" placeholder="类型" clearable style="width:140px;margin-left:12px" @change="load">
              <el-option label="合同审核" value="CONTRACT" />
              <el-option label="文件审核" value="DOCUMENT" />
              <el-option label="合规审核" value="COMPLIANCE" />
              <el-option label="其他" value="OTHER" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width:140px;margin-left:12px" @change="load">
              <el-option label="待审核" value="PENDING" />
              <el-option label="审核中" value="REVIEWING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </div>
          <el-button type="primary" @click="showCreate = true">发起审核</el-button>
        </div>
      </template>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="title" label="审核事项" min-width="180" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ typeText(row.reviewType) }}</template>
        </el-table-column>
        <el-table-column prop="submitter" label="提交人" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewer" label="审核人" width="120" />
        <el-table-column label="审核时间" width="160">
          <template #default="{ row }">{{ row.reviewTime ? row.reviewTime.slice(0, 16).replace('T', ' ') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openReview(row)">审核</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发起/编辑 -->
    <el-dialog v-model="showCreate" :title="editingId ? '编辑审核事项' : '发起审核'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="审核事项"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.reviewType" style="width:100%">
            <el-option label="合同审核" value="CONTRACT" />
            <el-option label="文件审核" value="DOCUMENT" />
            <el-option label="合规审核" value="COMPLIANCE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交人"><el-input v-model="form.submitter" /></el-form-item>
        <el-form-item label="审核内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 审核 -->
    <el-dialog v-model="showReview" title="审核处理" width="520px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="审核结论">
          <el-radio-group v-model="reviewForm.status">
            <el-radio-button label="APPROVED">通过</el-radio-button>
            <el-radio-button label="REJECTED">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核人"><el-input v-model="reviewForm.reviewer" /></el-form-item>
        <el-form-item label="审核意见"><el-input v-model="reviewForm.opinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReview = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReview">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contractApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)
const filterType = ref('')
const filterStatus = ref('')
const showCreate = ref(false)
const showReview = ref(false)
const saving = ref(false)
const editingId = ref(null)
const reviewingId = ref(null)

const form = reactive({ title: '', reviewType: 'CONTRACT', submitter: '', content: '' })
const reviewForm = reactive({ status: 'APPROVED', reviewer: '', opinion: '' })

const summary = computed(() => {
  const s = { pending: 0, reviewing: 0, approved: 0, rejected: 0 }
  for (const r of records.value) {
    if (s[r.status] !== undefined) s[r.status]++
  }
  return s
})

function enterpriseId() {
  return userStore.userInfo?.enterpriseId || 1
}
function typeText(t) {
  return { CONTRACT: '合同审核', DOCUMENT: '文件审核', COMPLIANCE: '合规审核', OTHER: '其他' }[t] || t
}
function statusText(s) {
  return { PENDING: '待审核', REVIEWING: '审核中', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s
}
function statusTag(s) {
  return { PENDING: 'info', REVIEWING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await contractApi.legalReviewList({ enterpriseId: enterpriseId(), reviewType: filterType.value || undefined, status: filterStatus.value || undefined, page: 1, size: 100 })
    records.value = data.list || []
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { title: row.title, reviewType: row.reviewType, submitter: row.submitter, content: row.content })
  showCreate.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { title: '', reviewType: 'CONTRACT', submitter: '', content: '' })
}

async function save() {
  if (!form.title) {
    ElMessage.warning('请填写审核事项')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, enterpriseId: enterpriseId() }
    if (editingId.value) {
      await contractApi.legalReviewUpdate({ ...payload, id: editingId.value })
    } else {
      await contractApi.legalReviewCreate(payload)
    }
    ElMessage.success('保存成功')
    showCreate.value = false
    resetForm()
    load()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function openReview(row) {
  reviewingId.value = row.id
  Object.assign(reviewForm, { status: 'APPROVED', reviewer: row.reviewer || '', opinion: row.opinion || '' })
  showReview.value = true
}

async function submitReview() {
  saving.value = true
  try {
    await contractApi.legalReviewReview(reviewingId.value, { ...reviewForm })
    ElMessage.success('审核完成')
    showReview.value = false
    load()
  } catch {
    ElMessage.error('审核失败')
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该审核记录？', '提示', { type: 'warning' })
  try {
    await contractApi.legalReviewDelete(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.legal-review-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.stat { text-align: center; }
.stat .num { font-size: 22px; font-weight: 700; color: #1a56db; display: block; }
.stat .lbl { font-size: 12px; color: #999; margin-top: 4px; display: block; }
</style>
