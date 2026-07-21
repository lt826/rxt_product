<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsListAPI } from '../../api/goods'
import { getKnowledgeListAPI } from '../../api/knowledge'
import { getExpertListAPI } from '../../api/expert'
import { getHomeFeaturedAPI } from '../../api/homeFeature'
import { getCarouselListAPI } from '../../api/carousel'
import LogoMark from '../../components/LogoMark.vue'
import RoleGlyph from '../../components/RoleGlyph.vue'

const router = useRouter()

const experts = ref([])
const shopItems = ref([])
const knowledgeItems = ref([])
const loading = ref(true)

const roles = [
  { id: 'farmer', label: '农户端', desc: '融资申请 · 商品管理 · 专家咨询', color: '#2d6a4f' },
  { id: 'buyer', label: '买家端', desc: '浏览商品 · 发布需求 · 直连农户', color: '#409160' },
  { id: 'bank', label: '银行端', desc: '融资审批 · 智能匹配 · 风控管理', color: '#1a5e8a' },
  { id: 'expert', label: '专家端', desc: '知识发布 · 问答处理 · 预约管理', color: '#5d4037' }
]

const financeProducts = ref([
  { bank: '中国农业银行', name: '种植贷', money: '100,000', desc: '最高可申请额度', icon: 'Coin' },
  { bank: '中国工商银行', name: '创业贷', money: '750,000', desc: '最高可申请额度', icon: 'Money' },
  { bank: '中国建设银行', name: '设备贷', money: '150,000', desc: '最高可申请额度', icon: 'Wallet' },
  { bank: '中国农业银行', name: '发展贷', money: '500,000', desc: '最高可申请额度', icon: 'CreditCard' }
])

const qaItems = ref([
  { q: '草莓杂草如何防治？', a: '1）人工除草，在开花前进行；2）地膜覆盖除草；3）合理轮作，减少杂草基数。', expert: '张娟' },
  { q: '土豆应该怎么施肥？', a: '基肥占总施肥量的三分之二，以有机肥为基础配合磷钾肥。出苗后立即追施提苗肥。', expert: '张娟' },
  { q: '阳光玫瑰温度控制在多少度？', a: '大棚栽培时，温度控制在26-30℃，避免长时间超过35℃。夜间保持15-18℃。', expert: '高燕' }
])

const knowledgeItemsStatic = [
  { title: '水稻种植的害虫防治', content: '采取"预防为主，综合防治"策略。主要虫害包括稻飞虱、稻纵卷叶螟、二化螟等。通过农业防治、生物防治、物理防治、化学防治综合运用，有效控制虫害危害。' },
  { title: '玉米种植过程详解', content: '选种与整地：选择适合当地气候的优质品种。播种前深耕土壤25-30厘米，结合施用腐熟农家肥配复合肥。采用机械化精量播种，控制行距60厘米。' },
  { title: '蔬菜大棚温湿度管理', content: '不同季节温室大棚温湿度管理要点不同。春季注意保温防霜冻，夏季加强通风降温，秋季控水增光，冬季做好防寒保暖工作。' }
]

const shopFallback = [
  { name: '红薯淀粉·农家自制', price: '¥223.00' },
  { name: '新疆伊犁小红杏·4斤装', price: '¥43.00' },
  { name: '高山甜香蕉·当季新鲜', price: '¥23.00' },
  { name: '山东大葱·时令蔬菜5斤', price: '¥34.00' },
  { name: '大别山野生羊肚菌', price: '¥345.00' }
]

const expertsFallback = [
  { name: '冯湛', specialty: '水稻', org: '山东农科院', title: '中级工程师' },
  { name: '姜学军', specialty: '植物学', org: '山东农科院', title: '高级工程师' },
  { name: '李家爱', specialty: '农艺', org: '农科院', title: '中级工程师' },
  { name: '刘辉', specialty: '农作物栽培', org: '农业资源研究所', title: '高级工程师' },
  { name: '李依敏', specialty: '农学', org: '农村经济合作站', title: '高级工程师' }
]

