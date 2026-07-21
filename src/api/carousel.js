import request from './index'

// 首页：获取轮播图（公开，按展示顺序）
export function getCarouselListAPI() {
  return request({
    url: '/api/v1/home-carousel/list',
    method: 'get'
  })
}

// 后台：新增轮播图 { imageUrl, link }
export function addCarouselAPI(data) {
  return request({
    url: '/api/v1/home-carousel',
    method: 'post',
    data
  })
}

// 后台：删除轮播图
export function deleteCarouselAPI(id) {
  return request({
    url: `/api/v1/home-carousel/${id}`,
    method: 'delete'
  })
}

// 后台：按 id 顺序重排 [id, ...]
export function reorderCarouselAPI(ids) {
  return request({
    url: '/api/v1/home-carousel/reorder',
    method: 'put',
    data: ids
  })
}
