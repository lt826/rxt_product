<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import LogoMark from '../../components/LogoMark.vue'
import RoleGlyph from '../../components/RoleGlyph.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: route.query.username || '',
  password: '',
  role: route.query.role || 'farmer'
})

const roles = [
  { value: 'farmer', label: '农户' },
  { value: 'buyer', label: '买家' },
  { value: 'bank', label: '银行' },
  { value: 'expert', label: '专家' },
  { value: 'admin', label: '管理' }
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
    const targetRole = data.role || form.role
    router.push(`/${targetRole}/dashboard`)
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
    <!-- 背景：纯渐变 + 极淡网格纹理，去掉漂浮粒子动画 -->
    <div class="bg-layer">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
    </div>

    <div class="login-container">
      <!-- Left: branding -->
      <div class="login-branding">
        <div class="brand-icon"><LogoMark :size="56" /></div>
        <h1>融销通</h1>
        <p class="brand-sub">农产品融销一体平台</p>
        <p class="brand-desc">融通资金 · 链接产销 · 智汇农业</p>
        <div class="brand-stats">
          <div class="stat-item">
            <div class="stat-num">融资</div>
            <div class="stat-label">银行信贷对接</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">产销</div>
            <div class="stat-label">农产品直达买家</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">知识</div>
            <div class="stat-label">专家在线指导</div>
          </div>
        </div>
      </div>

      <!-- Right: form -->
      <div class="login-form-panel">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账号继续使用</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
          <!-- Role selector -->
          <div class="role-selector">
            <div
              v-for="r in roles"
              :key="r.value"
              class="role-pill"
              :class="{ active: form.role === r.value }"
              @click="form.role = r.value"
            >
              <span class="pill-icon"><RoleGlyph :role="r.value" :size="22" /></span>
              <span class="pill-label">{{ r.label }}</span>
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
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="'Lock'"
              show-password
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <el-button text type="primary" @click="goToRegister" class="link-btn">立即注册</el-button>
        </div>
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
  position: relative;
  overflow: hidden;
  background: var(--green-deep);
  padding: 24px;
}

/* ===== Background ===== */
.bg-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
}
.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 35%, #409160 65%, #1b4332 100%);
}
.bg-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='80' height='80' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M40 0v80M0 40h80' stroke='%23ffffff' stroke-opacity='0.03' stroke-width='1'/%3E%3C/svg%3E");
}

/* ===== Container ===== */
.login-container {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 580px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(0,0,0,0.3);
}

/* ===== Left Branding ===== */
.login-branding {
  flex: 1;
  background: linear-gradient(160deg, rgba(27,67,50,0.95), rgba(45,106,79,0.85));
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  color: #fff;
  text-align: center;
  position: relative;
  overflow: hidden;
}
.login-branding::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 40%, rgba(82,183,136,0.2), transparent 60%);
}
.brand-icon {
  color: #fff;
  margin-bottom: 20px;
  position: relative;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.2));
}
.login-branding h1 {
  font-size: 36px;
  font-weight: 800;
  margin: 0 0 8px;
  letter-spacing: 0.1em;
  position: relative;
}
.brand-sub {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 6px;
  position: relative;
}
.brand-desc {
  font-size: 13px;
  opacity: 0.6;
  letter-spacing: 0.15em;
  margin: 0 0 40px;
  position: relative;
}
.brand-stats {
  display: flex;
  gap: 32px;
  position: relative;
}
.stat-item {
  text-align: center;
}
.stat-num {
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0.04em;
}
.stat-label {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 6px;
}

/* ===== Right Form Panel ===== */
.login-form-panel {
  width: 420px;
  background: var(--surface);
  display: flex;
  flex-direction: column;
  padding: 48px 40px;
}
.form-header {
  margin-bottom: 32px;
}
.form-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0 0 6px;
}
.form-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

/* Role selector pills */
.role-selector {
  display: flex;
  gap: 6px;
  margin-bottom: 24px;
  background: var(--bg);
  padding: 4px;
  border-radius: var(--radius-md);
}
.role-pill {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 4px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}
.role-pill:hover {
  background: var(--green-mist);
}
.role-pill.active {
  background: var(--green-mist);
  border-color: var(--green);
  box-shadow: 0 2px 8px rgba(45,106,79,0.15);
}
.pill-icon {
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
.role-pill.active .pill-icon {
  color: var(--green);
}
.pill-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
  font-weight: 500;
}
.role-pill.active .pill-label {
  color: var(--green);
  font-weight: 700;
}

/* Form */
.login-form .el-form-item {
  margin-bottom: 18px;
}
.login-form .el-input__wrapper {
  border-radius: var(--radius-sm);
  padding: 4px 12px;
}
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.15em;
  margin-top: 8px;
  border-radius: var(--radius-sm);
}

/* Footer */
.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: auto;
  padding-top: 24px;
  font-size: 14px;
  color: var(--text-muted);
}
.link-btn {
  font-weight: 600;
}

/* Responsive */
@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
    max-width: 460px;
  }
  .login-branding {
    padding: 32px 24px;
    min-height: 200px;
  }
  .brand-stats {
    gap: 24px;
  }
  .stat-num {
    font-size: 20px;
  }
  .login-form-panel {
    width: 100%;
    padding: 32px 24px;
  }
}
@media (max-width: 500px) {
  .login-container {
    border-radius: var(--radius-lg);
    min-height: auto;
  }
  .login-branding h1 {
    font-size: 28px;
  }
  .login-form-panel {
    padding: 24px 20px;
  }
}
</style>
