<script setup>
import { ref, onMounted } from 'vue'
import { listQuestionsForExpert } from '../../api/question'

// 专家端：查看自己已回复的问答（提问/回答在专家主页面进行）
const questions = ref([])
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await listQuestionsForExpert()
    if (res && res.code === 200) {
      questions.value = (res.data || [])
        .filter(q => q.status === 1) // 仅展示已回复的问答
        .map(q => ({
          id: q.id,
          questioner: q.questioner || '-',
          plantName: q.plantName || '-',
          question: q.title || q.question || '-',
          answer: q.answer || '-'
        }))
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(loadData)
</script>

<template>
  <div>
    <h2 class="page-title">问答管理</h2>
    <el-table :data="questions" v-loading="loading" stripe>
      <el-table-column prop="questioner" label="提问人" width="120" />
      <el-table-column prop="plantName" label="农作物" width="110" />
      <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="answer" label="我的回答" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default><el-tag type="success">已回复</el-tag></template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && questions.length === 0" description="暂无已回复的问答" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
