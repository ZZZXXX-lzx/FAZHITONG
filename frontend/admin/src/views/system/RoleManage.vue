<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="margin:0">角色权限管理</h2>
      <el-button type="primary" @click="openCreate">新增角色</el-button>
    </div>

    <el-card>
      <el-table :data="roles" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openPermission(row)">分配权限</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该角色？" @confirm="removeRole(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" placeholder="如：法务专员" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="form.roleCode" placeholder="如：LEGAL_OFFICER（大写）" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="角色职责说明" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限 -->
    <el-dialog v-model="permVisible" :title="`分配权限 - ${currentRole?.roleName || ''}`" width="620px">
      <div v-loading="permLoading" class="perm-box">
        <el-checkbox
          v-model="checkAll"
          :indeterminate="isIndeterminate"
          @change="onCheckAll"
          style="margin-bottom:12px"
        >全选</el-checkbox>
        <el-checkbox-group v-model="checkedPerms" class="perm-group">
          <el-checkbox v-for="p in permissions" :key="p.id" :label="p.id" border class="perm-item">
            {{ p.permissionName }}
            <span class="perm-code">{{ p.permissionCode }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPerms" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, roleApi } from '@/api'

const roles = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({ id: null, roleName: '', roleCode: '', description: '', status: 1 })

const permVisible = ref(false)
const permLoading = ref(false)
const savingPerms = ref(false)
const currentRole = ref(null)
const permissions = ref([])
const checkedPerms = ref([])

const isIndeterminate = computed(() =>
  checkedPerms.value.length > 0 && checkedPerms.value.length < permissions.value.length)
const checkAll = computed(() =>
  permissions.value.length > 0 && checkedPerms.value.length === permissions.value.length)

async function fetchRoles() {
  loading.value = true
  try {
    roles.value = await userApi.roles()
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { id: null, roleName: '', roleCode: '', description: '', status: 1 }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { id: row.id, roleName: row.roleName, roleCode: row.roleCode, description: row.description, status: row.status }
  dialogVisible.value = true
}

async function saveRole() {
  if (!form.value.roleName || !form.value.roleCode) {
    ElMessage.warning('请填写角色名称与编码')
    return
  }
  saving.value = true
  try {
    if (form.value.id) await roleApi.update(form.value)
    else await roleApi.create(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchRoles()
  } catch {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

async function removeRole(row) {
  try {
    await roleApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch {
    /* 拦截器已提示 */
  }
}

async function openPermission(row) {
  currentRole.value = row
  permVisible.value = true
  permLoading.value = true
  checkedPerms.value = []
  try {
    permissions.value = await roleApi.permissions()
    const detail = await roleApi.detail(row.id)
    checkedPerms.value = detail.checked || []
  } catch {
    /* 拦截器已提示 */
  } finally {
    permLoading.value = false
  }
}

function onCheckAll(val) {
  checkedPerms.value = val ? permissions.value.map(p => p.id) : []
}

async function savePermissions() {
  savingPerms.value = true
  try {
    await roleApi.assign(currentRole.value.id, checkedPerms.value)
    ElMessage.success('权限已保存')
    permVisible.value = false
  } catch {
    /* 拦截器已提示 */
  } finally {
    savingPerms.value = false
  }
}

onMounted(fetchRoles)
</script>

<style scoped>
.perm-box { min-height: 120px; }
.perm-group { display: flex; flex-direction: column; gap: 10px; }
.perm-item { margin: 0; width: 100%; }
.perm-code { margin-left: 8px; color: #999; font-size: 12px; font-family: Consolas, Menlo, monospace; }
</style>
