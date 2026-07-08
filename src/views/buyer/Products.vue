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
  ElMessage.success('\u5df2\u52a0\u5165\u8d2d\u7269\u8f66: ' + row.title + '\uff0c\u8bf7\u524d\u5f80\u7ed3\u7b97')
}

loadList()
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">商品采购</h2>
      <el-input v-model="keyword" placeholder="搜索商品名称或描述" clearable style="width: 280px" @keyup.enter="loadList" @clear="loadList">
        <template #append><el-button :icon="'Search'" @click="loadList" /></template>
      </el-input>
    </div>

    <div v-loading="loading">
      <el-row :gutter="16">
        <el-col v-for="item in list" :key="item.goodsId" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 16px">
          <el-card :body-style="{ padding: '16px' }" shadow="hover">
            <el-image v-if="item.picture" :src="item.picture" class="g-img" fit="cover" />
            <div v-else class="g-img-placeholder"><el-icon :size="36"><Goods /></el-icon></div>
            <h4 class="g-title">{{ item.title }}</h4>
            <p class="g-price">¥{{ item.price }}</p>
            <p class="g-meta">
              <span class="g-farmer">{{ item.ownName }}</span>
              <span v-if="item.address" class="g-addr">{{ item.address }}</span>
            </p>
            <p class="g-desc">{{ item.content }}</p>
            <el-button type="primary" size="small" style="width:100%;margin-top:4px" @click="purchase(item)">立即购买</el-button>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!list.length && !loading" description="暂无农户供应商品" />
    </div>
  </div>
</template>

<style scoped>
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0; }
.g-img { width:100%; height:140px; border-radius:6px; margin-bottom:12px; }
.g-img-placeholder { width:100%; height:140px; background:#f5f7fa; border-radius:6px; margin-bottom:12px; display:flex; align-items:center; justify-content:center; color:#c0c4cc; }
.g-title { margin:0 0 6px; font-size:15px; color:#303133; }
.g-price { color:#f56c6c; font-weight:600; font-size:18px; margin:0 0 4px; }
.g-meta { display:flex; gap:12px; font-size:13px; color:#909399; margin:0 0 8px; }
.g-desc { color:#606266; font-size:13px; margin:0; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; line-height:1.5; }
</style>
