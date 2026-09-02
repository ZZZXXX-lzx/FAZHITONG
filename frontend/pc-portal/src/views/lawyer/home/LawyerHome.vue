<template>
  <div class="lawyer-home">
    <div class="page-header">
      <h2>律师工作台</h2>
      <p>高效办案工具 · AI辅助法律文书</p>
    </div>
    <el-row :gutter="24">
      <el-col :xs="24" :sm="8">
        <el-card>
          <template #header>
            <strong>法律咨询</strong>
            <el-radio-group v-model="consultTab" size="small" style="margin-left:12px" @change="watchTab">
              <el-radio-button label="pending">待处理</el-radio-button>
              <el-radio-button label="all">全部</el-radio-button>
            </el-radio-group>
          </template>
          <div v-if="consultList.length === 0" style="color:#999;padding:20px 0;text-align:center">暂无咨询</div>
          <div v-for="item in consultList" :key="item.id" class="consult-item">
            <p><strong>{{ item.title }}</strong>
              <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small" style="margin-left:4px">
                {{ item.consultationType === 'AI' ? 'AI咨询' : (item.status === 1 ? '已回复' : '待回复') }}
              </el-tag>
            </p>
            <p style="color:#999;font-size:13px">{{ formatTime(item.createTime) }}</p>
            <div style="margin-top:4px">
              <el-button size="small" @click="handleView(item)">查看详情</el-button>
              <el-button v-if="item.status === 0" size="small" type="primary" @click="handleAnswer(item)">回复</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card>
          <template #header><strong>AI 智能工具</strong></template>
          <el-button style="width:100%;margin-bottom:12px" @click="$router.push('/lawyer/templates')">AI 文书生成</el-button>
          <el-button style="width:100%;margin-bottom:12px" @click="$router.push('/cases')">案例检索</el-button>
          <el-button style="width:100%;margin-bottom:12px" @click="$router.push('/lawyer/cases')">案件管理</el-button>
          <el-button style="width:100%;margin-bottom:12px" @click="$router.push('/lawyer/clients')">客户管理</el-button>
          <el-button style="width:100%" @click="$router.push('/lawyer/profile')">律师资料管理</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card>
          <template #header><strong>收费报价</strong></template>
          <p>法律文书：<strong>99-299元/份</strong></p>
          <p>合同审查：<strong>199-699元/份</strong></p>
          <p>律师咨询：<strong>50-200元/次</strong></p>
          <p>案件代理：<strong>面议</strong></p>
          <el-divider />
          <el-button type="warning" style="width:100%">设置服务价格</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="answerDialog" title="回复咨询">
      <p><strong>{{ currentConsult?.title }}</strong></p>
      <p style="color:#666;margin:12px 0">{{ currentConsult?.question }}</p>
      <el-input type="textarea" v-model="answerContent" :rows="6" placeholder="请输入回复内容..." />
      <template #footer>
        <el-button @click="answerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAnswer">提交回复</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog" title="咨询详情" width="640px">
      <template v-if="detailItem">
        <p><strong>标题：</strong>{{ detailItem.title }}</p>
        <p><strong>类型：</strong>{{ detailItem.consultationType === 'AI' ? 'AI 咨询' : '律师咨询' }}</p>
        <p><strong>状态：</strong>
          <el-tag :type="detailItem.status === 1 ? 'success' : 'warning'" size="small">
            {{ detailItem.status === 1 ? '已回复' : '待回复' }}
          </el-tag>
        </p>
        <p><strong>提交时间：</strong>{{ formatTime(detailItem.createTime) }}</p>
        <el-divider />
        <h4>问题描述</h4>
        <p style="white-space:pre-wrap;background:#f5f7fa;padding:12px;border-radius:6px">{{ detailItem.question }}</p>
        <el-divider v-if="detailItem.answer" />
        <template v-if="detailItem.answer">
          <h4>回复内容</h4>
          <p style="white-space:pre-wrap;background:#f0f9ff;padding:12px;border-radius:6px">{{ detailItem.answer }}</p>
          <p v-if="detailItem.answerTime" style="color:#999;font-size:13px;margin-top:8px">回复时间：{{ formatTime(detailItem.answerTime) }}</p>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { consultationApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const consultTab = ref('pending')
const pendingConsultations = ref([])
const allConsultations = ref([])
const answerDialog = ref(false)
const currentConsult = ref(null)
const answerContent = ref('')
const detailDialog = ref(false)
const detailItem = ref(null)

const consultList = computed(() => consultTab.value === 'pending' ? pendingConsultations.value : allConsultations.value)

onMounted(loadPending)

async function loadPending() {
  try {
    const data = await consultationApi.pending({ page: 1, size: 50 })
    pendingConsultations.value = data.list || []
  } catch { pendingConsultations.value = [] }
}

async function loadAll() {
  try {
    const data = await consultationApi.all({ page: 1, size: 50 })
    allConsultations.value = data.list || []
  } catch { allConsultations.value = [] }
}

function watchTab() {
  if (consultTab.value === 'all') loadAll()
}

function formatTime(time) {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return String(time)
}

function handleAnswer(item) {
  currentConsult.value = item
  answerDialog.value = true
}

async function handleView(item) {
  try {
    detailItem.value = await consultationApi.getById(item.id)
    detailDialog.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

async function submitAnswer() {
  await consultationApi.answer(currentConsult.value.id, userStore.userInfo.userId, answerContent.value)
  ElMessage.success('回复成功')
  answerDialog.value = false
  answerContent.value = ''
  loadPending()
  loadAll()
}
</script>

<style scoped>
.lawyer-home { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.consult-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.consult-item:last-child { border-bottom: none; }
</style>
