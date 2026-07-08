<template>
  <div>
    <h2 class="page-title">融资申请</h2>
    <el-card>
      <template #header><span>申请农业融资</span></template>
      <el-form label-width="100px">
        <el-form-item label="融资金额"><el-input placeholder="请输入金额（元）" /></el-form-item>
        <el-form-item label="融资期限"><el-select placeholder="选择期限" style="width:100%"><el-option label="6个月" value="6" /><el-option label="12个月" value="12" /><el-option label="24个月" value="24" /></el-select></el-form-item>
        <el-form-item label="贷款用途"><el-input type="textarea" :rows="3" placeholder="请描述贷款用途" /></el-form-item>
        <el-form-item label="抵押物"><el-input placeholder="如有抵押物请说明" /></el-form-item>
        <el-form-item><el-button type="primary">提交申请</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><span>我的申请记录</span></template>
      <el-table :data="records" border stripe style="width:100%">
        <el-table-column prop="id" label="编号" width="160" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === '通过' ? 'success' : row.status === '驳回' ? 'danger' : 'warning'">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="date" label="申请日期" />
        <el-table-column label="操作"><el-button text type="primary" size="small">查看详情</el-button></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const records = ref([
  { id: 'RZ20260708001', amount: '50,000', status: '审核中', date: '2026-07-08' },
  { id: 'RZ20260701002', amount: '30,000', status: '已通过', date: '2026-07-01' },
  { id: 'RZ20260620003', amount: '100,000', status: '已驳回', date: '2026-06-20' }
])
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
</style>
