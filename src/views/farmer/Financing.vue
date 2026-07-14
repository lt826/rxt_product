<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  applyFinanceAPI,
  getFinancingProductsAPI,
  getMatchedPartnersAPI,
  sendPartnerInvitationAPI,
  getSentInvitationsAPI
} from '../../api/finance'

function getCurrentUser() {
  try {
    const user = JSON.parse(localStorage.getItem('rxt_user') || '{}')
    return user.userName || 'anonymous'
  } catch {
    return 'anonymous'
  }
}

const DRAFT_KEY = 'rxt_financing_apply_draft_' + getCurrentUser()

const products = ref([])
const matchedPartners = ref([])
const selectedProduct = ref(null)
const sentInvitations = ref([])
const loading = ref(false)
const submitting = ref(false)
const showMatchDialog = ref(false)
const matchLoading = ref(false)
const inviting = ref(false)

// 申请表单
const form = ref({
  productId: null,
  amount: null,
  repaymentPeriod: null,
  cropItem: '',
  fileInfo: ''
})

// 联合贷款人控制
const needPartner = ref(false)
const selectedMatchPartner = ref(null)

// 联合贷款人信息（手动或智能匹配填充）
const partnerForm = ref({
  name: '',
  phone: ''
})

// 已发送邀请的状态
const partnerInvitation = ref(null)

const crops = ['小麦', '玉米', '水稻', '大豆', '棉花', '蔬菜', '果树', '其他']

// ------------------ 草稿保存 ------------------
function saveDraft() {
  try {
    const draft = {
      form: { ...form.value },
      needPartner: needPartner.value,
      partnerForm: { ...partnerForm.value },
      selectedProductId: selectedProduct.value?.id || null
    }
    localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
  } catch {
  }
}

function clearDraft() {
  localStorage.removeItem(DRAFT_KEY)
}

function restoreDraft() {
  try {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return
    const draft = JSON.parse(raw)
    if (draft.form) {
      form.value.productId = draft.form.productId || null
      form.value.amount = draft.form.amount ?? null
      form.value.repaymentPeriod = draft.form.repaymentPeriod ?? null
      form.value.cropItem = draft.form.cropItem || ''
      form.value.fileInfo = draft.form.fileInfo || ''
    }
    needPartner.value = draft.needPartner || false
    if (draft.partnerForm) {
      partnerForm.value.name = draft.partnerForm.name || ''
      partnerForm.value.phone = draft.partnerForm.phone || ''
    }
    if (draft.selectedProductId) {
      const pid = draft.selectedProductId
      const found = products.value.find(p => p.id === pid)
      if (found) {
        selectedProduct.value = found
      } else {
        const unwatch = watch(products, () => {
          const p = products.value.find(p => p.id === pid)
          if (p) {
            selectedProduct.value = p
            unwatch()
          }
        })
        onBeforeUnmount(() => unwatch())
      }
    }
  } catch {
  }
}

let draftTimer = null
function autoSaveDraft() {
  if (draftTimer) clearTimeout(draftTimer)
  draftTimer = setTimeout(saveDraft, 500)
}

watch(() => ({ ...form.value, needPartner: needPartner.value, ...partnerForm.value }), autoSaveDraft, { deep: true })
window.addEventListener('beforeunload', saveDraft)

// ------------------ 数据加载 ------------------
async function loadProducts() {
  loading.value = true
  try {
    const res = await getFinancingProductsAPI()
    if (res.code === 200) {
      products.value = (res.data || []).filter(p => p.status === 1)
    }
  } catch {
    ElMessage.error('获取融资产品失败')
  } finally {
    loading.value = false
  }
}

async function loadSentInvitations() {
  try {
    const res = await getSentInvitationsAPI()
    if (res.code === 200) {
      sentInvitations.value = res.data || []
    }
  } catch {
  }
}

// ------------------ 产品选择 ------------------
function handleSelectProduct(product) {
  selectedProduct.value = product
  form.value.productId = product.id
  form.value.amount = null
  form.value.repaymentPeriod = null
}

