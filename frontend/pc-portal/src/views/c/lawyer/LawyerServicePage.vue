<template>
  <div class="lawyer-service-page">
    <div class="page-header">
      <h2>律师委托</h2>
      <p>填写委托信息，专业律师将为您提供服务</p>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.prevent
      >
        <el-form-item label="选择律师" prop="lawyerId">
          <el-select
            v-model="form.lawyerId"
            placeholder="请选择律师"
            filterable
            style="width: 100%"
            @change="onLawyerChange"
          >
            <el-option
              v-for="lawyer in lawyers"
              :key="lawyer.id"
              :label="`${lawyer.nickname}${lawyer.lawFirm ? ' - ' + lawyer.lawFirm : ''}`"
              :value="lawyer.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="服务类型" prop="serviceType">
          <el-select v-model="form.serviceType" placeholder="请选择服务类型" style="width: 100%">
            <el-option label="法律咨询" value="CONSULT" />
            <el-option label="诉讼代理" value="LITIGATE" />
            <el-option label="案件审查" value="REVIEW" />
            <el-option label="文书代拟" value="DRAFT" />
            <el-option label="其他服务" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="委托标题" prop="title">
          <el-input v-model="form.title" placeholder="请简要描述委托事项" />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您的需求、案件背景、期望目标等"
          />
        </el-form-item>

        <el-form-item label="预算" prop="budget">
          <el-input-number
            v-model="form.budget"
            :min="0"
            :precision="2"
            placeholder="元"
            style="width: 200px"
          />
          <span class="form-tip">元</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交委托
          </el-button>
          <el-button @click="$router.push('/lawyers')">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { lawyerServiceApi } from '@/api'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const submitting = ref(false)
const lawyers = ref([])

const form = reactive({
  lawyerId: '',
  serviceType: '',
  title: '',
  description: '',
  budget: null,
})

const rules = {
  lawyerId: [{ required: true, message: '请选择律师', trigger: 'change' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入委托标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }],
}

async function fetchLawyers() {
  try {
    const res = await lawyerServiceApi.lawyers({ page: 1, size: 100 })
    lawyers.value = res.list || []
    // If lawyerId from query, auto-select
    if (route.query.lawyerId) {
      form.lawyerId = Number(route.query.lawyerId)
    }
  } catch {
    lawyers.value = []
  }
}

function onLawyerChange() {
  // placeholder for potential future logic
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
    await lawyerServiceApi.create({ ...form })
    ElMessage.success('委托提交成功！律师将尽快与您联系')
    router.push('/lawyers')
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchLawyers()
})
</script>

<style scoped>
.lawyer-service-page {
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
  max-width: 640px;
}
.form-tip {
  margin-left: 8px;
  color: #999;
  font-size: 13px;
}
</style>
