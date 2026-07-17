<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Search } from '@element-plus/icons-vue'
import { getGoodsListAPI } from '../../api/goods'
import ProductCard from '../../components/ProductCard.vue'

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const list = ref([])

// 购物车数量（localStorage），用于右上角徽标
const cartCount = ref(0)
function syncCartCount() {
  try {
    cartCount.value = JSON.parse(localStorage.getItem('rxt_cart') || '[]')
      .reduce((s, c) => s + (Number(c.quantity) || 0), 0)
  } catch {
    cartCount.value = 0
  }
}

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
  const id = item.goodsId ?? item.id
  const cart = JSON.parse(localStorage.getItem('rxt_cart') || '[]')
  const existing = cart.find(c => c.id === id)
  if (existing) {
    existing.quantity += 1
  } else {
    cart.push({
      id,
      title: item.title,
      price: parseFloat(item.price) || 0,
      quantity: 1,
      seller: item.ownName,
      address: item.address,
      picture: item.picture || ''
    })
  }
  localStorage.setItem('rxt_cart', JSON.stringify(cart))
  syncCartCount()
  ElMessage.success('已加入购物车：' + item.title)
}

function viewDetail(item) {
  const id = item.goodsId ?? item.id
  router.push('/buyer/goods/' + id)
}

function goToCart() {
  router.push('/buyer/cart')
}

// 去求购大厅
function goToDemand() {
  router.push('/buyer/demand')
}

onMounted(() => { syncCartCount(); loadList() })
</script>

<template>
  <div class="hall">
    <!-- 大厅头部：分类切换 + 购物车 -->
    <div class="hall-bar">
      <div class="hall-tabs">
        <button class="hall-tab active">商品货源</button>
        <button class="hall-tab" @click="goToDemand">求购需求</button>
      </div>
      <el-button type="primary" class="hall-cart" @click="goToCart">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物车</span>
        <span v-if="cartCount > 0" class="hall-cart-badge">{{ cartCount }}</span>
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="hall-search">
      <el-icon class="hall-search-icon"><Search /></el-icon>
      <input
        v-model="keyword"
        class="hall-search-input"
        placeholder="输入关键词搜索：苹果、土豆、原产地直供…"
        @keyup.enter="loadList"
      />
      <el-button type="primary" class="hall-search-btn" @click="loadList">搜索</el-button>
    </div>

    <!-- 商品网格 -->
    <div v-loading="loading" class="hall-grid">
      <ProductCard
        v-for="item in list"
        :key="item.goodsId"
        :item="item"
        @view="viewDetail"
        @add-cart="addToCart"
      />
    </div>
    <el-empty v-if="!list.length && !loading" description="暂无农户供应商品">
      <el-button @click="loadList">刷新</el-button>
    </el-empty>
  </div>
</template>

<style scoped>
.hall {
  max-width: 1280px;
  margin: 0 auto;
}

/* ===== 顶栏：切换 + 购物车 ===== */
.hall-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.hall-tabs {
  display: inline-flex;
  gap: 4px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 5px;
}
.hall-tab {
  position: relative;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  padding: 8px 20px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}
.hall-tab:hover {
  color: var(--green);
}
.hall-tab.active {
  color: #fff;
  background: linear-gradient(135deg, var(--green), var(--green-mid));
  box-shadow: 0 2px 8px rgba(27, 67, 50, 0.18);
}
.hall-cart {
  position: relative;
  height: 42px;
  padding: 0 22px;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-weight: 600;
}
.hall-cart-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: var(--price);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  line-height: 20px;
  text-align: center;
}

/* ===== 搜索栏 ===== */
.hall-search {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 6px 6px 6px 44px;
  margin-bottom: 22px;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.hall-search:focus-within {
  border-color: var(--green-light);
  box-shadow: 0 0 0 3px rgba(82, 183, 136, 0.15);
}
.hall-search-icon {
  position: absolute;
  left: 16px;
  font-size: 18px;
  color: var(--text-muted);
}
.hall-search-input {
  flex: 1;
  height: 40px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--text);
}
.hall-search-input::placeholder {
  color: var(--text-muted);
}
.hall-search-btn {
  height: 40px;
  padding: 0 26px;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

/* ===== 网格 ===== */
.hall-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  min-height: 120px;
}
@media (max-width: 1100px) { .hall-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 820px)  { .hall-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 520px)  { .hall-grid { grid-template-columns: 1fr; } }
</style>
