<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  submitFinancingIntentionAPI,
  getMyFinancingIntentionsAPI,
  updateFinancingIntentionAPI,
  deleteFinancingIntentionAPI
} from '../../api/finance'

const loading = ref(false)
const submitting = ref(false)
const intentions = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)

const form = ref({
  amount: null,
  application: '',
  item: '',
  repaymentPeriod: 12,
  area: null
})

const crops = ['小麦', '玉米', '水稻', '大豆', '棉花', '蔬菜', '果树', '其他']
const repaymentOptions = [3, 6, 12, 18, 24, 36]

const statusMap = {
  0: '待审核',
  1: '审核通过',
  2: '已拒绝'
}

const statusTypeMap = {
  0: 'warning',
  1: 'success',
  2: 'danger'
}

// 加载融资意向列表
async function loadIntentions() {
  loading.value = true
  try {
    const res = await getMyFinancingIntentionsAPI()
    if (res.code === 200) {
      intentions.value = res.data || []
    }
  } catch {
    ElMessage.error('获取融资意向失败')
  } finally {
    loading.value = false
  }
}

// 打开新增对话框
function openAddDialog() {
  editingId.value = null
  form.value = {
    amount: null,
    application: '',
    item: '',
    repaymentPeriod: 12,
    area: null
  }
  dialogVisible.value = true
}

// 打开编辑对话框
function openEditDialog(intention) {
  editingId.value = intention.id
  form.value = {
    amount: intention.amount,
    application: intention.application || '',
    item: intention.item || '',
    repaymentPeriod: intention.repaymentPeriod || 12,
    area: intention.area || null
  }
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  // 表单验证
  if (!form.value.amount || form.value.amount <= 0) {
    ElMessage.warning('请输入有效的融资金额')
    return
  }
  if (!form.value.application) {
    ElMessage.warning('请输入融资用途')
    return
  }
  if (!form.value.item) {
    ElMessage.warning('请选择融资农作物')
    return
  }
  if (!form.value.repaymentPeriod) {
    ElMessage.warning('请选择还款期限')
    return
  }
  if (!form.value.area || form.value.area <= 0) {
    ElMessage.warning('请输入有效的种植面积')
    return
  }

  submitting.value = true
  try {
    if (editingId.value) {
      // 更新
      const res = await updateFinancingIntentionAPI(editingId.value, form.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        await loadIntentions()
      }
    } else {
      // 新增
      const res = await submitFinancingIntentionAPI(form.value)
      if (res.code === 200) {
        ElMessage.success('融资意向已提交')
        dialogVisible.value = false
        await loadIntentions()
      }
    }
  } catch {
    // handled in interceptor
  } finally {
    submitting.value = false
  }
}

// 删除融资意向
async function handleDelete(intention) {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条融资意向吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await deleteFinancingIntentionAPI(intention.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadIntentions()
    }
  } catch {
    // 用户取消
  }
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.substring(0, 10)
}

onMounted(loadIntentions)
</script>

<template>
  <div class="intention-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">融资意向</h2>
        <p class="page-sub">填写您的融资需求，便于银行匹配合适的产品</p>
      </div>
      <el-button type="primary" size="large" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        新增意向
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="intentions" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="amount" label="融资金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="application" label="融资用途" min-width="150" />
        <el-table-column prop="item" label="融资农作物" width="120" />
        <el-table-column prop="repaymentPeriod" label="还款期限" width="100">
          <template #default="{ row }">
            {{ row.repaymentPeriod }}个月
          </template>
        </el-table-column>
        <el-table-column prop="area" label="种植面积" width="100">
          <template #default="{ row }">
            {{ row.area }}亩
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && intentions.length === 0" description="暂无融资意向，点击右上角新增" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑融资意向' : '新增融资意向'"
      width="500px"
      @close="() => dialogVisible = false"
    >
      <el-form label-width="120px">
        <el-form-item label="融资金额" required>
          <el-input
            v-model="form.amount"
            placeholder="请输入融资金额（元）"
            type="number"
            :min="0"
          >
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="融资用途" required>
          <el-input
            v-model="form.application"
            placeholder="如：购买种子、化肥、农机等"
            :rows="3"
            type="textarea"
          />
        </el-form-item>
        <el-form-item label="融资农作物" required>
          <el-select v-model="form.item" placeholder="请选择农作物" style="width: 100%">
            <el-option v-for="crop in crops" :key="crop" :label="crop" :value="crop" />
          </el-select>
        </el-form-item>
        <el-form-item label="还款期限" required>
          <el-select v-model="form.repaymentPeriod" placeholder="请选择还款期限" style="width: 100%">
            <el-option
              v-for="month in repaymentOptions"
              :key="month"
              :label="month + '个月'"
              :value="month"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="种植面积" required>
          <el-input
            v-model="form.area"
            placeholder="请输入种植面积"
            type="number"
            :min="0"
          >
            <template #append>亩</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingId ? '保存修改' : '提交意向' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.intention-page { max-width: 1200px; margin: 0 auto; }
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0 0 4px;
}
.page-sub {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}
.table-card {
  border-radius: var(--radius-md);
}
</style>
