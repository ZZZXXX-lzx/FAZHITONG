/**
 * 服务模块统一配置
 * roles 字段说明：
 *   - 不写 roles：通用服务，所有角色（含未登录）可见
 *   - roles: ['USER']：仅普通用户可见
 *   - roles: ['LAWYER']：仅律师可见
 *   - roles: ['ENTERPRISE']：仅企业可见
 * 管理员(ADMIN)始终可见所有服务。
 */
import {
  Document, ChatDotRound, Search, Notebook, DocumentChecked, Reading,
  User, Tools, ScaleToOriginal, OfficeBuilding, Connection, Umbrella,
  Postcard, EditPen, Files, CircleCheck, Folder, Coin,
} from '@element-plus/icons-vue'

/** 通用服务（所有角色可见） */
export const commonServices = [
  { icon: Document, color: '#1a56db', bg: '#e8effc', title: '文书生成', desc: 'AI智能生成起诉状、答辩状等法律文书', path: '/documents' },
  { icon: ChatDotRound, color: '#0d9488', bg: '#e0f4f1', title: '法律咨询', desc: '7×15小时在线咨询，5分钟快速响应', path: '/consultation' },
  { icon: Search, color: '#2563eb', bg: '#e6edfe', title: '案例检索', desc: '千万级裁判文书智能检索', path: '/cases' },
  { icon: Notebook, color: '#7c3aed', bg: '#f1e9fe', title: '法规检索', desc: '法律法规、司法解释快速查询', path: '/regulations' },
  { icon: DocumentChecked, color: '#b45309', bg: '#fbf0dd', title: '合同模板', desc: '海量合同模板一键套用', path: '/templates' },
  { icon: Reading, color: '#0e9f6e', bg: '#e2f6ec', title: '法律知识库', desc: '法律法规、法律常识、专业解读', path: '/knowledge' },
  { icon: Connection, color: '#0891b2', bg: '#e0f2f7', title: '法律知识图谱', desc: '领域、法规、概念关联关系可视化', path: '/knowledge-graph' },
  { icon: Tools, color: '#d97706', bg: '#fdf1dd', title: '法律工具箱', desc: '诉讼费、利息、工伤赔偿计算器', path: '/toolbox' },
  { icon: User, color: '#be185d', bg: '#fbe7f0', title: '找律师', desc: '认证律师大厅，按专长精准匹配', path: '/lawyers' },
]

/** 普通用户专属服务 */
export const userServices = [
  { icon: Umbrella, color: '#c2410c', bg: '#fdeee4', title: '法律援助', desc: '为经济困难群众提供免费法律服务', path: '/legal-aid', roles: ['USER'] },
  { icon: Postcard, color: '#0e9f6e', bg: '#e2f6ec', title: '律师委托', desc: '在线委托律师办理案件', path: '/lawyer-service', roles: ['USER'] },
]

/** 律师专属服务 */
export const lawyerServices = [
  { icon: ScaleToOriginal, color: '#7c3aed', bg: '#f1e9fe', title: '诉讼智能', desc: '案件分析、庭审提纲、判决预测', path: '/litigation', roles: ['LAWYER'] },
  { icon: EditPen, color: '#1a56db', bg: '#e8effc', title: '律师工作台', desc: 'AI辅助办案、文书生成', path: '/lawyer', roles: ['LAWYER'] },
  { icon: Folder, color: '#0d9488', bg: '#e0f4f1', title: '案件管理', desc: '承办案件全流程管理', path: '/lawyer/cases', roles: ['LAWYER'] },
  { icon: User, color: '#be185d', bg: '#fbe7f0', title: '客户管理', desc: '客户台账与跟进管理', path: '/lawyer/clients', roles: ['LAWYER'] },
]

/** 企业专属服务 */
export const enterpriseServices = [
  { icon: OfficeBuilding, color: '#0d9488', bg: '#e0f4f1', title: '尽职调查', desc: '企业尽调、风险清单、涉诉核查', path: '/due-diligence', roles: ['ENTERPRISE'] },
  { icon: Files, color: '#2563eb', bg: '#e6edfe', title: '合同管理', desc: '合同审批、归档、到期提醒', path: '/enterprise/contracts', roles: ['ENTERPRISE'] },
  { icon: CircleCheck, color: '#0e9f6e', bg: '#e2f6ec', title: '合规体检', desc: '企业合规风险智能检测', path: '/enterprise/compliance', roles: ['ENTERPRISE'] },
  { icon: OfficeBuilding, color: '#b45309', bg: '#fbf0dd', title: '知识产权', desc: '商标、专利、著作权台账管理', path: '/enterprise/ip', roles: ['ENTERPRISE'] },
  { icon: Coin, color: '#d97706', bg: '#fdf1dd', title: '投融资管理', desc: '融资轮次与对外投资台账', path: '/enterprise/investment', roles: ['ENTERPRISE'] },
  { icon: DocumentChecked, color: '#0891b2', bg: '#e0f2f7', title: '法律审核', desc: '合同、文件、合规事项审核', path: '/enterprise/legal-review', roles: ['ENTERPRISE'] },
]

/**
 * 根据用户类型过滤服务列表
 * @param {string} userType 用户类型 USER/LAWYER/ENTERPRISE/ADMIN
 * @param {boolean} isLoggedIn 是否已登录
 * @returns 过滤后的服务列表
 */
export function filterServices(list, userType, isLoggedIn) {
  const t = userType || ''
  return list.filter(s => {
    if (!s.roles) return true
    if (t === 'ADMIN') return true
    if (!isLoggedIn) return false
    return s.roles.includes(t)
  })
}

/** 获取某角色可见的全部服务（通用 + 专属） */
export function allServicesFor(userType, isLoggedIn) {
  const all = [
    ...commonServices,
    ...userServices,
    ...lawyerServices,
    ...enterpriseServices,
  ]
  return filterServices(all, userType, isLoggedIn)
}
