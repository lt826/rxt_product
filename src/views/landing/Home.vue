<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const roles = [
  { id: 'farmer', label: '农户端', icon: '👨‍🌾', desc: '融资申请·商品购买·农业知识·专家咨询' },
  { id: 'buyer', label: '买家端', icon: '📦', desc: '商品管理·收购需求发布' },
  { id: 'bank', label: '银行端', icon: '🏦', desc: '融资审批·产品配置·风控匹配' },
  { id: 'expert', label: '专家端', icon: '👨‍🔬', desc: '知识发布·问答处理·预约管理' },
  { id: 'admin', label: '平台管理端', icon: '⚙️', desc: '用户管理·系统配置·数据统计' }
]

function enterRole(roleId) {
  router.push({ path: '/login', query: { role: roleId } })
}

// 专家列表（静态演示数据）
const experts = [
  { name: '冯湛', specialty: '水稻', org: '山东农科院', phone: '13375645577', title: '中级工程师' },
  { name: '姜学军', specialty: '植物学', org: '山东农科院', phone: '15644959257', title: '高级工程师' },
  { name: '李家爱', specialty: '农艺', org: '农科院', phone: '15951278054', title: '中级工程师' },
  { name: '刘辉', specialty: '农作物栽培', org: '农业资源研究所', phone: '15526315266', title: '高级工程师' },
  { name: '李依敏', specialty: '农学', org: '农村经济合作站', phone: '13340274488', title: '高级工程师' }
]

// 精选商品（静态演示）
const shopItems = [
  { name: '地瓜粉红薯淀粉农家自制', price: '¥223.00', icon: '🥔' },
  { name: '新疆伊犁小红杏 净重4斤', price: '¥43.00', icon: '🍑' },
  { name: '高山甜香蕉 当季新鲜10斤', price: '¥23.00', icon: '🍌' },
  { name: '山东大葱5斤时令蔬菜', price: '¥34.00', icon: '🥬' },
  { name: '大别山野生羊肚菌干货', price: '¥345.00', icon: '🍄' }
]

// 金融产品
const financeProducts = [
  { bank: '中国农业银行', name: '种植贷', money: '100000', desc: '最高可申请额度' },
  { bank: '中国工商银行', name: '创业贷', money: '750000', desc: '最高可申请额度' },
  { bank: '中国建设银行', name: '设备贷', money: '150000', desc: '最高可申请额度' },
  { bank: '中国农业银行', name: '发展贷', money: '500000', desc: '最高可申请额度' }
]

// QA 示例
const qaItems = [
  {
    q: '草莓杂草如何防治？',
    a: '草莓植株低矮，种植密度大，除草困难。防治措施：1）人工除草，在开花前进行；2）地膜覆盖除草，使用黑色地膜效果很好；3）合理轮作，减少杂草基数。',
    expert: '张娟'
  },
  {
    q: '土豆应该怎么施肥？',
    a: '土豆基肥占总施肥量的三分之二，以有机肥为基础配合磷钾肥。基肥应埋于10厘米以下土层。出苗后应立即追施提苗肥，以氮肥为主，促进幼苗快速生长。',
    expert: '张娟'
  },
  {
    q: '阳光玫瑰温度控制在多少度？',
    a: '大棚栽培时，温度控制在26-30℃，避免长时间超过35℃的高温，以免影响花芽分化。夜间温度保持在15-18℃为宜。',
    expert: '高燕'
  }
]

// 知识文章
const knowledgeItems = [
  {
    title: '水稻种植的害虫防治',
    content: '水稻种植过程中，害虫防治需采取"预防为主，综合防治"的策略。主要虫害包括稻飞虱、稻纵卷叶螟、二化螟等。防治方法包括：农业防治选用抗虫品种；生物防治释放赤眼蜂等天敌；物理防治使用诱虫灯；化学防治在发生初期选用高效低毒农药。通过综合运用这些措施，可有效控制虫害危害，保障水稻产量和品质。'
  },
  {
    title: '玉米种植过程详解',
    content: '玉米种植是一个系统的农事过程。选种与整地阶段：选择适合当地气候的优质品种，北方宜选耐寒早熟型，南方需耐热品种。播种前深耕土壤25-30厘米，结合施用腐熟农家肥配复合肥。采用机械化精量播种，控制行距60厘米、株距25-30厘米，播种深度3-5厘米。'
  }
]
</script>

