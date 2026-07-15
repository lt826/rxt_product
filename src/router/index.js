import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/landing/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/landing/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/landing/Register.vue')
  },
  // 农户端 Farmer
  {
    path: '/farmer',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'farmer', title: '农户端', requiresAuth: true },
    redirect: '/farmer/dashboard',
    children: [
      { path: 'dashboard', name: 'FarmerDashboard', component: () => import('../views/farmer/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'financing', name: 'FarmerFinancing', component: () => import('../views/farmer/Financing.vue'), meta: { title: '融资申请' } },
      { path: 'financing-intention', name: 'FarmerFinancingIntention', component: () => import('../views/farmer/FinancingIntention.vue'), meta: { title: '融资意向' } },
      { path: 'partner-invitations', name: 'FarmerPartnerInvitations', component: () => import('../views/farmer/PartnerInvitations.vue'), meta: { title: '联合贷款邀请' } },
      { path: 'smart-match', name: 'FarmerSmartMatch', component: () => import('../views/farmer/SmartMatch.vue'), meta: { title: '智能匹配' } },
      { path: 'products', name: 'FarmerProducts', component: () => import('../views/farmer/Products.vue'), meta: { title: '商品货源' } },
      { path: 'demands', name: 'FarmerDemands', component: () => import('../views/farmer/Demands.vue'), meta: { title: '求购大厅' } },
      { path: 'knowledge', name: 'FarmerKnowledge', component: () => import('../views/farmer/Knowledge.vue'), meta: { title: '农业知识' } },
      { path: 'expert-qa', name: 'FarmerExpertQA', component: () => import('../views/farmer/ExpertQA.vue'), meta: { title: '专家问答' } },
      { path: 'appointments', name: 'FarmerAppointments', component: () => import('../views/farmer/Appointments.vue'), meta: { title: '专家预约' } },
      { path: 'messages', name: 'FarmerMessages', component: () => import('../views/farmer/Messages.vue'), meta: { title: '消息通知' } },
      { path: 'orders', name: 'FarmerOrders', component: () => import('../views/farmer/Orders.vue'), meta: { title: '我的订单' } }
    ]
  },
  // 买家端 Buyer
  {
    path: '/buyer',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'buyer', title: '买家端', requiresAuth: true },
    redirect: '/buyer/dashboard',
    children: [
      { path: 'dashboard', name: 'BuyerDashboard', component: () => import('../views/buyer/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'products', name: 'BuyerProducts', component: () => import('../views/buyer/Products.vue'), meta: { title: '商品采购' } },
      { path: 'goods/:id', name: 'BuyerGoodsDetail', component: () => import('../views/buyer/GoodsDetail.vue'), meta: { title: '商品详情' } },
      { path: 'cart', name: 'BuyerCart', component: () => import('../views/buyer/Cart.vue'), meta: { title: '购物车' } },
      { path: 'checkout', name: 'BuyerCheckout', component: () => import('../views/buyer/Checkout.vue'), meta: { title: '订单结算' } },
      { path: 'demand', name: 'BuyerDemand', component: () => import('../views/buyer/Demand.vue'), meta: { title: '收购需求' } },
      { path: 'contacts', name: 'BuyerContacts', component: () => import('../views/buyer/Contacts.vue'), meta: { title: '联系农户' } },
      { path: 'messages', name: 'BuyerMessages', component: () => import('../views/buyer/Messages.vue'), meta: { title: '买家消息' } },
      { path: 'orders', name: 'BuyerOrders', component: () => import('../views/buyer/Orders.vue'), meta: { title: '我的订单' } }
    ]
  },
  // 银行端 Bank
  {
    path: '/bank',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { role: 'bank', title: '银行端', requiresAuth: true },
    redirect: '/bank/dashboard',
    children: [
      { path: 'dashboard', name: 'BankDashboard', component: () => import('../views/bank/Dashboard.vue'), meta: { title: '首页' } },
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
      { path: 'dashboard', name: 'ExpertDashboard', component: () => import('../views/expert/Dashboard.vue'), meta: { title: '首页' } },
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
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue'), meta: { title: '用户管理' } },
      { path: 'goods', name: 'AdminGoods', component: () => import('../views/admin/Goods.vue'), meta: { title: '商品管理' } },
      { path: 'config', name: 'AdminConfig', component: () => import('../views/admin/Config.vue'), meta: { title: '系统配置' } },
      { path: 'stats', name: 'AdminStats', component: () => import('../views/admin/Stats.vue'), meta: { title: '数据统计' } }
    ]
  },
  // 个人中心 Profile —— 嵌在各角色端的 RoleLayout 内，顶部导航栏始终可见
  {
    path: '/profile',
    component: () => import('../layouts/RoleLayout.vue'),
    meta: { title: '个人中心', requiresAuth: true },
    redirect: '/profile/center',
    children: [
      {
        // 只保留侧栏 + 内容区，顶栏由 RoleLayout 提供
        path: '',
        component: () => import('../layouts/ProfileLayout.vue'),
        children: [
          { path: 'center', name: 'ProfileCenter', component: () => import('../views/profile/Center.vue'), meta: { title: '个人中心' } },
      { path: 'basic', name: 'ProfileBasic', component: () => import('../views/profile/BasicInfo.vue'), meta: { title: '基本信息' } },
      { path: 'address', name: 'ProfileAddress', component: () => import('../views/profile/Address.vue'), meta: { title: '收货地址' } },
      { path: 'password', name: 'ProfilePassword', component: () => import('../views/profile/Password.vue'), meta: { title: '密码管理' } },
      { path: 'my-products', name: 'ProfileMyProducts', component: () => import('../views/profile/MyProducts.vue'), meta: { title: '商品货源管理' } },
      { path: 'my-demands', name: 'ProfileMyDemands', component: () => import('../views/profile/MyDemands.vue'), meta: { title: '求购需求管理' } },
      { path: 'my-financing', name: 'ProfileMyFinancing', component: () => import('../views/profile/MyFinancing.vue'), meta: { title: '融资管理' } },
      { path: 'my-qa', name: 'ProfileMyQA', component: () => import('../views/profile/MyQA.vue'), meta: { title: '问答管理' } },
      { path: 'my-appointments', name: 'ProfileMyAppointments', component: () => import('../views/profile/MyAppointments.vue'), meta: { title: '预约管理' } },
      { path: 'my-knowledge', name: 'ProfileMyKnowledge', component: () => import('../views/profile/MyKnowledge.vue'), meta: { title: '知识管理' } },
      { path: 'expert-info', name: 'ProfileExpertInfo', component: () => import('../views/profile/ExpertInfo.vue'), meta: { title: '专家信息' } },
      { path: 'bank/match', name: 'ProfileBankMatch', component: () => import('../views/profile/BankMatch.vue'), meta: { title: '智能匹配' } },
      { path: 'bank/approval', name: 'ProfileBankApproval', component: () => import('../views/profile/BankApproval.vue'), meta: { title: '贷款审批' } }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由导航守卫
const VALID_ROLES = ['farmer', 'buyer', 'bank', 'expert', 'admin']
const homePath = (role) => `/${role}/dashboard`

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('rxt_token')
  let userInfo = null
  try {
    userInfo = JSON.parse(localStorage.getItem('rxt_user') || 'null')
  } catch (e) {
    userInfo = null
  }
  const role = userInfo && userInfo.role ? String(userInfo.role).toLowerCase() : null
  const isValidRole = role && VALID_ROLES.includes(role)
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  if (requiresAuth && !token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (token && !isValidRole && (requiresAuth || to.path === '/login')) {
    localStorage.removeItem('rxt_token')
    localStorage.removeItem('rxt_user')
    if (to.path === '/login') return next()
    return next({ path: '/login' })
  }

  if (to.path === '/login' && isValidRole) {
    return next({ path: homePath(role) })
  }

  if (isValidRole && to.meta.role && to.meta.role !== role) {
    return next({ path: homePath(role) })
  }

  next()
})

export default router
