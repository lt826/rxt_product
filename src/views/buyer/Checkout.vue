<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listAddressAPI } from '../../api/address'
import { createOrderAPI } from '../../api/order'

const router = useRouter()
const cart = ref(JSON.parse(localStorage.getItem('rxt_cart') || '[]'))
const addresses = ref([])
const addressId = ref(null)
const submitting = ref(false)

const totalAmount = computed(() =>
  cart.value.reduce((sum, item) => sum + (parseFloat(item.price) || 0) * item.quantity, 0)
)

const selectedAddress = computed(() => addresses.value.find(a => a.id === addressId.value))

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

async function submitOrder() {
  if (!cart.value.length) return ElMessage.warning('购物车为空')
  if (!addressId.value) return ElMessage.warning('请选择收货地址')
  submitting.value = true
  try {
    const items = cart.value.map(c => ({
      goodsId: c.id,
      uninPrice: parseFloat(c.price) || 0,
      count: c.quantity
    }))
    const res = await createOrderAPI({
      addressId: addressId.value,
      totalPrice: totalAmount.value,
      items
    })
    if (res.code === 200) {
      ElMessage.success('下单成功！')
      localStorage.setItem('rxt_cart', '[]')
      setTimeout(() => router.push('/buyer/orders'), 800)
    } else {
      ElMessage.error(res.message || '下单失败')
    }
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

onMounted(loadAddresses)
</script>

<template>
  <div class="checkout-page">
    <div class="checkout-header">
      <h2>确认订单</h2>
      <p class="checkout-sub">选择收货地址并提交订单</p>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="24" :md="16">
        <!-- 收货地址 -->
        <el-card shadow="never" class="checkout-card">
          <template #header><strong>收货地址</strong></template>
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
            <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            :disabled="!cart.length || !addressId"
            @click="submitOrder"
          >
            提交订单
          </el-button>
          <el-button text class="back-btn" @click="router.push('/buyer/cart')">返回购物车</el-button>
        </el-card>
      </el-col>
    </el-row>
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
</style>
