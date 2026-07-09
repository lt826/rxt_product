<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const role = computed(() => userStore.role)
const nickName = computed(() => userStore.nickName || userStore.userInfo?.realName || userStore.userInfo?.userName || '用户')

const roleMenuLinks = {
  farmer: [
    { label: '商品货源管理', path: '/profile/my-products', icon: '📦' },
    { label: '求购需求管理', path: '/profile/my-demands', icon: '📋' },
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
    { label: '知识管理', path: '/profile/my-knowledge', icon: '📚' },
    { label: '专家信息管理', path: '/profile/expert-info', icon: 'ℹ️' }
  ],
  bank: [
    { label: '智能匹配农户', path: '/profile/bank/match', icon: '🔍' },
    { label: '贷款审批', path: '/profile/bank/approval', icon: '✅' }
  ],
  admin: [
    { label: '所有用户管理', path: '/profile/all-users', icon: '👥' },
    { label: '所有商品管理', path: '/profile/all-goods', icon: '📦' }
  ]
}

const menuLinks = computed(() => roleMenuLinks[role.value] || [])
</script>

<template>
  <div>
    <h2 class="page-title">个人中心</h2>
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-avatar">
          <span style="font-size:48px">{{ ['👤','👨‍🌾','📦','🏦','👨‍🔬','⚙️'][['','farmer','buyer','bank','expert','admin'].indexOf(role)] }}</span>
        </div>
        <div>
          <h3>欢迎回来，{{ nickName }}</h3>
          <p style="color:#666;margin-top:4px">
            角色：{{ {farmer:'农户',buyer:'买家',bank:'银行工作人员',expert:'技术专家',admin:'平台管理员'}[role] || '用户' }}
          </p>
        </div>
      </div>
    </el-card>

    <h3 style="margin:24px 0 12px">快捷入口</h3>
    <div class="quick-grid">
      <div v-for="link in menuLinks" :key="link.path" class="quick-card" @click="router.push(link.path)">
        <span class="quick-icon">{{ link.icon }}</span>
        <span class="quick-label">{{ link.label }}</span>
      </div>
    </div>

    <el-card shadow="never" style="margin-top:24px">
      <template #header><strong>基础信息</strong></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userStore.userInfo?.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userStore.nickName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ userStore.userInfo?.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:16px">
        <el-button size="small" @click="router.push('/profile/basic')">编辑资料</el-button>
        <el-button size="small" @click="router.push('/profile/password')">修改密码</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; margin-bottom: 16px; }
.welcome-card { margin-bottom: 8px; }
.welcome-content { display: flex; align-items: center; gap: 20px; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
@media (max-width: 600px) { .quick-grid { grid-template-columns: repeat(2, 1fr); } }
.quick-card { background: #f9fafb; border: 1px solid #eee; border-radius: 8px; padding: 20px; text-align: center; cursor: pointer; transition: all 0.2s; }
.quick-card:hover { border-color: #488d37; box-shadow: 0 2px 8px rgba(72,141,55,0.1); }
.quick-icon { display: block; font-size: 32px; margin-bottom: 8px; }
.quick-label { font-size: 14px; color: #555; }
</style>
