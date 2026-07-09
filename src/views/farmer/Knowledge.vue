<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getKnowledgeListAPI } from '../../api/knowledge'

const articles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentArticle = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getKnowledgeListAPI()
    if (res.code === 200) {
      articles.value = (res.data || []).map(a => ({
        id: a.knowledgeId,
        title: a.title,
        content: a.content,
        desc: a.content ? a.content.substring(0, 120) + '...' : '',
        author: a.ownName || '平台',
        date: a.createTime ? a.createTime.substring(0, 10) : '-'
      }))
    }
  } catch {
    ElMessage.error('获取知识列表失败')
  } finally {
    loading.value = false
  }
})

function openArticle(a) {
  currentArticle.value = a
  dialogVisible.value = true
}
</script>

<template>
  <div class="knowledge-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">农业知识</h2>
        <p class="page-sub">学习种植技术，提升农业水平</p>
      </div>
    </div>

    <div v-loading="loading" class="agri-grid">
      <div v-for="article in articles" :key="article.id" class="agri-card" @click="openArticle(article)">
        <div class="agri-card-body">
          <div class="agri-icon">📖</div>
          <h3 class="agri-title">{{ article.title }}</h3>
          <p class="agri-content">{{ article.desc }}</p>
          <div class="agri-meta">
            <span>{{ article.author }}</span>
            <span>{{ article.date }}</span>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && articles.length === 0" description="暂无知识文章" />

    <el-dialog v-model="dialogVisible" :title="currentArticle?.title" width="700px" destroy-on-close>
      <div v-if="currentArticle" class="article-view">
        <div class="article-meta">
          <span>发布者：{{ currentArticle.author }}</span>
          <span>发布时间：{{ currentArticle.date }}</span>
        </div>
        <div class="article-body" v-html="currentArticle.content.replace(/\n/g, '<br>')"></div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.knowledge-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.agri-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
@media (max-width: 800px) { .agri-grid { grid-template-columns: 1fr; } }

.agri-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 24px;
  cursor: pointer;
  transition: all 0.25s;
}
.agri-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.agri-card-body { display: flex; flex-direction: column; gap: 12px; }
.agri-icon { font-size: 32px; }
.agri-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0;
}
.agri-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.agri-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-muted);
}

.article-view .article-meta {
  display: flex;
  gap: 20px;
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.article-view .article-body {
  line-height: 1.8;
  color: var(--text);
  white-space: pre-wrap;
  font-size: 15px;
}
</style>
