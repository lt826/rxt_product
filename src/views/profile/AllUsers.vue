<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await (await fetch('/api/v1/admin/users', { headers: { Authorization: `Bearer ${localStorage.getItem('rxt_token')}` } })).json()
    if (res.code === 200) {
      users.value = (res.data || []).map(u => ({
        userName: u.userName, nickName: u.nickName, realName: u.realName,
        phone: u.phone, role: u.role, createTime: u.createTime?.substring(0,10) || '-'
      }))
    }
  } catch(e) { ElMessage.error('获取用户列表失败') }
  finally { loading.value = false }
})
</script>

<template>
  <div>
    <h2 class="page-title">所有用户管理</h2>
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="userName" label="用户名" min-width="120" />
      <el-table-column prop="nickName" label="昵称" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{row}">
          <el-tag>{{ {farmer:'农户',buyer:'买家',bank:'银行',expert:'专家',admin:'管理员'}[row.role] || row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="110" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button size="small" text type="primary">编辑</el-button>
          <el-button size="small" text type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && users.length === 0" description="暂无用户" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
