<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getFarmersAPI, getUserInfoAPI } from '../../api/user'
import { getConversationAPI, sendMessageAPI } from '../../api/message'
import { followAPI, unfollowAPI, checkFollowAPI } from '../../api/follow'
import { useUserStore } from '../../stores/user'
import { useMessageStore } from '../../stores/message'
import { parseGoodsCard, goodsPreviewText } from '../../utils/goodsMessage'
import GoodsCardMessage from '../../components/GoodsCardMessage.vue'

const userStore = useUserStore()
const messageStore = useMessageStore()
const currentUserName = ref(userStore.userInfo?.userName || '')

const allFarmers = ref([])
const loading = ref(false)
const activeTab = ref('contacted') // contacted | contactable
const keyword = ref('')
const page = ref(1)
const pageSize = ref(8)

// 聊天弹窗
const chatVisible = ref(false)
const chatFarmer = ref(null)
const chatMessages = ref([])
const newMessage = ref('')
const messagesContainer = ref(null)
let refreshTimer = null
let chatTimer = null

// 农户资料弹窗（点头像打开；不含电话/地址；可关注/取消关注）
const profileVisible = ref(false)
const profileUser = ref(null)
const profileLoading = ref(false)
const following = ref(false)
const followLoading = ref(false)

// 会话映射：userName -> 会话信息（含未读数/预览）
const convMap = computed(() => {
  const m = {}
  messageStore.conversations.forEach(c => { m[c.sender] = c })
  return m
})

// 已联系农户：存在会话记录的农户，未读置顶
const contactedFarmers = computed(() => {
  const list = allFarmers.value
    .filter(f => convMap.value[f.userName])
    .map(f => {
      const c = convMap.value[f.userName]
      return {
        ...f,
        unreadCount: c.unreadCount || 0,
        lastContent: c.lastContent || '',
        lastTime: c.lastTime || '',
        partnerName: c.partnerName || f.realName || f.nickName || f.userName
      }
    })
  list.sort((a, b) => {
    const au = a.unreadCount > 0 ? 1 : 0
    const bu = b.unreadCount > 0 ? 1 : 0
    if (au !== bu) return bu - au
    return (b.lastTime || '').localeCompare(a.lastTime || '')
  })
  return list
})
const contactedUnread = computed(() => contactedFarmers.value.filter(f => f.unreadCount > 0).length)
const contactedSet = computed(() => new Set(contactedFarmers.value.map(f => f.userName)))
const contactableFarmers = computed(() => allFarmers.value.filter(f => !contactedSet.value.has(f.userName)))

const filteredList = computed(() => {
  const base = activeTab.value === 'contacted' ? contactedFarmers.value : contactableFarmers.value
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return base
  return base.filter(f =>
    (f.realName || '').toLowerCase().includes(kw) ||
    (f.nickName || '').toLowerCase().includes(kw) ||
    (f.userName || '').toLowerCase().includes(kw) ||
    (f.phone || '').toLowerCase().includes(kw) ||
    (f.address || '').toLowerCase().includes(kw)
  )
})
const total = computed(() => filteredList.value.length)
const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// 切换 Tab 或搜索时回到第一页
watch([activeTab, keyword], () => { page.value = 1 })

async function loadFarmers() {
  loading.value = true
  try {
    const res = await getFarmersAPI()
    if (res && res.code === 200) allFarmers.value = (res.data || []).filter(u => u && u.role === 'farmer')
  } catch { ElMessage.error('获取农户列表失败') } finally { loading.value = false }
}

function openChat(f) {
  chatFarmer.value = f
  chatVisible.value = true
  loadChatMessages()
  // 进入对话即清除该农户未读（红点消失）
  if ((convMap.value[f.userName]?.unreadCount || 0) > 0) messageStore.markRead(f.userName)
  chatTimer = setInterval(loadChatMessages, 3000)
}

async function loadChatMessages() {
  if (!chatFarmer.value) return
  try {
    const res = await getConversationAPI(chatFarmer.value.userName)
    if (res && res.code === 200) {
      const prevLen = chatMessages.value.length
      chatMessages.value = res.data || []
      if (chatMessages.value.length > prevLen) { await nextTick(); scrollToBottom() }
    }
  } catch { /* ignore */ }
}

async function sendMessage() {
  if (!newMessage.value.trim() || !chatFarmer.value) return
  try {
    const res = await sendMessageAPI({ toUser: chatFarmer.value.userName, content: newMessage.value.trim() })
    if (res && res.code === 200) {
      newMessage.value = ''
      await loadChatMessages()
      // 主动联系 → 记入"已联系农户"并刷新预览
      const last = chatMessages.value[chatMessages.value.length - 1]
      const name = chatFarmer.value.realName || chatFarmer.value.nickName || chatFarmer.value.userName
      messageStore.trackPartner(chatFarmer.value.userName, name, last?.content, last?.createTime)
    }
  } catch { ElMessage.error('发送失败') }
}

