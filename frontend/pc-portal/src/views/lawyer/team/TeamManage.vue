<template>
  <div class="team-manage">
    <div class="page-header">
      <h2>团队管理</h2>
      <p>律所团队成员 · 角色与在职状态</p>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.total }}</span><span class="lbl">团队人数</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.active }}</span><span class="lbl">在职</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.partner }}</span><span class="lbl">合伙人</span></div></el-card></el-col>
      <el-col :xs="12" :sm="6"><el-card shadow="never"><div class="stat"><span class="num">{{ summary.lawyer }}</span><span class="lbl">执业律师</span></div></el-card></el-col>
    </el-row>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <strong>团队成员</strong>
            <el-input v-model="keyword" placeholder="搜索姓名/电话/邮箱" clearable style="width:220px;margin-left:12px" @input="load" />
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width:120px;margin-left:12px" @change="load">
              <el-option label="在职" value="ACTIVE" />
              <el-option label="离职" value="INACTIVE" />
            </el-select>
          </div>
          <el-button type="primary" @click="openCreate">新增成员</el-button>
        </div>
      </template>
      <el-table :data="members" stripe v-loading="loading">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">{{ roleText(row.role) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="160" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="入职日期" width="130">
          <template #default="{ row }">{{ row.joinDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '在职' : '离职' }}</el-tag>
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

    <el-dialog v-model="showDialog" :title="editingId ? '编辑成员' : '新增成员'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="合伙人" value="PARTNER" />
            <el-option label="执业律师" value="LAWYER" />
            <el-option label="律师助理" value="ASSISTANT" />
            <el-option label="实习生" value="INTERN" />
            <el-option label="行政" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="入职日期"><el-date-picker v-model="form.joinDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button label="ACTIVE">在职</el-radio-button>
            <el-radio-button label="INACTIVE">离职</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input type="textarea" v-model="form.remark" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { consultationApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const members = ref([])
const loading = ref(false)
const keyword = ref('')
const filterStatus = ref('')
const showDialog = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ name: '', role: 'LAWYER', phone: '', email: '', joinDate: '', status: 'ACTIVE', remark: '' })

const summary = computed(() => {
  const s = { total: members.value.length, active: 0, partner: 0, lawyer: 0 }
  for (const m of members.value) {
    if (m.status === 'ACTIVE') s.active++
    if (m.role === 'PARTNER') s.partner++
    if (m.role === 'LAWYER') s.lawyer++
  }
  return s
})

function lawyerId() {
  return userStore.userInfo?.userId || 1
}
function roleText(r) {
  return { PARTNER: '合伙人', LAWYER: '执业律师', ASSISTANT: '律师助理', INTERN: '实习生', ADMIN: '行政' }[r] || r
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await consultationApi.teamList({ lawyerId: lawyerId(), keyword: keyword.value || undefined, status: filterStatus.value || undefined, page: 1, size: 100 })
    members.value = data.list || []
  } catch {
    ElMessage.error('加载成员失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', role: 'LAWYER', phone: '', email: '', joinDate: '', status: 'ACTIVE', remark: '' })
  showDialog.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { name: row.name, role: row.role, phone: row.phone, email: row.email, joinDate: row.joinDate || '', status: row.status, remark: row.remark })
  showDialog.value = true
}
async function save() {
  if (!form.name) { ElMessage.warning('请填写姓名'); return }
  saving.value = true
  try {
    const payload = { ...form, lawyerId: lawyerId() }
    if (editingId.value) {
      await consultationApi.teamUpdate({ ...payload, id: editingId.value })
    } else {
      await consultationApi.teamCreate(payload)
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
  await ElMessageBox.confirm('确认删除该成员？', '提示', { type: 'warning' })
  try {
    await consultationApi.teamDelete(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.team-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.stat { text-align: center; }
.stat .num { font-size: 22px; font-weight: 700; color: #1a56db; display: block; }
.stat .lbl { font-size: 12px; color: #999; margin-top: 4px; display: block; }
</style>
