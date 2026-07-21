<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { askQuestionAPI, getMyQuestionsAPI, getQuestionMessagesAPI, replyQuestionAPI } from '../../api/question'
import { listExperts } from '../../api/expert'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const currentUserName = ref(userStore.userInfo?.userName || '')

const questions = ref([])
const experts = ref([])
const loading = ref(false)
const submitting = ref(false)

const form = ref({ expertName: '', title: '', question: '', plantName: '' })

// 聊天弹窗
const chatVisible = ref(false)
const chatQuestion = ref(null)
const chatMessages = ref([])
const newReply = ref('')
const messagesContainer = ref(null)
let pollTimer = null

async function loadExperts() {
  try {
    const res = await listExperts()
    if (res.code === 200) experts.value = res.data || []
  } catch { /* ignore */ }
}

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getMyQuestionsAPI()
    if (res.code === 200) {
      questions.value = (res.data || []).map(q => ({
        id: q.id, title: q.title, question: q.question, answer: q.answer,
        expert: q.expertName || '待分配', expertRealName: '', date: '-', status: q.status,
        plantName: q.plantName
      }))
      if (experts.value.length) {
        const expertMap = Object.fromEntries(experts.value.map(e => [e.userName, e.realName]))
        questions.value.forEach(q => { q.expertRealName = expertMap[q.expert] || q.expert })
      }
    }
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleSubmit() {
  if (!form.value.title) return ElMessage.warning('请填写问题标题')
  if (!form.value.expertName) return ElMessage.warning('请选择要提问的专家')
  const desc = (form.value.question || '').trim()
  if (desc.length < 10) return ElMessage.warning('问题描述不得少于 10 个字，请补充详细内容')
  submitting.value = true
  try {
    const res = await askQuestionAPI({
      title: form.value.title, question: form.value.question,
      expertName: form.value.expertName, plantName: form.value.plantName || undefined
    })
    if (res.code === 200) {
      ElMessage.success('提问成功，等待专家解答')
      form.value = { expertName: '', title: '', question: '', plantName: '' }
      await loadQuestions()
    }
  } catch { /* handled in interceptor */ } finally { submitting.value = false }
}

// 打开与某问题的持续聊天
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
    }
  } catch { ElMessage.error('发送失败') }
}

function formatTime(timeStr) { return timeStr ? String(timeStr).replace('T', ' ').substring(11, 16) : '' }
function formatDate(timeStr) { return timeStr ? String(timeStr).replace('T', ' ').substring(0, 16) : '' }
function scrollToBottom() { if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight }

// 聊天打开时轮询刷新（接收专家回复）
function startPolling() {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (chatVisible.value) await loadChatMessages()
  }, 10000)
}
function stopPolling() { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }

function onChatClosed() { stopPolling() }

onMounted(async () => {
  await loadExperts()
  await loadQuestions()
})
onUnmounted(stopPolling)
</script>

<template>
  <div class="qa-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">专家问答</h2>
        <p class="page-sub">向农业专家提问，可在聊天框中持续交流</p>
      </div>
    </div>

    <el-card shadow="never" class="ask-card">
      <template #header><strong>向专家提问</strong></template>
      <el-form label-width="90px">
        <el-form-item label="选择专家" required>
          <el-select v-model="form.expertName" placeholder="请选择要咨询的专家" style="width:100%">
            <el-option
              v-for="ex in experts"
              :key="ex.userName"
              :label="`${ex.realName || ex.userName}（${ex.profession || '农业技术'}）`"
              :value="ex.userName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作物名称">
          <el-input v-model="form.plantName" placeholder="如：水稻、西红柿等" />
        </el-form-item>
        <el-form-item label="问题标题" required>
          <el-input v-model="form.title" placeholder="简要描述您的问题" />
        </el-form-item>
        <el-form-item label="问题描述" required>
          <el-input v-model="form.question" type="textarea" :rows="4" placeholder="详细描述您的问题（不少于 10 个字）" />
          <div class="qa-desc-tip" :class="{ 'not-enough': (form.question || '').trim().length < 10 }">
            至少 10 个字，当前 {{ (form.question || '').trim().length }} 字
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" size="large" class="submit-btn" @click="handleSubmit">提交问题</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:20px">
      <template #header>
        <div class="card-header-row">
          <strong>我的提问</strong>
          <el-button text size="small" @click="loadQuestions">刷新</el-button>
        </div>
      </template>
      <div v-for="q in questions" :key="q.id" class="qa-item" @click="openChat(q)">
        <div class="qa-item-main">
          <div class="qa-question">{{ q.title }}</div>
          <div class="qa-meta">提问给：{{ q.expertRealName || q.expert }}<span v-if="q.plantName"> · {{ q.plantName }}</span></div>
          <div class="qa-preview">{{ q.question }}</div>
        </div>
        <div class="qa-item-side">
          <el-tag v-if="q.status === 1" size="small" type="success">已回复</el-tag>
          <el-tag v-else size="small" type="warning">待回复</el-tag>
          <el-button size="small" type="primary" plain>查看 / 回复</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && questions.length === 0" description="暂无提问记录" />
    </el-card>

    <!-- 持续聊天弹窗 -->
    <el-dialog
      v-model="chatVisible"
      :title="chatQuestion ? ('与专家 ' + (chatQuestion.expertRealName || chatQuestion.expert) + ' 的对话') : ''"
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
          <div class="message-sender">{{ msg.senderRole === 'expert' ? '专家' : '我' }}</div>
          <div class="message-bubble">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
      <div class="chat-input-area">
        <el-input v-model="newReply" type="textarea" :rows="3" placeholder="输入追问内容，Ctrl+Enter 发送..." @keydown.enter.ctrl="sendReply" />
        <div class="chat-input-actions">
          <el-button size="small" @click="chatVisible = false">关闭</el-button>
          <el-button size="small" type="primary" @click="sendReply">发送</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.qa-page { max-width: 1200px; margin: 0 auto; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.ask-card { border-radius: var(--radius-md); }
.qa-desc-tip { margin-top: 6px; font-size: 12px; color: var(--text-muted); }
.qa-desc-tip.not-enough { color: var(--amber, #e08a1e); }
.submit-btn { width: 100%; border-radius: var(--radius-sm); }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.qa-item {
  display: flex; align-items: center; gap: 16px;
  padding: 16px; border-bottom: 1px solid var(--border);
  cursor: pointer; border-radius: var(--radius-sm); transition: background 0.2s;
}
.qa-item:last-child { border-bottom: none; }
.qa-item:hover { background: var(--green-mist); }
.qa-item-main { flex: 1; min-width: 0; }
.qa-question { font-weight: 600; color: var(--text); margin-bottom: 6px; }
.qa-meta { color: var(--text-muted); font-size: 13px; margin-bottom: 4px; }
.qa-preview { color: var(--text-secondary); font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.qa-item-side { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; flex-shrink: 0; }

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
