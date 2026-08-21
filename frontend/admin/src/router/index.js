import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/store/admin'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginPage.vue') },
  { path: '/', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardPage.vue'), meta: { requiresAuth: true, title: '数据看板' } },
  { path: '/users', name: 'Users', component: () => import('@/views/user/UserList.vue'), meta: { requiresAuth: true, title: '用户列表' } },
  { path: '/lawyers', name: 'Lawyers', component: () => import('@/views/user/LawyerManage.vue'), meta: { requiresAuth: true, title: '律师管理' } },
  { path: '/enterprises', name: 'Enterprises', component: () => import('@/views/user/EnterpriseManage.vue'), meta: { requiresAuth: true, title: '企业管理' } },
  { path: '/content', name: 'Content', component: () => import('@/views/content/ContentManage.vue'), meta: { requiresAuth: true, title: '内容管理' } },
  { path: '/knowledge-manage', name: 'KnowledgeManage', component: () => import('@/views/content/KnowledgeManage.vue'), meta: { requiresAuth: true, title: '知识库管理' } },
  { path: '/finance', name: 'Finance', component: () => import('@/views/finance/FinancePage.vue'), meta: { requiresAuth: true, title: '财务管理' } },
  { path: '/system/roles', name: 'Roles', component: () => import('@/views/system/RoleManage.vue'), meta: { requiresAuth: true, title: '角色管理' } },
  { path: '/system/config', name: 'Config', component: () => import('@/views/system/SystemConfig.vue'), meta: { requiresAuth: true, title: '系统配置' } },
  { path: '/system/logs', name: 'Logs', component: () => import('@/views/system/OperationLogs.vue'), meta: { requiresAuth: true, title: '操作日志' } },
  { path: '/system/feedback', name: 'FeedbackManage', component: () => import('@/views/system/FeedbackManage.vue'), meta: { requiresAuth: true, title: '反馈管理' } },
  { path: '/system/legal-aid', name: 'LegalAidManage', component: () => import('@/views/system/LegalAidManage.vue'), meta: { requiresAuth: true, title: '法律援助审核' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()
  document.title = to.meta.title ? `${to.meta.title} - 法保通管理后台` : '法保通管理后台'
  if (to.meta.requiresAuth && !adminStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && adminStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
