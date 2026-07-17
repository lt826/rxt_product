<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGoodsListAPI } from '../../api/goods'

const router = useRouter()
const route = useRoute()

const cart = ref(JSON.parse(localStorage.getItem('rxt_cart') || '[]'))
const goodsCache = ref([])

const totalAmount = computed(() => {
  return cart.value.reduce((sum, item) => sum + (parseFloat(item.price) || 0) * item.quantity, 0)
})

const totalCount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.quantity, 0)
})

function updateQuantity(item, delta) {
  const newQty = item.quantity + delta
  if (newQty <= 0) return
  item.quantity = newQty
  saveCart()
}

function removeItem(id) {
  cart.value = cart.value.filter(c => c.id !== id)
  saveCart()
  ElMessage.success('已移除')
}

async function clearCart() {
  try {
    await ElMessageBox.confirm('确定清空购物车吗？', '提示', { type: 'warning' })
    cart.value = []
    saveCart()
    ElMessage.success('购物车已清空')
  } catch {}
}

function saveCart() {
  localStorage.setItem('rxt_cart', JSON.stringify(cart.value))
}

function checkout() {
  if (cart.value.length === 0) return ElMessage.warning('购物车为空')
  router.push('/buyer/checkout')
}

// Load goods info for display
onMounted(async () => {
  try {
    const res = await getGoodsListAPI({ type: '1' })
    if (res.code === 200 && res.data) {
      goodsCache.value = res.data
    }
  } catch {
    // use cache
  }
})
</script>

<template>
  <div class="cart-page">
    <div class="cart-header">
      <div>
        <h2>购物车</h2>
        <p class="cart-count">{{ totalCount }} 件商品</p>
      </div>
      <el-button v-if="cart.length > 0" text type="danger" @click="clearCart">清空购物车</el-button>
    </div>

    <div v-if="cart.length === 0" class="cart-empty">
      <div class="empty-icon">🛒</div>
      <h3>购物车是空的</h3>
      <p>快去挑选心仪的农产品吧</p>
      <el-button type="primary" size="large" @click="router.push('/buyer/products')">去逛逛</el-button>
    </div>

    <template v-else>
      <div class="cart-items">
        <div v-for="item in cart" :key="item.id" class="cart-item-card">
          <div class="item-check">
            <el-checkbox :model-value="true" disabled />
          </div>
          <div class="item-thumb">
            <img
              v-if="item.picture && /^https?:\/\//i.test(item.picture)"
              :src="item.picture"
              :alt="item.title"
            />
            <span v-else>{{ ['🥔','🍑','🍌','🥬','🍄','🌽','🍎','🍇'][(item.id % 8)] }}</span>
          </div>
          <div class="item-info">
            <h4 class="item-name">{{ item.title || item.name || '商品' }}</h4>
            <p class="item-unit">单价: <span class="price">¥{{ item.price }}</span></p>
          </div>
          <div class="item-qty">
            <div class="qty-controls">
              <button class="qty-btn" @click="updateQuantity(item, -1)">−</button>
              <input class="qty-input" type="number" :model-value="item.quantity" @change="item.quantity = parseInt($event.target.value) || 1" min="1" max="99" />
              <button class="qty-btn" @click="updateQuantity(item, 1)">+</button>
            </div>
          </div>
          <div class="item-total">
            <span class="total-price">¥{{ ((parseFloat(item.price) || 0) * item.quantity).toFixed(2) }}</span>
          </div>
          <div class="item-remove">
            <el-button text type="danger" size="small" @click="removeItem(item.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- Summary bar -->
      <div class="cart-summary">
        <div class="summary-left">
          <el-checkbox :model-value="true" disabled />
          <span>全选</span>
        </div>
        <div class="summary-right">
          <div class="summary-total">
            <span class="summary-label">合计：</span>
            <span class="summary-price">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <el-button type="primary" size="large" class="checkout-btn" @click="checkout">
            去结算 ({{ totalCount }}件)
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cart-page {
  max-width: 1000px;
  margin: 0 auto;
}
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.cart-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0 0 4px;
}
.cart-count {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

/* Empty */
.cart-empty {
  text-align: center;
  padding: 80px 0;
}
.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}
.cart-empty h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 8px;
}
.cart-empty p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 24px;
}

/* Items */
.cart-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}
.cart-item-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  transition: all 0.2s;
}
.cart-item-card:hover {
  box-shadow: var(--shadow-sm);
}
.item-check { flex-shrink: 0; }
.item-thumb {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-sm);
  background: var(--bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  flex-shrink: 0;
  overflow: hidden;
}
.item-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.item-info { flex: 1; min-width: 0; }
.item-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-unit {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}
.price {
  color: var(--amber);
  font-weight: 700;
}

/* Qty */
.qty-controls {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.qty-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--bg);
  color: var(--text-secondary);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}
.qty-btn:hover {
  background: var(--green-mist);
  color: var(--green);
}
.qty-input {
  width: 48px;
  height: 32px;
  border: none;
  border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  background: var(--surface);
  outline: none;
  -moz-appearance: textfield;
}
.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Total */
.item-total {
  text-align: right;
  min-width: 90px;
}
.total-price {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--amber);
}

/* Remove */
.item-remove { flex-shrink: 0; }

/* Summary bar */
.cart-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 16px 24px;
}
.summary-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}
.summary-right {
  display: flex;
  align-items: center;
  gap: 24px;
}
.summary-total {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.summary-label {
  font-size: 14px;
  color: var(--text-secondary);
}
.summary-price {
  font-size: 28px;
  font-weight: 800;
  color: var(--amber);
}
.checkout-btn {
  height: 44px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-sm);
}

@media (max-width: 600px) {
  .cart-item-card {
    flex-wrap: wrap;
    gap: 12px;
  }
  .item-total { flex: 1; text-align: left; }
  .cart-summary {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  .summary-right { justify-content: space-between; }
}
</style>
