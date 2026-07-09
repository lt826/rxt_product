<script setup>
import { ref, onMounted } from 'vue'
import { getMyGoodsAPI } from '../../api/goods'
import { ElMessage } from 'element-plus'

const demands = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ title: '', price: null, content: '', address: '' })

async function loadDemands() {
  loading.value = true
  try {
    const res = await getMyGoodsAPI({ type: '2' })
    if (res.code === 200) {
      demands.value = (res.data || []).map(d => ({
        id: d.goodsId, title: d.title, price: d.price ? `¥${d.price}` : '面议',
        content: d.content, address: d.address,
        createTime: d.createTime ? d.createTime.substring(0,10) : '-'
      }))
    }
  } catch(e) { ElMessage.error('获取需求列表失败') }
  finally { loading.value = false }
}
onMounted(loadDemands)
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">求购需求管理</h2>
      <el-button type="primary" @click="dialogVisible = true">发布需求</el-button>
    </div>
    <el-table :data="demands" v-loading="loading" stripe>
      <el-table-column prop="title" label="需求标题" min-width="200" />
      <el-table-column prop="price" label="期望价格" width="120" />
      <el-table-column prop="address" label="地区" width="150" />
      <el-table-column prop="createTime" label="发布时间" width="120" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button size="small" text type="primary">编辑</el-button>
          <el-button size="small" text type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && demands.length === 0" description="暂无需求" />

    <el-dialog v-model="dialogVisible" title="发布求购需求" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="需求标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="期望价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="地区"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="需求描述"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="loadDemands(); dialogVisible = false">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
