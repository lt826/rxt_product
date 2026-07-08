<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { askQuestionAPI, getMyQuestionsAPI } from '../../api/question'

const questions = ref([])
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  expertName: '',
  title: '',
  question: '',
  plantName: ''
})

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getMyQuestionsAPI()
    if (res.code === 200) {
      questions.value = (res.data || []).map(q => ({
        id: q.id,
        title: q.title,
        question: q.question,
        answer: q.answer,
        expert: q.expertName || '待分配',
        date: '-',
        status: q.status
      }))
    }
  } catch (e) {
    // 忽略
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.value.title || !form.value.question) {
    ElMessage.warning('请填写问题标题和描述')
    return
  }
  submitting.value = true
  try {
    const res = await askQuestionAPI({
      title: form.value.title,
      question: form.value.question,
      expertName: form.value.expertName || undefined,
      plantName: form.value.plantName || undefined
    })
    if (res.code === 200) {
      ElMessage.success('提问成功，等待专家解答')
      form.value = { expertName: '', title: '', question: '', plantName: '' }
      await loadQuestions()
    }
  } catch (e) {
    // 已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadQuestions)
</script>

<template>
  <div>
    <h2 class="page-title">专家问答</h2>
    <el-card style="margin-bottom:20px">
      <template #header><span>向专家提问</span></template>
      <el-form>
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
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交问题</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header><span>我的提问</span></template>
      <div v-for="q in questions" :key="q.id" class="qa-item">
        <div class="qa-question">{{ q.title }}</div>
        <div class="qa-answer" v-if="q.answer">答：{{ q.answer }}</div>
        <el-tag v-else size="small" type="info">待回复</el-tag>
        <div class="qa-meta">{{ q.date }} · {{ q.expert }}</div>
      </div>
      <el-empty v-if="!loading && questions.length === 0" description="暂无提问记录" />
    </el-card>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.qa-item { padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.qa-item:last-child { border: none; }
.qa-question { font-weight: 500; color: #303133; margin-bottom: 6px; }
.qa-answer { color: #67c23a; font-size: 14px; margin-bottom: 6px; }
.qa-meta { color: #909399; font-size: 13px; margin-top: 4px; }
</style>
