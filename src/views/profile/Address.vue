<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAddressAPI, addAddressAPI, updateAddressAPI, deleteAddressAPI } from '../../api/address'
import { pcaTextArr, joinArea, parseArea } from '../../utils/chinaArea'

const userStore = useUserStore()

// 农户=发货地址，买家=收货地址，其它角色默认收货地址
const isFarmer = computed(() => userStore.role === 'farmer')
const pageTitle = computed(() => isFarmer.value ? '发货地址管理' : '收货地址管理')
const personLabel = computed(() => isFarmer.value ? '发货人' : '收货人')
const areaLabel = computed(() => isFarmer.value ? '发货地区' : '收货地区')

const addresses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
// 表单：area 为省市县级联数组，street 为详细地址；保存时拼成 addressDetail
const emptyForm = () => ({ consignee: '', phone: '', area: [], street: '', isDefault: false })
const form = ref(emptyForm())

const cascaderProps = { expandTrigger: 'hover' }

const dialogTitle = computed(() => editingId.value ? '编辑地址' : (isFarmer.value ? '添加发货地址' : '添加收货地址'))

async function load() {
  loading.value = true
  try {
    const res = await listAddressAPI()
    if (res && res.code === 200) {
      addresses.value = (res.data || []).map(a => ({ ...a, isDefault: a.isDefault === 1 }))
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(load)

function openAdd() {
  editingId.value = null
  form.value = emptyForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  const { area, street } = parseArea(row.addressDetail)
  form.value = { consignee: row.consignee, phone: row.phone, area, street, isDefault: row.isDefault }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.consignee || !form.value.phone) {
    return ElMessage.warning('请填写完整信息')
  }
  if (!form.value.area || form.value.area.length === 0) {
    return ElMessage.warning('请选择所在地区')
  }
  submitting.value = true
  const payload = {
    consignee: form.value.consignee,
    phone: form.value.phone,
    addressDetail: joinArea(form.value.area, form.value.street),
    isDefault: form.value.isDefault ? 1 : 0
  }
  try {
    const res = editingId.value
      ? await updateAddressAPI({ id: editingId.value, ...payload })
      : await addAddressAPI(payload)
    if (res && res.code === 200) {
      ElMessage.success(res.message || '保存成功')
      dialogVisible.value = false
      load()
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { submitting.value = false }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除该地址吗？`, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  } catch (e) { return }
  try {
    const res = await deleteAddressAPI(row.id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      load()
    }
  } catch (e) { /* 拦截器已提示 */ }
}

async function toggleDefault(row) {
  try {
    const res = await updateAddressAPI({ id: row.id, consignee: row.consignee, phone: row.phone, addressDetail: row.addressDetail, isDefault: row.isDefault ? 0 : 1 })
    if (res && res.code === 200) load()
  } catch (e) { /* 拦截器已提示 */ }
}
</script>

<template>
  <div v-loading="loading">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">{{ pageTitle }}</h2>
      <el-button type="primary" @click="openAdd">{{ isFarmer ? '添加发货地址' : '添加收货地址' }}</el-button>
    </div>

    <el-empty v-if="!loading && addresses.length === 0" :description="`暂无${isFarmer ? '发货' : '收货'}地址`" />

    <el-card v-for="addr in addresses" :key="addr.id" shadow="never" style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <strong>{{ addr.consignee }}</strong>　{{ addr.phone }}
          <el-tag v-if="addr.isDefault" size="small" type="success" style="margin-left:8px">默认</el-tag>
          <p style="color:#666;margin-top:4px">{{ addr.addressDetail }}</p>
        </div>
        <div>
          <el-button v-if="!addr.isDefault" text size="small" type="primary" @click="toggleDefault(addr)">设为默认</el-button>
          <el-button text size="small" type="primary" @click="openEdit(addr)">编辑</el-button>
          <el-button text size="small" type="danger" @click="remove(addr)">删除</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="540px">
      <el-form :model="form" label-width="90px">
        <el-form-item :label="personLabel"><el-input v-model="form.consignee" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item :label="areaLabel">
          <el-cascader
            v-model="form.area"
            :options="pcaTextArr"
            :props="cascaderProps"
            filterable
            placeholder="请选择 省 / 市 / 区县"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.street" type="textarea" :rows="2" placeholder="街道、门牌号等详细信息（选填）" />
        </el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.isDefault" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
