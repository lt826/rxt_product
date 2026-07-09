import request from './index'

// 查询全部专家（供农户提问/预约时选择）
export function listExperts() {
  return request({ url: '/api/v1/expert/list', method: 'get' })
}

// 向后兼容函数名
export function getExpertListAPI() {
  return listExperts()
}
