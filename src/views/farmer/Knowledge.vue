<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listKnowledge, knowledgeDetail, publishDiscuss } from '../../api/knowledge'

const loading = ref(false)
const keyword = ref('')
const list = ref([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const discusses = ref([])
const commentText = ref('')
const commenting = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await listKnowledge(keyword.value)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function openArticle(id) {
  detailVisible.value = true
  detailLoading.value = true
  commentText.value = ''
  try {
    const res = await knowledgeDetail(id)
    detail.value = res.data.knowledge
    discusses.value = res.data.discusses || []
  } finally {
    detailLoading.value = false
  }
}

async function submitComment() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commenting.value = true
  try {
    const res = await publishDiscuss({ knowledgeId: detail.value.knowledgeId, content: commentText.value })
    discusses.value.push(res.data)
    commentText.value = ''
    ElMessage.success('评论发表成功')
  } finally {
    commenting.value = false
  }
}

function fmt(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

loadData()
</script>

<template>
  <div v-loading="loading">
    <div class="page-head">
      <h2 class="page-title">农业知识</h2>
      <el-input v-model="keyword" placeholder="搜索标题或内容" clearable style="width: 240px" @keyup.enter="loadData" @clear="loadData">
        <template #append><el-button :icon="'Search'" @click="loadData" /></template>
      </el-input>
    </div>

    <el-row :gutter="16">
      <el-col v-for="a in list" :key="a.knowledgeId" :xs="24" :sm="12" style="margin-bottom: 16px">
        <el-card shadow="hover" class="k-card" @click="openArticle(a.knowledgeId)">
          <el-image v-if="a.picPath" :src="a.picPath" class="k-cover" fit="cover" />
          <h4 class="k-title">{{ a.title }}</h4>
          <p class="k-desc">{{ a.content }}</p>
          <div class="k-meta">
            <span><el-icon><User /></el-icon>{{ a.ownName }}</span>
            <span>{{ fmt(a.createTime) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length && !loading" description="暂无农业知识" />

    <el-drawer v-model="detailVisible" title="知识详情" size="560px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-image v-if="detail.picPath" :src="detail.picPath" class="k-cover-lg" fit="cover" />
          <h3 class="d-title">{{ detail.title }}</h3>
          <div class="d-meta">
            <span><el-icon><User /></el-icon>{{ detail.ownName }}</span>
            <span>{{ fmt(detail.createTime) }}</span>
          </div>
          <div class="d-content">{{ detail.content }}</div>

          <el-divider content-position="left">评论留言</el-divider>
          <el-input v-model="commentText" type="textarea" :rows="3" placeholder="发表技术评论或提问…" />
          <el-button type="primary" size="small" style="margin-top: 8px" :loading="commenting" @click="submitComment">发表评论</el-button>

          <div class="comment-list">
            <el-empty v-if="!discusses.length" description="还没有评论" :image-size="60" />
            <div v-for="d in discusses" :key="d.discussId" class="comment-item">
              <div class="c-head"><span class="c-name">{{ d.ownName }}</span><span class="c-time">{{ fmt(d.createTime) }}</span></div>
              <div class="c-body">{{ d.content }}</div>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0; }
.k-card { cursor: pointer; height: 100%; }
.k-cover { width: 100%; height: 140px; border-radius: 6px; margin-bottom: 12px; }
.k-title { margin: 0 0 8px; font-size: 16px; color: #303133; }
.k-desc { color: #606266; font-size: 14px; margin: 0 0 12px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.k-meta { display: flex; justify-content: space-between; color: #909399; font-size: 13px; align-items: center; }
.k-meta span { display: flex; align-items: center; gap: 4px; }
.k-cover-lg { width: 100%; height: 200px; border-radius: 6px; margin-bottom: 16px; }
.d-title { font-size: 20px; color: #303133; margin: 0 0 8px; }
.d-meta { display: flex; gap: 16px; color: #909399; font-size: 13px; margin-bottom: 16px; }
.d-meta span { display: flex; align-items: center; gap: 4px; }
.d-content { color: #303133; font-size: 15px; line-height: 1.8; white-space: pre-wrap; }
.comment-list { margin-top: 20px; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.comment-item:last-child { border: none; }
.c-head { display: flex; justify-content: space-between; margin-bottom: 6px; }
.c-name { font-weight: 500; color: #303133; font-size: 14px; }
.c-time { color: #c0c4cc; font-size: 12px; }
.c-body { color: #606266; font-size: 14px; line-height: 1.6; }
</style>
