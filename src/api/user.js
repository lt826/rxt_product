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
