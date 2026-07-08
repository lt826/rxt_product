import request from './index'

// 农户向指定专家提问
export function askQuestion(data) {
  return request({ url: '/api/v1/question/ask', method: 'post', data })
}

// 专家查看提问给自己的问题列表
export function listQuestionsForExpert() {
  return request({ url: '/api/v1/question/list', method: 'get' })
}

// 农户查看自己的提问记录
export function listMyQuestions() {
  return request({ url: '/api/v1/question/my', method: 'get' })
}

// 专家回复问题
export function answerQuestion(data) {
  return request({ url: '/api/v1/question/answer', method: 'put', data })
}
