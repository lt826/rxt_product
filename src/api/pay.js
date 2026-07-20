import request from './index'

// 生成支付宝扫码支付二维码（预下单）
export function getPayQrcodeAPI(purchaseId) {
  return request({
    url: '/api/v1/pay/qrcode',
    method: 'get',
    params: { purchaseId }
  })
}

// 轮询查询支付状态（扫码支付后自动检测）
export function queryPayStatusAPI(purchaseId, outTradeNo) {
  return request({
    url: '/api/v1/pay/query',
    method: 'get',
    params: { purchaseId, outTradeNo }
  })
}

// 手动「已完成支付」（demo 友好，不依赖真实支付）
export function confirmPayAPI(purchaseId) {
  return request({
    url: `/api/v1/pay/confirm/${purchaseId}`,
    method: 'post'
  })
}
