<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfoAPI, updateUserInfoAPI } from '../api/user'
import { useUserStore } from '../stores/user'

const props = defineProps({
  userName: { type: String, required: true }
})

const userStore = useUserStore()
const userInfo = ref(null)
const loading = ref(false)
const editMode = ref(false)
const submitting = ref(false)

const form = ref({
  userName: '', nickName: '', realName: '', phone: '', identityNum: '', address: ''
})

async function loadUserInfo() {
  if (!props.userName) return
  loading.value = true
  try {
    const res = await getUserInfoAPI(props.userName)
    if (res.code === 200) {
      userInfo.value = res.data
      resetForm()
    }
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function resetForm() {
  if (userInfo.value) {
    form.value = {
      userName: userInfo.value.userName || '',
      nickName: userInfo.value.nickName || '',
      realName: userInfo.value.realName || '',
      phone: userInfo.value.phone || '',
      identityNum: userInfo.value.identityNum || '',
      address: userInfo.value.address || ''
    }
  }
}

function startEdit() { editMode.value = true }
function cancelEdit() { editMode.value = false; resetForm() }

async function saveInfo() {
  submitting.value = true
  try {
    const res = await updateUserInfoAPI(form.value)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editMode.value = false
      await loadUserInfo()
      if (userStore.userInfo && form.value.nickName) {
        userStore.userInfo.nickName = form.value.nickName
      }
    }
  } catch {
    // handled in interceptor
  } finally {
    submitting.value = false
  }
}

watch(() => props.userName, () => { loadUserInfo() })
onMounted(() => { if (props.userName) loadUserInfo() })
</script>

<template>
  <el-card v-loading="loading" class="profile-card" shadow="never">
    <template #header>
      <div class="profile-header">
        <span class="profile-title">个人信息</span>
        <div class="profile-actions">
          <el-button v-if="!editMode" size="small" @click="startEdit">编辑</el-button>
          <template v-else>
            <el-button size="small" @click="cancelEdit">取消</el-button>
            <el-button size="small" type="primary" :loading="submitting" @click="saveInfo">保存</el-button>
          </template>
        </div>
      </div>
    </template>

    <!-- 查看模式：用 descriptions 保证字段整齐对齐 -->
    <el-descriptions v-if="userInfo && !editMode" :column="2" border class="profile-desc">
      <el-descriptions-item label="用户名">{{ userInfo.userName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ userInfo.nickName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="真实姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="身份证号">{{ userInfo.identityNum || '-' }}</el-descriptions-item>
      <el-descriptions-item label="角色">
        <el-tag size="small" type="success">{{ userInfo.role || '-' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="地址" :span="2">{{ userInfo.address || '-' }}</el-descriptions-item>
      <el-descriptions-item label="状态" :span="2">
        <el-tag v-if="userInfo.status === 'approved'" size="small" type="success">已通过</el-tag>
        <el-tag v-else-if="userInfo.status === 'pending'" size="small" type="warning">待审批</el-tag>
        <el-tag v-else-if="userInfo.status === 'rejected'" size="small" type="danger">已拒绝</el-tag>
        <span v-else>{{ userInfo.status || '-' }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <!-- 编辑模式 -->
    <el-form v-else-if="userInfo && editMode" label-width="90px" class="profile-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户名"><el-input v-model="form.userName" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="昵称"><el-input v-model="form.nickName" /></el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="身份证号"><el-input v-model="form.identityNum" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-empty v-else description="暂无用户信息" :image-size="80" />
  </el-card>
</template>

<style scoped>
.profile-card { margin-bottom: 20px; }
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.profile-title { font-weight: 600; color: var(--green-deep); }
.profile-actions { display: flex; gap: 8px; }

/* descriptions：统一标签列宽 + 基线对齐，杜绝"用户名/昵称对不齐" */
.profile-desc :deep(.el-descriptions__label) {
  width: 100px;
  min-width: 100px;
  font-weight: 600;
}
.profile-desc :deep(.el-descriptions__cell) {
  vertical-align: middle;
}

.profile-form .el-form-item { margin-bottom: 18px; }
</style>
