<template>
  <div>
    <h2 style="margin-bottom:20px">用户管理</h2>
    <el-card>
      <div style="margin-bottom:16px;display:flex;gap:12px">
        <el-input v-model="keyword" placeholder="搜索用户" style="width:300px" clearable />
        <el-select v-model="userType" placeholder="用户类型" clearable style="width:140px">
          <el-option label="普通用户" value="USER" />
          <el-option label="律师" value="LAWYER" />
          <el-option label="企业" value="ENTERPRISE" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-button type="primary" @click="loadUsers">搜索</el-button>
        <el-button @click="showCreate = true">新增用户</el-button>
      </div>
      <el-table :data="users" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="account" label="账号" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="userType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.userType === 'ADMIN'" type="danger">管理员</el-tag>
            <el-tag v-else-if="row.userType === 'LAWYER'" type="warning">律师</el-tag>
            <el-tag v-else-if="row.userType === 'ENTERPRISE'" type="success">企业</el-tag>
            <el-tag v-else>用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="20" style="margin-top:16px;text-align:center" />
    </el-card>
    <el-dialog v-model="showCreate" title="新增用户" width="500px">
      <el-form label-width="100px">
        <el-form-item label="账号"><el-input v-model="createForm.account" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="createForm.password" type="password" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="createForm.nickname" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="createForm.userType">
            <el-option label="普通用户" value="USER" />
            <el-option label="律师" value="LAWYER" />
            <el-option label="企业" value="ENTERPRISE" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'

const keyword = ref('')
const userType = ref('')
const users = ref([])
const total = ref(0)
const showCreate = ref(false)
const createForm = ref({ account: '', password: '', nickname: '', userType: 'USER' })

onMounted(() => loadUsers())

async function loadUsers() {
  const data = await userApi.list({ keyword: keyword.value || undefined, userType: userType.value || undefined, page: 1, size: 20 })
  users.value = data.list || []
  total.value = data.total
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该用户？').then(async () => {
    await userApi.delete(row.id)
    ElMessage.success('已删除')
    loadUsers()
  }).catch(() => {})
}

function toggleStatus(row) {
  userApi.update({ id: row.id, status: row.status === 1 ? 0 : 1 })
  ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
  loadUsers()
}

async function handleCreate() {
  await userApi.create(createForm.value)
  ElMessage.success('创建成功')
  showCreate.value = false
  loadUsers()
}
</script>
