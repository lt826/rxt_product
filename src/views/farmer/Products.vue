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
</script>

<template>
  <div>
    <h2 class="page-title">商品浏览</h2>
    <el-row :gutter="16">
      <el-col v-for="item in products" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom:16px">
        <el-card :body-style="{ padding: '16px' }" shadow="hover">
          <div class="product-img">{{ item.picture || '📦' }}</div>
          <h4>{{ item.title }}</h4>
          <p class="price">¥{{ item.price }}</p>
          <p class="seller">发布者：{{ item.seller }}</p>
          <p class="seller" v-if="item.address">产地：{{ item.address }}</p>
          <el-button type="primary" size="small" style="width:100%;margin-top:8px">查看详情</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && products.length === 0" description="暂无商品数据" />
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.product-img { font-size: 48px; text-align: center; padding: 16px 0; background: #f5f7fa; border-radius: 8px; margin-bottom: 12px; }
h4 { margin: 0 0 6px; font-size: 15px; color: #303133; }
.price { color: #f56c6c; font-weight: 600; font-size: 16px; margin: 0 0 4px; }
.seller { color: #909399; font-size: 13px; margin: 0; }
</style>
