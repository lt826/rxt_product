<template>
  <div>
    <h2 class="page-title">融资审批</h2>
    <el-card><template #header><span>融资申请列表</span></template><el-table :data="applications" border stripe><el-table-column prop="id" label="编号" width="170" /><el-table-column prop="farmer" label="农户" width="90" /><el-table-column prop="amount" label="申请金额" width="120" /><el-table-column prop="product" label="融资产品" width="120" /><el-table-column prop="term" label="期限" width="80" /><el-table-column prop="status" label="状态" width="100"><template #default="{row}"><el-tag :type="row.status==='待审批'?'warning':row.status==='已通过'?'success':'danger'">{{row.status}}</el-tag></template></el-table-column><el-table-column label="操作" width="160"><template #default="{row}"><el-button size="small" type="primary" @click="approve(row)">通过</el-button><el-button size="small" type="danger" @click="reject(row)">驳回</el-button></template></el-table-column></el-table></el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const applications = ref([
  { id: 'RZ20260708001', farmer: '张大爷', amount: '50,000', product: '春耕贷', term: '12月', status: '待审批' },
  { id: 'RZ20260707002', farmer: '李婶', amount: '30,000', product: '惠农贷', term: '6月', status: '待审批' },
  { id: 'RZ20260706003', farmer: '赵叔', amount: '100,000', product: '创业贷', term: '24月', status: '已通过' }
])
function approve(row) { row.status = '已通过'; ElMessage.success('已通过') }
function reject(row) { row.status = '已驳回'; ElMessage.warning('已驳回') }
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
