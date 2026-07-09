<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { applyFinanceAPI, getMyFinancesAPI } from '../../api/finance'

const records = ref([])
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  money: null,
  repayment: null,
  fileInfo: ''
})

const statusMap = { 0: '审核中', 1: '已驳回', 2: '已通过' }
const statusTypeMap = { 0: 'warning', 1: 'danger', 2: 'success' }

async function loadRecords() {
  loading.value = true
  try {
    const res = await getMyFinancesAPI()
    if (res.code === 200) {
      records.value = (res.data || []).map(f => ({
        id: f.financeId,
        amount: f.money ? `¥${f.money}` : '-',
        status: statusMap[f.status] || '审核中',
        statusCode: f.status,
        date: f.createTime ? f.createTime.substring(0, 10) : '-',
        remark: f.remark
      }))
    }
  } catch {
    ElMessage.error('获取申请记录失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.value.money || !form.value.repayment) {
    ElMessage.warning('请填写融资金额和期限')
    return
  }
  submitting.value = true
  try {
    const res = await applyFinanceAPI({
      money: Number(form.value.money),
      repayment: Number(form.value.repayment)
    })
    if (res.code === 200) {
      ElMessage.success('申请已提交')
      form.value = { money: null, repayment: null, fileInfo: '' }
      await loadRecords()
    }
  } catch {
    // handled in interceptor
  } finally {
    submitting.value = false
  }
}

function viewDetail(row) {
  ElMessage.info(`查看详情：${row.id}`)
}

onMounted(loadRecords)
</script>

<template>
  <div class="financing-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">融资申请</h2>
        <p class="page-sub">申请农业融资，助力生产经营</p>
      </div>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="24" :md="10">
        <el-card shadow="never" class="form-card">
          <template #header><strong>申请融资</strong></template>
          <el-form label-width="100px" class="finance-form">
            <el-form-item label="融资金额" required>
              <el-input v-model="form.money" placeholder="请输入金额（元）" type="number" />
            </el-form-item>
            <el-form-item label="融资期限" required>
              <el-select v-model="form.repayment" placeholder="选择期限" style="width:100%">
                <el-option label="6个月" :value="6" />
                <el-option label="12个月" :value="12" />
                <el-option label="24个月" :value="24" />
              </el-select>
            </el-form-item>
            <el-form-item label="补充资料">
              <el-input v-model="form.fileInfo" placeholder="如有补充说明请填写" :rows="3" type="textarea" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" size="large" class="submit-btn" @click="handleSubmit">
                提交申请
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="14">
        <el-card shadow="never" class="records-card">
          <template #header>
            <div class="card-header-row">
              <strong>申请记录</strong>
              <el-button text size="small" @click="loadRecords">刷新</el-button>
            </div>
          </template>
          <el-table :data="records" v-loading="loading" stripe>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="amount" label="金额" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTypeMap[row.statusCode] || 'warning'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="申请日期" width="120" />
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="viewDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && records.length === 0" description="暂无申请记录" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.financing-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.form-card, .records-card { border-radius: var(--radius-md); }
.finance-form .el-form-item { margin-bottom: 20px; }
.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-sm);
}
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
