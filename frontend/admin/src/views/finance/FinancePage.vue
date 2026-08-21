<template>
  <div>
    <h2 style="margin-bottom:20px">财务管理</h2>
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="8">
        <el-card><div class="finance-stat">本月收入</div><div class="finance-value">¥ {{ monthlyIncome }}</div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div class="finance-stat">总订单数</div><div class="finance-value">{{ totalOrders }}</div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div class="finance-stat">待支付订单</div><div class="finance-value">{{ pendingOrders }}</div></el-card>
      </el-col>
    </el-row>
    <el-card>
      <template #header><strong>订单列表</strong></template>
      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="orderType" label="类型" width="100" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'">
              {{ row.status === 0 ? '待支付' : row.status === 1 ? '已支付' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" style="margin-top:16px;text-align:center" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { paymentApi } from '@/api'

const orders = ref([])
const loading = ref(false)
const total = ref(0)

const monthlyIncome = computed(() => {
  const paid = orders.value.filter(o => o.status === 1)
  const totalAmount = paid.reduce((sum, o) => sum + (o.amount || 0), 0)
  return totalAmount.toLocaleString()
})

const totalOrders = computed(() => orders.value.length)
const pendingOrders = computed(() => orders.value.filter(o => o.status === 0).length)

async function fetchData(page = 1) {
  loading.value = true
  try {
    const res = await paymentApi.orders({ page, size: 20 })
    orders.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.finance-stat { color: #666; font-size: 14px; }
.finance-value { font-size: 28px; font-weight: 700; color: #1a56db; margin-top: 8px; }
</style>
