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

// ====== 银行端 API ======
export function getAllFinancesAPI() {
  return request({ url: '/api/v1/finance/bank/list', method: 'get' })
}

export function updateFinanceStatusAPI(id, status, remark) {
  return request({ url: `/api/v1/finance/${id}/status`, method: 'put', data: { status, remark } })
}
