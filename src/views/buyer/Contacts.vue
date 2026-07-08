<template>
  <div>
    <h2 class="page-title">联系农户</h2>
    <el-card><template #header><span>意向农户列表</span></template><div v-for="f in farmers" :key="f.id" class="farmer-card"><div class="farmer-info"><div class="farmer-avatar">{{ f.avatar }}</div><div><div class="farmer-name">{{ f.name }}</div><div class="farmer-detail">{{ f.product }} · {{ f.location }}</div></div></div><div><el-button size="small" type="primary" @click="contact(f)">联系</el-button></div></div></el-card>
  </div>
</template>
<script setup>
import { ElMessage } from 'element-plus'
const farmers = [
  { id: 1, avatar: '👨‍🌾', name: '张大爷', product: '有机大米', location: '湖南长沙' },
  { id: 2, avatar: '👩‍🌾', name: '李婶', product: '新鲜蔬菜', location: '山东寿光' },
  { id: 3, avatar: '🧑‍🌾', name: '赵叔', product: '土鸡蛋', location: '河南南阳' }
]
function contact(f) { ElMessage.success(`已发送联系请求给 ${f.name}`) }
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.farmer-card { display:flex;align-items:center;justify-content:space-between;padding:16px 0;border-bottom:1px solid #ebeef5 }
.farmer-card:last-child { border:none }
.farmer-info { display:flex;align-items:center;gap:12px }
.farmer-avatar { font-size:36px }
.farmer-name { font-weight:500;color:#303133 }
.farmer-detail { font-size:13px;color:#909399;margin-top:2px }
</style>
