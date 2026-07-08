<template>
  <div>
    <h2 class="page-title">消息通知</h2>
    <el-card>
      <div v-for="msg in messages" :key="msg.id" class="msg-item" :class="{ unread: !msg.read }">
        <div class="msg-icon">{{ msg.icon }}</div>
        <div class="msg-body">
          <div class="msg-title">{{ msg.title }}</div>
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-time">{{ msg.time }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
const messages = [
  { id: 1, icon: '💰', title: '融资进度更新', content: '您的融资申请已提交至银行审核', time: '2026-07-08 10:30', read: false },
  { id: 2, icon: '💬', title: '专家回复了您的问题', content: '王教授回复了您关于"水稻秧苗发黄"的问题', time: '2026-07-08 09:15', read: false },
  { id: 3, icon: '📦', title: '订单发货通知', content: '您购买的商品已发货，预计3天内到达', time: '2026-07-07 14:00', read: true },
  { id: 4, icon: '✅', title: '预约确认', content: '李专家已接受您的预约申请', time: '2026-07-06 16:20', read: true }
]
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.msg-item { display: flex; gap: 12px; padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.msg-item:last-child { border: none; }
.msg-item.unread { background: #ecf5ff; margin: 0 -16px; padding: 16px; border-radius: 8px; }
.msg-icon { font-size: 24px; }
.msg-title { font-weight: 500; color: #303133; }
.msg-content { font-size: 14px; color: #606266; margin-top: 4px; }
.msg-time { font-size: 12px; color: #c0c4cc; margin-top: 6px; }
</style>
