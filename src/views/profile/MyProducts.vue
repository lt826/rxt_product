<script setup>
import { ref, onMounted } from 'vue'
import { getMyGoodsAPI } from '../../api/goods'
import { ElMessage } from 'element-plus'

const goods = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ title: '', price: null, content: '', type: '1', address: '' })

async function loadGoods() {
  loading.value = true
  try {
    const res = await getMyGoodsAPI({ type: '1' })
    if (res.code === 200) {
      goods.value = (res.data || []).map(g => ({
        id: g.goodsId, title: g.title, price: g.price ? `¥${g.price}` : '面议',
        content: g.content, status: g.goodsStatus, address: g.address,
        createTime: g.createTime ? g.createTime.substring(0,10) : '-'
      }))
    }
  } catch(e) { ElMessage.error('获取商品列表失败') }
  finally { loading.value = false }
}

onMounted(loadGoods)
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">商品货源管理</h2>
      <el-button type="primary" @click="dialogVisible = true">发布商品</el-button>
    </div>
    <el-table :data="goods" v-loading="loading" stripe>
      <el-table-column prop="title" label="商品名称" min-width="180" />
      <el-table-column prop="price" label="价格" width="120" />
      <el-table-column prop="address" label="产地" width="150" />
      <el-table-column prop="createTime" label="发布时间" width="120" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button size="small" text type="primary">编辑</el-button>
          <el-button size="small" text type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && goods.length === 0" description="暂无商品" />

    <el-dialog v-model="dialogVisible" title="发布商品" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="产地地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="loadGoods(); dialogVisible = false">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
