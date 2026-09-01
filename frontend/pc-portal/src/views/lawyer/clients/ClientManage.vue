<template>
  <div class="client-manage">
    <div class="page-header">
      <h2>客户管理</h2>
      <p>客户信息台账 · 联系记录</p>
    </div>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <strong>客户列表</strong>
            <el-input v-model="keyword" placeholder="搜索客户姓名/电话" clearable style="width:220px;margin-left:12px" @input="load" />
          </div>
          <el-button type="primary" @click="openCreate">新增客户</el-button>
        </div>
      </template>
      <el-table :data="clients" stripe v-loading="loading">
        <el-table-column prop="clientName" label="客户名称" />
        <el-table-column prop="phone" label="联系电话" width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.clientType === 'ENTERPRISE' ? 'warning' : 'info'" size="small">
              {{ row.clientType === 'ENTERPRISE' ? '企业' : '个人' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑客户' : '新增客户'" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客户名称"><el-input v-model="form.clientName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.clientType" style="width:100%">
            <el-option label="个人" value="PERSONAL" />
            <el-option label="企业" value="ENTERPRISE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input type="textarea" v-model="form.remark" :rows="3" /></el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { consultationApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const clients = ref([])
const loading = ref(false)
const keyword = ref('')
const showDialog = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ clientName: '', phone: '', clientType: 'PERSONAL', remark: '' })

function lawyerId() {
  return userStore.userInfo?.userId || 1
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await consultationApi.clientList({ lawyerId: lawyerId(), keyword: keyword.value || undefined, page: 1, size: 100 })
    clients.value = data.list || []
  } catch {
    ElMessage.error('加载客户失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { clientName: '', phone: '', clientType: 'PERSONAL', remark: '' })
  showDialog.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { clientName: row.clientName, phone: row.phone, clientType: row.clientType, remark: row.remark })
  showDialog.value = true
}
async function save() {
  if (!form.clientName) { ElMessage.warning('请填写客户名称'); return }
  saving.value = true
  try {
    const payload = { ...form, lawyerId: lawyerId() }
    if (editingId.value) {
      await consultationApi.clientUpdate({ ...payload, id: editingId.value })
    } else {
      await consultationApi.clientCreate(payload)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    load()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除该客户？', '提示', { type: 'warning' })
  try {
    await consultationApi.clientDelete(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.client-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
</style>
