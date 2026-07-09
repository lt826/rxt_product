<script setup>
import { ref, onMounted } from 'vue'
import { getGoodsListAPI } from '../../api/goods'
import { ElMessage } from 'element-plus'

const products = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getGoodsListAPI()
    if (res.code === 200) {
      products.value = (res.data || []).map(g => ({
        id: g.goodsId,
        title: g.title,
        price: g.price,
        content: g.content,
        seller: g.ownName,
        type: g.type,
        address: g.address,
        picture: g.picture,
        createTime: g.createTime
      }))
    }
  } catch (e) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
})

function viewDetail(item) {
  ElMessage.info('查看详情：' + item.title)
}
</script>

<template>
  <div>
    <h2 class="page-title">商品货源</h2>
    <div v-loading="loading">
      <div class="goods-grid">
        <div v-for="item in products" :key="item.id" class="goods-card" @click="viewDetail(item)">
          <div class="goods-img">{{ item.picture || '📦' }}</div>
          <div class="goods-info">
            <div class="goods-title">{{ item.title }}</div>
            <div class="goods-price">
              <span class="price">¥{{ item.price }}</span>
              <span class="goods-seller">{{ item.seller }}</span>
            </div>
            <div class="goods-extra">
              <span v-if="item.address">{{ item.address }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && products.length === 0" description="暂无商品数据" />
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #222;
  margin: 0 0 20px;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}
@media (max-width: 1200px) {
  .goods-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 900px) {
  .goods-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 600px) {
  .goods-grid { grid-template-columns: repeat(2, 1fr); }
}
.goods-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 4px #eee;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.goods-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.goods-img {
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #c0c4cc;
}
.goods-info {
  padding: 10px 12px;
}
.goods-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.goods-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  color: #f03030;
  font-size: 16px;
  font-weight: bold;
}
.goods-seller {
  font-size: 12px;
  color: #999;
}
.goods-extra {
  font-size: 12px;
  color: #aaa;
  margin-top: 4px;
}
</style>
