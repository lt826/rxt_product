<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getUserInfoAPI, updateUserInfoAPI } from '../../api/user'
import { uploadImageAPI } from '../../api/upload'
import { maskPhone } from '../../utils/phone'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const form = ref({
  userName: userStore.userInfo?.userName || '',
  nickName: '',
  realName: '',
  phone: '',
  identityNum: '',
  address: '',
  avatar: ''
})

const loading = ref(false)
const saving = ref(false)
const uploadingAvatar = ref(false)

// 头像首字母兜底（无头像时展示）
const avatarInitial = computed(() => (form.value.nickName || form.value.userName || 'U').trim().charAt(0).toUpperCase())

// 头像上传：先上传图片拿到 URL，存入 form.avatar，点击「保存信息」时一并持久化
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
      ElMessage.success('头像上传成功，点击「保存信息」生效')
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch {
    ElMessage.error('头像上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
  }
}

// 手机号脱敏展示：真实号码保留在 form.phone（用于保存），页面只显示脱敏后的 138****5678；
// 点击「修改」进入编辑态输入新号码，避免真实号码直接显示在页面上。
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
  ElMessage.success('手机号已更新，点击下方「保存信息」生效')
}
function cancelEditPhone() {
  phoneEditing.value = false
  phoneInput.value = ''
}

// 拉取数据库中的真实资料回填（登录返回的 LoginVO 不含 realName/phone/address/identityNum）
async function loadInfo() {
  const userName = userStore.userInfo?.userName
  if (!userName) return
  loading.value = true
  try {
    const res = await getUserInfoAPI(userName)
    if (res && res.code === 200 && res.data) {
      const u = res.data
      form.value = {
        userName: u.userName || userName,
        nickName: u.nickName || '',
        realName: u.realName || '',
        phone: u.phone || '',
        identityNum: u.identityNum || '',
        address: u.address || '',
        avatar: u.avatar || ''
      }
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}
onMounted(loadInfo)

async function saveInfo() {
  if (!form.value.userName) return ElMessage.warning('缺少用户名')
  saving.value = true
  try {
    const res = await updateUserInfoAPI({ ...form.value })
    if (res && res.code === 200) {
      // 同步本地登录态中的昵称与头像（顶部导航栏展示用）
      const user = JSON.parse(localStorage.getItem('rxt_user') || '{}')
      if (form.value.nickName) user.nickName = form.value.nickName
      if (form.value.avatar !== undefined) user.avatar = form.value.avatar
      localStorage.setItem('rxt_user', JSON.stringify(user))
      userStore.userInfo = { ...userStore.userInfo, ...user }
      ElMessage.success(res.message || '保存成功')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { saving.value = false }
}
</script>

<template>
  <div v-loading="loading">
    <h2 class="page-title">基本信息管理</h2>
    <el-card shadow="never">
      <el-form :model="form" label-width="100px">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-upload
              :show-file-list="false"
              :http-request="uploadAvatar"
              accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
              class="avatar-uploader"
            >
              <div class="avatar-box" v-loading="uploadingAvatar">
                <el-avatar :size="84" :src="form.avatar" class="avatar-img">
                  <span class="avatar-fallback">{{ avatarInitial }}</span>
                </el-avatar>
                <div class="avatar-mask">
                  <el-icon><Camera /></el-icon>
                  <span>{{ form.avatar ? '更换' : '上传' }}</span>
                </div>
              </div>
            </el-upload>
            <div class="avatar-meta">
              <div class="avatar-tip">点击头像上传图片，建议使用正方形图片</div>
              <div class="avatar-formats">支持 JPG / PNG / GIF / WEBP，不超过 5MB</div>
              <el-button v-if="form.avatar" text type="danger" size="small" @click="form.avatar = ''">移除头像</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.userName" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <div class="phone-field">
            <template v-if="!phoneEditing">
              <span class="phone-masked">{{ phoneMasked || '未填写' }}</span>
              <el-button text type="primary" size="small" @click="startEditPhone">修改</el-button>
            </template>
            <template v-else>
              <el-input
                v-model="phoneInput"
                placeholder="请输入新的 11 位手机号"
                maxlength="11"
                class="phone-input"
              />
              <el-button type="primary" size="small" @click="confirmEditPhone">确定</el-button>
              <el-button size="small" @click="cancelEditPhone">取消</el-button>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.identityNum" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveInfo">保存信息</el-button>
        </el-form-item>
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
  width: 84px;
  height: 84px;
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
.avatar-fallback {
  color: #fff;
  font-size: 30px;
  font-weight: 700;
}
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
.avatar-tip { font-size: 13px; color: var(--text); }
.avatar-formats { font-size: 12px; color: var(--text-muted); }
</style>
