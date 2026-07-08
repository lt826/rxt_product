<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { applyReserveAPI, getMyReservesAPI } from '../../api/reserve'
import { listExperts } from '../../api/expert'

const experts = ref([])
const myReserves = ref([])
const loading = ref(false)
const submitting = ref(false)
const bookingExpert = ref(null)

const form = ref({
  plantName: '',
  area: '',
  address: '',
  phone: '',
  message: '',
  soilCondition: '',
  plantCondition: ''
})

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

async function loadReserves() {
  loading.value = true
  try {
    const res = await getMyReservesAPI()
    if (res.code === 200) {
      myReserves.value = (res.data || []).map(r => ({
        id: r.id,
        expert: r.expertName,
        plantName: r.plantName || '-',
        status: r.status === 0 || r.status === '0' ? '待确认' : '已完成',
        date: '-'
      }))
    }
  } catch (e) {
    // 忽略
  } finally {
    loading.value = false
  }
}

async function bookExpert(expert) {
  bookingExpert.value = expert
  submitting.value = true
  try {
    const res = await applyReserveAPI({
      expertName: expert.userName,
      plantName: form.value.plantName || undefined,
      area: form.value.area || undefined,
      address: form.value.address || undefined,
      phone: form.value.phone || undefined,
      message: form.value.message || undefined,
      soilCondition: form.value.soilCondition || undefined,
      plantCondition: form.value.plantCondition || undefined
    })
    if (res.code === 200) {
      ElMessage.success(`已提交预约申请：${expert.realName}，等待专家确认`)
      form.value = { plantName: '', area: '', address: '', phone: '', message: '', soilCondition: '', plantCondition: '' }
      bookingExpert.value = null
      await loadReserves()
    }
  } catch (e) {
    // 已在拦截器中处理
  } finally {
    submitting.value = false
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
  await loadReserves()
})
</script>

<template>
  <div>
    <h2 class="page-title">专家预约</h2>

    <el-card>
      <template #header><span>可预约的专家（{{ experts.length }}人）</span></template>
      <div v-for="ex in experts" :key="ex.userName" class="expert-card">
        <div class="expert-info">
          <div class="expert-avatar">{{ getAvatar(ex.userName) }}</div>
          <div>
            <div class="expert-name">{{ ex.realName }}</div>
            <div class="expert-field">{{ ex.profession || '农业技术' }}</div>
            <div class="expert-affiliation">{{ ex.belong || '' }}</div>
          </div>
        </div>
        <el-button type="primary" size="small" :loading="submitting && bookingExpert?.userName === ex.userName" @click="bookExpert(ex)">
          预约
        </el-button>
      </div>
      <el-empty v-if="!loading && experts.length === 0" description="暂无专家数据" />
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><span>我的预约记录</span></template>
      <div v-for="r in myReserves" :key="r.id" class="expert-card">
        <div class="expert-info">
          <div>
            <div class="expert-name">预约专家：{{ r.expert }}</div>
            <div class="expert-field">作物：{{ r.plantName }}</div>
          </div>
        </div>
        <el-tag :type="r.status === '待确认' ? 'warning' : 'success'">{{ r.status }}</el-tag>
      </div>
      <el-empty v-if="!loading && myReserves.length === 0" description="暂无预约记录" />
    </el-card>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.expert-card { display: flex; align-items: center; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.expert-card:last-child { border: none; }
.expert-info { display: flex; align-items: center; gap: 12px; }
.expert-avatar { font-size: 36px; }
.expert-name { font-weight: 500; color: #303133; }
.expert-field { font-size: 13px; color: #909399; margin-top: 2px; }
.expert-affiliation { font-size: 12px; color: #c0c4cc; margin-top: 1px; }
</style>