// 首页轮播图（管理员上传，展示在平台入口上方，无图则隐藏）
const carouselImages = ref([])

function mono(text) {
  const t = String(text || '').trim()
  return t ? t.charAt(0) : '农'
}

onMounted(async () => {
  loading.value = true
  try {
    const [goodsRes, knowRes, expertRes, featuredRes, carouselRes] = await Promise.allSettled([
      getGoodsListAPI({ type: '1' }),
      getKnowledgeListAPI(),
      getExpertListAPI(),
      getHomeFeaturedAPI(),
      getCarouselListAPI()
    ])

    // 首页轮播图（管理员上传）
    if (carouselRes.status === 'fulfilled' && carouselRes.value?.code === 200) {
      carouselImages.value = carouselRes.value.data || []
    }

    // 管理员精选内容（未配置或拉取失败则为空，各板块回退到默认内容）
    const featured = featuredRes.status === 'fulfilled' && featuredRes.value?.code === 200
      ? (featuredRes.value.data || {}) : {}
    const fFinance = featured.finance || []
    const fExpert = featured.expert || []
    const fAnswer = featured.answer || []
    const fKnowledge = featured.knowledge || []
    const fGoods = featured.goods || []

    // 专家用户名 -> 真实姓名（用于问答卡片展示专家名）
    const expertApiList = expertRes.status === 'fulfilled' && expertRes.value?.code === 200
      ? (expertRes.value.data || []) : []
    const expertSource = fExpert.length ? fExpert : expertApiList
    const expertNameMap = {}
    expertSource.forEach(e => { if (e.userName) expertNameMap[e.userName] = e.realName || e.userName })

    // 金融产品：有精选则覆盖，否则保持静态
    if (fFinance.length) {
      financeProducts.value = fFinance.slice(0, 4).map((p, i) => ({
        bank: p.bankName || '金融服务',
        name: p.name || '金融产品',
        money: p.maxQuota != null ? Number(p.maxQuota).toLocaleString() : '0',
        desc: p.rate != null ? `年利率 ${p.rate}%` : '最高可申请额度',
        icon: ['Coin', 'Money', 'Wallet', 'CreditCard'][i % 4]
      }))
    }

    // 专家：有精选则覆盖，否则走原接口，再否则静态兜底
    if (fExpert.length) {
      experts.value = fExpert.slice(0, 5).map(e => ({
        name: e.realName || e.userName, specialty: e.profession || '农学', org: e.belong || '农科院', title: e.position || '专家'
      }))
    } else if (expertApiList.length > 0) {
      experts.value = expertApiList.slice(0, 5).map(e => ({
        name: e.realName || e.userName, specialty: e.specialty || '农学', org: e.org || '农科院', title: e.title || '专家'
      }))
    } else {
      experts.value = expertsFallback
    }

    // 热门问答：有精选则覆盖，否则保持静态
    if (fAnswer.length) {
      qaItems.value = fAnswer.slice(0, 3).map(q => ({
        q: q.title || (q.question ? String(q.question).slice(0, 40) : '农业问答'),
        a: q.answer || '',
        expert: expertNameMap[q.expertName] || q.expertName || '专家'
      }))
    }

    // 农业知识：有精选则覆盖，否则走原接口，再否则静态兜底
    if (fKnowledge.length) {
      knowledgeItems.value = fKnowledge.slice(0, 3).map(k => ({ id: k.knowledgeId, title: k.title, content: k.content }))
    } else if (knowRes.status === 'fulfilled' && knowRes.value?.code === 200 && knowRes.value.data?.length > 0) {
      knowledgeItems.value = knowRes.value.data.slice(0, 3).map((k) => ({
        id: k.knowledgeId, title: k.title, content: k.content
      }))
    } else {
      knowledgeItems.value = knowledgeItemsStatic
    }

    // 助农电商：有精选则覆盖，否则走原接口，再否则静态兜底
    if (fGoods.length) {
      shopItems.value = fGoods.slice(0, 5).map(g => ({ id: g.goodsId, name: g.title, price: g.price ? `¥${g.price}` : '面议' }))
    } else if (goodsRes.status === 'fulfilled' && goodsRes.value?.code === 200 && goodsRes.value.data?.length > 0) {
      shopItems.value = goodsRes.value.data.slice(0, 5).map((g) => ({
        id: g.goodsId, name: g.title, price: g.price ? `¥${g.price}` : '面议'
      }))
    } else {
      shopItems.value = shopFallback
    }
  } catch {
    shopItems.value = shopFallback
    knowledgeItems.value = knowledgeItemsStatic
    experts.value = expertsFallback
  } finally {
    loading.value = false
  }
})

