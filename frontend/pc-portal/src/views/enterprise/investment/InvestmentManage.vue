<template>
  <div class="investment-manage">
    <div class="page-header">
      <h2>投融资管理</h2>
      <p>企业融资轮次 · 对外投资台账</p>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.financingAmount }}</span><span class="lbl">累计融资（万元）</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.financingCount }}</span><span class="lbl">融资轮次</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.investmentAmount }}</span><span class="lbl">对外投资（万元）</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.investmentCount }}</span><span class="lbl">投资笔数</span></div></el-card></el-col>
    </el-row>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <strong>投融资台账</strong>
            <el-select v-model="filterType" placeholder="类型" clearable style="width:140px;margin-left:12px" @change="load">
              <el-option label="融资" value="FINANCING" />
              <el-option label="对外投资" value="INVESTMENT" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width:140px;margin-left:12px" @change="load">
              <el-option label="计划中" value="PLANNED" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已终止" value="TERMINATED" />
            </el-select>
          </div>
          <el-button type="primary" @click="showCreate = true">新增记录</el-button>
        </div>
      </template>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="round" label="轮次" width="120" />
        <el-table-column prop="investor" label="投资方 / 被投企业" />
        <el-table-column label="金额（万元）" width="130">
          <template #default="{ row }">{{ fmt(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="估值（万元）" width="130">
          <template #default="{ row }">{{ fmt(row.valuation) }}</template>
        </el-table-column>
        <el-table-column label="股权比例" width="100">
          <template #default="{ row }">{{ row.equityRatio != null ? row.equityRatio + '%' : '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="120">
          <template #default="{ row }">{{ row.investDate ? row.investDate.slice(0, 10) : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog v-model="showCreate" :title="editingId ? '编辑投融资记录' : '新增投融资记录'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button label="FINANCING">融资</el-radio-button>
            <el-radio-button label="INVESTMENT">对外投资</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="轮次">
          <el-select v-model="form.round" style="width:100%" allow-create filterable clearable>
            <el-option v-for="r in rounds" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="投资方"><el-input v-model="form.investor" /></el-form-item>
        <el-form-item label="金额（万元）"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="估值（万元）"><el-input-number v-model="form.valuation" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="股权比例（%）"><el-input-number v-model="form.equityRatio" :min="0" :max="100" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="时间"><el-date-picker v-model="form.investDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="计划中" value="PLANNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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
const saving = ref(false)
const editingId = ref(null)

const rounds = ['天使轮', 'Pre-A', 'A轮', 'B轮', 'C轮', 'D轮', '战略投资', '其他']
const form = reactive({ type: 'FINANCING', round: '', investor: '', amount: null, valuation: null, equityRatio: null, investDate: '', status: 'PLANNED', remark: '' })

const summary = computed(() => {
  let financingAmount = 0, financingCount = 0, investmentAmount = 0, investmentCount = 0
  for (const r of records.value) {
    const amt = Number(r.amount) || 0
    if (r.type === 'INVESTMENT') {
      investmentAmount += amt
      investmentCount++
    } else {
      financingAmount += amt
      financingCount++
    }
  }
  return {
    financingAmount: financingAmount.toFixed(2),
    financingCount,
    investmentAmount: investmentAmount.toFixed(2),
    investmentCount,
  }
})

function enterpriseId() {
  return userStore.userInfo?.enterpriseId || 1
}
function typeText(t) {
  return { FINANCING: '融资', INVESTMENT: '对外投资' }[t] || t
}
function statusText(s) {
  return { PLANNED: '计划中', IN_PROGRESS: '进行中', COMPLETED: '已完成', TERMINATED: '已终止' }[s] || s
}
function statusTag(s) {
  return { PLANNED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', TERMINATED: 'danger' }[s] || 'info'
}
function fmt(v) {
  return v != null ? Number(v).toFixed(2) : '-'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await contractApi.investmentList({ enterpriseId: enterpriseId(), type: filterType.value || undefined, status: filterStatus.value || undefined, page: 1, size: 100 })
    records.value = data.list || []
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    type: row.type, round: row.round, investor: row.investor,
    amount: row.amount, valuation: row.valuation, equityRatio: row.equityRatio,
    investDate: row.investDate ? row.investDate.slice(0, 10) : '', status: row.status, remark: row.remark,
  })
  showCreate.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { type: 'FINANCING', round: '', investor: '', amount: null, valuation: null, equityRatio: null, investDate: '', status: 'PLANNED', remark: '' })
}

async function save() {
  if (!form.investor && !form.round) {
    ElMessage.warning('请至少填写投资方或轮次')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, enterpriseId: enterpriseId() }
    if (editingId.value) {
      await contractApi.investmentUpdate({ ...payload, id: editingId.value })
    } else {
      await contractApi.investmentCreate(payload)
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

async function remove(row) {
  await ElMessageBox.confirm('确认删除该投融资记录？', '提示', { type: 'warning' })
  try {
    await contractApi.investmentDelete(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.investment-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.stat { text-align: center; }
.stat .num { font-size: 22px; font-weight: 700; color: #1a56db; display: block; }
.stat .lbl { font-size: 12px; color: #999; margin-top: 4px; display: block; }
</style>
