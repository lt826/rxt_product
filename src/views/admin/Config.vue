<template>
  <div>
    <h2 class="page-title">系统配置</h2>
    <el-card style="margin-bottom:20px"><template #header><span>平台参数</span></template><el-form label-width="180px">
      <el-form-item label="平台名称"><el-input v-model="config.platformName" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="config.phone" /></el-form-item>
      <el-form-item label="备案号"><el-input v-model="config.icp" /></el-form-item>
      <el-form-item label="新用户默认角色"><el-select v-model="config.defaultRole"><el-option label="农户" value="farmer" /></el-select></el-form-item>
      <el-form-item label="融资审批方式"><el-radio-group v-model="config.approvalMode"><el-radio value="single">单人审批</el-radio><el-radio value="multi">多人会签</el-radio></el-radio-group></el-form-item>
      <el-form-item><el-button type="primary" @click="saveConfig">保存配置</el-button></el-form-item>
    </el-form></el-card>
    <el-card><template #header><span>消息模板</span></template><el-table :data="templates" border stripe><el-table-column prop="code" label="编码" width="180" /><el-table-column prop="name" label="名称" /><el-table-column label="操作" width="120"><el-button size="small" type="primary" link>编辑</el-button></el-table-column></el-table></el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const config = ref({ platformName:'rxt农业产融平台', phone:'400-888-8888', icp:'京ICP备xxxxxx号', defaultRole:'farmer', approvalMode:'single' })
const templates = ref([{ code:'FINANCE_SUBMIT', name:'融资提交通知' }, { code:'FINANCE_APPROVED', name:'融资审批通过' }, { code:'QA_REPLY', name:'问题回复通知' }, { code:'APPOINTMENT', name:'预约确认通知' }])
function saveConfig() { ElMessage.success('配置已保存') }
</script>
<style scoped>.page-title {font-size:20px;font-weight:600;color:#222;margin:0 0 20px}</style>