function enterRole(roleId) {
  router.push({ path: '/login', query: { role: roleId } })
}
</script>

<template>
  <div class="home-page">
    <!-- ===== Hero ===== -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-wave"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge"><LogoMark :size="15" /> 数字经济 · 智慧农业</div>
        <h1>农产品融销一体平台</h1>
        <p class="hero-sub">融通资金 · 链接产销 · 智汇农业</p>
        <p class="hero-desc">为农户、买家、银行、专家打造的一站式协同服务平台</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="hero-btn" @click="router.push('/register')">
            免费注册
          </el-button>
          <el-button size="large" class="hero-btn-secondary" @click="router.push('/login')">
            已有账号，去登录
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="stat-number">多角色</span>
            <span class="stat-text">农户 · 买家 · 银行 · 专家协同</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-number">一站式</span>
            <span class="stat-text">融资 · 产销 · 知识全流程</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-number">可追溯</span>
            <span class="stat-text">发布 · 审批 · 交易 · 咨询</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 首页轮播图（管理员上传，每张 5 秒） ===== -->
    <section v-if="carouselImages.length" class="home-banner">
      <el-carousel height="340px" :interval="5000" arrow="hover">
        <el-carousel-item v-for="(img, i) in carouselImages" :key="img.id || i">
          <a v-if="img.link" :href="img.link" target="_blank" rel="noopener" class="banner-slide">
            <img :src="img.imageUrl" :alt="'轮播图 ' + (i + 1)" class="banner-img" />
          </a>
          <div v-else class="banner-slide">
            <img :src="img.imageUrl" :alt="'轮播图 ' + (i + 1)" class="banner-img" />
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- ===== 平台入口 ===== -->
    <section class="section section-roles">
      <div class="section-head">
        <span class="section-eyebrow">PLATFORM PORTAL</span>
        <h2>平台入口</h2>
      </div>
      <div class="roles-grid">
        <div v-for="role in roles" :key="role.id" class="role-card" @click="enterRole(role.id)">
          <div class="role-card-icon" :style="{ background: role.color + '18', color: role.color }">
            <RoleGlyph :role="role.id" :size="32" />
          </div>
          <h3>{{ role.label }}</h3>
          <p>{{ role.desc }}</p>
          <span class="role-arrow">进入 →</span>
        </div>
      </div>
    </section>

    <!-- ===== 金融产品 ===== -->
    <section class="section section-finance" style="background: var(--bg)">
      <div class="section-head">
        <span class="section-eyebrow">FINANCIAL PRODUCTS</span>
        <h2>金融产品</h2>
      </div>
      <div class="finance-grid">
        <div v-for="(item, i) in financeProducts" :key="i" class="finance-card">
          <div class="finance-icon"><el-icon :size="34"><component :is="item.icon" /></el-icon></div>
          <div class="finance-bank">{{ item.bank }}</div>
          <h3>{{ item.name }}</h3>
          <div class="finance-amount">¥{{ item.money }}</div>
          <p>{{ item.desc }}</p>
          <el-button size="default" class="finance-btn" @click="router.push('/login')">立即申请</el-button>
        </div>
      </div>
    </section>

    <!-- ===== 专家一览 ===== -->
    <section class="section section-experts">
      <div class="section-head">
        <span class="section-eyebrow">EXPERT TEAM</span>
        <h2>农业专家团队</h2>
      </div>
      <div class="experts-grid">
        <div v-for="(ex, i) in experts" :key="i" class="expert-card">
          <div class="expert-avatar" :style="{ background: ['#e8f5e9','#e3f2fd','#fff3e0','#f3e5f5','#e0f2f1'][i % 5], color: ['#2e7d32','#1565c0','#ef6c00','#6a1b9a','#00695c'][i % 5] }">
            {{ mono(ex.name) }}
          </div>
          <div class="expert-detail">
            <h4>{{ ex.name }}</h4>
            <p><span class="detail-label">专长</span> {{ ex.specialty }}</p>
            <p><span class="detail-label">单位</span> {{ ex.org }}</p>
            <p><span class="detail-label">职称</span> {{ ex.title }}</p>
          </div>
          <el-button text type="primary" class="expert-action" @click="router.push('/login')">在线咨询</el-button>
        </div>
      </div>
    </section>

    <!-- ===== 专家问答 ===== -->
    <section class="section section-qa" style="background: var(--bg)">
      <div class="section-head">
        <span class="section-eyebrow">Q&amp;A HIGHLIGHTS</span>
        <h2>热门问答</h2>
      </div>
      <div class="qa-grid">
        <div v-for="(item, i) in qaItems" :key="i" class="qa-card">
          <div class="qa-q">
            <span class="qa-q-icon">Q</span>
            <span>{{ item.q }}</span>
          </div>
          <div class="qa-a">
            <span class="qa-a-label">专家 {{ item.expert }} 回答：</span>
            <p>{{ item.a }}</p>
          </div>
        </div>
      </div>
      <div class="section-more">
        <el-button text type="primary" size="large" @click="router.push('/login')">查看全部问答 →</el-button>
      </div>
    </section>

    <!-- ===== 农业知识 ===== -->
    <section class="section section-knowledge">
      <div class="section-head">
        <span class="section-eyebrow">KNOWLEDGE BASE</span>
        <h2>农业知识</h2>
      </div>
      <div class="knowledge-grid">
        <div v-for="(item, i) in knowledgeItems" :key="i" class="knowledge-card">
          <div class="knowledge-card-icon"><el-icon :size="40"><Reading /></el-icon></div>
          <div class="knowledge-card-body">
            <h3>{{ item.title }}</h3>
            <p>{{ item.content ? (item.content.length > 100 ? item.content.substring(0, 100) + '…' : item.content) : '' }}</p>
            <el-button text type="primary" size="small" @click="router.push('/login')">阅读更多 →</el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 助农电商 ===== -->
    <section class="section section-shop" style="background: var(--bg)">
      <div class="section-head">
        <span class="section-eyebrow">AGRICULTURAL E-COMMERCE</span>
        <h2>助农电商</h2>
      </div>
      <div class="shop-grid">
        <div v-for="(item, i) in shopItems" :key="i" class="shop-card" @click="item.id && router.push('/buyer/goods/' + item.id)">
          <div class="shop-card-icon">{{ mono(item.name) }}</div>
          <div class="shop-card-body">
            <div class="shop-card-name">{{ item.name }}</div>
            <div class="shop-card-price">{{ item.price }}</div>
          </div>
        </div>
      </div>
      <div class="section-more">
        <el-button type="primary" size="large" @click="router.push('/buyer/products')">进入农产品交易 →</el-button>
      </div>
    </section>

    <!-- ===== Footer ===== -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="footer-icon" style="color:#fff"><LogoMark :size="22" /></span>
          <span class="footer-name">融销通</span>
        </div>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">联系方式</a>
          <a href="#">服务条款</a>
          <a href="#">隐私政策</a>
        </div>
        <p class="footer-copy">农产品融销一体平台 &copy; 2026</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.home-page {
  font-family: "PingFang SC", "Microsoft YaHei", "Noto Sans SC", sans-serif;
  background: #fff;
}

