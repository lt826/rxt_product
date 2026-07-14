<template>
  <div class="goods-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">商品管理</h2>
        <p class="page-sub">审核并管理平台所有商品，清理违规内容</p>
      </div>
      <el-button
        type="danger"
        :disabled="selection.length === 0"
        :loading="batchDeleting"
        @click="handleBatchDelete"
      >
        批量删除<span v-if="selection.length">（{{ selection.length }}）</span>
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-controls">
        <span class="filter-label">类型</span>
        <el-select v-model="typeFilter" placeholder="全部" clearable class="type-select">
          <el-option label="销售供应" value="销售供应" />
          <el-option label="采购需求" value="采购需求" />
        </el-select>
        <span class="filter-label">标题</span>
        <el-input v-model="titleKeyword" placeholder="标题关键词" clearable class="title-input" />
        <span class="filter-label">发布者</span>
        <el-input v-model="ownerKeyword" placeholder="发布者" clearable class="owner-input" />
        <span class="filter-label">价格</span>
        <el-input-number v-model="priceMin" :min="0" :precision="2" placeholder="最低" controls-position="right" class="price-input" />
        <span class="filter-sep">-</span>
        <el-input-number v-model="priceMax" :min="0" :precision="2" placeholder="最高" controls-position="right" class="price-input" />
        <span class="filter-label">排序</span>
        <el-select v-model="sortOrder" placeholder="默认排序" clearable class="sort-select">
          <el-option label="价格升序" value="asc" />
          <el-option label="价格降序" value="desc" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="filteredGoods" v-loading="loading" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-row">
              <div v-if="row.picture && row.picture.startsWith('http')" class="expand-img">
                <img :src="row.picture" :alt="row.title" @error="row.picture = ''" />
              </div>
              <div class="expand-body">
                <div class="expand-item"><span class="expand-key">商品描述：</span>{{ row.content || '-' }}</div>
                <div class="expand-item"><span class="expand-key">产地：</span>{{ row.address || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">{{ row.price != null ? `¥${row.price}` : '面议' }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.type === '销售供应' ? 'success' : 'warning'">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="发布者" width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && goods.length === 0" description="暂无商品" />
      <el-empty v-else-if="!loading && filteredGoods.length === 0" description="没有符合条件的商品">
        <el-button @click="resetFilters">清空筛选条件</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getGoodsListAPI, deleteGoodsAPI } from '../../api/goods'
import { ElMessage, ElMessageBox } from 'element-plus'

const goods = ref([])
const loading = ref(false)
const selection = ref([])
const batchDeleting = ref(false)

// 筛选条件
const typeFilter = ref('')
const titleKeyword = ref('')
const ownerKeyword = ref('')
const priceMin = ref(undefined)
const priceMax = ref(undefined)
const sortOrder = ref('')

const filteredGoods = computed(() => {
  let arr = goods.value.slice()
  if (typeFilter.value) arr = arr.filter(g => g.type === typeFilter.value)
  const t = titleKeyword.value.trim()
  if (t) arr = arr.filter(g => g.title && String(g.title).includes(t))
  const o = ownerKeyword.value.trim()
  if (o) arr = arr.filter(g => g.owner && String(g.owner).includes(o))
  if (priceMin.value != null && priceMin.value !== '') {
    arr = arr.filter(g => g.price != null && Number(g.price) >= Number(priceMin.value))
  }
  if (priceMax.value != null && priceMax.value !== '') {
    arr = arr.filter(g => g.price != null && Number(g.price) <= Number(priceMax.value))
  }
  if (sortOrder.value === 'asc') {
    arr = arr.slice().sort((a, b) => Number(a.price || 0) - Number(b.price || 0))
  } else if (sortOrder.value === 'desc') {
    arr = arr.slice().sort((a, b) => Number(b.price || 0) - Number(a.price || 0))
  }
  return arr
})

onMounted(loadGoods)

async function loadGoods() {
  loading.value = true
  try {
    const res = await getGoodsListAPI()
    if (res.code === 200) {
      goods.value = (res.data || []).map(g => ({
        id: g.goodsId,
        title: g.title,
        price: g.price,
        type: g.type === '1' ? '销售供应' : '采购需求',
        owner: g.ownName,
        createTime: g.createTime?.substring(0, 10) || '-',
        content: g.content,
        address: g.address,
        picture: g.picture
      }))
    }
  } catch (e) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

function onSelectionChange(rows) {
  selection.value = rows
}

function resetFilters() {
  typeFilter.value = ''
  titleKeyword.value = ''
  ownerKeyword.value = ''
  priceMin.value = undefined
  priceMax.value = undefined
  sortOrder.value = ''
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除商品"${row.title}"吗？`, '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
    loading.value = true
    const res = await deleteGoodsAPI(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadGoods()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  } finally {
    loading.value = false
  }
}

async function handleBatchDelete() {
  if (selection.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selection.value.length} 条商品吗？`, '批量删除', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
    batchDeleting.value = true
    let ok = 0
    let fail = 0
    for (const item of selection.value) {
      try {
        const res = await deleteGoodsAPI(item.id)
        if (res.code === 200) ok++
        else fail++
      } catch (e) {
        fail++
      }
    }
    if (fail === 0) {
      ElMessage.success(`成功删除 ${ok} 条商品`)
    } else {
      ElMessage.warning(`成功 ${ok} 条，失败 ${fail} 条`)
    }
    await loadGoods()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('批量删除失败')
  } finally {
    batchDeleting.value = false
  }
}
</script>

<style scoped>
.goods-page { max-width: 1200px; }

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  padding: 14px 18px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
}
.filter-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-label { font-size: 13px; color: var(--text-muted); }
.filter-sep { color: var(--text-muted); }
.type-select { width: 130px; }
.title-input { width: 170px; }
.owner-input { width: 140px; }
.price-input { width: 110px; }
.sort-select { width: 130px; }

/* 行展开 */
.expand-row {
  display: flex;
  gap: 16px;
  padding: 8px 16px 16px 56px;
}
.expand-img {
  width: 120px;
  height: 120px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--bg);
  flex-shrink: 0;
}
.expand-img img { width: 100%; height: 100%; object-fit: cover; }
.expand-body { flex: 1; }
.expand-item { font-size: 13px; color: var(--text-secondary); line-height: 1.7; }
.expand-key { color: var(--text-muted); font-weight: 600; }
</style>
