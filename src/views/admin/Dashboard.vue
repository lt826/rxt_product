<template>
  <div>
    <h2 class="page-title">系统管理</h2>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-value" style="color:#409eff">1,284</div><div class="stat-label">注册用户</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#67c23a">36</div><div class="stat-label">活跃农户</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#e6a23c">12</div><div class="stat-label">入驻银行</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#f56c6c">¥386万</div><div class="stat-label">融资总额</div></div>
    </div>
    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header><span>用户增长趋势</span></template><div style="height:200px;display:flex;align-items:center;justify-content:center;color:#c0c4cc">图表区域（待接入）</div></el-card></el-col>
      <el-col :span="12"><el-card><template #header><span>融资数据概览</span></template><el-table :data="financeSummary" stripe><el-table-column prop="month" label="月份" /><el-table-column prop="applications" label="申请数" /><el-table-column prop="amount" label="总额（万）" /><el-table-column prop="approvalRate" label="通过率" /></el-table></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
const financeSummary = [
  { month:'2026-05', applications:28, amount:126, approvalRate:'78%' },
  { month:'2026-06', applications:35, amount:158, approvalRate:'82%' },
  { month:'2026-07', applications:18, amount:82, approvalRate:'85%' }
]
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.stats-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px }
.stat-card { background:#fff;border-radius:8px;padding:24px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06) }
.stat-value { font-size:28px;font-weight:700;line-height:1 }
.stat-label { margin-top:8px;font-size:14px;color:#909399 }
</style>
