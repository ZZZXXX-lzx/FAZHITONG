<template>
  <div class="consultation-page">
    <div class="page-header">
      <h2>法律咨询</h2>
      <p>AI智能咨询 · 专业律师服务</p>
    </div>
    <el-row :gutter="24">
      <el-col :span="12">
        <el-card>
          <template #header><strong>AI 智能咨询</strong></template>
          <p style="color:#666;margin-bottom:16px">7×24小时在线，立即解答您的法律问题</p>
          <el-input type="textarea" v-model="question" :rows="4" placeholder="请描述您的法律问题..." />
          <el-button type="primary" style="margin-top:12px" :loading="aiLoading" @click="askAI">立即咨询</el-button>
          <el-divider />
          <div v-if="aiAnswer" class="answer-box">
            <strong>AI回复：</strong>
            <p>{{ aiAnswer }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><strong>律师在线咨询</strong></template>
          <p style="color:#666;margin-bottom:16px">专业律师团队，5分钟快速响应</p>
          <template v-if="canLawyerConsult">
            <el-button type="success" @click="showLawyerDialog = true">发起律师咨询</el-button>
          </template>
          <el-alert v-else type="warning" :closable="false" show-icon>请先登录后发起律师咨询</el-alert>
          <el-divider />
          <h4 style="margin-bottom:12px">我的咨询记录</h4>
          <div v-for="item in myConsultations" :key="item.id" class="consult-item" style="cursor:pointer" @click="viewDetail(item)">
            <p><strong>{{ item.title }}</strong> <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small">{{ item.status === 1 ? '已回复' : '待回复' }}</el-tag></p>
            <p style="color:#999;font-size:13px">{{ formatTime(item.createTime) }}</p>
          </div>
          <p v-if="!myConsultations.length" style="color:#999;text-align:center;padding:20px 0">暂无咨询记录</p>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="showLawyerDialog" title="发起律师咨询">
      <el-form>
        <el-form-item label="标题"><el-input v-model="lawyerConsult.title" /></el-form-item>
        <el-form-item label="问题描述"><el-input type="textarea" v-model="lawyerConsult.question" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLawyerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLawyerConsult">提交</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="detailVisible" title="咨询详情" width="700px">
      <template v-if="detailItem">
        <p><strong>标题：</strong>{{ detailItem.title }}</p>
        <p><strong>类型：</strong>{{ detailItem.consultationType === 'LAWYER' ? '律师咨询' : 'AI咨询' }}</p>
        <p><strong>状态：</strong><el-tag :type="detailItem.status === 1 ? 'success' : 'warning'" size="small">{{ detailItem.status === 1 ? '已回复' : '待回复' }}</el-tag></p>
        <p><strong>提交时间：</strong>{{ formatTime(detailItem.createTime) }}</p>
        <el-divider />
        <h4>问题描述</h4>
        <p style="white-space:pre-wrap;background:#f5f7fa;padding:12px;border-radius:6px">{{ detailItem.question }}</p>
        <el-divider v-if="detailItem.answer" />
        <template v-if="detailItem.answer">
          <h4>回复内容</h4>
          <div class="answer-box"><p style="white-space:pre-wrap">{{ detailItem.answer }}</p></div>
          <p v-if="detailItem.answerTime" style="color:#999;font-size:13px;margin-top:8px">回复时间：{{ formatTime(detailItem.answerTime) }}</p>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { consultationApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const question = ref('')
const aiAnswer = ref('')
const aiLoading = ref(false)
const showLawyerDialog = ref(false)
const myConsultations = ref([])
const lawyerConsult = ref({ title: '', question: '' })
const canLawyerConsult = computed(() => ['USER', 'LAWYER', 'ENTERPRISE', 'ADMIN'].includes(userStore.userType))
const detailVisible = ref(false)
const detailItem = ref(null)

onMounted(() => {
  if (userStore.isLoggedIn) loadMyConsultations()
})

async function askAI() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后使用 AI 咨询')
    router.push('/login')
    return
  }
  if (!question.value.trim()) {
    ElMessage.warning('请输入您要咨询的问题')
    return
  }
  aiLoading.value = true
  try {
    const data = await consultationApi.create({ userId: userStore.userInfo.userId, title: 'AI咨询', question: question.value, type: 'AI' })
    aiAnswer.value = data?.answer || '暂未生成回复，请稍后重试'
    loadMyConsultations()
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    aiLoading.value = false
  }
}

async function loadMyConsultations() {
  try {
    const data = await consultationApi.my(userStore.userInfo.userId, { page: 1, size: 10 })
    myConsultations.value = data.list || []
  } catch {
    ElMessage.error('加载咨询记录失败')
  }
}

async function submitLawyerConsult() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后发起律师咨询')
    return
  }
  if (!lawyerConsult.value.title || !lawyerConsult.value.question) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await consultationApi.create({ userId: userStore.userInfo.userId, title: lawyerConsult.value.title, question: lawyerConsult.value.question, type: 'LAWYER' })
    ElMessage.success('咨询已提交，律师将尽快回复')
    showLawyerDialog.value = false
    lawyerConsult.value = { title: '', question: '' }
    loadMyConsultations()
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  }
}

async function viewDetail(item) {
  try {
    detailItem.value = await consultationApi.getById(item.id)
    detailVisible.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}
</script>

<style scoped>
.consultation-page { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.answer-box { background: #f0f9ff; padding: 16px; border-radius: 8px; white-space: pre-wrap; }
.consult-item { padding: 12px 0; border-bottom: 1px solid #eee; }
.consult-item:last-child { border-bottom: none; }
</style>
