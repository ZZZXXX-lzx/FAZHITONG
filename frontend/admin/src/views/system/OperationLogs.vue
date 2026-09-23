<template>
  <div>
    <h2 style="margin-bottom:20px">操作日志</h2>
    <el-card>
      <div style="margin-bottom:16px;display:flex;gap:12px;align-items:center">
        <el-input v-model="keyword" placeholder="搜索操作内容 / 操作人" clearable style="width:280px" @keyup.enter="search" @clear="search" />
        <el-select v-model="resultFilter" placeholder="筛选结果" clearable style="width:120px" @change="search">
          <el-option label="成功" value="成功" />
          <el-option label="失败" value="失败" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
      <el-table :data="logs" stripe v-loading="loading">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="username" label="操作人" width="120">
          <template #default="{ row }">{{ row.username || '—' }}</template>
        </el-table-column>
        <el-table-column prop="action" label="操作内容" min-width="240" />
        <el-table-column prop="ip" label="IP地址" width="140">
          <template #default="{ row }">{{ row.ip || '—' }}</template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === '成功' ? 'success' : 'danger'" size="small">{{ row.result || '—' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && logs.length === 0" description="暂无操作日志" />
      <el-pagination
        v-if="total > 0"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        style="margin-top:16px;text-align:right"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { operationLogApi } from '@/api'

const keyword = ref('')
const resultFilter = ref('')
const loading = ref(false)
const logs = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 19)
}

async function fetchData(p = 1) {
  page.value = p
  loading.value = true
  try {
    const params = { page: p, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (resultFilter.value) params.result = resultFilter.value
    const res = await operationLogApi.list(params)
    logs.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  fetchData(1)
}

onMounted(() => fetchData(1))
</script>
