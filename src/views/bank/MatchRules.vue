<template>
  <div>
    <h2 class="page-title">智能匹配规则</h2>
    <el-card style="margin-bottom:20px">
      <template #header><span>匹配规则配置</span></template>
      <el-form label-width="140px">
        <el-form-item label="信用评分门槛"><el-slider v-model="creditScore" :min="300" :max="900" show-input style="width:300px" /></el-form-item>
        <el-form-item label="历史种植面积"><el-slider v-model="landArea" :min="0" :max="100" show-input style="width:300px" />亩</el-form-item>
        <el-form-item label="最大负债率"><el-slider v-model="debtRatio" :min="0" :max="100" show-input style="width:300px" />%</el-form-item>
        <el-form-item label="数据来源天数"><el-input-number v-model="dataDays" :min="30" :max="365" />天</el-form-item>
        <el-form-item><el-button type="primary">保存规则</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card><template #header><span>匹配日志</span></template><el-table :data="logs" border stripe><el-table-column prop="time" label="时间" width="160" /><el-table-column prop="farmer" label="农户" width="100" /><el-table-column prop="product" label="推荐产品" /><el-table-column prop="score" label="匹配分" width="100" /><el-table-column prop="result" label="结果" width="80"><template #default="{row}"><el-tag :type="row.result==='匹配'?'success':'info'">{{row.result}}</el-tag></template></el-table-column></el-table></el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const creditScore = ref(600)
const landArea = ref(10)
const debtRatio = ref(50)
const dataDays = ref(180)
const logs = ref([
  { time:'2026-07-08 10:30', farmer:'张大爷', product:'春耕贷', score:82, result:'匹配' },
  { time:'2026-07-08 09:15', farmer:'李婶', product:'惠农贷', score:75, result:'匹配' },
  { time:'2026-07-07 14:00', farmer:'王叔', product:'创业贷', score:45, result:'不匹配' }
])
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
