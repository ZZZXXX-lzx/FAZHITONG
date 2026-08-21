<template>
  <div>
    <h2 style="margin-bottom:20px">企业管理</h2>
    <el-card>
      <el-table :data="enterprises" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="nickname" label="企业名称" />
        <el-table-column prop="account" label="账号" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已认证' : '待认证' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link @click="approve(row)">通过认证</el-button>
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

const enterprises = ref([])
const loading = ref(false)
const total = ref(0)

async function fetchData(page = 1) {
  loading.value = true
  try {
    const res = await userApi.list({ userType: 'ENTERPRISE', page, size: 20 })
    enterprises.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function approve(row) {
  await userApi.update({ id: row.id, status: 1 })
  row.status = 1
  ElMessage.success('认证通过')
}

onMounted(() => fetchData())
</script>
