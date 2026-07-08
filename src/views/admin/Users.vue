<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <el-card><template #header><span>用户列表</span></template>
      <div style="margin-bottom:16px;display:flex;gap:12px">
        <el-input placeholder="搜索用户名" style="width:200px" />
        <el-select placeholder="角色筛选"><el-option label="全部" value="" /><el-option label="农户" value="farmer" /><el-option label="买家" value="buyer" /><el-option label="银行" value="bank" /><el-option label="专家" value="expert" /></el-select>
        <el-select placeholder="状态"><el-option label="全部" value="" /><el-option label="正常" value="active" /><el-option label="待审核" value="pending" /></el-select>
        <el-button type="primary">搜索</el-button>
      </div>
      <el-table :data="users" border stripe><el-table-column prop="name" label="姓名" /><el-table-column prop="role" label="角色"><template #default="{row}"><el-tag>{{row.role}}</el-tag></template></el-table-column><el-table-column prop="phone" label="手机号" /><el-table-column prop="status" label="状态"><template #default="{row}"><el-tag :type="row.status==='正常'?'success':'warning'">{{row.status}}</el-tag></template></el-table-column><el-table-column prop="regDate" label="注册时间" width="120" /><el-table-column label="操作" width="150"><template #default="{row}"><el-button size="small" type="primary" link @click="audit(row)">审核</el-button><el-button size="small" type="danger" link>禁用</el-button></template></el-table-column></el-table></el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const users = ref([
  { name:'张大爷', role:'农户', phone:'138****1234', status:'正常', regDate:'2026-06-01' },
  { name:'绿源合作社', role:'买家', phone:'139****5678', status:'待审核', regDate:'2026-07-01' },
  { name:'王教授', role:'专家', phone:'137****9012', status:'正常', regDate:'2026-05-15' },
  { name:'农业银行客户经理', role:'银行', phone:'136****3456', status:'正常', regDate:'2026-06-20' }
])
function audit(row) { row.status='正常'; ElMessage.success(`${row.name} 审核通过`) }
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
