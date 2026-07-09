<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getKnowledgeListAPI } from '../../api/knowledge'
import { getExpertListAPI } from '../../api/expert'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('knowledge')
const knowledgeArticles = ref([])
const experts = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [knowRes, expertRes] = await Promise.allSettled([
      getKnowledgeListAPI(),
      getExpertListAPI()
    ])
    if (knowRes.status === 'fulfilled' && knowRes.value?.code === 200) {
      knowledgeArticles.value = (knowRes.value.data || []).map(k => ({
        id: k.knowledgeId, title: k.title, content: k.content, author: k.ownName || '平台',
        date: k.createTime ? k.createTime.substring(0,10) : '-'
      }))
    }
    if (expertRes.status === 'fulfilled' && expertRes.value?.code === 200) {
      experts.value = (expertRes.value.data || []).map(e => ({
        name: e.realName || e.userName, specialty: e.specialty || '农学',
        org: e.org || '农科院', title: e.title || '专家', phone: e.phone || ''
      }))
    }
  } catch(e) {}
  finally { loading.value = false }
})

function askExpert(expertName) {
  if (!userStore.isLoggedIn) return router.push('/login')
  router.push({ path: '/profile/my-qa', query: { expert: expertName } })
}

function reserveExpert(expertName) {
  if (!userStore.isLoggedIn) return router.push('/login')
  router.push({ path: '/profile/my-appointments', query: { expert: expertName } })
}
</script>

<template>
  <div class="expert-page">
    <div class="page-banner">
      <h1>专家助力</h1>
      <p>汇聚农业专家，提供知识科普、在线咨询与实地指导服务</p>
    </div>

    <div class="page-body">
      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" class="expert-tabs">
        <!-- 农业知识 -->
        <el-tab-pane label="农业知识" name="knowledge">
          <div class="knowledge-list" v-loading="loading">
            <div v-for="art in knowledgeArticles" :key="art.id" class="article-card">
              <h3>{{ art.title }}</h3>
              <p class="article-meta">{{ art.author }} · {{ art.date }}</p>
              <p class="article-summary">{{ art.content ? art.content.substring(0, 200) + '...' : '' }}</p>
            </div>
            <el-empty v-if="!loading && knowledgeArticles.length === 0" description="暂无知识内容" />
          </div>
        </el-tab-pane>

        <!-- 农业问答 -->
        <el-tab-pane label="农业问答" name="qa">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-card shadow="never">
                <template #header><strong>咨询专家</strong></template>
                <p style="color:#666;margin-bottom:16px">向农业专家提问，获取专业解答</p>
                <el-button type="primary" @click="!userStore.isLoggedIn ? router.push('/login') : router.push('/profile/my-qa')">
                  去咨询
                </el-button>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never">
                <template #header><strong>预约专家</strong></template>
                <p style="color:#666;margin-bottom:16px">预约专家进行实地技术指导</p>
                <el-button type="primary" @click="!userStore.isLoggedIn ? router.push('/login') : router.push('/profile/my-appointments')">
                  去预约
                </el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 专家介绍 -->
        <el-tab-pane label="专家介绍" name="experts">
          <div class="expert-grid" v-loading="loading">
            <div v-for="ex in experts" :key="ex.name" class="expert-card">
              <div class="expert-avatar">👨‍🏫</div>
              <div class="expert-detail">
                <h4>{{ ex.name }}</h4>
                <p>专业：{{ ex.specialty }}</p>
                <p>单位：{{ ex.org }}</p>
                <p>职称：{{ ex.title }}</p>
              </div>
              <div class="expert-actions">
                <el-button size="small" type="primary" @click="askExpert(ex.name)">咨询</el-button>
                <el-button size="small" @click="reserveExpert(ex.name)">预约</el-button>
              </div>
            </div>
            <el-empty v-if="!loading && experts.length === 0" description="暂无专家信息" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.expert-page { padding-bottom: 40px; }
.page-banner { background: linear-gradient(135deg, #3a7d44, #5d9e5e); color: #fff; padding: 40px 24px; text-align: center; }
.page-banner h1 { font-size: 28px; margin-bottom: 8px; }
.page-banner p { font-size: 16px; opacity: 0.9; }
.page-body { max-width: 1200px; margin: 0 auto; padding: 24px; }
.expert-tabs { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }

.knowledge-list { display: flex; flex-direction: column; gap: 16px; }
.article-card { background: #f9fafb; padding: 20px; border-radius: 6px; border: 1px solid #eee; }
.article-card h3 { font-size: 17px; color: #222; margin-bottom: 6px; }
.article-meta { font-size: 12px; color: #999; margin-bottom: 8px; }
.article-summary { font-size: 14px; color: #555; line-height: 1.6; }

.expert-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
@media (max-width: 900px) { .expert-grid { grid-template-columns: repeat(2, 1fr); } }
.expert-card { background: #f9fafb; border: 1px solid #eee; border-radius: 8px; display: flex; align-items: center; padding: 16px; gap: 16px; }
.expert-avatar { font-size: 40px; flex-shrink: 0; }
.expert-detail { flex: 1; }
.expert-detail h4 { font-size: 16px; color: #222; margin-bottom: 4px; }
.expert-detail p { font-size: 13px; color: #666; line-height: 1.5; margin: 0; }
.expert-actions { display: flex; flex-direction: column; gap: 6px; flex-shrink: 0; }
</style>
