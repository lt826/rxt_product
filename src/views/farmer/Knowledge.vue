<template>
  <div>
    <h2 class="page-title">农业知识</h2>
    <el-row :gutter="16">
      <el-col v-for="article in articles" :key="article.id" :span="12" style="margin-bottom:16px">
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
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const articles = [
  { id: 1, title: '水稻高产种植技术要点', desc: '介绍水稻从育苗到收割的关键技术', author: '王教授', date: '2026-07-05' },
  { id: 2, title: '有机蔬菜病虫害防治', desc: '不使用化学农药的病虫害防治方法', author: '李专家', date: '2026-07-03' },
  { id: 3, title: '农产品电商运营指南', desc: '如何在电商平台销售农产品', author: '张老师', date: '2026-06-28' },
  { id: 4, title: '农业融资常见问题解答', desc: '农民申请农业贷款需要注意什么', author: '农信社', date: '2026-06-20' }
]
function openArticle(a) { ElMessage.info(`打开文章：${a.title}`) }
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
h4 { margin: 0 0 8px; font-size: 16px; color: #303133; }
.article-desc { color: #606266; font-size: 14px; margin: 0 0 12px; line-height: 1.5; }
.article-meta { display: flex; justify-content: space-between; color: #909399; font-size: 13px; }
.el-card { cursor: pointer; }
</style>
