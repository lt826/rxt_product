<template>
  <div>
    <h2 class="page-title">数据统计</h2>
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6"><el-card><div class="kpi-value">1,284</div><div class="kpi-label">总用户数</div><div class="kpi-change up">↑ 12.5%</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="kpi-value">386</div><div class="kpi-label">融资总额（万）</div><div class="kpi-change up">↑ 23.1%</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="kpi-value">1,560</div><div class="kpi-label">订单总数</div><div class="kpi-change up">↑ 8.7%</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="kpi-value">92%</div><div class="kpi-label">融资通过率</div><div class="kpi-change down">↓ 2.3%</div></el-card></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header><span>用户分布</span></template><div class="chart-placeholder">
        <div v-for="d in userDistribution" :key="d.role" class="bar-item"><span class="bar-label">{{ d.role }}</span><div class="bar-track"><div class="bar-fill" :style="{width:d.percent+'%',background:d.color}"></div></div><span class="bar-value">{{ d.count }}</span></div>
      </div></el-card></el-col>
      <el-col :span="12"><el-card><template #header><span>月度融资趋势</span></template><el-table :data="monthlyTrend" stripe><el-table-column prop="month" label="月份" /><el-table-column prop="applications" label="申请数" /><el-table-column prop="approved" label="通过数" /><el-table-column prop="totalAmount" label="放款总额（万）" /></el-table></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
const userDistribution = [
  { role:'农户', count:856, percent:66.7, color:'#409eff' },
  { role:'买家', count:228, percent:17.8, color:'#67c23a' },
  { role:'银行', count:96, percent:7.5, color:'#e6a23c' },
  { role:'专家', count:64, percent:5.0, color:'#f56c6c' },
  { role:'管理员', count:40, percent:3.1, color:'#909399' }
]
const monthlyTrend = [
  { month:'2026-05', applications:28, approved:22, totalAmount:126 },
  { month:'2026-06', applications:35, approved:29, totalAmount:158 },
  { month:'2026-07', applications:18, approved:15, totalAmount:82 }
]
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.kpi-value { font-size:28px;font-weight:700;color:#303133;line-height:1;margin-bottom:4px }
.kpi-label { font-size:14px;color:#909399;margin-bottom:4px }
.kpi-change { font-size:13px;font-weight:500 }
.kpi-change.up { color:#67c23a }
.kpi-change.down { color:#f56c6c }
.chart-placeholder { padding:8px 0 }
.bar-item { display:flex;align-items:center;gap:12px;margin-bottom:12px }
.bar-label { width:50px;font-size:14px;color:#606266;text-align:right }
.bar-track { flex:1;height:24px;background:#f0f2f5;border-radius:4px;overflow:hidden }
.bar-fill { height:100%;border-radius:4px;transition:width 0.3s }
.bar-value { width:40px;font-size:14px;color:#303133 }
</style>
