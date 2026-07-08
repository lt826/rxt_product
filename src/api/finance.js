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
