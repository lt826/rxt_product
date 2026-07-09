<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getConversationAPI, sendMessageAPI, getUnreadSummaryAPI, markAsReadAPI } from '../../api/message'
import { getUserInfoAPI } from '../../api/user'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const currentUserName = ref(userStore.userInfo?.userName || '')
const conversations = ref([])
const unreadBadge = ref(0)
let pollTimer = null

const chatVisible = ref(false)
const chatPartner = ref('')
const chatPartnerName = ref('')
const chatMessages = ref([])
const newMessage = ref('')
const messagesContainer = ref(null)

onMounted(() => {
  loadConversations()
  loadUnreadCount()
  pollTimer = setInterval(() => { loadConversations(); loadUnreadCount() }, 10000)
})
onUnmounted(() => { clearPollTimer() })

async function loadUnreadCount() {
  try {
    const res = await getUnreadSummaryAPI()
    if (res && res.code === 200 && res.data) {
      unreadBadge.value = res.data.reduce((sum, item) => sum + (item.unreadCount || 0), 0)
    }
  } catch { /* ignore */ }
}

async function loadConversations() {
  try {
    const res = await getUnreadSummaryAPI()
    if (res && res.code === 200 && res.data) {
      const items = res.data
      for (const item of items) {
        try {
          const userRes = await getUserInfoAPI(item.sender)
          if (userRes && userRes.code === 200 && userRes.data) {
            item.partnerName = userRes.data.realName || userRes.data.nickName || item.sender
          }
        } catch { item.partnerName = item.sender }
      }
      conversations.value = items
    }
  } catch { /* ignore */ }
}

function openChat(partner) {
  chatPartner.value = partner.sender
  chatPartnerName.value = partner.partnerName || partner.sender
  chatVisible.value = true
  loadChatMessages()
  markAsRead(chatPartner.value)
}

async function markAsRead(sender) {
  try { await markAsReadAPI(sender); loadUnreadCount() } catch { /* ignore */ }
}

async function loadChatMessages() {
  if (!chatPartner.value) return
  try {
    const res = await getConversationAPI(chatPartner.value)
    if (res && res.code === 200) {
      chatMessages.value = res.data || []
      await nextTick()
      scrollToBottom()
    }
  } catch { /* ignore */ }
}

async function sendMessage() {
  if (!newMessage.value.trim() || !chatPartner.value) return
  try {
    const res = await sendMessageAPI({ toUser: chatPartner.value, content: newMessage.value.trim() })
    if (res && res.code === 200) {
      newMessage.value = ''
      await loadChatMessages()
    }
  } catch { ElMessage.error('发送失败') }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(11, 16)
}
function formatDate(timeStr) {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}
function scrollToBottom() {
  if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
}
function clearPollTimer() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}
</script>

<template>
  <div class="messages-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">消息中心</h2>
        <p class="page-sub">管理你的消息和对话</p>
      </div>
    </div>

    <el-badge :value="unreadBadge" :hidden="unreadBadge === 0" :max="99">
      <h3 style="margin:0 0 16px;font-size:16px;font-weight:600;color:var(--green-deep)">买家消息</h3>
    </el-badge>

    <el-card shadow="never" v-if="conversations.length > 0">
      <div
        v-for="conv in conversations"
        :key="conv.sender"
        class="conversation-item"
        :class="{ unread: conv.unreadCount > 0 }"
        @click="openChat(conv)"
      >
        <div class="conv-avatar">📦</div>
        <div class="conv-body">
          <div class="conv-header">
            <span class="conv-name">{{ conv.partnerName || conv.sender }}</span>
            <span class="conv-time">{{ formatDate(conv.lastTime) }}</span>
          </div>
          <div class="conv-preview">{{ conv.lastContent }}</div>
        </div>
        <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" :max="99" />
      </div>
    </el-card>
    <el-card shadow="never" v-else>
      <el-empty description="暂无买家消息" :image-size="80" />
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
.messages-page { max-width: 800px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.2s;
}
.conversation-item:hover { background: var(--bg); }
.conversation-item:last-child { border: none; }
.conversation-item.unread { background: var(--green-mist); margin: 0 -16px; padding: 14px 16px; border-radius: 8px; }
.conv-avatar { font-size: 32px; }
.conv-body { flex: 1; min-width: 0; }
.conv-header { display: flex; justify-content: space-between; align-items: center; }
.conv-name { font-weight: 500; color: var(--text); }
.conv-time { font-size: 12px; color: var(--text-muted); }
.conv-preview { font-size: 13px; color: var(--text-muted); margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.chat-messages { max-height: 400px; overflow-y: auto; padding: 12px 0; }
.message-row { display: flex; flex-direction: column; margin-bottom: 12px; }
.message-row.sent { align-items: flex-end; }
.message-row.received { align-items: flex-start; }
.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}
.message-row.sent .message-bubble { background: var(--green); color: #fff; }
.message-row.received .message-bubble { background: var(--bg); color: var(--text); }
.message-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; }
.chat-input-area { margin-top: 12px; }
.chat-input-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
</style>
