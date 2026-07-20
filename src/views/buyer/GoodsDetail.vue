<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetailAPI } from '../../api/goods'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { parseCategory, cleanContent, formatPrice } from '../../utils/goods'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const goods = ref(null)
const loading = ref(false)
const quantity = ref(1)
const imgBroken = ref(false)

const category = computed(() => (goods.value ? (parseCategory(goods.value.content) || '农产品') : ''))
const description = computed(() => (goods.value ? cleanContent(goods.value.content) : ''))
const priceText = computed(() => (goods.value ? formatPrice(goods.value.price) : ''))
const hasImage = computed(() => {
  if (!goods.value) return false
  const p = goods.value.picture
  return !!(p && /^https?:\/\//i.test(String(p)) && !imgBroken.value)
})
const mono = computed(() => (goods.value ? String(goods.value.title || '品').trim().charAt(0) : '品'))
const isBuyer = computed(() => userStore.role === 'buyer')
// 是否为该商品的发布农户（农户端查看自己的商品时可编辑）
const isOwner = computed(() => !!(goods.value && userStore.userInfo?.userName && goods.value.ownName === userStore.userInfo.userName))
const subtotal = computed(() => {
  if (!goods.value) return ''
  const n = Number(goods.value.price)
  if (!isFinite(n) || n <= 0) return ''
  return (n * quantity.value).toFixed(2)
})

onMounted(async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await getGoodsDetailAPI(id)
    if (res.code === 200) {
      goods.value = res.data
    }
  } catch {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
})

function addToCart() {
  if (!goods.value) return
  if (!userStore.isLoggedIn) return router.push('/login')
  const qty = Math.max(1, parseInt(quantity.value) || 1)
  const cart = JSON.parse(localStorage.getItem('rxt_cart') || '[]')
  const existing = cart.find(c => c.id === goods.value.goodsId)
  if (existing) {
    existing.quantity += qty
  } else {
    cart.push({
      id: goods.value.goodsId,
      title: goods.value.title,
      price: parseFloat(goods.value.price) || 0,
      quantity: qty,
      seller: goods.value.ownName,
      address: goods.value.address,
      picture: goods.value.picture || ''
    })
  }
  localStorage.setItem('rxt_cart', JSON.stringify(cart))
  ElMessage.success(`已加入购物车（${qty} 件）`)
}

function contactSeller() {
  if (!userStore.isLoggedIn) return router.push('/login')
  if (isBuyer.value) {
    router.push('/buyer/contacts')
  } else {
    ElMessage.info('请通过消息联系卖家商议')
  }
}

// 农户编辑自己发布的商品：跳回商品货源大厅并自动打开编辑弹窗
function editProduct() {
  if (!goods.value) return
  router.push({ path: '/farmer/products', query: { edit: goods.value.goodsId } })
}
</script>

