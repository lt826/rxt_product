import request from './index'

export function listKnowledge(keyword) {
  return request({ url: '/api/v1/knowledge/list', method: 'get', params: { keyword } })
}

export function knowledgeDetail(id) {
  return request({ url: `/api/v1/knowledge/detail/${id}`, method: 'get' })
}

export function publishKnowledge(data) {
  return request({ url: '/api/v1/knowledge/publish', method: 'post', data })
}

export function updateKnowledge(data) {
  return request({ url: '/api/v1/knowledge/update', method: 'put', data })
}

export function deleteKnowledge(id) {
  return request({ url: `/api/v1/knowledge/delete/${id}`, method: 'delete' })
}

export function listMyKnowledge() {
  return request({ url: '/api/v1/knowledge/my', method: 'get' })
}

export function publishDiscuss(data) {
  return request({ url: '/api/v1/knowledge/discuss', method: 'post', data })
}

// 向后兼容函数名
export function getKnowledgeListAPI() {
  return listKnowledge()
}

export function getKnowledgeDetailAPI(id) {
  return knowledgeDetail(id)
}

export function getMyKnowledgeAPI() {
  return listMyKnowledge()
}

export function publishKnowledgeAPI(data) {
  return publishKnowledge(data)
}

export function deleteKnowledgeAPI(id) {
  return deleteKnowledge(id)
}

export function addDiscussAPI(data) {
  return publishDiscuss(data)
}

export function getDiscussesAPI(knowledgeId) {
  return request({ url: `/api/v1/knowledge/${knowledgeId}/discusses`, method: 'get' })
}