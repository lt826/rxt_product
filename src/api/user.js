import request from './index'

// 获取用户信息
export function getUserInfoAPI(userName) {
  return request({
    url: '/api/v1/user/info',
    method: 'get',
    params: { userName }
  })
}

// 更新用户信息
export function updateUserInfoAPI(data) {
  return request({
    url: '/api/v1/user/update',
    method: 'put',
    data
  })
}

// 修改密码
export function changePasswordAPI(data) {
  return request({
    url: '/api/v1/user/password',
    method: 'put',
    data
  })
}

// 获取所有农户列表（买家端用）
export function getFarmersAPI() {
  return request({
    url: '/api/v1/farmers',
    method: 'get'
  })
}
