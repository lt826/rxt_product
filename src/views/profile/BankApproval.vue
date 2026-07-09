<script setup>
import { ref, onMounted } from 'vue'
import { getAllFinancesAPI, updateFinanceStatusAPI } from '../../api/finance'
import { ElMessage, ElMessageBox } from 'element-plus'

const applications = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentId = ref(null)
const remark = ref('')
const action = ref('')

async function loadApplications() {
  loading.value = true
  try {
    const res = await getAllFinancesAPI()
    if (res.code === 200) {
      applications.value = (res.data || []).map(f => ({
        id: f.financeId, applicant: f.ownName || f.realName, realName: f.realName,
        amount: f.money ? `¥${f.money}` : '-', phone: f.phone,
        status: f.status, statusLabel: {0:'待审批',1:'已驳回',2:'已通过'}[f.status] || '待审批',
        date: f.createTime?.substring(0,10) || '-', remark: f.remark || ''
      }))
    }
  } catch(e) { ElMessage.error('获取融资申请失败') }
  finally { loading.value = false }
}
onMounted(loadApplications)

async function handleAction(id, newStatus) {
  currentId.value = id
  action.value = newStatus === 2 ? '审核通过' : '驳回'
  remark.value = ''
  dialogVisible.value = true
}

async function confirmAction() {
  const status = action.value === '审核通过' ? 2 : 1
  try {
    const res = await updateFinanceStatusAPI(currentId.value, status, remark.value)
    if (res.code === 200) {
      ElMessage.success(`${action.value}成功`)
      dialogVisible.value = false
      loadApplications()
    }
  } catch(e) { ElMessage.error('操作失败') }
}
</script>

<template>
  <div>
    <h2 class="page-title">贷款审批</h2>
    <el-table :data="applications" v-loading="loading" stripe>
      <el-table-column prop="id" label="编号" width="70" />
      <el-table-column prop="applicant" label="申请人" width="120" />
      <el-table-column prop="amount" label="融资金额" width="120" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="date" label="申请日期" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="{0:'warning',1:'danger',2:'success'}[row.status] || 'info'">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleAction(row.id, 2)">通过</el-button>
            <el-button size="small" type="danger" @click="handleAction(row.id, 1)">驳回</el-button>
          </template>
          <span v-else style="color:#999;font-size:13px">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && applications.length === 0" description="暂无融资申请" />

    <el-dialog v-model="dialogVisible" :title="`${action}融资申请`" width="500px">
      <p>确定要{{ action }}该融资申请吗？</p>
      <el-input v-model="remark" type="textarea" :rows="3" :placeholder="`请输入${action === '驳回' ? '驳回原因' : '审批备注（可选）'}`" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAction">确定{{ action }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
