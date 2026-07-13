<template>
  <div class="products-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">融资产品配置</h2>
        <p class="page-sub">维护银行的融资产品（增 / 删 / 查 / 改）</p>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon style="margin-right:4px"><Plus /></el-icon> 新增产品
      </el-button>
    </div>

    <!-- 查询 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="产品名称">
          <el-input v-model="query.name" placeholder="按名称搜索" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:140px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table :data="filteredProducts" stripe v-loading="loading" empty-text="暂无产品数据">
        <el-table-column label="产品名称" prop="name" min-width="120" />
        <el-table-column label="额度范围" min-width="170">
          <template #default="{ row }">{{ quotaRange(row) }}</template>
        </el-table-column>
        <el-table-column label="年利率" width="100">
          <template #default="{ row }">{{ row.rate }}%</template>
        </el-table-column>
        <el-table-column label="期限" min-width="130">
          <template #default="{ row }">{{ termRange(row) }}</template>
        </el-table-column>
        <el-table-column label="联系电话" prop="contactPhone" min-width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑融资产品' : '新增融资产品'"
      width="580px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" placeholder="如：春耕贷" maxlength="100" />
        </el-form-item>
        <el-form-item label="最低额度" prop="minQuota">
          <el-input-number
            v-model="form.minQuota"
            :min="0"
            :step="1000"
            :precision="2"
            controls-position="right"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="最高额度" prop="maxQuota">
          <el-input-number
            v-model="form.maxQuota"
            :min="0"
            :step="1000"
            :precision="2"
            controls-position="right"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="年利率(%)" prop="rate">
          <el-input-number
            v-model="form.rate"
            :min="0"
            :step="0.1"
            :precision="2"
            controls-position="right"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="最短期限(月)" prop="minTerm">
          <el-input-number
            v-model="form.minTerm"
            :min="1"
            :step="1"
            :precision="0"
            controls-position="right"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="最长期限(月)" prop="maxTerm">
          <el-input-number
            v-model="form.maxTerm"
            :min="1"
            :step="1"
            :precision="0"
            controls-position="right"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="如：400-123-4567" maxlength="20" />
        </el-form-item>
        <el-form-item label="产品介绍" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getFinancingProductListAPI,
  createFinancingProductAPI,
  updateFinancingProductAPI,
  deleteFinancingProductAPI,
  updateFinancingProductStatusAPI
} from '../../api/finance'

const loading = ref(false)
const saving = ref(false)
const products = ref([])

// 查询条件
const query = reactive({ name: '', status: '' })

// 前端二次筛选（接口返回全部，这里按名称/状态过滤）
const filteredProducts = computed(() => {
  return products.value.filter((p) => {
    const okName = !query.name || (p.name || '').includes(query.name.trim())
    const okStatus = query.status === '' || p.status === query.status
    return okName && okStatus
  })
})

function resetQuery() {
  query.name = ''
  query.status = ''
}

// 金额格式化：1万以上用“万”
function formatWan(n) {
  if (n === null || n === undefined || n === '') return '-'
  const num = Number(n)
  if (isNaN(num)) return '-'
  if (num >= 10000) {
    const w = num / 10000
    return (Number.isInteger(w) ? String(w) : w.toFixed(2).replace(/\.?0+$/, '')) + '万'
  }
  return String(num)
}
function quotaRange(row) {
  return `${formatWan(row.minQuota)} ~ ${formatWan(row.maxQuota)} 元`
}
function termRange(row) {
  return `${row.minTerm} ~ ${row.maxTerm} 个月`
}

async function loadList() {
  loading.value = true
  try {
    const res = await getFinancingProductListAPI()
    if (res.code === 200) {
      products.value = res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    // 错误已由 axios 拦截器统一提示
  } finally {
    loading.value = false
  }
}

// ===== 新增 / 编辑 =====
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const defaultForm = () => ({
  id: undefined,
  name: '',
  minQuota: undefined,
  maxQuota: undefined,
  rate: undefined,
  minTerm: undefined,
  maxTerm: undefined,
  contactPhone: '',
  description: '',
  status: 1
})
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  minQuota: [{ required: true, message: '请输入最低额度', trigger: 'blur' }],
  maxQuota: [
    { required: true, message: '请输入最高额度', trigger: 'blur' },
    {
      validator: (_rule, value, cb) => {
        if (
          value !== undefined && value !== null &&
          form.minQuota !== undefined && form.minQuota !== null &&
          Number(value) < Number(form.minQuota)
        ) {
          cb(new Error('最高额度不能低于最低额度'))
        } else {
          cb()
        }
      },
      trigger: 'blur'
    }
  ],
  rate: [{ required: true, message: '请输入年利率', trigger: 'blur' }],
  minTerm: [{ required: true, message: '请输入最短期限', trigger: 'blur' }],
  maxTerm: [
    { required: true, message: '请输入最长期限', trigger: 'blur' },
    {
      validator: (_rule, value, cb) => {
        if (
          value !== undefined && value !== null &&
          form.minTerm !== undefined && form.minTerm !== null &&
          Number(value) < Number(form.minTerm)
        ) {
          cb(new Error('最长期限不能小于最短期限'))
        } else {
          cb()
        }
      },
      trigger: 'blur'
    }
  ]
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, defaultForm(), {
    id: row.id,
    name: row.name,
    minQuota: Number(row.minQuota),
    maxQuota: Number(row.maxQuota),
    rate: Number(row.rate),
    minTerm: row.minTerm,
    maxTerm: row.maxTerm,
    contactPhone: row.contactPhone || '',
    description: row.description || '',
    status: row.status
  })
  dialogVisible.value = true
}

async function save() {
  try {
    await formRef.value.validate()
  } catch {
    return // 校验未通过
  }
  saving.value = true
  try {
    const payload = { ...form }
    let res
    if (isEdit.value) {
      res = await updateFinancingProductAPI(form.id, payload)
    } else {
      delete payload.id
      res = await createFinancingProductAPI(payload)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '产品创建成功')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    // 错误已由 axios 拦截器统一提示
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    const res = await updateFinancingProductStatusAPI(row.id, next)
    if (res.code === 200) {
      ElMessage.success(next === 1 ? '已启用' : '已停用')
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    // 错误已由 axios 拦截器统一提示
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除产品「${row.name}」吗？该操作不可恢复。`, '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return // 用户取消
  }
  try {
    const res = await deleteFinancingProductAPI(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    // 错误已由 axios 拦截器统一提示
  }
}

onMounted(loadList)
</script>

<style scoped>
.products-page { max-width: 1200px; }
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
.filter-card { margin-bottom: 16px; }
.filter-card :deep(.el-card__body) { padding-bottom: 2px; }
.filter-card :deep(.el-form-item) { margin-bottom: 12px; }
</style>
