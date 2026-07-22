<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfoAPI, updateUserInfoAPI } from '../api/user'
import { uploadImageAPI } from '../api/upload'
import { maskPhone } from '../utils/phone'
import { useUserStore } from '../stores/user'

const props = defineProps({
  userName: { type: String, required: true }
})

const userStore = useUserStore()
const userInfo = ref(null)
const loading = ref(false)
const editMode = ref(false)
const submitting = ref(false)

// 手机号脱敏：真实号码保留在 form.phone（用于保存），页面只显示 138****5678；
// 点击「修改」才出现输入框，避免真实号码显示在页面上。
const phoneEditing = ref(false)
const phoneInput = ref('')

// 头像上传
const uploadingAvatar = ref(false)
const avatarInitial = computed(() => (form.value.nickName || form.value.userName || 'U').trim().charAt(0).toUpperCase())

async function uploadAvatar(options) {
  const file = options.file
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('只支持上传 JPG、PNG、GIF、WEBP 格式的图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  uploadingAvatar.value = true
  try {
    const res = await uploadImageAPI(file)
    if (res.code === 200 && res.data) {
      form.value.avatar = res.data
      ElMessage.success('头像上传成功，点击「保存」生效')
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch {
    ElMessage.error('头像上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
  }
}

const form = ref({
  userName: '', nickName: '', realName: '', phone: '', identityNum: '', address: '', avatar: ''
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
      address: userInfo.value.address || '',
      avatar: userInfo.value.avatar || ''
    }
  }
}

function startEdit() { editMode.value = true; phoneEditing.value = false; phoneInput.value = '' }
function cancelEdit() { editMode.value = false; resetForm(); phoneEditing.value = false; phoneInput.value = '' }

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
}
function cancelEditPhone() {
  phoneEditing.value = false
  phoneInput.value = ''
}

async function saveInfo() {
  submitting.value = true
  try {
    const res = await updateUserInfoAPI(form.value)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editMode.value = false
      await loadUserInfo()
      // 同步昵称与头像到本地登录态（顶部导航栏展示用）
      if (userStore.userInfo) {
        if (form.value.nickName) userStore.userInfo.nickName = form.value.nickName
        if (form.value.avatar !== undefined) userStore.userInfo.avatar = form.value.avatar
        const stored = JSON.parse(localStorage.getItem('rxt_user') || '{}')
        if (form.value.nickName) stored.nickName = form.value.nickName
        if (form.value.avatar !== undefined) stored.avatar = form.value.avatar
        localStorage.setItem('rxt_user', JSON.stringify(stored))
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
      <el-descriptions-item label="头像" :span="2">
        <el-avatar :size="64" :src="userInfo.avatar" class="profile-view-avatar">
          <span class="profile-view-fallback">{{ (userInfo.nickName || userInfo.userName || 'U')[0].toUpperCase() }}</span>
        </el-avatar>
      </el-descriptions-item>
      <el-descriptions-item label="用户名">{{ userInfo.userName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ userInfo.nickName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="真实姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ userInfo.phone ? maskPhone(userInfo.phone) : '-' }}</el-descriptions-item>
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
      <el-form-item label="头像">
        <div class="avatar-upload">
          <el-upload
            :show-file-list="false"
            :http-request="uploadAvatar"
            accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
            class="avatar-uploader"
          >
            <div class="avatar-box" v-loading="uploadingAvatar">
              <el-avatar :size="76" :src="form.avatar" class="avatar-img">
                <span class="avatar-fallback">{{ avatarInitial }}</span>
              </el-avatar>
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
                <span>{{ form.avatar ? '更换' : '上传' }}</span>
              </div>
            </div>
          </el-upload>
          <div class="avatar-meta">
            <div class="avatar-tip">点击头像上传，建议正方形图片</div>
            <el-button v-if="form.avatar" text type="danger" size="small" @click="form.avatar = ''">移除头像</el-button>
          </div>
        </div>
      </el-form-item>
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
          <el-form-item label="手机号">
            <div class="phone-field">
              <template v-if="!phoneEditing">
                <span class="phone-masked">{{ form.phone ? maskPhone(form.phone) : '未填写' }}</span>
                <el-button text type="primary" size="small" @click="startEditPhone">修改</el-button>
              </template>
              <template v-else>
                <el-input v-model="phoneInput" placeholder="请输入新的手机号" maxlength="11" class="phone-input" />
                <el-button type="primary" size="small" @click="confirmEditPhone">确定</el-button>
                <el-button size="small" @click="cancelEditPhone">取消</el-button>
              </template>
            </div>
          </el-form-item>
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
.phone-input { width: 200px; }

/* 查看模式头像 */
.profile-view-avatar {
  background: linear-gradient(135deg, var(--green), var(--green-light));
}
.profile-view-fallback { color: #fff; font-size: 26px; font-weight: 700; }

/* 头像上传 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}
.avatar-uploader { flex-shrink: 0; }
.avatar-uploader :deep(.el-upload) { display: block; }
.avatar-box {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--border);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.avatar-box:hover {
  border-color: var(--green-light);
  box-shadow: 0 0 0 4px rgba(82, 183, 136, 0.15);
}
.avatar-img {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--green), var(--green-light));
}
.avatar-fallback { color: #fff; font-size: 28px; font-weight: 700; }
.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-box:hover .avatar-mask { opacity: 1; }
.avatar-mask .el-icon { font-size: 18px; }
.avatar-meta { display: flex; flex-direction: column; gap: 4px; }
.avatar-tip { font-size: 12px; color: var(--text-muted); }
</style>
