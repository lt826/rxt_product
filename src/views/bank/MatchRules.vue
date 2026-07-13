<template>
  <div class="rules-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">智能匹配规则</h2>
        <p class="page-sub">配置融资审批匹配度各因子权重（四项占比之和需等于 100%）</p>
      </div>
    </div>

    <!-- 算法说明 -->
    <el-card shadow="never" class="info-card">
      <template #header><strong>匹配度算法说明</strong></template>
      <p class="info-line">匹配度 = 联合贷款人子分×权重 + 信誉子分×权重 + 金额子分×权重 + 期限子分×权重（各子分均为 0~100，权重之和=100）。</p>
      <ul class="info-list">
        <li><b>联合贷款人</b>：子分 = 人数/2×100（0 人=0、1 人=50、2 人=100）。默认权重 20% → 1 人 +10%、2 人 +20%。</li>
        <li><b>农户信誉分</b>：子分 = 信誉分（取自用户表 credit，0~100）。</li>
        <li><b>申请金额</b>：金额越低子分越高（归一化上限 50 万）。</li>
        <li><b>期限</b>：期限越短子分越高（6~36 个月线性递减）。</li>
      </ul>
    </el-card>

    <!-- 权重配置 -->
    <el-card shadow="never" v-loading="loading" style="margin-top:20px">
      <template #header><strong>权重配置（占比）</strong></template>
      <el-form label-width="140px" class="config-form">
        <el-form-item label="联合贷款人">
          <el-input-number v-model="form.partnerWeight" :min="0" :max="100" :step="5" :precision="0" controls-position="right" />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="农户信誉分">
          <el-input-number v-model="form.creditWeight" :min="0" :max="100" :step="5" :precision="0" controls-position="right" />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="申请金额">
          <el-input-number v-model="form.amountWeight" :min="0" :max="100" :step="5" :precision="0" controls-position="right" />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="期限">
          <el-input-number v-model="form.termWeight" :min="0" :max="100" :step="5" :precision="0" controls-position="right" />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="合计">
          <el-tag :type="sum === 100 ? 'success' : 'danger'" size="large">{{ sum }}%</el-tag>
          <span v-if="sum !== 100" class="warn-tip">
            四项权重之和需等于 100，当前{{ sum > 100 ? '超出' : '还差' }} {{ Math.abs(sum - 100) }}%
          </span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :disabled="sum !== 100" :loading="saving" @click="save">保存规则</el-button>
          <el-button size="large" @click="loadRule">重新加载</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMatchRuleAPI, updateMatchRuleAPI } from '../../api/finance'

const loading = ref(false)
const saving = ref(false)

const defaultForm = () => ({ partnerWeight: 20, creditWeight: 30, amountWeight: 25, termWeight: 25 })
const form = reactive(defaultForm())

const sum = computed(() =>
  Number(form.partnerWeight || 0) +
  Number(form.creditWeight || 0) +
  Number(form.amountWeight || 0) +
  Number(form.termWeight || 0)
)

async function loadRule() {
  loading.value = true
  try {
    const res = await getMatchRuleAPI()
    if (res.code === 200 && res.data) {
      form.partnerWeight = Number(res.data.partnerWeight)
      form.creditWeight = Number(res.data.creditWeight)
      form.amountWeight = Number(res.data.amountWeight)
      form.termWeight = Number(res.data.termWeight)
    }
  } catch (e) {
    /* 错误已由 axios 拦截器统一提示 */
  } finally {
    loading.value = false
  }
}

async function save() {
  if (sum.value !== 100) {
    ElMessage.warning('四项权重之和需等于 100')
    return
  }
  saving.value = true
  try {
    const res = await updateMatchRuleAPI({
      partnerWeight: form.partnerWeight,
      creditWeight: form.creditWeight,
      amountWeight: form.amountWeight,
      termWeight: form.termWeight
    })
    if (res.code === 200) {
      ElMessage.success('规则已保存，审批页匹配度将按新权重计算')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    /* 错误已由 axios 拦截器统一提示 */
  } finally {
    saving.value = false
  }
}

onMounted(loadRule)
</script>

<style scoped>
.rules-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.info-card .info-line { margin: 0 0 8px; color: var(--text-secondary); font-size: 14px; }
.info-list { margin: 0; padding-left: 20px; color: var(--text-secondary); font-size: 13px; line-height: 1.9; }
.config-form .el-form-item { margin-bottom: 22px; }
.unit { margin-left: 8px; color: var(--text-muted); font-size: 14px; }
.warn-tip { margin-left: 12px; color: var(--el-color-danger, #f56c6c); font-size: 13px; }
</style>
