<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/index'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const formRef = ref(null)
const form = reactive({ goodsId: null, title: '', price: null, content: '', picture: '', address: '' })
const rules = {
  title: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await request({ url: '/api/v1/goods/my', params: { type: '1' }, method: 'get' })
    list.value = res.data || []
  } finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { goodsId: null, title: '', price: null, content: '', picture: '', address: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = true
  Object.assign(form, { goodsId: row.goodsId, title: row.title, price: row.price, content: row.content, picture: row.picture || '', address: row.address || '' })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = { title: form.title, price: form.price, content: form.content, picture: form.picture || null, address: form.address || null }
  if (editing.value) {
    payload.goodsId = form.goodsId
    await request({ url: '/api/v1/goods/update', method: 'put', data: payload })
    ElMessage.success('修改成功')
  } else {
    payload.type = '1'
    await request({ url: '/api/v1/goods/publish', method: 'post', data: payload })
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  loadList()
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该商品吗？', '提示', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
  await request({ url: '/api/v1/goods/delete/' + row.goodsId, method: 'delete' })
  ElMessage.success('删除成功')
  loadList()
}

loadList()
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">我的商品</h2>
      <el-button type="primary" :icon="'Plus'" @click="openCreate">发布供应商品</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe style="width:100%">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="title" label="商品名称" min-width="180" />
      <el-table-column label="价格" width="110">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="content" label="描述" min-width="240">
        <template #default="{ row }"><span class="ellipsis">{{ row.content }}</span></template>
      </el-table-column>
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <el-image v-if="row.picture" :src="row.picture" style="width:40px;height:30px;border-radius:4px" fit="cover" />
          <span v-else class="muted">无</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.goodsStatus === 1 ? 'success' : 'info'">{{ row.goodsStatus === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑商品' : '发布供应商品'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="title"><el-input v-model="form.title" placeholder="如：有机大米 50kg" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" :min="0" style="width:100%" placeholder="期望价格" /></el-form-item>
        <el-form-item label="商品描述" prop="content"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="详细介绍商品信息、规格、数量等" /></el-form-item>
        <el-form-item label="图片地址"><el-input v-model="form.picture" placeholder="图片 URL（可选）" /></el-form-item>
        <el-form-item label="产地地址"><el-input v-model="form.address" placeholder="产地或交易地址（可选）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">{{ editing ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0; }
.muted { color: #c0c4cc; font-size: 13px; }
.ellipsis { display: inline-block; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
