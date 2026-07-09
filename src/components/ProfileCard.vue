<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfoAPI, updateUserInfoAPI } from '../api/user'
import { useUserStore } from '../stores/user'

const props = defineProps({
  userName: {
    type: String,
    required: true
  }
})

const userStore = useUserStore()
const userInfo = ref(null)
const loading = ref(false)
const editMode = ref(false)
const submitting = ref(false)

const form = ref({
  userName: '',
  nickName: '',
  realName: '',
  phone: '',
  identityNum: '',
  address: ''
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
  } catch (e) {
    // 忽略
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

function startEdit() {
  editMode.value = true
}

function cancelEdit() {
  editMode.value = false
  resetForm()
}

async function saveInfo() {
  submitting.value = true
  try {
    const res = await updateUserInfoAPI(form.value)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editMode.value = false
      await loadUserInfo()
      // 更新 store 中的用户信息
      if (userStore.userInfo && form.value.nickName) {
        userStore.userInfo.nickName = form.value.nickName
      }
    }
  } catch (e) {
    // 已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

// 也可以通过 watch 监听 userName 变化
watch(() => props.userName, () => {
  loadUserInfo()
})

onMounted(() => {
  if (props.userName) {
    loadUserInfo()
  }
})
</script>

<template>
  <el-card v-loading="loading" class="profile-card">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>个人信息</span>
        <el-button v-if="!editMode" size="small" @click="startEdit">编辑</el-button>
        <template v-else>
          <el-button size="small" @click="cancelEdit">取消</el-button>
          <el-button size="small" type="primary" :loading="submitting" @click="saveInfo">保存</el-button>
        </template>
      </div>
    </template>

    <el-form v-if="userInfo" label-width="100px" class="profile-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户名">
            <el-input v-if="editMode" v-model="form.userName" placeholder="请输入用户名" />
            <span v-else>{{ userInfo.userName || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="昵称">
            <el-input v-if="editMode" v-model="form.nickName" placeholder="请输入昵称" />
            <span v-else>{{ userInfo.nickName || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="真实姓名">
            <el-input v-if="editMode" v-model="form.realName" placeholder="请输入真实姓名" />
            <span v-else>{{ userInfo.realName || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号">
            <el-input v-if="editMode" v-model="form.phone" placeholder="请输入手机号" />
            <span v-else>{{ userInfo.phone || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="身份证号">
            <el-input v-if="editMode" v-model="form.identityNum" placeholder="请输入身份证号" />
            <span v-else>{{ userInfo.identityNum || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地址">
            <el-input v-if="editMode" v-model="form.address" placeholder="请输入地址" />
            <span v-else>{{ userInfo.address || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色">
            <span>{{ userInfo.role || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="积分">
            <span>{{ userInfo.integral ?? '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="注册时间">
            <span>{{ userInfo.createTime ? userInfo.createTime.replace('T', ' ').substring(0, 16) : '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-tag v-if="userInfo.status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="userInfo.status === 'pending'" type="warning">待审批</el-tag>
            <el-tag v-else-if="userInfo.status === 'rejected'" type="danger">已拒绝</el-tag>
            <span v-else>{{ userInfo.status || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-empty v-else description="暂无用户信息" />
  </el-card>
</template>

<style scoped>
.profile-card {
  margin-bottom: 20px;
}
.profile-form .el-form-item {
  margin-bottom: 16px;
}
.profile-form span {
  display: inline-block;
  padding: 4px 0;
  color: #606266;
}
</style>
