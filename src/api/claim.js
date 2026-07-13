import request from './index'

// 接取一条求购需求
export function claimAPI(data) {
  return request({
    url: '/api/v1/demand-claim/claim',
    method: 'post',
    data
  })
}

// 查询某条求购需求的接取者列表（发布者查看用）
export function getClaimsByDemandAPI(demandId) {
  return request({
    url: `/api/v1/demand-claim/by-demand/${demandId}`,
    method: 'get'
  })
}

// 查询当前用户已接取的需求记录（求购大厅标"已接取"用）
export function getMyClaimsAPI() {
  return request({
    url: '/api/v1/demand-claim/my-claims',
    method: 'get'
  })
}