function closeChat() {
  if (chatTimer) { clearInterval(chatTimer); chatTimer = null }
  chatVisible.value = false
  chatFarmer.value = null
  chatMessages.value = []
  newMessage.value = ''
}

// 打开农户资料弹窗：拉取完整资料 + 查询是否已关注
async function openProfile() {
  if (!chatFarmer.value) return
  profileVisible.value = true
  profileLoading.value = true
  following.value = false
  try {
    const [infoRes, checkRes] = await Promise.all([
      getUserInfoAPI(chatFarmer.value.userName),
      checkFollowAPI(chatFarmer.value.userName).catch(() => null)
    ])
    if (infoRes && infoRes.code === 200 && infoRes.data) profileUser.value = infoRes.data
    if (checkRes && checkRes.code === 200) following.value = !!checkRes.data.following
  } catch { /* ignore */ } finally { profileLoading.value = false }
}

// 关注 / 取消关注
async function toggleFollow() {
  if (!profileUser.value) return
  const uname = profileUser.value.userName
  followLoading.value = true
  try {
    if (following.value) {
      await unfollowAPI(uname)
      following.value = false
      ElMessage.success('已取消关注')
    } else {
      const res = await followAPI(uname)
      following.value = true
      ElMessage.success(res && res.data && res.data.newFollow === false ? '已关注' : '关注成功')
    }
  } catch { ElMessage.error('操作失败') } finally { followLoading.value = false }
}

function creditType(c) {
  const n = Number(c)
  if (n >= 80) return 'success'
  if (n >= 60) return 'warning'
  return 'danger'
}

// 解析消息为商品卡片（农户发来的商品可在卡片上一键加入购物车）
function goodsCardOf(msg) { return parseGoodsCard(msg && msg.content) }

function formatTime(timeStr) { return timeStr ? String(timeStr).replace('T', ' ').substring(11, 16) : '' }
function formatLast(timeStr) { return timeStr ? String(timeStr).replace('T', ' ').substring(0, 16) : '' }
function scrollToBottom() { if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight }

async function tick() { await messageStore.refresh() }

onMounted(async () => {
  messageStore.init()
  await loadFarmers()
  tick()
  refreshTimer = setInterval(tick, 10000)
})
onUnmounted(() => {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  if (chatTimer) { clearInterval(chatTimer); chatTimer = null }
})
</script>

