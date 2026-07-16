import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUnreadSummaryAPI, markAsReadAPI } from '../api/message'
import { useUserStore } from './user'

// 每个用户独立的消息会话本地存档（后端只提供"未读汇总"，已读会话会被丢弃，
// 故前端按用户名持久化会话元数据，保证已读消息不会"一读就没"）
const lsKey = (u) => `rxt_msg_${u}`

function readMeta(u) {
  try {
    const raw = localStorage.getItem(lsKey(u))
    return raw ? JSON.parse(raw) : {}
  } catch { return {} }
}
function writeMeta(u, obj) {
  try { localStorage.setItem(lsKey(u), JSON.stringify(obj)) } catch { /* ignore */ }
}

export const useMessageStore = defineStore('message', () => {
  const userStore = useUserStore()
  const me = computed(() => userStore.userInfo?.userName || '')

  // 实时未读（来自后端 unread-summary）：sender -> { unreadCount, lastContent, lastTime }
  const unreadMap = ref({})
  // 本地持久化的会话元数据：sender -> { displayName, lastContent, lastTime, hidden }
  const partners = ref({})

  // 按当前登录用户加载本地存档（切换账号/刷新后都能恢复）
  function init() {
    if (!me.value) { partners.value = {}; unreadMap.value = {}; return }
    partners.value = readMeta(me.value)
    unreadMap.value = {}
  }
  function persist() {
    if (!me.value) return
    writeMeta(me.value, partners.value)
  }

  // 总未读数（导航栏徽标用）
  const totalUnread = computed(() =>
    Object.values(unreadMap.value).reduce((s, v) => s + (Number(v?.unreadCount) || 0), 0)
  )

  function unreadOf(sender) {
    return Number(unreadMap.value[sender]?.unreadCount) || 0
  }

  // 合并后的全部会话（排除已隐藏）：未读优先、再按最后时间倒序
  const conversations = computed(() => {
    const senders = new Set([
      ...Object.keys(partners.value),
      ...Object.keys(unreadMap.value)
    ])
    const list = []
    senders.forEach(sender => {
      const p = partners.value[sender] || {}
      if (p.hidden) return
      const u = unreadMap.value[sender] || {}
      const unreadCount = Number(u.unreadCount) || 0
      list.push({
        sender,
        partnerName: p.displayName || sender,
        lastContent: u.lastContent || p.lastContent || '',
        lastTime: u.lastTime || p.lastTime || '',
        unreadCount,
        isRead: unreadCount === 0
      })
    })
    list.sort((a, b) => {
      const au = a.unreadCount > 0 ? 1 : 0
      const bu = b.unreadCount > 0 ? 1 : 0
      if (au !== bu) return bu - au
      return (b.lastTime || '').localeCompare(a.lastTime || '')
    })
    return list
  })

  // 拉取未读汇总，并把预览同步进本地存档（保证标记已读后仍能看到最后一条）
  async function refresh() {
    if (!me.value) return
    try {
      const res = await getUnreadSummaryAPI()
      if (res && res.code === 200 && Array.isArray(res.data)) {
        const map = {}
        res.data.forEach(item => {
          const sender = item.sender
          if (!sender) return
          const unreadCount = Number(item.unreadCount) || 0
          map[sender] = {
            unreadCount,
            lastContent: item.lastContent || '',
            lastTime: item.lastTime || ''
          }
          const prev = partners.value[sender] || {}
          partners.value[sender] = {
            ...prev,
            // 新消息到达则自动取消隐藏（之前被"删除"的会话重新出现）
            hidden: unreadCount > 0 ? false : !!prev.hidden,
            displayName: prev.displayName || sender,
            lastContent: item.lastContent || prev.lastContent || '',
            lastTime: item.lastTime || prev.lastTime || ''
          }
        })
        unreadMap.value = map
        persist()
      }
    } catch { /* 拦截器已提示 */ }
  }

  // 记录/更新某个会话（主动联系对方、或打开历史对话时调用）
  function trackPartner(sender, displayName, lastContent, lastTime) {
    if (!sender) return
    const prev = partners.value[sender] || {}
    partners.value[sender] = {
      ...prev,
      hidden: false,
      displayName: displayName || prev.displayName || sender,
      lastContent: lastContent || prev.lastContent || '',
      lastTime: lastTime || prev.lastTime || ''
    }
    persist()
  }

  // 标记来自某人的消息为已读（后端 + 本地）
  async function markRead(sender) {
    if (!sender) return
    try { await markAsReadAPI(sender) } catch { /* ignore */ }
    if (unreadMap.value[sender]) {
      const next = { ...unreadMap.value }
      delete next[sender]
      unreadMap.value = next
    }
  }

  // 前端隐藏（"删除"）—— 后端暂无删除接口，故为本地隐藏；
  // 当对方再次发来新消息时会自动恢复显示（见 refresh）。
  function hidePartners(senders) {
    let changed = false
    senders.forEach(s => {
      if (!s) return
      const prev = partners.value[s] || {}
      partners.value[s] = { ...prev, hidden: true }
      changed = true
    })
    if (changed) persist()
  }

  // 隐藏全部已读会话
  function hideAllRead() {
    let changed = false
    Object.keys(partners.value).forEach(s => {
      const unread = Number(unreadMap.value[s]?.unreadCount) || 0
      if (unread === 0 && !partners.value[s]?.hidden) {
        partners.value[s] = { ...(partners.value[s] || {}), hidden: true }
        changed = true
      }
    })
    if (changed) persist()
  }

  return {
    me, totalUnread, conversations, unreadMap, partners,
    init, refresh, trackPartner, markRead, unreadOf, hidePartners, hideAllRead
  }
})
