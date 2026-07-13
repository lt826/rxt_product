<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllFinancesAPI, updateFinanceStatusAPI } from '../../api/finance'
import { useUserStore } from '../../stores/user'
import ProfileCard from '../../components/ProfileCard.vue'

const router = useRouter()
const userStore = useUserStore()
const userName = computed(() => userStore.nickName || userStore.userInfo?.userName || '')

const loading = ref(false)
const stats = reactive({ totalPending: 0, todayProcessed: 0, activeProducts: 0, monthlyTotal: '¥0' })
const pendingList = ref([])
const logs = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await getAllFinancesAPI()
    if (res.code === 200) {
      const list = res.data || []
      pendingList.value = list.filter(f => f.status === 0).map(f => ({
        financeId: f.financeId,
        ownName: f.ownName || '-',
        money: f.money ? '¥' + f.money : '-',
        createTime: f.createTime ? f.createTime.substring(0, 10) : '-',
        status: f.status,
        remark: f.remark
      }))
      stats.totalPending = pendingList.value.length
      stats.todayProcessed = list.filter(f => f.status > 0 && f.updateTime?.startsWith(new Date().toISOString().substring(0,10))).length

      const today = new Date().toISOString().substring(0, 10)
      const todayItems = list.filter(f => f.updateTime?.startsWith(today))
      logs.value = todayItems.map(f => ({
        time: (f.updateTime || '').substring(11, 16) || '--:--',
        text: f.ownName + (f.status === 1 ? ' 的融资申请已通过' : f.status === 2 ? ' 的融资申请已驳回' : ' 提交了融资申请'),
        type: f.status === 0 ? 'primary' : f.status === 1 ? 'success' : 'danger'
      }))
    }
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确认通过 ${row.ownName} 的融资申请？`, '提示')
    await updateFinanceStatusAPI(row.financeId, 1, '审核通过')
    ElMessage.success('已通过')
    loadData()
  } catch { /* cancelled */ }
}

async function handleReject(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', { confirmButtonText: '驳回', cancelButtonText: '取消' })
    await updateFinanceStatusAPI(row.financeId, 2, value || '审核不通过')
    ElMessage.success('已驳回')
    loadData()
  } catch { /* cancelled */ }
}

onMounted(loadData)
</script>

<template>
  <div class="dashboard">
    <div class="dash-welcome">
      <div>
        <h2 class="welcome-greeting">银行首页 🏦</h2>
        <p class="welcome-sub">融资审批与业务管理</p>
      </div>
      <el-button type="primary" @click="router.push('/bank/products')">产品配置 →</el-button>
    </div>

    <ProfileCard v-if="userName" :user-name="userName" class="profile-card-inline" />

    <!-- Stats -->
    <div class="stats-grid" v-loading="loading">
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #409eff, #79b8ff)">
          <el-icon :size="24" color="#fff"><DocumentChecked /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ stats.totalPending }}</div>
          <div class="stat-label">待审批申请</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #67c23a, #95d675)">
          <el-icon :size="24" color="#fff"><Timer /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ stats.todayProcessed }}</div>
          <div class="stat-label">本日处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #e6a23c, #f7d08e)">
          <el-icon :size="24" color="#fff"><Coin /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ stats.activeProducts }}</div>
          <div class="stat-label">活跃融资产品</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-gradient" style="background: linear-gradient(135deg, #f56c6c, #fba0a0)">
          <el-icon :size="24" color="#fff"><Money /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ stats.monthlyTotal }}</div>
          <div class="stat-label">本月放贷总额</div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-row">
              <span>待审批列表</span>
              <el-button text type="primary" size="small" @click="loadData">刷新</el-button>
            </div>
          </template>
          <el-table :data="pendingList" stripe v-loading="loading">
            <el-table-column prop="ownName" label="农户" width="120" />
            <el-table-column prop="money" label="金额" width="120" />
            <el-table-column prop="createTime" label="提交日期" width="130" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleApprove(row)">通过</el-button>
                <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && pendingList.length === 0" description="暂无待审批申请" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10">
        <el-card shadow="never">
          <template #header><span>今日动态</span></template>
          <el-timeline>
            <el-timeline-item v-for="log in logs" :key="log.time" :timestamp="log.time" :type="log.type" placement="top">
              <el-card shadow="hover" class="log-card">{{ log.text }}</el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="!loading && logs.length === 0" description="暂无动态" />
        </el-card>
      </el-col>
    </el-row>
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

.profile-card-inline { margin-bottom: 24px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
.stat-number { font-size: 26px; font-weight: 800; line-height: 1; color: var(--text); }
.stat-label { margin-top: 6px; font-size: 13px; color: var(--text-muted); }

.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.log-card { border-radius: var(--radius-sm); margin-bottom: 4px; }
.log-card p { font-size: 14px; color: var(--text-secondary); margin: 0; line-height: 1.5; }
</style>
