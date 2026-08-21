<template>
  <div class="lawyer-home">
    <div class="page-header">
      <h2>律师工作台</h2>
      <p>高效办案工具 · AI辅助法律文书</p>
    </div>
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card>
          <template #header><strong>待处理咨询</strong></template>
          <div v-if="pendingConsultations.length === 0" style="color:#999;padding:20px 0;text-align:center">暂无待处理咨询</div>
          <div v-for="item in pendingConsultations" :key="item.id" class="consult-item">
            <p><strong>{{ item.title }}</strong></p>
            <p style="color:#999;font-size:13px">{{ item.createTime }}</p>
            <el-button size="small" type="primary" @click="handleAnswer(item)">回复</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><strong>AI 智能工具</strong></template>
          <el-button style="width:100%;margin-bottom:12px" @click="$router.push('/lawyer/templates')">AI 文书生成</el-button>
          <el-button style="width:100%;margin-bottom:12px" @click="$router.push('/cases')">案例检索</el-button>
          <el-button style="width:100%" @click="$router.push('/lawyer/profile')">律师资料管理</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { consultationApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const pendingConsultations = ref([])
const answerDialog = ref(false)
const currentConsult = ref(null)
const answerContent = ref('')

onMounted(async () => {
  const data = await consultationApi.pending({ page: 1, size: 10 })
  pendingConsultations.value = data.list || []
})

function handleAnswer(item) {
  currentConsult.value = item
  answerDialog.value = true
}

async function submitAnswer() {
  await consultationApi.answer(currentConsult.value.id, userStore.userInfo.userId, answerContent.value)
  ElMessage.success('回复成功')
  answerDialog.value = false
  answerContent.value = ''
  const data = await consultationApi.pending({ page: 1, size: 10 })
  pendingConsultations.value = data.list || []
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