// ------------------ 联合贷款人切换 ------------------
function handleNeedPartnerChange(val) {
  if (!val) {
    needPartner.value = false
    partnerForm.value = { name: '', phone: '' }
    selectedMatchPartner.value = null
    partnerInvitation.value = null
  } else {
    needPartner.value = true
  }
}

// ------------------ 智能匹配 ------------------
async function openMatchDialog() {
  if (!form.value.amount || !form.value.cropItem) {
    ElMessage.warning('请先填写融资金额和融资农作物')
    return
  }
  showMatchDialog.value = true
  matchLoading.value = true
  try {
    const res = await getMatchedPartnersAPI(form.value.amount, form.value.cropItem)
    if (res.code === 200) {
      // 只展示匹配度大于 0 的农户
      matchedPartners.value = (res.data || []).filter(p => (p.matchScore || 0) > 0)
    }
  } catch {
  } finally {
    matchLoading.value = false
  }
}

// 从匹配列表中选择联合贷款人
function selectMatchPartner(partner) {
  partnerForm.value = {
    name: partner.realName || partner.userName || '',
    phone: partner.phone || ''
  }
  selectedMatchPartner.value = partner
  showMatchDialog.value = false
  ElMessage.success('已选择 ' + partnerForm.value.name + ' 作为联合贷款人')
}

// 匹配度等级
function getMatchLevel(score) {
  if (score >= 80) return { text: '高匹配', type: 'success' }
  if (score >= 60) return { text: '中匹配', type: 'warning' }
  return { text: '低匹配', type: 'info' }
}

// ------------------ 发送邀请 ------------------
async function sendPartnerInvite() {
  if (!partnerForm.value.name || !partnerForm.value.phone) {
    ElMessage.warning('请先选择或填写联合贷款人信息')
    return
  }

  inviting.value = true
  try {
    const description = '希望与您联合申请' + form.value.cropItem + '种植贷款，金额' + form.value.amount + '元'
    const res = await sendPartnerInvitationAPI({
      inviteeName: partnerForm.value.name,
      inviteePhone: partnerForm.value.phone,
      projectDescription: description
    })
    if (res.code === 200) {
      ElMessage.success('邀请已发送，等待对方确认')
      await loadSentInvitations()
      // 查找刚发送的邀请（在我发送的列表中对方姓名在 applicantRealName 字段）
      const newInv = sentInvitations.value.find(
        inv => inv.applicantRealName === partnerForm.value.name && inv.status === 0
      )
      if (newInv) {
        partnerInvitation.value = newInv
      } else if (res.data) {
        // 直接用返回结果
        partnerInvitation.value = { status: 0, applicantRealName: partnerForm.value.name }
      }
    }
  } catch {
  } finally {
    inviting.value = false
  }
}

// 根据当前联合贷款人信息，从已发送列表中恢复/刷新邀请状态
function reconcilePartnerInvitation() {
  if (!partnerForm.value.phone && !partnerForm.value.name) {
    return
  }
  // 优先按电话匹配（唯一），其次按姓名；后端已发送列表把被邀请人信息放在 applicant* 字段
  const found = sentInvitations.value.find(inv => {
    const phone = inv.applicantPhone || inv.inviteePhone
    const name = inv.applicantRealName || inv.inviteeRealName
    if (partnerForm.value.phone && phone === partnerForm.value.phone) return true
    if (partnerForm.value.name && name === partnerForm.value.name) return true
    return false
  })
  partnerInvitation.value = found || null
}

// 查询联合贷款人邀请的最新状态
async function refreshPartnerStatus() {
  await loadSentInvitations()
  reconcilePartnerInvitation()
}

// 已同意的联合贷款人
const agreedPartner = computed(() => {
  if (!partnerInvitation.value) return null
  return partnerInvitation.value.status === 1 ? partnerInvitation.value : null
})

// 邀请状态文本
const invitationStatusText = computed(() => {
  if (!partnerInvitation.value) return ''
  const map = { 0: '等待对方确认', 1: '对方已同意', 2: '对方已拒绝' }
  return map[partnerInvitation.value.status] || ''
})

