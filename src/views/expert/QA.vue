<template>
  <div>
    <h2 class="page-title">问答处理</h2>
    <el-card><template #header><span>农户提问列表</span></template><div v-for="q in questions" :key="q.id" class="qa-item"><div class="qa-header"><span class="qa-farmer">{{ q.farmer }}</span><el-tag v-if="q.reply" size="small" type="success">已回复</el-tag><el-tag v-else size="small" type="warning">待回复</el-tag></div><div class="qa-title">{{ q.title }}</div><div class="qa-content">{{ q.content }}</div><div class="qa-time">{{ q.date }}</div><div v-if="q.reply" class="qa-reply"><strong>我的回复：</strong>{{ q.reply }}</div><div v-else><el-input v-model="replyText[q.id]" placeholder="输入回复内容..." style="margin-top:8px" /><el-button size="small" type="primary" style="margin-top:8px" @click="submitReply(q)">回复</el-button></div></div></el-card>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
const replyText = reactive({})
const questions = ref([
  { id:1, farmer:'农户张某', title:'大棚湿度控制', content:'大棚种植黄瓜，湿度一直控制不好，请问有什么办法？', date:'2026-07-08', reply:null },
  { id:2, farmer:'农户李某', title:'有机肥使用方法', content:'有机肥和化肥怎么搭配使用效果最好？', date:'2026-07-07', reply:'建议基肥以有机肥为主，追肥搭配复合肥。一般每亩有机肥2000-3000kg配20-30kg复合肥。' },
  { id:3, farmer:'农户赵某', title:'水稻病虫害', content:'水稻出现稻瘟病迹象，该怎么处理？', date:'2026-07-06', reply:null }
])
function submitReply(q) { const text = replyText[q.id]; if(!text) return; q.reply = text; replyText[q.id] = ''; ElMessage.success('回复成功') }
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
.qa-item { padding:20px 0;border-bottom:1px solid #ebeef5 }
.qa-item:last-child { border:none }
.qa-header { display:flex;align-items:center;gap:8px;margin-bottom:6px }
.qa-farmer { font-weight:500;color:#303133 }
.qa-title { font-size:16px;font-weight:500;color:#303133;margin-bottom:6px }
.qa-content { color:#606266;font-size:14px;margin-bottom:6px;line-height:1.5 }
.qa-time { font-size:12px;color:#c0c4cc;margin-bottom:6px }
.qa-reply { background:#f0f9eb;padding:12px;border-radius:6px;margin-top:8px;color:#67c23a;font-size:14px }
</style>
