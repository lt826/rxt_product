<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConversationAPI, sendMessageAPI } from '../../api/message'
import { getUserInfoAPI } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { useMessageStore } from '../../stores/message'

const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()
const currentUserName = ref(userStore.userInfo?.userName || '')

const activeTab = ref('all') // all | unread | read
const selected = ref(new Set())

// 聊天弹窗
const chatVisible = ref(false)
const chatPartner = ref('')
const chatPartnerName = ref('')
const chatMessages = ref([])
const newMessage = ref('')
const messagesContainer = ref(null)
let pollTimer = null

const allConversations = computed(() => messageStore.conversations)
const filteredConversations = computed(() => {
  if (activeTab.value === 'unread') return allConversations.value.filter(c => !c.isRead)
  if (activeTab.value === 'read') return allConversations.value.filter(c => c.isRead)
  return allConversations.value
})
const unreadMsgCount = computed(() => messageStore.totalUnread)
const unreadConvCount = computed(() => allConversations.value.filter(c => !c.isRead).length)
const readConvCount = computed(() => allConversations.value.filter(c => c.isRead).length)

// 仅允许删除已读会话；选中的未读会话会被忽略
const selectedRead = computed(() =>
  allConversations.value.filter(c => selected.value.has(c.sender) && c.isRead).map(c => c.sender)
)
const allReadInViewSelected = computed(() => {
  const readInView = filteredConversations.value.filter(c => c.isRead)
  return readInView.length > 0 && readInView.every(c => selected.value.has(c.sender))
})
const hasReadInView = computed(() => filteredConversations.value.some(c => c.isRead))

function toggleSelect(sender) {
  const next = new Set(selected.value)
  if (next.has(sender)) next.delete(sender)
  else next.add(sender)
  selected.value = next
}
function toggleSelectAll(val) {
  const next = new Set(selected.value)
  filteredConversations.value.filter(c => c.isRead).forEach(c => {
    if (val) next.add(c.sender); else next.delete(c.sender)
  })
  selected.value = next
}

// 后端未读汇总不含昵称，这里为尚无昵称的会话补全真实姓名（仅请求一次后缓存到本地）
async function enrichNames() {
  const unresolved = allConversations.value.filter(c => !c.partnerName || c.partnerName === c.sender)
  for (const c of unresolved) {
    try {
      const res = await getUserInfoAPI(c.sender)
      if (res && res.code === 200 && res.data) {
        const name = res.data.realName || res.data.nickName || c.sender
        messageStore.trackPartner(c.sender, name)
      }
    } catch { /* ignore */ }
  }
}

function openChat(conv) {
  chatPartner.value = conv.sender
  chatPartnerName.value = conv.partnerName || conv.sender
  chatVisible.value = true
  loadChatMessages()
  messageStore.markRead(conv.sender)
}

async function loadChatMessages() {
  if (!chatPartner.value) return
  try {
    const res = await getConversationAPI(chatPartner.value)
    if (res && res.code === 200) {
      chatMessages.value = res.data || []
      const last = chatMessages.value[chatMessages.value.length - 1]
      if (last) messageStore.trackPartner(chatPartner.value, chatPartnerName.value, last.content, last.createTime)
      await nextTick()
      scrollToBottom()
    }
  } catch { /* ignore */ }
}

async function sendMessage() {
  if (!newMessage.value.trim() || !chatPartner.value) return
  try {
    const res = await sendMessageAPI({ toUser: chatPartner.value, content: newMessage.value.trim() })
    if (res && res.code === 200) { newMessage.value = ''; await loadChatMessages() }
  } catch { ElMessage.error('发送失败') }
}

async function deleteSelected() {
  const targets = selectedRead.value
  if (!targets.length) {
    ElMessage.warning('请勾选要删除的已读消息')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${targets.length} 条已读会话？`, '删除已读消息', { type: 'warning' })
  } catch { return }
  messageStore.hidePartners(targets)
  selected.value = new Set()
  ElMessage.success('已删除选中已读消息')
}

async function deleteAllRead() {
  const targets = allConversations.value.filter(c => c.isRead).map(c => c.sender)
  if (!targets.length) {
    ElMessage.info('没有可删除的已读消息')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除全部 ${targets.length} 条已读会话？`, '删除全部已读', { type: 'warning' })
  } catch { return }
  messageStore.hideAllRead()
  selected.value = new Set()
  ElMessage.success('已删除全部已读消息')
}

async function startChatWith(userName) {
  let name = userName
  try {
    const res = await getUserInfoAPI(userName)
    if (res && res.code === 200 && res.data) name = res.data.realName || res.data.nickName || userName
  } catch { /* ignore */ }
  messageStore.trackPartner(userName, name)
  openChat({ sender: userName, partnerName: name })
}

function formatTime(timeStr) { return timeStr ? String(timeStr).replace('T', ' ').substring(11, 16) : '' }
function formatDate(timeStr) { return timeStr ? String(timeStr).replace('T', ' ').substring(0, 16) : '' }
function scrollToBottom() { if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight }

