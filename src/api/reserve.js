import request from './index'

// 农户发起专家出诊预约
export function applyReserve(data) {
  return request({ url: '/api/v1/reserve/apply', method: 'post', data })
}

// 农户查看自己的预约
export function listMyReserves() {
  return request({ url: '/api/v1/reserve/my', method: 'get' })
}

// 专家查看提问给自己的预约
export function listReservesForExpert() {
  return request({ url: '/api/v1/reserve/list', method: 'get' })
}

// 专家处理预约（answer 回复意向，status：1接受/2拒绝）
export function handleReserve(data) {
  return request({ url: '/api/v1/reserve/handle', method: 'put', data })
}
