<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/index'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const formRef = ref(null)
const form = reactive({ goodsId: null, title: '', price: null, content: '', address: '' })
const rules = {
  title: [{ required: true, message: '请输入采购农产品的名称', trigger: 'blur' }],
  content: [{ required: true, message: '请描述采购需求', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await request({ url: '/api/v1/goods/my', params: { type: '2' }, method: 'get' })
    list.value = res.data || []
  } finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { goodsId: null, title: '', price: null, content: '', address: '' })
  dialogVisible.value = true
}
function openEdit(row) {
  editing.value = true
  Object.assign(form, { goodsId: row.goodsId, title: row.title, price: row.price, content: row.content, address: row.address || '' })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = { title: form.title, price: form.price, content: form.content, address: form.address || null }
  if (editing.value) {
    payload.goodsId = form.goodsId
    await request({ url: '/api/v1/goods/update', method: 'put', data: payload })
    ElMessage.success('修改成功')
  } else {
    payload.type = '2'
    const res = await request({ url: '/api/v1/goods/publish', method: 'post', data: payload })
    if (res && res.code === 200) {
      ElMessage.success('发布成功')
    } else {
      ElMessage.error(res?.message || '发布失败')
      return
    }
  }
  dialogVisible.value = false
  loadList()
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该采购需求吗？', '提示', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
  await request({ url: '/api/v1/goods/delete/' + row.goodsId, method: 'delete' })
  ElMessage.success('删除成功')
  loadList()
}

onMounted(() => { loadList() })
</script>

<template>
  <div class="demand-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">收购需求</h2>
        <p class="page-sub">发布你的采购需求，让农户主动联系你</p>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon style="margin-right:4px"><Plus /></el-icon> 发布需求
      </el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="title" label="农产品" min-width="200">
          <template #default="{ row }">
            <span style="font-weight:600;color:var(--text)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最高收购价" width="120">
          <template #default="{ row }"><span class="price-tag">¥{{ row.price || '面议' }}</span></template>
        </el-table-column>
        <el-table-column prop="content" label="需求详情" min-width="260">
          <template #default="{ row }"><span class="ellipsis">{{ row.content }}</span></template>
        </el-table-column>
        <el-table-column prop="address" label="收货地址" width="160">
          <template #default="{ row }">{{ row.address || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!list.length && !loading" description="还没有发布收购需求">
        <el-button type="primary" @click="openCreate">立即发布</el-button>
      </el-empty>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑需求' : '发布收购需求'" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="农产品名称" prop="title"><el-input v-model="form.title" placeholder="如：有机大米" /></el-form-item>
        <el-form-item label="最高收购价" prop="price"><el-input-number v-model="form.price" :precision="2" :min="0" style="width:100%" placeholder="元/kg（可选，空表示面议）" /></el-form-item>
        <el-form-item label="需求详情" prop="content"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="需求量、规格、品质要求等" /></el-form-item>
        <el-form-item label="收货地址"><el-input v-model="form.address" placeholder="收货地址（可选）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">{{ editing ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.demand-page { max-width: 1200px; }
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.price-tag { color: var(--amber); font-weight: 700; font-size: 16px; }
.ellipsis { display: inline-block; max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
