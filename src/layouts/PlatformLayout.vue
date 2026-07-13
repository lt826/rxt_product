<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const nickName = computed(() => userStore.nickName || userStore.userInfo?.realName || '用户')
const role = computed(() => userStore.role)

const navItems = [
  { path: '/trade/home', label: '商品大厅', icon: 'Shop' },
  { path: '/trade/cart', label: '购物车', icon: 'ShoppingCart' },
  { path: '/trade/checkout', label: '订单结算', icon: 'Document' },
  { path: '/profile/center', label: '个人中心', icon: 'User', isProfile: true }
]

function navigate(path) {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  router.push(path)
}

function isItemActive(item) {
  // 个人中心入口在任意 /profile/* 页面下都高亮
  if (item.isProfile) return route.path.startsWith('/profile')
  return route.path.startsWith(item.path)
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

function goBack() {
  const dashboardMap = {
    farmer: '/farmer/dashboard',
    buyer: '/buyer/dashboard',
    bank: '/bank/dashboard',
    expert: '/expert/dashboard',
    admin: '/admin/dashboard'
  }
  const dashboard = dashboardMap[role.value] || '/farmer/dashboard'
  router.push(dashboard)
}
</script>

<template>
  <div class="platform-wrap">
    <!-- Top nav -->
    <header class="platform-header">
      <div class="header-inner">
        <div class="header-left">
          <div class="logo-mark" @click="goBack" style="cursor:pointer">
            <span class="logo-leaf">🌾</span>
            <span class="logo-word">融销通 · 交易</span>
          </div>
        </div>

        <nav class="main-nav">
          <div
            v-for="item in navItems"
            :key="item.path"
            class="nav-item"
            :class="{ active: isItemActive(item) }"
            @click="navigate(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </div>
        </nav>

        <div class="header-right">
          <div class="user-pill" @click="router.push('/profile/center')" style="cursor:pointer">
            <span class="user-avatar-circle">{{ (nickName || 'U')[0] }}</span>
            <span class="user-name">{{ nickName }}</span>
          </div>
          <el-button text size="small" class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出</span>
          </el-button>
        </div>
      </div>
    </header>

    <!-- Main content -->
    <main class="platform-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.platform-wrap {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

/* Header */
.platform-header {
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(27,67,50,0.04);
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  height: 56px;
}
.logo-mark {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.2s;
}
.logo-mark:hover { opacity: 0.7; }
.logo-leaf { font-size: 22px; }
.logo-word {
  font-size: 17px;
  font-weight: 800;
  color: var(--green-deep);
  letter-spacing: 0.04em;
}

/* Nav */
.main-nav {
  display: flex;
  gap: 4px;
  background: var(--bg);
  padding: 4px;
  border-radius: var(--radius-md);
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
  font-weight: 500;
  user-select: none;
}
.nav-item:hover {
  color: var(--green);
  background: var(--green-mist);
}
.nav-item.active {
  color: var(--green);
  background: var(--surface);
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(27,67,50,0.08);
}
.nav-item .el-icon { font-size: 16px; }

/* Right */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
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
.logout-btn {
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}
.logout-btn:hover { color: var(--green); }

/* Main */
.platform-main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 32px;
}

@media (max-width: 900px) {
  .header-inner { padding: 0 16px; }
  .platform-main { padding: 20px 16px; }
  .user-name { display: none; }
}
</style>
