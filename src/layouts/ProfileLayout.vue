<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const role = computed(() => userStore.role)

const roleLabelMap = {
  farmer: '农户', buyer: '买家', bank: '银行工作人员', expert: '技术专家', admin: '平台管理员'
}

const profileMenus = {
  common: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/address', label: '收货地址', icon: 'Location' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' }
  ],
  farmer: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/address', label: '收货地址', icon: 'Location' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '我的发布' },
    { path: '/profile/my-products', label: '商品货源管理', icon: 'Goods' },
    { path: '/profile/my-demands', label: '求购需求管理', icon: 'ShoppingCart' },
    { divider: true, label: '我的订单' },
    { path: '/profile/orders/sold', label: '我卖出的', icon: 'Sell' },
    { path: '/profile/orders/bought', label: '我买到的', icon: 'TakeawayBox' },
    { divider: true, label: '融资' },
    { path: '/profile/my-financing', label: '融资管理', icon: 'Coin' }
  ],
  buyer: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/address', label: '收货地址', icon: 'Location' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '我的订单' },
    { path: '/profile/orders/bought', label: '我买到的', icon: 'TakeawayBox' }
  ],
  expert: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '专家管理' },
    { path: '/profile/my-qa', label: '问答管理', icon: 'ChatDotSquare' },
    { path: '/profile/my-appointments', label: '预约管理', icon: 'Calendar' },
    { path: '/profile/my-knowledge', label: '知识管理', icon: 'Reading' },
    { path: '/profile/expert-info', label: '专家信息', icon: 'InfoFilled' }
  ],
  bank: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '融资管理' },
    { path: '/profile/bank/match', label: '智能匹配农户', icon: 'Search' },
    { path: '/profile/bank/approval', label: '贷款审批', icon: 'DocumentChecked' }
  ],
  admin: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '平台管理' },
    { path: '/profile/all-users', label: '所有用户', icon: 'UserFilled' },
    { path: '/profile/all-goods', label: '所有商品', icon: 'GoodsFilled' }
  ]
}

const menuItems = computed(() => profileMenus[role.value] || profileMenus.common)

function isActive(path) {
  return route.path === path
}

function navigate(path) {
  router.push(path)
}

function goToHome() {
  router.push('/')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    userStore.logout()
    router.push('/')
  } catch { }
}
</script>

<template>
  <div class="profile-layout">
    <!-- Top bar -->
    <header class="profile-header">
      <div class="header-left">
        <div class="logo-mark" @click="goToHome" style="cursor:pointer">
          <span class="logo-leaf">🌾</span>
          <span class="logo-word">融销通</span>
        </div>
        <el-divider direction="vertical" />
        <span class="header-title">个人中心</span>
      </div>
      <div class="header-right">
        <span class="role-badge">{{ roleLabelMap[role] || '用户' }}</span>
        <div class="user-pill" @click="router.push('/profile/center')" style="cursor:pointer">
          <span class="user-avatar-circle">{{ (userStore.nickName || userStore.userInfo?.userName || 'U')[0] }}</span>
          <span class="user-name">{{ userStore.nickName || userStore.userInfo?.realName || '用户' }}</span>
        </div>
        <el-button text size="small" @click="handleLogout" style="color:var(--text-muted)">退出</el-button>
      </div>
    </header>

    <div class="profile-body">
      <!-- Sidebar -->
      <aside class="profile-sidebar">
        <nav class="sidebar-nav">
          <div
            v-for="(item, idx) in menuItems"
            :key="idx"
            :class="['nav-item', { active: item.path && isActive(item.path), divider: item.divider }]"
            @click="item.path && navigate(item.path)"
          >
            <el-icon v-if="item.icon && !item.divider"><component :is="item.icon" /></el-icon>
            <span v-if="!item.divider">{{ item.label }}</span>
            <span v-else class="divider-label">{{ item.label }}</span>
          </div>
        </nav>
      </aside>

      <!-- Content -->
      <section class="profile-content">
        <router-view />
      </section>
    </div>
  </div>
</template>

<style scoped>
.profile-layout {
  min-height: 100vh;
  background: var(--bg);
  display: flex;
  flex-direction: column;
}

/* Header */
.profile-header {
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  padding: 0 32px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(27,67,50,0.04);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.logo-mark {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.2s;
}
.logo-mark:hover { opacity: 0.7; }
.logo-leaf { font-size: 20px; }
.logo-word {
  font-size: 17px;
  font-weight: 800;
  color: var(--green-deep);
  letter-spacing: 0.06em;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.role-badge {
  background: var(--green-mist);
  color: var(--green);
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.user-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}
.user-pill:hover { background: var(--bg); }
.user-avatar-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--green), var(--green-light));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}
.user-name {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Body */
.profile-body {
  display: flex;
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  gap: 24px;
}

/* Sidebar */
.profile-sidebar {
  width: 240px;
  flex-shrink: 0;
}
.sidebar-nav {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  padding: 8px 0;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 20px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
  border-left: 3px solid transparent;
}
.nav-item:hover {
  background: var(--green-mist);
  color: var(--green);
}
.nav-item.active {
  background: var(--green-mist);
  color: var(--green);
  font-weight: 600;
  border-left-color: var(--green);
}
.nav-item .el-icon {
  font-size: 16px;
  opacity: 0.8;
}
.nav-item.active .el-icon {
  opacity: 1;
}
.nav-item.divider {
  font-size: 11px;
  color: var(--text-muted);
  padding: 14px 20px 6px;
  cursor: default;
  pointer-events: none;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

/* Content */
.profile-content {
  flex: 1;
  background: var(--surface);
  border-radius: var(--radius-md);
  padding: 32px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  min-height: 500px;
}

@media (max-width: 900px) {
  .profile-body {
    flex-direction: column;
    padding: 16px;
  }
  .profile-sidebar {
    width: 100%;
  }
  .sidebar-nav {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 8px;
    position: static;
  }
  .nav-item {
    flex: 0 0 auto;
    padding: 8px 14px;
    font-size: 13px;
    border-left: none;
    border-radius: var(--radius-sm);
  }
  .nav-item.divider {
    display: none;
  }
  .profile-content {
    padding: 20px;
  }
}
</style>
