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
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span style="font-weight:600;color:var(--green-deep)">个人信息</span>
        <div style="display:flex;gap:8px">
          <el-button v-if="!editMode" size="small" @click="startEdit">编辑</el-button>
          <template v-else>
            <el-button size="small" @click="cancelEdit">取消</el-button>
            <el-button size="small" type="primary" :loading="submitting" @click="saveInfo">保存</el-button>
          </template>
        </div>
      </div>
    </template>

    <el-form v-if="userInfo" label-width="90px" class="profile-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户名">
            <el-input v-if="editMode" v-model="form.userName" />
            <span v-else>{{ userInfo.userName || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="昵称">
            <el-input v-if="editMode" v-model="form.nickName" />
            <span v-else>{{ userInfo.nickName || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="真实姓名">
            <el-input v-if="editMode" v-model="form.realName" />
            <span v-else>{{ userInfo.realName || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号">
            <el-input v-if="editMode" v-model="form.phone" />
            <span v-else>{{ userInfo.phone || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="身份证号">
            <el-input v-if="editMode" v-model="form.identityNum" />
            <span v-else>{{ userInfo.identityNum || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地址">
            <el-input v-if="editMode" v-model="form.address" />
            <span v-else>{{ userInfo.address || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色">
            <el-tag size="small" type="success">{{ userInfo.role || '-' }}</el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-tag v-if="userInfo.status === 'approved'" size="small" type="success">已通过</el-tag>
            <el-tag v-else-if="userInfo.status === 'pending'" size="small" type="warning">待审批</el-tag>
            <el-tag v-else-if="userInfo.status === 'rejected'" size="small" type="danger">已拒绝</el-tag>
            <span v-else>{{ userInfo.status || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-empty v-else description="暂无用户信息" :image-size="80" />
  </el-card>
</template>

<style scoped>
.profile-card { margin-bottom: 20px; }
.profile-form .el-form-item { margin-bottom: 18px; }
.profile-form span { display: inline-block; padding: 6px 0; color: var(--text-secondary); font-size: 14px; }
</style>
