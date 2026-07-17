import request from './index'

// 关注某农户（follower 取当前登录用户）
export function followAPI(followee) {
  return request({ url: `/api/v1/follow/${followee}`, method: 'post' })
}

// 取消关注
export function unfollowAPI(followee) {
  return request({ url: `/api/v1/follow/${followee}`, method: 'delete' })
}

// 查询当前用户是否已关注某农户
export function checkFollowAPI(followee) {
  return request({ url: '/api/v1/follow/check', method: 'get', params: { followee } })
}

// 查询某用户的粉丝数
export function getFansCountAPI(userName) {
  return request({ url: '/api/v1/follow/fans/count', method: 'get', params: { userName } })
}
