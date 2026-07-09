<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listQuestionsForExpert, answerQuestion } from '../../api/question'

const loading = ref(false)
const list = ref([])
const replyText = reactive({})
const replyLoading = ref({})
const activeFilter = ref('all')

async function loadList() {
  loading.value = true
  try {
    const res = await listQuestionsForExpert()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const filtered = computed(() =>
  list.value.filter((q) => {
    if (activeFilter.value === 'pending') return q.status === 0
    if (activeFilter.value === 'done') return q.status === 1
    return true
  })
)

async function submitReply(q) {
  const text = (replyText[q.id] || '').trim()
  if (!text) return ElMessage.warning('请输入回复内容')
  replyLoading.value[q.id] = true
  try {
    await answerQuestion({ id: q.id, answer: text })
    q.answer = text
    q.status = 1
    replyText[q.id] = ''
    ElMessage.success('回复成功')
  } finally {
    replyLoading.value[q.id] = false
  }
}

onMounted(loadList)
</script>

<template>
  <div class="qa-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">问答处理</h2>
        <p class="page-sub">回复农户提问，提供专业指导</p>
      </div>
    </div>

    <el-radio-group v-model="activeFilter" class="filter-group" style="margin-bottom:20px">
      <el-radio-button value="all">全部</el-radio-button>
      <el-radio-button value="pending">待回复</el-radio-button>
      <el-radio-button value="done">已回复</el-radio-button>
    </el-radio-group>

    <el-card shadow="never" v-loading="loading">
      <div v-for="q in filtered" :key="q.id" class="qa-item">
        <div class="qa-head">
          <span class="qa-farmer">
            <el-icon><User /></el-icon>{{ q.questioner }}
            <span class="qa-phone" v-if="q.phone">· {{ q.phone }}</span>
          </span>
          <div class="qa-tags">
            <el-tag size="small" type="info" effect="plain">{{ q.plantName || '未指定' }}</el-tag>
            <el-tag v-if="q.status === 1" size="small" type="success">已回复</el-tag>
            <el-tag v-else size="small" type="warning">待回复</el-tag>
          </div>
        </div>
        <div class="qa-title">{{ q.title }}</div>
        <div class="qa-content">{{ q.question }}</div>
        <div v-if="q.answer" class="qa-reply"><strong>我的回复：</strong>{{ q.answer }}</div>
        <div v-else class="qa-form">
          <el-input v-model="replyText[q.id]" type="textarea" :rows="2" placeholder="输入回复内容，提交后将通知农户…" />
          <el-button size="small" type="primary" style="margin-top: 8px" :loading="replyLoading[q.id]" @click="submitReply(q)">回复</el-button>
        </div>
      </div>
      <el-empty v-if="!filtered.length" description="暂无问题" />
    </el-card>
  </div>
</template>

<style scoped>
.qa-page { max-width: 1000px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.filter-group { display: flex; gap: 0; }
.qa-item { padding: 20px 0; border-bottom: 1px solid var(--border); }
.qa-item:last-child { border: none; }
.qa-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.qa-farmer { display: flex; align-items: center; gap: 6px; font-weight: 500; color: var(--text); }
.qa-phone { color: var(--text-muted); font-weight: normal; }
.qa-tags { display: flex; gap: 6px; }
.qa-title { font-size: 16px; font-weight: 600; color: var(--text); margin-bottom: 6px; }
.qa-content { color: var(--text-secondary); font-size: 14px; line-height: 1.6; margin-bottom: 8px; }
.qa-reply { background: var(--green-mist); padding: 12px; border-radius: var(--radius-sm); color: var(--green); font-size: 14px; line-height: 1.6; }
.qa-form { margin-top: 4px; }
</style>
