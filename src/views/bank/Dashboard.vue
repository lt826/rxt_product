<template>
  <div>
    <h2 class="page-title">银行工作台</h2>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-value" style="color:#409eff">24</div><div class="stat-label">待审批申请</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#67c23a">6</div><div class="stat-label">本日处理</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#e6a23c">3</div><div class="stat-label">活跃融资产品</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#f56c6c">¥128万</div><div class="stat-label">本月放贷总额</div></div>
    </div>
    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header><span>待审批列表</span></template><el-table :data="pendingApprovals" stripe><el-table-column prop="farmer" label="农户" width="100" /><el-table-column prop="amount" label="金额" width="100" /><el-table-column prop="date" label="提交日期" /><el-table-column label="操作" width="140"><template #default><el-button size="small" type="primary">审核</el-button><el-button size="small" type="danger">驳回</el-button></template></el-table-column></el-table></el-card></el-col>
      <el-col :span="12"><el-card><template #header><span>今日动态</span></template><el-timeline><el-timeline-item timestamp="10:30" type="primary">农户张大爷提交5万元融资申请</el-timeline-item><el-timeline-item timestamp="09:15" type="success">农户李婶的3万元贷款已放款</el-timeline-item><el-timeline-item timestamp="08:00" type="warning">融资产品"春耕贷"审批通过</el-timeline-item></el-timeline></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
const pendingApprovals = [
  { farmer: '张大爷', amount: '50,000', date: '2026-07-08', product: '春耕贷' },
  { farmer: '李婶', amount: '30,000', date: '2026-07-07', product: '惠农贷' },
  { farmer: '赵叔', amount: '100,000', date: '2026-07-06', product: '创业贷' }
]
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.stats-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px }
.stat-card { background:#fff;border-radius:8px;padding:24px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06) }
.stat-value { font-size:28px;font-weight:700;line-height:1 }
.stat-label { margin-top:8px;font-size:14px;color:#909399 }
</style>
