<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsListAPI } from '../../api/goods'

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const list = ref([])

async function loadList() {
  loading.value = true
  try {
    const res = await getGoodsListAPI({ type: '1', keyword: keyword.value })
    if (res && res.code === 200) {
      list.value = res.data || []
    } else {
      list.value = []
    }
  } catch (e) {
    console.error('获取商品列表失败:', e)
    ElMessage.error('获取商品列表失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

function addToCart(item) {
  const cart = JSON.parse(localStorage.getItem('rxt_cart') || '[]')
  const existingIndex = cart.findIndex(c => c.id === item.goodsId)
  if (existingIndex >= 0) {
    cart[existingIndex].quantity += 1
  } else {
    cart.push({
      id: item.goodsId,
      title: item.title,
      price: parseFloat(item.price) || 0,
      quantity: 1,
      seller: item.ownName,
      address: item.address
    })
  }
  localStorage.setItem('rxt_cart', JSON.stringify(cart))
  ElMessage.success('已加入购物车：' + item.title)
}

function goToCart() {
  router.push('/trade/cart')
}

onMounted(() => { loadList() })

function handleImageError(event) {
  event.target.style.display = 'none'
  if (event.target.nextElementSibling) {
    event.target.nextElementSibling.style.display = 'flex'
  }
}
</script>

<template>
  <div class="products-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">商品采购</h2>
        <p class="page-sub">浏览优质农产品，一键加入购物车</p>
      </div>
      <div class="head-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索商品名称..."
          clearable
          class="search-input"
          @keyup.enter="loadList"
          @clear="loadList"
        >
          <template #append>
            <el-button @click="loadList"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="goToCart" class="cart-btn">
          <el-icon><ShoppingCart /></el-icon>
          购物车
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="goods-grid">
      <div v-for="item in list" :key="item.goodsId" class="goods-card">
        <div class="goods-img" @click="router.push('/trade/goods/' + item.goodsId)" style="cursor:pointer">
          <img
            v-if="item.picture && item.picture.startsWith('http')"
            :src="item.picture"
            :alt="item.title"
            @error="handleImageError"
          />
          <span v-else>📦</span>
        </div>
        <div class="goods-info">
          <div class="goods-title">{{ item.title }}</div>
          <div class="goods-price-row">
            <span class="price">¥{{ item.price }}</span>
            <span class="goods-seller">{{ item.ownName }}</span>
          </div>
          <div class="goods-tags">
            <el-tag v-if="item.type" size="small" type="success" effect="plain">{{ item.type }}</el-tag>
            <el-tag v-if="item.address" size="small" type="info" effect="plain">{{ item.address }}</el-tag>
          </div>
          <el-button type="primary" size="small" class="add-cart-btn" @click="addToCart(item)">
            <el-icon><Plus /></el-icon> 加入购物车
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-if="!list.length && !loading" description="暂无农户供应商品">
      <el-button type="primary" @click="router.push('/trade/home')">去商品大厅看看</el-button>
    </el-empty>
  </div>
</template>

<style scoped>
.products-page {
  max-width: 1200px;
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}
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
.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-input {
  width: 300px;
}
.cart-btn {
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Grid */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
@media (max-width: 1200px) { .goods-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 900px) { .goods-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 600px) { .goods-grid { grid-template-columns: repeat(2, 1fr); } }

.goods-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  transition: all 0.25s;
}
.goods-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.goods-img {
  width: 100%;
  height: 180px;
  background: var(--bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  overflow: hidden;
}
.goods-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.goods-img span { display: none; }

.goods-info {
  padding: 14px;
}
.goods-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.goods-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.price {
  color: var(--amber);
  font-size: 18px;
  font-weight: 700;
}
.goods-seller {
  font-size: 12px;
  color: var(--text-muted);
}
.goods-tags {
  display: flex;
  gap: 4px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.add-cart-btn {
  width: 100%;
  border-radius: var(--radius-sm);
  font-weight: 600;
}
</style>
