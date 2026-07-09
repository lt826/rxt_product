<script setup>
import { ref, onMounted, computed } from 'vue'
import { getStatsOverviewAPI } from '../../api/admin'
import { useUserStore } from '../../stores/user'
import ProfileCard from '../../components/ProfileCard.vue'

const userStore = useUserStore()
const userName = computed(() => userStore.userInfo?.userName || '')

const stats = ref([
  { label: '注册用户', value: '-', color: '#409eff' },
  { label: '活跃农户', value: '-', color: '#67c23a' },
  { label: '入驻银行', value: '-', color: '#e6a23c' },
  { label: '融资总额', value: '-', color: '#f56c6c' }
])

const financeSummary = ref([])

onMounted(async () => {
  try {
    const res = await getStatsOverviewAPI()
    if (res.code === 200 && res.data) {
      const d = res.data
      // 格式化金额显示
      const formatMoney = (val) => {
        if (!val) return '¥0'
        const num = typeof val === 'number' ? val : parseFloat(val)
        if (num >= 10000) return '¥' + (num / 10000).toFixed(1) + '万'
        return '¥' + num.toLocaleString()
      }

      stats.value = [
        { label: '注册用户', value: String(d.totalUsers ?? '-'), color: '#409eff' },
        { label: '活跃农户', value: String(d.activeFarmers ?? '-'), color: '#67c23a' },
        { label: '入驻银行', value: String(d.activeBanks ?? '-'), color: '#e6a23c' },
        { label: '融资总额', value: formatMoney(d.totalFinancing), color: '#f56c6c' }
      ]

      // 月度数据
      if (d.monthlyTrend && d.monthlyTrend.length > 0) {
        financeSummary.value = d.monthlyTrend.map(m => ({
          month: m.month,
          applications: m.applications || 0,
          amount: m.totalAmount || '0',
          approvalRate: m.approved && m.applications
            ? ((m.approved / m.applications) * 100).toFixed(0) + '%'
            : '-'
        }))
      } else {
        // 空数据占位
        financeSummary.value = [
          { month: '暂无数据', applications: '-', amount: '-', approvalRate: '-' }
        ]
      }
    }
  } catch (e) {
    // 保持默认值
  }
})
</script>

<template>
  <div>
    <h2 class="page-title">系统管理</h2>

    <ProfileCard v-if="userName" :user-name="userName" />

    <div class="stats-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card><template #header><span>用户增长趋势</span></template>
          <div style="height:200px;display:flex;align-items:center;justify-content:center;color:#c0c4cc">图表区域（待接入）</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header><span>融资数据概览</span></template>
          <el-table :data="financeSummary" stripe>
            <el-table-column prop="month" label="月份" />
            <el-table-column prop="applications" label="申请数" />
            <el-table-column prop="amount" label="总额（万）" />
            <el-table-column prop="approvalRate" label="通过率" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-title { font-size:20px;font-weight:600;color:#222;margin:0 0 20px }
.stats-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px }
.stat-card { background:#fff;border-radius:8px;padding:24px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06) }
.stat-value { font-size:28px;font-weight:700;line-height:1 }
.stat-label { margin-top:8px;font-size:14px;color:#909399 }
</style>
