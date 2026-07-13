import request from './index'

// 查询当前用户全部地址
export function listAddressAPI() {
  return request({ url: '/api/v1/address/list', method: 'get' })
}

// 新增地址
export function addAddressAPI(data) {
  return request({ url: '/api/v1/address', method: 'post', data })
}

// 更新地址
export function updateAddressAPI(data) {
  return request({ url: '/api/v1/address', method: 'put', data })
}

// 删除地址
export function deleteAddressAPI(id) {
  return request({ url: `/api/v1/address/${id}`, method: 'delete' })
}
