<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  userName: '',
  password: '',
  confirmPassword: '',
  nickName: '',
  realName: '',
  phone: '',
  identityNum: '',
  address: '',
  role: 'farmer'
})

const roles = [
  { value: 'farmer', label: '农户' },
  { value: 'buyer', label: '买家' },
  { value: 'bank', label: '银行工作人员' },
  { value: 'expert', label: '农业专家' },
  { value: 'admin', label: '平台管理员' }
]

const rules = {
  userName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      userName: form.userName,
      password: form.password,
      nickName: form.nickName,
      realName: form.realName,
      phone: form.phone,
      identityNum: form.identityNum,
      address: form.address,
      role: form.role
    })
    ElMessage.success('注册成功，请登录')
    router.push({ path: '/login', query: { username: form.userName } })
  } catch (e) {
    // error already handled in store
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <div class="logo">🌾 rxt</div>
        <h2>创建账号</h2>
        <p>注册后即可使用各模块功能</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
        <div class="form-section-title">账号信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="至少6位密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">基本信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号">
          <el-input v-model="form.identityNum" placeholder="选填" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="选填" />
        </el-form-item>

        <div class="form-section-title">角色选择</div>
        <el-form-item label="选择角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option v-for="r in roles" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>

        <el-button type="primary" style="width:100%; margin-top: 8px;" :loading="loading" @click="handleRegister">
          注 册
        </el-button>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <el-button text type="primary" @click="router.push('/login')">立即登录</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 40px 16px;
}
.register-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 560px;
  max-width: 100%;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.register-header {
  text-align: center;
  margin-bottom: 32px;
}
.logo {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #764ba2;
}
.register-header h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #303133;
}
.register-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin: 20px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.form-section-title:first-of-type {
  margin-top: 0;
}
.register-form .el-form-item {
  margin-bottom: 18px;
}
.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
}
</style>
