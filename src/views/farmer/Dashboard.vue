<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyFinancesAPI, getFinancingProductsAPI } from '../../api/finance'
import { listMyQuestions as getMyQuestionsAPI } from '../../api/question'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const userName = computed(() => userStore.nickName || userStore.userInfo?.userName || '农户')

const loading = ref(false)
const stats = ref([
  { label: '可申请融资产品', value: '0', icon: 'Coin', color: 'var(--green)', bg: 'linear-gradient(135deg, #2d6a4f, #52b788)' },
  { label: '进行中的申请', value: '-', icon: 'Loading', color: '#e6a23c', bg: 'linear-gradient(135deg, #e6a23c, #f7d08e)' },
  { label: '我的提问', value: '-', icon: 'ChatDotRound', color: '#409eff', bg: 'linear-gradient(135deg, #409eff, #79b8ff)' },
  { label: '待回复问题', value: '-', icon: 'Bell', color: '#f56c6c', bg: 'linear-gradient(135deg, #f56c6c, #fba0a0)' }
])

const recentActivities = ref([])

const quickActions = [
  { label: '申请融资', icon: 'Coin', path: '/farmer/financing', desc: '查看可用融资产品' },
  { label: '商品货源', icon: 'Goods', path: '/farmer/products', desc: '浏览商品与市场' },
  { label: '专家问答', icon: 'ChatDotRound', path: '/farmer/expert-qa', desc: '向专家提问' },
  { label: '农业知识', icon: 'Reading', path: '/farmer/knowledge', desc: '学习种植技术' },
  { label: '专家预约', icon: 'Calendar', path: '/farmer/appointments', desc: '预约线下咨询' },
  { label: '消息通知', icon: 'Bell', path: '/farmer/messages', desc: '查看系统消息' }
]

onMounted(async () => {
  loading.value = true
  try {
    const [financeRes, questionRes, productsRes] = await Promise.allSettled([
      getMyFinancesAPI(),
      getMyQuestionsAPI(),
      getFinancingProductsAPI()
    ])

    const finances = financeRes.value?.code === 200 ? financeRes.value.data || [] : []
    const questions = questionRes.value?.code === 200 ? questionRes.value.data || [] : []
    const products = productsRes.value?.code === 200 ? productsRes.value.data || [] : []

    const pendingFinances = finances.filter(f => f.status === 0 || f.status === '0')
    const unansweredQuestions = questions.filter(q => !q.answer)

    stats.value = [
      { label: '可申请融资产品', value: String(products.length), icon: 'Coin', color: 'var(--green)', bg: 'linear-gradient(135deg, #2d6a4f, #52b788)' },
      { label: '进行中的申请', value: String(pendingFinances.length), icon: 'Loading', color: '#e6a23c', bg: 'linear-gradient(135deg, #e6a23c, #f7d08e)' },
      { label: '我的提问', value: String(questions.length), icon: 'ChatDotRound', color: '#409eff', bg: 'linear-gradient(135deg, #409eff, #79b8ff)' },
      { label: '待回复问题', value: String(unansweredQuestions.length), icon: 'Bell', color: '#f56c6c', bg: 'linear-gradient(135deg, #f56c6c, #fba0a0)' }
    ]

    const activities = []
    finances.slice(0, 3).forEach(f => {
      const time = f.createTime ? f.createTime.substring(0, 10) : '-'
      const statusText = f.status === 0 || f.status === '0' ? '融资申请已提交，等待银行审核' : '融资申请已处理'
      activities.push({ time, text: statusText, type: 'primary' })
    })
    questions.slice(0, 3).forEach(q => {
      const time = '-'
      const text = q.answer ? `问题"${q.title}"已获得专家回复` : `问题"${q.title}"等待专家回答`
      activities.push({ time, text, type: q.answer ? 'success' : 'info' })
    })
    recentActivities.value = activities.slice(0, 5)
  } catch {
    // keep defaults
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="dashboard">
    <!-- Welcome header -->
    <div class="dash-welcome">
      <div>
        <h2 class="welcome-greeting">你好，{{ userName }} 👋</h2>
        <p class="welcome-sub">欢迎回来，以下是你的首页概览</p>
      </div>
      <el-button type="primary" @click="router.push('/trade/home')">浏览市场 →</el-button>
    </div>

    <!-- Stats cards -->
    <div class="stats-grid">
      <div v-for="(s, i) in stats" :key="s.label" class="stat-card" v-loading="loading">
        <div class="stat-gradient" :style="{ background: s.bg }">
          <el-icon :size="24" color="#fff"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="section-header">
      <h3 class="section-title">快捷操作</h3>
    </div>
    <div class="actions-grid">
      <div v-for="a in quickActions" :key="a.label" class="action-card" @click="router.push(a.path)">
        <div class="action-icon-wrap">
          <el-icon :size="24"><component :is="a.icon" /></el-icon>
        </div>
        <span class="action-label">{{ a.label }}</span>
        <span class="action-desc">{{ a.desc }}</span>
      </div>
    </div>

    <!-- Recent activity -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="card-header-row">
          <span>最近动态</span>
          <el-button text type="primary" size="small" @click="router.push('/farmer/messages')">查看全部</el-button>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="act in recentActivities"
          :key="act.text"
          :timestamp="act.time"
          :type="act.type || ''"
          placement="top"
        >
          <el-card shadow="hover" class="activity-card">{{ act.text }}</el-card>
        </el-timeline-item>
        <el-timeline-item v-if="recentActivities.length === 0" timestamp="暂无" placement="top">
          <el-card shadow="hover" class="activity-card">
            <p>尚无动态数据</p>
            <p class="hint">尝试申请融资或向专家提问来产生第一条动态</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

/* Welcome */
.dash-welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}
.welcome-greeting {
  font-size: 24px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0 0 4px;
}
.welcome-sub {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

/* Stats */
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
.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.stat-gradient {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-body {
  padding: 16px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.stat-number {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
  color: var(--text);
}
.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

/* Section */
.section-header {
  margin-bottom: 16px;
}
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0;
}

/* Actions */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}
@media (max-width: 900px) { .actions-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 600px) { .actions-grid { grid-template-columns: repeat(2, 1fr); } }
.action-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.25s;
  text-align: center;
}
.action-card:hover {
  border-color: var(--green-light);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.action-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: var(--green-mist);
  color: var(--green);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.action-card:hover .action-icon-wrap {
  background: var(--green);
  color: #fff;
}
.action-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}
.action-desc {
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.3;
}

/* Activity */
.section-card {
  border-radius: var(--radius-md);
}
.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.activity-card {
  border-radius: var(--radius-sm);
  margin-bottom: 4px;
}
.activity-card p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}
.hint {
  color: var(--text-muted) !important;
  font-size: 13px !important;
  margin-top: 4px;
}
</style>
