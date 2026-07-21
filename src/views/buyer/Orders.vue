<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBoughtOrdersAPI, receiveOrderAPI } from '../../api/order'

const router = useRouter()
const orders = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getBoughtOrdersAPI()
    if (res.code === 200) orders.value = res.data || []
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

async function confirmReceive(o) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'warning' })
    const res = await receiveOrderAPI(o.purchaseId)
    if (res.code === 200) { ElMessage.success('已确认收货'); load() }
    else ElMessage.error(res.message || '操作失败')
  } catch { /* 取消 */ }
}

// 待付款订单：跳转到订单结算界面继续支付
function payOrder(o) {
  router.push('/buyer/checkout?orderId=' + o.purchaseId)
}

function tagType(s) {
  if (s === 1) return 'danger'
  if (s === 2) return 'warning'
  if (s === 3) return 'primary'
  if (s === 4) return 'success'
  return 'info'
}

onMounted(load)
</script>

<template>
  <div class="orders-page" v-loading="loading">
    <div class="page-head">
      <h2 class="page-title">我的订单</h2>
      <p class="page-sub">查看采购订单与物流状态</p>
    </div>

    <el-empty v-if="!orders.length && !loading" description="暂无订单" />

    <div class="order-list">
      <el-card v-for="o in orders" :key="o.purchaseId" shadow="never" class="order-card">
        <div class="order-head">
          <span class="order-no">订单号：#{{ o.purchaseId }}</span>
          <span class="order-time">{{ o.createTime }}</span>
          <el-tag :type="tagType(o.status)" size="small">{{ o.statusText }}</el-tag>
        </div>

        <div class="order-items">
          <div v-for="(it, i) in o.items" :key="i" class="item-row">
            <span class="item-name">{{ it.title }}</span>
            <span class="item-qty">¥{{ it.uninPrice }} × {{ it.count }}</span>
            <span class="item-sum">¥{{ it.sumPrice }}</span>
          </div>
        </div>

        <div class="order-info">
          <div class="info-block">
            <div class="info-title">收货信息</div>
            <div>{{ o.consignee }} {{ o.consigneePhone }}</div>
            <div class="muted">{{ o.address }}</div>
          </div>
          <div v-if="o.shipName || o.shipAddress" class="info-block">
            <div class="info-title">物流信息</div>
            <div>发货人：{{ o.shipName }} {{ o.shipPhone }}</div>
            <div class="muted">发货地址：{{ o.shipAddress }}</div>
          </div>
        </div>

        <div class="order-foot">
          <div class="total">合计：<span class="price">¥{{ o.totalPrice }}</span></div>
          <el-button v-if="o.status === 1" type="primary" @click="payOrder(o)">去支付</el-button>
          <el-button v-if="o.status === 3" type="primary" @click="confirmReceive(o)">确认收货</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.orders-page { max-width: 1000px; }
.page-head { margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.order-list { display: flex; flex-direction: column; gap: 16px; }
.order-card { border-radius: var(--radius-md); }
.order-head { display: flex; align-items: center; gap: 12px; padding-bottom: 12px; border-bottom: 1px solid var(--border); }
.order-no { font-weight: 600; color: var(--text); }
.order-time { font-size: 13px; color: var(--text-muted); flex: 1; }

.order-items { padding: 12px 0; border-bottom: 1px solid var(--border); }
.item-row { display: flex; align-items: center; gap: 12px; padding: 6px 0; font-size: 14px; }
.item-name { flex: 1; color: var(--text); }
.item-qty { color: var(--text-muted); }
.item-sum { color: var(--amber); font-weight: 600; min-width: 80px; text-align: right; }

.order-info { display: flex; gap: 32px; padding: 14px 0; border-bottom: 1px solid var(--border); flex-wrap: wrap; }
.info-block { font-size: 13px; color: var(--text); line-height: 1.8; }
.info-title { font-weight: 600; color: var(--green-deep); margin-bottom: 2px; }
.muted { color: var(--text-muted); }

.order-foot { display: flex; justify-content: flex-end; align-items: center; gap: 16px; padding-top: 14px; }
.total { font-size: 15px; color: var(--text-secondary); }
.total .price { font-size: 20px; font-weight: 800; color: var(--amber); }
</style>
