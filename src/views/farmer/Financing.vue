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
// 联合贷款人最多 2 人；每个槽位：{ name, phone, invitation, matchInfo }
const MAX_PARTNERS = 2
const partners = ref([])
// 当前正在发送邀请的槽位下标（用于按钮 loading）
const invitingIndex = ref(null)

function emptyPartnerSlot() {
  return { name: '', phone: '', invitation: null, matchInfo: null }
}

const crops = ['小麦', '玉米', '水稻', '大豆', '棉花', '蔬菜', '果树', '其他']

// ------------------ 草稿保存 ------------------
function saveDraft() {
  try {
    const draft = {
      form: { ...form.value },
      needPartner: needPartner.value,
      // 只持久化姓名/电话，邀请状态挂载后从后端重新对账，避免草稿中的状态过期
      partners: partners.value.map(p => ({ name: p.name, phone: p.phone })),
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
    if (Array.isArray(draft.partners)) {
      partners.value = draft.partners
        .slice(0, MAX_PARTNERS)
        .map(p => ({ name: p.name || '', phone: p.phone || '', invitation: null, matchInfo: null }))
    } else {
      partners.value = draft.needPartner ? [emptyPartnerSlot()] : []
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

watch(() => ({ ...form.value, needPartner: needPartner.value, partners: partners.value }), autoSaveDraft, { deep: true })
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
    partners.value = []
  } else {
    needPartner.value = true
    if (partners.value.length === 0) {
      partners.value = [emptyPartnerSlot()]
    }
  }
}

function addPartnerSlot() {
  if (partners.value.length >= MAX_PARTNERS) return
  partners.value.push(emptyPartnerSlot())
}

function removePartnerSlot(index) {
  partners.value.splice(index, 1)
}

function clearPartnerSlot(index) {
  partners.value[index] = emptyPartnerSlot()
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

// 从匹配列表中选择联合贷款人：填入第一个空槽位；无空槽位且未满则自动新增一个
function selectMatchPartner(partner) {
  const phone = partner.phone || ''
  // 同手机号去重：已存在于任意槽位则拒绝
  const dup = partners.value.some(p => p.phone && phone && p.phone === phone)
  if (dup) {
    ElMessage.warning('该联合贷款人已在槽位中，请勿重复添加')
    return
  }
  // 优先填第一个"空的"槽位（姓名/电话均未填写），避免覆盖已填写的槽位
  let idx = partners.value.findIndex(p => !p.name && !p.phone)
  if (idx === -1) {
    // 没有空槽位：未满则自动新增第 2 个槽位
    if (partners.value.length >= MAX_PARTNERS) {
      ElMessage.warning('联合贷款人槽位已满（最多 2 人），请先清除某个槽位')
      return
    }
    partners.value.push(emptyPartnerSlot())
    idx = partners.value.length - 1
  }
  partners.value[idx] = {
    name: partner.realName || partner.userName || '',
    phone,
    invitation: null,
    matchInfo: partner
  }
  showMatchDialog.value = false
  ElMessage.success('已选择 ' + partners.value[idx].name + ' 作为联合贷款人')
}

// 匹配度等级
function getMatchLevel(score) {
  if (score >= 80) return { text: '高匹配', type: 'success' }
  if (score >= 60) return { text: '中匹配', type: 'warning' }
  return { text: '低匹配', type: 'info' }
}

// ------------------ 发送邀请 ------------------
async function sendPartnerInvite(index) {
  const slot = partners.value[index]
  if (!slot) return
  if (!slot.name || !slot.phone) {
    ElMessage.warning('请先选择或填写联合贷款人信息')
    return
  }
  // 同一手机号不能在多个槽位重复邀请
  const dupPhone = partners.value.some((p, i) => i !== index && p.phone && p.phone === slot.phone)
  if (dupPhone) {
    ElMessage.warning('该手机号已在其他联合贷款人槽位使用')
    return
  }

  invitingIndex.value = index
  try {
    const description = '希望与您联合申请' + form.value.cropItem + '种植贷款，金额' + form.value.amount + '元'
    const res = await sendPartnerInvitationAPI({
      inviteeName: slot.name,
      inviteePhone: slot.phone,
      projectDescription: description
    })
    if (res.code === 200) {
      ElMessage.success('邀请已发送，等待对方确认')
      await loadSentInvitations()
      reconcileSlotInvitation(index)
    }
  } catch {
  } finally {
    invitingIndex.value = null
  }
}

// 根据某槽位的联合贷款人信息，从已发送列表中恢复/刷新邀请状态
function reconcileSlotInvitation(index) {
  const slot = partners.value[index]
  if (!slot) return
  if (!slot.phone && !slot.name) {
    slot.invitation = null
    return
  }
  // 后端"我发送的"列表把被邀请人信息放在 applicant* 字段
  const found = sentInvitations.value.find(inv => {
    const phone = inv.applicantPhone || inv.inviteePhone
    const name = inv.applicantRealName || inv.inviteeRealName
    if (slot.phone && phone === slot.phone) return true
    if (slot.name && name === slot.name) return true
    return false
  })
  slot.invitation = found || null
}

function reconcileAllInvitations() {
  partners.value.forEach((_, i) => reconcileSlotInvitation(i))
}

// 查询联合贷款人邀请的最新状态
async function refreshPartnerStatus(index) {
  await loadSentInvitations()
  if (index == null) {
    reconcileAllInvitations()
  } else {
    reconcileSlotInvitation(index)
  }
}

// 已同意的联合贷款人（用于提交申请）
const agreedPartners = computed(() =>
  partners.value.filter(p => p.invitation && p.invitation.status === 1)
)
// 是否存在待确认 / 已拒绝 / 已填写未发送邀请的槽位（影响是否可提交）
const hasPendingSlot = computed(() => partners.value.some(p => p.invitation && p.invitation.status === 0))
const hasRejectedSlot = computed(() => partners.value.some(p => p.invitation && p.invitation.status === 2))
const hasUninvitedFilledSlot = computed(() => partners.value.some(p => (p.name || p.phone) && !p.invitation))

// 邀请状态文本/类型（按槽位）
function slotInvitationStatusText(slot) {
  if (!slot || !slot.invitation) return ''
  const map = { 0: '等待对方确认', 1: '对方已同意', 2: '对方已拒绝' }
  return map[slot.invitation.status] || ''
}
function slotInvitationStatusType(slot) {
  if (!slot || !slot.invitation) return 'info'
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[slot.invitation.status] || 'info'
}

// ------------------ 提交申请 ------------------
const canSubmit = computed(() => {
  if (!form.value.productId || !form.value.amount || !form.value.repaymentPeriod || !form.value.cropItem) {
    return false
  }
  if (!needPartner.value) {
    // 没有联合贷款人，可以直接提交
    return true
  }
  // 需要联合贷款人：至少 1 位已同意，且其余槽位不得有待确认/已拒绝/未发送邀请的情况
  if (agreedPartners.value.length < 1) return false
  if (hasPendingSlot.value || hasRejectedSlot.value || hasUninvitedFilledSlot.value) return false
  return true
})

const submitHint = computed(() => {
  if (!form.value.productId) return '请先选择融资产品'
  if (!form.value.amount) return '请填写融资金额'
  if (!form.value.repaymentPeriod) return '请选择还款期限'
  if (!form.value.cropItem) return '请填写融资农作物'
  if (!needPartner.value) return ''
  if (hasUninvitedFilledSlot.value) return '有联合贷款人信息未发送邀请，请补发或清除'
  if (agreedPartners.value.length < 1) {
    if (!partners.value.some(p => p.name || p.phone)) return '请通过智能匹配选择或手动填写联合贷款人'
    if (!partners.value.some(p => p.invitation)) return '请为联合贷款人发送邀请'
    if (hasPendingSlot.value) return '等待联合贷款人同意邀请'
    if (hasRejectedSlot.value) return '联合贷款人已拒绝邀请，请重新选择'
    return '请等待联合贷款人同意'
  }
  if (hasPendingSlot.value) return '有联合贷款人邀请待确认，可等待同意或移除该槽位'
  if (hasRejectedSlot.value) return '有联合贷款人已拒绝邀请，请移除或重新选择'
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
    // 已同意的联合贷款人按顺序填入 combination1 / combination2（至多 2 位）
    const agreed = agreedPartners.value
    const data = {
      productId: form.value.productId,
      money: Number(form.value.amount),
      repayment: Number(form.value.repaymentPeriod),
      cropItem: form.value.cropItem,
      fileInfo: form.value.fileInfo,
      combinationName1: agreed[0]?.invitation?.applicantRealName || agreed[0]?.name || '',
      combinationPhone1: agreed[0]?.invitation?.applicantPhone || agreed[0]?.phone || '',
      combinationName2: agreed[1]?.invitation?.applicantRealName || agreed[1]?.name || '',
      combinationPhone2: agreed[1]?.invitation?.applicantPhone || agreed[1]?.phone || ''
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
      partners.value = []
    }
  } catch {
  } finally {
    submitting.value = false
  }
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
    partners.value = []
    ElMessage.success('已放弃草稿')
  }).catch(() => {})
}

// 刷新邀请状态（轮询）：只要有待确认的邀请就刷新全部槽位
function startPolling() {
  const timer = setInterval(() => {
    if (needPartner.value && hasPendingSlot.value) {
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
  // 用已发送列表回填各槽位邀请状态，避免刷新/重登后"发送邀请"按钮重复出现
  reconcileAllInvitations()
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
        <p class="page-sub">选择银行产品，填写申请信息，可选联合贷款人（最多 2 人）</p>
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
                <span class="toggle-label">需要联合贷款人（最多 2 人）</span>
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
                  <span class="match-hint-text partner-count">
                    已同意 {{ agreedPartners.length }} / {{ partners.length }} 个槽位
                  </span>
                </div>

                <!-- 联合贷款人槽位列表 -->
                <div v-for="(slot, index) in partners" :key="index" class="partner-form-area">
                  <div class="partner-slot-head">
                    <span class="partner-slot-title">联合贷款人 {{ index + 1 }}</span>
                    <el-button
                      v-if="partners.length > 1"
                      text
                      type="danger"
                      size="small"
                      @click="removePartnerSlot(index)"
                    >
                      移除
                    </el-button>
                  </div>

                  <div class="partner-fields">
                    <el-input
                      v-model="slot.name"
                      placeholder="联合贷款人姓名"
                      :disabled="!!slot.invitation"
                      style="width: 200px"
                    >
                      <template #prepend>姓名</template>
                    </el-input>
                    <el-input
                      v-model="slot.phone"
                      placeholder="联合贷款人电话"
                      :disabled="!!slot.invitation"
                      style="width: 220px"
                    >
                      <template #prepend>电话</template>
                    </el-input>

                    <div class="partner-actions">
                      <!-- 未发送邀请 -->
                      <template v-if="!slot.invitation">
                        <el-button
                          v-if="slot.name && slot.phone"
                          type="primary"
                          size="small"
                          :loading="invitingIndex === index"
                          @click="sendPartnerInvite(index)"
                        >
                          发送邀请
                        </el-button>
                        <el-button
                          v-if="slot.name || slot.phone"
                          text
                          type="danger"
                          size="small"
                          @click="clearPartnerSlot(index)"
                        >
                          清除
                        </el-button>
                      </template>

                      <!-- 已发送邀请，显示状态 -->
                      <template v-else>
                        <el-tag :type="slotInvitationStatusType(slot)" size="default">
                          {{ slotInvitationStatusText(slot) }}
                        </el-tag>
                        <el-button
                          v-if="slot.invitation.status === 2"
                          text
                          type="primary"
                          size="small"
                          @click="clearPartnerSlot(index)"
                        >
                          重新选择
                        </el-button>
                        <el-button
                          v-if="slot.invitation.status === 2 || slot.invitation.status === 1"
                          text
                          type="primary"
                          size="small"
                          @click="refreshPartnerStatus(index)"
                        >
                          刷新状态
                        </el-button>
                      </template>
                    </div>
                  </div>

                  <!-- 已选择的匹配伙伴信息 -->
                  <div v-if="slot.matchInfo" class="selected-match-info">
                    <el-icon><InfoFilled /></el-icon>
                    <span>信用分：{{ slot.matchInfo.credit || '-' }}</span>
                    <span>农作物：{{ slot.matchInfo.cropItem || '-' }}</span>
                    <span>地址：{{ slot.matchInfo.address || '-' }}</span>
                  </div>
                </div>

                <!-- 添加联合贷款人 -->
                <div v-if="partners.length < MAX_PARTNERS" class="add-partner-action">
                  <el-button plain @click="addPartnerSlot">
                    <el-icon><Plus /></el-icon>
                    添加联合贷款人（还可添加 {{ MAX_PARTNERS - partners.length }} 位）
                  </el-button>
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
        系统根据信用分、融资金额和农作物为您推荐匹配度较高的联合贷款人，点击"选择"自动填写到第一个可用槽位
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
  flex-wrap: wrap;
}

.match-hint-text {
  font-size: 12px;
  color: var(--text-muted);
}

.partner-count {
  margin-left: auto;
}

.partner-form-area {
  padding: 12px;
  background: white;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  margin-bottom: 12px;
}

.partner-slot-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.partner-slot-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--green-deep);
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

.add-partner-action {
  display: flex;
  justify-content: center;
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
