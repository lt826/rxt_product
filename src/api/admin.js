import request from './index'

export function getUsersAPI() {
  return request({
    url: '/api/v1/admin/users',
    method: 'get'
  })
}

export function getUserDetailAPI(userName) {
  return request({
    url: `/api/v1/admin/users/${userName}`,
    method: 'get'
  })
}

export function createUserAPI(data) {
  return request({
    url: '/api/v1/admin/users',
    method: 'post',
    data
  })
}

export function updateUserAPI(userName, data) {
  return request({
    url: `/api/v1/admin/users/${userName}`,
    method: 'put',
    data
  })
}

export function deleteUserAPI(userName) {
  return request({
    url: `/api/v1/admin/users/${userName}`,
    method: 'delete'
  })
}

export function getStatsOverviewAPI() {
  return request({
    url: '/api/v1/admin/stats/overview',
    method: 'get'
  })
}

// 获取待审批用户列表
export function getPendingUsersAPI() {
  return request({
    url: '/api/v1/admin/users/pending',
    method: 'get'
  })
}

// 审批用户
export function approveUserAPI(userName, approved) {
  return request({
    url: `/api/v1/admin/users/${userName}/approve`,
    method: 'post',
    params: { approved }
  })
}
