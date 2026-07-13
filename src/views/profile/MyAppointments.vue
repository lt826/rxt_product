<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getExpertAppointmentsAPI } from '../../api/appointment'

// 专家端：查看全部预约处理（同意/拒绝/完成等在专家主页面进行）
const userStore = useUserStore()
const appointments = ref([])
const loading = ref(false)

const statusMap = { pending: '待处理', approved: '已同意', rejected: '已拒绝', completed: '已完成', cancelled: '已取消' }
const statusTypeMap = { pending: 'warning', approved: 'success', rejected: 'danger', completed: 'success', cancelled: 'info' }

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').substring(0, 16) : '-'
}

async function loadData() {
  loading.value = true
  try {
    const expertName = userStore.userInfo?.userName
    if (!expertName) return
    const res = await getExpertAppointmentsAPI(expertName)
    if (res && res.code === 200) {
      appointments.value = (res.data || []).map(a => ({
        id: a.id,
        farmer: a.farmerName || '-',
        address: a.address || '-',
        time: fmtTime(a.appointmentTime),
        description: a.description || '-',
        reply: a.reply || '-',
        status: a.status
      }))
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(loadData)
</script>

<template>
  <div>
    <h2 class="page-title">预约管理</h2>
    <el-table :data="appointments" v-loading="loading" stripe>
      <el-table-column prop="farmer" label="预约农户" width="120" />
      <el-table-column prop="address" label="预约地址" width="160" show-overflow-tooltip />
      <el-table-column prop="time" label="预约时间" width="150" />
      <el-table-column prop="description" label="预约说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="reply" label="处理回复" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTypeMap[row.status] || 'info'">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
