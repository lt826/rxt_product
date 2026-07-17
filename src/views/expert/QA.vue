<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { listQuestionsForExpert, getQuestionMessagesAPI, replyQuestionAPI } from '../../api/question'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const currentUserName = ref(userStore.userInfo?.userName || '')
const loading = ref(false)
const list = ref([])
const activeFilter = ref('all')

// 聊天弹窗
const chatVisible = ref(false)
const chatQuestion = ref(null)
const chatMessages = ref([])
const newReply = ref('')
const messagesContainer = ref(null)
let pollTimer = null

async function loadList() {
  loading.value = true
  try {
    const res = await listQuestionsForExpert()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const filtered = computed(() =>
  list.value.filter((q) => {
    if (activeFilter.value === 'pending') return q.status === 0
    if (activeFilter.value === 'done') return q.status === 1
    return true
  })
)

async function openChat(q) {
  chatQuestion.value = q
  chatVisible.value = true
  newReply.value = ''
  await loadChatMessages()
}

async function loadChatMessages() {
  if (!chatQuestion.value) return
  try {
    const res = await getQuestionMessagesAPI(chatQuestion.value.id)
    if (res.code === 200) {
      chatMessages.value = res.data || []
      // 同新消息把列表里的 status 刷新（专家回复后变为已回复）
      const hasExpertReply = chatMessages.value.some(m => m.senderRole === 'expert')
      if (chatQuestion.value.status === 0 && hasExpertReply) {
        chatQuestion.value.status = 1
      }
      await nextTick()
      scrollToBottom()
    }
  } catch { /* ignore */ }
}

async function sendReply() {
  if (!newReply.value.trim() || !chatQuestion.value) return
  try {
    const res = await replyQuestionAPI(chatQuestion.value.id, newReply.value.trim())
    if (res.code === 200) {
      newReply.value = ''
      await loadChatMessages()
      ElMessage.success('回复成功')
    }
  } catch { ElMessage.error('发送失败') }
}

function formatTime(timeStr) { return timeStr ? String(timeStr).replace('T', ' ').substring(11, 16) : '' }
function scrollToBottom() { if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight }

function startPolling() {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (chatVisible.value) await loadChatMessages()
  }, 10000)
}
function stopPolling() { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }
function onChatClosed() { stopPolling(); loadList() }

onMounted(loadList)
onUnmounted(stopPolling)
</script>

<template>
  <div class="qa-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">问答处理</h2>
        <p class="page-sub">点击问题进入聊天框，与农户持续交流</p>
      </div>
    </div>

    <el-radio-group v-model="activeFilter" class="filter-group" style="margin-bottom:20px">
      <el-radio-button value="all">全部</el-radio-button>
      <el-radio-button value="pending">待回复</el-radio-button>
      <el-radio-button value="done">已回复</el-radio-button>
    </el-radio-group>

    <el-card shadow="never" v-loading="loading">
      <div v-for="q in filtered" :key="q.id" class="qa-item" @click="openChat(q)">
        <div class="qa-item-main">
          <div class="qa-head">
            <span class="qa-farmer">
              <el-icon><User /></el-icon>{{ q.questioner }}
              <span class="qa-phone" v-if="q.phone">· {{ q.phone }}</span>
            </span>
            <div class="qa-tags">
              <el-tag size="small" type="info" effect="plain">{{ q.plantName || '未指定' }}</el-tag>
              <el-tag v-if="q.status === 1" size="small" type="success">已回复</el-tag>
              <el-tag v-else size="small" type="warning">待回复</el-tag>
            </div>
          </div>
          <div class="qa-title">{{ q.title }}</div>
          <div class="qa-content">{{ q.question }}</div>
        </div>
        <div class="qa-item-side">
          <el-button size="small" type="primary" plain>进入回复</el-button>
        </div>
      </div>
      <el-empty v-if="!filtered.length" description="暂无问题" />
    </el-card>

    <!-- 持续聊天弹窗 -->
    <el-dialog
      v-model="chatVisible"
      :title="chatQuestion ? ('与农户 ' + (chatQuestion.questioner || '') + ' 的对话') : ''"
      width="600px"
      destroy-on-close
      @open="startPolling"
      @close="onChatClosed"
    >
      <div v-if="chatQuestion" class="chat-topic">主题：{{ chatQuestion.title }}</div>
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="chatMessages.length === 0" style="text-align:center;padding:40px 0;color:var(--text-muted)">
          暂无消息
        </div>
        <div
          v-for="msg in chatMessages"
          :key="msg.id || msg.createTime"
          class="message-row"
          :class="msg.sender === currentUserName ? 'sent' : 'received'"
        >
          <div class="message-sender">{{ msg.senderRole === 'expert' ? '我' : '农户' }}</div>
          <div class="message-bubble">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
      <div class="chat-input-area">
        <el-input v-model="newReply" type="textarea" :rows="3" placeholder="输入回复内容，提交后将通知农户… Ctrl+Enter 发送" @keydown.enter.ctrl="sendReply" />
        <div class="chat-input-actions">
          <el-button size="small" @click="chatVisible = false">关闭</el-button>
          <el-button size="small" type="primary" @click="sendReply">发送</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.qa-page { max-width: 1000px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.filter-group { display: flex; gap: 0; }
.qa-item {
  display: flex; align-items: center; gap: 16px;
  padding: 20px 8px; border-bottom: 1px solid var(--border);
  cursor: pointer; border-radius: var(--radius-sm); transition: background 0.2s;
}
.qa-item:last-child { border-bottom: none; }
.qa-item:hover { background: var(--green-mist); }
.qa-item-main { flex: 1; min-width: 0; }
.qa-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.qa-farmer { display: flex; align-items: center; gap: 6px; font-weight: 500; color: var(--text); }
.qa-phone { color: var(--text-muted); font-weight: normal; }
.qa-tags { display: flex; gap: 6px; }
.qa-title { font-size: 16px; font-weight: 600; color: var(--text); margin-bottom: 6px; }
.qa-content { color: var(--text-secondary); font-size: 14px; line-height: 1.6; }
.qa-item-side { flex-shrink: 0; }

.chat-topic { font-size: 13px; color: var(--text-muted); margin-bottom: 8px; padding-bottom: 8px; border-bottom: 1px solid var(--border); }
.chat-messages { max-height: 400px; overflow-y: auto; padding: 12px 0; }
.message-row { display: flex; flex-direction: column; margin-bottom: 12px; }
.message-row.sent { align-items: flex-end; }
.message-row.received { align-items: flex-start; }
.message-sender { font-size: 11px; color: var(--text-muted); margin-bottom: 2px; }
.message-bubble { max-width: 70%; padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.message-row.sent .message-bubble { background: var(--green); color: #fff; }
.message-row.received .message-bubble { background: var(--bg); color: var(--text); }
.message-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; }
.chat-input-area { margin-top: 12px; }
.chat-input-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
</style>
