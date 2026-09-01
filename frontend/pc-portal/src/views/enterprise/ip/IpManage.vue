<template>
  <div class="ip-manage">
    <div class="page-header">
      <h2>知识产权管理</h2>
      <p>商标 · 专利 · 著作权台账管理</p>
    </div>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <strong>IP 台账</strong>
            <el-select v-model="filterType" placeholder="类型" clearable style="width:140px;margin-left:12px" @change="load">
              <el-option label="商标" value="TRADEMARK" />
              <el-option label="专利" value="PATENT" />
              <el-option label="著作权" value="COPYRIGHT" />
            </el-select>
          </div>
          <el-button type="primary" @click="showCreate = true">新增知识产权</el-button>
        </div>
      </template>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ typeText(row.ipType) }}</template>
        </el-table-column>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="registerNo" label="注册/申请号" width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="权利人" width="160" />
        <el-table-column label="到期日" width="130">
          <template #default="{ row }">{{ row.expireDate ? row.expireDate.slice(0, 10) : '-' }}</template>
        </el-table-column>
        <el-table-column label="到期预警" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.daysToExpire !== null && row.daysToExpire !== undefined && row.daysToExpire <= 90" type="danger" size="small">
              {{ row.daysToExpire }} 天
            </el-tag>
            <span v-else>-</span>
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
    <el-dialog v-model="showCreate" :title="editingId ? '编辑知识产权' : '新增知识产权'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="类型">
          <el-select v-model="form.ipType" style="width:100%">
            <el-option label="商标" value="TRADEMARK" />
            <el-option label="专利" value="PATENT" />
            <el-option label="著作权" value="COPYRIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="注册/申请号"><el-input v-model="form.registerNo" /></el-form-item>
        <el-form-item label="权利人"><el-input v-model="form.owner" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="申请中" value="PENDING" />
            <el-option label="已授权" value="GRANTED" />
            <el-option label="已失效" value="INVALID" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请日期"><el-date-picker v-model="form.applyDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="到期日期"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contractApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)
const filterType = ref('')
const showCreate = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ ipType: 'TRADEMARK', name: '', registerNo: '', owner: '', status: 'PENDING', applyDate: '', expireDate: '' })

function enterpriseId() {
  return userStore.userInfo?.enterpriseId || 1
}
function typeText(t) {
  return { TRADEMARK: '商标', PATENT: '专利', COPYRIGHT: '著作权' }[t] || t
}
function statusText(s) {
  return { PENDING: '申请中', GRANTED: '已授权', INVALID: '已失效' }[s] || s
}
function statusTag(s) {
  return { PENDING: 'warning', GRANTED: 'success', INVALID: 'info' }[s] || 'info'
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await contractApi.ipList({ enterpriseId: enterpriseId(), ipType: filterType.value || undefined, page: 1, size: 100 })
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
    ipType: row.ipType, name: row.name, registerNo: row.registerNo, owner: row.owner,
    status: row.status, applyDate: row.applyDate ? row.applyDate.slice(0, 10) : '', expireDate: row.expireDate ? row.expireDate.slice(0, 10) : '',
  })
  showCreate.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { ipType: 'TRADEMARK', name: '', registerNo: '', owner: '', status: 'PENDING', applyDate: '', expireDate: '' })
}

async function save() {
  if (!form.name) {
    ElMessage.warning('请填写名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, enterpriseId: enterpriseId() }
    if (editingId.value) {
      await contractApi.ipUpdate({ ...payload, id: editingId.value })
    } else {
      await contractApi.ipCreate(payload)
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
  await ElMessageBox.confirm('确认删除该知识产权记录？', '提示', { type: 'warning' })
  try {
    await contractApi.ipDelete(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.ip-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
</style>
