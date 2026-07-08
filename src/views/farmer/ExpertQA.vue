<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listExperts } from '../../api/expert'
import { askQuestion, listMyQuestions } from '../../api/question'

const experts = ref([])
const myQuestions = ref([])
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  expertName: '',
  plantName: '',
  title: '',
  question: ''
})

const rules = {
  expertName: [{ required: true, message: '请选择专家', trigger: 'change' }],
  title: [{ required: true, message: '请输入问题标题', trigger: 'blur' }],
  question: [{ required: true, message: '请描述您的问题', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const [e, q] = await Promise.all([listExperts(), listMyQuestions()])
    experts.value = e.data || []
    myQuestions.value = q.data || []
  } finally {
    loading.value = false
  }
}

function expertLabel(e) {
  return `${e.realName}（${e.profession || '专家'}）`
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await askQuestion({ ...form })
    ElMessage.success('提问成功，等待专家解答')
    Object.assign(form, { expertName: '', plantName: '', title: '', question: '' })
    myQuestions.value = (await listMyQuestions()).data || []
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading">
    <h2 class="page-title">专家问答</h2>

    <el-card style="margin-bottom: 20px">
      <template #header><span>向专家提问</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="选择专家" prop="expertName">
          <el-select v-model="form.expertName" placeholder="请选择专家" style="width: 100%" filterable>
            <el-option v-for="e in experts" :key="e.userName" :label="expertLabel(e)" :value="e.userName">
              <span>{{ e.realName }}</span>
              <span class="opt-sub">{{ e.profession }} · {{ e.belong }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="作物名称">
          <el-input v-model="form.plantName" placeholder="如：水稻、西红柿（可选）" />
        </el-form-item>
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="form.title" placeholder="简要描述您的问题" />
        </el-form-item>
        <el-form-item label="问题描述" prop="question">
          <el-input v-model="form.question" type="textarea" :rows="4" placeholder="详细描述问题现象、发生时间等" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交问题</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header><span>我的提问</span></template>
      <el-empty v-if="!myQuestions.length" description="还没有提问记录" />
      <div v-for="q in myQuestions" :key="q.id" class="qa-item">
        <div class="qa-top">
          <div class="qa-title">{{ q.title }}</div>
          <el-tag v-if="q.status === 1" size="small" type="success">已回复</el-tag>
          <el-tag v-else size="small" type="info">待回复</el-tag>
        </div>
        <div class="qa-q">{{ q.question }}</div>
        <div class="qa-a" v-if="q.answer"><strong>专家回复：</strong>{{ q.answer }}</div>
        <div class="qa-meta">
          <el-tag size="small" type="info" effect="plain">{{ q.plantName || '未指定' }}</el-tag>
          <span>提问对象：{{ q.expertName }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.opt-sub { float: right; color: #909399; font-size: 12px; }
.qa-item { padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.qa-item:last-child { border: none; }
.qa-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.qa-title { font-weight: 500; color: #303133; }
.qa-q { color: #606266; font-size: 14px; margin-bottom: 8px; line-height: 1.6; }
.qa-a { color: #529b2e; font-size: 14px; margin-bottom: 8px; background: #f0f9eb; padding: 10px; border-radius: 6px; line-height: 1.6; }
.qa-meta { display: flex; align-items: center; gap: 12px; color: #909399; font-size: 13px; }
</style>
