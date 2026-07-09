<template>
  <div class="rules-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">智能匹配规则</h2>
        <p class="page-sub">配置融资申请的智能匹配条件</p>
      </div>
    </div>

    <el-card shadow="never" class="config-card">
      <template #header><strong>匹配规则配置</strong></template>
      <el-form label-width="140px" class="config-form">
        <el-form-item label="信用评分门槛">
          <el-slider v-model="creditScore" :min="300" :max="900" show-input style="width:300px" />
        </el-form-item>
        <el-form-item label="历史种植面积">
          <el-slider v-model="landArea" :min="0" :max="100" show-input style="width:300px" />
          <span class="unit">亩</span>
        </el-form-item>
        <el-form-item label="最大负债率">
          <el-slider v-model="debtRatio" :min="0" :max="100" show-input style="width:300px" />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="数据来源天数">
          <el-input-number v-model="dataDays" :min="30" :max="365" />
          <span class="unit">天</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="save-btn">保存规则</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:20px">
      <template #header><strong>匹配日志</strong></template>
      <el-table :data="logs" stripe>
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="farmer" label="农户" width="100" />
        <el-table-column prop="product" label="推荐产品" />
        <el-table-column prop="score" label="匹配分" width="100" />
        <el-table-column label="结果" width="80">
          <template #default="{row}">
            <el-tag :type="row.result==='匹配'?'success':'info'" size="small">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
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
.rules-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.config-card { border-radius: var(--radius-md); }
.config-form .el-form-item { margin-bottom: 24px; }
.unit { margin-left: 8px; color: var(--text-muted); font-size: 14px; }
.save-btn { border-radius: var(--radius-sm); font-weight: 600; }
</style>
