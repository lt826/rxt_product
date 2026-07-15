import request from './index'

// 买家下单
export function createOrderAPI(data) {
  return request({ url: '/api/v1/orders', method: 'post', data })
}

// 我买到的订单
export function getBoughtOrdersAPI() {
  return request({ url: '/api/v1/orders/bought', method: 'get' })
}

// 我卖出的订单
export function getSoldOrdersAPI() {
  return request({ url: '/api/v1/orders/sold', method: 'get' })
}

// 农户发货（填物流信息）
export function shipOrderAPI(id, data) {
  return request({ url: `/api/v1/orders/${id}/ship`, method: 'put', data })
}

// 买家确认收货
export function receiveOrderAPI(id) {
  return request({ url: `/api/v1/orders/${id}/receive`, method: 'put' })
}
