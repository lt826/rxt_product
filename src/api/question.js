import request from './index'

// ======【合并保留】农民向指定专家提问 ======
export function askQuestion(data) {
  return request({ url: '/api/v1/question/ask', method: 'post', data })
}

// ======【合并保留】专家查看提问给自己的问题列表 ======
export function listQuestionsForExpert() {
  return request({ url: '/api/v1/question/list', method: 'get' })
}

// ======【合并保留】农民查看自己的提问记录 ======
export function listMyQuestions() {
  return request({ url: '/api/v1/question/my', method: 'get' })
}

// ======【合并保留】专家回复问题 ======
export function answerQuestion(data) {
  return request({ url: '/api/v1/question/answer', method: 'put', data })
}

// ======【农户端用】向后兼容的函数名 ======
export function askQuestionAPI(data) {
  return askQuestion(data)
}

export function getMyQuestionsAPI() {
  return listMyQuestions()
}

export function getQuestionDetailAPI(id) {
  return request({ url: `/api/v1/question/${id}`, method: 'get' })
}


