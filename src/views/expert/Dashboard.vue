<template>
  <div>
    <h2 class="page-title">专家工作台</h2>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-value" style="color:#409eff">8</div><div class="stat-label">待回答问题</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#67c23a">5</div><div class="stat-label">已发布文章</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#e6a23c">3</div><div class="stat-label">待确认预约</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#f56c6c">95%</div><div class="stat-label">好评率</div></div>
    </div>
    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header><span>最新提问</span></template><div v-for="q in latestQA" :key="q.id" class="qa-mini"><div class="qa-title">{{ q.title }}</div><div class="qa-meta">{{ q.farmer }} · {{ q.date }}</div></div></el-card></el-col>
      <el-col :span="12"><el-card><template #header><span>预约提醒</span></template><div v-for="a in appointments" :key="a.id" class="qa-mini"><div class="qa-title">{{ a.farmer }} · {{ a.topic }}</div><div class="qa-meta">{{ a.date }} · <el-button size="small" type="primary" link>确认</el-button></div></div></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
const latestQA = [
  { id:1, title:'大棚湿度控制多少合适？', farmer:'农户李某', date:'2026-07-08' },
  { id:2, title:'有机肥和化肥怎么搭配？', farmer:'农户张某', date:'2026-07-07' }
]
const appointments = [
  { id:1, farmer:'农户赵某', topic:'水稻病害咨询', date:'2026-07-10 14:00' },
  { id:2, farmer:'农户钱某', topic:'土壤改良方案', date:'2026-07-11 10:00' }
]
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.stats-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px }
.stat-card { background:#fff;border-radius:8px;padding:24px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06) }
.stat-value { font-size:36px;font-weight:700;line-height:1 }
.stat-label { margin-top:8px;font-size:14px;color:#909399 }
.qa-mini { padding:12px 0;border-bottom:1px solid #ebeef5 }
.qa-mini:last-child { border:none }
.qa-title { font-weight:500;color:#303133;margin-bottom:4px }
.qa-meta { font-size:13px;color:#909399 }
</style>
