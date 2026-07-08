<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const role = computed(() => route.meta.role)
const roleTitle = computed(() => route.meta.title)

const menuItems = computed(() => {
  const items = {
    farmer: [
      { path: '/farmer/dashboard', icon: 'Odometer', label: '工作台' },
      { path: '/farmer/financing', icon: 'Coin', label: '融资申请' },
      { path: '/farmer/products', icon: 'Goods', label: '商品浏览' },
      { path: '/farmer/knowledge', icon: 'Notebook', label: '农业知识' },
      { path: '/farmer/expert-qa', icon: 'ChatDotSquare', label: '专家问答' },
      { path: '/farmer/appointments', icon: 'Calendar', label: '专家预约' },
      { path: '/farmer/messages', icon: 'Bell', label: '消息通知' }
    ],
    buyer: [
      { path: '/buyer/dashboard', icon: 'Odometer', label: '工作台' },
      { path: '/buyer/products', icon: 'Goods', label: '商品管理' },
      { path: '/buyer/demand', icon: 'ShoppingCart', label: '收购需求' },
      { path: '/buyer/contacts', icon: 'User', label: '联系农户' }
    ],
    bank: [
      { path: '/bank/dashboard', icon: 'Odometer', label: '工作台' },
      { path: '/bank/approvals', icon: 'DocumentChecked', label: '融资审批' },
      { path: '/bank/products', icon: 'Coin', label: '融资产品配置' },
      { path: '/bank/match-rules', icon: 'SetUp', label: '智能匹配规则' }
    ],
    expert: [
      { path: '/expert/dashboard', icon: 'Odometer', label: '工作台' },
      { path: '/expert/knowledge', icon: 'Notebook', label: '知识管理' },
      { path: '/expert/qa', icon: 'ChatDotSquare', label: '问答处理' },
      { path: '/expert/appointments', icon: 'Calendar', label: '预约处理' }
    ],
    admin: [
      { path: '/admin/dashboard', icon: 'Odometer', label: '工作台' },
      { path: '/admin/users', icon: 'UserFilled', label: '用户管理' },
      { path: '/admin/config', icon: 'Setting', label: '系统配置' },
      { path: '/admin/stats', icon: 'DataAnalysis', label: '数据统计' }
    ]
  }
  return items[role.value] || []
})

function isActive(path) {
  return route.path === path
}

function navigate(path) {
  router.push(path)
}

function goHome() {
  router.push('/')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/')
  } catch {
    // cancelled
  }
}
</script>

<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" class="role-sidebar">
      <div class="sidebar-header" @click="goHome">
        <div class="logo">🌾</div>
        <span class="title">rxt</span>
      </div>
      <div class="role-badge">{{ roleTitle }}</div>
      <el-menu
        :default-active="route.path"
        class="sidebar-menu"
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
          @click="navigate(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="role-header">
        <span class="current-title">{{ route.meta.title }}</span>
        <div class="header-actions">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.nickName || userStore.userInfo?.roleName || '' }}
          </span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="role-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.role-sidebar {
  background: #001529;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.sidebar-header .logo {
  font-size: 24px;
}
.sidebar-header .title {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}
.role-badge {
  padding: 8px 20px;
  color: rgba(255,255,255,0.45);
  font-size: 12px;
  letter-spacing: 2px;
  text-transform: uppercase;
}
.sidebar-menu {
  border-right: none;
  flex: 1;
}
.role-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
}
.current-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}
.role-main {
  background: #f5f7fa;
  padding: 24px;
}
</style>
