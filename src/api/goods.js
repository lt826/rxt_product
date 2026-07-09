import request from './index'

export function getGoodsListAPI(params) {
  return request({
    url: '/api/v1/goods/list',
    method: 'get',
    params
  })
}

export function getMyGoodsAPI(params) {
  return request({
    url: '/api/v1/goods/my',
    method: 'get',
    params
  })
}

export function getGoodsDetailAPI(goodsId) {
  return request({
    url: `/api/v1/goods/detail/${goodsId}`,
    method: 'get'
  })
}

export function publishGoodsAPI(data) {
  return request({
    url: '/api/v1/goods/publish',
    method: 'post',
    data
  })
}

export function updateGoodsAPI(goodsId, data) {
  return request({
    url: `/api/v1/goods/${goodsId}`,
    method: 'put',
    data
  })
}

export function deleteGoodsAPI(goodsId) {
  return request({
    url: `/api/v1/goods/${goodsId}`,
    method: 'delete'
  })
}

export function getPriceTrendAPI(plantName) {
  return request({
    url: '/api/v1/goods/price-trend',
    method: 'get',
    params: { plantName }
  })
}