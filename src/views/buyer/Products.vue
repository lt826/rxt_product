<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/index'

const loading = ref(false)
const keyword = ref('')
const list = ref([])

async function loadList() {
  loading.value = true
  try {
    const res = await request({ url: '/api/v1/goods/list', params: { type: '1', keyword: keyword.value }, method: 'get' })
    list.value = res.data || []
  } finally { loading.value = false }
}

function purchase(row) {
  ElMessage.success('已加入购物车：' + row.title + '，请前往结算')
}

loadList()
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">商品采购</h2>
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width:280px" @keyup.enter="loadList" @clear="loadList">
          <template #append><el-button @click="loadList">搜索</el-button></template>
        </el-input>
      </div>
    </div>

    <div v-loading="loading">
      <div class="goods-grid">
        <div v-for="item in list" :key="item.goodsId" class="goods-card">
          <div class="goods-img">{{ item.picture ? '🖼️' : '📦' }}</div>
          <div class="goods-info">
            <div class="goods-title">{{ item.title }}</div>
            <div class="goods-price">
              <span class="price">¥{{ item.price }}</span>
              <span class="goods-seller">{{ item.ownName }}</span>
            </div>
            <div v-if="item.address" class="goods-extra">{{ item.address }}</div>
            <el-button type="primary" size="small" style="width:100%;margin-top:8px" @click="purchase(item)">立即购买</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!list.length && !loading" description="暂无农户供应商品" />
    </div>
  </div>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-title { font-size: 20px; font-weight: 600; color: #222; margin: 0; }
.search-area { display: flex; align-items: center; }
.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}
@media (max-width: 1200px) { .goods-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 900px) { .goods-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 600px) { .goods-grid { grid-template-columns: repeat(2, 1fr); } }
.goods-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 4px #eee;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.goods-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.12); }
.goods-img {
  width: 100%;
  height: 160px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
  color: #c0c4cc;
}
.goods-info { padding: 10px 12px; }
.goods-title { font-size: 14px; color: #333; margin-bottom: 6px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.price { color: #f03030; font-size: 16px; font-weight: bold; }
.goods-price { display: flex; justify-content: space-between; align-items: center; }
.goods-seller { font-size: 12px; color: #999; }
.goods-extra { font-size: 12px; color: #aaa; margin-top: 2px; }
</style>
