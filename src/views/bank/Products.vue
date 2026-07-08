<template>
  <div>
    <h2 class="page-title">融资产品配置</h2>
    <div style="margin-bottom:16px"><el-button type="primary" @click="dialogVisible=true">新增产品</el-button></div>
    <el-table :data="products" border stripe><el-table-column prop="name" label="产品名称" /><el-table-column prop="quota" label="额度范围" /><el-table-column prop="rate" label="利率" /><el-table-column prop="term" label="期限" /><el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag :type="row.status==='启用'?'success':'info'">{{row.status}}</el-tag></template></el-table-column><el-table-column label="操作" width="140"><template #default><el-button size="small" type="primary" link>编辑</el-button><el-button size="small" type="danger" link>停用</el-button></template></el-table-column></el-table>

    <el-dialog v-model="dialogVisible" title="新增融资产品" width="500px">
      <el-form label-width="100px">
        <el-form-item label="产品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="最低额度"><el-input v-model="form.minQuota" /></el-form-item>
        <el-form-item label="最高额度"><el-input v-model="form.maxQuota" /></el-form-item>
        <el-form-item label="年利率"><el-input v-model="form.rate" />%</el-form-item>
        <el-form-item label="最长期限"><el-select v-model="form.term"><el-option label="6个月" value="6" /><el-option label="12个月" value="12" /><el-option label="24个月" value="24" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
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
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
