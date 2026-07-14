<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import { Bell, ArrowDown } from '@element-plus/icons-vue'
import { getUnreadNotificationCountAPI, getMyNotificationsAPI, markNotificationReadAPI } from '../api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const headerNavRef = ref(null)
const activeDropdown = ref(null)

// 点导航栏以外关闭下拉
function handleClickOutside(e) {
  if (headerNavRef.value && !headerNavRef.value.contains(e.target)) {
    activeDropdown.value = null
  }
}

// 角色端路由带 meta.role；个人中心 /profile 路由不带，回退到当前登录用户角色
const role = computed(() => route.meta.role || userStore.role)

const ROLE_TITLE = {
  farmer: '农户端', buyer: '买家端', bank: '银行端', expert: '专家端', admin: '平台管理端'
}
const roleTitle = computed(() => ROLE_TITLE[role.value] || '个人中心')

const ROLE_MENU_MAP = {
  farmer: [
    { path: "/farmer/dashboard", label: "首页", icon: "HomeFilled", isRoot: true },
    {
      label: "融资服务", icon: "Wallet", isRoot: true,
      children: [
        { path: "/farmer/financing-intention", label: "融资意向", icon: "DocumentAdd" },
        { path: "/farmer/financing", label: "融资申请", icon: "Coin" },
        { path: "/farmer/partner-invitations", label: "联合贷款申请", icon: "Connection" }
      ]
    },
    {
      label: "专家助力", icon: "Service", isRoot: true,
      children: [
        { path: "/farmer/knowledge", label: "农业知识", icon: "Reading" },
        { path: "/farmer/expert-qa", label: "专家问答", icon: "ChatDotRound" },
        { path: "/farmer/appointments", label: "专家预约", icon: "Calendar" }
      ]
    },
    {
      label: "农产品交易", icon: "ShoppingBasket", isRoot: true,
      children: [
        { path: "/farmer/products", label: "商品货源", icon: "Goods" },
        { path: "/farmer/demands", label: "求购大厅", icon: "ShoppingCart" }
      ]
    },
    { path: "/farmer/messages", label: "消息通知", icon: "Bell" }
  ],
  buyer: [
    { path: '/buyer/dashboard', label: '首页', icon: 'HomeFilled' },
    { path: '/buyer/products', label: '商品采购', icon: 'ShoppingBag' },
    { path: '/buyer/demand', label: '收购需求', icon: 'Document' },
    { path: '/buyer/contacts', label: '联系农户', icon: 'User' }
  ],
  bank: [
    { path: '/bank/dashboard', label: '首页', icon: 'HomeFilled' },
    { path: '/bank/approvals', label: '融资审批', icon: 'DocumentChecked' },
    { path: '/bank/products', label: '产品配置', icon: 'Setting' },
    { path: '/bank/match-rules', label: '匹配规则', icon: 'Connection' }
  ],
  expert: [
    { path: '/expert/dashboard', label: '首页', icon: 'HomeFilled' },
    { path: '/expert/knowledge', label: '知识管理', icon: 'Reading' },
    { path: '/expert/qa', label: '问答处理', icon: 'ChatDotSquare' },
    { path: '/expert/appointments', label: '预约处理', icon: 'Calendar' }
  ],
  admin: [
    { path: '/admin/dashboard', label: '首页', icon: 'HomeFilled' },
    { path: '/admin/users', label: '用户管理', icon: 'UserFilled' },
    { path: '/admin/goods', label: '商品管理', icon: 'Goods' },
    { path: '/admin/config', label: '系统配置', icon: 'Setting' },
    { path: '/admin/stats', label: '数据统计', icon: 'DataAnalysis' }
  ]
}

// 所有角色端统一在导航栏末尾追加「个人中心」入口
const profileTab = { path: '/profile/center', label: '个人信息', icon: 'User', isProfile: true }