/* ===== 首页轮播图 ===== */
.home-banner {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 0;
  box-sizing: border-box;
}
.home-banner :deep(.el-carousel__container) {
  border-radius: var(--radius-md);
  overflow: hidden;
}
.home-banner :deep(.el-carousel__indicators) {
  bottom: 16px;
}
.home-banner :deep(.el-carousel__indicator--horizontal button) {
  background: rgba(255, 255, 255, 0.6);
}
.home-banner :deep(.el-carousel__indicator--horizontal.is-active button) {
  background: #fff;
}
.banner-slide {
  display: block;
  width: 100%;
  height: 100%;
}
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
@media (max-width: 700px) {
  .home-banner :deep(.el-carousel__container) { height: 200px !important; }
}

/* ===== Hero ===== */
.hero {
  position: relative;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(160deg, #1b4332 0%, #2d6a4f 30%, #409160 60%, #52b788 100%);
}
.hero-bg { position: absolute; inset: 0; }
.hero-wave {
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 120px;
  background: #fff;
  clip-path: ellipse(55% 100% at 50% 100%);
}
.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
  padding: 80px 24px 140px;
  max-width: 760px;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(10px);
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.08em;
  margin-bottom: 24px;
  border: 1px solid rgba(255,255,255,0.2);
}
.hero-content h1 {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: 0.04em;
  line-height: 1.2;
  text-shadow: 0 2px 12px rgba(0,0,0,0.15);
}
.hero-sub {
  font-size: 20px;
  margin: 0 0 8px;
  opacity: 0.95;
  font-weight: 500;
  letter-spacing: 0.08em;
}
.hero-desc {
  font-size: 15px;
  margin: 0 0 36px;
  opacity: 0.75;
  line-height: 1.6;
}
.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 48px;
}
.hero-btn {
  height: 50px;
  padding: 0 36px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.4) !important;
  backdrop-filter: blur(10px);
  color: #fff !important;
  transition: all 0.2s;
}
.hero-btn:hover {
  background: rgba(255,255,255,0.3) !important;
  transform: translateY(-1px);
}
.hero-btn-secondary {
  height: 50px;
  padding: 0 36px;
  font-size: 16px;
  font-weight: 500;
  border-radius: var(--radius-md);
  border: 1px solid rgba(255,255,255,0.3) !important;
  color: #fff !important;
  background: transparent !important;
  transition: all 0.2s;
}
.hero-btn-secondary:hover {
  border-color: rgba(255,255,255,0.6) !important;
  background: rgba(255,255,255,0.1) !important;
}
.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28px;
}
.hero-stat { text-align: center; }
.stat-number {
  display: block;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0.02em;
}
.stat-text {
  display: block;
  font-size: 12px;
  opacity: 0.75;
  margin-top: 6px;
}
.hero-stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255,255,255,0.2);
}

