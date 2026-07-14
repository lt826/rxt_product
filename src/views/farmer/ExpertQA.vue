<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { askQuestionAPI, getMyQuestionsAPI } from '../../api/question'
import { listExperts } from '../../api/expert'

const questions = ref([])
const experts = ref([])
const loading = ref(false)
const submitting = ref(false)

const form = ref({ expertName: '', title: '', question: '', plantName: '' })

async function loadExperts() {
  try {
    const res = await listExperts()
    if (res.code === 200) experts.value = res.data || []
  } catch { /* ignore */ }
}

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getMyQuestionsAPI()
    if (res.code === 200) {
      questions.value = (res.data || []).map(q => ({
        id: q.id, title: q.title, question: q.question, answer: q.answer,
        expert: q.expertName || '待分配', expertRealName: '', date: '-', status: q.status
      }))
      if (experts.value.length) {
        const expertMap = Object.fromEntries(experts.value.map(e => [e.userName, e.realName]))
        questions.value.forEach(q => { q.expertRealName = expertMap[q.expert] || q.expert })
      }
    }
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleSubmit() {
  if (!form.value.title || !form.value.question) return ElMessage.warning('请填写问题标题和描述')
  if (!form.value.expertName) return ElMessage.warning('请选择要提问的专家')
  submitting.value = true
  try {
    const res = await askQuestionAPI({
      title: form.value.title, question: form.value.question,
      expertName: form.value.expertName, plantName: form.value.plantName || undefined
    })
    if (res.code === 200) {
      ElMessage.success('提问成功，等待专家解答')
      form.value = { expertName: '', title: '', question: '', plantName: '' }
      await loadQuestions()
    }
  } catch { /* handled in interceptor */ } finally { submitting.value = false }
}

onMounted(async () => { await loadExperts(); await loadQuestions() })
</script>

<template>
  <div class="qa-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">专家问答</h2>
        <p class="page-sub">向农业专家提问，获取专业指导</p>
      </div>
    </div>

    <el-card shadow="never" class="ask-card">
      <template #header><strong>向专家提问</strong></template>
      <el-form label-width="90px">
        <el-form-item label="选择专家" required>
          <el-select v-model="form.expertName" placeholder="请选择要咨询的专家" style="width:100%">
            <el-option
              v-for="ex in experts"
              :key="ex.userName"
              :label="`${ex.realName || ex.userName}（${ex.profession || '农业技术'}）`"
              :value="ex.userName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作物名称">
          <el-input v-model="form.plantName" placeholder="如：水稻、西红柿等" />
        </el-form-item>
        <el-form-item label="问题标题" required>
          <el-input v-model="form.title" placeholder="简要描述您的问题" />
        </el-form-item>
        <el-form-item label="问题描述" required>
          <el-input v-model="form.question" type="textarea" :rows="4" placeholder="详细描述您的问题" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" size="large" class="submit-btn" @click="handleSubmit">提交问题</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:20px">
      <template #header>
        <div class="card-header-row">
          <strong>我的提问</strong>
          <el-button text size="small" @click="loadQuestions">刷新</el-button>
        </div>
      </template>
      <div v-for="q in questions" :key="q.id" class="qa-item">
        <div class="qa-question">{{ q.title }}</div>
        <div class="qa-meta">提问给：{{ q.expertRealName || q.expert }}</div>
        <div class="qa-answer" v-if="q.answer">答：{{ q.answer }}</div>
        <el-tag v-else size="small" type="info">待回复</el-tag>
      </div>
      <el-empty v-if="!loading && questions.length === 0" description="暂无提问记录" />
    </el-card>
  </div>
</template>

<style scoped>
.qa-page { max-width: 1200px; margin: 0 auto; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.ask-card { border-radius: var(--radius-md); }
.submit-btn { width: 100%; border-radius: var(--radius-sm); }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.qa-item { padding: 16px 0; border-bottom: 1px solid var(--border); }
.qa-item:last-child { border: none; }
.qa-question { font-weight: 500; color: var(--text); margin-bottom: 6px; }
.qa-answer { color: var(--green); font-size: 14px; margin-bottom: 6px; font-weight: 500; }
.qa-meta { color: var(--text-muted); font-size: 13px; margin-top: 4px; }
</style>
