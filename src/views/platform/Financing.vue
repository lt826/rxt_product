<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { getGoodsListAPI } from '../../api/goods'
import { getKnowledgeListAPI } from '../../api/knowledge'

const router = useRouter()
const userStore = useUserStore()

const loanProducts = ref([
  { bank: '中国农业银行', name: '种植贷', rate: '3.45%', maxAmount: '100,000', term: '12个月', desc: '面向种植农户，用于购买种子、化肥等农资' },
  { bank: '中国工商银行', name: '创业贷', rate: '3.85%', maxAmount: '750,000', term: '36个月', desc: '支持农业创业项目，助力新型农业经营主体发展' },
  { bank: '中国建设银行', name: '设备贷', rate: '3.65%', maxAmount: '150,000', term: '24个月', desc: '用于购买农业机械设备，提高生产效率' },
  { bank: '中国农业银行', name: '发展贷', rate: '3.55%', maxAmount: '500,000', term: '60个月', desc: '支持规模化种植、养殖及农产品加工' },
  { bank: '中国邮政储蓄', name: '惠农贷', rate: '3.75%', maxAmount: '200,000', term: '24个月', desc: '面向广大农户，手续简便，放款快速' },
  { bank: '农村商业银行', name: '助农贷', rate: '3.95%', maxAmount: '300,000', term: '36个月', desc: '支持农户扩大生产经营规模' }
])

const smartMatchForm = ref({ amount: null, item: '', purpose: '' })
const matchLoading = ref(false)
const matchResult = ref(null)

const applyForm = ref({ product: '', amount: null, term: '', remark: '' })

function applyLoan() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push('/profile/my-financing')
}

function doSmartMatch() {
  matchLoading.value = true
  matchResult.value = null
  setTimeout(() => {
    matchResult.value = {
      recommend: '种植贷 - 中国农业银行',
      reason: '根据您的种植项目和融资需求，种植贷产品匹配度最高',
      rate: '3.45%',
      maxAmount: '100,000'
    }
    matchLoading.value = false
  }, 1200)
}
</script>

<template>
  <div class="financing-page">
    <!-- Banner -->
    <div class="page-banner">
      <h1>融资服务</h1>
      <p>为您提供多元化融资解决方案，助力农业发展</p>
    </div>

    <div class="page-body">
      <!-- 贷款产品一览 -->
      <section class="section-card">
        <h2 class="section-title-icon">📋 贷款产品一览</h2>
        <div class="product-grid">
          <div v-for="(p, i) in loanProducts" :key="i" class="product-card">
            <div class="product-header">
              <span class="bank-name">{{ p.bank }}</span>
              <span class="product-name">{{ p.name }}</span>
            </div>
            <div class="product-meta">
              <div class="meta-item"><span class="label">年利率</span><span class="value green">{{ p.rate }}</span></div>
              <div class="meta-item"><span class="label">最高额度</span><span class="value">¥{{ p.maxAmount }}</span></div>
              <div class="meta-item"><span class="label">还款期限</span><span class="value">{{ p.term }}</span></div>
            </div>
            <p class="product-desc">{{ p.desc }}</p>
            <button class="btn-apply" @click="applyLoan()">立即申请</button>
          </div>
        </div>
      </section>

      <!-- 智能匹配 -->
      <section class="section-card">
        <h2 class="section-title-icon">🔍 智能匹配</h2>
        <el-card shadow="never">
          <el-form :model="smartMatchForm" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="融资金额">
                  <el-input-number v-model="smartMatchForm.amount" :min="10000" :step="10000" :max="10000000" style="width:100%" placeholder="输入金额" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="种植项目">
                  <el-input v-model="smartMatchForm.item" placeholder="如：水稻、玉米" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="资金用途">
                  <el-input v-model="smartMatchForm.purpose" placeholder="如：购买农资、扩大种植" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="doSmartMatch" :loading="matchLoading">开始匹配</el-button>
            </el-form-item>
          </el-form>

          <div v-if="matchResult" class="match-result">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <strong>推荐方案：{{ matchResult.recommend }}</strong>
              </template>
              <p>{{ matchResult.reason }}</p>
              <p>年利率 {{ matchResult.rate }} · 最高额度 ¥{{ matchResult.maxAmount }}</p>
            </el-alert>
          </div>
        </el-card>
      </section>

      <!-- 贷款申请 -->
      <section class="section-card">
        <h2 class="section-title-icon">📝 贷款申请</h2>
        <el-card shadow="never">
          <el-empty v-if="!userStore.isLoggedIn" description="请先登录后申请贷款">
            <el-button type="primary" @click="router.push('/login')">去登录</el-button>
          </el-empty>
          <div v-else>
            <el-alert type="info" :closable="false" show-icon>
              <template #title>贷款申请入口</template>
              <p>您可以在个人中心的"我的融资管理"中提交融资申请并查看申请进度。</p>
            </el-alert>
            <div style="margin-top:16px">
              <el-button type="primary" @click="router.push('/profile/my-financing')">前往融资管理</el-button>
            </div>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<style scoped>
.financing-page { padding-bottom: 40px; }
.page-banner { background: linear-gradient(135deg, #2d7a2a, #488d37); color: #fff; padding: 40px 24px; text-align: center; }
.page-banner h1 { font-size: 28px; margin-bottom: 8px; }
.page-banner p { font-size: 16px; opacity: 0.9; }
.page-body { max-width: 1200px; margin: 0 auto; padding: 24px; }
.section-card { margin-bottom: 32px; }
.section-title-icon { font-size: 20px; margin-bottom: 16px; color: #333; }
.product-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
@media (max-width: 900px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .product-grid { grid-template-columns: 1fr; } }
.product-card { background: #fff; border: 1px solid #eee; border-radius: 8px; padding: 20px; transition: box-shadow 0.2s; }
.product-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.product-header { margin-bottom: 12px; }
.bank-name { font-size: 13px; color: #488d37; font-weight: 600; display: block; }
.product-name { font-size: 17px; font-weight: 700; color: #222; }
.product-meta { display: flex; gap: 16px; margin-bottom: 12px; }
.meta-item { flex: 1; }
.meta-item .label { display: block; font-size: 12px; color: #999; }
.meta-item .value { font-size: 16px; font-weight: 700; color: #333; }
.meta-item .value.green { color: #488d37; }
.product-desc { font-size: 13px; color: #888; line-height: 1.5; margin-bottom: 12px; }
.btn-apply { width: 100%; height: 36px; background: #488d37; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; transition: background 0.2s; }
.btn-apply:hover { background: #59a044; }
.match-result { margin-top: 16px; }
</style>