async function tick() {
  await messageStore.refresh()
  enrichNames()
}

onMounted(() => {
  messageStore.init()
  tick()
  pollTimer = setInterval(tick, 10000)
  const to = route.query.to
  if (to) startChatWith(String(to))
})
onUnmounted(() => { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } })
</script>

<template>
  <div class="messages-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">消息通知</h2>
        <p class="page-sub">买家消息持续保留，已读/未读分类，可批量删除已读</p>
      </div>
      <el-badge :value="unreadMsgCount" :hidden="unreadMsgCount === 0" :max="99" type="danger">
        <el-tag type="info" effect="plain">未读 {{ unreadMsgCount }} 条 · 已读会话 {{ readConvCount }}</el-tag>
      </el-badge>
    </div>

    <el-card shadow="never">
      <!-- 工具条：分类 + 批量删除 -->
      <div class="toolbar">
        <el-radio-group v-model="activeTab">
          <el-radio-button value="all">全部 ({{ allConversations.length }})</el-radio-button>
          <el-radio-button value="unread">未读 ({{ unreadConvCount }})</el-radio-button>
          <el-radio-button value="read">已读 ({{ readConvCount }})</el-radio-button>
        </el-radio-group>
        <div class="toolbar-actions">
          <el-checkbox v-if="hasReadInView" :model-value="allReadInViewSelected" @change="toggleSelectAll">全选已读</el-checkbox>
          <el-button size="small" type="danger" plain :disabled="!selectedRead.length" @click="deleteSelected">
            删除选中已读<span v-if="selectedRead.length"> ({{ selectedRead.length }})</span>
          </el-button>
          <el-button size="small" type="danger" :disabled="!readConvCount" @click="deleteAllRead">删除全部已读</el-button>
        </div>
      </div>

      <el-empty v-if="!filteredConversations.length" :description="activeTab === 'unread' ? '没有未读消息' : (activeTab === 'read' ? '没有已读消息' : '暂无消息')" :image-size="80" />

      <div v-else class="conv-list">
        <div
          v-for="conv in filteredConversations"
          :key="conv.sender"
          class="conversation-item"
          :class="{ unread: !conv.isRead, selected: selected.has(conv.sender) }"
        >
          <el-checkbox
            v-if="conv.isRead"
            :model-value="selected.has(conv.sender)"
            class="conv-check"
            @change="toggleSelect(conv.sender)"
            @click.stop
          />
          <span v-else class="conv-dot" title="未读"></span>
          <div class="conv-avatar">📦</div>
          <div class="conv-body" @click="openChat(conv)">
            <div class="conv-header">
              <span class="conv-name">{{ conv.partnerName || conv.sender }}</span>
              <span class="conv-time">{{ formatDate(conv.lastTime) }}</span>
            </div>
            <div class="conv-preview">{{ conv.lastContent || '点击查看对话' }}</div>
          </div>
          <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" :max="99" type="danger" />
          <el-tag v-else size="small" type="info" effect="plain">已读</el-tag>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="chatVisible" :title="'与 ' + (chatPartnerName || chatPartner).toString()" width="600px" destroy-on-close>
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="chatMessages.length === 0" style="text-align:center;padding:40px 0;color:var(--text-muted)">
          暂无消息，开始对话吧！
        </div>
        <div
          v-for="msg in chatMessages"
          :key="msg.messageId"
          class="message-row"
          :class="msg.fromUser === currentUserName ? 'sent' : 'received'"
        >
          <div class="message-bubble">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
      <div class="chat-input-area">
        <el-input v-model="newMessage" type="textarea" :rows="3" placeholder="回复消息..." @keydown.enter.ctrl="sendMessage" />
        <div class="chat-input-actions">
          <el-button size="small" @click="chatVisible = false">关闭</el-button>
          <el-button size="small" type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.messages-page { max-width: 1200px; margin: 0 auto; }
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.toolbar { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; margin-bottom: 8px; }
.toolbar-actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }

.conv-list { display: flex; flex-direction: column; }
.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 8px;
  border-bottom: 1px solid var(--border);
  transition: background 0.2s;
}
.conversation-item:last-child { border: none; }
.conversation-item.unread { background: var(--green-mist); border-radius: 8px; padding-left: 12px; }
.conversation-item.selected { background: #fef0f0; border-radius: 8px; }
.conv-check { flex-shrink: 0; }
.conv-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--green); flex-shrink: 0; }
.conv-avatar { font-size: 30px; }
.conv-body { flex: 1; min-width: 0; cursor: pointer; }
.conv-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.conv-name { font-weight: 600; color: var(--text); }
.conv-time { font-size: 12px; color: var(--text-muted); white-space: nowrap; }
.conv-preview { font-size: 13px; color: var(--text-muted); margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

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
</style>
