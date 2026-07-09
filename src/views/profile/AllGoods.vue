<script setup>
import { ref, onMounted } from 'vue'
import { getGoodsListAPI } from '../../api/goods'
import { ElMessage } from 'element-plus'

const goods = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getGoodsListAPI()
    if (res.code === 200) {
      goods.value = (res.data || []).map(g => ({
        id: g.goodsId, title: g.title, price: g.price ? `¥${g.price}` : '面议',
        type: g.type === '1' ? '销售供应' : '采购需求',
        owner: g.ownName, createTime: g.createTime?.substring(0,10) || '-'
      }))
    }
  } catch(e) { ElMessage.error('获取商品列表失败') }
  finally { loading.value = false }
})
</script>

<template>
  <div>
    <h2 class="page-title">所有商品管理</h2>
    <el-table :data="goods" v-loading="loading" stripe>
      <el-table-column prop="id" label="编号" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="price" label="价格" width="120" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{row}"><el-tag :type="row.type === '销售供应' ? 'success' : 'warning'">{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="owner" label="发布者" width="120" />
      <el-table-column prop="createTime" label="发布时间" width="110" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button size="small" text type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && goods.length === 0" description="暂无商品" />
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
</style>
