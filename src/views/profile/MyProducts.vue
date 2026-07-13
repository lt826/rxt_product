<script setup>
import { ref, onMounted } from 'vue'
import { getMyGoodsAPI } from '../../api/goods'

// 农户端：查看自己发布的商品（发布/编辑在农户主页面进行）
const goods = ref([])
const loading = ref(false)

async function loadGoods() {
  loading.value = true
  try {
    const res = await getMyGoodsAPI({ type: '1' })
    if (res.code === 200) {
      goods.value = (res.data || []).map(g => ({
        id: g.goodsId,
        title: g.title,
        price: g.price ? `¥${g.price}` : '面议',
        content: g.content,
        address: g.address || '-',
        createTime: g.createTime ? g.createTime.substring(0, 10) : '-'
      }))
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(loadGoods)
</script>

<template>
  <div>
    <h2 class="page-title">商品货源管理</h2>
    <el-table :data="goods" v-loading="loading" stripe>
      <el-table-column prop="title" label="商品名称" min-width="180" />
      <el-table-column prop="price" label="价格" width="120" />
      <el-table-column prop="address" label="产地" width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="发布时间" width="120" />
    </el-table>
    <el-empty v-if="!loading && goods.length === 0" description="暂无商品" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
