<template>
  <div>
    <h2 style="margin-bottom: 20px">反馈管理</h2>

    <el-card>
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center">
        <el-select v-model="filterType" placeholder="筛选类型" clearable style="width: 160px" @change="fetchList">
          <el-option label="Bug反馈" value="BUG" />
          <el-option label="功能建议" value="SUGGESTION" />
          <el-option label="投诉" value="COMPLAINT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 160px" @change="fetchList">
          <el-option label="待处理" :value="0" />
          <el-option label="已回复" :value="1" />
        </el-select>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeColor(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已回复' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复" min-width="160" show-overflow-tooltip />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openReplyDialog(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
        style="margin-top: 16px; text-align: center"
      />
    </el-card>

    <el-dialog v-model="replyDialogVisible" title="回复反馈" width="600px">
      <template v-if="currentFeedback">
        <div style="margin-bottom: 16px">
          <p><strong>标题：</strong>{{ currentFeedback.title }}</p>
          <p><strong>类型：</strong>{{ typeLabel(currentFeedback.type) }}</p>
          <p><strong>内容：</strong></p>
          <div style="background: #f5f7fa; padding: 12px; border-radius: 6px; white-space: pre-wrap">{{ currentFeedback.content }}</div>
        </div>
        <el-divider />
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="4"
          placeholder="请输入回复内容"
        />
      </template>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleReply">确认回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { feedbackApi } from '@/api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const filterType = ref('')
const filterStatus = ref('')

const replyDialogVisible = ref(false)
const submitting = ref(false)
const currentFeedback = ref(null)
const replyContent = ref('')

const typeMap = {
  BUG: { label: 'Bug', color: 'danger' },
  SUGGESTION: { label: '建议', color: 'success' },
  COMPLAINT: { label: '投诉', color: 'warning' },
  OTHER: { label: '其他', color: 'info' },
}

function typeLabel(type) {
  return typeMap[type]?.label || '其他'
}

function typeColor(type) {
  return typeMap[type]?.color || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterType.value) params.type = filterType.value
    if (filterStatus.value !== '') params.status = filterStatus.value
    const res = await feedbackApi.list(params)
    let data = res.list || []
    // client-side filter as fallback if backend doesn't support all filters
    if (filterType.value) data = data.filter(item => item.type === filterType.value)
    if (filterStatus.value !== '') data = data.filter(item => item.status === filterStatus.value)
    list.value = data
    total.value = res.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  fetchList()
}

function openReplyDialog(row) {
  currentFeedback.value = row
  replyContent.value = row.reply || ''
  replyDialogVisible.value = true
}

async function handleReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitting.value = true
  try {
    await feedbackApi.reply(currentFeedback.value.id, replyContent.value.trim())
    ElMessage.success('回复成功')
    currentFeedback.value.status = 1
    currentFeedback.value.reply = replyContent.value.trim()
    replyDialogVisible.value = false
  } catch {
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
</style>
