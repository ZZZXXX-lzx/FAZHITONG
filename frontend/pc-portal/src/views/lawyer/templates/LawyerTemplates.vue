<template>
  <div class="lawyer-templates">
    <div class="page-header">
      <h2>AI 文书生成</h2>
      <p>AI智能生成各类法律文书，提升办案效率</p>
    </div>
    <el-row :gutter="24">
      <el-col :span="8" v-for="cat in lawyerDocTypes" :key="cat.name">
        <el-card class="doc-type-card" shadow="hover" @click="showGenerate(cat)">
          <div class="doc-icon">{{ cat.icon }}</div>
          <h3>{{ cat.name }}</h3>
          <p>{{ cat.desc }}</p>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="genDialog" title="AI生成文书" width="700px">
      <el-form :model="genForm">
        <el-form-item label="文书类型"><el-input :model-value="selectedType?.name" disabled /></el-form-item>
        <el-form-item v-for="field in selectedType?.fields || []" :key="field" :label="field">
          <el-input v-model="genForm[field]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialog = false">取消</el-button>
        <el-button type="primary" :loading="genLoading" @click="handleGenerate">AI生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { buildDocx, downloadBlob } from '@/utils/docx'

const genDialog = ref(false)
const genLoading = ref(false)
const selectedType = ref(null)
const genForm = reactive({})

const lawyerDocTypes = [
  { name: '起诉状', icon: '📄', desc: '民事起诉状、刑事自诉状', fields: ['原告', '被告', '诉讼请求', '事实与理由'] },
  { name: '答辩状', icon: '📝', desc: '民事答辩状、仲裁答辩书', fields: ['答辩人', '被答辩人', '答辩事项', '答辩理由'] },
  { name: '上诉状', icon: '⬆️', desc: '民事上诉状、刑事上诉状', fields: ['上诉人', '被上诉人', '上诉请求', '上诉理由'] },
  { name: '律师函', icon: '✉️', desc: '律师函、催告函', fields: ['致送单位', '委托事项', '事实说明', '法律依据'] },
  { name: '代理词', icon: '🎯', desc: '代理词、辩护词', fields: ['案件名称', '当事人', '代理意见', '法律依据'] },
  { name: '申请书', icon: '📋', desc: '执行申请、保全申请', fields: ['申请人', '被申请人', '申请事项', '事实理由'] },
]

function showGenerate(cat) {
  selectedType.value = cat
  cat.fields.forEach(f => { genForm[f] = '' })
  genDialog.value = true
}

async function handleGenerate() {
  genLoading.value = true
  try {
    const lines = (selectedType.value.fields || []).map(f => `${f}：${genForm[f] || ''}`)
    const blob = await buildDocx(selectedType.value.name, lines.join('\n'))
    downloadBlob(blob, `${selectedType.value.name}.docx`)
    ElMessage.success('文书已生成并下载')
    genDialog.value = false
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    genLoading.value = false
  }
}
</script>

<style scoped>
.lawyer-templates { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.doc-type-card { text-align: center; padding: 24px; cursor: pointer; margin-bottom: 20px; }
.doc-icon { font-size: 40px; margin-bottom: 12px; }
.doc-type-card h3 { font-size: 16px; margin-bottom: 4px; }
.doc-type-card p { color: #999; font-size: 13px; }
</style>
