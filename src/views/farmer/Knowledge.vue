<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getKnowledgeListAPI } from '../../api/knowledge'

const articles = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getKnowledgeListAPI()
    if (res.code === 200) {
      articles.value = (res.data || []).map(a => ({
        id: a.knowledgeId,
        title: a.title,
        content: a.content,
        desc: a.content ? a.content.substring(0, 60) + '...' : '',
        author: a.ownName || '平台',
        date: a.createTime ? a.createTime.substring(0, 10) : '-'
      }))
    }
  } catch (e) {
    ElMessage.error('获取知识列表失败')
  } finally {
    loading.value = false
  }
})

function openArticle(a) {
  ElMessage.info(`打开文章：${a.title}`)
}
</script>

<template>
  <div>
    <h2 class="page-title">农业知识</h2>
    <el-row :gutter="16">
      <el-col v-for="article in articles" :key="article.id" :xs="24" :sm="12" style="margin-bottom:16px">
        <el-card shadow="hover" @click="openArticle(article)">
          <h4>{{ article.title }}</h4>
          <p class="article-desc">{{ article.desc }}</p>
          <div class="article-meta">
            <span>{{ article.author }}</span>
            <span>{{ article.date }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && articles.length === 0" description="暂无知识文章" />
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
h4 { margin: 0 0 8px; font-size: 16px; color: #303133; }
.article-desc { color: #606266; font-size: 14px; margin: 0 0 12px; line-height: 1.5; }
.article-meta { display: flex; justify-content: space-between; color: #909399; font-size: 13px; }
.el-card { cursor: pointer; }
</style>
