<template>
  <div>
    <h2 style="margin-bottom:20px">律师管理</h2>
    <el-card>
      <el-table :data="lawyers" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="nickname" label="姓名" />
        <el-table-column prop="account" label="账号" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === -1 ? 'danger' : 'warning'">
              {{ row.status === 1 ? '已认证' : row.status === -1 ? '已驳回' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link @click="approve(row)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" link @click="reject(row)">驳回</el-button>
            <el-button link type="primary">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" style="margin-top:16px;text-align:center" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'

const lawyers = ref([])
const loading = ref(false)
const total = ref(0)

async function fetchData(page = 1) {
  loading.value = true
  try {
    const res = await userApi.list({ userType: 'LAWYER', page, size: 20 })
    lawyers.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function approve(row) {
  await userApi.update({ id: row.id, status: 1 })
  row.status = 1
  ElMessage.success('已通过认证')
}

async function reject(row) {
  await userApi.update({ id: row.id, status: -1 })
  row.status = -1
  ElMessage.warning('已驳回')
}

onMounted(() => fetchData())
</script>