const invitationStatusType = computed(() => {
  if (!partnerInvitation.value) return 'info'
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[partnerInvitation.value.status] || 'info'
})

// ------------------ 提交申请 ------------------
const canSubmit = computed(() => {
  if (!form.value.productId || !form.value.amount || !form.value.repaymentPeriod || !form.value.cropItem) {
    return false
  }
  // 如果有联合贷款人，必须有已同意的
  if (needPartner.value) {
    return agreedPartner.value !== null
  }
  // 没有联合贷款人，可以直接提交
  return true
})

const submitHint = computed(() => {
  if (!form.value.productId) return '请先选择融资产品'
  if (!form.value.amount) return '请填写融资金额'
  if (!form.value.repaymentPeriod) return '请选择还款期限'
  if (!form.value.cropItem) return '请填写融资农作物'
  if (needPartner.value && !agreedPartner.value) {
    if (!partnerForm.value.name) return '请通过智能匹配选择或手动填写联合贷款人'
    if (!partnerInvitation.value) return '请发送邀请给联合贷款人'
    if (partnerInvitation.value.status === 2) return '对方已拒绝邀请，请选择其他联合贷款人'
    if (partnerInvitation.value.status === 0) return '等待对方同意邀请'
    return ''
  }
  return ''
})

// 融资金额实时校验：超出产品额度区间立即提示
const amountError = computed(() => {
  if (!selectedProduct.value) return ''
  const raw = form.value.amount
  if (raw === null || raw === '' || raw === undefined) return ''
  const amount = Number(raw)
  if (isNaN(amount)) return ''
  const minQuota = Number(selectedProduct.value.minQuota)
  const maxQuota = Number(selectedProduct.value.maxQuota)
  if (amount < minQuota || amount > maxQuota) {
    return `融资金额应在 ${minQuota.toLocaleString()} - ${maxQuota.toLocaleString()} 元之间`
  }
  return ''
})

async function handleSubmit() {
  if (!canSubmit.value) {
    ElMessage.warning(submitHint.value || '请完善申请信息')
    return
  }

  // 验证融资农作物
  if (!form.value.cropItem) {
    ElMessage.warning('请填写融资农作物')
    return
  }

  // 验证额度范围
  const product = products.value.find(p => p.id === form.value.productId)
  if (product) {
    const minQuota = Number(product.minQuota)
    const maxQuota = Number(product.maxQuota)
    const amount = Number(form.value.amount)
    if (amount < minQuota || amount > maxQuota) {
      ElMessage.warning('融资金额应在 ' + minQuota + ' - ' + maxQuota + ' 元之间')
      return
    }
    const minTerm = product.minTerm
    const maxTerm = product.maxTerm
    const period = Number(form.value.repaymentPeriod)
    if (period < minTerm || period > maxTerm) {
      ElMessage.warning('还款期限应在 ' + minTerm + ' - ' + maxTerm + ' 个月之间')
      return
    }
  }

  submitting.value = true
  try {
    const data = {
      productId: form.value.productId,
      money: Number(form.value.amount),
      repayment: Number(form.value.repaymentPeriod),
      cropItem: form.value.cropItem,
      fileInfo: form.value.fileInfo,
      // 联合贷款人信息（有则传递，无则留空）
      combinationName1: agreedPartner.value?.applicantRealName || '',
      combinationPhone1: agreedPartner.value?.applicantPhone || '',
      combinationName2: '',
      combinationPhone2: ''
    }

    const res = await applyFinanceAPI(data)
    if (res.code === 200) {
      ElMessage.success('融资申请已提交，请等待银行审核')
      clearDraft()
      // 重置表单
      form.value = {
        productId: null,
        amount: null,
        repaymentPeriod: null,
        cropItem: '',
        fileInfo: ''
      }
      selectedProduct.value = null
      needPartner.value = false
      partnerForm.value = { name: '', phone: '' }
      selectedMatchPartner.value = null
      partnerInvitation.value = null
    }
  } catch {
  } finally {
    submitting.value = false
  }
}

