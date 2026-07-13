<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ChatDotRound } from '@element-plus/icons-vue'
import { getGoodsListAPI } from '../../api/goods'
import { claimAPI, getMyClaimsAPI } from '../../api/claim'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const me = userStore.userInfo?.userName || ''

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const myClaimIds = ref([])
const detailVisible = ref(false)
const detailItem = ref(null)

async function loadList() {
  loading.value = true
  try {
    const res = await getGoodsListAPI({ type: '2', keyword: keyword.value })
    if (res && res.code === 200) {
      // 过滤掉自己发的需求（农户不接自己的）
      list.value = (res.data || []).filter(g => g.ownName && g.ownName !== me)
    } else {
      list.value = []
    }
  } catch (e) {
    ElMessage.error('获取求购需求失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

async function loadMyClaims() {
  try {
    const res = await getMyClaimsAPI()
    if (res && res.code === 200) {
      myClaimIds.value = (res.data || []).map(c => c.demandId)
    }
  } catch { /* ignore */ }
}

function isClaimed(item) {
  return myClaimIds.value.includes(item.goodsId)
}

function openDetail(item) {
  detailItem.value = item
  detailVisible.value = true
}

async function doClaim(item) {
  try {
    await ElMessageBox.confirm(
      `确认接取求购【${item.title}】？接取后将向发布者发起对话。`,
      '接取确认',
      { type: 'warning', confirmButtonText: '接取并联系', cancelButtonText: '取消' }
    )
  } catch { return }
  try {
    const res = await claimAPI({ demandId: item.goodsId })
    if (res && res.code === 200) {
      ElMessage.success('接取成功，已为你们建立对话')
      myClaimIds.value = [...myClaimIds.value, item.goodsId]
      detailVisible.value = false
      router.push('/farmer/messages?to=' + encodeURIComponent(item.ownName))
    } else {
      ElMessage.error(res?.message || '接取失败')
    }
  } catch (e) {
    ElMessage.error('接取失败，请重试')
  }
}

function goChat(item) {
  router.push('/farmer/messages?to=' + encodeURIComponent(item.ownName))
}

function fmtDate(t) {
  return t ? String(t).replace('T', ' ').substring(0, 10) : '-'
}

onMounted(() => {
  loadList()
  loadMyClaims()
})
</script>

<template>
  <div class="demands-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">求购大厅</h2>
        <p class="page-sub">浏览买家的求购需求，接取后即可与对方私聊细谈</p>
      </div>
      <el-input
        v-model="keyword"
        placeholder="搜索农产品名..."
        clearable
        class="search-input"
        @keyup.enter="loadList"
        @clear="loadList"
      >
        <template #append>
          <el-button @click="loadList"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
    </div>

    <div v-loading="loading" class="demand-grid">
      <div v-for="item in list" :key="item.goodsId" class="demand-card">
        <div class="card-body" @click="openDetail(item)">
          <div class="card-title-row">
            <span class="card-title">{{ item.title }}</span>
            <span class="card-price">{{ item.price ? '¥' + item.price : '面议' }}</span>
          </div>
          <div class="card-content">{{ item.content }}</div>
          <div class="card-meta">
            <span>发布者：{{ item.ownName }}</span>
            <span v-if="item.address">{{ item.address }}</span>
            <span>{{ fmtDate(item.createTime) }}</span>
          </div>
        </div>
        <div class="card-actions">
          <el-button v-if="!isClaimed(item)" type="primary" @click="doClaim(item)">接取并联系</el-button>
          <el-button v-else type="success" plain @click="goChat(item)">
            <el-icon><ChatDotRound /></el-icon>&nbsp;已接取·去聊天
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && list.length === 0" description="暂无求购需求" />

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="求购需求详情" width="560px">
      <template v-if="detailItem">
        <div class="detail-row"><span class="detail-label">农产品</span><strong>{{ detailItem.title }}</strong></div>
        <div class="detail-row"><span class="detail-label">期望价</span><span class="price-tag">{{ detailItem.price ? '¥' + detailItem.price : '面议' }}</span></div>
        <div class="detail-row"><span class="detail-label">发布者</span>{{ detailItem.ownName }}</div>
        <div class="detail-row" v-if="detailItem.address"><span class="detail-label">收货地</span>{{ detailItem.address }}</div>
        <div class="detail-row"><span class="detail-label">详情</span>{{ detailItem.content }}</div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detailItem && !isClaimed(detailItem)" type="primary" @click="doClaim(detailItem)">接取并联系</el-button>
        <el-button v-else-if="detailItem" type="success" @click="goChat(detailItem)">去聊天</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.demands-page { max-width: 1200px; }
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.search-input { width: 320px; }

.demand-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
@media (max-width: 900px) { .demand-grid { grid-template-columns: 1fr; } }

.demand-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  transition: all 0.25s;
  display: flex;
  flex-direction: column;
}
.demand-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.card-body { padding: 16px; cursor: pointer; flex: 1; }
.card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; gap: 12px; }
.card-title { font-size: 16px; font-weight: 600; color: var(--text); }
.card-price { color: var(--amber); font-weight: 700; font-size: 16px; white-space: nowrap; }
.card-content {
  font-size: 13px; color: var(--text-secondary); line-height: 1.6; margin-bottom: 10px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.card-meta { display: flex; flex-wrap: wrap; gap: 12px; font-size: 12px; color: var(--text-muted); }
.card-actions { padding: 12px 16px; border-top: 1px solid var(--border); }
.card-actions .el-button { width: 100%; }

.detail-row { margin-bottom: 12px; font-size: 14px; color: var(--text); }
.detail-label { display: inline-block; width: 70px; color: var(--text-muted); }
.price-tag { color: var(--amber); font-weight: 700; }
</style>
