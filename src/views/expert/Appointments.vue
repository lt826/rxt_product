<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExpertAppointmentsAPI, handleAppointmentAPI, completeAppointmentAPI } from '../../api/appointment'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const appointments = ref([])
const activeFilter = ref('pending')
const handleVisible = ref(false)
const handleForm = ref({ id: null, reply: '' })
const submitting = ref(false)

const statusMap = {
  pending: { text: '申请中', type: 'warning' },
  approved: { text: '已同意', type: 'success' },
  rejected: { text: '已拒绝', type: 'danger' },
  completed: { text: '已完成', type: 'info' },
  cancelled: { text: '已取消', type: 'info' }
}

function getStatusText(s) { return statusMap[s]?.text || s }
function getStatusType(s) { return statusMap[s]?.type || 'info' }

async function loadAppointments() {
  loading.value = true
  try {
    const res = await getExpertAppointmentsAPI(userStore.userInfo.userName)
    if (res.code === 200) appointments.value = res.data || []
  } catch { /* ignore */ } finally { loading.value = false }
}

const filtered = computed(() => {
  if (activeFilter.value === 'all') return appointments.value
  return appointments.value.filter(a => a.status === activeFilter.value)
})

function openHandleDialog(apt) {
  handleForm.value = { id: apt.id, reply: '' }
  handleVisible.value = true
}

async function handleReply(approved) {
  submitting.value = true
  try {
    const res = await handleAppointmentAPI(handleForm.value.id, userStore.userInfo.userName, approved, handleForm.value.reply)
    if (res.code === 200) {
      ElMessage.success(approved ? '已同意预约' : '已拒绝预约')
      handleVisible.value = false
      await loadAppointments()
    }
  } catch { /* handled in interceptor */ } finally { submitting.value = false }
}

async function completeAppointment(apt) {
  try {
    await ElMessageBox.confirm('确定完成该预约吗？', '提示', { type: 'success' })
    const res = await completeAppointmentAPI(apt.id, userStore.userInfo.userName)
    if (res.code === 200) { ElMessage.success('预约已完成'); await loadAppointments() }
  } catch { /* cancelled */ }
}

onMounted(loadAppointments)
</script>

<template>
  <div class="appointments-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">预约处理</h2>
        <p class="page-sub">管理农户预约请求</p>
      </div>
    </div>

    <el-radio-group v-model="activeFilter" class="filter-group" style="margin-bottom:20px">
      <el-radio-button value="pending">待处理</el-radio-button>
      <el-radio-button value="approved">已同意</el-radio-button>
      <el-radio-button value="completed">已完成</el-radio-button>
      <el-radio-button value="all">全部</el-radio-button>
    </el-radio-group>

    <el-card shadow="never" v-loading="loading">
      <div v-if="!filtered.length" style="text-align:center;padding:40px 0">
        <el-empty description="暂无预约" />
      </div>
      <div v-for="apt in filtered" :key="apt.id" class="apt-card">
        <div class="apt-main">
          <div class="apt-farmer">
            <el-icon><User /></el-icon> {{ apt.farmerName }}
          </div>
          <div class="apt-line"><el-icon><Location /></el-icon><span>{{ apt.address }}</span></div>
          <div class="apt-line"><el-icon><Clock /></el-icon><span>{{ apt.appointmentTime ? apt.appointmentTime.replace('T', ' ') : '-' }}</span></div>
          <div v-if="apt.description" class="apt-line muted"><el-icon><Document /></el-icon><span>{{ apt.description }}</span></div>
        </div>
        <div class="apt-side">
          <el-tag :type="getStatusType(apt.status)" size="small">{{ getStatusText(apt.status) }}</el-tag>
          <div v-if="apt.status === 'pending'" class="apt-actions">
            <el-button size="small" type="primary" @click="openHandleDialog(apt)">处理</el-button>
          </div>
          <div v-else-if="apt.status === 'approved'" class="apt-actions">
            <el-button size="small" type="success" @click="completeAppointment(apt)">完成</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="handleVisible" title="处理预约" width="500px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="回复">
          <el-input v-model="handleForm.reply" type="textarea" :rows="4" placeholder="请输入回复内容（拒绝时必填）" />
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
.appointments-page { max-width: 1000px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.filter-group { display: flex; gap: 0; }
.apt-card { display: flex; gap: 16px; padding: 16px 0; border-bottom: 1px solid var(--border); align-items: flex-start; }
.apt-card:last-child { border: none; }
.apt-main { flex: 1; }
.apt-farmer { display: flex; align-items: center; gap: 6px; font-weight: 600; color: var(--text); margin-bottom: 8px; }
.apt-line { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; font-size: 14px; color: var(--text-secondary); }
.apt-line .el-icon { color: var(--green); font-size: 15px; flex-shrink: 0; }
.apt-line.muted .el-icon { color: var(--text-muted); }
.muted { color: var(--text-muted); font-size: 13px; }
.apt-side { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; flex-shrink: 0; }
.apt-actions { display: flex; gap: 8px; }
</style>
