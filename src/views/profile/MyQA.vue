<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { listExperts } from '../../api/expert'
import { askQuestionAPI, getMyQuestionsAPI } from '../../api/question'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const questions = ref([])
const experts = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ expertName: route.query.expert || '', title: '', question: '', plantName: '' })

async function loadData() {
  loading.value = true
  try {
    const [qaRes, expRes] = await Promise.allSettled([getMyQuestionsAPI(), listExperts()])
    if (qaRes.status === 'fulfilled' && qaRes.value?.code === 200) {
      questions.value = (qaRes.value.data || []).map(q => ({
        id: q.id, expert: q.expertName, question: q.title || q.question,
        answer: q.answer || '等待专家解答', status: q.status, date: q.createTime?.substring(0,10)
      }))
    }
    if (expRes.status === 'fulfilled' && expRes.value?.code === 200) {
      experts.value = (expRes.value.data || []).map(e => ({
        value: e.userName, label: e.realName || e.userName
      }))
    }
  } catch(e) {}
  finally { loading.value = false }
}
onMounted(loadData)

async function submitQuestion() {
  if (!form.value.expertName || !form.value.title) return ElMessage.warning('请填写完整信息')
  try {
    const res = await askQuestionAPI(form.value)
    if (res.code === 200) {
      ElMessage.success('提问成功')
      dialogVisible.value = false
      form.value = { expertName: '', title: '', question: '', plantName: '' }
      loadData()
    }
  } catch(e) { ElMessage.error('提问失败') }
}
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">问答管理</h2>
      <el-button type="primary" @click="dialogVisible = true">回答提问</el-button>
    </div>
    <el-table :data="questions" v-loading="loading" stripe>
      <el-table-column prop="expert" label="提问专家" width="120" />
      <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="answer" label="回答" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{row}"><el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已回复' : '待回复' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button size="small" :disabled="row.status === 1" @click="ElMessage.info('请回复')">回复</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && questions.length === 0" description="暂无提问记录" />

    <el-dialog v-model="dialogVisible" title="向专家提问" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择专家">
          <el-select v-model="form.expertName" filterable style="width:100%">
            <el-option v-for="exp in experts" :key="exp.value" :label="exp.label" :value="exp.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="农作物"><el-input v-model="form.plantName" placeholder="如：水稻、玉米" /></el-form-item>
        <el-form-item label="问题详情"><el-input v-model="form.question" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
