<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 角色标识（来自路由 meta）
const role = computed(() => route.meta.role)
const roleTitle = computed(() => route.meta.title)

// 角色菜单映射表（抽离为常量，避免重复创建）
const ROLE_MENU_MAP = {
  farmer: [
    { path: '/farmer/dashboard', icon: 'Odometer', label: '工作台' },
    { path: '/farmer/financing', icon: 'Coin', label: '融资申请' },
    { path: '/farmer/products', icon: 'Goods', label: '我的商品' },
    { path: '/farmer/knowledge', icon: 'Notebook', label: '农业知识' },
    { path: '/farmer/expert-qa', icon: 'ChatDotSquare', label: '专家问答' },
    { path: '/farmer/appointments', icon: 'Calendar', label: '专家预约' },
    { path: '/farmer/messages', icon: 'Bell', label: '消息通知' }
  ],
  buyer: [
    { path: '/buyer/dashboard', icon: 'Odometer', label: '工作台' },
    { path: '/buyer/products', icon: 'Goods', label: '商品采购' },
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

// 有效角色列表，用于校验
const VALID_ROLES = ['farmer', 'buyer', 'bank', 'expert', 'admin']

// 当前角色对应的菜单项
const menuItems = computed(() => {
  const roleName = role.value
  if (!roleName || !VALID_ROLES.includes(roleName)) {
    // 如果角色无效，可以返回空数组，同时可考虑跳转404
    // 这里保留空数组，由模板决定显示什么
    return []
  }
  return ROLE_MENU_MAP[roleName] || []
})

// 判断菜单项是否高亮（支持子路由前缀匹配）
function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

// 导航到指定路径
function navigate(path) {
  router.push(path)
}

// 回到首页
function goHome() {
  router.push('/')
}

// 退出登录
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
    // 用户取消，不做任何操作
  }
}
</script>

<template>
  <el-container style="min-height: 100vh">
    <!-- 侧边栏 -->
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
        <!-- 如果没有菜单项，可展示提示 -->
        <div v-if="!menuItems.length" class="no-menu-tip">
          暂无菜单权限
        </div>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="role-header">
        <span class="current-title">{{ route.meta.title }}</span>
        <div class="header-actions">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.nickName || userStore.userInfo?.roleName || '游客' }}
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
.no-menu-tip {
  color: rgba(255,255,255,0.3);
  text-align: center;
  padding: 20px 0;
  font-size: 14px;
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