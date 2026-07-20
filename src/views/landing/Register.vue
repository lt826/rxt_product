<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import LogoMark from '../../components/LogoMark.vue'
import RoleGlyph from '../../components/RoleGlyph.vue'

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
  { value: 'bank', label: '银行人员' },
  { value: 'expert', label: '农业专家' },
  { value: 'admin', label: '管理员' }
]

const needsApproval = computed(() => {
  return ['bank', 'expert', 'admin'].includes(form.role)
})

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

    if (needsApproval.value) {
      ElMessage.success('注册申请已提交，请等待管理员审批')
    } else {
      ElMessage.success('注册成功，请登录')
    }
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
    <div class="bg-layer">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
    </div>

    <div class="register-container">
      <!-- Left: info -->
      <div class="register-info">
        <div class="info-icon"><LogoMark :size="48" /></div>
        <h2>加入融销通</h2>
        <p class="info-desc">开启你的智慧农业之旅</p>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-icon"><el-icon><Coin /></el-icon></span>
            <div>
              <strong>融资服务</strong>
              <span>对接多家银行，快速获取资金支持</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon"><el-icon><Goods /></el-icon></span>
            <div>
              <strong>电商平台</strong>
              <span>一键上架，直达全国买家</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon"><el-icon><Service /></el-icon></span>
            <div>
              <strong>专家咨询</strong>
              <span>农业专家在线解答种植难题</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon"><el-icon><Reading /></el-icon></span>
            <div>
              <strong>知识库</strong>
              <span>海量农业技术文章随时学习</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: form -->
      <div class="register-form-panel">
        <div class="form-header">
          <div class="back-link" @click="router.push('/login')">← 返回登录</div>
          <h2>创建账号</h2>
          <p>填写以下信息完成注册</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
          <!-- Role selection -->
          <div class="form-section">
            <label class="section-label">选择角色</label>
            <div class="role-grid">
              <div
                v-for="r in roles"
                :key="r.value"
                class="role-card"
                :class="{ active: form.role === r.value }"
                @click="form.role = r.value"
              >
                <span class="role-glyph"><RoleGlyph :role="r.value" :size="24" /></span>
                <span class="role-name">{{ r.label }}</span>
              </div>
            </div>
          </div>

          <!-- Account info -->
          <div class="form-section">
            <label class="section-label">账号信息</label>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="账号" prop="userName">
                  <el-input v-model="form.userName" placeholder="登录账号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="昵称" prop="nickName">
                  <el-input v-model="form.nickName" placeholder="显示名称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="密码" prop="password">
                  <el-input v-model="form.password" type="password" placeholder="至少6位" show-password />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入" show-password />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- Basic info -->
          <div class="form-section">
            <label class="section-label">基本信息 <span class="optional">(选填)</span></label>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="真实姓名">
                  <el-input v-model="form.realName" placeholder="用于身份认证" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="用于接收通知" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="身份证号">
              <el-input v-model="form.identityNum" placeholder="融资服务需要" />
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="form.address" placeholder="所在地区" />
            </el-form-item>
          </div>

          <!-- Approval alert -->
          <el-alert v-if="needsApproval" type="warning" :closable="false" show-icon class="approval-alert">
            <template #title>
              <strong>需要审批</strong> — 该角色注册后需等待管理员审批才能登录
            </template>
          </el-alert>

          <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            完 成 注 册
          </el-button>
        </el-form>
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
  position: relative;
  overflow-y: auto;
  padding: 40px 24px;
  background: var(--green-deep);
}

.bg-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
}
.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 50%, #1b4332 100%);
}
.bg-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.register-container {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  max-width: 1100px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(0,0,0,0.3);
  min-height: 640px;
}

/* ===== Left Info Panel ===== */
.register-info {
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
}
.info-icon {
  color: #fff;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.register-info h2 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px;
}
.info-desc {
  font-size: 14px;
  opacity: 0.7;
  margin: 0 0 36px;
}
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  text-align: left;
  width: 100%;
  max-width: 320px;
}
.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.feature-icon {
  color: rgba(255,255,255,0.92);
  font-size: 22px;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  background: rgba(255,255,255,0.1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}
.feature-item strong {
  display: block;
  font-size: 15px;
  margin-bottom: 2px;
}
.feature-item span:last-child {
  font-size: 13px;
  opacity: 0.7;
  line-height: 1.4;
}

/* ===== Right Form Panel ===== */
.register-form-panel {
  width: 520px;
  background: var(--surface);
  padding: 40px 36px;
  overflow-y: auto;
}
.form-header {
  margin-bottom: 28px;
}
.back-link {
  font-size: 13px;
  color: var(--green);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 12px;
  transition: opacity 0.2s;
}
.back-link:hover { opacity: 0.7; }
.form-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0 0 4px;
}
.form-header p {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}

/* Section labels */
.form-section {
  margin-bottom: 20px;
}
.section-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--green);
  margin-bottom: 12px;
}
.optional {
  font-weight: 400;
  color: var(--text-muted);
  font-size: 12px;
}

/* Role grid */
.role-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}
.role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 4px;
  border-radius: var(--radius-sm);
  border: 2px solid var(--border);
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg);
}
.role-card:hover {
  border-color: var(--green-pale);
  background: var(--green-mist);
}
.role-card.active {
  border-color: var(--green);
  background: var(--green-mist);
  box-shadow: 0 2px 8px rgba(45,106,79,0.12);
}
.role-glyph {
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
.role-name {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 6px;
  font-weight: 500;
}
.role-card.active .role-glyph {
  color: var(--green);
}
.role-card.active .role-name {
  color: var(--green);
  font-weight: 700;
}

/* Form */
.register-form .el-form-item {
  margin-bottom: 14px;
}
.register-form .el-input__wrapper {
  border-radius: var(--radius-sm);
}

.approval-alert {
  margin-bottom: 16px;
  border-radius: var(--radius-sm);
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.2em;
  border-radius: var(--radius-sm);
}

/* Responsive */
@media (max-width: 900px) {
  .register-container {
    flex-direction: column;
    max-width: 500px;
  }
  .register-info {
    padding: 32px 24px;
    min-height: 200px;
  }
  .register-form-panel {
    width: 100%;
    padding: 28px 24px;
  }
  .role-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}
@media (max-width: 500px) {
  .register-container {
    border-radius: var(--radius-lg);
  }
  .role-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
