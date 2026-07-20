<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAppointmentAPI, getFarmerAppointmentsAPI, cancelAppointmentAPI } from '../../api/appointment'
import { listExperts } from '../../api/expert'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const experts = ref([])
const appointments = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const bookingExpert = ref(null)

const form = ref({ expertName: '', address: '', appointmentTime: '', description: '' })

const statusMap = {
  pending: { text: '申请中', type: 'warning' },
  approved: { text: '已同意', type: 'success' },
  rejected: { text: '已拒绝', type: 'danger' },
  completed: { text: '已完成', type: 'info' },
  cancelled: { text: '已取消', type: 'info' }
}

function getStatusText(status) { return statusMap[status]?.text || status }
function getStatusType(status) { return statusMap[status]?.type || 'info' }

async function loadExperts() {
  try { const res = await listExperts(); if (res.code === 200) experts.value = res.data || [] } catch { /* ignore */ }
}

async function loadAppointments() {
  loading.value = true
  try {
    const res = await getFarmerAppointmentsAPI(userStore.userInfo.userName)
    if (res.code === 200) appointments.value = res.data || []
  } catch { /* ignore */ } finally { loading.value = false }
}

function openBookingDialog(expert) {
  bookingExpert.value = expert
  form.value = { expertName: expert.userName, address: '', appointmentTime: '', description: '' }
  dialogVisible.value = true
}

async function submitBooking() {
  if (!userStore.userInfo.userName) return ElMessage.warning('请先重新登录')
  if (!form.value.address) return ElMessage.warning('请输入预约地址')
  if (!form.value.appointmentTime) return ElMessage.warning('请选择预约时间')
  submitting.value = true
  try {
    const res = await createAppointmentAPI({
      farmerName: userStore.userInfo.userName, expertName: form.value.expertName,
      address: form.value.address, appointmentTime: form.value.appointmentTime,
      description: form.value.description
    })
    if (res.code === 200) {
      ElMessage.success('预约申请已提交')
      dialogVisible.value = false
      await loadAppointments()
    }
  } catch { /* handled in interceptor */ } finally { submitting.value = false }
}

async function handleCancel(apt) {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', { type: 'warning' })
    const res = await cancelAppointmentAPI(apt.id, userStore.userInfo.userName)
    if (res.code === 200) { ElMessage.success('已取消预约'); await loadAppointments() }
  } catch { /* cancelled */ }
}

function getAvatar(name) {
  return String(name || '专').trim().charAt(0).toUpperCase()
}

onMounted(async () => { await loadExperts(); await loadAppointments() })
</script>

<template>
  <div class="appointments-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">专家预约</h2>
        <p class="page-sub">预约线下专家咨询，获取面对面指导</p>
      </div>
    </div>

    <!-- Experts list -->
    <el-card shadow="never" class="experts-card">
      <template #header>
        <div class="card-header-row">
          <strong>可预约专家（{{ experts.length }}人）</strong>
        </div>
      </template>
      <div class="experts-list">
        <div v-for="ex in experts" :key="ex.userName" class="expert-item">
          <div class="expert-item-avatar">{{ getAvatar(ex.userName) }}</div>
          <div class="expert-item-info">
            <div class="expert-item-name">{{ ex.realName || ex.userName }}</div>
            <div class="expert-item-detail">{{ ex.profession || '农业技术' }}{{ ex.belong ? ' · ' + ex.belong : '' }}</div>
          </div>
          <el-button type="primary" size="small" @click="openBookingDialog(ex)">预约</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && experts.length === 0" description="暂无专家数据" />
    </el-card>

    <!-- Appointment records -->
    <el-card shadow="never" style="margin-top:20px">
      <template #header>
        <div class="card-header-row">
          <strong>我的预约记录</strong>
          <el-button text size="small" @click="loadAppointments">刷新</el-button>
        </div>
      </template>
      <div v-loading="loading" class="apt-list">
        <div v-for="apt in appointments" :key="apt.id" class="apt-item">
          <div class="apt-header">
            <span class="apt-expert">{{ apt.expertRealName || '专家' }}</span>
            <el-tag :type="getStatusType(apt.status)" size="small">{{ getStatusText(apt.status) }}</el-tag>
          </div>
          <div class="apt-detail"><el-icon><Location /></el-icon><span>{{ apt.address }}</span></div>
          <div class="apt-detail"><el-icon><Clock /></el-icon><span>{{ apt.appointmentTime ? apt.appointmentTime.replace('T', ' ') : '-' }}</span></div>
          <div v-if="apt.description" class="apt-detail"><el-icon><Document /></el-icon><span>{{ apt.description }}</span></div>
          <div v-if="apt.reply" class="apt-reply"><el-icon><ChatDotRound /></el-icon><span>{{ apt.reply }}</span></div>
          <div v-if="apt.status === 'pending' || apt.status === 'approved'" class="apt-actions">
            <el-button size="small" type="danger" link @click="handleCancel(apt)">取消预约</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />
    </el-card>

    <!-- Booking dialog -->
    <el-dialog v-model="dialogVisible" :title="`预约 ${bookingExpert?.realName || bookingExpert?.userName}`" width="500px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="预约地址" required>
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="预约时间" required>
          <el-date-picker
            v-model="form.appointmentTime" type="datetime" placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%"
          />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请简要说明预约事项" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBooking">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.appointments-page { max-width: 1200px; margin: 0 auto; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }

.experts-list { display: flex; flex-direction: column; gap: 12px; }
.expert-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  background: var(--bg);
  border-radius: var(--radius-sm);
  transition: background 0.2s;
}
.expert-item:hover { background: var(--green-mist); }
.expert-item-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--green), var(--green-light));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  flex-shrink: 0;
}
.expert-item-name { font-weight: 600; color: var(--text); font-size: 15px; }
.expert-item-detail { font-size: 13px; color: var(--text-muted); margin-top: 2px; }

.apt-list { display: flex; flex-direction: column; gap: 12px; }
.apt-item {
  padding: 16px;
  background: var(--bg);
  border-radius: var(--radius-sm);
}
.apt-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.apt-expert { font-weight: 600; color: var(--text); }
.apt-detail {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.apt-detail .el-icon { color: var(--green); font-size: 15px; flex-shrink: 0; }
.apt-reply {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 8px;
  padding: 10px 14px;
  background: var(--green-mist);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--green);
}
.apt-reply .el-icon { font-size: 15px; flex-shrink: 0; margin-top: 2px; }
.apt-actions { margin-top: 8px; }
</style>
