<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const form = ref({
  realName: '', specialty: '', org: '', title: '', phone: '', introduction: ''
})

onMounted(() => {
  const info = userStore.userInfo || {}
  form.value.realName = info.realName || ''
  form.value.phone = info.phone || ''
})

function saveInfo() {
  ElMessage.success('专家信息保存成功')
}
</script>

<template>
  <div>
    <h2 class="page-title">专家信息管理</h2>
    <el-card shadow="never" style="max-width:700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="专业领域"><el-input v-model="form.specialty" placeholder="如：水稻、植物学" /></el-form-item>
        <el-form-item label="所属单位"><el-input v-model="form.org" placeholder="如：山东农科院" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="form.title" placeholder="如：高级工程师" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="form.introduction" type="textarea" :rows="4" /></el-form-item>
        <el-form-item><el-button type="primary" @click="saveInfo">保存信息</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
