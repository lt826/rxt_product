<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const showPassword = ref(false)
const formRef = ref(null)

const form = reactive({
  username: route.query.username || '',
  password: '',
  role: 'farmer'
})

const roles = [
  { value: 'farmer', label: '农户', icon: '👨‍🌾' },
  { value: 'buyer', label: '买家', icon: '📦' },
  { value: 'bank', label: '银行工作人员', icon: '🏦' },
  { value: 'expert', label: '农业专家', icon: '👨‍🔬' },
  { value: 'admin', label: '平台管理员', icon: '⚙️' }
]

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await userStore.login(form)
    router.push(`/${data.role}/dashboard`)
  } catch (e) {
    // error already handled in store
  } finally {
    loading.value = false
  }
}

function goToRegister() {
  router.push('/register')
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">🌾 rxt</div>
        <h2>欢迎回来</h2>
        <p>请选择角色并输入账号密码登录</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
        <!-- 角色选择 -->
        <div class="role-selector">
          <div
            v-for="r in roles"
            :key="r.value"
            class="role-option"
            :class="{ active: form.role === r.value }"
            @click="form.role = r.value"
          >
            <span class="role-icon">{{ r.icon }}</span>
            <span class="role-label">{{ r.label }}</span>
          </div>
        </div>

        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入账号"
            size="large"
            :prefix-icon="'User'"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="'Lock'"
            show-password
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          style="width:100%; margin-top: 4px;"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <el-button text type="primary" @click="goToRegister">立即注册</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 420px;
  max-width: 90%;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.login-header {
  text-align: center;
  margin-bottom: 28px;
}
.logo {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #764ba2;
}
.login-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #303133;
}
.login-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 角色选择器 - 网格布局 */
.role-selector {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  margin-bottom: 20px;
}
.role-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 4px;
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}
.role-option:hover {
  border-color: #764ba2;
  background: #f3eeff;
}
.role-option.active {
  border-color: #764ba2;
  background: #f3eeff;
  box-shadow: 0 0 0 1px #764ba2;
}
.role-icon {
  font-size: 20px;
  line-height: 1.3;
}
.role-label {
  font-size: 11px;
  color: #606266;
  margin-top: 4px;
  white-space: nowrap;
}
.role-option.active .role-label {
  color: #764ba2;
  font-weight: 600;
}

.login-form .el-form-item {
  margin-bottom: 16px;
}
.login-footer {
  text-align: center;
  margin-top: 18px;
  color: #909399;
  font-size: 14px;
}
</style>
