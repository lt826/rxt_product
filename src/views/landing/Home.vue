<script setup>
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
</script>

<template>
  <div class="landing">
    <header class="landing-header">
      <div class="header-inner">
        <div class="logo">🌾 rxt</div>
        <el-button type="primary" @click="router.push('/login')">登录</el-button>
      </div>
    </header>

    <section class="hero">
      <h1>农业产融服务平台</h1>
      <p class="subtitle">连接农户、买家、银行与专家，让农业金融更高效</p>
    </section>

    <section class="roles">
      <div
        v-for="role in roles"
        :key="role.id"
        class="role-card"
        @click="enterRole(role.id)"
      >
        <div class="role-icon">{{ role.icon }}</div>
        <h3>{{ role.label }}</h3>
        <p>{{ role.desc }}</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.landing {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.landing-header {
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255,255,255,0.15);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
}
.logo {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}
:deep(.el-button--primary) {
  background: #fff;
  color: #764ba2;
  border: none;
}
:deep(.el-button--primary:hover) {
  opacity: 0.9;
}
.hero {
  text-align: center;
  padding: 80px 24px 40px;
}
.hero h1 {
  color: #fff;
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px;
}
.subtitle {
  color: rgba(255,255,255,0.85);
  font-size: 18px;
  margin: 0 0 48px;
}
.roles {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  padding: 0 24px 80px;
}
.role-card {
  background: rgba(255,255,255,0.95);
  border-radius: 12px;
  padding: 32px 20px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.role-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.15);
}
.role-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
.role-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}
.role-card p {
  font-size: 13px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
}
</style>
