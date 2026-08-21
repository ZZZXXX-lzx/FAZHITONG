<template>
  <div class="template-list">
    <div class="page-header">
      <h2>合同模板</h2>
      <p>海量常用合同模板，一键套用</p>
    </div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in templateCategories" :key="item.name">
        <el-card class="template-cat-card" shadow="hover" @click="$router.push('/documents')">
          <div class="cat-icon">{{ item.icon }}</div>
          <h3>{{ item.name }}</h3>
          <p>{{ item.count }}个模板</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { documentApi } from '@/api'

const templateCategories = ref([
  { name: '买卖合同', icon: '🤝', count: 0, category: 'CONTRACT' },
  { name: '租赁合同', icon: '🏠', count: 0, category: 'CONTRACT' },
  { name: '劳动合同', icon: '👔', count: 0, category: 'CONTRACT' },
  { name: '服务合同', icon: '📋', count: 0, category: 'CONTRACT' },
  { name: '借款合同', icon: '💰', count: 0, category: 'CONTRACT' },
  { name: '股权合同', icon: '📊', count: 0, category: 'CONTRACT' },
  { name: '知识产权', icon: '📖', count: 0, category: 'CONTRACT' },
  { name: '合伙协议', icon: '🤝', count: 0, category: 'CONTRACT' },
])

onMounted(async () => {
  try {
    const data = await documentApi.templates({ page: 1, size: 1 })
    templateCategories.value = templateCategories.value.map(c => ({ ...c, count: data.total || 6 }))
  } catch {
    templateCategories.value = templateCategories.value.map(c => ({ ...c, count: 6 }))
  }
})
</script>

<style scoped>
.template-list { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
.template-cat-card { text-align: center; padding: 24px 16px; cursor: pointer; margin-bottom: 20px; }
.cat-icon { font-size: 40px; margin-bottom: 12px; }
.template-cat-card h3 { font-size: 16px; margin-bottom: 4px; }
.template-cat-card p { color: #999; font-size: 13px; }
</style>
