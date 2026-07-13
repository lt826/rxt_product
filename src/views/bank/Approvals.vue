<template>
  <div class="approvals-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">融资审批</h2>
        <p class="page-sub">审核农户融资申请（含智能匹配度，辅助决策）</p>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header-row">
          <strong>融资申请列表</strong>
          <el-button text size="small" @click="loadList">刷新</el-button>
        </div>
      </template>
      <el-table :data="applications" stripe empty-text="暂无融资申请">
        <el-table-column prop="financeId" label="编号" width="80" />
        <el-table-column label="农户" min-width="100">
          <template #default="{ row }">{{ row.realName || row.ownName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="coLenderNames" label="联合贷款人" min-width="150" />
        <el-table-column label="申请金额" width="140">
          <template #default="{ row }">{{ formatMoney(row.money) }}</template>
        </el-table-column>
        <el-table-column label="期限" width="110">
          <template #default="{ row }">{{ row.repayment != null ? row.repayment + ' 个月' : '-' }}</template>
        </el-table-column>
        <el-table-column label="匹配度" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="Number(row.matchScore || 0)"
              :color="scoreColor(row.matchScore)"
              :stroke-width="14"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.status] || 'warning'" size="small">{{ statusMap[row.status] || '待审批' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" :disabled="row.status !== 0" @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 0" @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && applications.length === 0" description="暂无融资申请" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApprovalListAPI, updateFinanceStatusAPI } from '../../api/finance'

const loading = ref(false)
const applications = ref([])
// 与后端 updateFinanceStatus 自洽：0 待审批 / 1 已通过 / 2 已驳回
const statusMap = { 0: '待审批', 1: '已通过', 2: '已驳回' }
const typeMap = { 0: 'warning', 1: 'success', 2: 'danger' }

function scoreColor(score) {
  const s = Number(score || 0)
  if (s > 60) return '#67c23a' // 绿：>60
  if (s >= 40) return '#e6a23c' // 橙：40~60
  return '#f56c6c' // 红：<40
}

function formatMoney(money) {
  if (money == null || money === '') return '-'
  return '¥' + Number(money).toLocaleString()
}

async function loadList() {
  loading.value = true
  try {
    const res = await getApprovalListAPI()
    if (res.code === 200) {
      applications.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取列表失败')
    }
  } catch (e) {
    /* 错误已由 axios 拦截器统一提示 */
  } finally {
    loading.value = false
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm('确认通过该融资申请？', '提示')
  } catch {
    return // 用户取消
  }
  try {
    await updateFinanceStatusAPI(row.financeId, 1, '审核通过')
    ElMessage.success('已通过')
    loadList()
  } catch (e) {
    /* 错误已由 axios 拦截器统一提示 */
  }
}

async function reject(row) {
  let value = ''
  try {
    const { value: v } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', {
      confirmButtonText: '驳回',
      cancelButtonText: '取消'
    })
    value = v
  } catch {
    return // 用户取消
  }
  try {
    await updateFinanceStatusAPI(row.financeId, 2, value || '审核不通过')
    ElMessage.success('已驳回')
    loadList()
  } catch (e) {
    /* 错误已由 axios 拦截器统一提示 */
  }
}

onMounted(loadList)
</script>

<style scoped>
.approvals-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
