import request from './index'

// ======【合并保留】农民发起专家出诊预约 ======
export function applyReserve(data) {
  return request({ url: '/api/v1/reserve/apply', method: 'post', data })
}

// ======【合并保留】农民查看自己的预约 ======
export function listMyReserves() {
  return request({ url: '/api/v1/reserve/my', method: 'get' })
}

// ======【合并保留】专家查看提问给自己的预约 ======
export function listReservesForExpert() {
  return request({ url: '/api/v1/reserve/list', method: 'get' })
}

// ======【合并保留】专家处理预约 ======
export function handleReserve(data) {
  return request({ url: '/api/v1/reserve/handle', method: 'put', data })
}

// ======【农户端用】向后兼容的函数名 ======
export function applyReserveAPI(data) {
  return applyReserve(data)
}

export function getMyReservesAPI() {
  return listMyReserves()
}

export function getReserveDetailAPI(id) {
  return request({ url: `/api/v1/reserve/${id}`, method: 'get' })
}


