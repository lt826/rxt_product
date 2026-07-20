<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import request from '../../api/index'

const router = useRouter()
const userStore = useUserStore()
const userName = computed(() => userStore.nickName || userStore.userInfo?.userName || '买家')

const loading = ref(false)
const stats = ref([
  { label: '在售商品', value: 0, icon: 'Goods', color: '#409eff', bg: 'linear-gradient(135deg, #409eff, #79b8ff)' },
  { label: '收购需求', value: 0, icon: 'Document', color: '#67c23a', bg: 'linear-gradient(135deg, #67c23a, #95d675)' },
  { label: '待联系农户', value: 0, icon: 'User', color: '#e6a23c', bg: 'linear-gradient(135deg, #e6a23c, #f7d08e)' }
])

const matches = ref([])

async function loadDashboard() {
  loading.value = true
  try {
    const [statsRes, matchesRes] = await Promise.allSettled([
      request({ url: '/api/v1/buyer/stats', method: 'get' }),
      request({ url: '/api/v1/buyer/matches', method: 'get' })
    ])

    if (statsRes.value?.code === 200) {
      const data = statsRes.value.data || {}
      stats.value = [
        { label: '在售商品', value: data.productCount || 0, icon: 'Goods', color: '#409eff', bg: 'linear-gradient(135deg, #409eff, #79b8ff)' },
        { label: '收购需求', value: data.demandCount || 0, icon: 'Document', color: '#67c23a', bg: 'linear-gradient(135deg, #67c23a, #95d675)' },
        { label: '待联系农户', value: data.farmerCount || 0, icon: 'User', color: '#e6a23c', bg: 'linear-gradient(135deg, #e6a23c, #f7d08e)' }
      ]
    }

    if (matchesRes.value?.code === 200) {
      matches.value = (matchesRes.value.data || []).map(m => ({
        product: m.product || '-',
        amount: m.amount ? (m.amount.length > 30 ? m.amount.substring(0, 30) + '…' : m.amount) : '-',
        farmer: m.farmer || '-',
        status: m.status || '待确认'
      }))
    }
  } catch (e) {
    console.error('加载首页数据失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <div class="dashboard">
    <!-- Welcome -->
    <div class="dash-welcome">
      <div>
        <h2 class="welcome-greeting">买家首页</h2>
        <p class="welcome-sub">你好，{{ userName }}，以下是你的业务概览</p>
      </div>
      <div style="display:flex;gap:8px">
        <el-button @click="router.push('/buyer/demand')">发布收购需求</el-button>
        <el-button type="primary" @click="router.push('/buyer/products')">浏览市场 →</el-button>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-grid" v-loading="loading">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-gradient" :style="{ background: s.bg }">
          <el-icon :size="24" color="#fff"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- Matches table -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header-row">
          <span>最近收购需求匹配</span>
          <el-button text type="primary" size="small" @click="router.push('/buyer/demand')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="matches" stripe v-loading="loading">
        <el-table-column prop="product" label="农产品" min-width="160" />
        <el-table-column label="需求详情" min-width="240">
          <template #default="{ row }">
            <span class="ellipsis">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="farmer" label="意向农户" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已匹配' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default>
            <el-button text type="primary" size="small" @click="router.push('/buyer/contacts')">联系</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!matches.length && !loading" description="暂无匹配数据，请先发布收购需求">
        <el-button type="primary" @click="router.push('/buyer/demand')">发布需求</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1200px; }

.dash-welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}
.welcome-greeting { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.welcome-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}
@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
.stat-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  transition: all 0.25s;
}
.stat-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.stat-gradient { padding: 16px; display: flex; justify-content: center; }
.stat-body { padding: 16px 20px 20px; text-align: center; }
.stat-number { font-size: 32px; font-weight: 800; line-height: 1; color: var(--text); }
.stat-label { margin-top: 6px; font-size: 13px; color: var(--text-muted); }

.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.ellipsis { display: inline-block; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
