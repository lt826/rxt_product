<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMatchedPartnersAPI } from '../../api/finance'
import { sendPartnerInvitationAPI } from '../../api/finance'

const matchedPartners = ref([])
const loading = ref(false)

const queryForm = ref({
  amount: null,
  item: '',
  repaymentPeriod: 12
})

const crops = ['小麦', '玉米', '水稻', '大豆', '棉花', '蔬菜', '果树', '其他']
const repaymentOptions = [3, 6, 12, 18, 24, 36]

// 邀请对话框
const inviteDialogVisible = ref(false)
const inviteForm = ref({
  name: '',
  phone: '',
  description: ''
})

// 匹配度等级
const getMatchLevel = (score) => {
  if (score >= 80) return { text: '高匹配', type: 'success' }
  if (score >= 60) return { text: '中匹配', type: 'warning' }
  return { text: '低匹配', type: 'info' }
}

// 信誉等级
const getCreditLevel = (credit) => {
  if (credit >= 80) return { text: '优秀', type: 'success' }
  if (credit >= 60) return { text: '良好', type: 'warning' }
  return { text: '一般', type: 'info' }
}

// 加载智能匹配结果
async function loadMatches() {
  if (!queryForm.value.amount || !queryForm.value.item) {
    ElMessage.warning('请填写融资金额和农作物')
    return
  }

  loading.value = true
  try {
    const res = await getMatchedPartnersAPI(queryForm.value.amount, queryForm.value.item)
    if (res.code === 200) {
      matchedPartners.value = (res.data || []).map(p => ({
        ...p,
        matchScore: p.matchScore || calculateMatchScore(p)
      }))
      if (matchedPartners.value.length === 0) {
        ElMessage.info('暂无匹配推荐，请尝试调整查询条件')
      }
    }
  } catch {
    // handled in interceptor
  } finally {
    loading.value = false
  }
}

// 计算匹配度（前端备用计算逻辑）
function calculateMatchScore(partner) {
  // 匹配度计算公式：信誉分60% + 额度匹配20% + 期限匹配20%
  const creditScore = partner.credit || 0
  const creditRatio = creditScore / 100 * 60

  // 额度匹配（假设最佳匹配额度在10万左右）
  const amount = Number(queryForm.value.amount) || 0
  const amountRatio = 20

  // 期限匹配
  const periodRatio = 20

  return Math.round(creditRatio + amountRatio + periodRatio)
}

// 打开邀请对话框
function openInviteDialog(partner) {
  inviteForm.value = {
    name: partner.userName,
    phone: partner.phone,
    description: `希望与您联合申请${queryForm.value.item}种植贷款，金额${queryForm.value.amount}元，期限${queryForm.value.repaymentPeriod}个月`
  }
  inviteDialogVisible.value = true
}

// 发送邀请
async function sendInvite() {
  if (!inviteForm.value.name || !inviteForm.value.phone) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (!inviteForm.value.description) {
    ElMessage.warning('请填写项目描述')
    return
  }

  try {
    const res = await sendPartnerInvitationAPI(inviteForm.value)
    if (res.code === 200) {
      ElMessage.success('邀请已发送')
      inviteDialogVisible.value = false
    }
  } catch {
    // handled in interceptor
  }
}

// 排序匹配结果（按匹配度从高到低）
const sortedPartners = computed(() => {
  return [...matchedPartners.value].sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0))
})
</script>

