<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listQuestionsForExpert } from '../../api/question'
import { listReservesForExpert } from '../../api/reserve'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const userName = computed(() => userStore.nickName || userStore.userInfo?.userName || '专家')

const pendingQuestions = ref(0)
const answeredQuestions = ref(0)
const pendingAppointments = ref(0)
const totalArticles = ref(0)

const latestQA = ref([])
const appointments = ref([])

onMounted(async () => {
  try {
    const [qRes, aRes] = await Promise.allSettled([
      listQuestionsForExpert(),
      listReservesForExpert()
    ])

    const questions = qRes.value?.code === 200 ? (qRes.value.data || []) : []
    const reserves = aRes.value?.code === 200 ? (aRes.value.data || []) : []

    pendingQuestions.value = questions.filter(q => q.status === 0 || q.status === '0').length
    answeredQuestions.value = questions.filter(q => q.status === 1 || q.status === '1').length
    pendingAppointments.value = reserves.filter(r => r.status === 0 || r.status === '0').length

    latestQA.value = questions.slice(0, 5).map(q => ({
      id: q.id,
      title: q.title || q.question?.substring(0, 20) || '未命名问题',
      questioner: q.questioner || '匿名农户',
      date: '-'
    }))

    appointments.value = reserves.filter(r => r.status === 0 || r.status === '0').slice(0, 5).map(a => ({
      id: a.id,
      questioner: a.questioner || '匿名农户',
      topic: a.plantName || '农业咨询',
      date: '-'
    }))
  } catch {
    // keep defaults
  }
})
</script>

<template>
  <div class="dashboard">
    <div class="dash-welcome">
      <div>
        <h2 class="welcome-greeting">专家首页</h2>
        <p class="welcome-sub">管理你的问答和预约</p>
      </div>
      <el-button type="primary" @click="router.push('/expert/qa')">处理问答 →</el-button>
    </div>

    <!-- Stats -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #e6a23c, #f7d08e)">
          <el-icon :size="24" color="#fff"><ChatDotRound /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ pendingQuestions }}</div>
          <div class="stat-label">待回答问题</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #67c23a, #95d675)">
          <el-icon :size="24" color="#fff"><Check /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ answeredQuestions }}</div>
          <div class="stat-label">已回复问题</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #409eff, #79b8ff)">
          <el-icon :size="24" color="#fff"><Calendar /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ pendingAppointments }}</div>
          <div class="stat-label">待确认预约</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #f56c6c, #fba0a0)">
          <el-icon :size="24" color="#fff"><Reading /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ totalArticles || 0 }}</div>
          <div class="stat-label">已发布文章</div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-row">
              <span>最新提问</span>
              <el-button text type="primary" size="small" @click="router.push('/expert/qa')">全部 →</el-button>
            </div>
          </template>
          <div v-for="q in latestQA" :key="q.id" class="qa-mini">
            <div class="qa-title">{{ q.title }}</div>
            <div class="qa-meta">{{ q.questioner }}</div>
          </div>
          <div v-if="!latestQA.length" class="empty-hint">
            <p>暂无提问</p>
            <el-button type="primary" link @click="router.push('/expert/qa')">去看看</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-row">
              <span>预约提醒</span>
              <el-button text type="primary" size="small" @click="router.push('/expert/appointments')">全部 →</el-button>
            </div>
          </template>
          <div v-for="a in appointments" :key="a.id" class="qa-mini">
            <div class="qa-title">{{ a.questioner }} · {{ a.topic }}</div>
            <div class="qa-meta">
              <el-button size="small" type="primary" link @click="router.push('/expert/appointments')">去处理</el-button>
            </div>
          </div>
          <div v-if="!appointments.length" class="empty-hint">
            <p>暂无预约</p>
            <el-button type="primary" link @click="router.push('/expert/appointments')">去看看</el-button>
          </div>
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
.stat-number { font-size: 32px; font-weight: 800; line-height: 1; color: var(--text); }
.stat-label { margin-top: 6px; font-size: 13px; color: var(--text-muted); }

.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.qa-mini { padding: 12px 0; border-bottom: 1px solid var(--border); }
.qa-mini:last-child { border: none; }
.qa-title { font-weight: 500; color: var(--text); margin-bottom: 4px; font-size: 14px; }
.qa-meta { font-size: 13px; color: var(--text-muted); }
.empty-hint { text-align: center; padding: 24px 0; color: var(--text-muted); }
.empty-hint p { margin: 0 0 8px; font-size: 14px; }
</style>
