<template>
  <div>
    <h2 class="page-title">融资审批</h2>
    <el-card v-loading="loading"><template #header><span>融资申请列表</span></template><el-table :data="applications" border stripe><el-table-column prop="financeId" label="编号" width="170" /><el-table-column prop="ownName" label="农户" width="90" /><el-table-column prop="money" label="申请金额" width="120" /><el-table-column prop="repayment" label="期限" width="80" /><el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.status==='待审批'?'warning':row.status==='已通过'?'success':'danger'">{{row.status}}</el-tag></template></el-table-column><el-table-column label="操作" width="160"><template #default="{row}"><el-button size="small" type="primary" :disabled="row._status !== 0" @click="approve(row)">通过</el-button><el-button size="small" type="danger" :disabled="row._status !== 0" @click="reject(row)">驳回</el-button></template></el-table-column></el-table><el-empty v-if="!loading && applications.length === 0" description="暂无融资申请" /></el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllFinancesAPI, updateFinanceStatusAPI } from '../../api/finance'

const loading = ref(false)
const applications = ref([])
const statusMap = { 0: '待审批', 1: '已通过', 2: '已驳回' }
const typeMap = { 0: 'warning', 1: 'success', 2: 'danger' }

async function loadList() {
  loading.value = true
  try {
    const res = await getAllFinancesAPI()
    if (res.code === 200) {
      applications.value = (res.data || []).map(f => ({
        financeId: f.financeId,
        ownName: f.ownName || '-',
        money: f.money ? '¥' + f.money : '-',
        repayment: (f.repayment || '-') + '月',
        status: statusMap[f.status] || '待审批',
        _status: f.status
      }))
    }
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm('确认通过该融资申请？', '提示')
    await updateFinanceStatusAPI(row.financeId, 1, '审核通过')
    ElMessage.success('已通过')
    loadList()
  } catch (e) { /* 取消或失败 */ }
}

async function reject(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', { confirmButtonText: '驳回', cancelButtonText: '取消' })
    await updateFinanceStatusAPI(row.financeId, 2, value || '审核不通过')
    ElMessage.success('已驳回')
    loadList()
  } catch (e) { /* 取消或失败 */ }
}

onMounted(loadList)
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