// 过滤掉子菜单项，只保留顶级菜单
const topLevelItems = computed(() => (menuItems.value || []).filter(item => !item._hidden))
const menuItems = computed(() => {
  const base = ROLE_MENU_MAP[role.value] || []
  // 为子菜单项标记 _hidden，使其不显示为主 tab，但通过父级下拉访问
  const withHiddenFlag = base.flatMap(item => {
    if (item.children) {
      return [item, ...item.children.map(c => ({ ...c, _hidden: true }))]
    }
    return item
  })
  return [...withHiddenFlag, profileTab]
})

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

function isTabActive(item) {
  // 个人中心 tab 在任意 /profile/* 页面下都高亮
  if (item.isProfile) return route.path.startsWith('/profile')
  // 带子菜单的父项：任意子项激活时父项也高亮
  if (item.children) {
    return item.children.some(c => isActive(c.path)) || isActive(item.path)
  }
  return isActive(item.path)
}

function toggleDropdown(item) {
  if (!item.children) return
  activeDropdown.value = activeDropdown.value === item.label ? null : item.label
}

function closeDropdown() {
  activeDropdown.value = null
}

function navigate(path) {
  closeDropdown()
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

// ===== 系统通知（审批结果等） =====
const unreadCount = ref(0)
const notifList = ref([])
let notifTimer = null

async function loadUnread() {
  try {
    const res = await getUnreadNotificationCountAPI()
    if (res && res.code === 200 && res.data) {
      unreadCount.value = Number(res.data.count || 0)
    }
  } catch { /* ignore */ }
}

async function loadNotifList() {
  try {
    const res = await getMyNotificationsAPI()
    if (res && res.code === 200) {
      notifList.value = (res.data || []).slice(0, 20)
    }
  } catch { /* ignore */ }
}

async function readNotif(n) {
  if (n.isRead === 1) return
  try {
    await markNotificationReadAPI(n.id)
    n.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch { /* ignore */ }
}

function formatNotifTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadUnread()
  notifTimer = setInterval(loadUnread, 15000)
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  if (notifTimer) clearInterval(notifTimer)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="app-layout">
    <!-- Top header bar -->
    <header class="app-header">
      <div class="header-left">
        <div class="logo-mark" @click="goHome" style="cursor:pointer">
          <span class="logo-leaf">🌾</span>
          <span class="logo-word">融销通</span>
        </div>
        <el-divider direction="vertical" />
        <span class="header-role">{{ roleTitle }}</span>
      </div>

      <div class="header-center">
        <nav ref="headerNavRef" class="header-nav">
          <div
            v-for="item in topLevelItems"
            :key="item.path || item.label"
            class="nav-tab"
            :class="{ active: isTabActive(item) }"
            @click="!item.children ? navigate(item.path) : toggleDropdown(item)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
            <el-icon v-if="item.children" class="dropdown-arrow"><ArrowDown /></el-icon>
            <!-- Dropdown submenu -->
            <transition name="dropdown">
              <div v-if="item.children && activeDropdown === item.label" class="nav-dropdown" @click.stop>
                <div
                  v-for="child in item.children"
                  :key="child.path"
                  class="dropdown-item"
                  :class="{ active: isActive(child.path) }"
                  @click="navigate(child.path)"
                >
                  <el-icon><component :is="child.icon" /></el-icon>
                  <span>{{ child.label }}</span>
                </div>
              </div>
            </transition>
          </div>
        </nav>
      </div>

      <div class="header-right">
        <el-popover placement="bottom-end" :width="360" trigger="click" @show="loadNotifList">
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
              <el-icon class="notif-bell"><Bell /></el-icon>
            </el-badge>
          </template>
          <div class="notif-panel">
            <div class="notif-head"><strong>系统通知</strong></div>
            <div class="notif-list">
              <div v-if="notifList.length === 0" class="notif-empty">暂无通知</div>
              <div
                v-for="n in notifList"
                :key="n.id"
                class="notif-item"
                :class="{ unread: n.isRead === 0 }"
                @click="readNotif(n)"
              >
                <div class="notif-title">{{ n.title }}</div>
                <div class="notif-content">{{ n.content }}</div>
                <div class="notif-time">{{ formatNotifTime(n.createTime) }}</div>
              </div>
            </div>
          </div>
        </el-popover>
        <div class="user-pill" @click="router.push('/profile/center')" style="cursor:pointer">
          <span class="user-avatar-circle">{{ (userStore.nickName || userStore.userInfo?.userName || 'U')[0] }}</span>
          <span class="user-name">{{ userStore.nickName || userStore.userInfo?.roleName || '用户' }}</span>
        </div>
        <el-button text size="small" class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出</span>
        </el-button>
      </div>
    </header>

    <!-- Main content area -->
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: var(--bg);
  display: flex;
  flex-direction: column;
}

/* ===== Header ===== */
.app-header {
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(27,67,50,0.04);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-left: 28px;
  min-width: 220px;
}
.logo-mark {
  display: flex;
  align-items: center;
  gap: 8px;
  transition: opacity 0.2s;
}
.logo-mark:hover { opacity: 0.7; }
.logo-leaf {
  font-size: 24px;
}
.logo-word {
  font-size: 18px;
  font-weight: 800;
  color: var(--green-deep);
  letter-spacing: 0.06em;
}
.header-role {
  font-size: 13px;
  color: var(--green);
  background: var(--green-mist);
  padding: 3px 12px;
  border-radius: 20px;
  font-weight: 600;
}
.el-divider {
  height: 20px;
}

/* Nav tabs */
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 24px;
  overflow: visible;
}
.header-nav {
  display: flex;
  gap: 4px;
  background: var(--bg);
  padding: 4px;
  border-radius: var(--radius-md);
}
.nav-tab {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
  white-space: nowrap;
  user-select: none;
  font-weight: 500;
}
.nav-tab:hover {
  color: var(--green);
  background: var(--green-mist);
}
.nav-tab.active {
  color: var(--green);
  background: var(--surface);
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(27,67,50,0.08);
}
.nav-tab .el-icon {
  font-size: 16px;
}
.dropdown-arrow {
  font-size: 12px;
  margin-left: 2px;
  transition: transform 0.2s;
}
.nav-tab:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* Dropdown submenu */
.nav-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  box-shadow: 0 8px 24px rgba(27,67,50,0.12);
  padding: 6px;
  min-width: 160px;
  z-index: 200;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
  white-space: nowrap;
  font-weight: 500;
}
.dropdown-item:hover {
  color: var(--green);
  background: var(--green-mist);
}
.dropdown-item.active {
  color: var(--green);
  background: var(--green-mist);
  font-weight: 600;
}
.dropdown-item .el-icon {
  font-size: 15px;
}