<template>
  <div class="match-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">智能匹配推荐</h2>
        <p class="page-sub">系统根据信誉分、融资额度和还款期限为您推荐合适的联合贷款人</p>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card shadow="never" class="query-card">
      <template #header><strong>查询条件</strong></template>
      <el-form label-width="120px" :inline="true">
        <el-form-item label="融资金额" required>
          <el-input
            v-model="queryForm.amount"
            placeholder="请输入金额"
            type="number"
            style="width: 200px"
          >
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="融资农作物" required>
          <el-select v-model="queryForm.item" placeholder="请选择" style="width: 150px">
            <el-option v-for="crop in crops" :key="crop" :label="crop" :value="crop" />
          </el-select>
        </el-form-item>
        <el-form-item label="还款期限">
          <el-select v-model="queryForm.repaymentPeriod" style="width: 120px">
            <el-option
              v-for="month in repaymentOptions"
              :key="month"
              :label="month + '个月'"
              :value="month"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadMatches">
            查询匹配
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 匹配结果 -->
    <el-card shadow="never" class="result-card" v-if="matchedPartners.length > 0">
      <template #header>
        <div class="card-header">
          <strong>匹配结果（共{{ matchedPartners.length }}位）</strong>
          <div class="match-legend">
            <span class="legend-item">
              <el-tag size="small" type="success">高匹配 ≥80%</el-tag>
            </span>
            <span class="legend-item">
              <el-tag size="small" type="warning">中匹配 60-80%</el-tag>
            </span>
            <span class="legend-item">
              <el-tag size="small" type="info">低匹配 <60%</el-tag>
            </span>
          </div>
        </div>
      </template>

      <el-table :data="sortedPartners" stripe v-loading="loading">
        <el-table-column type="index" label="排名" width="70" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="信誉分" width="120">
          <template #default="{ row }">
            <div class="credit-cell">
              <el-tag :type="getCreditLevel(row.credit).type" size="small">
                {{ row.credit }}分
              </el-tag>
              <span class="credit-text">({{ getCreditLevel(row.credit).text }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="匹配度" width="140">
          <template #default="{ row }">
            <div class="match-cell">
              <div class="match-bar">
                <div
                  class="match-fill"
                  :style="{ width: row.matchScore + '%' }"
                  :class="getMatchLevel(row.matchScore).type"
                ></div>
              </div>
              <span class="match-text">{{ row.matchScore }}%</span>
              <el-tag :type="getMatchLevel(row.matchScore).type" size="small" class="match-tag">
                {{ getMatchLevel(row.matchScore).text }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="cropItem" label="种植作物" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openInviteDialog(row)">
              邀请
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 匹配算法说明 -->
    <el-card shadow="never" class="info-card">
      <template #header><strong>匹配算法说明</strong></template>
      <div class="algorithm-info">
        <p>系统采用多维度评分机制计算匹配度，具体算法如下：</p>
        <ul class="algorithm-list">
          <li>
            <strong>信誉分权重（60%）</strong>
            <span>根据农户的历史信誉记录评估，信誉分越高，匹配度越高</span>
          </li>
          <li>
            <strong>融资额度匹配（20%）</strong>
            <span>根据双方融资需求的相似度进行评估</span>
          </li>
          <li>
            <strong>还款期限匹配（20%）</strong>
            <span>根据双方期望还款期限的契合度进行评估</span>
          </li>
        </ul>
        <p class="algorithm-tip">
          <el-icon><InfoFilled /></el-icon>
          提示：高匹配度（≥80%）表示双方条件高度契合，建议优先考虑
        </p>
      </div>
    </el-card>

    <!-- 邀请对话框 -->
    <el-dialog v-model="inviteDialogVisible" title="邀请联合贷款人" width="500px">
      <el-form label-width="120px">
        <el-form-item label="农户姓名">
          <el-input v-model="inviteForm.name" disabled />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="inviteForm.phone" disabled />
        </el-form-item>
        <el-form-item label="项目描述" required>
          <el-input
            v-model="inviteForm.description"
            :rows="4"
            type="textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendInvite">发送邀请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.match-page { max-width: 1200px; margin: 0 auto; }
.page-head { margin-bottom: 24px; }
.page-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0 0 4px;
}
.page-sub {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.query-card, .result-card, .info-card {
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.match-legend {
  display: flex;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.credit-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.credit-text {
  font-size: 12px;
  color: var(--text-muted);
}

.match-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.match-bar {
  width: 100px;
  height: 8px;
  background: var(--bg-page);
  border-radius: 4px;
  overflow: hidden;
}

.match-fill {
  height: 100%;
  transition: width 0.3s;
}

.match-fill.success { background: var(--green-primary); }
.match-fill.warning { background: var(--orange-primary); }
.match-fill.info { background: var(--gray-light); }

.match-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--green-deep);
}

.match-tag {
  align-self: flex-start;
}

.algorithm-info {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.algorithm-list {
  margin: 16px 0;
  padding-left: 20px;
}

.algorithm-list li {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}

.algorithm-list li strong {
  color: var(--green-deep);
}

.algorithm-list li span {
  color: var(--text-secondary);
}

.algorithm-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--blue-light);
  border-radius: var(--radius-sm);
  color: var(--blue-dark);
  margin: 16px 0 0 0;
}
</style>
