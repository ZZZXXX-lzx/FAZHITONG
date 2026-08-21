<template>
  <div>
    <h2 style="margin-bottom: 20px">法律援助审核</h2>

    <el-card>
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center">
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 160px" @change="fetchList">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column label="案件类型" width="120">
          <template #default="{ row }">{{ caseTypeLabel(row.caseType) }}</template>
        </el-table-column>
        <el-table-column prop="caseDescription" label="案情描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusColor(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAuditDialog(row)">审核</el-button>
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

    <el-dialog v-model="auditDialogVisible" title="法律援助审核" width="600px">
      <template v-if="currentItem">
        <div style="margin-bottom: 16px">
          <p><strong>申请人：</strong>{{ currentItem.applicantName }}</p>
          <p><strong>身份证号：</strong>{{ currentItem.idCard }}</p>
          <p><strong>联系电话：</strong>{{ currentItem.phone }}</p>
          <p><strong>联系地址：</strong>{{ currentItem.address }}</p>
          <p><strong>案件类型：</strong>{{ caseTypeLabel(currentItem.caseType) }}</p>
          <p><strong>经济状况：</strong>{{ economicLabel(currentItem.economicStatus) }}</p>
          <p><strong>案情描述：</strong></p>
          <div style="background: #f5f7fa; padding: 12px; border-radius: 6px; white-space: pre-wrap">{{ currentItem.caseDescription }}</div>
        </div>
        <el-divider />
        <el-form label-width="100px">
          <el-form-item label="审核结果">
            <el-radio-group v-model="auditForm.status">
              <el-radio value="APPROVED">通过</el-radio>
              <el-radio value="REJECTED">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="auditForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入审核备注"
            />
          </el-form-item>
          <el-form-item label="指派律师ID">
            <el-input
              v-model="auditForm.assignedLawyerId"
              placeholder="选填，输入律师用户ID"
            />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAudit">确认审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { legalAidApi } from '@/api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const filterStatus = ref('')

const auditDialogVisible = ref(false)
const submitting = ref(false)
const currentItem = ref(null)

const auditForm = reactive({
  status: 'APPROVED',
  remark: '',
  assignedLawyerId: '',
})

const statusMap = {
  PENDING: { label: '待审核', color: 'warning' },
  APPROVED: { label: '已通过', color: 'success' },
  REJECTED: { label: '已驳回', color: 'danger' },
}

function statusLabel(status) {
  return statusMap[status]?.label || status || '待审核'
}

function statusColor(status) {
  return statusMap[status]?.color || 'info'
}

const caseTypeMap = {
  CIVIL: '民事案件',
  CRIMINAL: '刑事案件',
  ADMINISTRATIVE: '行政案件',
  LABOR: '劳动争议',
  FAMILY: '婚姻家庭',
  OTHER: '其他',
}

function caseTypeLabel(type) {
  return caseTypeMap[type] || type || ''
}

const economicMap = {
  LOW_INCOME: '低保户',
  HARDSHIP: '困难家庭',
  DISABILITY: '残疾',
  OTHER: '其他困难',
}

function economicLabel(status) {
  return economicMap[status] || status || ''
}

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await legalAidApi.list(params)
    let data = res.list || []
    if (filterStatus.value) data = data.filter(item => item.status === filterStatus.value)
    list.value = data
    total.value = res.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  fetchList()
}

function openAuditDialog(row) {
  currentItem.value = row
  auditForm.status = 'APPROVED'
  auditForm.remark = ''
  auditForm.assignedLawyerId = ''
  auditDialogVisible.value = true
}

async function handleAudit() {
  if (!currentItem.value) return
  submitting.value = true
  try {
    const params = {
      status: auditForm.status,
      remark: auditForm.remark || undefined,
      assignedLawyerId: auditForm.assignedLawyerId || undefined,
    }
    await legalAidApi.audit(currentItem.value.id, params.status, params.remark, params.assignedLawyerId)
    ElMessage.success('审核成功')
    currentItem.value.status = auditForm.status
    if (auditForm.remark) currentItem.value.auditRemark = auditForm.remark
    auditDialogVisible.value = false
  } catch {
    ElMessage.error('审核失败')
  } finally {
    submitting.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
</style>
