<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetailAPI } from '../../api/goods'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const goods = ref(null)
const loading = ref(false)

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
  const cart = JSON.parse(localStorage.getItem('rxt_cart') || '[]')
  const exists = cart.find(c => c.id === goods.value.goodsId)
  if (!exists) {
    cart.push({ id: goods.value.goodsId, title: goods.value.title, price: parseFloat(goods.value.price) || 0, quantity: 1 })
    localStorage.setItem('rxt_cart', JSON.stringify(cart))
    ElMessage.success('已加入购物车')
  } else {
    ElMessage.info('该商品已在购物车中')
  }
}

function buyNow() {
  if (!userStore.isLoggedIn) return router.push('/login')
  ElMessage.info('请联系卖家商议购买事宜')
}
</script>

<template>
  <div class="detail-page">
    <div class="detail-body" v-loading="loading">
      <div v-if="goods" class="detail-card">
        <el-button text @click="router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>

        <div class="detail-layout">
          <!-- Image -->
          <div class="detail-image">
            <span>{{ ['🥔','🍑','🍌','🥬','🍄','🌽','🍎','🍇'][goods.goodsId % 8] }}</span>
          </div>

          <!-- Info -->
          <div class="detail-info">
            <h1 class="detail-title">{{ goods.title }}</h1>
            <div class="detail-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ goods.price || '面议' }}</span>
            </div>

            <div class="detail-meta">
              <div class="meta-row">
                <span class="meta-label">发布者</span>
                <span class="meta-value">{{ goods.ownName || '未知' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">产地</span>
                <span class="meta-value">{{ goods.address || '未填写' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">发布时间</span>
                <span class="meta-value">{{ goods.createTime ? goods.createTime.substring(0,10) : '-' }}</span>
              </div>
            </div>

            <div class="detail-description">
              <h4>商品描述</h4>
              <p>{{ goods.content || '暂无描述' }}</p>
            </div>

            <div class="detail-actions">
              <el-button type="primary" size="large" class="action-btn" @click="addToCart">
                <el-icon><Plus /></el-icon> 加入购物车
              </el-button>
              <el-button size="large" class="action-btn-secondary" @click="buyNow">
                <el-icon><ShoppingCart /></el-icon> 立即购买
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
  max-width: 1000px;
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
  margin-bottom: 20px;
  color: var(--text-secondary);
  font-weight: 500;
}
.back-btn:hover { color: var(--green); }

.detail-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 40px;
}
@media (max-width: 768px) {
  .detail-layout { grid-template-columns: 1fr; }
}

.detail-image {
  height: 360px;
  background: var(--green-mist);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 100px;
}

.detail-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0 0 16px;
  line-height: 1.3;
}

.detail-price {
  margin-bottom: 24px;
}
.price-symbol {
  font-size: 20px;
  color: var(--amber);
  font-weight: 700;
}
.price-value {
  font-size: 36px;
  color: var(--amber);
  font-weight: 800;
}

.detail-meta {
  background: var(--bg);
  border-radius: var(--radius-sm);
  padding: 16px 20px;
  margin-bottom: 24px;
}
.meta-row {
  display: flex;
  padding: 6px 0;
  font-size: 14px;
}
.meta-row:not(:last-child) {
  border-bottom: 1px solid var(--border);
}
.meta-label {
  width: 70px;
  color: var(--text-muted);
  flex-shrink: 0;
}
.meta-value {
  color: var(--text);
  font-weight: 500;
}

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
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid var(--border);
}
.action-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-sm);
}
.action-btn-secondary {
  flex: 1;
  height: 48px;
  font-size: 16px;
  border-radius: var(--radius-sm);
}
</style>
