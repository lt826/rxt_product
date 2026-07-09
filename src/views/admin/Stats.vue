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
  if (!statsData.value?.monthlyTrend || statsData.value.monthlyTrend.length === 0) {
    return []
  }
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
    if (res.code === 200) {
      statsData.value = res.data
    }
  } catch (e) {
    // 保持默认
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h2 class="page-title">数据统计</h2>

    <!-- KPI 卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6">
        <el-card>
          <div class="kpi-value">{{ statsData?.totalUsers ?? '-' }}</div>
          <div class="kpi-label">总用户数</div>
          <div class="kpi-change up">含 {{ statsData?.activeFarmers ?? 0 }} 名农户</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="kpi-value">{{ statsData?.totalFinancing ? formatMoney(statsData.totalFinancing) : '-' }}</div>
          <div class="kpi-label">融资总额</div>
          <div class="kpi-change up">通过率 {{ statsData?.approvalRate ?? 0 }}%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="kpi-value">{{ statsData?.totalOrders ?? '-' }}</div>
          <div class="kpi-label">订单总数</div>
          <div class="kpi-change up">{{ statsData?.activeBuyers ?? 0 }} 家采购商</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="kpi-value">{{ statsData?.approvalRate ?? '-' }}%</div>
          <div class="kpi-label">融资通过率</div>
          <div class="kpi-change" :class="(statsData?.approvalRate ?? 0) >= 50 ? 'up' : 'down'">
            {{ statsData?.totalFinances ?? 0 }} 笔申请
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card v-loading="loading">
          <template #header><span>用户分布</span></template>
          <div class="chart-placeholder">
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
      <el-col :span="12">
        <el-card v-loading="loading">
          <template #header><span>月度融资趋势</span></template>
          <el-table :data="monthlyTrend" stripe>
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
.page-title { font-size:20px;font-weight:600;color:#222;margin:0 0 20px }
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
