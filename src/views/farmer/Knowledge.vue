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
  } catch (e) {
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
  <div>
    <h2 class="page-title">农业知识</h2>
    <div v-loading="loading" class="agri-grid">
      <div v-for="article in articles" :key="article.id" class="agri-card" @click="openArticle(article)">
        <div class="agri-img">
          <span>🌾</span>
        </div>
        <div class="agri-text">
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

    <!-- 文章详情弹窗 -->
    <el-dialog v-model="dialogVisible" :title="currentArticle?.title" width="700px" class="article-dialog">
      <div v-if="currentArticle" class="article-content">
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
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #222;
  margin: 0 0 20px;
}
.agri-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}
@media (max-width: 800px) {
  .agri-grid { grid-template-columns: 1fr; }
}
.agri-card {
  background: #fff;
  display: flex;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px #eee;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.agri-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.agri-img {
  width: 200px;
  min-height: 150px;
  background: #eef8e9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  flex-shrink: 0;
}
.agri-text {
  padding: 16px 20px;
  flex: 1;
}
.agri-title {
  font-size: 17px;
  margin-bottom: 8px;
  color: #222;
  font-weight: 600;
}
.agri-content {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.agri-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 13px;
  color: #999;
}

.article-dialog .article-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
.article-dialog .article-body {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  font-size: 15px;
}
</style>