<template>
  <div class="contacts-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">联系农户</h2>
        <p class="page-sub">与农户在线沟通；收到新消息的农户会自动置顶并标红</p>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header-row">
          <el-radio-group v-model="activeTab">
            <el-radio-button value="contacted">
              已联系农户 ({{ contactedFarmers.length }})<span v-if="contactedUnread > 0" class="tab-red-dot"></span>
            </el-radio-button>
            <el-radio-button value="contactable">可联系农户 ({{ contactableFarmers.length }})</el-radio-button>
          </el-radio-group>
          <el-input v-model="keyword" placeholder="搜索姓名 / 手机号 / 地址" clearable style="width:240px" :prefix-icon="Search" />
        </div>
      </template>

      <el-empty v-if="!pagedList.length" :description="activeTab === 'contacted' ? '暂无已联系农户，请前往可联系农户发起沟通' : '暂无可联系农户'" :image-size="80" />

      <div v-else class="farmer-list">
        <div
          v-for="f in pagedList"
          :key="f.userName"
          class="farmer-card"
          :class="{ 'has-unread': f.unreadCount > 0 }"
        >
          <div class="farmer-info">
            <div class="farmer-avatar">👨‍🌾</div>
            <div class="farmer-text">
              <div class="farmer-name">
                {{ f.realName || f.nickName || f.userName }}
                <el-badge v-if="f.unreadCount > 0" :value="f.unreadCount" :max="99" type="danger" class="name-badge" />
              </div>
              <div class="farmer-detail">{{ f.nickName || f.userName }} · {{ f.phone || '暂无电话' }} · {{ f.address || '地址未填写' }}</div>
              <div v-if="f.lastContent" class="farmer-last">最新消息：{{ goodsPreviewText(f.lastContent) }}<span v-if="f.lastTime"> · {{ formatLast(f.lastTime) }}</span></div>
            </div>
          </div>
          <el-button :type="f.unreadCount > 0 ? 'danger' : 'primary'" size="small" @click="openChat(f)">
            {{ f.unreadCount > 0 ? '查看消息' : '联系' }}
          </el-button>
        </div>
      </div>

      <div v-if="total > pageSize" class="pager">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="(p) => (page = p)"
        />
      </div>
    </el-card>

    <el-dialog v-model="chatVisible" width="600px" @close="closeChat">
      <template #header>
        <div class="chat-header" @click="openProfile" title="点击查看农户资料">
          <el-avatar :size="40" :src="chatFarmer?.avatar" class="chat-header-avatar">
            <span class="chat-header-emoji">👨‍🌾</span>
          </el-avatar>
          <div class="chat-header-text">
            <div class="chat-header-name">{{ chatFarmer?.realName || chatFarmer?.nickName || chatFarmer?.userName || '' }}</div>
            <div class="chat-header-tip">点击头像查看资料 · 关注后可收到ta的新商品通知</div>
          </div>
        </div>
      </template>
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="chatMessages.length === 0" style="text-align:center;padding:40px 0;color:var(--text-muted)">
          暂无消息，开始对话吧！
        </div>
        <div v-for="msg in chatMessages" :key="msg.messageId" class="message-row" :class="msg.fromUser === currentUserName ? 'sent' : 'received'">
          <GoodsCardMessage v-if="goodsCardOf(msg)" :card="goodsCardOf(msg)" :can-buy="true" />
          <div v-else class="message-bubble">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
      <div class="chat-input-area">
        <el-input v-model="newMessage" type="textarea" :rows="3" placeholder="输入消息..." @keydown.enter.ctrl="sendMessage" />
        <div class="chat-input-actions">
          <el-button size="small" @click="chatVisible = false">关闭</el-button>
          <el-button size="small" type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 农户资料弹窗（不显示电话/地址） -->
    <el-dialog v-model="profileVisible" title="农户资料" width="420px" append-to-body>
      <div v-loading="profileLoading" class="profile-box">
        <div class="profile-top">
          <el-avatar :size="64" :src="profileUser?.avatar" class="profile-avatar">
            <span class="chat-header-emoji">👨‍🌾</span>
          </el-avatar>
          <div class="profile-name">{{ profileUser?.realName || profileUser?.nickName || profileUser?.userName || '-' }}</div>
          <el-tag size="small" type="success" effect="plain">农户</el-tag>
        </div>
        <el-descriptions :column="1" border size="small" class="profile-desc">
          <el-descriptions-item label="昵称">{{ profileUser?.nickName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ profileUser?.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="信誉分">
            <el-tag :type="creditType(profileUser?.credit)" size="small" effect="light">{{ profileUser?.credit ?? 0 }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="profile-notice">为保护农户隐私，电话与地址不对外展示，可在聊天中沟通。</div>
        <el-button
          :type="following ? 'default' : 'primary'"
          :loading="followLoading"
          class="profile-follow-btn"
          @click="toggleFollow"
        >
          {{ following ? '已关注 · 取消关注' : '+ 关注' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.contacts-page { max-width: 1000px; margin: 0 auto; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.card-header-row { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.tab-red-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: var(--el-color-danger, #f56c6c); margin-left: 6px; vertical-align: middle; }

.farmer-list { display: flex; flex-direction: column; }
.farmer-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 12px; border-bottom: 1px solid var(--border); border-left: 4px solid transparent;
  transition: background 0.2s;
}
.farmer-card:last-child { border: none; }
.farmer-card.has-unread { background: #fef0f0; border-left-color: var(--el-color-danger, #f56c6c); border-radius: 6px; }
.farmer-info { display: flex; align-items: center; gap: 12px; min-width: 0; }
.farmer-avatar { font-size: 36px; flex-shrink: 0; }
.farmer-text { min-width: 0; }
.farmer-name { font-weight: 600; color: var(--text); font-size: 15px; display: flex; align-items: center; gap: 8px; }
.name-badge { margin-left: 4px; }
.farmer-detail { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.farmer-last { font-size: 12px; color: var(--el-color-danger, #f56c6c); margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 520px; }

.pager { display: flex; justify-content: flex-end; margin-top: 16px; }

.chat-messages { max-height: 400px; overflow-y: auto; padding: 12px 0; }
.message-row { display: flex; flex-direction: column; margin-bottom: 12px; }
.message-row.sent { align-items: flex-end; }
.message-row.received { align-items: flex-start; }
.message-bubble { max-width: 70%; padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.message-row.sent .message-bubble { background: var(--green); color: #fff; }
.message-row.received .message-bubble { background: var(--bg); color: var(--text); }
.message-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; }
.chat-input-area { margin-top: 12px; }
.chat-input-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }

/* 聊天头部可点头像 */
.chat-header { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.chat-header-avatar { flex-shrink: 0; background: linear-gradient(135deg, var(--green), var(--green-light)); }
.chat-header-emoji { font-size: 22px; }
.chat-header-text { min-width: 0; }
.chat-header-name { font-size: 16px; font-weight: 700; color: var(--text); }
.chat-header-tip { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.chat-header:hover .chat-header-name { color: var(--green); }

/* 农户资料弹窗 */
.profile-box { padding: 4px 0; }
.profile-top { display: flex; flex-direction: column; align-items: center; gap: 8px; margin-bottom: 16px; }
.profile-avatar { background: linear-gradient(135deg, var(--green), var(--green-light)); }
.profile-name { font-size: 18px; font-weight: 700; color: var(--text); }
.profile-desc { margin-bottom: 12px; }
.profile-notice { font-size: 12px; color: var(--text-muted); background: var(--bg); padding: 8px 12px; border-radius: 6px; margin-bottom: 16px; line-height: 1.6; }
.profile-follow-btn { width: 100%; }
:deep(.profile-desc .el-descriptions__label) { width: 90px; min-width: 90px; }
</style>
