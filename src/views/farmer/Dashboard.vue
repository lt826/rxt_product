<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyFinancesAPI } from '../../api/finance'
import { getMyQuestionsAPI } from '../../api/question'
import { getKnowledgeListAPI } from '../../api/knowledge'
import { useUserStore } from '../../stores/user'
import ProfileCard from '../../components/ProfileCard.vue'

const userStore = useUserStore()
const stats = ref({ pendingFinance: 0, myQuestions: 0, knowledgeCount: 0 })

const userName = computed(() => userStore.userInfo.userName || '')

onMounted(async () => {
  try {
    const [finRes, qaRes, knowRes] = await Promise.allSettled([
      getMyFinancesAPI(),
      getMyQuestionsAPI(),
      getKnowledgeListAPI()
    ])
    if (finRes.status === 'fulfilled' && finRes.value.code === 200) {
      stats.value.pendingFinance = (finRes.value.data || []).filter(f => f.status === 0).length
    }
    if (qaRes.status === 'fulfilled' && qaRes.value.code === 200) {
      stats.value.myQuestions = (qaRes.value.data || []).length
    }
    if (knowRes.status === 'fulfilled' && knowRes.value.code === 200) {
      stats.value.knowledgeCount = (knowRes.value.data || []).length
    }
  } catch (e) { /* 忽略 */ }
})
</script>

<template>
  <div>
    <h2 class="page-title">工作台</h2>

    <ProfileCard v-if="userName" :user-name="userName" />

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background:#e8f5e9;color:#67c23a">💰</div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.pendingFinance }}</div>
          <div class="stat-label">待审批融资</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#e3f2fd;color:#409eff">💬</div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.myQuestions }}</div>
          <div class="stat-label">我的提问</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#fef3e2;color:#e6a23c">📖</div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.knowledgeCount }}</div>
          <div class="stat-label">知识文章</div>
        </div>
      </div>
    </div>

    <el-card>
      <template #header>快速入口</template>
      <div class="quick-links">
        <router-link to="/farmer/financing" class="quick-link">📋 融资申请</router-link>
        <router-link to="/farmer/products" class="quick-link">📦 商品浏览</router-link>
        <router-link to="/farmer/expert-qa" class="quick-link">❓ 专家问答</router-link>
        <router-link to="/farmer/appointments" class="quick-link">📅 专家预约</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #222;
  margin: 0 0 20px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 4px #eee;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #333;
}
.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 6px;
}
.quick-links {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.quick-link {
  display: block;
  padding: 16px;
  text-align: center;
  background: #f8f9fa;
  border-radius: 6px;
  color: #333;
  text-decoration: none;
  font-size: 15px;
  transition: background 0.2s;
}
.quick-link:hover {
  background: #eef8e9;
  color: #488d37;
}
</style>
