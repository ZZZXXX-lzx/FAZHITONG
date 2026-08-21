<template>
  <div>
    <h2 style="margin-bottom:20px">操作日志</h2>
    <el-card>
      <div style="margin-bottom:16px;display:flex;gap:12px;align-items:center">
        <el-input v-model="keyword" placeholder="搜索操作内容" clearable style="width:300px" @input="filterLogs" />
        <el-select v-model="resultFilter" placeholder="筛选结果" clearable style="width:120px" @change="filterLogs">
          <el-option label="全部" value="" />
          <el-option label="成功" value="成功" />
          <el-option label="失败" value="失败" />
        </el-select>
      </div>
      <el-table :data="filteredLogs" stripe v-loading="loading">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="user" label="操作人" width="120" />
        <el-table-column prop="action" label="操作内容" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === '成功' ? 'success' : 'danger'" size="small">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" style="margin-top:16px;text-align:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const keyword = ref('')
const resultFilter = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20

const allLogs = ref([
  { time: '2026-08-22 09:00:00', user: 'admin', action: '登录管理后台', ip: '127.0.0.1', result: '成功' },
  { time: '2026-08-22 08:30:00', user: 'admin', action: '审核律师认证 - 张律师', ip: '127.0.0.1', result: '成功' },
  { time: '2026-08-21 23:00:00', user: '系统', action: '每日数据备份完成', ip: '-', result: '成功' },
  { time: '2026-08-21 16:30:00', user: 'admin', action: '新增文书模板 - 民事起诉状', ip: '127.0.0.1', result: '成功' },
  { time: '2026-08-21 15:00:00', user: 'admin', action: '审核企业认证 - 某某公司', ip: '127.0.0.1', result: '成功' },
  { time: '2026-08-21 10:00:00', user: 'admin', action: '修改系统配置 - 关闭注册', ip: '127.0.0.1', result: '成功' },
  { time: '2026-08-20 18:00:00', user: 'admin', action: '删除用户 - test_user', ip: '127.0.0.1', result: '成功' },
  { time: '2026-08-20 14:00:00', user: 'unknown', action: '尝试登录失败', ip: '192.168.1.100', result: '失败' },
])

const filteredLogs = computed(() => {
  let result = allLogs.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(l => l.action.toLowerCase().includes(kw) || l.user.toLowerCase().includes(kw))
  }
  if (resultFilter.value) {
    result = result.filter(l => l.result === resultFilter.value)
  }
  return result
})

const total = computed(() => filteredLogs.value.length)

function filterLogs() {
  currentPage.value = 1
}

onMounted(() => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 300)
})
</script>
