<template>
  <div>
    <h2 class="page-title">预约处理</h2>
    <el-card><template #header><span>预约请求</span></template><div v-for="a in appointments" :key="a.id" class="apt-card"><div class="apt-info"><div class="apt-farmer">{{ a.farmer }}</div><div class="apt-detail">{{ a.topic }} · {{ a.date }} · {{ a.duration }}分钟</div></div><div class="apt-status"><el-tag v-if="a.status==='待确认'" type="warning">待确认</el-tag><el-tag v-else-if="a.status==='已确认'" type="success">已确认</el-tag><el-tag v-else type="info">已拒绝</el-tag></div><div class="apt-actions" v-if="a.status==='待确认'"><el-button size="small" type="primary" @click="accept(a)">接受</el-button><el-button size="small" type="danger" @click="reject(a)">拒绝</el-button></div></div></el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const appointments = ref([
  { id:1, farmer:'农户赵某', topic:'水稻病害咨询', date:'2026-07-10 14:00', duration:30, status:'待确认' },
  { id:2, farmer:'农户钱某', topic:'土壤改良方案', date:'2026-07-11 10:00', duration:30, status:'待确认' },
  { id:3, farmer:'农户孙某', topic:'施肥技术指导', date:'2026-07-09 15:00', duration:60, status:'已确认' }
])
function accept(a) { a.status='已确认'; ElMessage.success('已接受预约') }
function reject(a) { a.status='已拒绝'; ElMessage.warning('已拒绝预约') }
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.apt-card { display:flex;align-items:center;justify-content:space-between;padding:16px 0;border-bottom:1px solid #ebeef5;gap:16px }
.apt-card:last-child { border:none }
.apt-farmer { font-weight:500;color:#303133 }
.apt-detail { font-size:13px;color:#909399;margin-top:4px }
.apt-actions { display:flex;gap:8px;flex-shrink:0 }
</style>