/* ===== Sections ===== */
.section {
  padding: 64px 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.section-head {
  text-align: center;
  margin-bottom: 48px;
}
.section-eyebrow {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: var(--green-light);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-bottom: 8px;
}
.section-head h2 {
  font-size: 30px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0;
  letter-spacing: -0.02em;
}
.section-more { text-align: center; margin-top: 36px; }

/* ===== Roles Grid ===== */
.roles-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
@media (max-width: 900px) { .roles-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 500px) { .roles-grid { grid-template-columns: 1fr; } }
.role-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 32px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s;
}
.role-card:hover {
  border-color: var(--green-light);
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
}
.role-card-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
.role-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0 0 8px;
}
.role-card p {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
  margin: 0 0 14px;
}
.role-arrow {
  font-size: 13px;
  color: var(--green);
  font-weight: 600;
}

/* ===== Finance ===== */
.finance-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
@media (max-width: 900px) { .finance-grid { grid-template-columns: repeat(2, 1fr); } }
.finance-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  padding: 28px 20px;
  text-align: center;
  border: 1px solid var(--border);
  transition: all 0.25s;
}
.finance-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.finance-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--green-mist);
  color: var(--green);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
}
.finance-bank {
  font-size: 13px;
  font-weight: 600;
  color: var(--green);
  margin-bottom: 8px;
}
.finance-card h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0 0 6px;
}
.finance-amount {
  font-size: 26px;
  font-weight: 800;
  color: var(--green);
  margin: 8px 0;
}
.finance-card > p {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 16px;
}
.finance-btn {
  width: 100%;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

/* ===== Experts ===== */
.experts-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
@media (max-width: 900px) { .experts-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 500px) { .experts-grid { grid-template-columns: 1fr; } }
.expert-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  transition: all 0.25s;
}
.expert-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.expert-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
}
.expert-detail {
  text-align: center;
  margin-bottom: 16px;
  width: 100%;
}
.expert-detail h4 {
  font-size: 16px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0 0 8px;
}
.expert-detail p {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 4px;
  line-height: 1.5;
}
.detail-label {
  color: var(--text-muted);
  margin-right: 4px;
}
.expert-action { font-weight: 600; }

