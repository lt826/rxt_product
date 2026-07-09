<script setup>
import { ref, onMounted } from 'vue'
import { getMyKnowledgeAPI, publishKnowledgeAPI, deleteKnowledgeAPI } from '../../api/knowledge'
import { ElMessage } from 'element-plus'

const articles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ title: '', content: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getMyKnowledgeAPI()
    if (res.code === 200) {
      articles.value = (res.data || []).map(k => ({
        id: k.knowledgeId, title: k.title, content: k.content, date: k.createTime?.substring(0,10) || '-'
      }))
    }
  } catch(e) {}
  finally { loading.value = false }
}
onMounted(loadData)

async function publishArticle() {
  if (!form.value.title || !form.value.content) return ElMessage.warning('请填写完整信息')
  try {
    const res = await publishKnowledgeAPI(form.value)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      dialogVisible.value = false
      form.value = { title: '', content: '' }
      loadData()
    }
  } catch(e) { ElMessage.error('发布失败') }
}

async function remove(id) {
  try {
    const res = await deleteKnowledgeAPI(id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  } catch(e) { ElMessage.error('删除失败') }
}
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">知识管理</h2>
      <el-button type="primary" @click="dialogVisible = true">发布知识</el-button>
    </div>
    <el-table :data="articles" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="date" label="发布时间" width="120" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button size="small" text type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && articles.length === 0" description="暂无知识文章" />

    <el-dialog v-model="dialogVisible" title="发布农业知识" width="700px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="publishArticle">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
