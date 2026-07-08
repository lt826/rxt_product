<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { applyReserveAPI, getMyReservesAPI } from '../../api/reserve'

const experts = [
  { id: 1, avatar: '👨‍🏫', name: '王教授', field: '水稻种植、土壤改良' },
  { id: 2, avatar: '👩‍🔬', name: '李专家', field: '蔬菜种植、病虫害防治' },
  { id: 3, avatar: '👨‍🌾', name: '张老师', field: '农产品电商、品牌建设' }
]

const myReserves = ref([])
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  expertName: '',
  plantName: '',
  area: '',
  address: '',
  phone: '',
  message: ''
})

async function loadReserves() {
  loading.value = true
  try {
    const res = await getMyReservesAPI()
    if (res.code === 200) {
      myReserves.value = (res.data || []).map(r => ({
        id: r.id,
        expert: r.expertName,
        plantName: r.plantName,
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
  form.value.expertName = expert.name
  submitting.value = true
  try {
    const res = await applyReserveAPI({
      expertName: expert.name,
      plantName: form.value.plantName || undefined,
      area: form.value.area || undefined,
      address: form.value.address || undefined,
      phone: form.value.phone || undefined,
      message: form.value.message || undefined
    })
    if (res.code === 200) {
      ElMessage.success(`已提交预约申请：${expert.name}，等待专家确认`)
      await loadReserves()
    }
  } catch (e) {
    // 已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadReserves)
</script>

<template>
  <div>
    <h2 class="page-title">专家预约</h2>
    <el-card>
      <template #header><span>可预约的专家</span></template>
      <div v-for="expert in experts" :key="expert.id" class="expert-card">
        <div class="expert-info">
          <div class="expert-avatar">{{ expert.avatar }}</div>
          <div>
            <div class="expert-name">{{ expert.name }}</div>
            <div class="expert-field">{{ expert.field }}</div>
          </div>
        </div>
        <el-button type="primary" size="small" :loading="submitting" @click="bookExpert(expert)">预约</el-button>
      </div>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><span>我的预约记录</span></template>
      <div v-for="r in myReserves" :key="r.id" class="expert-card">
        <div class="expert-info">
          <div>
            <div class="expert-name">预约专家：{{ r.expert }}</div>
            <div class="expert-field">作物：{{ r.plantName || '-' }}</div>
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
</style>
