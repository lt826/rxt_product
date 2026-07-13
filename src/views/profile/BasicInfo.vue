<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getUserInfoAPI, updateUserInfoAPI } from '../../api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const form = ref({
  userName: userStore.userInfo?.userName || '',
  nickName: '',
  realName: '',
  phone: '',
  identityNum: '',
  address: ''
})

const loading = ref(false)
const saving = ref(false)

// 拉取数据库中的真实资料回填（登录返回的 LoginVO 不含 realName/phone/address/identityNum）
async function loadInfo() {
  const userName = userStore.userInfo?.userName
  if (!userName) return
  loading.value = true
  try {
    const res = await getUserInfoAPI(userName)
    if (res && res.code === 200 && res.data) {
      const u = res.data
      form.value = {
        userName: u.userName || userName,
        nickName: u.nickName || '',
        realName: u.realName || '',
        phone: u.phone || '',
        identityNum: u.identityNum || '',
        address: u.address || ''
      }
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(loadInfo)

async function saveInfo() {
  if (!form.value.userName) return ElMessage.warning('缺少用户名')
  saving.value = true
  try {
    const res = await updateUserInfoAPI({ ...form.value })
    if (res && res.code === 200) {
      // 同步本地登录态中的昵称（顶部导航栏展示用）
      const user = JSON.parse(localStorage.getItem('rxt_user') || '{}')
      if (form.value.nickName) user.nickName = form.value.nickName
      localStorage.setItem('rxt_user', JSON.stringify(user))
      userStore.userInfo = { ...userStore.userInfo, ...user }
      ElMessage.success(res.message || '保存成功')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { saving.value = false }
}
</script>

<template>
  <div v-loading="loading">
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
          <el-button type="primary" :loading="saving" @click="saveInfo">保存信息</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
