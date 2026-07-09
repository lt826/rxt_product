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

const ROLE_MENU_MAP = {
  farmer: [
    { path: '/farmer/dashboard', label: '工作台' },
    { path: '/farmer/products', label: '商品货源' },
    { path: '/farmer/financing', label: '融资申请' },
    { path: '/farmer/expert-qa', label: '专家问答' },
    { path: '/farmer/appointments', label: '专家预约' },
    { path: '/farmer/knowledge', label: '农业知识' },
    { path: '/farmer/messages', label: '消息通知' }
  ],
  buyer: [
    { path: '/buyer/dashboard', label: '工作台' },
    { path: '/buyer/products', label: '商品采购' },
    { path: '/buyer/demand', label: '收购需求' },
    { path: '/buyer/contacts', label: '联系农户' }
  ],
  bank: [
    { path: '/bank/dashboard', label: '工作台' },
    { path: '/bank/approvals', label: '融资审批' },
    { path: '/bank/products', label: '产品配置' },
    { path: '/bank/match-rules', label: '匹配规则' }
  ],
  expert: [
    { path: '/expert/dashboard', label: '工作台' },
    { path: '/expert/knowledge', label: '知识管理' },
    { path: '/expert/qa', label: '问答处理' },
    { path: '/expert/appointments', label: '预约处理' }
  ],
  admin: [
    { path: '/admin/dashboard', label: '工作台' },
    { path: '/admin/users', label: '用户管理' },
    { path: '/admin/config', label: '系统配置' },
    { path: '/admin/stats', label: '数据统计' }
  ]
}

const menuItems = computed(() => ROLE_MENU_MAP[role.value] || [])

function isActive(path) {
  return route.path.startsWith(path)
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
  } catch { }
}
</script>

<template>
  <div class="layout-wrap">
    <!-- 顶部白色Logo栏 -->
    <header class="header-wrap">
      <div class="logo-box" @click="goHome" style="cursor:pointer">
        <div class="logo-icon">🌾</div>
        <div class="logo-text">
          <h1>融销通</h1>
          <p>农产品融销一体平台</p>
        </div>
      </div>
      <div class="header-right">
        <span class="role-tag">{{ roleTitle }}</span>
        <span class="user-info">
          <el-icon><User /></el-icon>
          {{ userStore.nickName || userStore.userInfo?.roleName || '用户' }}
        </span>
        <el-button text size="small" @click="handleLogout" style="color:#666">退出</el-button>
      </div>
    </header>

    <!-- 绿色导航条 -->
    <nav class="nav-bar">
      <div
        v-for="item in menuItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        @click="navigate(item.path)"
      >
        {{ item.label }}
      </div>
    </nav>

    <!-- 主体内容 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout-wrap {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 顶部白色Logo栏 */
.header-wrap {
  background: #fff;
  padding: 10px 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo-box {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  width: 60px;
  height: 60px;
  background: #488d37;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
}
.logo-text h1 {
  font-size: 22px;
  color: #222;
  margin: 0;
}
.logo-text p {
  font-size: 14px;
  color: #666;
  margin: 0;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.role-tag {
  background: #eef8e9;
  color: #488d37;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

/* 绿色导航条 */
.nav-bar {
  background: #488d37;
  display: flex;
  padding: 0 80px;
}
.nav-item {
  padding: 14px 24px;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  user-select: none;
}
.nav-item:hover {
  background: #59a044;
}
.nav-item.active {
  border-bottom: 3px solid #fff;
  background: #59a044;
}

/* 主体内容 */
.main-content {
  padding: 24px 80px;
}
</style>
