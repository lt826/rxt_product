import request from './index'

// 首页：获取各板块精选内容（公开）
export function getHomeFeaturedAPI() {
  return request({
    url: '/api/v1/homepage-feature/list',
    method: 'get'
  })
}

// 后台：获取某板块候选内容与已选配置
export function getFeatureSectionAPI(section) {
  return request({
    url: '/api/v1/homepage-feature/section',
    method: 'get',
    params: { section }
  })
}

// 后台：整体保存某板块的精选内容
export function saveFeatureSectionAPI(section, items) {
  return request({
    url: `/api/v1/homepage-feature/section/${section}`,
    method: 'put',
    data: items
  })
}
