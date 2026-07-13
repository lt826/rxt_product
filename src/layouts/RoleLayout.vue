<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { getUnreadNotificationCountAPI, getMyNotificationsAPI, markNotificationReadAPI } from '../api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sidebarCollapsed = ref(false)

const role = computed(() => route.meta.role)
const roleTitle = computed(() => route.meta.title)

const ROLE_MENU_MAP = {
  farmer: [
    { path: "/farmer/dashboard", label: "工作台", icon: "Odometer" },
    { path: "/farmer/products", label: "商品货源", icon: "Goods" },
    { path: "/farmer/demands", label: "求购大厅", icon: "ShoppingCart" },
    { path: "/farmer/financing-intention", label: "融资意向", icon: "DocumentAdd" },
    { path: "/farmer/financing", label: "融资申请", icon: "Coin" },
    { path: "/farmer/partner-invitations", label: "联合贷款邀请", icon: "Connection" },
    { path: "/farmer/expert-qa", label: "专家问答", icon: "ChatDotRound" },
    { path: "/farmer/appointments", label: "专家预约", icon: "Calendar" },
    { path: "/farmer/knowledge", label: "农业知识", icon: "Reading" },
    { path: "/farmer/messages", label: "消息通知", icon: "Bell" }
  ],
  buyer: [
    { path: '/buyer/dashboard', label: '工作台', icon: 'Odometer' },
    { path: '/buyer/products', label: '商品采购', icon: 'ShoppingBag' },
    { path: '/buyer/demand', label: '收购需求', icon: 'Document' },
    { path: '/buyer/contacts', label: '联系农户', icon: 'User' },
    { path: '/buyer/messages', label: '买家消息', icon: 'Bell' }
  ],
  bank: [
    { path: '/bank/dashboard', label: '工作台', icon: 'Odometer' },
    { path: '/bank/approvals', label: '融资审批', icon: 'DocumentChecked' },
    { path: '/bank/products', label: '产品配置', icon: 'Setting' },
    { path: '/bank/match-rules', label: '匹配规则', icon: 'Connection' }
  ],
  expert: [
    { path: '/expert/dashboard', label: '工作台', icon: 'Odometer' },
    { path: '/expert/knowledge', label: '知识管理', icon: 'Reading' },
    { path: '/expert/qa', label: '问答处理', icon: 'ChatDotSquare' },
    { path: '/expert/appointments', label: '预约处理', icon: 'Calendar' }
  ],
  admin: [
    { path: '/admin/dashboard', label: '工作台', icon: 'Odometer' },
    { path: '/admin/users', label: '用户管理', icon: 'UserFilled' },
    { path: '/admin/config', label: '系统配置', icon: 'Setting' },
    { path: '/admin/stats', label: '数据统计', icon: 'DataAnalysis' }
  ]
}

const menuItems = computed(() => ROLE_MENU_MAP[role.value] || [])

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
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
})
onUnmounted(() => {
  if (notifTimer) clearInterval(notifTimer)
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
        <nav class="header-nav">
          <div
            v-for="item in menuItems"
            :key="item.path"
            class="nav-tab"
            :class="{ active: isActive(item.path) }"
            @click="navigate(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
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
          <el-dropdown trigger="click">
            <span class="dropdown-trigger">
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile/center')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
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
  overflow-x: auto;
}
.header-nav {
  display: flex;
  gap: 4px;
  background: var(--bg);
  padding: 4px;
  border-radius: var(--radius-md);
}
.nav-tab {
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
.dropdown-trigger {
  color: var(--text-muted);
  display: flex;
  transition: color 0.2s;
}
.dropdown-trigger:hover {
  color: var(--green);
}

/* ===== Main ===== */
.app-main {
  flex: 1;
  padding: 28px 36px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
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
