<script setup>
import { ref, onMounted } from 'vue'
import { getMyFinancesAPI } from '../../api/finance'
import { ElMessage } from 'element-plus'

// 农户端：查看自己的融资申请（申请在农户主页面进行）
const records = ref([])
const loading = ref(false)

const statusMap = { 0: '审核中', 1: '已驳回', 2: '已通过' }
const statusTypeMap = { 0: 'warning', 1: 'danger', 2: 'success' }

// 拼接联合贷款人（姓名 + 电话），最多 2 人
function formatPartners(f) {
  const arr = []
  if (f.combinationName1) arr.push(`${f.combinationName1}${f.combinationPhone1 ? '（' + f.combinationPhone1 + '）' : ''}`)
  if (f.combinationName2) arr.push(`${f.combinationName2}${f.combinationPhone2 ? '（' + f.combinationPhone2 + '）' : ''}`)
  return arr.length ? arr.join('、') : '-'
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await getMyFinancesAPI()
    if (res.code === 200) {
      records.value = (res.data || []).map(f => ({
        id: f.financeId,
        amount: f.money ? `¥${f.money}` : '-',
        cropItem: f.cropItem || '-',
        status: statusMap[f.status] || '审核中', statusCode: f.status,
        date: f.createTime?.substring(0, 10) || '-', remark: f.remark || '-',
        rate: f.rate ? `${f.rate}%` : '-',
        repayment: f.repayment ? `${f.repayment}个月` : '-',
        partners: formatPartners(f)
      }))
    }
  } catch (e) { ElMessage.error('获取融资记录失败') }
  finally { loading.value = false }
}
onMounted(loadRecords)
</script>

<template>
  <div>
    <h2 class="page-title">我的融资管理</h2>
    <el-table :data="records" v-loading="loading" stripe>
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="amount" label="融资金额" width="120" />
      <el-table-column prop="cropItem" label="融资农作物" width="120" show-overflow-tooltip />
      <el-table-column prop="rate" label="年利率" width="90" />
      <el-table-column prop="repayment" label="还款期限" width="100" />
      <el-table-column prop="partners" label="联合贷款人" min-width="180" show-overflow-tooltip />
      <el-table-column prop="date" label="申请日期" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{row}"><el-tag :type="statusTypeMap[row.statusCode] || 'info'">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
    </el-table>
    <el-empty v-if="!loading && records.length === 0" description="暂无融资记录" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