<template>
  <div class="detail-page">
    <div class="detail-body" v-loading="loading">
      <div v-if="goods" class="detail-card">
        <el-button text @click="router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回商品大厅
        </el-button>

        <div class="detail-layout">
          <!-- 主图 -->
          <div class="detail-media">
            <img
              v-if="hasImage"
              :src="goods.picture"
              :alt="goods.title"
              class="detail-img"
              @error="imgBroken = true"
            />
            <div v-else class="detail-emoji">{{ mono }}</div>
            <span class="detail-chip detail-chip--trust">产地直供</span>
          </div>

          <!-- 信息 -->
          <div class="detail-info">
            <div class="detail-cat"><span class="cat-dot"></span>{{ category }}</div>
            <h1 class="detail-title">{{ goods.title }}</h1>

            <div class="detail-price-box">
              <div class="detail-price">
                <span class="p-yen">¥</span><span class="p-num">{{ priceText || '面议' }}</span><span class="p-unit">/斤</span>
              </div>
              <div v-if="subtotal" class="detail-subtotal">小计 ¥{{ subtotal }}</div>
            </div>

            <div class="detail-meta">
              <div class="meta-row">
                <span class="meta-label">发布农户</span>
                <span class="meta-value">{{ goods.ownName || '未知' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">产地</span>
                <span class="meta-value">{{ goods.address || '未填写' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">上架时间</span>
                <span class="meta-value">{{ goods.createTime ? String(goods.createTime).substring(0, 10) : '-' }}</span>
              </div>
            </div>

            <div class="detail-description">
              <h4>商品描述</h4>
              <p>{{ description || '暂无描述' }}</p>
            </div>

            <!-- 数量（仅买家） -->
            <div v-if="isBuyer" class="detail-qty">
              <span class="qty-label">购买数量</span>
              <div class="qty-stepper">
                <button class="qty-btn" :disabled="quantity <= 1" @click="quantity = Math.max(1, quantity - 1)">−</button>
                <input class="qty-input" type="number" v-model.number="quantity" min="1" max="999" />
                <button class="qty-btn" @click="quantity = quantity + 1">+</button>
                <span class="qty-unit">斤</span>
              </div>
            </div>

            <!-- 买家：加购 / 联系卖家 -->
            <div v-if="isBuyer" class="detail-actions">
              <el-button type="primary" size="large" class="action-btn" @click="addToCart">
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
              <el-button size="large" class="action-btn-secondary" @click="contactSeller">
                <el-icon><ChatLineRound /></el-icon> 联系卖家
              </el-button>
            </div>
            <el-button
              v-if="isBuyer"
              text
              class="goto-cart"
              @click="router.push('/buyer/cart')"
            >
              查看购物车去结算 <el-icon><ArrowRight /></el-icon>
            </el-button>

            <!-- 农户查看自己发布的商品：编辑入口 -->
            <div v-if="!isBuyer && isOwner" class="detail-actions">
              <el-button type="primary" size="large" class="action-btn" @click="editProduct">
                <el-icon><Edit /></el-icon> 编辑商品
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !goods" description="商品不存在" :image-size="100" />
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 1100px;
  margin: 0 auto;
}
.detail-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}
.back-btn {
  margin-bottom: 22px;
  color: var(--text-secondary);
  font-weight: 500;
}
.back-btn:hover { color: var(--green); }

.detail-layout {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 44px;
}
@media (max-width: 880px) {
  .detail-layout { grid-template-columns: 1fr; gap: 28px; }
}

/* 主图 */
.detail-media {
  position: relative;
  height: 420px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: linear-gradient(135deg, var(--green-mist), #eef7f0);
}
.detail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-emoji {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 120px;
  font-weight: 800;
  color: var(--green-deep);
  opacity: 0.85;
}
.detail-chip {
  position: absolute;
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 20px;
  color: #fff;
}
.detail-chip--trust {
  top: 14px;
  right: 14px;
  background: linear-gradient(135deg, var(--green), var(--green-light));
  box-shadow: 0 2px 8px rgba(27, 67, 50, 0.25);
}

/* 信息 */
.detail-cat {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--green);
  letter-spacing: 0.05em;
  margin-bottom: 8px;
}
.cat-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--green-light); }
.detail-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--text);
  margin: 0 0 18px;
  line-height: 1.3;
}

.detail-price-box {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  background: var(--price-soft);
  border-radius: var(--radius-md);
  padding: 18px 22px;
  margin-bottom: 22px;
  flex-wrap: wrap;
}
.detail-price {
  display: inline-flex;
  align-items: baseline;
  color: var(--price);
}
.p-yen { font-size: 20px; font-weight: 700; }
.p-num { font-size: 40px; font-weight: 800; letter-spacing: -0.02em; line-height: 1; }
.p-unit { font-size: 14px; color: var(--text-muted); margin-left: 4px; }
.detail-subtotal {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 600;
}

.detail-meta {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 6px 20px;
  margin-bottom: 24px;
}
.meta-row {
  display: flex;
  padding: 12px 0;
  font-size: 14px;
}
.meta-row:not(:last-child) { border-bottom: 1px solid var(--border); }
.meta-label { width: 80px; color: var(--text-muted); flex-shrink: 0; }
.meta-value { color: var(--text); font-weight: 500; }

.detail-description h4 {
  font-size: 16px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0 0 10px;
}
.detail-description p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin: 0;
  white-space: pre-line;
}

/* 数量 */
.detail-qty {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 24px 0 6px;
}
.qty-label { font-size: 14px; color: var(--text-secondary); font-weight: 500; }
.qty-stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.qty-btn {
  width: 36px;
  height: 38px;
  border: none;
  background: var(--bg);
  color: var(--text-secondary);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.15s;
}
.qty-btn:hover:not(:disabled) { background: var(--green-mist); color: var(--green); }
.qty-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.qty-input {
  width: 56px;
  height: 38px;
  border: none;
  border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  background: var(--surface);
  outline: none;
  -moz-appearance: textfield;
}
.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.qty-unit { font-size: 13px; color: var(--text-muted); padding: 0 12px; }

/* 操作 */
.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
  padding-top: 24px;
  border-top: 1px solid var(--border);
}
.action-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-sm);
}
.action-btn-secondary {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  color: var(--green-deep);
  border-color: var(--green-pale);
  background: var(--green-mist);
}
.action-btn-secondary:hover {
  color: var(--green-deep);
  border-color: var(--green-light);
  background: var(--green-pale);
}
.goto-cart {
  margin-top: 12px;
  color: var(--green);
  font-weight: 500;
}
.goto-cart .el-icon { margin-left: 2px; }
</style>
