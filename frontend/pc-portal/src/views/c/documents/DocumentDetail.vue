<template>
  <div class="document-detail">
    <div class="page-header">
      <el-button @click="$router.back()">← 返回</el-button>
      <h2>{{ template.name }}</h2>
      <p>类型：{{ template.category }}　|　填写信息后生成规范的 Word 文档</p>
    </div>

    <template v-if="userStore.isLoggedIn">
      <el-card v-if="fields.length" class="fill-card">
        <template #header><strong>填写文书信息</strong></template>
        <el-form label-width="140px">
          <el-form-item v-for="f in fields" :key="f.key" :label="f.key">
            <el-input v-model="f.value" type="textarea" :autosize="{ minRows: 1, maxRows: 4 }" :placeholder="`请输入${f.key}`" />
          </el-form-item>
        </el-form>
      </el-card>
      <el-alert v-else type="info" :closable="false" show-icon style="margin-top:20px" title="该模板无需填写额外信息，可直接生成。" />
      <el-button type="primary" size="large" style="margin-top:20px" :loading="loading" @click="handleGenerate">
        生成并下载 Word 文档
      </el-button>
    </template>
    <el-alert v-else type="info" show-icon :closable="false" style="margin-top:20px">请先登录后使用文书生成功能</el-alert>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { buildDocx, downloadBlob } from '@/utils/docx'
import { documentApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const template = ref({})
const fields = ref([])
const loading = ref(false)

onMounted(async () => {
  try {
    template.value = await documentApi.getTemplate(route.params.id)
    fields.value = extractPlaceholders(template.value.content || '').map(k => ({ key: k, value: '' }))
  } catch {
    ElMessage.error('加载模板失败')
  }
})

function extractPlaceholders(content) {
  const set = new Set()
  const re = /\$\{([^}]+)\}/g
  let m
  while ((m = re.exec(content)) !== null) set.add(m[1])
  return Array.from(set)
}

async function handleGenerate() {
  const data = {}
  fields.value.forEach(f => { data[f.key] = f.value })
  loading.value = true
  try {
    const record = await documentApi.generate({ userId: userStore.userInfo.userId, templateId: template.value.id, data })
    const filled = record.data != null ? record.data : template.value.content
    const blob = await buildDocx(template.value.name, filled)
    downloadBlob(blob, `${template.value.name}.docx`)
    ElMessage.success('Word 文档已生成并下载')
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.document-detail { max-width: 800px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; }
.fill-card { margin-top: 20px; }
</style>
