<template>
  <div class="legal-aid-page">
    <div class="page-header">
      <h2>法律援助</h2>
      <p>为经济困难的群众提供免费法律服务，填写以下信息申请援助</p>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent
      >
        <el-form-item label="申请人姓名" prop="applicantName">
          <el-input v-model="form.applicantName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号码" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>

        <el-form-item label="联系地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="案件类型" prop="caseType">
          <el-select v-model="form.caseType" placeholder="请选择案件类型" style="width: 100%">
            <el-option label="民事案件" value="CIVIL" />
            <el-option label="刑事案件" value="CRIMINAL" />
            <el-option label="行政案件" value="ADMINISTRATIVE" />
            <el-option label="劳动争议" value="LABOR" />
            <el-option label="婚姻家庭" value="FAMILY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="案情描述" prop="caseDescription">
          <el-input
            v-model="form.caseDescription"
            type="textarea"
            :rows="6"
            placeholder="请详细描述案件基本情况"
          />
        </el-form-item>

        <el-form-item label="经济状况" prop="economicStatus">
          <el-select v-model="form.economicStatus" placeholder="请选择经济状况" style="width: 100%">
            <el-option label="低保户" value="LOW_INCOME" />
            <el-option label="困难家庭" value="HARDSHIP" />
            <el-option label="残疾" value="DISABILITY" />
            <el-option label="其他困难" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-alert
      v-if="applyResult"
      type="success"
      :closable="false"
      style="margin-top: 20px"
    >
      <div class="result-text">
        申请提交成功！申请编号：{{ applyResult.id }}，当前状态：待审核
      </div>
    </el-alert>

    <div class="my-applications-section">
      <h3 class="section-title">我的申请</h3>
      <div v-loading="loading">
        <el-card
          v-for="item in applications"
          :key="item.id"
          class="application-item"
          shadow="never"
        >
          <div class="application-header">
            <span class="application-no">申请编号：{{ item.id }}</span>
            <el-tag :type="statusColor(item.status)" size="small">
              {{ statusLabel(item.status) }}
            </el-tag>
          </div>
          <div class="application-info">
            <span class="info-item">申请人：{{ item.applicantName }}</span>
            <span class="info-item">案件类型：{{ caseTypeLabel(item.caseType) }}</span>
            <span class="info-item">提交时间：{{ formatTime(item.createTime) }}</span>
          </div>
          <div class="application-desc">{{ item.caseDescription }}</div>
          <div v-if="item.auditRemark" class="audit-remark">
            <span class="remark-label">审核备注：</span>{{ item.auditRemark }}
          </div>
        </el-card>

        <el-empty v-if="!loading && applications.length === 0" description="暂无申请记录" />
      </div>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
        style="margin-top: 20px; text-align: center"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { legalAidApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const applyResult = ref(null)
const applications = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const form = reactive({
  applicantName: '',
  idCard: '',
  phone: '',
  address: '',
  caseType: '',
  caseDescription: '',
  economicStatus: '',
})

const rules = {
  applicantName: [{ required: true, message: '请输入申请人姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入联系地址', trigger: 'blur' }],
  caseType: [{ required: true, message: '请选择案件类型', trigger: 'change' }],
  caseDescription: [{ required: true, message: '请输入案情描述', trigger: 'blur' }],
  economicStatus: [{ required: true, message: '请选择经济状况', trigger: 'change' }],
}

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

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const res = await legalAidApi.apply({
      ...form,
      userId: userStore.userInfo.userId,
    })
    applyResult.value = res
    ElMessage.success('申请提交成功！')
    form.applicantName = ''
    form.idCard = ''
    form.phone = ''
    form.address = ''
    form.caseType = ''
    form.caseDescription = ''
    form.economicStatus = ''
    fetchApplications()
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function fetchApplications() {
  loading.value = true
  try {
    const res = await legalAidApi.my({
      userId: userStore.userInfo.userId,
      page: page.value,
      size: size.value,
    })
    applications.value = res.list || []
    total.value = res.total || 0
  } catch {
    applications.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  fetchApplications()
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.legal-aid-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}
.page-header p {
  color: #666;
}
.form-card {
  margin-bottom: 32px;
}
.result-text {
  font-size: 15px;
  font-weight: 600;
}
.my-applications-section {
  margin-top: 32px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1a1a2e;
}
.application-item {
  margin-bottom: 12px;
}
.application-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.application-no {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}
.application-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}
.info-item {
  white-space: nowrap;
}
.application-desc {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
}
.audit-remark {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fdf6ec;
  border-radius: 6px;
  font-size: 13px;
  color: #e6a23c;
}
.remark-label {
  font-weight: 600;
}
</style>
