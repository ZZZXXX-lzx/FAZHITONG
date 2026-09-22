<template>
  <div class="finance-page">
    <h2 style="margin-bottom:20px">财务管理</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总收入</div>
          <div class="stat-value money">{{ fmtMoney(stats.totalIncome) }}</div>
          <div class="stat-sub">已支付订单累计</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">本月收入</div>
          <div class="stat-value money">{{ fmtMoney(stats.monthIncome) }}</div>
          <div class="stat-sub">本月已支付</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ stats.totalOrders ?? 0 }}</div>
          <div class="stat-sub">已支付 {{ stats.paidOrders ?? 0 }} · 已取消 {{ stats.cancelledOrders ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">待支付订单</div>
          <div class="stat-value warn">{{ stats.pendingOrders ?? 0 }}</div>
          <div class="stat-sub">等待用户付款</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单列表 -->
    <el-card>
      <template #header>
        <div class="table-header">
          <strong>订单列表</strong>
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:140px" @change="fetchData(1)">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </div>
      </template>
      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.orderType)">{{ typeLabel(row.orderType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">{{ fmtMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="支付时间" width="180">
          <template #default="{ row }">{{ row.payTime ? formatTime(row.payTime) : '—' }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="20"
        :current-page="page"
        style="margin-top:16px;text-align:right"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { paymentApi } from '@/api'

const orders = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const statusFilter = ref(null)

const stats = reactive({
  totalIncome: 0,
  monthIncome: 0,
  totalOrders: 0,
  pendingOrders: 0,
  paidOrders: 0,
  cancelledOrders: 0,
})

function fmtMoney(v) {
  const n = Number(v || 0)
  return '¥ ' + n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function statusLabel(s) {
  return s === 0 ? '待支付' : s === 1 ? '已支付' : '已取消'
}

function typeLabel(t) {
  const map = {
    MEMBER: '会员购买',
    CONSULTATION: '法律咨询',
    LAWYER_SERVICE: '律师服务',
    DOCUMENT: '文书服务',
    CONTRACT: '合同服务',
  }
  return map[t] || t || '其他'
}

function typeTag(t) {
  const map = {
    MEMBER: 'warning',
    CONSULTATION: 'primary',
    LAWYER_SERVICE: 'success',
    DOCUMENT: 'info',
    CONTRACT: 'info',
  }
  return map[t] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadStats() {
  try {
    const s = await paymentApi.stats()
    if (s) Object.assign(stats, s)
  } catch (e) {
    /* 统计失败不阻塞列表 */
  }
}

async function fetchData(p = 1) {
  page.value = p
  loading.value = true
  try {
    const params = { page: p, size: 20 }
    if (statusFilter.value !== null && statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const res = await paymentApi.orders(params)
    orders.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  fetchData(1)
})
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-label { color: #909399; font-size: 14px; }
.stat-value { font-size: 30px; font-weight: 700; color: #1a56db; margin: 10px 0 6px; }
.stat-value.money { font-size: 24px; }
.stat-value.warn { color: #e6a23c; }
.stat-sub { color: #c0c4cc; font-size: 12px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
</style>
