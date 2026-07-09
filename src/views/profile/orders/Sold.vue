<script setup>
import { ref } from 'vue'
const orders = ref([])
</script>
<template>
  <div>
    <h2 class="page-title">我卖出的</h2>
    <el-table :data="orders" stripe>
      <el-table-column prop="title" label="商品" min-width="200"/>
      <el-table-column prop="buyer" label="买家" width="120"/>
      <el-table-column prop="amount" label="金额" width="120"/>
      <el-table-column prop="createTime" label="成交时间" width="120"/>
      <el-table-column label="状态" width="100"><template #default><el-tag type="success">已完成</el-tag></template></el-table-column>
    </el-table>
    <el-empty description="暂无卖出记录" />
  </div>
</template>
<style scoped>.page-title { font-size: 18px; margin-bottom: 16px; }</style>
