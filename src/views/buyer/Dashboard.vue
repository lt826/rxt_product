<template>
  <div>
    <h2 class="page-title">买家工作台</h2>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-value" style="color:#409eff">12</div><div class="stat-label">在售商品</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#67c23a">5</div><div class="stat-label">收购需求</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#e6a23c">8</div><div class="stat-label">待联系农户</div></div>
      <div class="stat-card"><div class="stat-value" style="color:#f56c6c">3</div><div class="stat-label">今日询价</div></div>
    </div>
    <el-card><template #header><span>最近收购需求匹配</span></template><el-table :data="matches" border stripe style="width:100%"><el-table-column prop="product" label="农产品" /><el-table-column prop="amount" label="需求量" /><el-table-column prop="farmer" label="意向农户" /><el-table-column prop="status" label="状态"><template #default="{row}"><el-tag :type="row.status==='已匹配'?'success':'warning'">{{row.status}}</el-tag></template></el-table-column></el-table></el-card>
  </div>
</template>
<script setup>
const matches = [
  { product: '有机大米', amount: '500kg', farmer: '张大爷', status: '已匹配' },
  { product: '新鲜蔬菜', amount: '200kg', farmer: '李叔', status: '待确认' }
]
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#222;margin:0 0 20px }
.stats-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px }
.stat-card { background:#fff;border-radius:8px;padding:24px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06) }
.stat-value { font-size:36px;font-weight:700;line-height:1 }
.stat-label { margin-top:8px;font-size:14px;color:#909399 }
</style>
