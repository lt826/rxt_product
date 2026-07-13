<script setup>
import { ref, onMounted } from 'vue'
import { getUsersAPI } from '../../api/admin'

// 管理员端：所有用户（只读查看）
const users = ref([])
const loading = ref(false)

const roleMap = { farmer: '农户', buyer: '买家', bank: '银行', expert: '专家', admin: '管理员' }

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUsersAPI()
    if (res.code === 200) {
      users.value = (res.data || []).map(u => ({
        userName: u.userName,
        nickName: u.nickName || '-',
        realName: u.realName || '-',
        phone: u.phone || '-',
        role: u.role,
        createTime: u.createTime?.substring(0, 10) || '-'
      }))
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(loadUsers)
</script>

<template>
  <div>
    <h2 class="page-title">所有用户</h2>
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="userName" label="用户名" min-width="120" />
      <el-table-column prop="nickName" label="昵称" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag>{{ roleMap[row.role] || row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="120" />
    </el-table>
    <el-empty v-if="!loading && users.length === 0" description="暂无用户" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
