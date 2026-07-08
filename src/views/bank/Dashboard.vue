<template>
  <div>
    <h2 class="page-title">银行工作台</h2>
    <div class="stats-grid" v-loading="loading">
      <div class="stat-card"><div class="stat-value" style="color:#409eff">{{ stats.totalPending }}</div><div class="stat-label">待审批申请</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#67c23a">{{ stats.todayProcessed }}</div><div class="stat-label">本日处理</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#e6a23c">{{ stats.activeProducts }}</div><div class="stat-label">活跃融资产品</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#f56c6c">{{ stats.monthlyTotal }}</div><div class="stat-label">本月放贷总额</div></div>
    </div>
    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header><span>待审批列表</span></template><el-table :data="pendingList" stripe v-loading="loading"><el-table-column prop="ownName" label="农户" width="100" /><el-table-column prop="money" label="金额" width="100" /><el-table-column prop="createTime" label="提交日期" /><el-table-column label="操作" width="140"><template #default="{row}"><el-button size="small" type="primary" @click="handleApprove(row)">审核</el-button><el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button></template></el-table-column></el-table><el-empty v-if="!loading && pendingList.length === 0" description="暂无待审批申请" /></el-card></el-col>
      <el-col :span="12"><el-card><template #header><span>今日动态</span></template><el-timeline><el-timeline-item v-for="log in logs" :key="log.time" :timestamp="log.time" :type="log.type">{{ log.text }}</el-timeline-item></el-timeline><el-empty v-if="logs.length === 0" description="暂无动态" /></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllFinancesAPI, updateFinanceStatusAPI } from '../../api/finance'

const loading = ref(false)
const stats = reactive({ totalPending: 0, todayProcessed: 0, activeProducts: 0, monthlyTotal: '¥0' })
const pendingList = ref([])
const logs = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await getAllFinancesAPI()
    if (res.code === 200) {
      const list = res.data || []
      pendingList.value = list.filter(f => f.status === 0).map(f => ({
        financeId: f.financeId,
        ownName: f.ownName || '-',
        money: f.money ? '¥' + f.money : '-',
        createTime: f.createTime ? f.createTime.substring(0, 10) : '-',
        status: f.status,
        remark: f.remark
      }))
      stats.totalPending = pendingList.value.length
      stats.todayProcessed = list.filter(f => f.status > 0 && f.updateTime?.startsWith(new Date().toISOString().substring(0,10))).length
      
      // Today's logs
      const today = new Date().toISOString().substring(0, 10)
      const todayItems = list.filter(f => f.updateTime?.startsWith(today))
      logs.value = todayItems.map(f => ({
        time: (f.updateTime || '').substring(11, 16) || '--:--',
        text: f.ownName + (f.status === 1 ? '的融资申请已通过' : f.status === 2 ? '的融资申请已驳回' : '提交了融资申请'),
        type: f.status === 0 ? 'primary' : f.status === 1 ? 'success' : 'danger'
      }))
    }
  } catch (e) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确认通过 ${row.ownName} 的融资申请？`, '提示')
    await updateFinanceStatusAPI(row.financeId, 1, '审核通过')
    ElMessage.success('已通过')
    loadData()
  } catch (e) { /* 取消或失败 */ }
}

async function handleReject(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', { confirmButtonText: '驳回', cancelButtonText: '取消' })
    await updateFinanceStatusAPI(row.financeId, 2, value || '审核不通过')
    ElMessage.success('已驳回')
    loadData()
  } catch (e) { /* 取消或失败 */ }
}

onMounted(loadData)
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.stats-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px }
.stat-card { background:#fff;border-radius:8px;padding:24px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06) }
.stat-value { font-size:28px;font-weight:700;line-height:1 }
.stat-label { margin-top:8px;font-size:14px;color:#909399 }
</style>
