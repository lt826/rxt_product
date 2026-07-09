<script setup>
import { ref, onMounted, computed } from 'vue'
import { getStatsOverviewAPI } from '../../api/admin'

const statsData = ref(null)
const loading = ref(false)

const userDistribution = computed(() => {
  if (!statsData.value?.userDistribution) {
    return [
      { role: '农户', count: '-', percent: 0, color: '#409eff' },
      { role: '买家', count: '-', percent: 0, color: '#67c23a' },
      { role: '银行', count: '-', percent: 0, color: '#e6a23c' },
      { role: '专家', count: '-', percent: 0, color: '#f56c6c' },
      { role: '管理员', count: '-', percent: 0, color: '#909399' }
    ]
  }
  return statsData.value.userDistribution
})

const monthlyTrend = computed(() => {
  if (!statsData.value?.monthlyTrend || statsData.value.monthlyTrend.length === 0) return []
  return statsData.value.monthlyTrend
})

const formatMoney = (val) => {
  if (!val) return '¥0'
  const num = typeof val === 'number' ? val : parseFloat(val)
  if (num >= 10000) return '¥' + (num / 10000).toFixed(1) + '万'
  return '¥' + num.toLocaleString()
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getStatsOverviewAPI()
    if (res.code === 200) statsData.value = res.data
  } catch { /* keep default */ } finally { loading.value = false }
})
</script>

<template>
  <div class="stats-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">数据统计</h2>
        <p class="page-sub">平台运营数据概览</p>
      </div>
    </div>

    <!-- KPI Cards -->
    <el-row :gutter="16" style="margin-bottom:24px">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-icon" style="background:linear-gradient(135deg,#409eff,#79b8ff)">
            <el-icon :size="24" color="#fff"><UserFilled /></el-icon>
          </div>
          <div class="kpi-value">{{ statsData?.totalUsers ?? '-' }}</div>
          <div class="kpi-label">总用户数</div>
          <div class="kpi-sub">含 {{ statsData?.activeFarmers ?? 0 }} 名农户</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-icon" style="background:linear-gradient(135deg,#67c23a,#95d675)">
            <el-icon :size="24" color="#fff"><Money /></el-icon>
          </div>
          <div class="kpi-value">{{ statsData?.totalFinancing ? formatMoney(statsData.totalFinancing) : '-' }}</div>
          <div class="kpi-label">融资总额</div>
          <div class="kpi-sub">通过率 {{ statsData?.approvalRate ?? 0 }}%</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-icon" style="background:linear-gradient(135deg,#e6a23c,#f7d08e)">
            <el-icon :size="24" color="#fff"><ShoppingBag /></el-icon>
          </div>
          <div class="kpi-value">{{ statsData?.totalOrders ?? '-' }}</div>
          <div class="kpi-label">订单总数</div>
          <div class="kpi-sub">{{ statsData?.activeBuyers ?? 0 }} 家采购商</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-icon" style="background:linear-gradient(135deg,#f56c6c,#fba0a0)">
            <el-icon :size="24" color="#fff"><TrendCharts /></el-icon>
          </div>
          <div class="kpi-value">{{ statsData?.approvalRate ?? '-' }}%</div>
          <div class="kpi-label">融资通过率</div>
          <div class="kpi-sub">{{ statsData?.totalFinances ?? 0 }} 笔申请</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never" v-loading="loading">
          <template #header><strong>用户分布</strong></template>
          <div class="distribution-chart">
            <div v-for="d in userDistribution" :key="d.role" class="bar-item">
              <span class="bar-label">{{ d.role }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: d.percent + '%', background: d.color }"></div>
              </div>
              <span class="bar-value">{{ d.count }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never" v-loading="loading">
          <template #header><strong>月度融资趋势</strong></template>
          <el-table :data="monthlyTrend" stripe size="small">
            <el-table-column prop="month" label="月份" />
            <el-table-column prop="applications" label="申请数" />
            <el-table-column prop="approved" label="通过数" />
            <el-table-column prop="totalAmount" label="放款总额（万）" />
          </el-table>
          <el-empty v-if="monthlyTrend.length === 0" description="暂无融资数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stats-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.kpi-card { border-radius: var(--radius-md); cursor: default; }
.kpi-icon { width: 48px; height: 48px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.kpi-value { font-size: 28px; font-weight: 800; color: var(--text); line-height: 1; margin-bottom: 4px; }
.kpi-label { font-size: 14px; color: var(--text-muted); margin-bottom: 4px; }
.kpi-sub { font-size: 13px; color: var(--green); font-weight: 500; }

.distribution-chart { padding: 8px 0 }
.bar-item { display: flex; align-items: center; gap: 12px; margin-bottom: 12px }
.bar-label { width: 50px; font-size: 14px; color: var(--text-secondary); text-align: right }
.bar-track { flex: 1; height: 24px; background: var(--bg); border-radius: 4px; overflow: hidden }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.3s }
.bar-value { width: 40px; font-size: 14px; color: var(--text); font-weight: 600; }
</style>
