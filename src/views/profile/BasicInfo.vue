<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const form = ref({
  userName: userStore.userInfo?.userName || '',
  nickName: userStore.userInfo?.nickName || '',
  realName: userStore.userInfo?.realName || '',
  phone: userStore.userInfo?.phone || '',
  identityNum: userStore.userInfo?.identityNum || '',
  address: userStore.userInfo?.address || ''
})

const loading = ref(false)

async function saveInfo() {
  loading.value = true
  try {
    // 更新本地存储
    const user = JSON.parse(localStorage.getItem('rxt_user') || '{}')
    Object.assign(user, form.value)
    localStorage.setItem('rxt_user', JSON.stringify(user))
    userStore.userInfo = user
    ElMessage.success('保存成功')
  } catch(e) { ElMessage.error('保存失败') }
  finally { loading.value = false }
}
</script>

<template>
  <div>
    <h2 class="page-title">基本信息管理</h2>
    <el-card shadow="never">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.userName" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.identityNum" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="saveInfo">保存信息</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
