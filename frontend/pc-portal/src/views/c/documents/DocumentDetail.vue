<template>
  <div class="document-detail">
    <div class="page-header">
      <el-button @click="$router.back()">← 返回</el-button>
      <h2>{{ template.name }}</h2>
      <p>类型: {{ template.type }}</p>
    </div>
    <el-card v-if="template.content">
      <pre class="template-content">{{ template.content }}</pre>
    </el-card>
    <el-button v-if="userStore.isLoggedIn" type="primary" size="large" style="margin-top:20px" @click="handleGenerate">使用此模板生成文书</el-button>
    <el-alert v-else type="info" show-icon :closable="false" style="margin-top:20px">请先登录后使用文书生成功能</el-alert>

    <el-dialog v-model="showResult" title="生成结果" width="700px">
      <pre class="result-content">{{ generatedContent }}</pre>
      <template #footer>
        <el-button @click="downloadContent">下载文本文件</el-button>
        <el-button type="primary" @click="showResult = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { documentApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const template = ref({})
const showResult = ref(false)
const generatedContent = ref('')

onMounted(async () => {
  template.value = await documentApi.getTemplate(route.params.id)
})

async function handleGenerate() {
  const record = await documentApi.generate({ userId: userStore.userInfo.userId, templateId: template.value.id, data: {} })
  generatedContent.value = record.data != null ? record.data : template.value.content
  showResult.value = true
  ElMessage.success('文书生成成功！')
}

function downloadContent() {
  const blob = new Blob([generatedContent.value], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${template.value.name || '文书'}.txt`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.document-detail { max-width: 800px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.template-content { white-space: pre-wrap; font-size: 14px; line-height: 1.8; background: #f9f9f9; padding: 20px; border-radius: 8px; }
</style>