/* Dropdown transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-6px);
}
.dropdown-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-6px);
}

/* Right side */
.header-right {
  display: flex;
  align-items: center;
  padding-right: 28px;
}
.user-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 24px;
  transition: background 0.2s;
  cursor: pointer;
}
.user-pill:hover {
  background: var(--bg);
}
.user-avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--green), var(--green-light));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
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
  margin-left: 8px;
  transition: color 0.2s;
}
.logout-btn:hover { color: var(--green); }

/* ===== Main ===== */
.app-main {
  flex: 1;
  padding: 28px 36px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  min-height: calc(100vh - 70px);
}

/* Responsive */
@media (max-width: 900px) {
  .header-center { display: none; }
  .app-main { padding: 20px 16px; }
  .header-left { padding-left: 16px; }
  .header-right { padding-right: 16px; }
  .user-name { display: none; }
}

/* ===== 通知铃铛 ===== */
.notif-badge { margin-right: 18px; }
.notif-bell {
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}
.notif-bell:hover { color: var(--green); }
.notif-panel { margin: -4px -8px; }
.notif-head { padding: 8px 12px; border-bottom: 1px solid var(--border); }
.notif-list { max-height: 360px; overflow-y: auto; }
.notif-empty { padding: 32px 0; text-align: center; color: var(--text-muted); font-size: 13px; }
.notif-item {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.2s;
}
.notif-item:hover { background: var(--bg); }
.notif-item.unread { background: var(--green-mist); }
.notif-title { font-size: 14px; font-weight: 600; color: var(--text); }
.notif-content { font-size: 13px; color: var(--text-secondary); margin-top: 4px; white-space: pre-line; line-height: 1.5; }
.notif-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; }
</style>
