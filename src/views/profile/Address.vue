<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const addresses = ref([])
const dialogVisible = ref(false)
const form = ref({ name: '', phone: '', address: '', isDefault: false })

function addAddress() {
  if (!form.value.name || !form.value.phone || !form.value.address) return ElMessage.warning('请填写完整信息')
  addresses.value.push({ ...form.value, id: Date.now() })
  form.value = { name: '', phone: '', address: '', isDefault: false }
  dialogVisible.value = false
  ElMessage.success('添加成功')
}

function removeAddress(id) {
  addresses.value = addresses.value.filter(a => a.id !== id)
  ElMessage.success('删除成功')
}
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">收货地址管理</h2>
      <el-button type="primary" @click="dialogVisible = true">添加地址</el-button>
    </div>

    <div v-if="addresses.length === 0" style="text-align:center;padding:40px 0">
      <el-empty description="暂无收货地址" />
    </div>

    <el-card v-for="addr in addresses" :key="addr.id" shadow="never" style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <strong>{{ addr.name }}</strong>　{{ addr.phone }}
          <el-tag v-if="addr.isDefault" size="small" type="success" style="margin-left:8px">默认</el-tag>
          <p style="color:#666;margin-top:4px">{{ addr.address }}</p>
        </div>
        <el-button text type="danger" @click="removeAddress(addr.id)">删除</el-button>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="添加收货地址" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.address" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.isDefault" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
