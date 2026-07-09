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

// 预约弹窗
const dialogVisible = ref(false)
const bookingExpert = ref(null)

const form = ref({
  expertName: '',
  address: '',
  appointmentTime: '',
  description: ''
})

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

async function loadExperts() {
  try {
    const res = await listExperts()
    if (res.code === 200) {
      experts.value = res.data || []
    }
  } catch (e) {
    // 忽略
  }
}

async function loadAppointments() {
  loading.value = true
  try {
    const res = await getFarmerAppointmentsAPI(userStore.userInfo.userName)
    if (res.code === 200) {
      appointments.value = res.data || []
    }
  } catch (e) {
    // 忽略
  } finally {
    loading.value = false
  }
}

function openBookingDialog(expert) {
  bookingExpert.value = expert
  form.value.expertName = expert.userName
  form.value.address = ''
  form.value.appointmentTime = ''
  form.value.description = ''
  dialogVisible.value = true
}

async function submitBooking() {
  // 检查用户是否登录
  if (!userStore.userInfo.userName) {
    ElMessage.warning('请先重新登录')
    return
  }
  if (!form.value.address) {
    ElMessage.warning('请输入预约地址')
    return
  }
  if (!form.value.appointmentTime) {
    ElMessage.warning('请选择预约时间')
    return
  }

  submitting.value = true
  try {
    const res = await createAppointmentAPI({
      farmerName: userStore.userInfo.userName,
      expertName: form.value.expertName,
      address: form.value.address,
      appointmentTime: form.value.appointmentTime,
      description: form.value.description
    })
    if (res.code === 200) {
      ElMessage.success('预约申请已提交，请等待专家确认')
      dialogVisible.value = false
      await loadAppointments()
    }
  } catch (e) {
    // 已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

async function handleCancel(appointment) {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', { type: 'warning' })
    const res = await cancelAppointmentAPI(appointment.id, userStore.userInfo.userName)
    if (res.code === 200) {
      ElMessage.success('已取消预约')
      await loadAppointments()
    }
  } catch (e) {
    // 取消或失败
  }
}

function getAvatar(name) {
  const avatars = ['👨‍🏫','👩‍🔬','👨‍🌾','👩‍🌾','🧑‍🔬','👨‍💼','👩‍💼','👨‍🔧','👩‍🔧','🧑‍🏫']
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = ((hash << 5) - hash) + name.charCodeAt(i)
  return avatars[Math.abs(hash) % avatars.length]
}

onMounted(async () => {
  await loadExperts()
  await loadAppointments()
})
</script>

<template>
  <div>
    <h2 class="page-title">专家预约</h2>

    <!-- 可预约的专家列表 -->
    <el-card>
      <template #header><span>可预约的专家（{{ experts.length }}人）</span></template>
      <div v-for="ex in experts" :key="ex.userName" class="expert-card">
        <div class="expert-info">
          <div class="expert-avatar">{{ getAvatar(ex.userName) }}</div>
          <div>
            <div class="expert-name">{{ ex.realName || ex.userName }}</div>
            <div class="expert-field">{{ ex.profession || '农业技术' }}</div>
            <div class="expert-affiliation">{{ ex.belong || '' }}</div>
          </div>
        </div>
        <el-button type="primary" size="small" @click="openBookingDialog(ex)">
          预约
        </el-button>
      </div>
      <el-empty v-if="!loading && experts.length === 0" description="暂无专家数据" />
    </el-card>

    <!-- 我的预约记录 -->
    <el-card style="margin-top:20px">
      <template #header><span>我的预约记录</span></template>
      <div v-loading="loading">
        <div v-for="apt in appointments" :key="apt.id" class="apt-item">
          <div class="apt-header">
            <span class="apt-expert">{{ apt.expertRealName || '专家' }}</span>
            <el-tag :type="getStatusType(apt.status)">{{ getStatusText(apt.status) }}</el-tag>
          </div>
          <div class="apt-detail">
            <span class="apt-label">📍 地址：</span>
            <span>{{ apt.address }}</span>
          </div>
          <div class="apt-detail">
            <span class="apt-label">🕐 时间：</span>
            <span>{{ apt.appointmentTime ? apt.appointmentTime.replace('T', ' ') : '-' }}</span>
          </div>
          <div v-if="apt.description" class="apt-detail">
            <span class="apt-label">📝 说明：</span>
            <span>{{ apt.description }}</span>
          </div>
          <div v-if="apt.reply" class="apt-reply">
            <span class="apt-label">💬 专家回复：</span>
            <span>{{ apt.reply }}</span>
          </div>
          <div v-if="apt.status === 'pending' || apt.status === 'approved'" class="apt-actions">
            <el-button size="small" type="danger" link @click="handleCancel(apt)">取消预约</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />
      </div>
    </el-card>

    <!-- 预约弹窗 -->
    <el-dialog v-model="dialogVisible" :title="`预约 ${bookingExpert?.realName || bookingExpert?.userName}`" width="500px">
      <el-form label-width="100px">
        <el-form-item label="预约地址" required>
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="预约时间" required>
          <el-date-picker
            v-model="form.appointmentTime"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width:100%"
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
.page-title { font-size: 20px; font-weight: 600; color: #222; margin: 0 0 20px }
.expert-card { display: flex; align-items: center; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid #ebeef5 }
.expert-card:last-child { border: none }
.expert-info { display: flex; align-items: center; gap: 12px }
.expert-avatar { font-size: 36px }
.expert-name { font-weight: 500; color: #303133 }
.expert-field { font-size: 13px; color: #909399; margin-top: 2px }
.expert-affiliation { font-size: 12px; color: #c0c4cc; margin-top: 1px }

.apt-item { padding: 16px; background: #f8f9fa; border-radius: 8px; margin-bottom: 12px }
.apt-item:last-child { margin-bottom: 0 }
.apt-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px }
.apt-expert { font-weight: 500; color: #303133 }
.apt-detail { font-size: 14px; color: #606266; margin-bottom: 6px }
.apt-label { color: #909399; margin-right: 4px }
.apt-reply { margin-top: 8px; padding: 8px 12px; background: #fff; border-radius: 4px; font-size: 14px }
.apt-reply .apt-label { color: #67c23a }
.apt-actions { margin-top: 8px }
</style>
