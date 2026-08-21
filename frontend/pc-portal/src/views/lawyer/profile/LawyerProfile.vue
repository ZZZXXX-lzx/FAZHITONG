<template>
  <div class="lawyer-profile">
    <div class="page-header">
      <h2>律师资料</h2>
      <p>完善资料，获取更多案源</p>
    </div>
    <el-card style="max-width:600px">
      <el-form :model="profile" label-width="120px">
        <el-form-item label="姓名">
          <el-input v-model="profile.name" />
        </el-form-item>
        <el-form-item label="执业证号">
          <el-input v-model="profile.certNo" />
        </el-form-item>
        <el-form-item label="所属律所">
          <el-input v-model="profile.lawFirm" />
        </el-form-item>
        <el-form-item label="专业领域">
          <el-checkbox-group v-model="profile.specialties">
            <el-checkbox label="合同纠纷" />
            <el-checkbox label="劳动纠纷" />
            <el-checkbox label="知识产权" />
            <el-checkbox label="公司法务" />
            <el-checkbox label="刑事辩护" />
            <el-checkbox label="房产纠纷" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="执业年限">
          <el-input-number v-model="profile.years" :min="0" :max="50" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input type="textarea" v-model="profile.bio" :rows="4" placeholder="请描述您的专业背景和经验" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存资料</el-button>
          <el-button v-if="!verified" type="warning">提交认证</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const verified = ref(false)
const profile = reactive({
  name: '',
  certNo: '',
  lawFirm: '',
  specialties: [],
  years: 0,
  bio: '',
})

function save() {
  ElMessage.success('资料保存成功')
}
</script>

<style scoped>
.lawyer-profile { max-width: 1200px; margin: 0 auto; padding: 32px 20px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; }
.page-header p { color: #666; }
</style>
