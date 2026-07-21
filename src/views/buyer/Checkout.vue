<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import { listAddressAPI } from '../../api/address'
import { createOrderAPI, getBoughtOrdersAPI } from '../../api/order'
import { getPayQrcodeAPI, queryPayStatusAPI, confirmPayAPI } from '../../api/pay'

const router = useRouter()
const route = useRoute()

// 支付已存在的待付款订单：从「我的订单 - 去支付」带 orderId 进入
const payOrderId = route.query.orderId ? Number(route.query.orderId) : null
const isPayMode = computed(() => !!payOrderId)

// 待结算商品：优先来自购物车勾选项（rxt_checkout_items），回退到整购物车
const cart = ref(
  JSON.parse(localStorage.getItem('rxt_checkout_items') || localStorage.getItem('rxt_cart') || '[]')
)
const addresses = ref([])
const addressId = ref(null)
const submitting = ref(false)
const loading = ref(false)

// 支付模式下，订单已有的收货信息（只读展示）
const orderInfo = ref(null)

const totalAmount = computed(() =>
  cart.value.reduce((sum, item) => sum + (parseFloat(item.price) || 0) * item.quantity, 0)
)
// 支付模式以订单原价为准确认
const displayTotal = computed(() =>
  isPayMode.value ? Number(orderInfo.value?.totalPrice ?? totalAmount.value) : totalAmount.value
)

const selectedAddress = computed(() => addresses.value.find(a => a.id === addressId.value))

// 支付二维码弹窗状态
const payDialogVisible = ref(false)
const payingOrderId = ref(null)
const qrImgSrc = ref('')
const outTradeNo = ref('')
const payAmount = ref(0)
const generatingQr = ref(false)
const confirming = ref(false)
const errorMsg = ref('')
let pollTimer = null

async function loadAddresses() {
  try {
    const res = await listAddressAPI()
    if (res.code === 200) {
      addresses.value = res.data || []
      const def = addresses.value.find(a => a.isDefault === 1) || addresses.value[0]
      if (def) addressId.value = def.id
    }
  } catch { /* ignore */ }
}

