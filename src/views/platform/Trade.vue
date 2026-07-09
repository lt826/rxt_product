<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsListAPI } from '../../api/goods'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('goods')
const goods = ref([])
const demands = ref([])
const cart = ref(JSON.parse(localStorage.getItem('rxt_cart') || '[]'))
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [goodsRes, demandRes] = await Promise.allSettled([
      getGoodsListAPI({ type: '1' }),
      getGoodsListAPI({ type: '2' })
    ])
    if (goodsRes.status === 'fulfilled' && goodsRes.value?.code === 200) {
      goods.value = (goodsRes.value.data || []).map(g => ({
        id: g.goodsId, title: g.title, price: g.price ? `¥${g.price}` : '面议',
        content: g.content || '', seller: g.ownName, address: g.address,
        picture: g.picture, createTime: g.createTime
      }))
    }
    if (demandRes.status === 'fulfilled' && demandRes.value?.code === 200) {
      demands.value = (demandRes.value.data || []).map(d => ({
        id: d.goodsId, title: d.title, price: d.price ? `¥${d.price}` : '面议',
        content: d.content || '', buyer: d.ownName, address: d.address, createTime: d.createTime
      }))
    }
  } catch(e) {}
  finally { loading.value = false }
})

function viewDetail(id) { router.push(`/trade/goods/${id}`) }

function addToCart(item) {
  const exists = cart.value.find(c => c.id === item.id)
  if (!exists) {
    cart.value.push({ id: item.id, title: item.title, price: item.price, quantity: 1 })
    localStorage.setItem('rxt_cart', JSON.stringify(cart.value))
    ElMessage.success('已加入购物车')
  } else {
    ElMessage.info('该商品已在购物车中')
  }
}

function goToCart() {
  if (!userStore.isLoggedIn) return router.push('/login')
  router.push('/trade/cart')
}
</script>

<template>
  <div class="trade-page">
    <div class="page-banner">
      <h1>农产品交易</h1>
      <p>连接农户与买家，打造便捷的农产品交易通道</p>
      <div class="banner-actions">
        <el-badge :value="cart.length" :hidden="cart.length === 0">
          <el-button size="large" @click="goToCart" style="background:#fff;color:#333">🛒 购物车</el-button>
        </el-badge>
      </div>
    </div>

    <div class="page-body">
      <el-tabs v-model="activeTab" class="trade-tabs">
        <!-- 商品货源 -->
        <el-tab-pane label="商品货源" name="goods">
          <div class="goods-grid" v-loading="loading">
            <div v-for="item in goods" :key="item.id" class="goods-card">
              <div class="goods-img">{{ ['🥔','🍑','🍌','🥬','🍄','🌽','🍎','🍇'][Math.floor(Math.random()*8)] }}</div>
              <div class="goods-info">
                <h3 @click="viewDetail(item.id)">{{ item.title }}</h3>
                <p class="goods-meta">{{ item.seller }} · {{ item.address }}</p>
                <div class="goods-bottom">
                  <span class="goods-price">{{ item.price }}</span>
                  <div class="goods-actions">
                    <el-button size="small" @click="viewDetail(item.id)">详情</el-button>
                    <el-button size="small" type="primary" @click="addToCart(item)">加入购物车</el-button>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="!loading && goods.length === 0" description="暂无商品" />
          </div>
        </el-tab-pane>

        <!-- 求购需求 -->
        <el-tab-pane label="求购需求" name="demands">
          <div class="demands-list" v-loading="loading">
            <div v-for="d in demands" :key="d.id" class="demand-card">
              <div class="demand-header">
                <h3>{{ d.title }}</h3>
                <span class="demand-price">{{ d.price }}</span>
              </div>
              <p class="demand-content">{{ d.content ? d.content.substring(0, 150) : '' }}</p>
              <p class="demand-meta">发布者：{{ d.buyer }} · {{ d.address }} · {{ d.createTime ? d.createTime.substring(0,10) : '' }}</p>
              <el-button size="small" @click="!userStore.isLoggedIn ? router.push('/login') : ElMessage.info('请联系买家')">联系买家</el-button>
            </div>
            <el-empty v-if="!loading && demands.length === 0" description="暂无求购需求" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.trade-page { padding-bottom: 40px; }
.page-banner { background: linear-gradient(135deg, #c06c3a, #e8964e); color: #fff; padding: 40px 24px; text-align: center; position: relative; }
.page-banner h1 { font-size: 28px; margin-bottom: 8px; }
.page-banner p { font-size: 16px; opacity: 0.9; margin-bottom: 16px; }
.banner-actions { display: flex; justify-content: center; gap: 12px; }
.page-body { max-width: 1200px; margin: 0 auto; padding: 24px; }
.trade-tabs { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }

.goods-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
@media (max-width: 600px) { .goods-grid { grid-template-columns: 1fr; } }
.goods-card { display: flex; border: 1px solid #eee; border-radius: 8px; overflow: hidden; transition: box-shadow 0.2s; background: #fafafa; }
.goods-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.goods-img { width: 120px; min-height: 120px; display: flex; align-items: center; justify-content: center; font-size: 48px; background: #f0f7ee; flex-shrink: 0; }
.goods-info { flex: 1; padding: 12px; display: flex; flex-direction: column; }
.goods-info h3 { font-size: 16px; color: #222; margin-bottom: 4px; cursor: pointer; }
.goods-info h3:hover { color: #488d37; }
.goods-meta { font-size: 12px; color: #999; margin-bottom: 8px; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.goods-price { font-size: 18px; color: #f03030; font-weight: 700; }
.goods-actions { display: flex; gap: 6px; }

.demands-list { display: flex; flex-direction: column; gap: 16px; }
.demand-card { background: #fafafa; border: 1px solid #eee; border-radius: 8px; padding: 16px; }
.demand-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.demand-header h3 { font-size: 16px; color: #222; }
.demand-price { font-size: 16px; color: #f03030; font-weight: 700; }
.demand-content { font-size: 14px; color: #555; line-height: 1.5; margin-bottom: 8px; }
.demand-meta { font-size: 12px; color: #999; margin-bottom: 8px; }
</style>
