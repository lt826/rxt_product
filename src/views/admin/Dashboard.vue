<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getStatsOverviewAPI, getUserGrowthAPI } from '../../api/admin'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const userName = computed(() => userStore.nickName || userStore.userInfo?.userName || '管理员')

const stats = ref([
  { label: '注册用户', value: '-', icon: 'UserFilled', color: '#409eff', bg: 'linear-gradient(135deg, #409eff, #79b8ff)' },
  { label: '活跃农户', value: '-', icon: 'User', color: '#67c23a', bg: 'linear-gradient(135deg, #67c23a, #95d675)' },
  { label: '入驻银行', value: '-', icon: 'OfficeBuilding', color: '#e6a23c', bg: 'linear-gradient(135deg, #e6a23c, #f7d08e)' },
  { label: '融资总额', value: '-', icon: 'Money', color: '#f56c6c', bg: 'linear-gradient(135deg, #f56c6c, #fba0a0)' }
])

const financeSummary = ref([])

// 用户增长图表
const chartRef = ref(null)
let chartInstance = null
const growthMode = ref('month')
const growthLoading = ref(false)
const growthData = ref([])

const modeLabel = computed(() => {
  return { day: '按日', month: '按月', year: '按年' }[growthMode.value] || '按月'
})

onMounted(async () => {
  try {
    const res = await getStatsOverviewAPI()
    if (res.code === 200 && res.data) {
      const d = res.data
      const formatMoney = (val) => {
        if (!val) return '¥0'
        const num = typeof val === 'number' ? val : parseFloat(val)
        if (num >= 10000) return '¥' + (num / 10000).toFixed(1) + '万'
        return '¥' + num.toLocaleString()
      }

      stats.value = [
        { label: '注册用户', value: String(d.totalUsers ?? '-'), icon: 'UserFilled', color: '#409eff', bg: 'linear-gradient(135deg, #409eff, #79b8ff)' },
        { label: '活跃农户', value: String(d.activeFarmers ?? '-'), icon: 'User', color: '#67c23a', bg: 'linear-gradient(135deg, #67c23a, #95d675)' },
        { label: '入驻银行', value: String(d.activeBanks ?? '-'), icon: 'OfficeBuilding', color: '#e6a23c', bg: 'linear-gradient(135deg, #e6a23c, #f7d08e)' },
        { label: '融资总额', value: formatMoney(d.totalFinancing), icon: 'Money', color: '#f56c6c', bg: 'linear-gradient(135deg, #f56c6c, #fba0a0)' }
      ]

      if (d.monthlyTrend && d.monthlyTrend.length > 0) {
        financeSummary.value = d.monthlyTrend.map(m => ({
          month: m.month,
          applications: m.applications || 0,
          amount: m.totalAmount || '0',
          approvalRate: m.approved && m.applications
            ? ((m.approved / m.applications) * 100).toFixed(0) + '%'
            : '-'
        }))
      }
    }
  } catch { /* keep defaults */ }

  await loadGrowthData()
  initChart()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

function handleResize() {
  if (chartInstance) chartInstance.resize()
}

async function loadGrowthData() {
  growthLoading.value = true
  try {
    const res = await getUserGrowthAPI(growthMode.value)
    if (res.code === 200 && res.data) {
      growthData.value = res.data
    }
  } catch { /* ignore */ }
  finally { growthLoading.value = false }
}

async function onModeChange(mode) {
  growthMode.value = mode
  await loadGrowthData()
  if (chartInstance) {
    chartInstance.setOption(buildChartOption(), true)
  }
}

function initChart() {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(buildChartOption())
}

function buildChartOption() {
  const data = growthData.value || []
  const dates = data.map(d => d.date)
  const counts = data.map(d => d.count)

  const xLabel = { day: '日期', month: '月份', year: '年份' }[growthMode.value] || '月份'

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: dates.length > 12 ? 45 : 0, fontSize: 11 },
      name: xLabel
    },
    yAxis: {
      type: 'value',
      name: '新增用户',
      minInterval: 1,
      axisLabel: { formatter: '{value}' }
    },
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#b3e19d' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '40%',
        label: {
          show: true,
          position: 'top',
          color: '#333',
          fontSize: 11
        }
      },
      {
        name: '趋势',
        type: 'line',
        data: counts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' },
        label: {
          show: true,
          position: 'top',
          color: '#409eff',
          fontSize: 10,
          formatter: p => p.value > 0 ? p.value : ''
        }
      }
    ]
  }
}
</script>

<template>
  <div class="dashboard">
    <div class="dash-welcome">
      <div>
        <h2 class="welcome-greeting">系统管理面板 📊</h2>
        <p class="welcome-sub">平台运营数据概览</p>
      </div>
      <el-button type="primary" @click="router.push('/admin/users')">用户管理 →</el-button>
    </div>

    <!-- Stats -->
    <div class="stats-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-gradient" :style="{ background: s.bg }">
          <el-icon :size="24" color="#fff"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <span>用户增长趋势</span>
              <el-radio-group v-model="growthMode" size="small" @change="onModeChange">
                <el-radio-button value="day">按日</el-radio-button>
                <el-radio-button value="month">按月</el-radio-button>
                <el-radio-button value="year">按年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div v-loading="growthLoading" class="chart-body">
            <div v-if="growthData.length === 0 && !growthLoading" class="chart-empty">
              <el-icon :size="36" color="#c0c4cc"><TrendCharts /></el-icon>
              <p>暂无增长数据</p>
            </div>
            <div ref="chartRef" class="chart-container"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never">
          <template #header><span>融资数据概览</span></template>
          <el-table :data="financeSummary" stripe size="small">
            <el-table-column prop="month" label="月份" width="100" />
            <el-table-column prop="applications" label="申请数" width="80" />
            <el-table-column prop="amount" label="总额（万）" />
            <el-table-column prop="approvalRate" label="通过率" width="80" />
          </el-table>
          <el-empty v-if="financeSummary.length === 0" description="暂无融资数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1200px; }
.dash-welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}
.welcome-greeting { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.welcome-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}
@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
.stat-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  transition: all 0.25s;
}
.stat-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.stat-gradient { padding: 16px; display: flex; justify-content: center; }
.stat-body { padding: 16px 20px 20px; text-align: center; }
.stat-number { font-size: 28px; font-weight: 800; line-height: 1; color: var(--text); }
.stat-label { margin-top: 6px; font-size: 13px; color: var(--text-muted); }

/* 图表 */
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart-body {
  position: relative;
  min-height: 280px;
}
.chart-container {
  width: 100%;
  height: 280px;
}
.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  gap: 8px;
}
.chart-empty p { font-size: 14px; margin: 0; }
</style>
