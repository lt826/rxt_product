<script setup>
import { ref, onMounted } from 'vue'
import { getMyKnowledgeAPI, deleteKnowledgeAPI } from '../../api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'

// 专家端：查看自己发布的知识文章（发布在专家主页面进行）
const articles = ref([])
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await getMyKnowledgeAPI()
    if (res.code === 200) {
      articles.value = (res.data || []).map(k => ({
        id: k.knowledgeId, title: k.title, content: k.content, date: k.createTime?.substring(0, 10) || '-'
      }))
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(loadData)

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？`, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  } catch (e) { return }
  try {
    const res = await deleteKnowledgeAPI(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  } catch (e) { /* 拦截器已提示 */ }
}
</script>

<template>
  <div>
    <h2 class="page-title">知识管理</h2>
    <el-table :data="articles" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
      <el-table-column prop="date" label="发布时间" width="140" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && articles.length === 0" description="暂无知识文章" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
