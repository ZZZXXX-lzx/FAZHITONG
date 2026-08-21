<template>
  <div>
    <h2 style="margin-bottom:20px">角色权限管理</h2>
    <el-card>
      <el-table :data="roles" stripe v-loading="loading">
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="150">
          <template #default>
            <el-button link type="primary">编辑权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'

const roles = ref([])
const loading = ref(false)

async function fetchRoles() {
  loading.value = true
  try {
    roles.value = await userApi.roles()
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchRoles())
</script>
