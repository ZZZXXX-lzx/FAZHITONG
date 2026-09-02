import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: () => import('@/views/layout/LayoutEntry.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/c/home/HomeEntry.vue'), meta: { title: '首页' } },
      { path: 'services', name: 'MobileServices', component: () => import('@/views/c/mobile/MobileServices.vue'), meta: { title: '全部服务' } },
      { path: 'mine', name: 'Mine', component: () => import('@/views/c/mobile/MinePage.vue'), meta: { title: '我的' } },
      { path: 'documents', name: 'Documents', component: () => import('@/views/c/documents/DocumentList.vue'), meta: { title: '文书生成' } },
      { path: 'documents/:id', name: 'DocumentDetail', component: () => import('@/views/c/documents/DocumentDetail.vue'), meta: { title: '文书详情' } },
      { path: 'consultation', name: 'Consultation', component: () => import('@/views/c/consultation/ConsultationPage.vue'), meta: { title: '法律咨询' } },
      { path: 'cases', name: 'Cases', component: () => import('@/views/c/cases/CaseSearch.vue'), meta: { title: '案例检索' } },
      { path: 'regulations', name: 'Regulations', component: () => import('@/views/c/cases/RegulationSearch.vue'), meta: { title: '法规检索' } },
      { path: 'templates', name: 'Templates', component: () => import('@/views/c/templates/TemplateList.vue'), meta: { title: '合同模板' } },
      { path: 'lawyer', name: 'LawyerHome', component: () => import('@/views/lawyer/home/LawyerHome.vue'), meta: { title: '律师工作台', roles: ['LAWYER', 'ADMIN'] } },
      { path: 'lawyer/templates', name: 'LawyerTemplates', component: () => import('@/views/lawyer/templates/LawyerTemplates.vue'), meta: { title: 'AI文书生成', roles: ['LAWYER', 'ADMIN'] } },
      { path: 'lawyer/profile', name: 'LawyerProfile', component: () => import('@/views/lawyer/profile/LawyerProfile.vue'), meta: { title: '律师资料', roles: ['LAWYER', 'ADMIN'] } },
      { path: 'lawyer/cases', name: 'LawyerCases', component: () => import('@/views/lawyer/cases/CaseManage.vue'), meta: { title: '案件管理', roles: ['LAWYER', 'ADMIN'] } },
      { path: 'lawyer/clients', name: 'LawyerClients', component: () => import('@/views/lawyer/clients/ClientManage.vue'), meta: { title: '客户管理', roles: ['LAWYER', 'ADMIN'] } },
      { path: 'enterprise', name: 'EnterpriseHome', component: () => import('@/views/enterprise/home/EnterpriseHome.vue'), meta: { title: '企业工作台', roles: ['ENTERPRISE', 'ADMIN'] } },
      { path: 'enterprise/contracts', name: 'EnterpriseContracts', component: () => import('@/views/enterprise/contracts/ContractManage.vue'), meta: { title: '合同管理', roles: ['ENTERPRISE', 'ADMIN'] } },
      { path: 'enterprise/compliance', name: 'EnterpriseCompliance', component: () => import('@/views/enterprise/compliance/CompliancePage.vue'), meta: { title: '合规体检', roles: ['ENTERPRISE', 'ADMIN'] } },
      { path: 'enterprise/ip', name: 'EnterpriseIp', component: () => import('@/views/enterprise/ip/IpManage.vue'), meta: { title: '知识产权管理', roles: ['ENTERPRISE', 'ADMIN'] } },
      { path: 'knowledge', name: 'Knowledge', component: () => import('@/views/c/knowledge/KnowledgePage.vue'), meta: { title: '法律知识库' } },
      { path: 'knowledge/:id', name: 'KnowledgeDetail', component: () => import('@/views/c/knowledge/KnowledgeDetail.vue'), meta: { title: '文章详情' } },
      { path: 'knowledge-graph', name: 'KnowledgeGraph', component: () => import('@/views/c/knowledge/KnowledgeGraph.vue'), meta: { title: '法律知识图谱' } },
      { path: 'lawyers', name: 'LawyerHall', component: () => import('@/views/c/lawyer/LawyerHall.vue'), meta: { title: '找律师' } },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/c/notification/NotificationPage.vue'), meta: { title: '消息中心', requiresAuth: true } },
      { path: 'toolbox', name: 'Toolbox', component: () => import('@/views/c/toolbox/ToolboxPage.vue'), meta: { title: '法律工具箱' } },
      { path: 'litigation', name: 'Litigation', component: () => import('@/views/c/toolbox/LitigationPage.vue'), meta: { title: '诉讼智能助手' } },
      { path: 'due-diligence', name: 'DueDiligence', component: () => import('@/views/c/toolbox/DueDiligencePage.vue'), meta: { title: '企业尽职调查' } },
      { path: 'feedback', name: 'Feedback', component: () => import('@/views/c/feedback/FeedbackPage.vue'), meta: { title: '意见反馈', requiresAuth: true } },
      { path: 'legal-aid', name: 'LegalAid', component: () => import('@/views/c/legal-aid/LegalAidPage.vue'), meta: { title: '法律援助', requiresAuth: true } },
      { path: 'lawyer-service', name: 'LawyerService', component: () => import('@/views/c/lawyer/LawyerServicePage.vue'), meta: { title: '律师委托', requiresAuth: true } },
    ],
  },
  { path: '/login', name: 'Login', component: () => import('@/views/LoginPage.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/RegisterPage.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 法智通` : '法智通 - 智能法律服务平台'

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return next('/login')
  }

  const roles = to.meta.roles
  if (roles) {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      return next('/login')
    }
    if (!roles.includes(userStore.userType)) {
      ElMessage.warning('您没有权限访问该页面')
      return next('/')
    }
  }

  next()
})

export default router
