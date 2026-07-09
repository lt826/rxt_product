<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cart = ref(JSON.parse(localStorage.getItem('rxt_cart') || '[]'))

const totalAmount = computed(() => {
  return cart.value.reduce((sum, item) => sum + (parseFloat(item.price) || 0) * item.quantity, 0)
})

const form = ref({ address: '', phone: userStore.userInfo?.phone || '', note: '' })
const submitting = ref(false)

function submitOrder() {
  if (!form.value.address) return ElMessage.warning('请填写收货地址')
  submitting.value = true

  setTimeout(() => {
    ElMessage.success('订单提交成功！')
    localStorage.setItem('rxt_cart', '[]')
    setTimeout(() => {
      router.push('/buyer/dashboard')
      submitting.value = false
    }, 1500)
  }, 800)
}
</script>

<template>
  <div class="checkout-page">
    <div class="checkout-header">
      <h2>确认订单</h2>
      <p class="checkout-sub">填写收货信息并完成支付</p>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="24" :md="16">
        <el-card shadow="never" class="checkout-card">
          <template #header><strong>收货信息</strong></template>
          <el-form :model="form" label-width="90px" class="checkout-form">
            <el-form-item label="收货地址">
              <el-input v-model="form.address" placeholder="请填写详细收货地址" :rows="3" type="textarea" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="联系电话" />
            </el-form-item>
            <el-form-item label="订单备注">
              <el-input v-model="form.note" placeholder="选填，如有特殊要求请备注" :rows="3" type="textarea" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="never" class="summary-card">
          <template #header><strong>订单摘要</strong></template>
          <div v-for="item in cart" :key="item.id" class="summary-item">
            <span class="summary-name">{{ item.title || '商品' }} x{{ item.quantity }}</span>
            <span class="summary-price">¥{{ ((parseFloat(item.price) || 0) * item.quantity).toFixed(2) }}</span>
          </div>
          <el-divider style="margin: 16px 0" />
          <div class="summary-total">
            <span>合计</span>
            <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            @click="submitOrder"
          >
            提交订单
          </el-button>
          <el-button text class="back-btn" @click="router.push('/trade/cart')">返回修改</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.checkout-page {
  max-width: 1000px;
  margin: 0 auto;
}
.checkout-header {
  margin-bottom: 28px;
}
.checkout-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0 0 4px;
}
.checkout-sub {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}
.checkout-card, .summary-card {
  border-radius: var(--radius-md);
}
.checkout-form .el-form-item {
  margin-bottom: 18px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}
.summary-price {
  font-weight: 600;
  color: var(--text);
}
.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 20px;
}
.total-price {
  font-size: 24px;
  font-weight: 800;
  color: var(--amber);
}
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-sm);
}
.back-btn {
  width: 100%;
  margin-top: 8px;
  color: var(--text-muted);
}
</style>
