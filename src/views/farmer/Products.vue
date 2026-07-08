<template>
  <div>
    <h2 class="page-title">商品浏览</h2>
    <el-row :gutter="16">
      <el-col v-for="item in products" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom:16px">
        <el-card :body-style="{ padding: '16px' }" shadow="hover">
          <div class="product-img">{{ item.icon }}</div>
          <h4>{{ item.name }}</h4>
          <p class="price">¥{{ item.price }}/{{ item.unit }}</p>
          <p class="seller">{{ item.seller }}</p>
          <el-button type="primary" size="small" style="width:100%;margin-top:8px">立即购买</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const products = [
  { id: 1, icon: '🌾', name: '有机大米 50kg', price: 280, unit: '袋', seller: '绿源合作社' },
  { id: 2, icon: '🥬', name: '新鲜蔬菜套餐', price: 68, unit: '份', seller: '阳光农场' },
  { id: 3, icon: '🍎', name: '红富士苹果', price: 12, unit: '斤', seller: '丰收果园' },
  { id: 4, icon: '🥩', name: '土鸡蛋 30枚', price: 45, unit: '盒', seller: '山村养殖场' }
]
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.product-img { font-size: 48px; text-align: center; padding: 16px 0; background: #f5f7fa; border-radius: 8px; margin-bottom: 12px; }
h4 { margin: 0 0 6px; font-size: 15px; color: #303133; }
.price { color: #f56c6c; font-weight: 600; font-size: 16px; margin: 0 0 4px; }
.seller { color: #909399; font-size: 13px; margin: 0; }
</style>
