import request from './index'

// 查询所有农业知识（农户可看），可选关键字
export function listKnowledge(keyword) {
  return request({ url: '/api/v1/knowledge/list', method: 'get', params: { keyword } })
}

// 知识详情（含评论列表）
export function knowledgeDetail(id) {
  return request({ url: `/api/v1/knowledge/detail/${id}`, method: 'get' })
}

// 专家发布新文章
export function publishKnowledge(data) {
  return request({ url: '/api/v1/knowledge/publish', method: 'post', data })
}

// 专家修改自己的文章
export function updateKnowledge(data) {
  return request({ url: '/api/v1/knowledge/update', method: 'put', data })
}

// 专家删除自己的文章
export function deleteKnowledge(id) {
  return request({ url: `/api/v1/knowledge/delete/${id}`, method: 'delete' })
}

// 专家查看自己发布的文章
export function listMyKnowledge() {
  return request({ url: '/api/v1/knowledge/my', method: 'get' })
}

// 发表知识评论
export function publishDiscuss(data) {
  return request({ url: '/api/v1/knowledge/discuss', method: 'post', data })
}
