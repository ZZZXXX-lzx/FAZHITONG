<template>
  <div class="lawyer-hall">
    <div class="page-header">
      <h2>找律师</h2>
      <p>专业律师团队，为您提供优质法律服务</p>
    </div>

    <el-card class="search-box">
      <el-form :inline="true">
        <el-form-item>
          <el-input
            v-model="keyword"
            placeholder="搜索专长、律所"
            clearable
            style="width: 300px"
            @clear="search"
            @keyup.enter="search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="lawyer-grid" v-loading="loading">
      <el-card
        v-for="lawyer in lawyers"
        :key="lawyer.id"
        class="lawyer-card"
        shadow="hover"
      >
        <div class="lawyer-avatar">
          <el-avatar :size="64" :src="lawyer.avatar">
            {{ lawyer.nickname?.[0] }}
          </el-avatar>
        </div>
        <div class="lawyer-info">
          <div class="lawyer-name">{{ lawyer.nickname }}</div>
          <div class="lawyer-detail">
            <span class="detail-item" v-if="lawyer.licenseNo">
              <el-icon><Document /></el-icon>
              执业证号：{{ lawyer.licenseNo }}
            </span>
            <span class="detail-item" v-if="lawyer.lawFirm">
              <el-icon><OfficeBuilding /></el-icon>
              {{ lawyer.lawFirm }}
            </span>
            <span class="detail-item" v-if="lawyer.practiceYears">
              执业年限：{{ lawyer.practiceYears }}年
            </span>
          </div>
          <div class="lawyer-fields" v-if="lawyer.specialties">
            <el-tag
              v-for="field in parseSpecialties(lawyer.specialties)"
              :key="field"
              size="small"
              type="success"
              effect="plain"
            >
              {{ field }}
            </el-tag>
          </div>
          <div class="lawyer-desc" v-if="lawyer.description">
            {{ lawyer.description }}
          </div>
          <div class="lawyer-action">
            <el-button type="primary" @click="goEntrust(lawyer)">委托</el-button>
          </div>
        </div>
      </el-card>

      <el-empty v-if="!loading && lawyers.length === 0" description="暂无律师" />
    </div>

    <el-pagination
      v-if="total > 0"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="size"
      :current-page="page"
      @current-change="onPageChange"
      style="margin-top: 20px; text-align: center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, OfficeBuilding } from '@element-plus/icons-vue'
import { lawyerServiceApi } from '@/api'

const router = useRouter()

const keyword = ref('')
const lawyers = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const loading = ref(false)

async function fetchLawyers() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await lawyerServiceApi.lawyers(params)
    lawyers.value = res.list || []
    total.value = res.total || 0
  } catch {
    lawyers.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  fetchLawyers()
}

function onPageChange(p) {
  page.value = p
  fetchLawyers()
}

function goEntrust(lawyer) {
  router.push({ path: '/lawyer-service', query: { lawyerId: lawyer.id, lawyerName: lawyer.nickname } })
}

function parseSpecialties(specialties) {
  if (!specialties) return []
  if (Array.isArray(specialties)) return specialties
  return String(specialties).split(/[,，、]/).map(s => s.trim()).filter(Boolean)
}

onMounted(() => {
  fetchLawyers()
})
</script>

<style scoped>
.lawyer-hall {
  max-width: 1200px;
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
.search-box {
  margin-bottom: 24px;
}
.lawyer-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.lawyer-card {
  transition: transform 0.2s;
}
.lawyer-card:hover {
  transform: translateY(-2px);
}
.lawyer-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}
.lawyer-info {
  text-align: center;
}
.lawyer-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 12px;
}
.lawyer-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}
.detail-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.lawyer-fields {
  display: flex;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.lawyer-desc {
  font-size: 13px;
  color: #999;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.lawyer-action {
  text-align: center;
}
@media (max-width: 768px) {
  .lawyer-grid {
    grid-template-columns: 1fr;
  }
}
</style>
