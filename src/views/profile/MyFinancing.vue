<script setup>
import { ref, onMounted } from 'vue'
import { applyFinanceAPI, getMyFinancesAPI } from '../../api/finance'
import { ElMessage } from 'element-plus'

const records = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

const form = ref({ money: null, repayment: null, fileInfo: '' })

const statusMap = { 0: '审核中', 1: '已驳回', 2: '已通过' }
const statusTypeMap = { 0: 'warning', 1: 'danger', 2: 'success' }

async function loadRecords() {
  loading.value = true
  try {
    const res = await getMyFinancesAPI()
    if (res.code === 200) {
      records.value = (res.data || []).map(f => ({
        id: f.financeId, amount: f.money ? `¥${f.money}` : '-',
        status: statusMap[f.status] || '审核中', statusCode: f.status,
        date: f.createTime?.substring(0,10) || '-', remark: f.remark,
        rate: f.rate ? `${f.rate}%` : '-', repayment: f.repayment ? `${f.repayment}个月` : '-'
      }))
    }
  } catch(e) { ElMessage.error('获取融资记录失败') }
  finally { loading.value = false }
}
onMounted(loadRecords)

async function submitFinance() {
  if (!form.value.money) return ElMessage.warning('请输入融资金额')
  submitting.value = true
  try {
    const res = await applyFinanceAPI({ money: form.value.money, repayment: form.value.repayment || 12, fileInfo: form.value.fileInfo })
    if (res.code === 200) {
      ElMessage.success('融资申请已提交')
      dialogVisible.value = false
      form.value = { money: null, repayment: null, fileInfo: '' }
      loadRecords()
    }
  } catch(e) { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">我的融资管理</h2>
      <el-button type="primary" @click="dialogVisible = true">申请融资</el-button>
    </div>
    <el-table :data="records" v-loading="loading" stripe>
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="amount" label="融资金额" width="120" />
      <el-table-column prop="rate" label="年利率" width="100" />
      <el-table-column prop="repayment" label="还款期限" width="100" />
      <el-table-column prop="date" label="申请日期" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{row}"><el-tag :type="statusTypeMap[row.statusCode] || 'info'">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
    </el-table>
    <el-empty v-if="!loading && records.length === 0" description="暂无融资记录" />

    <el-dialog v-model="dialogVisible" title="申请融资" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="融资金额(元)"><el-input-number v-model="form.money" :min="1000" :step="10000" style="width:100%" /></el-form-item>
        <el-form-item label="还款期限(月)"><el-input-number v-model="form.repayment" :min="1" :max="60" style="width:100%" /></el-form-item>
        <el-form-item label="补充资料"><el-input v-model="form.fileInfo" type="textarea" :rows="3" placeholder="可填写补充说明或资料链接" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitFinance">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
