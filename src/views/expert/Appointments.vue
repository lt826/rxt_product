<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { listReservesForExpert, handleReserve } from '../../api/reserve'

const loading = ref(false)
const list = ref([])
const activeFilter = ref('pending')
const detail = ref(null)
const detailVisible = ref(false)

const handleForm = ref({ id: null, answer: '' })
const handleVisible = ref(false)
const submitting = ref(false)

async function loadList() {
  loading.value = true
  try {
    const res = await listReservesForExpert()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const filtered = computed(() =>
  list.value.filter((r) => {
    if (activeFilter.value === 'pending') return r.status === 0
    if (activeFilter.value === 'accepted') return r.status === 1
    if (activeFilter.value === 'rejected') return r.status === 2
    return true
  })
)

function statusInfo(s) {
  if (s === 1) return { text: '已接受', type: 'success' }
  if (s === 2) return { text: '已拒绝', type: 'info' }
  return { text: '待处理', type: 'warning' }
}

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

function openHandle(row) {
  handleForm.value = { id: row.id, answer: row.answer || '' }
  detailVisible.value = false
  handleVisible.value = true
}

async function submitHandle(status) {
  if (!handleForm.value.answer && status === 1) {
    handleForm.value.answer = '已接受，请保持电话畅通，近期上门实地指导。'
  }
  submitting.value = true
  try {
    await handleReserve({ id: handleForm.value.id, answer: handleForm.value.answer, status })
    ElMessage.success(status === 1 ? '已接受预约' : '已拒绝预约')
    handleVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

loadList()
</script>

<template>
  <div>
    <h2 class="page-title">预约处理</h2>

    <el-radio-group v-model="activeFilter" style="margin-bottom: 16px">
      <el-radio-button label="pending">待处理</el-radio-button>
      <el-radio-button label="accepted">已接受</el-radio-button>
      <el-radio-button label="rejected">已拒绝</el-radio-button>
      <el-radio-button label="all">全部</el-radio-button>
    </el-radio-group>

    <el-card v-loading="loading">
      <template #header><span>预约请求</span></template>
      <el-empty v-if="!filtered.length" description="暂无预约" />

      <div v-for="r in filtered" :key="r.id" class="apt-card">
        <div class="apt-main" @click="openDetail(r)">
          <div class="apt-farmer">
            <el-icon><User /></el-icon>{{ r.questioner }}
            <span class="apt-phone" v-if="r.phone">· {{ r.phone }}</span>
          </div>
          <div class="apt-line">
            <el-tag size="small" type="info">{{ r.plantName }}</el-tag>
            <span class="apt-area">面积：{{ r.area }}</span>
          </div>
          <div class="apt-line muted">{{ r.address }}</div>
          <div class="apt-line muted" v-if="r.message">留言：{{ r.message }}</div>
        </div>
        <div class="apt-side">
          <el-tag :type="statusInfo(r.status).type">{{ statusInfo(r.status).text }}</el-tag>
          <div v-if="r.status === 0" class="apt-actions">
            <el-button size="small" type="primary" @click.stop="openHandle(r)">处理</el-button>
          </div>
          <div v-else-if="r.answer" class="apt-answer">回复：{{ r.answer }}</div>
        </div>
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="预约详情" size="460px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="农户">{{ detail.questioner }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detail.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作物">{{ detail.plantName }}</el-descriptions-item>
          <el-descriptions-item label="面积">{{ detail.area }}</el-descriptions-item>
          <el-descriptions-item label="土地地址">{{ detail.address }}</el-descriptions-item>
          <el-descriptions-item label="土壤条件">{{ detail.soilCondition }}</el-descriptions-item>
          <el-descriptions-item label="作物条件">{{ detail.plantCondition }}</el-descriptions-item>
          <el-descriptions-item label="作物详情">{{ detail.plantDetail }}</el-descriptions-item>
          <el-descriptions-item label="留言">{{ detail.message || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusInfo(detail.status).type">{{ statusInfo(detail.status).text }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.answer" label="专家回复">{{ detail.answer }}</el-descriptions-item>
        </el-descriptions>
        <el-button v-if="detail.status === 0" type="primary" style="margin-top: 16px" @click="openHandle(detail)">处理该预约</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="handleVisible" title="处理预约" width="520px">
      <el-form label-width="80px">
        <el-form-item label="回复意向">
          <el-input v-model="handleForm.answer" type="textarea" :rows="4" placeholder="给农户的回复，例如上门时间、建议等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitHandle(2)">拒绝</el-button>
        <el-button type="primary" :loading="submitting" @click="submitHandle(1)">接受</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #222; margin: 0 0 20px; }
.apt-card { display: flex; gap: 16px; padding: 16px 0; border-bottom: 1px solid #ebeef5; align-items: flex-start; }
.apt-card:last-child { border: none; }
.apt-main { flex: 1; cursor: pointer; }
.apt-farmer { display: flex; align-items: center; gap: 6px; font-weight: 500; color: #303133; margin-bottom: 8px; }
.apt-phone { color: #909399; font-weight: normal; }
.apt-line { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; font-size: 14px; color: #606266; }
.apt-area { color: #606266; font-size: 13px; }
.muted { color: #909399; font-size: 13px; }
.apt-side { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; flex-shrink: 0; }
.apt-actions { display: flex; gap: 8px; }
.apt-answer { font-size: 12px; color: #909399; max-width: 160px; }
</style>
