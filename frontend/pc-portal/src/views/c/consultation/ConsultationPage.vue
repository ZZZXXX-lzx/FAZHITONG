<template>
  <div class="consultation-page">
    <div class="page-header">
      <h2>法律咨询</h2>
      <p>AI 智能咨询 · 专业律师服务，7×24 小时在线响应</p>
    </div>

    <!-- 快速问题引导 -->
    <div class="quick-questions">
      <span class="qq-label">不知道该问什么？试试：</span>
      <el-tag v-for="q in quickQuestions" :key="q" class="qq" effect="plain" @click="question = q">{{ q }}</el-tag>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="12">
        <el-card>
          <template #header><strong>AI 智能咨询</strong></template>
          <p style="color:#666;margin-bottom:16px">7×24小时在线，立即解答您的法律问题，自动保存咨询记录</p>
          <el-input
            type="textarea"
            v-model="question"
            :rows="5"
            placeholder="请描述您的法律问题，例如：房东不退押金算违法吗？..."
            maxlength="1000" show-word-limit
          />
          <div class="ai-actions">
            <el-button type="primary" :loading="aiLoading" @click="askAI">立即咨询</el-button>
            <el-button v-if="question" @click="question = ''">清空</el-button>
          </div>
          <el-divider />
          <div v-if="aiLoading" class="answer-box loading-box">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="aiAnswer" class="answer-box">
            <div class="answer-head"><strong>AI 回复</strong><el-tag size="small" type="success">已保存为咨询记录</el-tag></div>
            <p style="white-space:pre-wrap">{{ aiAnswer }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card>
          <template #header><strong>律师在线咨询</strong></template>
          <p style="color:#666;margin-bottom:16px">专业律师团队，5分钟快速响应</p>
          <template v-if="canLawyerConsult">
            <el-button type="success" @click="showLawyerDialog = true">发起律师咨询</el-button>
          </template>
          <el-alert v-else type="warning" :closable="false" show-icon>请先登录后发起律师咨询</el-alert>
          <el-divider />

          <div class="tabs-row">
            <span
              v-for="t in tabs"
              :key="t.key"
              class="tab-chip"
              :class="{ active: activeTab === t.key }"
              @click="switchTab(t.key)"
            >{{ t.label }}</span>
          </div>

          <div class="consult-list" v-loading="listLoading">
            <div v-for="item in filtered" :key="item.id" class="consult-item" @click="viewDetail(item)">
              <p class="consult-title">
                <strong>{{ item.title }}</strong>
                <span class="consult-tags">
                  <el-tag v-if="item.consultationType === 'LAWYER'" size="small" type="warning" effect="plain">律师</el-tag>
                  <el-tag v-else size="small" type="info" effect="plain">AI</el-tag>
                  <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small">{{ item.status === 1 ? '已回复' : '待回复' }}</el-tag>
                </span>
              </p>
              <p class="consult-answer" v-if="item.answer">{{ item.answer }}</p>
              <p class="consult-time">{{ formatTime(item.createTime) }}</p>
            </div>
            <el-empty v-if="!listLoading && filtered.length === 0" :description="activeTab ? '该分类下暂无咨询记录' : '暂无咨询记录'" :image-size="72" />
          </div>
          <div v-if="hasMore" class="load-more" @click="loadMore">
            <el-button text type="primary" :loading="moreLoading">加载更多</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发起律师咨询 -->
    <el-dialog v-model="showLawyerDialog" title="发起律师咨询" :width="isMobile ? '94%' : '520px'">
      <el-form>
        <el-form-item label="标题"><el-input v-model="lawyerConsult.title" placeholder="一句话概括问题" /></el-form-item>
        <el-form-item label="问题描述"><el-input type="textarea" v-model="lawyerConsult.question" :rows="5" placeholder="请详细描述情况，便于律师快速判断" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLawyerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLawyerConsult">提交</el-button>
      </template>
    </el-dialog>

    <!-- 咨询详情 -->
    <el-dialog v-model="detailVisible" title="咨询详情" :width="isMobile ? '94%' : '700px'">
      <template v-if="detailItem">
        <p><strong>标题：</strong>{{ detailItem.title }}</p>
        <p><strong>类型：</strong>{{ detailItem.consultationType === 'LAWYER' ? '律师咨询' : 'AI咨询' }}</p>
        <p><strong>状态：</strong><el-tag :type="detailItem.status === 1 ? 'success' : 'warning'" size="small">{{ detailItem.status === 1 ? '已回复' : '待回复' }}</el-tag></p>
        <p><strong>提交时间：</strong>{{ formatTime(detailItem.createTime) }}</p>
        <el-divider />
        <h4>问题描述</h4>
        <p class="pre-block">{{ detailItem.question }}</p>
        <template v-if="detailItem.answer">
          <el-divider />
          <h4>回复内容</h4>
          <div class="answer-box"><p style="white-space:pre-wrap">{{ detailItem.answer }}</p></div>
          <p v-if="detailItem.answerTime" class="answer-time">回复时间：{{ formatTime(detailItem.answerTime) }}</p>
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
import { isMobileDevice } from '@/utils/device'

const router = useRouter()
const userStore = useUserStore()
const isMobile = isMobileDevice()

const question = ref('')
const aiAnswer = ref('')
const aiLoading = ref(false)
const showLawyerDialog = ref(false)
const lawyerConsult = ref({ title: '', question: '' })
const canLawyerConsult = computed(() => ['USER', 'LAWYER', 'ENTERPRISE', 'ADMIN'].includes(userStore.userType))
const detailVisible = ref(false)
const detailItem = ref(null)

const quickQuestions = [
  '房东不退押金怎么办？',
  '试用期无故被辞退能索赔吗？',
  '借款利息超过多少算高利贷？',
  '交通事故责任如何划分？',
]

// 记录分页
const myConsultations = ref([])
const listLoading = ref(false)
const moreLoading = ref(false)
const page = ref(1)
const pageSize = 10
const pageTotal = ref(0)
const activeTab = ref('')
const tabs = [
  { key: '', label: '全部' },
  { key: 'AI', label: 'AI咨询' },
  { key: 'LAWYER', label: '律师咨询' },
  { key: 'pending', label: '待回复' },
  { key: 'answered', label: '已回复' },
]

const filtered = computed(() => {
  if (!activeTab.value) return myConsultations.value
  return myConsultations.value.filter(c => {
    if (activeTab.value === 'AI' || activeTab.value === 'LAWYER') return c.consultationType === activeTab.value
    if (activeTab.value === 'pending') return c.status !== 1
    if (activeTab.value === 'answered') return c.status === 1
    return true
  })
})

const hasMore = computed(() => page.value * pageSize < pageTotal.value)

onMounted(() => {
  if (userStore.isLoggedIn) loadMyConsultations(true)
})

function switchTab(key) {
  activeTab.value = key
}

async function loadMyConsultations(reset = false) {
  if (!userStore.isLoggedIn) return
  const p = reset ? 1 : page.value + 1
  listLoading.value = reset
  moreLoading.value = !reset
  try {
    const data = await consultationApi.my(userStore.userInfo.userId, { page: p, size: pageSize })
    page.value = p
    pageTotal.value = data.total || 0
    const list = data.list || []
    myConsultations.value = reset ? list : myConsultations.value.concat(list)
  } catch {
    ElMessage.error('加载咨询记录失败')
  } finally {
    listLoading.value = false
    moreLoading.value = false
  }
}

function loadMore() {
  loadMyConsultations(false)
}

async function askAI() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后使用 AI 咨询')
    router.push('/login')
    return
  }
  const q = question.value.trim()
  if (!q) {
    ElMessage.warning('请输入您要咨询的问题')
    return
  }
  aiLoading.value = true
  try {
    const data = await consultationApi.create({ userId: userStore.userInfo.userId, title: q.slice(0, 20), question: q, type: 'AI' })
    aiAnswer.value = data?.answer || '已收到您的问题，我们会尽快为您解答'
    loadMyConsultations(true)
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    aiLoading.value = false
  }
}

