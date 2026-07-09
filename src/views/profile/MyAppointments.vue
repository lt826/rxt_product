<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyReservesAPI, applyReserveAPI } from '../../api/reserve'
import { listExperts } from '../../api/expert'

const appointments = ref([])
const experts = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ expertName: '', plantName: '', address: '', area: '', phone: '', message: '', soilCondition: '', plantCondition: '' })
const statusMap = { 0: '申请中', 1: '已完成' }

async function loadData() {
  loading.value = true
  try {
    const [res, expRes] = await Promise.allSettled([getMyReservesAPI(), listExperts()])
    if (res.status === 'fulfilled' && res.value?.code === 200) {
      appointments.value = (res.value.data || []).map(r => ({
        id: r.id, expert: r.expertName, plant: r.plantName, address: r.address,
        status: statusMap[r.status] || '申请中', answer: r.answer || '待处理',
        createTime: r.createTime?.substring(0,10) || '-'
      }))
    }
    if (expRes.status === 'fulfilled' && expRes.value?.code === 200) {
      experts.value = (expRes.value.data || []).map(e => ({ value: e.userName, label: e.realName || e.userName }))
    }
  } catch(e) {}
  finally { loading.value = false }
}
onMounted(loadData)

async function submitReserve() {
  if (!form.value.expertName) return ElMessage.warning('请选择专家')
  try {
    const res = await applyReserveAPI(form.value)
    if (res.code === 200) {
      ElMessage.success('预约申请已提交')
      dialogVisible.value = false
      form.value = { expertName: '', plantName: '', address: '', area: '', phone: '', message: '', soilCondition: '', plantCondition: '' }
      loadData()
    }
  } catch(e) { ElMessage.error('提交失败') }
}
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">预约管理</h2>
      <el-button type="primary" @click="dialogVisible = true">预约专家</el-button>
    </div>
    <el-table :data="appointments" v-loading="loading" stripe>
      <el-table-column prop="expert" label="专家" width="120" />
      <el-table-column prop="plant" label="农作物" width="120" />
      <el-table-column prop="address" label="地址" width="150" show-overflow-tooltip />
      <el-table-column prop="answer" label="专家回复" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{row}"><el-tag :type="row.status === '已完成' ? 'success' : 'warning'">{{ row.status }}</el-tag></template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />

    <el-dialog v-model="dialogVisible" title="预约专家" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择专家"><el-select v-model="form.expertName" filterable style="width:100%"><el-option v-for="exp in experts" :key="exp.value" :label="exp.label" :value="exp.value" /></el-select></el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="农作物"><el-input v-model="form.plantName" /></el-form-item></el-col><el-col :span="12"><el-form-item label="面积(亩)"><el-input v-model="form.area" /></el-form-item></el-col></el-row>
        <el-form-item label="土地地址"><el-input v-model="form.address" /></el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="土壤条件"><el-input v-model="form.soilCondition" /></el-form-item></el-col><el-col :span="12"><el-form-item label="作物条件"><el-input v-model="form.plantCondition" /></el-form-item></el-col></el-row>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="留言"><el-input v-model="form.message" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReserve">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
