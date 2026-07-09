<template>
  <div class="products-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">融资产品配置</h2>
        <p class="page-sub">管理银行的融资产品</p>
      </div>
      <el-button type="primary" @click="dialogVisible=true">
        <el-icon style="margin-right:4px"><Plus /></el-icon> 新增产品
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="products" stripe>
        <el-table-column prop="name" label="产品名称" />
        <el-table-column prop="quota" label="额度范围" />
        <el-table-column prop="rate" label="利率" />
        <el-table-column prop="term" label="期限" />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status==='启用'?'success':'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default>
            <el-button size="small" type="primary" link>编辑</el-button>
            <el-button size="small" type="danger" link>停用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增融资产品" width="500px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="产品名称"><el-input v-model="form.name" placeholder="如：春耕贷" /></el-form-item>
        <el-form-item label="最低额度"><el-input v-model="form.minQuota" placeholder="如：10000" /></el-form-item>
        <el-form-item label="最高额度"><el-input v-model="form.maxQuota" placeholder="如：100000" /></el-form-item>
        <el-form-item label="年利率"><el-input v-model="form.rate" placeholder="如：3.85" />%</el-form-item>
        <el-form-item label="最长期限">
          <el-select v-model="form.term" style="width:100%">
            <el-option label="6个月" value="6" /><el-option label="12个月" value="12" /><el-option label="24个月" value="24" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const form = ref({ name:'', minQuota:'', maxQuota:'', rate:'', term:'' })
const products = ref([
  { name:'春耕贷', quota:'1万-10万', rate:'3.85%', term:'6-12月', status:'启用' },
  { name:'惠农贷', quota:'5千-5万', rate:'4.35%', term:'6-24月', status:'启用' },
  { name:'创业贷', quota:'5万-50万', rate:'4.75%', term:'12-36月', status:'停用' }
])
function save() { dialogVisible.value = false; ElMessage.success('产品已创建') }
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
</style>
