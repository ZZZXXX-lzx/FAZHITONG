<template>
  <div class="contract-manage">
    <div class="page-header">
      <h2>合同管理</h2>
      <p>合同全生命周期管理 · 智能审查</p>
    </div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <strong>合同列表</strong>
          <div>
            <el-button type="primary" @click="showUpload = true">上传合同审查</el-button>
            <el-button @click="showCreate = true">新建合同</el-button>
          </div>
        </div>
      </template>
      <el-table :data="contracts" stripe>
        <el-table-column prop="title" label="合同名称" />
        <el-table-column prop="contractNo" label="编号" width="160" />
        <el-table-column prop="partyA" label="甲方" width="150" />
        <el-table-column prop="partyB" label="乙方" width="150" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SIGNED' ? 'success' : row.status === 'EXPIRED' ? 'danger' : 'info'">
              {{ row.status === 'DRAFT' ? '草稿' : row.status === 'SIGNED' ? '已签署' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="showUpload" title="上传合同审查" width="500px">
      <el-form>
        <el-form-item label="合同名称"><el-input v-model="uploadForm.title" /></el-form-item>
        <el-form-item label="上传文件">
          <el-upload drag>
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
            <template #tip><div class="el-upload__tip">支持PDF、Word格式</div></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" @click="submitUpload">提交审查</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="showCreate" title="新建合同" width="600px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="合同名称"><el-input v-model="createForm.title" /></el-form-item>
        <el-form-item label="甲方"><el-input v-model="createForm.partyA" /></el-form-item>
        <el-form-item label="乙方"><el-input v-model="createForm.partyB" /></el-form-item>
        <el-form-item label="合同金额"><el-input v-model="createForm.amount" /></el-form-item>
        <el-form-item label="签署日期"><el-date-picker v-model="createForm.signDate" type="date" /></el-form-item>
        <el-form-item label="到期日期"><el-date-picker v-model="createForm.expireDate" type="date" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { contractApi } from '@/api'

const contracts = ref([])
const showUpload = ref(false)
const showCreate = ref(false)
const uploadForm = reactive({ title: '' })
const createForm = reactive({ title: '', partyA: '', partyB: '', amount: '', signDate: '', expireDate: '' })

onMounted(async () => {
  const data = await contractApi.enterpriseList({ enterpriseId: 1, page: 1, size: 20 })
  contracts.value = data.list || []
})

function submitUpload() {
  ElMessage.success('合同已提交审查')
  showUpload.value = false
}

function submitCreate() {
  ElMessage.success('合同创建成功')
  showCreate.value = false
}
</script>

<style scoped>
.contract-manage { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
</style>
