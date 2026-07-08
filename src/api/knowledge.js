import request from './index'

// ======【合并保留】查询所有农业知识 ======
export function listKnowledge(keyword) {
  return request({ url: '/api/v1/knowledge/list', method: 'get', params: { keyword } })
}

// ======【合并保留】知识详情 ======
export function knowledgeDetail(id) {
  return request({ url: `/api/v1/knowledge/${id}`, method: 'get' })
}

// ======【合并保留】专家发布新文章 ======
export function publishKnowledge(data) {
  return request({ url: '/api/v1/knowledge/publish', method: 'post', data })
}

// ======【合并保留】专家修改自己的文章 ======
export function updateKnowledge(data) {
  return request({ url: '/api/v1/knowledge/update', method: 'put', data })
}

// ======【合并保留】专家删除自己的文章 ======
export function deleteKnowledge(id) {
  return request({ url: `/api/v1/knowledge/delete/${id}`, method: 'delete' })
}

// ======【合并保留】专家查看自己发布的文章 ======
export function listMyKnowledge() {
  return request({ url: '/api/v1/knowledge/my', method: 'get' })
}

// ======【合并保留】发表知识评论 ======
export function publishDiscuss(data) {
  return request({ url: '/api/v1/knowledge/discuss', method: 'post', data })
}

// ======【农户端用】向后兼容的函数名 ======
export function getKnowledgeListAPI() {
  return listKnowledge()
}

export function getKnowledgeDetailAPI(id) {
  return knowledgeDetail(id)
}

export function addDiscussAPI(data) {
  return publishDiscuss(data)
}

export function getDiscussesAPI(knowledgeId) {
  return request({ url: `/api/v1/knowledge/${knowledgeId}/discusses`, method: 'get' })
}
