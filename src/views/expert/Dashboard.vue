<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listQuestionsForExpert } from '../../api/question'
import { listReservesForExpert } from '../../api/reserve'

const router = useRouter()

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

    // 最新提问
    latestQA.value = questions.slice(0, 5).map(q => ({
      id: q.id,
      title: q.title || q.question?.substring(0, 20) || '未命名问题',
      questioner: q.questioner || '匿名农户',
      date: '-'
    }))

    // 预约提醒
    appointments.value = reserves.filter(r => r.status === 0 || r.status === '0').slice(0, 5).map(a => ({
      id: a.id,
      questioner: a.questioner || '匿名农户',
      topic: a.plantName || '农业咨询',
      date: '-'
    }))
  } catch (e) {
    // 忽略
  }
})
</script>

<template>
  <div>
    <h2 class="page-title">专家工作台</h2>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value" style="color:#e6a23c">{{ pendingQuestions }}</div>
        <div class="stat-label">待回答问题</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color:#67c23a">{{ answeredQuestions }}</div>
        <div class="stat-label">已回复问题</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color:#409eff">{{ pendingAppointments }}</div>
        <div class="stat-label">待确认预约</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color:#f56c6c">{{ totalArticles || 0 }}</div>
        <div class="stat-label">已发布文章</div>
      </div>
    </div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span>最新提问</span></template>
          <div v-for="q in latestQA" :key="q.id" class="qa-mini">
            <div class="qa-title">{{ q.title }}</div>
            <div class="qa-meta">{{ q.questioner }}</div>
          </div>
          <div v-if="!latestQA.length" style="text-align:center;color:#909399;padding:20px 0">
            <el-button type="primary" link @click="router.push('/expert/qa')">暂无提问，去看看</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>预约提醒</span></template>
          <div v-for="a in appointments" :key="a.id" class="qa-mini">
            <div class="qa-title">{{ a.questioner }} · {{ a.topic }}</div>
            <div class="qa-meta">
              <el-button size="small" type="primary" link @click="router.push('/expert/appointments')">去处理</el-button>
            </div>
          </div>
          <div v-if="!appointments.length" style="text-align:center;color:#909399;padding:20px 0">
            <el-button type="primary" link @click="router.push('/expert/appointments')">暂无预约，去看看</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-title { font-size:20px;font-weight:600;color:#222;margin:0 0 20px }
.stats-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px }
.stat-card { background:#fff;border-radius:8px;padding:24px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06) }
.stat-value { font-size:36px;font-weight:700;line-height:1 }
.stat-label { margin-top:8px;font-size:14px;color:#909399 }
.qa-mini { padding:12px 0;border-bottom:1px solid #ebeef5 }
.qa-mini:last-child { border:none }
.qa-title { font-weight:500;color:#303133;margin-bottom:4px }
.qa-meta { font-size:13px;color:#909399 }
</style>
