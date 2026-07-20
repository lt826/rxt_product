<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getConversationAPI, sendMessageAPI } from '../../api/message'
import { getFarmersAPI, getUserInfoAPI } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { parseGoodsCard } from '../../utils/goodsMessage'
import GoodsCardMessage from '../../components/GoodsCardMessage.vue'

const route = useRoute()
const userStore = useUserStore()
const currentUserName = ref(userStore.userInfo?.userName || '')
const conversations = ref([])
const loading = ref(false)
const chatVisible = ref(false)
const chatPartner = ref('')
const chatPartnerName = ref('')
const chatMessages = ref([])
const newMessage = ref('')
const messagesContainer = ref(null)
let pollTimer = null

onMounted(() => {
  loadConversations()
  // 支持 ?to=xxx 直接打开与某用户的聊天（接取者去私聊跳转用）
  const to = route.query.to
  if (to) startChatWith(String(to))
})
onUnmounted(() => { clearPollTimer() })

async function loadConversations() {
  loading.value = true
  try {
    const res = await getFarmersAPI()
    if (res && res.code === 200) {
      conversations.value = (res.data || []).filter(u => u && u.role === 'farmer').map(f => ({
        sender: f.userName,
        partnerName: f.realName || f.nickName || f.userName,
        lastContent: '点击开始对话',
        lastTime: '',
        unreadCount: 0
      }))
    }
  } catch { /* ignore */ } finally { loading.value = false }
}

async function startChatWith(userName) {
  let partnerName = userName
  try {
    const res = await getUserInfoAPI(userName)
    if (res && res.code === 200 && res.data) {
      partnerName = res.data.realName || res.data.nickName || userName
    }
  } catch { /* ignore */ }
  openChat({ sender: userName, partnerName })
}

function openChat(partner) {
  chatPartner.value = partner.sender
  chatPartnerName.value = partner.partnerName || partner.sender
  chatVisible.value = true
  loadChatMessages()
  pollTimer = setInterval(loadChatMessages, 3000)
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
    if (res && res.code === 200) { newMessage.value = ''; await loadChatMessages() }
  } catch { ElMessage.error('发送失败') }
}

// 解析消息为商品卡片（农户发来的商品可在卡片上一键加入购物车）
function goodsCardOf(msg) { return parseGoodsCard(msg && msg.content) }

function formatTime(timeStr) { if (!timeStr) return ''; return timeStr.replace('T', ' ').substring(11, 16) }
function scrollToBottom() { if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight }
function clearPollTimer() { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }
</script>

<template>
  <div class="messages-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">买家消息</h2>
        <p class="page-sub">与农户沟通，了解需求</p>
      </div>
    </div>
    <el-card shadow="never" v-loading="loading">
      <template #header><strong>对话列表</strong></template>
      <div v-if="conversations.length === 0" style="text-align:center;padding:40px 0">
        <el-empty description="暂无对话" :image-size="80" />
      </div>
      <div v-for="conv in conversations" :key="conv.sender" class="conversation-item" @click="openChat(conv)">
        <div class="conv-avatar">{{ (conv.partnerName || conv.sender || '农').trim().charAt(0) }}</div>
        <div class="conv-body">
          <div class="conv-header">
            <span class="conv-name">{{ conv.partnerName }}</span>
          </div>
          <div class="conv-preview">{{ conv.lastContent }}</div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="chatVisible" :title="'与 ' + (chatPartnerName || chatPartner).toString()" width="600px">
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
  </div>
</template>

<style scoped>
.messages-page { max-width: 800px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.conversation-item { display: flex; align-items: center; gap: 12px; padding: 14px 0; border-bottom: 1px solid var(--border); cursor: pointer; transition: background 0.2s; }
.conversation-item:hover { background: var(--bg); }
.conv-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--green), var(--green-light));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  flex-shrink: 0;
}
.conv-body { flex: 1; min-width: 0; }
.conv-header { display: flex; justify-content: space-between; align-items: center; }
.conv-name { font-weight: 500; color: var(--text); }
.conv-preview { font-size: 13px; color: var(--text-muted); margin-top: 4px; }
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
