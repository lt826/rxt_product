<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const role = computed(() => userStore.role)

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
    { path: '/profile/address', label: '发货地址', icon: 'Location' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '我的发布' },
    { path: '/profile/my-products', label: '商品货源管理', icon: 'Goods' },
    { divider: true, label: '融资' },
    { path: '/profile/my-financing', label: '融资管理', icon: 'Coin' }
  ],
  buyer: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/address', label: '收货地址', icon: 'Location' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' }
  ],
  expert: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '专家管理' },
    { path: '/profile/my-qa', label: '问答管理', icon: 'ChatDotSquare' },
    { path: '/profile/my-appointments', label: '预约管理', icon: 'Calendar' },
    { path: '/profile/my-knowledge', label: '知识管理', icon: 'Reading' }
  ],
  bank: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' },
    { divider: true, label: '融资管理' },
    { path: '/profile/bank/approval', label: '贷款审批', icon: 'DocumentChecked' }
  ],
  admin: [
    { path: '/profile/center', label: '个人中心', icon: 'User' },
    { path: '/profile/basic', label: '基本信息', icon: 'Edit' },
    { path: '/profile/password', label: '密码管理', icon: 'Lock' }
  ]
}

const menuItems = computed(() => profileMenus[role.value] || profileMenus.common)

function isActive(path) {
  return route.path === path
}

function navigate(path) {
  router.push(path)
}
</script>

<template>
  <!-- 仅保留侧栏 + 内容区；顶部导航栏（含个人中心入口与退出按钮）由外层 RoleLayout 提供 -->
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
</template>

<style scoped>
/* Body —— 嵌在 RoleLayout 的 app-main 内，无需再自带外边距/最大宽度 */
.profile-body {
  display: flex;
  flex: 1;
  gap: 24px;
  align-items: flex-start;
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