// 支付模式：拉取待支付订单详情，回填商品清单与金额
async function loadPayOrder() {
  loading.value = true
  try {
    const res = await getBoughtOrdersAPI()
    if (res.code === 200) {
      const o = (res.data || []).find(x => Number(x.purchaseId) === Number(payOrderId))
      if (o) {
        orderInfo.value = o
        cart.value = (o.items || []).map(it => ({
          id: it.goodsId,
          title: it.title,
          price: it.uninPrice,
          quantity: it.count
        }))
        payAmount.value = Number(o.totalPrice) || 0
      } else {
        ElMessage.error('订单不存在或已支付')
        router.replace('/buyer/orders')
      }
    }
  } catch { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}

async function submitOrder() {
  // 支付模式：订单已存在，直接拉起支付
  if (isPayMode.value) {
    if (!payOrderId) return
    payAmount.value = displayTotal.value
    startPayment(payOrderId)
    return
  }
  if (!cart.value.length) return ElMessage.warning('购物车为空')
  if (!addressId.value) return ElMessage.warning('请选择收货地址')
  submitting.value = true
  try {
    const items = cart.value.map(c => ({
      goodsId: c.id,
      uninPrice: parseFloat(c.price) || 0,
      count: c.quantity
    }))
    const amt = totalAmount.value
    const res = await createOrderAPI({
      addressId: addressId.value,
      totalPrice: amt,
      items
    })
    if (res.code === 200) {
      const purchaseId = res.data?.purchaseId
      // 仅移除本次下单的商品，保留未勾选的商品
      const checkoutIds = cart.value.map(c => c.id)
      const remaining = JSON.parse(localStorage.getItem('rxt_cart') || '[]').filter(c => !checkoutIds.includes(c.id))
      localStorage.setItem('rxt_cart', JSON.stringify(remaining))
      localStorage.removeItem('rxt_checkout_items')
      payAmount.value = amt
      if (purchaseId) {
        startPayment(purchaseId)
      } else {
        ElMessage.success('下单成功！')
        router.push('/buyer/orders')
      }
    } else {
      ElMessage.error(res.message || '下单失败')
    }
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

// 打开支付弹窗：拉取支付宝二维码并启动轮询
async function startPayment(purchaseId) {
  payingOrderId.value = purchaseId
  payDialogVisible.value = true
  qrImgSrc.value = ''
  outTradeNo.value = ''
  errorMsg.value = ''
  generatingQr.value = true
  try {
    const res = await getPayQrcodeAPI(purchaseId)
    if (res.code === 200 && res.data) {
      if (res.data.totalPrice != null) payAmount.value = res.data.totalPrice
      outTradeNo.value = res.data.outTradeNo || ''
      if (res.data.qrCode) {
        qrImgSrc.value = await QRCode.toDataURL(res.data.qrCode, { width: 240, margin: 1 })
      } else if (res.data.errorMsg) {
        errorMsg.value = res.data.errorMsg
      }
    }
  } catch { /* ignore */ } finally {
    generatingQr.value = false
  }
  startPolling()
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (!payingOrderId.value || !outTradeNo.value) return
    try {
      const res = await queryPayStatusAPI(payingOrderId.value, outTradeNo.value)
      if (res.code === 200 && res.data?.paid) {
        handlePaidSuccess()
      }
    } catch { /* ignore */ }
  }, 2500)
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

// 手动「已完成支付」
async function onConfirmPaid() {
  if (!payingOrderId.value) return
  confirming.value = true
  try {
    const res = await confirmPayAPI(payingOrderId.value)
    if (res.code === 200) {
      handlePaidSuccess()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch { /* 拦截器已提示 */ } finally {
    confirming.value = false
  }
}

function handlePaidSuccess() {
  stopPolling()
  payDialogVisible.value = false
  ElMessage.success(isPayMode.value ? '支付成功！' : '下单成功！')
  setTimeout(() => router.push('/buyer/orders'), 600)
}

function onPayDialogClose() {
  stopPolling()
}

onMounted(() => {
  if (isPayMode.value) loadPayOrder()
  else loadAddresses()
})
onUnmounted(() => stopPolling())
</script>

<template>
  <div class="checkout-page">
    <div class="checkout-header">
      <h2>{{ isPayMode ? '订单支付' : '确认订单' }}</h2>
      <p class="checkout-sub">{{ isPayMode ? '完成该订单的支付' : '选择收货地址并提交订单' }}</p>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="24" :md="16" v-loading="loading">
        <!-- 收货地址 -->
        <el-card shadow="never" class="checkout-card">
          <template #header><strong>收货地址</strong></template>
          <!-- 支付模式：订单已有地址，只读展示 -->
          <template v-if="isPayMode">
            <div v-if="orderInfo" class="addr-detail">
              <div><span class="lbl">收货人</span>{{ orderInfo.consignee }}</div>
              <div><span class="lbl">电话</span>{{ orderInfo.consigneePhone }}</div>
              <div><span class="lbl">地址</span>{{ orderInfo.address }}</div>
            </div>
            <el-empty v-else description="订单信息加载中" :image-size="60" />
          </template>
          <!-- 下单模式：选择收货地址 -->
          <template v-else>
            <div v-if="addresses.length">
              <el-select v-model="addressId" placeholder="请选择收货地址" style="width: 100%" size="large">
                <el-option
                  v-for="a in addresses"
                  :key="a.id"
                  :label="`${a.consignee} ${a.phone} ${a.addressDetail}`"
                  :value="a.id"
                />
              </el-select>
              <div v-if="selectedAddress" class="addr-detail">
                <div><span class="lbl">收货人</span>{{ selectedAddress.consignee }}</div>
                <div><span class="lbl">电话</span>{{ selectedAddress.phone }}</div>
                <div><span class="lbl">地址</span>{{ selectedAddress.addressDetail }}</div>
              </div>
            </div>
            <div v-else class="addr-empty">
              <span>暂无收货地址</span>
              <el-button type="primary" @click="router.push('/profile/address')">去添加</el-button>
            </div>
          </template>
        </el-card>

        <!-- 商品清单 -->
        <el-card shadow="never" class="checkout-card">
          <template #header><strong>商品清单</strong></template>
          <div v-for="item in cart" :key="item.id" class="goods-row">
            <span class="goods-name">{{ item.title || item.name || '商品' }}</span>
            <span class="goods-qty">¥{{ item.price }} × {{ item.quantity }}</span>
            <span class="goods-sum">¥{{ ((parseFloat(item.price) || 0) * item.quantity).toFixed(2) }}</span>
          </div>
          <el-empty v-if="!cart.length" description="购物车为空" :image-size="60" />
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="never" class="summary-card">
          <template #header><strong>订单摘要</strong></template>
          <div class="summary-total">
            <span>合计</span>
            <span class="total-price">¥{{ Number(displayTotal).toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            :disabled="isPayMode ? !cart.length : (!cart.length || !addressId)"
            @click="submitOrder"
          >
            {{ isPayMode ? '立即支付' : '提交订单' }}
          </el-button>
          <el-button text class="back-btn" @click="router.push(isPayMode ? '/buyer/orders' : '/buyer/cart')">
            {{ isPayMode ? '返回订单' : '返回购物车' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 支付宝沙箱支付弹窗 -->
    <el-dialog
      v-model="payDialogVisible"
      title="支付宝支付"
      width="420px"
      :close-on-click-modal="false"
      align-center
      @close="onPayDialogClose"
    >
      <div class="pay-box">
        <div class="pay-amount">¥{{ Number(payAmount).toFixed(2) }}</div>
        <div class="pay-qr">
          <div v-if="generatingQr" class="pay-qr-state">
            <el-icon class="is-loading" :size="28"><Loading /></el-icon>
            <span>正在生成二维码...</span>
          </div>
          <img v-else-if="qrImgSrc" :src="qrImgSrc" alt="支付二维码" />
          <div v-else class="pay-qr-state">
            <el-icon :size="28"><WarningFilled /></el-icon>
            <span v-if="errorMsg" class="pay-error">{{ errorMsg }}</span>
            <span v-else>二维码生成失败<br />请检查支付宝沙箱凭证，或直接点击下方按钮</span>
          </div>
        </div>
        <div class="pay-tip">请使用支付宝（沙箱）扫码完成支付</div>
        <div class="pay-actions">
          <el-button
            type="primary"
            size="large"
            class="pay-confirm-btn"
            :loading="confirming"
            @click="onConfirmPaid"
          >
            已完成支付
          </el-button>
        </div>
        <div class="pay-hint">扫码支付成功后自动跳转；也可直接点击上方按钮完成下单</div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.checkout-page { max-width: 1000px; margin: 0 auto; }
.checkout-header { margin-bottom: 28px; }
.checkout-header h2 { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.checkout-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.checkout-card { border-radius: var(--radius-md); margin-bottom: 16px; }

.addr-detail { margin-top: 16px; background: var(--bg); border-radius: var(--radius-sm); padding: 14px 16px; font-size: 14px; color: var(--text); line-height: 1.9; }
.addr-detail .lbl { display: inline-block; width: 56px; color: var(--text-muted); }
.addr-empty { display: flex; align-items: center; gap: 12px; color: var(--text-muted); font-size: 14px; }

.goods-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid var(--border); font-size: 14px; }
.goods-row:last-child { border-bottom: none; }
.goods-name { flex: 1; color: var(--text); }
.goods-qty { color: var(--text-muted); }
.goods-sum { color: var(--amber); font-weight: 700; min-width: 80px; text-align: right; }

.summary-total { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 20px; }
.total-price { font-size: 26px; font-weight: 800; color: var(--amber); }
.submit-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 600; border-radius: var(--radius-sm); }
.back-btn { width: 100%; margin-top: 8px; color: var(--text-muted); }

/* 支付弹窗 */
.pay-box { display: flex; flex-direction: column; align-items: center; padding: 6px 0 2px; }
.pay-amount { font-size: 30px; font-weight: 800; color: var(--amber); margin-bottom: 16px; }
.pay-qr {
  width: 248px; height: 248px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--border); border-radius: var(--radius-sm); background: var(--bg);
}
.pay-qr img { width: 224px; height: 224px; }
.pay-qr-state { display: flex; flex-direction: column; align-items: center; gap: 10px; color: var(--text-muted); font-size: 13px; text-align: center; padding: 0 16px; }
.pay-error { font-size: 12px; color: var(--red, #e74c3c); word-break: break-all; line-height: 1.5; width: 100%; overflow-y: auto; max-height: 180px; }
.pay-tip { margin-top: 14px; font-size: 14px; color: var(--text-secondary); }
.pay-actions { width: 100%; margin-top: 20px; }
.pay-confirm-btn { width: 100%; height: 46px; font-size: 15px; font-weight: 600; border-radius: var(--radius-sm); }
.pay-hint { margin-top: 12px; font-size: 12px; color: var(--text-muted); text-align: center; line-height: 1.6; }
</style>