/* ===== QA ===== */
.qa-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
@media (max-width: 800px) { .qa-grid { grid-template-columns: 1fr; } }
.qa-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  padding: 24px;
}
.qa-q {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
}
.qa-q-icon {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: var(--green-mist);
  color: var(--green);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
}
.qa-q span {
  font-size: 15px;
  font-weight: 600;
  color: var(--green-deep);
  line-height: 1.4;
}
.qa-a {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
}
.qa-a-label {
  display: block;
  font-weight: 600;
  color: var(--green);
  margin-bottom: 6px;
  font-size: 13px;
}

/* ===== Knowledge ===== */
.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
@media (max-width: 800px) { .knowledge-grid { grid-template-columns: 1fr; } }
.knowledge-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  display: flex;
  transition: all 0.25s;
}
.knowledge-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.knowledge-card-icon {
  width: 100px;
  min-height: 140px;
  background: var(--green-mist);
  color: var(--green);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.knowledge-card-body {
  flex: 1;
  padding: 20px;
}
.knowledge-card-body h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--green-deep);
  margin: 0 0 8px;
}
.knowledge-card-body p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 12px;
}

/* ===== Shop ===== */
.shop-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
@media (max-width: 900px) { .shop-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 500px) { .shop-grid { grid-template-columns: repeat(2, 1fr); } }
.shop-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
}
.shop-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.shop-card-icon {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  font-weight: 700;
  color: var(--green-deep);
  background: linear-gradient(135deg, var(--green-mist), #eef7f0);
}
.shop-card-body { padding: 12px 14px; }
.shop-card-name {
  font-size: 14px;
  color: var(--text);
  font-weight: 500;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}
.shop-card-price {
  color: var(--amber);
  font-weight: 700;
  font-size: 18px;
}

/* ===== Footer ===== */
.site-footer {
  background: var(--green-deep);
  color: rgba(255,255,255,0.8);
  padding: 40px 24px;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}
.footer-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
}
.footer-icon { display: flex; align-items: center; }
.footer-name {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.08em;
}
.footer-links {
  display: flex;
  justify-content: center;
  gap: 28px;
  margin-bottom: 16px;
}
.footer-links a {
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}
.footer-links a:hover { color: #fff; }
.footer-copy {
  font-size: 13px;
  opacity: 0.5;
}
</style>
