<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePasswordAPI } from '../../api/user'

const form = ref({
  oldPassword: '', newPassword: '', confirmPassword: ''
})
const loading = ref(false)

async function savePassword() {
  if (!form.value.oldPassword) return ElMessage.warning('请输入原密码')
  if (!form.value.newPassword) return ElMessage.warning('请输入新密码')
  if (form.value.newPassword !== form.value.confirmPassword) return ElMessage.warning('两次密码不一致')
  if (form.value.newPassword.length < 6) return ElMessage.warning('密码长度不少于6位')
  if (form.value.oldPassword === form.value.newPassword) return ElMessage.warning('新密码不能与原密码相同')

  loading.value = true
  try {
    const res = await changePasswordAPI({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    })
    if (res && res.code === 200) {
      ElMessage.success(res.message || '密码修改成功')
      form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      ElMessage.error(res?.message || '密码修改失败')
    }
  } catch (e) {
    // 错误提示（如“原密码不正确”）已由响应拦截器统一弹出
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h2 class="page-title">密码管理</h2>
    <el-card shadow="never" style="max-width:500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码（不少于6位）" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="savePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
