import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/landing/Home.vue'),
    meta: { layout: 'blank' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/landing/Login.vue'),
    meta: { layout: 'blank' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/landing/Register.vue'),
    meta: { layout: 'blank' }
  },
  // 农户端 Farmer
  {
    path: '/farmer',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'farmer', title: '农户端', requiresAuth: true },
    redirect: '/farmer/dashboard',
    children: [
      { path: 'dashboard', name: 'FarmerDashboard', component: () => import('../views/farmer/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'financing', name: 'FarmerFinancing', component: () => import('../views/farmer/Financing.vue'), meta: { title: '融资申请' } },
      { path: 'products', name: 'FarmerProducts', component: () => import('../views/farmer/Products.vue'), meta: { title: '商品浏览' } },
      { path: 'knowledge', name: 'FarmerKnowledge', component: () => import('../views/farmer/Knowledge.vue'), meta: { title: '农业知识' } },
      { path: 'expert-qa', name: 'FarmerExpertQA', component: () => import('../views/farmer/ExpertQA.vue'), meta: { title: '专家问答' } },
      { path: 'appointments', name: 'FarmerAppointments', component: () => import('../views/farmer/Appointments.vue'), meta: { title: '专家预约' } },
      { path: 'messages', name: 'FarmerMessages', component: () => import('../views/farmer/Messages.vue'), meta: { title: '消息通知' } }
    ]
  },
  // 买家端 Buyer
  {
    path: '/buyer',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'buyer', title: '买家端', requiresAuth: true },
    redirect: '/buyer/dashboard',
    children: [
      { path: 'dashboard', name: 'BuyerDashboard', component: () => import('../views/buyer/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'products', name: 'BuyerProducts', component: () => import('../views/buyer/Products.vue'), meta: { title: '商品管理' } },
      { path: 'demand', name: 'BuyerDemand', component: () => import('../views/buyer/Demand.vue'), meta: { title: '收购需求' } },
      { path: 'contacts', name: 'BuyerContacts', component: () => import('../views/buyer/Contacts.vue'), meta: { title: '联系农户' } }
    ]
  },
  // 银行端 Bank
  {
    path: '/bank',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'bank', title: '银行端', requiresAuth: true },
    redirect: '/bank/dashboard',
    children: [
      { path: 'dashboard', name: 'BankDashboard', component: () => import('../views/bank/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'approvals', name: 'BankApprovals', component: () => import('../views/bank/Approvals.vue'), meta: { title: '融资审批' } },
      { path: 'products', name: 'BankProducts', component: () => import('../views/bank/Products.vue'), meta: { title: '融资产品配置' } },
      { path: 'match-rules', name: 'BankMatchRules', component: () => import('../views/bank/MatchRules.vue'), meta: { title: '智能匹配规则' } }
    ]
  },
  // 专家端 Expert
  {
    path: '/expert',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'expert', title: '专家端', requiresAuth: true },
    redirect: '/expert/dashboard',
    children: [
      { path: 'dashboard', name: 'ExpertDashboard', component: () => import('../views/expert/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'knowledge', name: 'ExpertKnowledge', component: () => import('../views/expert/Knowledge.vue'), meta: { title: '知识管理' } },
      { path: 'qa', name: 'ExpertQA', component: () => import('../views/expert/QA.vue'), meta: { title: '问答处理' } },
      { path: 'appointments', name: 'ExpertAppointments', component: () => import('../views/expert/Appointments.vue'), meta: { title: '预约处理' } }
    ]
  },
  // 平台管理端 Admin
  {
    path: '/admin',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'admin', title: '平台管理端', requiresAuth: true },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue'), meta: { title: '用户管理' } },
      { path: 'config', name: 'AdminConfig', component: () => import('../views/admin/Config.vue'), meta: { title: '系统配置' } },
      { path: 'stats', name: 'AdminStats', component: () => import('../views/admin/Stats.vue'), meta: { title: '数据统计' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由导航守卫：需要登录才能访问的页面自动跳转到登录页
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('rxt_token')
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    // 如果路由指定了角色，校验用户角色是否匹配
    if (to.meta.role) {
      const userInfo = JSON.parse(localStorage.getItem('rxt_user') || 'null')
      if (userInfo && userInfo.role !== to.meta.role) {
        next({ path: `/${userInfo.role}/dashboard` })
        return
      }
    }
  }
  next()
})

export default router