// 移除联合贷款人
function clearPartner() {
  partnerForm.value = { name: '', phone: '' }
  selectedMatchPartner.value = null
  partnerInvitation.value = null
}

// 放弃保存（清空草稿和表单）
function handleDiscardDraft() {
  ElMessageBox.confirm('确定放弃当前填写的内容吗？放弃后草稿将被清空。', '提示', {
    confirmButtonText: '确定放弃',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    clearDraft()
    form.value = {
      productId: null,
      amount: null,
      repaymentPeriod: null,
      cropItem: '',
      fileInfo: ''
    }
    selectedProduct.value = null
    needPartner.value = false
    partnerForm.value = { name: '', phone: '' }
    selectedMatchPartner.value = null
    partnerInvitation.value = null
    ElMessage.success('已放弃草稿')
  }).catch(() => {})
}

// 刷新邀请状态（轮询）
function startPolling() {
  const timer = setInterval(() => {
    if (needPartner.value && partnerInvitation.value && partnerInvitation.value.status === 0) {
      refreshPartnerStatus()
    }
  }, 5000)
  return timer
}

let pollTimer = null
onMounted(async () => {
  loadProducts()
  restoreDraft()
  await loadSentInvitations()
  // 用已发送列表回填邀请状态，避免刷新/重登后"发送邀请"按钮重复出现
  reconcilePartnerInvitation()
  pollTimer = startPolling()
})

// 格式化利率
function formatRate(rate) {
  return rate ? rate + '%' : '-'
}
</script>