<template>
  <div class="home-page">
    <!-- ===== 顶部导航 ===== -->
    <header class="top-nav">
      <div class="nav-inner">
        <div class="nav-logo" @click="router.push('/')">
          <span class="logo-icon">🌾</span>
          <span class="logo-text">融销通</span>
        </div>
        <div class="nav-menu">
          <a href="#roles">平台入口</a>
          <a href="#finance">金融产品</a>
          <a href="#expert">专家介绍</a>
          <a href="#knowledge">农业知识</a>
          <a href="#shop">助农电商</a>
        </div>
        <div class="nav-actions">
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </div>
      </div>
    </header>

    <!-- ===== Banner/Hero ===== -->
    <section class="banner">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <h1>基于数字经济的农产品融销一体平台</h1>
        <p class="banner-tagline">融通资金 · 链接产销 · 智汇农业</p>
        <p class="banner-sub">农户、买家、银行、专家一站式协同平台</p>
        <div class="banner-actions">
          <el-button type="primary" size="large" @click="router.push('/register')">立即注册</el-button>
          <el-button size="large" @click="router.push('/login')" style="background:rgba(255,255,255,0.2);color:#fff;border:1px solid rgba(255,255,255,0.4)">已有账号，去登录</el-button>
        </div>
      </div>
    </section>

    <!-- ===== 角色入口 ===== -->
    <section id="roles" class="section-wrap">
      <div class="section-title">
        <h2>平台入口</h2>
        <p>Platform Portal</p>
      </div>
      <div class="roles-grid">
        <div v-for="role in roles" :key="role.id" class="role-card" @click="enterRole(role.id)">
          <div class="role-icon">{{ role.icon }}</div>
          <h3>{{ role.label }}</h3>
          <p>{{ role.desc }}</p>
          <span class="role-link">进入 →</span>
        </div>
      </div>
    </section>

    <!-- ===== 金融产品 ===== -->
    <section id="finance" class="section-wrap" style="background:#f8f9fa">
      <div class="section-title">
        <h2>金融产品</h2>
        <p>Financial Products</p>
      </div>
      <div class="finance-grid">
        <div v-for="(item, i) in financeProducts" :key="i" class="finance-card">
          <div class="bank-logo">{{ item.bank }}</div>
          <h3>{{ item.name }}</h3>
          <p>{{ item.desc }}</p>
          <div class="finance-money">¥{{ item.money }}</div>
          <p>在线申请 · 期限长 · 还款灵活</p>
          <button class="btn-primary" @click="router.push('/login')">我要贷款</button>
        </div>
      </div>
    </section>

    <!-- ===== 专家介绍 ===== -->
    <section id="expert" class="section-wrap">
      <div class="section-title">
        <h2>专家介绍</h2>
        <p>Expert Introduction</p>
      </div>
      <div class="expert-grid">
        <div v-for="(ex, i) in experts" :key="i" class="expert-card">
          <div class="expert-avatar">{{ ['👨‍🏫','👩‍🔬','👨‍🌾','🧑‍🔬','👩‍🏫'][i] }}</div>
          <div class="expert-actions">
            <button class="btn-chat">直接咨询</button>
            <button class="btn-order">线下预约</button>
          </div>
          <div class="expert-info">
            <p><strong>{{ ex.name }}</strong></p>
            <p>专业：{{ ex.specialty }}</p>
            <p>单位：{{ ex.org }}</p>
            <p>职称：{{ ex.title }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 专家问答 ===== -->
    <section id="qa" class="section-wrap" style="background:#f8f9fa">
      <div class="qa-layout">
        <div class="qa-sidebar">
          <h3>专家问答</h3>
          <div class="qa-side-icon">💬</div>
          <p>农业专家在线解答您的种植养殖问题</p>
          <button class="btn-sm-gray" @click="router.push('/login')">查看更多</button>
        </div>
        <div class="qa-list">
          <div v-for="(item, i) in qaItems" :key="i" class="qa-item">
            <h4>Q&A {{ item.q }}</h4>
            <p><strong>专家：{{ item.expert }}</strong><br>{{ item.a }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 农业知识 ===== -->
    <section id="knowledge" class="section-wrap">
      <div class="section-title">
        <h2>农业知识</h2>
        <p>Agricultural Knowledge</p>
      </div>
      <div class="knowledge-list">
        <div v-for="(item, i) in knowledgeItems" :key="i" class="knowledge-row">
          <div class="knowledge-text">
            <h3>｜{{ item.title }}</h3>
            <p>{{ item.content }}</p>
            <button class="btn-primary" @click="router.push('/login')">查看详情</button>
          </div>
          <div class="knowledge-img">
            <span>{{ ['🌾','🌽'][i] }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 助农电商 ===== -->
    <section id="shop" class="section-wrap" style="background:#f8f9fa">
      <div class="section-title">
        <h2>助农电商</h2>
        <p>Agricultural E-commerce</p>
      </div>
      <div class="shop-grid">
        <div v-for="(item, i) in shopItems" :key="i" class="shop-card">
          <div class="shop-img">{{ item.icon }}</div>
          <div class="shop-desc">{{ item.name }}</div>
          <div class="shop-price">{{ item.price }}</div>
        </div>
      </div>
    </section>

    <!-- ===== 底部栏 ===== -->
    <footer class="site-footer">
      <div class="footer-links">
        <a href="#">关于我们</a>
        <a href="#">联系方式</a>
        <a href="#">服务条款</a>
        <a href="#">隐私政策</a>
      </div>
      <p>融销通 — 农产品融销一体平台 &copy; 2026</p>
    </footer>
  </div>
</template>

<style scoped>
.home-page {
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  background: #fff;
}
/* 导航 */
.top-nav {
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-inner {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 40px;
}
.nav-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.logo-icon {
  font-size: 28px;
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #222;
}
.nav-menu {
  display: flex;
  gap: 36px;
}
.nav-menu a {
  text-decoration: none;
  color: #555;
  font-size: 15px;
  transition: color 0.2s;
}
.nav-menu a:hover {
  color: #488d37;
}
.nav-actions {
  display: flex;
  gap: 8px;
}

/* Banner */
.banner {
  position: relative;
  height: 480px;
  background: linear-gradient(135deg, #2d7a2a 0%, #488d37 30%, #5fa84e 60%, #3a7f30 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}
.banner-content {
  position: relative;
  text-align: center;
  color: #fff;
  z-index: 1;
  padding: 0 24px;
}
.banner-content h1 {
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.banner-tagline {
  font-size: 20px;
  margin-bottom: 8px;
  opacity: 0.95;
}
.banner-sub {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 32px;
}
.banner-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 通用区块 */
.section-wrap {
  padding: 50px 40px;
}
.section-title {
  text-align: center;
  margin-bottom: 32px;
}
.section-title h2 {
  font-size: 26px;
  color: #222;
  margin-bottom: 4px;
}
.section-title p {
  font-size: 13px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* 角色入口 */
.roles-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}
@media (max-width: 900px) { .roles-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 600px) { .roles-grid { grid-template-columns: repeat(2, 1fr); } }
.role-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 28px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s;
}
.role-card:hover {
  border-color: #488d37;
  box-shadow: 0 4px 16px rgba(72,141,55,0.12);
  transform: translateY(-3px);
}
.role-icon {
  font-size: 44px;
  margin-bottom: 12px;
}
.role-card h3 {
  font-size: 17px;
  font-weight: 600;
  color: #222;
  margin-bottom: 8px;
}
.role-card p {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin-bottom: 12px;
}
.role-link {
  font-size: 13px;
  color: #488d37;
  font-weight: 500;
}

/* 金融产品 */
.finance-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
@media (max-width: 900px) { .finance-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .finance-grid { grid-template-columns: 1fr; } }
.finance-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 24px 16px;
  text-align: center;
}
.bank-logo {
  font-size: 15px;
  font-weight: 700;
  color: #488d37;
  margin-bottom: 12px;
}
.finance-card h3 {
  font-size: 17px;
  margin-bottom: 8px;
  color: #222;
}
.finance-card p {
  font-size: 14px;
  color: #888;
  line-height: 1.6;
}
.finance-money {
  font-size: 24px;
  color: #488d37;
  font-weight: 700;
  margin: 8px 0;
}
.btn-primary {
  width: 100%;
  height: 38px;
  background: #488d37;
  color: #fff;
  border: none;
  border-radius: 4px;
  margin-top: 12px;
  cursor: pointer;
  font-size: 15px;
  transition: background 0.2s;
}
.btn-primary:hover {
  background: #59a044;
}

/* 专家 */
.expert-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}
@media (max-width: 900px) { .expert-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 600px) { .expert-grid { grid-template-columns: repeat(2, 1fr); } }
.expert-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}
.expert-avatar {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  background: #f0f7ee;
}
.expert-actions {
  display: flex;
}
.expert-actions button {
  flex: 1;
  height: 34px;
  border: none;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-chat {
  background: #488d37;
  color: #fff;
}
.btn-chat:hover { background: #59a044; }
.btn-order {
  background: #eee;
  color: #555;
}
.btn-order:hover { background: #ddd; }
.expert-info {
  padding: 10px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}
.expert-info strong {
  color: #333;
}

/* QA */
.qa-layout {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 30px;
}
@media (max-width: 800px) { .qa-layout { flex-direction: column; } }
.qa-sidebar {
  width: 240px;
  background: #488d37;
  color: #fff;
  padding: 24px 20px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.qa-sidebar h3 {
  font-size: 20px;
  margin-bottom: 16px;
}
.qa-side-icon {
  font-size: 56px;
  text-align: center;
  margin: 16px 0;
}
.qa-sidebar p {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 20px;
  opacity: 0.9;
}
.btn-sm-gray {
  background: rgba(255,255,255,0.2);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.3);
  padding: 8px 20px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}
.btn-sm-gray:hover { background: rgba(255,255,255,0.3); }
.qa-list {
  flex: 1;
}
.qa-item {
  margin-bottom: 24px;
  background: #fff;
  padding: 20px;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.qa-item h4 {
  color: #488d37;
  font-size: 16px;
  margin-bottom: 8px;
}
.qa-item p {
  font-size: 14px;
  color: #555;
  line-height: 1.7;
}

/* 农业知识 */
.knowledge-list {
  max-width: 1200px;
  margin: 0 auto;
}
.knowledge-row {
  display: flex;
  gap: 32px;
  margin-bottom: 32px;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
@media (max-width: 800px) { .knowledge-row { flex-direction: column; } }
.knowledge-text {
  flex: 1;
  padding: 24px;
}
.knowledge-text h3 {
  font-size: 18px;
  margin-bottom: 12px;
  color: #222;
}
.knowledge-text p {
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  margin-bottom: 16px;
}
.knowledge-img {
  width: 300px;
  min-height: 200px;
  background: #eef8e9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  flex-shrink: 0;
}
@media (max-width: 800px) { .knowledge-img { width: 100%; min-height: 120px; } }

/* 电商 */
.shop-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
@media (max-width: 900px) { .shop-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 600px) { .shop-grid { grid-template-columns: repeat(2, 1fr); } }
.shop-card {
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.shop-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.shop-img {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  background: #f8f9fa;
}
.shop-desc {
  padding: 8px 10px 2px;
  font-size: 14px;
  color: #444;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.shop-price {
  color: #f03030;
  font-weight: 700;
  padding: 0 10px 10px;
  font-size: 16px;
}

/* 底部 */
.site-footer {
  background: #488d37;
  color: rgba(255,255,255,0.85);
  text-align: center;
  padding: 24px 40px;
  font-size: 14px;
}
.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 12px;
}
.footer-links a {
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  font-size: 14px;
}
.footer-links a:hover {
  color: #fff;
}
.site-footer p {
  font-size: 13px;
  opacity: 0.75;
}
</style>
