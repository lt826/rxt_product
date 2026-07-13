<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getUserInfoAPI } from '../../api/user'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const role = computed(() => userStore.role)
const loading = ref(false)
// 完整资料（含 realName / phone / address / avatar / credit 等），登录返回的 LoginVO 不含这些字段，需单独拉取
const profile = ref({})

const nickName = computed(() => profile.value.nickName || userStore.nickName || userStore.userInfo?.userName || '用户')
const initial = computed(() => (nickName.value || 'U')[0].toUpperCase())
const roleLabel = computed(() => ({ farmer: '农户', buyer: '买家', bank: '银行工作人员', expert: '技术专家', admin: '平台管理员' }[role.value] || '用户'))

// 信誉分着色
const credit = computed(() => (profile.value.credit ?? 0))
const creditType = computed(() => {
  const c = Number(credit.value)
  if (c >= 80) return 'success'
  if (c >= 60) return 'warning'
  return 'danger'
})

const roleDashboardMap = {
  farmer: '/farmer/dashboard',
  buyer: '/buyer/dashboard',
  bank: '/bank/dashboard',
  expert: '/expert/dashboard',
  admin: '/admin/dashboard'
}

function goBack() {
  const dashboard = roleDashboardMap[role.value] || '/farmer/dashboard'
  router.push(dashboard)
}

async function loadProfile() {
  const userName = userStore.userInfo?.userName
  if (!userName) return
  loading.value = true
  try {
    const res = await getUserInfoAPI(userName)
    if (res && res.code === 200 && res.data) {
      profile.value = res.data
    }
  } catch (e) { /* 拦截器已提示 */ }
  finally { loading.value = false }
}

onMounted(loadProfile)

const roleMenuLinks = {
  farmer: [
    { label: '商品货源管理', path: '/profile/my-products', icon: '📦' },
    { label: '我的订单', path: '/profile/orders/sold', icon: '📄' },
    { label: '我的融资管理', path: '/profile/my-financing', icon: '💰' }
  ],
  buyer: [
    { label: '我的订单', path: '/profile/orders/bought', icon: '📄' },
    { label: '收货地址管理', path: '/profile/address', icon: '📍' }
  ],
  expert: [
    { label: '问答管理', path: '/profile/my-qa', icon: '💬' },
    { label: '预约管理', path: '/profile/my-appointments', icon: '📅' },
    { label: '知识管理', path: '/profile/my-knowledge', icon: '📚' }
  ],
  bank: [
    { label: '贷款审批', path: '/profile/bank/approval', icon: '✅' }
  ],
  admin: [
    { label: '所有用户管理', path: '/profile/all-users', icon: '👥' },
    { label: '所有商品管理', path: '/profile/all-goods', icon: '📦' }
  ]
}

const menuLinks = computed(() => roleMenuLinks[role.value] || [])

function def(v) { return (v === null || v === undefined || v === '') ? '-' : v }
</script>

<template>
  <div v-loading="loading">
    <div class="page-header">
      <el-button text type="primary" @click="goBack" style="color:var(--green)">
        <el-icon><ArrowLeft /></el-icon>
        返回首页
      </el-button>
      <h2 class="page-title">个人中心</h2>
    </div>

    <!-- 欢迎卡：头像 + 昵称 + 角色 + 信誉分 -->
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <el-avatar :size="72" :src="profile.avatar" class="welcome-avatar">
          <span class="avatar-fallback">{{ initial }}</span>
        </el-avatar>
        <div class="welcome-meta">
          <h3>欢迎回来，{{ nickName }}</h3>
          <p class="muted">角色：{{ roleLabel }}</p>
        </div>
        <div class="welcome-credit">
          <span class="credit-label">信誉分</span>
          <el-tag :type="creditType" size="large" effect="dark" round>{{ credit }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 完整资料 -->
    <el-card shadow="never" style="margin-top:24px">
      <template #header><strong>基础信息</strong></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ def(profile.userName) }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ def(profile.nickName) }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ def(profile.realName) }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ def(profile.phone) }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ def(profile.address) }}</el-descriptions-item>
        <el-descriptions-item label="头像">
          <el-avatar :size="40" :src="profile.avatar">
            <span class="avatar-fallback">{{ initial }}</span>
          </el-avatar>
        </el-descriptions-item>
        <el-descriptions-item label="信誉分">
          <el-tag :type="creditType" effect="light">{{ credit }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:16px">
        <el-button size="small" @click="router.push('/profile/basic')">编辑资料</el-button>
        <el-button size="small" @click="router.push('/profile/password')">修改密码</el-button>
      </div>
    </el-card>

    <h3 style="margin:24px 0 12px">快捷入口</h3>
    <div class="quick-grid">
      <div v-for="link in menuLinks" :key="link.path" class="quick-card" @click="router.push(link.path)">
        <span class="quick-icon">{{ link.icon }}</span>
        <span class="quick-label">{{ link.label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.page-title { font-size: 20px; margin: 0; }
.welcome-card { margin-bottom: 8px; }
.welcome-content { display: flex; align-items: center; gap: 20px; }
.welcome-avatar { flex-shrink: 0; background: linear-gradient(135deg, var(--green), var(--green-light)); }
.avatar-fallback { color: #fff; font-size: 24px; font-weight: 700; }
.welcome-meta { flex: 1; }
.welcome-meta h3 { margin: 0; }
.muted { color: #666; margin-top: 4px; }
.welcome-credit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.credit-label { font-size: 12px; color: var(--text-muted); }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
@media (max-width: 600px) {
  .quick-grid { grid-template-columns: repeat(2, 1fr); }
  .welcome-credit { display: none; }
}
.quick-card { background: #f9fafb; border: 1px solid #eee; border-radius: 8px; padding: 20px; text-align: center; cursor: pointer; transition: all 0.2s; }
.quick-card:hover { border-color: #488d37; box-shadow: 0 2px 8px rgba(72,141,55,0.1); }
.quick-icon { display: block; font-size: 32px; margin-bottom: 8px; }
.quick-label { font-size: 14px; color: #555; }
</style>