<template>
  <div class="apply-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">融资申请</h2>
        <p class="page-sub">选择银行产品，填写申请信息，可选联合贷款人</p>
      </div>
      <el-button size="small" @click="handleDiscardDraft">放弃保存</el-button>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：产品选择 -->
      <el-col :xs="24" :sm="24" :md="10">
        <el-card shadow="never" class="products-card">
          <template #header><strong>银行融资产品</strong></template>
          <div v-loading="loading" class="product-list">
            <div
              v-for="product in products"
              :key="product.id"
              class="product-item"
              :class="{ selected: selectedProduct?.id === product.id }"
              @click="handleSelectProduct(product)"
            >
              <div class="product-name">{{ product.name }}</div>
              <div class="product-detail">额度：{{ Number(product.minQuota).toLocaleString() }} - {{ Number(product.maxQuota).toLocaleString() }}元</div>
              <div class="product-detail">期限：{{ product.minTerm }}-{{ product.maxTerm }}个月</div>
              <div class="product-detail">利率：{{ formatRate(product.rate) }}</div>
              <div class="product-desc">{{ product.description }}</div>
            </div>
            <el-empty v-if="!loading && products.length === 0" description="暂无可用产品" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：申请表单 -->
      <el-col :xs="24" :sm="24" :md="14">
        <el-card shadow="never" class="form-card">
          <template #header><strong>申请信息</strong></template>
          <el-form label-width="120px">
            <el-form-item label="已选产品">
              <el-tag v-if="selectedProduct" type="success">{{ selectedProduct.name }}</el-tag>
              <el-tag v-else type="info">请先选择产品</el-tag>
            </el-form-item>

            <el-form-item label="融资金额" required>
              <el-input
                v-model="form.amount"
                placeholder="请输入金额"
                type="number"
                :disabled="!selectedProduct"
              >
                <template #append>元</template>
              </el-input>
              <div v-if="selectedProduct" class="form-hint">
                范围：{{ Number(selectedProduct.minQuota).toLocaleString() }} - {{ Number(selectedProduct.maxQuota).toLocaleString() }}元
              </div>
              <div v-if="amountError" class="form-hint amount-error">
                {{ amountError }}
              </div>
            </el-form-item>

            <el-form-item label="还款期限" required>
              <el-select
                v-model="form.repaymentPeriod"
                placeholder="请选择期限"
                style="width: 100%"
                :disabled="!selectedProduct"
              >
                <el-option
                  v-for="month in selectedProduct ?
                    Array.from({ length: selectedProduct.maxTerm - selectedProduct.minTerm + 1 }, (_, i) => selectedProduct.minTerm + i) : []"
                  :key="month"
                  :label="month + '个月'"
                  :value="month"
                />
              </el-select>
              <div v-if="selectedProduct" class="form-hint">
                范围：{{ selectedProduct.minTerm }}-{{ selectedProduct.maxTerm }}个月
              </div>
            </el-form-item>

            <el-form-item label="融资农作物" required>
              <el-select v-model="form.cropItem" placeholder="请选择农作物" :disabled="!selectedProduct" style="width: 100%">
                <el-option v-for="crop in crops" :key="crop" :label="crop" :value="crop" />
              </el-select>
            </el-form-item>

            <el-form-item label="补充资料">
              <el-input
                v-model="form.fileInfo"
                placeholder="如有补充说明请填写"
                :rows="3"
                type="textarea"
                :disabled="!selectedProduct"
              />
            </el-form-item>

            <!-- 联合贷款人区域 -->
            <el-divider />
            <div class="partner-section">
              <!-- 是否需要联合贷款人 -->
              <div class="partner-toggle">
                <el-switch
                  v-model="needPartner"
                  @change="handleNeedPartnerChange"
                />
                <span class="toggle-label">需要联合贷款人</span>
              </div>

              <template v-if="needPartner">
                <!-- 智能匹配按钮 -->
                <div class="match-action">
                  <el-button
                    type="primary"
                    :disabled="!form.amount || !form.cropItem"
                    @click="openMatchDialog"
                    plain
                  >
                    <el-icon><MagicStick /></el-icon>
                    智能匹配联合贷款人
                  </el-button>
                  <span v-if="!form.amount || !form.cropItem" class="match-hint-text">
                    请先填写融资金额和农作物
                  </span>
                </div>

                <!-- 联合贷款人信息 -->
                <div class="partner-form-area">
                  <div class="partner-fields">
                    <el-input
                      v-model="partnerForm.name"
                      placeholder="联合贷款人姓名"
                      :disabled="!!partnerInvitation"
                      style="width: 200px"
                    >
                      <template #prepend>姓名</template>
                    </el-input>
                    <el-input
                      v-model="partnerForm.phone"
                      placeholder="联合贷款人电话"
                      :disabled="!!partnerInvitation"
                      style="width: 220px"
                    >
                      <template #prepend>电话</template>
                    </el-input>

                    <div class="partner-actions">
                      <!-- 未发送邀请 -->
                      <template v-if="!partnerInvitation">
                        <el-button
                          v-if="partnerForm.name && partnerForm.phone"
                          type="primary"
                          size="small"
                          :loading="inviting"
                          @click="sendPartnerInvite"
                        >
                          发送邀请
                        </el-button>
                        <el-button
                          v-if="partnerForm.name || partnerForm.phone"
                          text
                          type="danger"
                          size="small"
                          @click="clearPartner"
                        >
                          清除
                        </el-button>
                      </template>

                      <!-- 已发送邀请，显示状态 -->
                      <template v-else>
                        <el-tag :type="invitationStatusType" size="default">
                          {{ invitationStatusText }}
                        </el-tag>
                        <el-button
                          v-if="partnerInvitation.status === 2"
                          text
                          type="primary"
                          size="small"
                          @click="clearPartner"
                        >
                          重新选择
                        </el-button>
                        <el-button
                          v-if="partnerInvitation.status === 2 || partnerInvitation.status === 1"
                          text
                          type="primary"
                          size="small"
                          @click="refreshPartnerStatus"
                        >
                          刷新状态
                        </el-button>
                      </template>
                    </div>
                  </div>

                  <!-- 已选择的匹配伙伴信息 -->
                  <div v-if="selectedMatchPartner" class="selected-match-info">
                    <el-icon><InfoFilled /></el-icon>
                    <span>信用分：{{ selectedMatchPartner.credit || '-' }}</span>
                    <span>农作物：{{ selectedMatchPartner.cropItem || '-' }}</span>
                    <span>地址：{{ selectedMatchPartner.address || '-' }}</span>
                  </div>
                </div>
              </template>

              <!-- 无联合贷款人提示 -->
              <div v-else class="no-partner-hint">
                <el-icon><InfoFilled /></el-icon>
                <span>不添加联合贷款人，可直接提交申请</span>
              </div>
            </div>

            <!-- 提交按钮 -->
            <el-form-item style="margin-top: 24px;">
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                :disabled="!canSubmit"
                class="submit-btn"
                @click="handleSubmit"
              >
                提交申请
              </el-button>
              <div v-if="submitHint" class="form-hint submit-hint">
                {{ submitHint }}
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 智能匹配对话框 -->
    <el-dialog v-model="showMatchDialog" title="智能匹配推荐" width="700px">
      <div class="match-hint">
        <el-icon><InfoFilled /></el-icon>
        系统根据信用分、融资金额和农作物为您推荐匹配度较高的联合贷款人，点击"选择"自动填写信息
      </div>
      <el-table :data="matchedPartners" v-loading="matchLoading" stripe max-height="450">
        <el-table-column prop="realName" label="姓名" width="90" />
        <el-table-column label="信用分" width="90">
          <template #default="{ row }">
            <el-tag
              :type="(row.credit || 0) >= 80 ? 'success' : (row.credit || 0) >= 60 ? 'warning' : 'danger'"
              size="small"
            >
              {{ row.credit || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="匹配度" width="140">
          <template #default="{ row }">
            <div class="match-score-cell">
              <div class="match-score-bar">
                <div
                  class="match-score-fill"
                  :style="{ width: (row.matchScore || 0) + '%' }"
                  :class="getMatchLevel(row.matchScore).type"
                />
              </div>
              <span class="match-score-text">{{ row.matchScore || 0 }}%</span>
              <el-tag :type="getMatchLevel(row.matchScore).type" size="small">
                {{ getMatchLevel(row.matchScore).text }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="cropItem" label="种植作物" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="selectMatchPartner(row)">
              选择
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!matchLoading && matchedPartners.length === 0" description="暂无匹配推荐" />
    </el-dialog>
  </div>
</template>


<style scoped>
.apply-page { max-width: 1200px; margin: 0 auto; }
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

.products-card, .form-card {
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.product-list {
  max-height: 600px;
  overflow-y: auto;
}

.product-item {
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-item:hover {
  border-color: var(--green-primary);
  background: var(--green-light);
}

.product-item.selected {
  border-color: var(--green-primary);
  background: var(--green-light);
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--green-deep);
  margin-bottom: 8px;
}

.product-detail {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.product-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
  line-height: 1.5;
}

.form-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.amount-error {
  color: var(--el-color-danger);
  font-weight: 500;
}

.submit-hint {
  margin-top: 8px;
  color: var(--orange-primary);
}

/* 联合贷款人区域 */
.partner-section {
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}

.partner-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.toggle-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.match-action {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.match-hint-text {
  font-size: 12px;
  color: var(--text-muted);
}

.partner-form-area {
  padding: 12px;
  background: white;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.partner-fields {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.partner-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-match-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 10px;
  padding: 8px 12px;
  background: var(--blue-light);
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--blue-dark);
}

.no-partner-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-sm);
}

/* 匹配对话框 */
.match-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--blue-light);
  border-radius: var(--radius-sm);
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--blue-dark);
}

.match-score-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.match-score-bar {
  width: 60px;
  height: 8px;
  background: var(--bg-page);
  border-radius: 4px;
  overflow: hidden;
}

.match-score-fill {
  height: 100%;
  transition: width 0.3s;
}

.match-score-fill.success { background: var(--green-primary); }
.match-score-fill.warning { background: var(--orange-primary); }
.match-score-fill.info { background: var(--gray-light); }

.match-score-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--green-deep);
  min-width: 36px;
}
</style>
