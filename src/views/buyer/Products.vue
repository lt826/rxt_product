<template>
  <div>
    <h2 class="page-title">商品管理</h2>
    <div style="margin-bottom:16px"><el-button type="primary" @click="showAdd=true">上架新商品</el-button></div>
    <el-table :data="products" border stripe style="width:100%">
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="price" label="价格（元）" width="120" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><el-tag :type="row.status==='上架'?'success':'info'">{{row.status}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180"><template #default="{row}"><el-button size="small">编辑</el-button><el-button size="small" :type="row.status==='上架'?'danger':'success'">{{row.status==='上架'?'下架':'上架'}}</el-button></template></el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const products = ref([
  { name: '有机大米 50kg', price: 280, stock: 100, status: '上架' },
  { name: '新鲜蔬菜套餐', price: 68, stock: 200, status: '上架' },
  { name: '红富士苹果', price: 12, stock: 500, status: '下架' }
])
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
