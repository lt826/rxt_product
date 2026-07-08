import request from './index'

export function loginAPI(data) {
  return request({
    url: '/api/v1/user/login',
    method: 'post',
    data
  })
}

export function registerAPI(data) {
  return request({
    url: '/api/v1/user/register',
    method: 'post',
    data
  })
}
