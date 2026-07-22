<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { maskPhone } from '../../utils/phone'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const form = ref({
  realName: '', specialty: '', org: '', title: '', phone: '', introduction: ''
})

// 手机号脱敏：真实号码保留在 form.phone（用于保存），页面只显示 138****5678
const phoneEditing = ref(false)
const phoneInput = ref('')
const phoneMasked = computed(() => maskPhone(form.value.phone))

function startEditPhone() {
  phoneInput.value = ''
  phoneEditing.value = true
}
function confirmEditPhone() {
  const v = phoneInput.value.trim()
  if (!v) { phoneEditing.value = false; return }
  if (!/^1\d{10}$/.test(v)) {
    ElMessage.warning('请输入正确的 11 位手机号')
    return
  }
  form.value.phone = v
  phoneEditing.value = false
  ElMessage.success('联系电话已更新，点击下方「保存信息」生效')
}
function cancelEditPhone() {
  phoneEditing.value = false
  phoneInput.value = ''
}

onMounted(() => {
  const info = userStore.userInfo || {}
  form.value.realName = info.realName || ''
  form.value.phone = info.phone || ''
})

function saveInfo() {
  ElMessage.success('专家信息保存成功')
}
</script>

<template>
  <div>
    <h2 class="page-title">专家信息管理</h2>
    <el-card shadow="never" style="max-width:700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="专业领域"><el-input v-model="form.specialty" placeholder="如：水稻、植物学" /></el-form-item>
        <el-form-item label="所属单位"><el-input v-model="form.org" placeholder="如：山东农科院" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="form.title" placeholder="如：高级工程师" /></el-form-item>
        <el-form-item label="联系电话">
          <div class="phone-field">
            <template v-if="!phoneEditing">
              <span class="phone-masked">{{ phoneMasked || '未填写' }}</span>
              <el-button text type="primary" size="small" @click="startEditPhone">修改</el-button>
            </template>
            <template v-else>
              <el-input v-model="phoneInput" placeholder="请输入新的手机号" maxlength="11" class="phone-input" />
              <el-button type="primary" size="small" @click="confirmEditPhone">确定</el-button>
              <el-button size="small" @click="cancelEditPhone">取消</el-button>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="个人简介"><el-input v-model="form.introduction" type="textarea" :rows="4" /></el-form-item>
        <el-form-item><el-button type="primary" @click="saveInfo">保存信息</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }

.phone-field {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.phone-masked {
  font-size: 14px;
  color: var(--text);
  letter-spacing: 0.04em;
}
.phone-input { width: 220px; }
</style>
