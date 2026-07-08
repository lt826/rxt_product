<template>
  <div>
    <h2 class="page-title">专家预约</h2>
    <el-card>
      <template #header><span>可预约的专家</span></template>
      <div v-for="expert in experts" :key="expert.id" class="expert-card">
        <div class="expert-info">
          <div class="expert-avatar">{{ expert.avatar }}</div>
          <div>
            <div class="expert-name">{{ expert.name }}</div>
            <div class="expert-field">{{ expert.field }}</div>
          </div>
        </div>
        <el-button type="primary" size="small" @click="bookExpert(expert)">预约</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
const experts = [
  { id: 1, avatar: '👨‍🏫', name: '王教授', field: '水稻种植、土壤改良' },
  { id: 2, avatar: '👩‍🔬', name: '李专家', field: '蔬菜种植、病虫害防治' },
  { id: 3, avatar: '👨‍🌾', name: '张老师', field: '农产品电商、品牌建设' }
]
function bookExpert(e) { ElMessage.success(`已提交预约申请：${e.name}，等待专家确认`) }
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.expert-card { display: flex; align-items: center; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.expert-card:last-child { border: none; }
.expert-info { display: flex; align-items: center; gap: 12px; }
.expert-avatar { font-size: 36px; }
.expert-name { font-weight: 500; color: #303133; }
.expert-field { font-size: 13px; color: #909399; margin-top: 2px; }
</style>
