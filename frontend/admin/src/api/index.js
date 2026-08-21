import request from './request'

export const userApi = {
  list: (params) => request.get('/user/list', { params }),
  getById: (id) => request.get(`/user/${id}`),
  create: (data) => request.post('/user', data),
  update: (data) => request.put('/user', data),
  delete: (id) => request.delete(`/user/${id}`),
  roles: () => request.get('/user/roles'),
  dashboardStats: () => request.get('/user/dashboard/stats'),
}

export const lawyerApi = {
  list: (params) => request.get('/user/lawyer/list', { params }),
  getByUserId: (userId) => request.get(`/user/lawyer/info/${userId}`),
  submit: (data) => request.post('/user/lawyer/submit', data),
  audit: (id, status) => request.post(`/user/lawyer/audit/${id}`, null, { params: { status } }),
}

export const enterpriseApi = {
  list: (params) => request.get('/user/enterprise/list', { params }),
  getByUserId: (userId) => request.get(`/user/enterprise/info/${userId}`),
  submit: (data) => request.post('/user/enterprise/submit', data),
  audit: (id, status) => request.post(`/user/enterprise/audit/${id}`, null, { params: { status } }),
}

export const documentApi = {
  categories: () => request.get('/document/categories'),
  templates: (params) => request.get('/document/templates', { params }),
  getTemplate: (id) => request.get(`/document/templates/${id}`),
  createTemplate: (data) => request.post('/document/templates', data),
  updateTemplate: (id, data) => request.put(`/document/templates/${id}`, data),
  toggleStatus: (id, status) => request.post(`/document/templates/${id}/status`, null, { params: { status } }),
  uploadFile: (formData) => request.post('/document/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
}

export const consultationApi = {
  list: (params) => request.get('/consultation/all', { params }),
  getById: (id) => request.get(`/consultation/${id}`),
  pending: (params) => request.get('/consultation/pending', { params }),
  answer: (id, lawyerId, answer) => request.post(`/consultation/${id}/answer`, null, { params: { lawyerId, answer } }),
}

export const caseApi = {
  search: (params) => request.get('/case/search', { params }),
  getById: (id) => request.get(`/case/${id}`),
}

export const paymentApi = {
  orders: (params) => request.get('/payment/admin/orders', { params }),
}

export const knowledgeApi = {
  categories: () => request.get('/case/knowledge/categories'),
  articles: (params) => request.get('/case/knowledge/articles', { params }),
  create: (data) => request.post('/case/knowledge/articles', data),
  update: (id, data) => request.put(`/case/knowledge/articles/${id}`, data),
  toggleStatus: (id, status) => request.post(`/case/knowledge/articles/${id}/status`, null, { params: { status } }),
}
export const feedbackApi = {
  list: (params) => request.get('/user/feedback/list', { params }),
  reply: (id, reply) => request.post(`/user/feedback/${id}/reply`, null, { params: { reply } }),
}
export const legalAidApi = {
  list: (params) => request.get('/consultation/legal-aid/list', { params }),
  getById: (id) => request.get(`/consultation/legal-aid/${id}`),
  audit: (id, status, remark, assignedLawyerId) => request.post(`/consultation/legal-aid/${id}/audit`, null, { params: { status, remark, assignedLawyerId } }),
}
export const reviewApi = {
  list: (params) => request.get('/user/review/list', { params }),
  toggleStatus: (id, status) => request.post(`/user/review/${id}/status`, null, { params: { status } }),
}
export const lawyerServiceApi = {
  lawyerCases: (params) => request.get('/consultation/lawyer-service/lawyer', { params }),
  accept: (id, remark) => request.post(`/consultation/lawyer-service/${id}/accept`, null, { params: { lawyerRemark: remark } }),
  finish: (id) => request.post(`/consultation/lawyer-service/${id}/finish`),
}
