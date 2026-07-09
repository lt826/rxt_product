<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExpertAppointmentsAPI, handleAppointmentAPI, completeAppointmentAPI } from '../../api/appointment'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const appointments = ref([])
const activeFilter = ref('pending')

// 处理弹窗
const handleVisible = ref(false)
const handleForm = ref({
  id: null,
  reply: ''
})
const submitting = ref(false)

// 状态映射
const statusMap = {
  'pending': { text: '申请中', type: 'warning' },
  'approved': { text: '已同意', type: 'success' },
  'rejected': { text: '已拒绝', type: 'danger' },
  'completed': { text: '已完成', type: 'info' },
  'cancelled': { text: '已取消', type: 'info' }
}

function getStatusText(status) {
  return statusMap[status]?.text || status
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

async function loadAppointments() {
  loading.value = true
  try {
    const res = await getExpertAppointmentsAPI(userStore.userInfo.userName)
    if (res.code === 200) {
      appointments.value = res.data || []
    }
  } catch (e) {
    // 忽略
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  if (activeFilter.value === 'all') return appointments.value
  return appointments.value.filter(a => a.status === activeFilter.value)
})

function openHandleDialog(appointment) {
  handleForm.value = {
    id: appointment.id,
    reply: ''
  }
  handleVisible.value = true
}

async function handleReply(approved) {
  submitting.value = true
  try {
    const res = await handleAppointmentAPI(
      handleForm.value.id,
      userStore.userInfo.userName,
      approved,
      handleForm.value.reply
    )
    if (res.code === 200) {
      ElMessage.success(approved ? '已同意预约' : '已拒绝预约')
      handleVisible.value = false
      await loadAppointments()
    }
  } catch (e) {
    // 已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

async function completeAppointment(appointment) {
  try {
    await ElMessageBox.confirm('确定完成该预约吗？', '提示', { type: 'success' })
    const res = await completeAppointmentAPI(appointment.id, userStore.userInfo.userName)
    if (res.code === 200) {
      ElMessage.success('预约已完成')
      await loadAppointments()
    }
  } catch (e) {
    // 取消或失败
  }
}

onMounted(loadAppointments)
</script>

<template>
  <div>
    <h2 class="page-title">预约处理</h2>

    <el-radio-group v-model="activeFilter" style="margin-bottom: 16px">
      <el-radio-button label="pending">待处理</el-radio-button>
      <el-radio-button label="approved">已同意</el-radio-button>
      <el-radio-button label="completed">已完成</el-radio-button>
      <el-radio-button label="all">全部</el-radio-button>
    </el-radio-group>

    <el-card v-loading="loading">
      <template #header><span>预约请求</span></template>
      <el-empty v-if="!loading && filtered.length === 0" description="暂无预约" />

      <div v-for="apt in filtered" :key="apt.id" class="apt-card">
        <div class="apt-main">
          <div class="apt-farmer">
            <el-icon><User /></el-icon>
            {{ apt.farmerName }}
          </div>
          <div class="apt-line">📍 {{ apt.address }}</div>
          <div class="apt-line">🕐 {{ apt.appointmentTime ? apt.appointmentTime.replace('T', ' ') : '-' }}</div>
          <div v-if="apt.description" class="apt-line muted">📝 说明：{{ apt.description }}</div>
        </div>
        <div class="apt-side">
          <el-tag :type="getStatusType(apt.status)">{{ getStatusText(apt.status) }}</el-tag>
          <div v-if="apt.status === 'pending'" class="apt-actions">
            <el-button size="small" type="primary" @click="openHandleDialog(apt)">处理</el-button>
          </div>
          <div v-else-if="apt.status === 'approved'" class="apt-actions">
            <el-button size="small" type="success" @click="completeAppointment(apt)">完成</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 处理预约弹窗 -->
    <el-dialog v-model="handleVisible" title="处理预约" width="500px">
      <el-form label-width="80px">
        <el-form-item label="回复">
          <el-input
            v-model="handleForm.reply"
            type="textarea"
            :rows="4"
            :placeholder="handleForm.reply || '请输入回复内容（拒绝时必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleReply(false)">拒绝</el-button>
        <el-button type="primary" :loading="submitting" @click="handleReply(true)">同意</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #222; margin: 0 0 20px }
.apt-card { display: flex; gap: 16px; padding: 16px 0; border-bottom: 1px solid #ebeef5; align-items: flex-start }
.apt-card:last-child { border: none }
.apt-main { flex: 1 }
.apt-farmer { display: flex; align-items: center; gap: 6px; font-weight: 500; color: #303133; margin-bottom: 8px }
.apt-line { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; font-size: 14px; color: #606266 }
.muted { color: #909399; font-size: 13px }
.apt-side { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; flex-shrink: 0 }
.apt-actions { display: flex; gap: 8px }
</style>
