<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSoldOrdersAPI, shipOrderAPI } from '../../api/order'

const orders = ref([])
const loading = ref(false)

// 发货弹窗
const shipVisible = ref(false)
const shipping = ref(false)
const shipForm = ref({ purchaseId: null, shipAddress: '', shipName: '', shipPhone: '' })

async function load() {
  loading.value = true
  try {
    const res = await getSoldOrdersAPI()
    if (res.code === 200) orders.value = res.data || []
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

function openShip(o) {
  shipForm.value = {
    purchaseId: o.purchaseId,
    shipAddress: o.shipAddress || '',
    shipName: o.shipName || '',
    shipPhone: o.shipPhone || ''
  }
  shipVisible.value = true
}

async function submitShip() {
  if (!shipForm.value.shipAddress) return ElMessage.warning('请填写发货地址')
  if (!shipForm.value.shipName) return ElMessage.warning('请填写发货人姓名')
  if (!shipForm.value.shipPhone) return ElMessage.warning('请填写发货人电话')
  shipping.value = true
  try {
    const res = await shipOrderAPI(shipForm.value.purchaseId, {
      shipAddress: shipForm.value.shipAddress,
      shipName: shipForm.value.shipName,
      shipPhone: shipForm.value.shipPhone
    })
    if (res.code === 200) {
      ElMessage.success('发货成功')
      shipVisible.value = false
      load()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch { /* ignore */ } finally {
    shipping.value = false
  }
}

function tagType(s) {
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
      <p class="page-sub">买家下单后在此发货并填写物流信息</p>
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
            <div class="info-title">买家收货信息</div>
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
          <el-button v-if="o.status === 2" type="primary" @click="openShip(o)">发货</el-button>
        </div>
      </el-card>
    </div>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipVisible" title="填写物流信息" width="460px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="发货人姓名">
          <el-input v-model="shipForm.shipName" placeholder="请填写发货人姓名" />
        </el-form-item>
        <el-form-item label="发货人电话">
          <el-input v-model="shipForm.shipPhone" placeholder="请填写发货人电话" />
        </el-form-item>
        <el-form-item label="发货地址">
          <el-input v-model="shipForm.shipAddress" type="textarea" :rows="2" placeholder="请填写发货地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>
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
