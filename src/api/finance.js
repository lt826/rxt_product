import request from './index'

export function applyFinanceAPI(data) {
  return request({
    url: '/api/v1/finance/apply',
    method: 'post',
    data
  })
}

export function getMyFinancesAPI() {
  return request({
    url: '/api/v1/finance/list',
    method: 'get'
  })
}

export function getFinanceDetailAPI(financeId) {
  return request({
    url: `/api/v1/finance/${financeId}`,
    method: 'get'
  })
}

export function getRecommendPartnersAPI(amount, item) {
  return request({
    url: '/api/v1/finance/recommend-partners',
    method: 'get',
    params: { amount, item }
  })
}

// 获取银行融资产品列表
export function getFinancingProductsAPI() {
  return request({
    url: '/api/v1/finance/products',
    method: 'get'
  })
}

// ====== 融资意向 API ======
export function submitFinancingIntentionAPI(data) {
  return request({
    url: '/api/v1/financing-intention/submit',
    method: 'post',
    data
  })
}

export function getMyFinancingIntentionsAPI() {
  return request({
    url: '/api/v1/financing-intention/my',
    method: 'get'
  })
}

export function updateFinancingIntentionAPI(id, data) {
  return request({
    url: `/api/v1/financing-intention/${id}`,
    method: 'put',
    data
  })
}

export function deleteFinancingIntentionAPI(id) {
  return request({
    url: `/api/v1/financing-intention/${id}`,
    method: 'delete'
  })
}

// ====== 联合贷款人邀请 API ======
export function sendPartnerInvitationAPI(data) {
  return request({
    url: '/api/v1/partner-invitation/send',
    method: 'post',
    data
  })
}

export function getReceivedInvitationsAPI() {
  return request({
    url: '/api/v1/partner-invitation/received',
    method: 'get'
  })
}

export function getSentInvitationsAPI() {
  return request({
    url: '/api/v1/partner-invitation/sent',
    method: 'get'
  })
}

export function replyInvitationAPI(id, status, remark) {
  return request({
    url: `/api/v1/partner-invitation/${id}/reply`,
    method: 'put',
    data: { status, remark }
  })
}

export function clearReceivedInvitationsAPI() {
  return request({
    url: '/api/v1/partner-invitation/received',
    method: 'delete'
  })
}

export function clearSentInvitationsAPI() {
  return request({
    url: '/api/v1/partner-invitation/sent',
    method: 'delete'
  })
}

// ====== 智能匹配 API ======
// 使用 getRecommendPartnersAPI 作为智能匹配接口
export { getRecommendPartnersAPI as getMatchedPartnersAPI }

// ====== 银行端 API ======
export function getAllFinancesAPI() {
  return request({ url: '/api/v1/finance/bank/list', method: 'get' })
}

export function updateFinanceStatusAPI(id, status, remark) {
  return request({ url: `/api/v1/finance/${id}/status`, method: 'put', data: { status, remark } })
}

// ====== 银行端·融资产品配置（tb_financing_product）CRUD ======
// 查询全部融资产品
export function getFinancingProductListAPI() {
  return request({ url: '/api/v1/financing-product/list', method: 'get' })
}

// 新增融资产品
export function createFinancingProductAPI(data) {
  return request({ url: '/api/v1/financing-product/create', method: 'post', data })
}

// 修改融资产品
export function updateFinancingProductAPI(id, data) {
  return request({ url: `/api/v1/financing-product/${id}`, method: 'put', data })
}

// 删除融资产品
export function deleteFinancingProductAPI(id) {
  return request({ url: `/api/v1/financing-product/${id}`, method: 'delete' })
}

// 启用/停用融资产品（status: 1-启用 0-停用）
export function updateFinancingProductStatusAPI(id, status) {
  return request({ url: `/api/v1/financing-product/${id}/status`, method: 'put', data: status })
}

// ====== 银行端·融资审批匹配度 ======
// 融资审批列表（含联合贷款人、信誉分、匹配度）
export function getApprovalListAPI() {
  return request({ url: '/api/v1/finance/bank/approval-list', method: 'get' })
}

// 获取匹配规则权重
export function getMatchRuleAPI() {
  return request({ url: '/api/v1/match-rule', method: 'get' })
}

// 保存匹配规则权重
export function updateMatchRuleAPI(data) {
  return request({ url: '/api/v1/match-rule', method: 'put', data })
}
