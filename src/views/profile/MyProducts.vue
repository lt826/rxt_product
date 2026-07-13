<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyGoodsAPI, publishGoodsAPI, updateGoodsAPI, deleteGoodsAPI } from '../../api/goods'
import { ElMessage, ElMessageBox } from 'element-plus'

const goods = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const form = ref({ title: '', price: null, content: '', address: '', picture: '' })

const dialogTitle = computed(() => editingId.value ? '编辑商品' : '发布商品')

async function loadGoods() {
  loading.value = true
  try {
    const res = await getMyGoodsAPI({ type: '1' })
    if (res.code === 200) {
      goods.value = (res.data || []).map(g => ({
        id: g.goodsId, title: g.title,
        price: g.price ? `¥${g.price}` : '面议',
        rawPrice: g.price != null ? Number(g.price) : null,
        content: g.content, address: g.address, picture: g.picture || '',
        createTime: g.createTime ? g.createTime.substring(0, 10) : '-'
      }))
    }
  } catch (e) { ElMessage.error('获取商品列表失败') }
  finally { loading.value = false }
}

function openCreate() {
  editingId.value = null
  form.value = { title: '', price: null, content: '', address: '', picture: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    title: row.title || '',
    price: row.rawPrice,
    content: row.content || '',
    address: row.address || '',
    picture: row.picture || ''
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.title?.trim()) return ElMessage.warning('请输入商品名称')
  if (form.value.price == null || form.value.price < 0) return ElMessage.warning('请输入有效的价格')
  submitting.value = true
  const editing = !!editingId.value
  const successMsg = editing ? '修改成功' : '发布成功'
  try {
    const payload = {
      title: form.value.title.trim(),
      price: Number(form.value.price),
      content: form.value.content?.trim() || '',
      address: form.value.address?.trim() || '',
      picture: form.value.picture?.trim() || '',
      type: '1'
    }
    const res = editing
      ? await updateGoodsAPI(editingId.value, payload)
      : await publishGoodsAPI(payload)
    if (res.code === 200) {
      ElMessage.success(successMsg)
      dialogVisible.value = false
      await loadGoods()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.title}」吗？`, '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
  } catch (e) { return }
  try {
    const res = await deleteGoodsAPI(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadGoods()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(loadGoods)
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">商品货源管理</h2>
      <el-button type="primary" @click="openCreate">发布商品</el-button>
    </div>
    <el-table :data="goods" v-loading="loading" stripe>
      <el-table-column prop="title" label="商品名称" min-width="180" />
      <el-table-column prop="price" label="价格" width="120" />
      <el-table-column prop="address" label="产地" width="150" />
      <el-table-column prop="createTime" label="发布时间" width="120" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && goods.length === 0" description="暂无商品" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.title" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" controls-position="right" />
        </el-form-item>
        <el-form-item label="产地地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="图片链接">
          <el-input v-model="form.picture" placeholder="可选，填写图片URL" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">{{ editingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