async function submitLawyerConsult() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后发起律师咨询')
    return
  }
  if (!lawyerConsult.value.title.trim() || !lawyerConsult.value.question.trim()) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await consultationApi.create({ userId: userStore.userInfo.userId, title: lawyerConsult.value.title, question: lawyerConsult.value.question, type: 'LAWYER' })
    ElMessage.success('咨询已提交，律师将尽快回复')
    showLawyerDialog.value = false
    lawyerConsult.value = { title: '', question: '' }
    loadMyConsultations(true)
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
.quick-questions { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 18px; }
.qq-label { font-size: 13px; color: #888; }
.qq { cursor: pointer; }
.ai-actions { margin-top: 12px; }
.answer-box { background: #f0f9ff; padding: 16px; border-radius: 8px; white-space: pre-wrap; }
.loading-box { padding: 8px; }
.answer-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.tabs-row { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.tab-chip { font-size: 13px; padding: 4px 12px; border-radius: 14px; background: #f2f3f5; color: #555; cursor: pointer; transition: all .2s; }
.tab-chip.active { background: #1a56db; color: #fff; }
.consult-list { min-height: 120px; }
.consult-item { padding: 12px 4px; border-bottom: 1px solid #eee; cursor: pointer; }
.consult-item:last-child { border-bottom: none; }
.consult-title { display: flex; justify-content: space-between; gap: 10px; align-items: flex-start; }
.consult-tags { display: inline-flex; gap: 4px; white-space: nowrap; }
.consult-answer { color: #666; font-size: 13px; margin-top: 6px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.consult-time { color: #999; font-size: 12px; margin-top: 6px; }
.load-more { text-align: center; margin-top: 8px; }
.pre-block { white-space: pre-wrap; background: #f5f7fa; padding: 12px; border-radius: 6px; }
.answer-time { color: #999; font-size: 13px; margin-top: 8px; }
@media (max-width: 560px) {
  .consultation-page { padding: 16px 12px; }
  .consult-title { flex-direction: column; gap: 4px; }
}
</style>