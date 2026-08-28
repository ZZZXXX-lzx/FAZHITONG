<template>
  <div class="contract-manage">
    <div class="page-header">
      <h2>合同管理</h2>
      <p>合同全生命周期管理 · AI 智能审查</p>
    </div>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <strong>合同列表</strong>
          <div>
            <el-button type="primary" @click="showReview = true">合同智能审查</el-button>
            <el-button @click="showCreate = true">新建合同</el-button>
          </div>
        </div>
      </template>
      <el-table :data="contracts" stripe v-loading="loading">
        <el-table-column prop="title" label="合同名称" />
        <el-table-column prop="contractNo" label="编号" width="160" />
        <el-table-column prop="partyA" label="甲方" width="150" />
        <el-table-column prop="partyB" label="乙方" width="150" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SIGNED' ? 'success' : row.status === 'EXPIRED' ? 'danger' : 'info'">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 合同智能审查 -->
    <el-dialog v-model="showReview" title="合同智能审查" width="720px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"
        title="请粘贴待审查的合同文本，AI 将识别风险点、给出修改建议与法律依据。" />
      <el-input type="textarea" v-model="reviewText" :rows="12" placeholder="在此粘贴合同全文..." />
      <template #footer>
        <el-button @click="showReview = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="submitReview">开始审查</el-button>
      </template>
    </el-dialog>

    <!-- 审查结果 -->
    <el-dialog v-model="showResult" title="审查结果" width="720px">
      <div v-if="reviewResult">
        <el-tag :type="riskTagType" size="large" style="margin-bottom:16px">
          风险等级：{{ reviewResult.riskLevel }}
        </el-tag>
        <div class="report-box">{{ reviewResult.report }}</div>
      </div>
    </el-dialog>

    <!-- 新建合同 -->
    <el-dialog v-model="showCreate" title="新建合同" width="600px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="合同名称"><el-input v-model="createForm.title" /></el-form-item>
        <el-form-item label="合同编号"><el-input v-model="createForm.contractNo" /></el-form-item>
        <el-form-item label="甲方"><el-input v-model="createForm.partyA" /></el-form-item>
        <el-form-item label="乙方"><el-input v-model="createForm.partyB" /></el-form-item>
        <el-form-item label="合同金额"><el-input v-model="createForm.amount" /></el-form-item>
        <el-form-item label="签署日期"><el-date-picker v-model="createForm.signDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="到期日期"><el-date-picker v-model="createForm.expireDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { contractApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const contracts = ref([])
const loading = ref(false)

// 智能审查
const showReview = ref(false)
const showResult = ref(false)
const reviewText = ref('')
const reviewLoading = ref(false)
const reviewResult = ref(null)

// 新建
const showCreate = ref(false)
const createLoading = ref(false)
const createForm = reactive({ title: '', contractNo: '', partyA: '', partyB: '', amount: '', signDate: '', expireDate: '' })

const riskTagType = computed(() => {
  const lv = reviewResult.value?.riskLevel
  if (lv === '高') return 'danger'
  if (lv === '中') return 'warning'
  return 'success'
})

function statusText(status) {
  if (status === 'DRAFT') return '草稿'
  if (status === 'SIGNED') return '已签署'
  if (status === 'EXPIRED') return '已过期'
  return status || '草稿'
}

function enterpriseId() {
  // 演示环境默认企业 id=1；实际应由企业信息接口提供
  return userStore.userInfo?.enterpriseId || 1
}

onMounted(loadContracts)

async function loadContracts() {
  loading.value = true
  try {
    const data = await contractApi.enterpriseList({ enterpriseId: enterpriseId(), page: 1, size: 50 })
    contracts.value = data.list || []
  } catch {
    ElMessage.error('加载合同列表失败')
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (!reviewText.value.trim()) {
    ElMessage.warning('请先粘贴合同文本')
    return
  }
  reviewLoading.value = true
  try {
    reviewResult.value = await contractApi.aiReview(reviewText.value.trim())
    showReview.value = false
    showResult.value = true
  } catch {
    ElMessage.error('审查失败，请稍后重试')
  } finally {
    reviewLoading.value = false
  }
}

async function submitCreate() {
  if (!createForm.title) {
    ElMessage.warning('请填写合同名称')
    return
  }
  createLoading.value = true
  try {
    await contractApi.createEnterprise({
      enterpriseId: enterpriseId(),
      title: createForm.title,
      contractNo: createForm.contractNo,
      partyA: createForm.partyA,
      partyB: createForm.partyB,
      amount: createForm.amount,
      status: 'DRAFT',
      signDate: createForm.signDate || null,
      expireDate: createForm.expireDate || null,
    })
    ElMessage.success('合同创建成功')
    showCreate.value = false
    Object.assign(createForm, { title: '', contractNo: '', partyA: '', partyB: '', amount: '', signDate: '', expireDate: '' })
    loadContracts()
  } catch {
    ElMessage.error('创建失败，请稍后重试')
  } finally {
    createLoading.value = false
  }
}
</script>

<style scoped>
.contract-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.report-box {
  white-space: pre-wrap;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  line-height: 1.7;
  max-height: 420px;
  overflow-y: auto;
}
</style>
