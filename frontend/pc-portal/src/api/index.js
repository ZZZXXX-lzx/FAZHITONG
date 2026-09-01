import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
}

export const userApi = {
  getById: (id) => request.get(`/user/${id}`),
  list: (params) => request.get('/user/list', { params }),
  create: (data) => request.post('/user', data),
  update: (data) => request.put('/user', data),
  delete: (id) => request.delete(`/user/${id}`),
  roles: () => request.get('/user/roles'),
}

export const documentApi = {
  categories: () => request.get('/document/categories'),
  templates: (params) => request.get('/document/templates', { params }),
  getTemplate: (id) => request.get(`/document/templates/${id}`),
  generate: (params) => request.post('/document/generate', params.data, { params: { userId: params.userId, templateId: params.templateId } }),
  records: (userId) => request.get('/document/records', { params: { userId } }),
  aiDraft: (type, description) => request.post('/document/ai-draft', { type, description }),
}

export const consultationApi = {
  create: (params) => request.post('/consultation', null, { params }),
  answer: (id, lawyerId, answer) => request.post(`/consultation/${id}/answer`, null, { params: { lawyerId, answer } }),
  my: (userId, params) => request.get('/consultation/my', { params: { userId, ...params } }),
  lawyer: (lawyerId, params) => request.get('/consultation/lawyer', { params: { lawyerId, ...params } }),
  pending: (params) => request.get('/consultation/pending', { params }),
  all: (params) => request.get('/consultation/all', { params }),
  getById: (id) => request.get(`/consultation/${id}`),
}

export const contractApi = {
  upload: (params) => request.post('/contract/upload', null, { params }),
  review: (id, riskReport, riskLevel) => request.post(`/contract/${id}/review`, null, { params: { riskReport, riskLevel } }),
  records: (params) => request.get('/contract/records', { params }),
  enterpriseList: (params) => request.get('/contract/enterprise/list', { params }),
  createEnterprise: (data) => request.post('/contract/enterprise', data),
  aiReview: (text, dimension, userId, enterpriseId) => request.post('/contract/ai-review', { text, dimension, userId, enterpriseId }),
  transition: (id, action, signerName) => request.post(`/contract/enterprise/${id}/transition`, null, { params: { action, signerName } }),
  expiring: (enterpriseId, days) => request.get('/contract/enterprise/expiring', { params: { enterpriseId, days } }),
  ipList: (params) => request.get('/contract/ip/list', { params }),
  ipCreate: (data) => request.post('/contract/ip', data),
  ipUpdate: (data) => request.put('/contract/ip', data),
  ipDelete: (id) => request.delete(`/contract/ip/${id}`),
  ipExpiring: (enterpriseId, days) => request.get('/contract/ip/expiring', { params: { enterpriseId, days } }),
}

export const caseApi = {
  search: (params) => request.get('/case/search', { params }),
  getById: (id) => request.get(`/case/${id}`),
}

export const complianceApi = {
  questions: (enterpriseId) => request.get('/user/compliance/questions', { params: { enterpriseId } }),
  submitAnswers: (data) => request.post('/user/compliance/answers', data),
  report: (enterpriseId) => request.get('/user/compliance/report', { params: { enterpriseId } }),
}

export const regulationApi = {
  search: (params) => request.get('/case/regulation/search', { params }),
  getById: (id) => request.get(`/case/regulation/${id}`),
}

export const paymentApi = {
  createOrder: (params) => request.post('/payment/order', null, { params }),
  pay: (orderId) => request.post(`/payment/pay/${orderId}`),
  orders: (params) => request.get('/payment/orders', { params }),
  adminOrders: (params) => request.get('/payment/admin/orders', { params }),
  member: (userId) => request.get('/payment/member', { params: { userId } }),
}

export const dashboardApi = {
  stats: () => request.get('/user/dashboard/stats'),
}

export const knowledgeApi = {
  categories: () => request.get('/case/knowledge/categories'),
  articles: (params) => request.get('/case/knowledge/articles', { params }),
  getArticle: (id) => request.get(`/case/knowledge/articles/${id}`),
  retrieve: (query, topK) => request.post('/case/kb/retrieve', { query, topK }),
}
export const notificationApi = {
  list: (params) => request.get('/user/notification/list', { params }),
  unreadCount: (userId) => request.get('/user/notification/unread-count', { params: { userId } }),
  read: (id) => request.post(`/user/notification/read/${id}`),
  readAll: (userId) => request.post('/user/notification/read-all', null, { params: { userId } }),
}
export const reviewApi = {
  create: (data) => request.post('/user/review', data),
  list: (params) => request.get('/user/review/list', { params }),
  my: (params) => request.get('/user/review/my', { params }),
  summary: (params) => request.get('/user/review/summary', { params }),
}
export const favoriteApi = {
  add: (data) => request.post('/user/favorite', data),
  remove: (params) => request.delete('/user/favorite', { params }),
  list: (params) => request.get('/user/favorite/list', { params }),
  check: (params) => request.get('/user/favorite/check', { params }),
  count: (params) => request.get('/user/favorite/count', { params }),
}
export const feedbackApi = {
  submit: (data) => request.post('/user/feedback', data),
  my: (params) => request.get('/user/feedback/my', { params }),
}
export const legalAidApi = {
  apply: (data) => request.post('/consultation/legal-aid', data),
  my: (params) => request.get('/consultation/legal-aid/my', { params }),
  getById: (id) => request.get(`/consultation/legal-aid/${id}`),
}
export const lawyerServiceApi = {
  create: (data) => request.post('/consultation/lawyer-service', data),
  my: (params) => request.get('/consultation/lawyer-service/my', { params }),
  lawyerCases: (params) => request.get('/consultation/lawyer-service/lawyer', { params }),
  getById: (id) => request.get(`/consultation/lawyer-service/${id}`),
  accept: (id, remark) => request.post(`/consultation/lawyer-service/${id}/accept`, null, { params: { lawyerRemark: remark } }),
  finish: (id) => request.post(`/consultation/lawyer-service/${id}/finish`),
  cancel: (id) => request.post(`/consultation/lawyer-service/${id}/cancel`),
  lawyers: (params) => request.get('/consultation/lawyer-service/lawyers', { params }),
  prices: (lawyerId) => request.get(`/consultation/lawyer-service/prices/${lawyerId}`),
}
