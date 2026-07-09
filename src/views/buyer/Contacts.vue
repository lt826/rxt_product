<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getFarmersAPI } from '../../api/user'
import { getConversationAPI, sendMessageAPI, markAsReadAPI } from '../../api/message'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const currentUserName = ref(userStore.userInfo?.userName || '')
const farmers = ref([])
const loading = ref(false)
const chatVisible = ref(false)
const chatFarmer = ref(null)
const chatMessages = ref([])
const newMessage = ref('')
const messagesContainer = ref(null)
let pollTimer = null

onMounted(async () => { await loadFarmers() })
onUnmounted(() => { clearPollTimer() })

async function loadFarmers() {
  loading.value = true
  try {
    const res = await getFarmersAPI()
    if (res && res.code === 200) farmers.value = (res.data || []).filter(u => u && u.role === 'farmer')
  } catch { ElMessage.error('获取农户列表失败') } finally { loading.value = false }
}

function openChat(farmer) {
  chatFarmer.value = farmer
  chatVisible.value = true
  loadChatMessages()
  pollTimer = setInterval(loadChatMessages, 3000)
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
    if (res && res.code === 200) { newMessage.value = ''; await loadChatMessages() }
  } catch { ElMessage.error('发送失败') }
}

async function markPartnerRead() {
  if (!chatFarmer.value) return
  try { await markAsReadAPI(chatFarmer.value.userName) } catch { /* ignore */ }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(11, 16)
}
function scrollToBottom() {
  if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
}
function closeChat() { clearPollTimer(); markPartnerRead(); chatVisible.value = false; chatFarmer.value = null; chatMessages.value = []; newMessage.value = '' }
function clearPollTimer() { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }
</script>

<template>
  <div class="contacts-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">联系农户</h2>
        <p class="page-sub">查看农户信息，直接与农户沟通</p>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <template #header><strong>意向农户列表</strong></template>
      <div v-if="farmers.length === 0" style="text-align:center;padding:40px 0">
        <el-empty description="暂无农户数据" :image-size="80" />
      </div>
      <div v-for="f in farmers" :key="f.userName" class="farmer-card">
        <div class="farmer-info">
          <div class="farmer-avatar">👨‍🌾</div>
          <div>
            <div class="farmer-name">{{ f.realName || f.nickName || f.userName }}</div>
            <div class="farmer-detail">{{ f.nickName || f.userName }} · {{ f.phone || '暂无电话' }} · {{ f.address || '地址未填写' }}</div>
          </div>
        </div>
        <el-button type="primary" size="small" @click="openChat(f)">联系</el-button>
      </div>
    </el-card>

    <el-dialog v-model="chatVisible" :title="'与 ' + (chatFarmer?.realName || chatFarmer?.nickName || chatFarmer?.userName || '').toString()" width="600px" @close="closeChat">
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="chatMessages.length === 0" style="text-align:center;padding:40px 0;color:var(--text-muted)">
          暂无消息，开始对话吧！
        </div>
        <div v-for="msg in chatMessages" :key="msg.messageId" class="message-row" :class="msg.fromUser === currentUserName ? 'sent' : 'received'">
          <div class="message-bubble">{{ msg.content }}</div>
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
.contacts-page { max-width: 1000px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.farmer-card { display: flex; align-items: center; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid var(--border); }
.farmer-card:last-child { border: none; }
.farmer-info { display: flex; align-items: center; gap: 12px; }
.farmer-avatar { font-size: 36px; }
.farmer-name { font-weight: 600; color: var(--text); font-size: 15px; }
.farmer-detail { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
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
