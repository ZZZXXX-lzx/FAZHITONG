<template>
  <div class="document-list">
    <div class="page-header">
      <h2>法律文书生成</h2>
      <p>选择文书类型，AI 智能生成专业法律文书</p>
      <el-button type="primary" @click="openDraft">AI 智能起草</el-button>
    </div>

    <el-tabs v-model="activeCategory" @tab-change="loadTemplates">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane v-for="cat in categories" :key="cat.code" :label="cat.name" :name="cat.code" />
    </el-tabs>
    <el-input v-model="keyword" placeholder="搜索文书模板" clearable class="tpl-search" @input="loadTemplates" />
    <el-row :gutter="12">
      <el-col :xs="12" :sm="6" v-for="tpl in templates" :key="tpl.id">
        <el-card class="template-card" shadow="hover" @click="$router.push(`/documents/${tpl.id}`)">
          <h3>{{ tpl.name }}</h3>
          <p class="template-type">{{ tpl.category }}</p>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="onPageChange" style="margin-top:24px;text-align:center" />

    <!-- AI 智能起草 -->
    <el-dialog v-model="showDraft" title="AI 智能起草" width="720px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="文书类型">
          <el-input v-model="draftType" placeholder="如：劳动合同、借款协议、民事起诉状..." />
        </el-form-item>
        <el-form-item label="需求描述">
          <el-input type="textarea" v-model="draftDesc" :rows="8" placeholder="请描述您的需求，例如：甲方委托乙方开发小程序，约定开发周期、费用与验收标准..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDraft = false">取消</el-button>
        <el-button type="primary" :loading="draftLoading" @click="submitDraft">生成文书</el-button>
      </template>
    </el-dialog>

    <!-- 起草结果 -->
    <el-dialog v-model="showDraftResult" title="生成结果" width="720px">
      <div class="draft-box">{{ draftResult }}</div>
      <template #footer>
        <el-button @click="copyDraft">复制文本</el-button>
        <el-button type="primary" @click="showDraftResult = false">关闭</el-button>
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

const activeCategory = ref('')
const keyword = ref('')
const categories = ref([])
const templates = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const showDraft = ref(false)
const showDraftResult = ref(false)
const draftType = ref('')
const draftDesc = ref('')
const draftLoading = ref(false)
const draftResult = ref('')

onMounted(async () => {
  const cat = route.query.category
  if (cat) activeCategory.value = String(cat)
  try {
    categories.value = await documentApi.categories()
  } catch { /* 分类加载失败不阻塞 */ }
  loadTemplates()
})

async function loadTemplates() {
  try {
    const data = await documentApi.templates({ category: activeCategory.value || undefined, keyword: keyword.value || undefined, page: page.value, size: size.value })
    templates.value = data.list
    total.value = data.total
  } catch {
    ElMessage.error('加载模板失败')
  }
}

function onPageChange(p) { page.value = p; loadTemplates() }

function openDraft() {
  draftResult.value = ''
  showDraft.value = true
}

async function submitDraft() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再使用 AI 智能起草')
    return
  }
  if (!draftDesc.value.trim()) {
    ElMessage.warning('请填写需求描述')
    return
  }
  draftLoading.value = true
  try {
    draftResult.value = await documentApi.aiDraft(draftType.value.trim() || '法律文书', draftDesc.value.trim())
    showDraft.value = false
    showDraftResult.value = true
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    draftLoading.value = false
  }
}

async function copyDraft() {
  try {
    await navigator.clipboard.writeText(draftResult.value)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本复制')
  }
}
</script>

<style scoped>
.document-list { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; margin-top: 4px; margin-bottom: 12px; }
.tpl-search { width: 300px; margin-bottom: 20px; }
.template-card { cursor: pointer; margin-bottom: 12px; }
.template-card h3 { font-size: 16px; margin-bottom: 8px; }
.template-type { color: #999; font-size: 13px; }
.draft-box {
  white-space: pre-wrap;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  line-height: 1.7;
  max-height: 440px;
  overflow-y: auto;
}
</style>
