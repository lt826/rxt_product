<template>
  <div>
    <h2 class="page-title">专家问答</h2>
    <el-card style="margin-bottom:20px">
      <template #header><span>向专家提问</span></template>
      <el-form>
        <el-form-item label="选择专家"><el-select placeholder="请选择专家" style="width:100%"><el-option label="王教授 - 水稻种植" value="1" /><el-option label="李专家 - 蔬菜种植" value="2" /></el-select></el-form-item>
        <el-form-item label="问题标题"><el-input placeholder="简要描述您的问题" /></el-form-item>
        <el-form-item label="问题描述"><el-input type="textarea" :rows="4" placeholder="详细描述您的问题" /></el-form-item>
        <el-form-item><el-button type="primary">提交问题</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header><span>我的提问</span></template>
      <div v-for="q in questions" :key="q.id" class="qa-item">
        <div class="qa-question">{{ q.title }}</div>
        <div class="qa-answer" v-if="q.answer">答：{{ q.answer }}</div>
        <el-tag v-else size="small" type="info">待回复</el-tag>
        <div class="qa-meta">{{ q.date }} · {{ q.expert }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
const questions = [
  { id: 1, title: '水稻秧苗发黄怎么办？', answer: '可能是缺氮或缺铁，建议追施尿素或叶面喷施硫酸亚铁', expert: '王教授', date: '2026-07-06' },
  { id: 2, title: '大棚西红柿裂果原因', answer: null, expert: '李专家', date: '2026-07-08' }
]
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.qa-item { padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.qa-item:last-child { border: none; }
.qa-question { font-weight: 500; color: #303133; margin-bottom: 6px; }
.qa-answer { color: #67c23a; font-size: 14px; margin-bottom: 6px; }
.qa-meta { color: #909399; font-size: 13px; margin-top: 4px; }
</style>
