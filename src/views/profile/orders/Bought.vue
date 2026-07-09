<script setup>
import { ref } from 'vue'
const orders = ref([])
</script>
<template>
  <div>
    <h2 class="page-title">我买到的</h2>
    <el-table :data="orders" stripe>
      <el-table-column prop="title" label="商品" min-width="200"/>
      <el-table-column prop="seller" label="卖家" width="120"/>
      <el-table-column prop="amount" label="金额" width="120"/>
      <el-table-column prop="createTime" label="购买时间" width="120"/>
      <el-table-column label="状态" width="100"><template #default><el-tag type="success">已完成</el-tag></template></el-table-column>
    </el-table>
    <el-empty description="暂无购买记录" />
  </div>
</template>
<style scoped>.page-title { font-size: 18px; margin-bottom: 16px; }</style>
