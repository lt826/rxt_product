<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const stats = ref([
  { label: '可申请融资产品', value: '6', color: '#409eff' },
  { label: '进行中的申请', value: '2', color: '#67c23a' },
  { label: '待处理消息', value: '3', color: '#e6a23c' },
  { label: '未读通知', value: '1', color: '#f56c6c' }
])

const quickActions = [
  { label: '申请融资', icon: 'Coin', path: '/farmer/financing' },
  { label: '浏览商品', icon: 'Goods', path: '/farmer/products' },
  { label: '咨询专家', icon: 'ChatDotSquare', path: '/farmer/expert-qa' },
  { label: '查看消息', icon: 'Bell', path: '/farmer/messages' }
]
</script>

<template>
  <div class="dashboard">
    <h2 class="page-title">农户工作台</h2>
    <div class="stats-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>
    <h3 class="section-title">快捷操作</h3>
    <div class="actions-grid">
      <div v-for="a in quickActions" :key="a.label" class="action-card" @click="router.push(a.path)">
        <el-icon :size="28"><component :is="a.icon" /></el-icon>
        <span>{{ a.label }}</span>
      </div>
    </div>

    <el-card class="section-card">
      <template #header><span>最近动态</span></template>
      <el-timeline>
        <el-timeline-item timestamp="2026-07-08" type="primary">融资申请已提交，等待银行审核</el-timeline-item>
        <el-timeline-item timestamp="2026-07-07" type="success">您咨询的问题已获得专家回复</el-timeline-item>
        <el-timeline-item timestamp="2026-07-06">订单 #20260706 已发货</el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px,1fr)); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 8px; padding: 24px; text-align: center; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.stat-value { font-size: 36px; font-weight: 700; line-height: 1; }
.stat-label { margin-top: 8px; font-size: 14px; color: #909399; }
.section-title { font-size: 16px; font-weight: 500; color: #303133; margin: 0 0 12px; }
.actions-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px,1fr)); gap: 12px; margin-bottom: 24px; }
.action-card { background: #fff; border-radius: 8px; padding: 20px; text-align: center; cursor: pointer; box-shadow: 0 1px 4px rgba(0,0,0,0.06); transition: transform 0.15s; }
.action-card:hover { transform: translateY(-2px); }
.action-card span { display: block; margin-top: 8px; font-size: 14px; color: #606266; }
.section-card { margin-bottom: 24px; }
</style>
