<template>
  <div class="feedback-page">
    <div class="page-header">
      <h2>意见反馈</h2>
      <p>您的意见对我们很重要，请告诉我们您的想法</p>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.prevent
      >
        <el-form-item label="反馈类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择反馈类型" style="width: 100%">
            <el-option label="Bug反馈" value="BUG" />
            <el-option label="功能建议" value="SUGGESTION" />
            <el-option label="投诉" value="COMPLAINT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请简要描述问题或建议" />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您的反馈内容"
          />
        </el-form-item>

        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="手机号/邮箱（选填）" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交反馈
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="my-feedback-section">
      <h3 class="section-title">我的反馈</h3>
      <div v-loading="loading">
        <el-card
          v-for="item in myFeedback"
          :key="item.id"
          class="feedback-item"
          shadow="never"
        >
          <div class="feedback-row">
            <div class="feedback-type">
              <el-tag :type="typeColor(item.type)" size="small">
                {{ typeLabel(item.type) }}
              </el-tag>
            </div>
            <div class="feedback-content">
              <div class="feedback-title">{{ item.title }}</div>
              <div class="feedback-text">{{ item.content }}</div>
              <div class="feedback-meta">
                <span class="meta-time">{{ formatTime(item.createTime) }}</span>
                <el-tag
                  :type="item.status === 1 ? 'success' : 'info'"
                  size="small"
                  effect="plain"
                >
                  {{ item.status === 1 ? '已回复' : '待处理' }}
                </el-tag>
              </div>
              <div v-if="item.reply" class="feedback-reply">
                <div class="reply-label">官方回复：</div>
                <div class="reply-text">{{ item.reply }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-empty v-if="!loading && myFeedback.length === 0" description="暂无反馈记录" />
      </div>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
        style="margin-top: 20px; text-align: center"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { feedbackApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const myFeedback = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const form = reactive({
  type: '',
  title: '',
  content: '',
  contact: '',
})

const rules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }],
}

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

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await feedbackApi.submit({
      ...form,
      userId: userStore.userInfo.userId,
    })
    ElMessage.success('反馈提交成功，感谢您的支持！')
    form.type = ''
    form.title = ''
    form.content = ''
    form.contact = ''
    fetchMyFeedback()
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function fetchMyFeedback() {
  loading.value = true
  try {
    const res = await feedbackApi.my({
      userId: userStore.userInfo.userId,
      page: page.value,
      size: size.value,
    })
    myFeedback.value = res.list || []
    total.value = res.total || 0
  } catch {
    myFeedback.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  fetchMyFeedback()
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

onMounted(() => {
  fetchMyFeedback()
})
</script>

<style scoped>
.feedback-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}
.page-header p {
  color: #666;
}
.form-card {
  margin-bottom: 32px;
}
.my-feedback-section {
  margin-top: 32px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1a1a2e;
}
.feedback-item {
  margin-bottom: 12px;
}
.feedback-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.feedback-type {
  flex-shrink: 0;
  padding-top: 2px;
}
.feedback-content {
  flex: 1;
  min-width: 0;
}
.feedback-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}
.feedback-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}
.feedback-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}
.meta-time {
  font-size: 12px;
  color: #999;
}
.feedback-reply {
  margin-top: 12px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
}
.reply-label {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
}
.reply-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}
</style>
