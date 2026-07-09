<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const form = ref({ amount: null, item: '' })
const loading = ref(false)
const result = ref(null)

function doMatch() {
  if (!form.value.amount) return ElMessage.warning('请输入融资金额')
  loading.value = true
  setTimeout(() => {
    result.value = {
      partners: [
        { name: '张**', item: '水稻', amount: 50000, credit: 85, matchRate: '96%' },
        { name: '李**', item: '玉米', amount: 30000, credit: 78, matchRate: '92%' },
        { name: '王**', item: '小麦', amount: 80000, credit: 92, matchRate: '88%' }
      ]
    }
    loading.value = false
  }, 1000)
}
</script>

<template>
  <div>
    <h2 class="page-title">智能匹配农户</h2>
    <el-card shadow="never" style="margin-bottom:20px">
      <el-form :model="form" inline>
        <el-form-item label="融资金额(元)">
          <el-input-number v-model="form.amount" :min="10000" :step="10000" :max="10000000" />
        </el-form-item>
        <el-form-item label="种植项目">
          <el-input v-model="form.item" placeholder="如：水稻、玉米" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doMatch" :loading="loading">开始匹配</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-if="result" class="match-result">
      <h3>匹配结果</h3>
      <el-table :data="result.partners" stripe>
        <el-table-column prop="name" label="农户" width="120" />
        <el-table-column prop="item" label="种植项目" width="120" />
        <el-table-column prop="amount" label="需求金额" width="120">
          <template #default="{row}">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="credit" label="信誉评分" width="100" />
        <el-table-column prop="matchRate" label="匹配度" width="100" />
        <el-table-column label="操作" width="120">
          <template #default>
            <el-button size="small" type="primary" @click="ElMessage.success('已发送融资邀请')">邀请融资</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; margin-bottom: 16px; }
.match-result h3 { margin-bottom: 12px; }
</style>
